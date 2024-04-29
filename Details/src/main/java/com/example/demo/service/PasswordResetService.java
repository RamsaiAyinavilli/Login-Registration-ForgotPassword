package com.example.demo.service;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;  // Import LocalDateTime

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.User;
import com.example.demo.repository.PasswordResetTokenRepository;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;


    public void generateResetToken(User user) {
        String token = generateToken();
        PasswordResetToken resetToken = new PasswordResetToken( token, user, LocalDateTime.now().plusMinutes(5));
        
        tokenRepository.save(resetToken);

        System.out.println("Saving token for user: " + user.getEmail());
        
        sendResetEmail(user.getEmail(), token);
    }

    public boolean isValidToken(String token) {
        Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isPresent()) {
            PasswordResetToken resetToken = optionalToken.get();
            if (!resetToken.isExpired()) {
                System.out.println("Token is valid: " + token);
                return true;
            } else {
                System.out.println("Token is expired: " + token);
            }
        } else {
            System.out.println("Token not found: " + token);
        }
        return false;
    }
    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    @Autowired
    private JavaMailSender javaMailSender;

    private void sendResetEmail(String email, String token) {

        
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset");
            message.setText("Use this OTP to reset your password: " + token);
            javaMailSender.send(message);
            System.out.println("Email sent successfully");
            
    }

    public User getUserByToken(String token) {
        Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isPresent()) {
            PasswordResetToken retrievedToken = optionalToken.get();
            if (retrievedToken.getUser() != null) {
                User user = retrievedToken.getUser();
                System.out.println("User found for token " + token + ": " + user.getEmail());
                return user;
            } else {
                System.out.println("User is null for token: " + token);
                throw new IllegalArgumentException("User is null for the provided token");
            }
        }
        System.out.println("Token not found: " + token);
        throw new IllegalArgumentException("Token not found: " + token);
    }

}
