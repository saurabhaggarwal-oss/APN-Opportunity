package com.ttn.ck.apn.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper used across all endpoints.
 * Provides a consistent response structure with success status,
 * message, and optional data payload.
 *
 * Example success response:
 * {
 *   "success": true,
 *   "message": "Records fetched successfully",
 *   "data": [ ... ],
 *   "timestamp": "2026-03-12T15:40:00"
 * }
 *
 * Example error response:
 * {
 *   "success": false,
 *   "message": "No records found for the given filters",
 *   "timestamp": "2026-03-12T15:40:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Factory method for successful responses with data.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Factory method for successful responses without data.
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Factory method for error responses.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
