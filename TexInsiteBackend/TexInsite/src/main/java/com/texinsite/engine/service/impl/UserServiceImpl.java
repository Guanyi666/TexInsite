package com.texinsite.engine.service.impl;

import com.texinsite.engine.dto.LoginRequest;
import com.texinsite.engine.dto.RegisterRequest;
import com.texinsite.engine.model.User;
import com.texinsite.engine.repository.UserRepository;
import com.texinsite.engine.service.UserService;
import com.texinsite.engine.utils.JwtUtils;
import com.texinsite.engine.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author Duan Guanyi
 * @version 1.0.0
 * @date 2026/3/11
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void register(RegisterRequest dto) {
        String username = normalizeRequiredField(dto.getUsername(), "用户名不能为空");
        String email = normalizeRequiredField(dto.getEmail(), "邮箱不能为空");

        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
    }

    @Override
    public LoginVO login(LoginRequest dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtils.generateToken(user.getUsername());
        return new LoginVO(token, user.getUsername());
    }

    private String normalizeRequiredField(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
