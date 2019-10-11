package ru.darkkeks.pixel;

public interface MessageHandler {
    void handleMessage(String message);
    void handleBinaryMessage(byte[] b);
    void onClose();
}
