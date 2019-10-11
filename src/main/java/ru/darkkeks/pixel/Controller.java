package ru.darkkeks.pixel;

import ru.darkkeks.pixel.graphics.BoardGraphics;
import ru.darkkeks.pixel.graphics.Template;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.http.HttpClient;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Controller {

    private final HttpClient httpClient;
    private final Template template;
    private final PixelQueue queue;
    private PixelAccount observer;

    private BoardGraphics graphics;

    private Set<PixelAccount> accounts;
    private ScheduledThreadPoolExecutor executor;

    private HealthCheck healthCheck;
    private int lastMinuteStats;

    public Controller(LoginCredentials observerCredentials, Template template) {
        this.template = template;
        this.accounts = new HashSet<>();

        executor = new ScheduledThreadPoolExecutor(4); // TODO Single thread to prevent threading issues :)
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
            System.out.println("Accounts active: " + accounts.size());
            System.out.println("Current queue size: " + queue.size());
            accounts.forEach(account -> {
                if (account.canPlace()) {
                    if (queue.size() > 0) {
                        PixelQueue.Point point = queue.pop();
                        Color color = template.getColor(point.getX(), point.getY());
                        System.out.println("Placing pixel x=" + point.getX() + ", y=" + point.getY());
                        Pixel pixel = Pixel.place(point.getX(), point.getY(), color);
                        account.sendPixel(pixel);
                        healthCheck.onPixel(pixel);
                        executor.schedule(() -> {
                            if (healthCheck.checkHealth(pixel)) {
                                lastMinuteStats++;
                            }
                        }, 10, TimeUnit.SECONDS);
                    }
                }
            });
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
            System.out.println("Started!");
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
}
