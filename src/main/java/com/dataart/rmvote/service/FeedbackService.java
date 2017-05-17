package com.dataart.rmvote.service;

import com.dataart.rmvote.model.FeedbackText;
import com.dataart.rmvote.model.UserPrincipal;
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
public class FeedbackService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FeedbackService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void addCommentForUser(int userId, FeedbackText feedback, UserPrincipal author) {
        //We need only one comment from one user
        boolean deleted = deleteCommentForUser(userId, author) > 0;
        if (log.isTraceEnabled() && deleted) {
            log.trace("Previous comment for user {} from {} was deleted before recording a new feedback", userId, author);
        }
        Timestamp now = Timestamp.from(Instant.now());
        try {
            jdbcTemplate.update("insert into feedbacks (user_id, feedback_text, author_id, feedback_date) values (?,?,?,?)"
                    , userId, feedback.getText(), author.getId(), now);
        } catch (DataAccessException e) {
            log.error("Issue with adding feedback", e);
            throw new IllegalStateException("Problem with database access", e);
        }
    }

    @Transactional
    public int deleteCommentForUser(int userId, UserPrincipal author) {
        int voted;
        try {
            voted = jdbcTemplate.update("delete from feedbacks where user_id = ? and author_id = ?", userId, author.getId());
        } catch (DataAccessException e) {
            log.error("Issue with deleting feedback", e);
            throw new IllegalStateException("Problem with database access", e);
        }
        return voted;
    }

}
