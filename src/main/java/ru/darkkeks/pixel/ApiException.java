package ru.darkkeks.pixel;

import com.google.gson.JsonObject;

public class ApiException extends RuntimeException {

    private final JsonObject result;

    public ApiException(JsonObject result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ApiException{" +
                "result=" + result +
                '}';
    }
}
