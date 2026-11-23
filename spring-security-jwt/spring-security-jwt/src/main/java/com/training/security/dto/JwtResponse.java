package com.training.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    @Builder.Default
    private String tokenType = "Bearer";

    private String accessToken;
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
}
