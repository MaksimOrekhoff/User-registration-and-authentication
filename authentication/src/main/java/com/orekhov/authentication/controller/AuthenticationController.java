package com.orekhov.authentication.controller;

import com.orekhov.authentication.access.JwtTokenProvider;
import com.orekhov.authentication.bean.ErrorResponse;
import com.orekhov.authentication.bean.RegistrationRequest;
import com.orekhov.authentication.bean.UserResponse;
import com.orekhov.authentication.entity.AppUser;
import com.orekhov.authentication.service.RegistrationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "api/v1/auth")
@AllArgsConstructor
@Slf4j
public class AuthenticationController {
    private final RegistrationService registrationService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/registration")
    public ResponseEntity<?> registration(@RequestBody RegistrationRequest registrationRequest, HttpServletResponse response) {
        try {
            AppUser appUser = registrationService.register(registrationRequest);
              setAuthToken(appUser, response);
             setRefreshToken(appUser, response);
            return buildUserResponse(appUser);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
              clearAuthAndRefreshTokens(response);
            return buildErrorResponse(e.getLocalizedMessage());
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> current() {
        try {
            AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return buildUserResponse(appUser);
        } catch (NullPointerException e) {
            log.error(e.getLocalizedMessage());
        }
        return buildUserResponse(new AppUser());
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse httpServletResponse) {
        clearAuthAndRefreshTokens(httpServletResponse);
        SecurityContextHolder.clearContext();
        return buildUserResponse(new AppUser());
    }

    private void clearAuthAndRefreshTokens(HttpServletResponse httpServletResponse) {
        Cookie authCookie = new Cookie(jwtTokenProvider.getAuthCookieName(), "-");
        authCookie.setPath(jwtTokenProvider.getPathCookie());

        Cookie refreshCookie = new Cookie(jwtTokenProvider.getRefreshCookieName(), "-");
        refreshCookie.setPath(jwtTokenProvider.getPathCookie());

        httpServletResponse.addCookie(authCookie);
        httpServletResponse.addCookie(refreshCookie);
    }

    private void setAuthToken(AppUser appUser, HttpServletResponse httpServletResponse) {
        String token = jwtTokenProvider.createAuthToken(appUser.getEmail(), appUser.getRole().name());
        Cookie cookie = new Cookie(jwtTokenProvider.getAuthCookieName(), token);
        cookie.setPath(jwtTokenProvider.getPathCookie());
        cookie.setHttpOnly(true);
        cookie.setMaxAge(jwtTokenProvider.getAuthExpirationCookie());
        httpServletResponse.addCookie(cookie);
    }

    private void setRefreshToken(AppUser appUser, HttpServletResponse httpServletResponse) {
        String token = jwtTokenProvider.createRefreshToken(appUser.getEmail(), appUser.getRole().name());
        Cookie cookie = new Cookie(jwtTokenProvider.getRefreshCookieName(), token);
        cookie.setPath(jwtTokenProvider.getPathCookie());
        cookie.setHttpOnly(true);
        cookie.setMaxAge(jwtTokenProvider.getRefreshExpirationCookie());
        httpServletResponse.addCookie(cookie);
    }

    private ResponseEntity<?> buildUserResponse(AppUser appUser) {
        return ResponseEntity.ok(new UserResponse(appUser));
    }

    private ResponseEntity<?> buildErrorResponse(String message) {
        return ResponseEntity.ok(new ErrorResponse(message));
    }
}
