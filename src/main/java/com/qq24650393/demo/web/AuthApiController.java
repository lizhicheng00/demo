package com.qq24650393.demo.web;

import com.qq24650393.demo.config.JwtTokenService;
import com.qq24650393.demo.web.api.AuthApi;
import com.qq24650393.demo.web.model.ApiResponseCurrentUserResponse;
import com.qq24650393.demo.web.model.ApiResponseTokenResponse;
import com.qq24650393.demo.web.model.CurrentUserResponse;
import com.qq24650393.demo.web.model.LoginRequest;
import com.qq24650393.demo.web.model.TokenResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthApiController implements AuthApi {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthApiController(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public ResponseEntity<ApiResponseTokenResponse> login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        String token = jwtTokenService.generate((UserDetails) authentication.getPrincipal());
        TokenResponse tokenResponse = new TokenResponse()
                .tokenType("Bearer")
                .accessToken(token);
        return ResponseEntity.ok(WebResponses.success(new ApiResponseTokenResponse().data(tokenResponse)));
    }

    @Override
    public ResponseEntity<ApiResponseCurrentUserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        CurrentUserResponse user = new CurrentUserResponse()
                .username(authentication.getName())
                .roles(roles);
        return ResponseEntity.ok(WebResponses.success(new ApiResponseCurrentUserResponse().data(user)));
    }
}
