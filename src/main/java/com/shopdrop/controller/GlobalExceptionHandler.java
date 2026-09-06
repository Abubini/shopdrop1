package com.shopdrop.controller;

import com.shopdrop.service.InvalidOrderTransitionException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, InvalidOrderTransitionException.class, IllegalStateException.class})
    public String handleBusinessException(Exception ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error";
    }
}
