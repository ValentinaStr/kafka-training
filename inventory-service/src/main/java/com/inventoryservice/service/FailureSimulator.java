package com.inventoryservice.service;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class FailureSimulator {

    private final Random random = new Random();

    public void maybeFail() {
        if (random.nextBoolean()) {
            throw new RuntimeException("Simulated processing failure");
        }
    }
}
