package com.qq24650393.demo.auth;

import java.util.List;

public record CurrentUserResponse(String username, List<String> roles) {
}
