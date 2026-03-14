package com.texinsite.engine.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author Duan Guanyi
 * @version 1.0.0
 * @date 2026/3/11
 */
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少6位")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;


    // 手动get，set方法
    public @Email(message = "邮箱格式不正确") String getEmail() {
        return email;
    }

    public @NotBlank(message = "密码不能为空") String getPassword() {
        return password;
    }

    public @NotBlank(message = "用户名不能为空") String getUsername() {
        return username;
    }

    public void setEmail(@Email(message = "邮箱格式不正确") String email) {
        this.email = email;
    }

    public void setPassword(@NotBlank(message = "密码不能为空") String password) {
        this.password = password;
    }

    public void setUsername(@NotBlank(message = "用户名不能为空") String username) {
        this.username = username;
    }
    //==========================================================
}

