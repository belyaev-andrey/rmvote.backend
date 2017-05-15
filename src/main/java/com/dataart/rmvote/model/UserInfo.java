package com.dataart.rmvote.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private boolean voted;
    private int positiveVotes;
    private int negativeVotes;
    private UserFeedback[] feedbacks;

}