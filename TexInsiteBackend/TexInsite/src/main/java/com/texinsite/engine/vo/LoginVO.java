package com.texinsite.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Duan Guanyi
 * @version 1.0.0
 * @date 2026/3/11
 */
@Data
//@AllArgsConstructor
public class LoginVO {
    private String token;

    private String username;

    public LoginVO(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}
