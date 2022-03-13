package com.codeject.customer;

public record CustomerRegistrationRequest(
        String firstName,
        String lastName,
        String email) {
}