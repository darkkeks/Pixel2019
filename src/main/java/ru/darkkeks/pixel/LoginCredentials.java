package ru.darkkeks.pixel;

public class LoginCredentials {

    private String signature;

    public LoginCredentials(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return "LoginCredentials{" +
                "signature='" + signature + '\'' +
                '}';
    }
}
