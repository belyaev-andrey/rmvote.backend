package com.dataart.rmvote.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Authorization data.
 */
@Data
@AllArgsConstructor
public class AuthResponse {

    private String status;
    private String token;

}
