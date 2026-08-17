package com.backend.modulo.controller;

import com.backend.modulo.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ApiDebugController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/api/debug/claims")
    public ResponseEntity<?> claims(@RequestHeader HttpHeaders headers) {
        String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
        Map<String, Object> resp = new HashMap<>();
        resp.put("authorizationHeader", auth);
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwtTokenProvider.extractAllClaims(token);
                resp.put("claims", claims);
            } catch (Exception ex) {
                resp.put("error", "could not parse token: " + ex.getMessage());
            }
        } else {
            resp.put("error", "no bearer token provided");
        }
        return ResponseEntity.ok(resp);
    }
}
