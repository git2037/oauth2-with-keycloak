package com.example.resourceserver.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class ResponseAPI<T> {
    public enum StatusAPI {
        SUCCESS, FAILURE
    }

    private StatusAPI status = StatusAPI.SUCCESS;
    private String message;
    private T data;
}

