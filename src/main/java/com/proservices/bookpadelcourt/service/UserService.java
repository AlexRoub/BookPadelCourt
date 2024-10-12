package com.proservices.bookpadelcourt.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.proservices.bookpadelcourt.dto.UserRegistrationDto;
import com.proservices.bookpadelcourt.entity.User;
import com.proservices.bookpadelcourt.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public UserService(UserRepository userRepository,
		PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public User registerNewUser(UserRegistrationDto registrationDto) {
		// Check if username or email already exists
		if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
			throw new RuntimeException("Username is already taken!");
		}

		if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
			throw new RuntimeException("Email is already in use!");
		}

		// Create new user entity
		User user = new User();
		user.setUsername(registrationDto.getUsername());
		user.setEmail(registrationDto.getEmail());
		user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
		user.setRole("USER"); // Default role

		return userRepository.save(user);
	}
}
