package ru.darkkeks.pixel;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

@ClientEndpoint
public class WebsocketClient {

    private final String endpoint;
    private Session userSession = null;
    private MessageHandler handler;

    public WebsocketClient(String endpoint, MessageHandler handler) {
        System.out.println(endpoint);
        this.endpoint = endpoint;
        this.handler = handler;
    }

    public void start() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(endpoint));
        } catch (DeploymentException | IOException | URISyntaxException e) {
            throw new RuntimeException("Can't connect to websocket endpoint", e);
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        handler.onClose();
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(String message) {
        handler.handleMessage(message);
    }

    @OnMessage
    public void onBinaryMessage(byte[] b) {
        handler.handleBinaryMessage(b);
    }

    public Future<Void> sendBinary(ByteBuffer buffer) {
        return userSession.getAsyncRemote().sendBinary(buffer);
    }

    public Future<Void> sendString(String string) {
        return userSession.getAsyncRemote().sendText(string);
    }

    public void close() {
        try {
            userSession.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}