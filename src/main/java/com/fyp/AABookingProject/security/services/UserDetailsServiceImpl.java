package com.fyp.AABookingProject.security.services;

import com.fyp.AABookingProject.core.entity.Advisor;
import com.fyp.AABookingProject.core.entity.Student;
import com.fyp.AABookingProject.core.entity.User;
import com.fyp.AABookingProject.core.repository.AdvisorRepository;
import com.fyp.AABookingProject.core.repository.StudentRepository;
import com.fyp.AABookingProject.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return UserDetailsImpl.build(user.get());
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }

    @Transactional
    public UserDetails loadById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserDetailsImpl.build(user);
    }
}
