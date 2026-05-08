package com.group24.projectselection.service.shared;

public record CrudResult<T>(
        boolean success,
        int statusCode,
        String message,
        T data
) {
    public static <T> CrudResult<T> success(int statusCode, String message, T data) {
        return new CrudResult<>(true, statusCode, message, data);
    }

    public static <T> CrudResult<T> error(int statusCode, String message) {
        return new CrudResult<>(false, statusCode, message, null);
    }
}
