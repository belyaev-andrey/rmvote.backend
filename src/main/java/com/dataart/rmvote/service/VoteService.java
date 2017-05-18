package com.dataart.rmvote.service;

import com.dataart.rmvote.model.UserPrincipal;
import com.dataart.rmvote.model.VoteValue;
import com.dataart.rmvote.model.VotesSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

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
        boolean voted = deleteVote(userId, voter) > 0;
        if (log.isTraceEnabled() && voted){
            log.trace("{}'s vote for user {} was deleted before recording a new vote", voter, userId);
        }
        try {
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

    @Transactional(readOnly = true)
    public VotesSummary getVotesForUser(int userId){
        List<VotesSummary> summary =
                jdbcTemplate.query("SELECT user_id, sum(vote_pro), sum(vote_contra) FROM votes WHERE user_id = ? GROUP BY user_id",
                        new Object[]{userId},
                        (rs, rowNum) -> new VotesSummary(rs.getInt(1), true, rs.getInt(2), rs.getInt(3), null));
        if (!CollectionUtils.isEmpty(summary)){
            return summary.get(0);
        } else {
            return new VotesSummary(userId, false, 0, 0, null);
        }
    }

}
