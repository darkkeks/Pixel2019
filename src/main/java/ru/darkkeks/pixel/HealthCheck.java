package ru.darkkeks.pixel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HealthCheck {

    private Map<Pixel, LoginCredentials> toConfirm;

    public HealthCheck() {
        this.toConfirm = new ConcurrentHashMap<>();
    }

    public void onPixel(Pixel pixel) {
        toConfirm.remove(pixel);
    }

    public void onPlace(Pixel pixel, LoginCredentials credentials) {
        toConfirm.put(pixel, credentials);
    }

    public boolean checkHealth(Pixel pixel) {
        LoginCredentials credentials = toConfirm.remove(pixel);
        if(credentials != null) {
            System.out.println("Unhealthy account! "
                    + credentials.toString().split("vk_user_id=")[1].split("&")[0]);
            return false;
        }
        return true;
    }
}
