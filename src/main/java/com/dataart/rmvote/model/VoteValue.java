package com.dataart.rmvote.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 */
@AllArgsConstructor
public enum VoteValue {
    PRO(1),
    CONTRA(-1);

    @Getter
    private final int value;

}
