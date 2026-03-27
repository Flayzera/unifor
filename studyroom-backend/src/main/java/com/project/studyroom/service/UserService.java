package com.project.studyroom.service;

import com.google.firebase.auth.FirebaseAuth;
import com.project.studyroom.model.User;
import com.project.studyroom.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void create(User user) throws Exception {
        if(user.getRole() == null) {
            user.setRole("STUDENT");
        }
        userRepository.save(user);
    }

    public User findById(String uid) throws Exception {
        User user = userRepository.getById(uid);
        if (user == null) {
            throw new RuntimeException("Perfil de usuário não encontrado.");
        }
        return user;
    }

    public void makeAdmin(String uid) throws Exception {
        Map<String,Object> claims = new HashMap<>();
        claims.put("admin", true);
        FirebaseAuth.getInstance().setCustomUserClaims(uid, claims);

        User user = userRepository.getById(uid);
        if(user != null) {
            user.setRole("ADMIN");
            userRepository.save(user);
        }
    }
}
