package ru.darkkeks.pixel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public class PixelApi {

    public static final String API_URL = "https://pixel.w84.vkforms.ru/api";
    public static final String WS_URL = "ws://pixel.w84.vkforms.ru";
    private final HttpClient client;
    private final String loginSignature;

    public PixelApi(String loginSignature, HttpClient client) {
        this.loginSignature = loginSignature;
        this.client = client;
    }

    private <T> CompletableFuture<HttpResponse<T>> makeRequest(String endpoint,
                                                                  HttpResponse.BodyHandler<T> handler) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(API_URL + endpoint))
                    .header("X-vk-sign", loginSignature)
                    .build();
            return client.sendAsync(request, handler);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Can't make api request", e);
        }
    }

    public CompletableFuture<JsonObject> start() {
        return makeRequest("/start", HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            System.out.println(response.body());
            JsonObject result = JsonParser.parseString(response.body()).getAsJsonObject();
            if (result.has("error")) {
                throw new ApiException(result);
            }
            return result.get("response").getAsJsonObject();
        });
    }

    public CompletableFuture<BufferedImage> data() {
        int hour = LocalDateTime.now().getHour();
        int minute = LocalDateTime.now().getMinute();
        return makeRequest("/data" + String.format("?ts=%d-%d", hour, minute),
                HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            String imageData = response.body().substring(0, Constants.PIXEL_COUNT);
            String frozen = response.body().substring(Constants.PIXEL_COUNT);

            BufferedImage result = new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB);
            for(int i = 0; i < Constants.PIXEL_COUNT; ++i) {
                int x = i % Constants.WIDTH;
                int y = i / Constants.WIDTH;

                result.setRGB(x, y, Constants.charToColor(imageData.charAt(i)).getRGB());
            }

            return result;
        });
    }

}
