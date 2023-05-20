package com.orekhov.client.controller;

import com.orekhov.client.access.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/v1/client")
@ResponseBody
@AllArgsConstructor
public class ClientController {
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/access")
    public ResponseEntity<?> getAccess() {
        return ResponseEntity.ok(currentUserProvider.get().isEnabled() ? "Access granted" : "Forbidden!");
    }
}
