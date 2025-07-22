package com.fyp.AABookingProject.authentication.controller;

import com.fyp.AABookingProject.authentication.model.LoginRequest;
import com.fyp.AABookingProject.authentication.model.SignUpRequestAdvisor;
import com.fyp.AABookingProject.authentication.model.SignUpRequestStudent;
import com.fyp.AABookingProject.core.commonModel.response.JwtResponse;
import com.fyp.AABookingProject.core.commonModel.response.MessageResponse;
import com.fyp.AABookingProject.core.entity.Advisor;
import com.fyp.AABookingProject.core.entity.Student;
import com.fyp.AABookingProject.core.entity.User;
import com.fyp.AABookingProject.core.enumClass.ERole;
import com.fyp.AABookingProject.core.repository.AdvisorRepository;
import com.fyp.AABookingProject.core.repository.StudentRepository;
import com.fyp.AABookingProject.core.repository.UserRepository;
import com.fyp.AABookingProject.security.jwt.JwtUtils;
import com.fyp.AABookingProject.security.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            StudentRepository studentRepository,
            AdvisorRepository advisorRepository,
            PasswordEncoder encoder,
            JwtUtils jwtUtils
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(HttpServletRequest request) {
        return ResponseEntity.ok("Test Successfully");
    }

    // Unified login handler
    private ResponseEntity<?> performLogin(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String role = userDetails.getAuthorities().iterator().next().getAuthority();

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getEmail(),
                    role
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    // Login endpoints
    @PostMapping("/loginStudent")
    public ResponseEntity<?> loginStudent(@Valid @RequestBody LoginRequest request) {
        return performLogin(request.getUsername(), request.getPassword());
    }

    @PostMapping("/loginAdvisor")
    public ResponseEntity<?> loginAdvisor(@Valid @RequestBody LoginRequest request) {
        return performLogin(request.getUsername(), request.getPassword());
    }

    @PostMapping("/loginAdmin")
    public ResponseEntity<?> loginAdmin(@Valid @RequestBody LoginRequest request) {
        return performLogin(request.getUsername(), request.getPassword());
    }

    // Student registration
    @PostMapping("/registerStudent")
    public ResponseEntity<?> registerStudent(@Valid @RequestBody SignUpRequestStudent request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(ERole.STUDENT);
        user.setEnabled(true);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        Student student = new Student();
        student.setFullName(request.getFirstName() + " " + request.getLastName());
        student.setPhone(request.getPhoneNumber());
        student.setUser(user);
        user.setStudent(student);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Student registered successfully!"));
    }

    // Advisor registration
    @PostMapping("/registerAdvisor")
    public ResponseEntity<?> registerAdvisor(@Valid @RequestBody SignUpRequestAdvisor request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(ERole.ADVISOR);
        user.setEnabled(true);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        Advisor advisor = new Advisor();
        advisor.setFullName(request.getFirstName() + " " + request.getLastName());
        advisor.setDepartment(request.getDepartment());
        advisor.setPhone(request.getPhoneNumber());
        advisor.setUser(user);
        user.setAdvisor(advisor);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Advisor registered successfully!"));
    }
}
