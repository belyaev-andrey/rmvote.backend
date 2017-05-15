package com.dataart.rmvote.api;

import com.dataart.rmvote.model.AuthRequest;
import com.dataart.rmvote.model.AuthResponse;
import com.dataart.rmvote.model.UserInfo;
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
    public UserInfo getFeedback(@ApiParam(value = "User ID to get infor about", required = true) @PathVariable int userId,
                                @ApiParam(value = "Authorization token", required = true) @RequestHeader("Auth-Token") String token) {
        return new UserInfo();
    }

}
