package com.pm.pmauthservice.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class RegisterResponse {
    private UUID id;
    private String username;
    private String email;
    private Set<String> roles;
    private Instant createdAt;
    private String message;
}
