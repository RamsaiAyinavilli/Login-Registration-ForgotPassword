package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;  

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.User;
import com.example.demo.repository.PasswordResetTokenRepository;

@Service
@EnableScheduling
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender javaMailSender;
    
    
    
    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void generateResetToken(User user) {
        String token = generateToken();
        PasswordResetToken resetToken = new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(5));
        tokenRepository.save(resetToken);
        
        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        sendResetEmail(user.getEmail(), resetLink);
    }

 
    private void sendResetEmail(String email, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset");
        message.setText("Click the following link to reset your password: " + resetLink);
        javaMailSender.send(message);
        System.out.println("Email sent successfully");
    }
    
    
    public boolean isValidToken(String token) {
        Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isPresent()) {
            PasswordResetToken resetToken = optionalToken.get();
            return !resetToken.isExpired();
        }
        return false;
    }

    public User getUserByToken(String token) {
        Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isPresent()) {
            PasswordResetToken retrievedToken = optionalToken.get();
            if (retrievedToken.getUser() != null) {
                return retrievedToken.getUser();
            } else {
                throw new IllegalArgumentException("User is null for the provided token");
            }
        }
        throw new IllegalArgumentException("Token not found: " + token);
    }
    
    @Scheduled(fixedRate = 1  * 60 * 1000)
    public void deleteExpiredTokens() {
        List<PasswordResetToken> expiredTokens = tokenRepository.findAllByExpiryDateBefore(LocalDateTime.now());
        tokenRepository.deleteAll(expiredTokens);
        System.out.println("Expired tokens deleted: " + expiredTokens.size());
    }
    
}
