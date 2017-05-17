package com.dataart.rmvote.api;

import com.dataart.rmvote.model.AuthRequest;
import com.dataart.rmvote.model.AuthResponse;
import com.dataart.rmvote.model.FeedbackText;
import com.dataart.rmvote.model.UserPrincipal;
import com.dataart.rmvote.model.Vote;
import com.dataart.rmvote.model.VotesSummary;
import com.dataart.rmvote.service.FeedbackService;
import com.dataart.rmvote.service.LoginService;
import com.dataart.rmvote.service.VoteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
@Slf4j
@Api(tags = "Voting endpoint")
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
@CrossOrigin("*")
public class VoteController {

    @Value("${admin.token}")
    private String adminToken;

    private final LoginService loginService;
    private final VoteService voteService;
    private final FeedbackService feedbackService;
    private final Cache cache;

    @Autowired
    public VoteController(LoginService loginService, VoteService voteService, FeedbackService feedbackService, CacheManager cacheManager) {
        this.loginService = loginService;
        this.voteService = voteService;
        this.feedbackService = feedbackService;
        cache = cacheManager.getCache("login");
    }


    @RequestMapping(path = "admin/create",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void createUser(@RequestBody AuthRequest authRequest, @RequestHeader("Auth-Token") String token, HttpServletRequest request) {
        if (!adminToken.equals(token)) {
            throw new IllegalArgumentException("Please provide a proper token to create a new user");
        }
        log.info("Creating a new user: {}, creator's host is {} , IP is {}", authRequest.getUser(), request.getRemoteHost(), request.getRemoteAddr());
        int id = loginService.createUser(authRequest.getUser(), authRequest.getPassword()).intValue();
        log.info("Created user: {} with id {}", authRequest.getUser(), id);
    }

    @ResponseBody
    @RequestMapping(
            path = "/login",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Log in user",
            response = AuthResponse.class)
    public AuthResponse login(
            @ApiParam(value = "JSON with login data", required = true)
            @RequestBody AuthRequest authRequest,
            HttpServletRequest request) {
        log.info("Trying to login: {}, user's host is {} , IP is {}", authRequest.getUser(), request.getRemoteHost(), request.getRemoteAddr());
        UserPrincipal principal = loginService.login(authRequest.getUser(), authRequest.getPassword());
        log.info("Login for user {} succeeded, principal is {}", authRequest.getUser(), principal);
        cache.put(principal.getToken(), principal);
        return new AuthResponse("OK", principal.getToken());
    }

    @ResponseBody
    @RequestMapping(
            path = "user/{userId}/info",
            method = RequestMethod.GET
    )
    @ApiOperation(value = "Get feedback for a particular user",
            response = VotesSummary.class)
    public VotesSummary getFeedback(@ApiParam(value = "User ID to get information about", required = true) @PathVariable int userId,
                                    @ApiParam(value = "Authorization token", required = true) @RequestHeader("Auth-Token") String token,
                                    HttpServletRequest request) {
        UserPrincipal principal = getUserByToken(token, request);
        log.info("Getting vote info for user: {}, request's host is {} , IP is {}. Request by user: {}",
                userId, request.getRemoteHost(), request.getRemoteAddr(), principal);
        VotesSummary votesSummary = voteService.getVotesForUser(userId);
        List<FeedbackText> feedbackList = feedbackService.getFeedbackForUser(userId);
        votesSummary.setFeedbacks(feedbackList.toArray(new FeedbackText[feedbackList.size()]));
        log.info("Vote info for user {} extracted. Request by user: {}", userId, principal);
        return votesSummary;
    }

    @RequestMapping(
            path = "user/{userId}/vote",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ApiOperation(value = "Vote for user")
    public void addVote(@ApiParam(value = "User ID to vote for", required = true) @PathVariable int userId,
                        @ApiParam(value = "Vote value", allowableValues = "PRO, CONTRA", required = true) @RequestBody Vote vote,
                        @ApiParam(value = "Authorization token", required = true) @RequestHeader("Auth-Token") String token,
                        HttpServletRequest request) {
        UserPrincipal principal = getUserByToken(token, request);
        log.info("Voting for user: {}, request form user {} host is {} , IP is {}", userId, principal, request.getRemoteHost(), request.getRemoteAddr());
        voteService.addVoteForUser(userId, vote.getValue(), principal);
        log.info("Vote for user {} registered. Voter {}", userId, principal);
    }

    @RequestMapping(
            path = "user/{userId}/vote",
            method = RequestMethod.DELETE
    )
    @ApiOperation(value = "Remove vote for user")
    public void deleteVote(@ApiParam(value = "User ID to vote for", required = true) @PathVariable int userId,
                           @ApiParam(value = "Authorization token", required = true) @RequestHeader("Auth-Token") String token,
                           HttpServletRequest request) {
        UserPrincipal principal = getUserByToken(token, request);
        log.info("Removing vote for user: {}, request form user {} host is {} , IP is {}", userId, principal, request.getRemoteHost(), request.getRemoteAddr());
        voteService.deleteVote(userId, principal);
        log.info("Vote for user {} deleted. Voter {}", userId, principal);
    }

    @RequestMapping(
            path = "user/{userId}/feedback",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ApiOperation(value = "Provide feedback for user")
    public void addFeedback(@ApiParam(value = "User ID to provide feedback", required = true) @PathVariable int userId,
                            @ApiParam(value = "Feedback text up to 2000 chars", required = true) @RequestBody FeedbackText feedbackText,
                            @ApiParam(value = "Authorization token", required = true) @RequestHeader("Auth-Token") String token,
                            HttpServletRequest request) {
        UserPrincipal principal = getUserByToken(token, request);
        //Quite an ugly solution, but we agreed on this with FE developer
        String author = feedbackText.getAuthor();
        updatePmName(principal, author);
        log.info("Providing feedback for user: {}, request form user {} host is {} , IP is {}", userId, principal, request.getRemoteHost(), request.getRemoteAddr());
        feedbackService.addCommentForUser(userId, feedbackText, principal);
        log.info("Feedabck for user {} registered. Author {}", userId, principal);
    }

    @RequestMapping(
            path = "user/{userId}/feedback",
            method = RequestMethod.DELETE
    )
    @ApiOperation(value = "Provide feedback for user")
    public void deleteFeedback(@ApiParam(value = "User ID to remove feedback", required = true) @PathVariable int userId,
                               @ApiParam(value = "Authorization token", required = true) @RequestHeader("Auth-Token") String token,
                               HttpServletRequest request) {
        UserPrincipal principal = getUserByToken(token, request);
        log.info("Deleting feedback for user: {}, request form user {} host is {} , IP is {}", userId, principal, request.getRemoteHost(), request.getRemoteAddr());
        feedbackService.deleteCommentForUser(userId, principal);
        log.info("Feedback for user {} deleted. Author {}", userId, principal);
    }

    private UserPrincipal getUserByToken(String token, HttpServletRequest request) {
        log.trace("Trying to get logged user's info from cache by token {}, request from IP {}", token, request.getRemoteAddr());
        UserPrincipal principal = cache.get(token, UserPrincipal.class);
        log.trace("Logged user's info from cache by token {}, request from IP {}: {}", token, request.getRemoteAddr(), principal);
        if (principal == null) {
            throw new IllegalArgumentException(String.format("The user is not logged in, token is: %s", token));
        }
        return principal;
    }

    private void updatePmName(UserPrincipal principal, String newPmName) {
        if (newPmName != null && !newPmName.equals(principal.getPmName())) {
            log.info("Updating user {} with new PM name {}", principal, newPmName);
            loginService.updatePmName(principal.getName(), newPmName);
            cache.put(principal.getToken(), principal);
        }
    }


}
