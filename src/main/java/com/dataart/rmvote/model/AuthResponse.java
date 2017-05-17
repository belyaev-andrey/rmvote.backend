package com.dataart.rmvote.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * Authorization data.
 */
@Data
@AllArgsConstructor
@SuppressWarnings("serial")
public class AuthResponse implements Serializable {

    private String status;
    private String token;

}
