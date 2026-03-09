package com.pm.pmauthservice.controller;

import com.pm.pmauthservice.dto.RegisterRequest;
import com.pm.pmauthservice.dto.RegisterResponse;
import com.pm.pmauthservice.mapper.UserMapper;
import com.pm.pmauthservice.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        var user = registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toRegisterResponse(user));
    }
}
