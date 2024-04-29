package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;


@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;
    

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public User saveUser(User user) {
        return userRepository.save(user);
    }
 
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
        //resetIdSequence();
    }
   
	public User getUserByEmailAndPassword(String email, String password) {
		return userRepository.findByEmailAndPassword(email, password);
	}


	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}


	public void resetPassword(User user, String password) {
		
		 user.setPassword(password);
		userRepository.save(user);
	}



}
