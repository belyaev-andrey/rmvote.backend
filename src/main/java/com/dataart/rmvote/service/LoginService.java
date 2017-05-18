package com.dataart.rmvote.service;

import com.dataart.rmvote.model.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
@Slf4j
@Service
public class LoginService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LoginService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Number createUser(String name, String password) {
        String hashed = getHash(name, password);

        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate);
        Map<String, Object> params = new HashMap<>();
        params.put("user_name", name);
        params.put("password", hashed);
        Number returnKey = null;
        try {
            returnKey = insert.withTableName("users").usingColumns("user_name", "password").usingGeneratedKeyColumns("user_id").executeAndReturnKey(params);
        } catch (DataAccessException e) {
            log.error("Issue with DB access", e);
            throw new IllegalStateException("Problem with database access", e);
        }
        return returnKey;
    }

    @Transactional(readOnly = true)
    public UserPrincipal login(String name, String password) {
        UserPrincipal principal = getUserPrincipalByName(name);
        //Don't want to compare it in DB
        String hashed = getHash(name, password);
        if (hashed.equals(principal.getToken())) {
            principal.setToken(UUID.randomUUID().toString());
            return principal;
        } else {
            throw new IllegalArgumentException("Username or password are not valid");
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserPrincipal updatePmName(String name, String pmName){
        UserPrincipal principal = getUserPrincipalByName(name);
        try {
            jdbcTemplate.update("update users set pm_name = ? where user_id = ?", pmName, principal.getId());
            principal.setPmName(pmName);
        } catch (DataAccessException e) {
            log.error("Issue with updating PM name for user", e);
            throw new IllegalStateException("Problem with database access", e);
        }
        return principal;
    }

    private UserPrincipal getUserPrincipalByName(String name) {
        UserPrincipal principal;
        try {
            principal =
                    jdbcTemplate.queryForObject(
                            "select user_id, user_name, pm_name, password from users where user_name = ?", new Object[]{name},
                            (rs, rowNum) -> new UserPrincipal(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Username or password are not valid");
        } catch (DataAccessException e) {
            log.error("Issue with DB access", e);
            throw new IllegalStateException("Problem with database access", e);
        }
        return principal;
    }

    private static String getHash(String name, String password) {
        String hashed;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(("$" + password + "#" + name).getBytes());
            hashed = Base64.encodeBytes(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Bad Crypto Library", e);
        }
        return hashed;
    }

}
