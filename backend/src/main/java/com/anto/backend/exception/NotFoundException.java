package com.anto.backend.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(Integer id) {
        super("Could not find recipe with id " + id);
    }
}
