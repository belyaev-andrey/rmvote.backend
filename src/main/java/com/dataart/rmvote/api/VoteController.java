package com.dataart.rmvote.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by abelyaev on 5/15/2017.
 */
@RestController
public class VoteController {

    @RequestMapping("/")
    public String index(){
        return "Hello, I'm a voting app";
    }

}
