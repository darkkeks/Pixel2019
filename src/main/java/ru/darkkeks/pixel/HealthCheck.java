package ru.darkkeks.pixel;

import java.util.HashMap;
import java.util.Map;

public class HealthCheck {

    private Map<Pixel, LoginCredentials> toConfirm;

    public HealthCheck() {
        this.toConfirm = new HashMap<>();
    }

    public void onPixel(Pixel pixel) {
        toConfirm.remove(pixel);
    }

    public void onPlace(Pixel pixel, LoginCredentials credentials) {
        toConfirm.put(pixel, credentials);
    }

    public boolean checkHealth(Pixel pixel) {
        if(toConfirm.containsKey(pixel)) {
            System.out.println("Unhealthy account! " + toConfirm.get(pixel).toString());
            toConfirm.remove(pixel);
            return false;
        }
        return true;
    }
}
