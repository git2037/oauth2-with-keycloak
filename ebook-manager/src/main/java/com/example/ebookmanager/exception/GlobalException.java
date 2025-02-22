package com.example.ebookmanager.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalException {
    @ExceptionHandler(Exception.class)
    public void exception(Exception e, HttpServletResponse response) {
        throw new ForbidenException(HttpStatus.FORBIDDEN.getReasonPhrase());
    }
}
