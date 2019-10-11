package ru.darkkeks.pixel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PixelAccount implements MessageHandler {

    private final PixelApi pixelApi;
    private final LoginCredentials loginSignature;
    private String wsUrl;
    private String dataUrl;
    private int sign;
    private String code;
    private WebsocketClient websocketClient;

    private int ttl;
    private int wait;

    private Consumer<Pixel> pixelConsumer;
    private Consumer<PixelAccount> onClose;

    public PixelAccount(LoginCredentials loginSignature, HttpClient client, Consumer<PixelAccount> onClose) {
        this.loginSignature = loginSignature;
        this.onClose = onClose;
        pixelApi = new PixelApi(loginSignature.getSignature(), client);
    }

    public CompletableFuture<Void> start() {
        return pixelApi.start().thenAccept(data -> {
            wsUrl = data.get("url").getAsString();
            dataUrl = data.get("data").getAsString();
            sign = data.get("sign").getAsInt();
            code = data.get("code").getAsString();
            //TODO deadline ?
            connectWebSocket();
        });
    }

    private void connectWebSocket() {
        websocketClient = new WebsocketClient(PixelApi.WS_URL + wsUrl +
                loginSignature.getSignature() +
                "&s=" + sign +
                "&c=" + Util.evaluateJS(code), this);
    }

    public void tick() {
        if (wait > 0) wait--;
    }

    public boolean canPlace() {
        return wait == 0;
    }

    public void sendPixel(Pixel pixel) {
        if (!canPlace()) throw new RuntimeException("Wait > 0: " + wait);

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(pixel.pack());
        buffer.flip();

        wait += ttl + 1;
        websocketClient.sendBinary(buffer);
    }

    @Override
    public void handleMessage(String message) {
        System.out.println("Message: " + message);
        if(message.equals("restart")) {
            System.out.println("Server asked for a restart :)");
            websocketClient.close();
            return;
        }

        JsonObject object = JsonParser.parseString(message).getAsJsonObject();
        JsonElement value = object.get("v");

        int type = object.get("t").getAsInt();
        switch (type) {
            case 2:
                JsonObject result = value.getAsJsonObject();
                if(result.has("wait")) {
                    wait = result.get("wait").getAsInt();
                }
                ttl = result.get("ttl").getAsInt();
                break;
            case 3:
                System.out.println("Server asked for a restart :)");
                websocketClient.close();
                break;
            case 8:
                break;
            default:
                System.out.println("Unknown message: " + message);
        }
    }

    @Override
    public void handleBinaryMessage(byte[] b) {
        ByteBuffer buffer = ByteBuffer.wrap(b);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for(int i = 0; i < b.length / 3 / 4; ++i) {
            Pixel pixel = Pixel.unpack(buffer.getInt(), buffer.getInt(), buffer.getInt());
            if (pixelConsumer != null) {
                pixelConsumer.accept(pixel);
            }
        }
    }

    @Override
    public void onClose() {
        onClose.accept(this);
    }

    public PixelApi getPixelApi() {
        return pixelApi;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public LoginCredentials getLoginSignature() {
        return loginSignature;
    }

    public void setPixelConsumer(Consumer<Pixel> pixelConsumer) {
        this.pixelConsumer = pixelConsumer;
    }
}
