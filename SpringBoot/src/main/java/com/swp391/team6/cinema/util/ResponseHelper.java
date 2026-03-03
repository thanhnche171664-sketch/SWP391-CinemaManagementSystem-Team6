package com.swp391.team6.cinema.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHelper {

    public static <T> ResponseEntity<T> success(T data) {
        return ResponseEntity.ok(data);
    }

    public static <T> ResponseEntity<T> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(data);
    }

    public static <T> ResponseEntity<T> badRequest(T data) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(data);
    }

    public static <T> ResponseEntity<T> notFound(T data) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(data);
    }

    public static <T> ResponseEntity<T> error(T data) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(data);
    }
}
