package com.example.demo.controller;


import java.util.List;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.User;
import com.example.demo.service.PasswordResetService;
import com.example.demo.service.UserService;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

@Autowired
    private PasswordResetService passwordResetService;
    
    @GetMapping("/userdetails")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "indexpage";
    }
 

    @GetMapping("/registeruser")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "registerpage";
    }

    @PostMapping("/registeruser")
    public String registerUser(User user , Model model) {
    	
    	// Check if the email already exists
        User existingUser = userService.findByEmail(user.getEmail());
        
        if (existingUser != null) {
            // Email already exists, return to registration page with an error message
            model.addAttribute("error", "Email already exists!");
            return "registerpage";
        }
        User savedUser = userService.saveUser(user);
        if (savedUser != null) {
        	
            return "redirect:/loginuser";
        } else {
            return "registerpage";
        }
    }


    @GetMapping("/loginuser")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "loginpage";
    }
    

    @PostMapping("/loginuser")
    public String loginUser(User user, Model model) {
        User loggedInUser = userService.getUserByEmailAndPassword(user.getEmail(), user.getPassword());
        if (loggedInUser != null) {
            return "redirect:/userdetails";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "loginpage";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return "redirect:/userdetails";
    }
  
    
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(email);
            
            if (user != null) {
                passwordResetService.generateResetToken(user);
                
                // Redirect to reset-password page with success message
                redirectAttributes.addAttribute("success", "Email sent successfully!");
                return "redirect:/reset-password";
            } else {
                model.addAttribute("error", "No user found with this email");
                return "forgot-password";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error processing forgot password");
            return "errorpage";
        }
    }


    @GetMapping("/reset-password")
    public String showResetPasswordForm() {
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token, @RequestParam String password, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (passwordResetService.isValidToken(token)) {
                User user = passwordResetService.getUserByToken(token);
                
                if (user != null) {
                	
                    userService.resetPassword(user, password);
                    
                    // Redirect to loginuser page with success message
                    redirectAttributes.addAttribute("resetSuccess", "Password reset successfully!");
                    return "redirect:/loginuser";
                } else {
                    model.addAttribute("error", "User not found for the provided token");
                    return "errorpage";
                }
            } else {
                model.addAttribute("error", "Invalid or expired token");
                return "redirect:/forgot-password?error";
            }
        } catch(Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error resetting password");
            return "errorpage";
        }
    }

    
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        e.printStackTrace();
        model.addAttribute("error", "An error occurred: " + e.getMessage());
        return "errorpage";
    }
}
