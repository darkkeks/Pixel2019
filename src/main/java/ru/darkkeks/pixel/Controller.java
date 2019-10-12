package ru.darkkeks.pixel;

import ru.darkkeks.pixel.graphics.BoardGraphics;
import ru.darkkeks.pixel.graphics.Template;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.http.HttpClient;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
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

    private boolean boardLoaded = false;
    private List<Pixel> beforeLoad;
    
    public Controller(LoginCredentials observerCredentials, File templateFile) {
        this.template = Template.load(templateFile);
        this.accounts = ConcurrentHashMap.newKeySet();

        this.speedQueue = new ArrayBlockingQueue<>(128);

        executor = new ScheduledThreadPoolExecutor(24);
        graphics = new BoardGraphics(new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB));
        graphics.setTemplate(template);
        httpClient = HttpClient.newHttpClient();

        beforeLoad = new ArrayList<>();
        healthCheck = new HealthCheck();
        queue = new PixelQueue(template);

        startTicker();

        addAccount(observerCredentials).thenAccept(account -> {
            observer = account;
            hookObserver();
        });

        runBot();

        graphics.setOnTemplateUpdate(() -> {
            queue.rebuild(graphics.getImage());
        });

        executor.submit(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                templateFile.getAbsoluteFile().getParentFile().toPath().register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY);

                WatchKey key;
                while((key = watchService.take()) != null) {
                    key.pollEvents().forEach(watchEvent -> {
                        if(watchEvent.context().toString().equals(templateFile.getName())) {
                            updateTemplate(Template.load(templateFile));
                            System.out.println("Template modified. Updating!");
                        }
                    });
                    key.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void runBot() {
        executor.scheduleAtFixedRate(() -> {
            String output = String.format("Accounts active: %5d, queue size: %5d", accounts.size(), queue.size());
            
            speedQueue.offer(queue.size());
            if(speedQueue.size() > 60) {
                int prev = speedQueue.poll();
                output += String.format(", current speed: %5.2f pixels/second", (prev - queue.size()) / 60d);
            }

            System.out.println(output);

            try {
                if(!boardLoaded) return;
                accounts.forEach(account -> {
                    if (account.canPlace()) {
                        if (!queue.isEmpty()) {
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

            boardLoaded = true;
            beforeLoad.forEach(this::onPixel);
        });

        observer.setPixelConsumer(this::onPixel);
    }

    private void onPixel(Pixel pixel) {
        if(!boardLoaded) {
            beforeLoad.add(pixel);
        }

        healthCheck.onPixel(pixel);
        queue.onPixelChange(pixel);
        if (graphics != null) {
            graphics.setPixel(pixel.getX(), pixel.getY(), pixel.getColor());
        }
    }
    
    private void updateTemplate(Template newTemplate) {
        template = newTemplate;
        graphics.setTemplate(template);
        queue.setTemplate(template);
        queue.rebuild(graphics.getImage());
    }
}
