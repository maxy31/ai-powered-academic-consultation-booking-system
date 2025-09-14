package com.fyp.AABookingProject.authentication.controller;

import com.fyp.AABookingProject.authentication.model.*;
import com.fyp.AABookingProject.core.commonModel.response.JwtResponse;
import com.fyp.AABookingProject.core.commonModel.response.MessageResponse;
import com.fyp.AABookingProject.core.entity.Advisor;
import com.fyp.AABookingProject.core.entity.Department;
import com.fyp.AABookingProject.core.entity.Student;
import com.fyp.AABookingProject.core.entity.User;
import com.fyp.AABookingProject.core.enumClass.ERole;
import com.fyp.AABookingProject.core.repository.AdvisorRepository;
import com.fyp.AABookingProject.core.repository.DepartmentRepository;
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

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AdvisorRepository advisorRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            StudentRepository studentRepository,
            AdvisorRepository advisorRepository, DepartmentRepository departmentRepository,
            PasswordEncoder encoder,
            JwtUtils jwtUtils
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.advisorRepository = advisorRepository;
        this.departmentRepository = departmentRepository;
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
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhoneNumber());
        user.setRole(ERole.STUDENT);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        Student student = new Student();
        student.setUser(user);

        Optional<Advisor> advisorTarget = advisorRepository.findById(request.getAdvisorId());
        advisorTarget.ifPresent(student::setAdvisor);

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
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhoneNumber());
        user.setRole(ERole.ADVISOR);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        Advisor advisor = new Advisor();
        advisor.setDepartmentId(request.getDepartmentId());
        advisor.setUser(user);
        user.setAdvisor(advisor);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Advisor registered successfully!"));
    }

    @GetMapping("/showAdvisors")
    public ResponseEntity<List<AdvisorListDTO>> showAdvisors(){
        List<Advisor> advisors = advisorRepository.findAll();
        List<AdvisorListDTO> advisorList = advisors.stream()
                .map(advisor -> new AdvisorListDTO(
                        advisor.getId(),
                        advisor.getUser().getFirstName() + " " + advisor.getUser().getLastName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(advisorList);
    }

    @GetMapping("/showDepartments")
    public ResponseEntity<List<DepartmentListDTO>> showDepartments(){
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentListDTO> departmentList = departments.stream()
                .map(department -> new DepartmentListDTO(
                        department.getId(),
                        department.getDepartmentName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(departmentList);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        request.getSession().invalidate();  // 使 session 失效
        return ResponseEntity.ok("Logged out");
    }
}
