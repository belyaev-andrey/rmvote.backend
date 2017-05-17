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

    private int userId;
    private Boolean voted;
    private Integer positiveVotes;
    private Integer negativeVotes;
    private FeedbackText[] feedbacks;

}
