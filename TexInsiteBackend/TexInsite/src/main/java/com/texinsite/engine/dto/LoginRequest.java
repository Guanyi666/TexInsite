package com.texinsite.engine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Duan Guanyi
 * @version 1.0.0
 * @date 2026/3/11
 */
@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    public void setUsername(@NotBlank(message = "用户名不能为空") String username) {
        this.username = username;
    }

    public void setPassword(@NotBlank(message = "密码不能为空") String password) {
        this.password = password;
    }

    public @NotBlank(message = "用户名不能为空") String getUsername() {
        return username;
    }

    public @NotBlank(message = "密码不能为空") String getPassword() {
        return password;
    }
}
