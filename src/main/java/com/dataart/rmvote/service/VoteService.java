package com.dataart.rmvote.service;

import com.dataart.rmvote.model.UserPrincipal;
import com.dataart.rmvote.model.VoteValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

/**
 *
 */
@Slf4j
@Service
public class VoteService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public VoteService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void addVoteForUser(int userId, VoteValue vote, UserPrincipal voter){
        int pro = 1;
        int contra = 0;
        if (vote == VoteValue.CONTRA){
            pro = 0;
            contra = 1;
        }
        //Just delete votes to avoid hassle with merge statement which is not supported on every DB.
        int voted = deleteVote(userId, voter);
        try {
            log.trace("User {} voted for user {}: {}", voter.getName(), userId, (voted > 0));
            Timestamp now = Timestamp.from(Instant.now());
            jdbcTemplate.update("insert into votes (user_id, vote_pro, vote_contra, voter_id, vote_date) values (?,?,?,?,?)", userId, pro, contra, voter.getId(), now);
        } catch (DataAccessException e) {
            log.error("Issue with inserting vote", e);
            throw new IllegalStateException("Problem with database access", e);
        }
    }

    @Transactional
    public int deleteVote(int userId, UserPrincipal voter) {
        int voted;
        try {
            voted = jdbcTemplate.update("delete from votes where user_id = ? and voter_id = ?", userId, voter.getId());
        } catch (DataAccessException e) {
            log.error("Issue with deleting votes", e);
            throw new IllegalStateException("Problem with database access", e);
        }
        return voted;
    }

}
