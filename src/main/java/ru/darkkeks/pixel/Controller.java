package ru.darkkeks.pixel;

import ru.darkkeks.pixel.graphics.BoardGraphics;
import ru.darkkeks.pixel.graphics.Template;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.http.HttpClient;
import java.util.Set;
import java.util.concurrent.*;

public class Controller {

    private final HttpClient httpClient;
    private Template template;
    private final PixelQueue queue;
    private PixelAccount observer;

    private BoardGraphics graphics;

    private Set<PixelAccount> accounts;
    private ScheduledThreadPoolExecutor executor;

    private HealthCheck healthCheck;
    private int lastMinuteStats;

    private BlockingQueue<Integer> speedQueue;
    
    private static boolean needQueueRebuild = false;

    public Controller(LoginCredentials observerCredentials, Template template) {
        this.template = template;
        this.accounts = ConcurrentHashMap.newKeySet();

        this.speedQueue = new ArrayBlockingQueue<>(128);

        executor = new ScheduledThreadPoolExecutor(24);
        graphics = new BoardGraphics(new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB));
        graphics.setTemplate(template);
        httpClient = HttpClient.newHttpClient();

        healthCheck = new HealthCheck();
        queue = new PixelQueue(template);

        startTicker();

        addAccount(observerCredentials).thenAccept(account -> {
            observer = account;
            hookObserver();
        });

        runBot();
    }

    private void runBot() {
        executor.scheduleAtFixedRate(() -> {
            String output = String.format("Accounts active: %5d, queue size: %5d", accounts.size(), queue.size());
            
            if (needQueueRebuild) {
                queue.rebuild(graphics.getImage());
            }

            speedQueue.offer(queue.size());
            if(speedQueue.size() > 60) {
                int prev = speedQueue.poll();
                output += String.format(", current speed: %5.2f pixels/second", (prev - queue.size()) / 60d);
            }

            System.out.println(output);

            try {
                accounts.forEach(account -> {
                    if (account.canPlace()) {
                        if (queue.size() > 0) {
                            PixelQueue.Point point = queue.pop();
                            Color color = template.getColorAbs(point.getX(), point.getY());
                            System.out.println("Placing pixel x=" + point.getX() + ", y=" + point.getY());
                            Pixel pixel = Pixel.place(point.getX(), point.getY(), color);
                            CompletableFuture.supplyAsync(() -> {
                                healthCheck.onPlace(pixel, account.getLoginSignature());
                                try {
                                    return account.sendPixel(pixel).get();
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }, executor).thenRun(() -> {
                                executor.schedule(() -> {
                                    if (healthCheck.checkHealth(pixel)) {
                                        lastMinuteStats++;
                                    }
                                }, 10, TimeUnit.SECONDS);
                            });
                        }
                    }
                });
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);

        executor.scheduleAtFixedRate(() -> {
            System.out.println("Rebuilding queue");
            queue.rebuild(graphics.getImage());
        }, 0, 30, TimeUnit.SECONDS);

        executor.scheduleAtFixedRate(() -> {
            System.out.println("Placed during last minute: " + lastMinuteStats);
            lastMinuteStats = 0;
        }, 0, 60, TimeUnit.SECONDS);
    }

    private void startTicker() {
        executor.scheduleAtFixedRate(() -> {
            accounts.forEach(PixelAccount::tick);
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void reconnect(PixelAccount account) {
        accounts.remove(account);
        executor.schedule(() -> {
            addAccount(account.getLoginSignature()).thenAccept(newAccount -> {
                if (account == observer) {
                    observer = newAccount;
                    hookObserver();
                }
            });
        }, 5, TimeUnit.SECONDS);
    }

    public CompletableFuture<PixelAccount> addAccount(LoginCredentials credentials) {
        PixelAccount account = new PixelAccount(credentials, httpClient, this::reconnect);
        return account.start().thenApply(v -> {
            accounts.add(account);
            return account;
        });
    }

    private void hookObserver() {
        observer.getPixelApi().data().thenAccept(board -> {
            graphics.updateBoard(board);
            queue.rebuild(board);
        });

        observer.setPixelConsumer(pixel -> {
            healthCheck.onPixel(pixel);
            queue.onPixelChange(pixel);
            if (graphics != null) {
                graphics.setPixel(pixel.getX(), pixel.getY(), pixel.getColor());
            }
        });
    }
    
    public void updateTemplate(Template newTemplate) {
        template = newTemplate;
        graphics.setTemplate(template);
    }
}
