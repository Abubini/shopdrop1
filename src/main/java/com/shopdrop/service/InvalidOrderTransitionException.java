package com.shopdrop.service;

/** Thrown when an order is asked to move to a status its current state does not allow. */
public class InvalidOrderTransitionException extends RuntimeException {

    public InvalidOrderTransitionException(String message) {
        super(message);
    }
}
