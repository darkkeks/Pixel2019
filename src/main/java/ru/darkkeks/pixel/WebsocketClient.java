package ru.darkkeks.pixel;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

@ClientEndpoint
public class WebsocketClient {

    private Session userSession = null;
    private MessageHandler handler;

    public WebsocketClient(String endpoint, MessageHandler handler) {
        System.out.println(endpoint);
        this.handler = handler;
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

    public void sendBinary(ByteBuffer buffer) {
        userSession.getAsyncRemote().sendBinary(buffer);
    }

    public void close() {
        try {
            userSession.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}