package com.pm.pmauthservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class ProfileResponse {
    private UUID id;
    private String username;
    private String email;
    private Set<String> roles;
    private Instant createdAt;
}