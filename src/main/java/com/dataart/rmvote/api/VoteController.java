package com.dataart.rmvote.api;

import com.dataart.rmvote.model.AuthRequest;
import com.dataart.rmvote.model.AuthResponse;
import com.dataart.rmvote.model.FeedbackText;
import com.dataart.rmvote.model.UserInfo;
import com.dataart.rmvote.model.Vote;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@Slf4j
@Api(tags = "Voting endpoint")
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
public class VoteController {

    @ResponseBody
    @RequestMapping(
            path = "/login",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Log in user",
            response = AuthResponse.class)
    public AuthResponse login (
            @ApiParam(value = "JSON with login data", required = true)
            @RequestBody AuthRequest authRequest){
        return new AuthResponse("OK", "token");
    }


    @ResponseBody
    @RequestMapping(
            path = "user/{userId}/info",
            method = RequestMethod.GET
    )
    @ApiOperation(value = "Log in user",
            response = UserInfo.class)
    public UserInfo getFeedback(@ApiParam(value = "User ID to get information about", required = true) @PathVariable int userId,
                                @ApiParam(value = "Authorization token", required = true) @RequestHeader("Auth-Token") String token) {
        return new UserInfo();
    }


    @RequestMapping(
            path = "user/{userId}/vote",
            method = RequestMethod.POST
    )
    @ApiOperation(value = "Vote for user")
    public void addVote(@ApiParam(value = "User ID to vote for", required = true) @PathVariable int userId,
                        @ApiParam(value = "Vote value", allowableValues = "PRO, CONTRA", required = true) @RequestBody Vote vote,
                        @ApiParam(value = "Authorization token", required = true) @RequestHeader("Auth-Token") String token){
        log.info("Adding vote for user {} vote is {}", userId, vote);
    }

    @RequestMapping(
            path = "user/{userId}/vote",
            method = RequestMethod.DELETE
    )
    @ApiOperation(value = "Remove vote for user")
    public void deleteVote(@ApiParam(value = "User ID to vote for", required = true) @PathVariable int userId,
                        @ApiParam(value = "Authorization token", required = true) @RequestHeader("Auth-Token") String token){
        log.info("Removing vote for user {}", userId);
    }


    @RequestMapping(
            path = "user/{userId}/feedback",
            method = RequestMethod.POST
    )
    @ApiOperation(value = "Provide feedback for user")
    public void addFeedback(@ApiParam(value = "User ID to provide feedback", required = true) @PathVariable int userId,
                        @ApiParam(value = "Feedback text", allowableValues = "PRO, CONTRA", required = true) @RequestBody FeedbackText feedbackText,
                        @ApiParam(value = "Authorization token", required = true) @RequestHeader("Auth-Token") String token){
        log.info("Adding feedback for user {} : {}", userId, feedbackText);
    }


    @RequestMapping(
            path = "user/{userId}/feedback",
            method = RequestMethod.DELETE
    )
    @ApiOperation(value = "Provide feedback for user")
    public void deleteFeedback(@ApiParam(value = "User ID to remove feedback", required = true) @PathVariable int userId,
                            @ApiParam(value = "Authorization token", required = true) @RequestHeader("Auth-Token") String token){
        log.info("Deleting feedback for user {} : {}", userId);
    }


}
