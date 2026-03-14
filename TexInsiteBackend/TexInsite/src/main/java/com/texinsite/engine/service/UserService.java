package com.texinsite.engine.service;

import com.texinsite.engine.dto.LoginRequest;
import com.texinsite.engine.dto.RegisterRequest;
import com.texinsite.engine.vo.LoginVO;

/**
 * @author Duan Guanyi
 * @version 1.0.0
 * @date 2026/3/11
 */
public interface UserService {
    void register(RegisterRequest dto);

    LoginVO login(LoginRequest dto);

}
