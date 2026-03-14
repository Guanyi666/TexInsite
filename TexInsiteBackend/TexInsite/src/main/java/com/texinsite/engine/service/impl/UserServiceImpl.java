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
        // 1.查询用户是否存在
        if(userRepository.findByUsername(dto.getUsername()).isPresent()){
            throw new RuntimeException("用户名已存在");
        }
        // 2.创建新用户并加密密码
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // 3.保存到数据库
        userRepository.save(user);
    }

    @Override
    public LoginVO login(LoginRequest dto) {
        // 1.查找用户
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 2.校验密码
        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3.生成Token
        String token = jwtUtils.generateToken(user.getUsername());

        // 4.返回VO
        return new LoginVO(token, user.getUsername());
    }
}
