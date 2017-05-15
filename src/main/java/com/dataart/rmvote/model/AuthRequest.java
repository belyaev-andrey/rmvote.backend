package com.dataart.rmvote.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Json model for login.
 */
@Data
@AllArgsConstructor
public class AuthRequest {
    private String user;
    private String password;
}
