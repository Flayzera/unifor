package com.project.studyroom.controller;

import com.project.studyroom.config.security.AuthenticatedUser;
import com.project.studyroom.model.User;
import com.project.studyroom.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<String> createProfile(@RequestBody User user, @AuthenticatedUser String uid) throws Exception {

        user.setId(uid);
        userService.create(user);
        return ResponseEntity.status(201).body("Perfil sincronizado com sucesso.");
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@AuthenticatedUser String uid) throws Exception {
        return ResponseEntity.ok(userService.findById(uid));
    }

    @PostMapping("/{uid}/promote")
    @PreAuthorize("hasRole('ADMIN')") // Só um ADM promove outro
    public ResponseEntity<String> promoteToAdmin(@PathVariable String uid) throws Exception {
        userService.makeAdmin(uid);
        return ResponseEntity.ok("Usuário promovido a ADMIN. Peça para ele relogar.");
    }
}
