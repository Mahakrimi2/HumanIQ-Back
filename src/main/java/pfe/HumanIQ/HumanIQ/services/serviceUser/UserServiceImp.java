package pfe.HumanIQ.HumanIQ.services.serviceUser;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.DTO.request.ChangePasswordRequest;
import pfe.HumanIQ.HumanIQ.models.*;
import pfe.HumanIQ.HumanIQ.repositories.DepartmentRepository;
import pfe.HumanIQ.HumanIQ.repositories.RoleRepository;
import pfe.HumanIQ.HumanIQ.repositories.TokenRepo;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImp implements UserService, UserDetailsService {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepo tokenRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    public UserServiceImp(UserRepo userRepository, PasswordEncoder passwordEncoder, TokenRepo tokenRepository, RoleRepository roleRepository, DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    @Override
    public List<User> getAllUsersemp() {
        Role role = roleRepository.findByName(UserRole.ROLE_EMPLOYEE);
        return userRepository.findByRoles(role);
    }



    @Override
    public User createEmployee(User user,Long id) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("username already exists");
        }
        Department department = departmentRepository.findById(id).orElse(null);
        if (department == null) {
            return null;
        }
        System.out.println(id);
        Role role = roleRepository.findByName(UserRole.ROLE_EMPLOYEE);
        ////user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(role);
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println(role);
        newUser.setRoles(userRoles);
        newUser.setFullname(user.getFullname());
        newUser.setAddress(user.getAddress());
        newUser.setNationalID(user.getNationalID());
        newUser.setHireDate(user.getHireDate());
        newUser.setGender(user.getGender());
        newUser.setPosition(user.getPosition());
        newUser.setTelNumber(user.getTelNumber());
        newUser.setDepartment(department);
        return userRepository.save(newUser);
    }
    @Override
    public User createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("username already exists");
        }

        Role role = roleRepository.findByName(UserRole.ROLE_EMPLOYEE);
        ////user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(role);
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println(role);
        newUser.setRoles(userRoles);
        newUser.setFullname(user.getFullname());
        newUser.setAddress(user.getAddress());
        newUser.setNationalID(user.getNationalID());
        newUser.setHireDate(user.getHireDate());
        newUser.setGender(user.getGender());
        newUser.setPosition(user.getPosition());
        newUser.setTelNumber(user.getTelNumber());
        newUser.setDepartments(user.getDepartments());
        return userRepository.save(newUser);
    }





    @Override
    public User updateUser(User user,Long id) {
        User existingUser = findUserById(id);
        if(existingUser != null){
            Optional<User> userWithEmail = userRepository.findByUsername(user.getUsername());
//            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(user.getId())) {
//                throw new RuntimeException("Email already exists");
//            }
//            if (user.getPassword() != null && !user.getPassword().equals(existingUser.getPassword())) {
//                user.setPassword(passwordEncoder.encode(user.getPassword()));
//            } else {
//                user.setPassword(existingUser.getPassword());
//            }
            existingUser.setUsername(user.getUsername());
            //existingUser.setRoles(user.getRoles());
            existingUser.setFullname(user.getFullname());
            existingUser.setAddress(user.getAddress());
            //existingUser.setGender(user.getGender() != null ? user.getGender() : existingUser.getGender());
            existingUser.setPosition(user.getPosition() );
            existingUser.setTelNumber(user.getTelNumber());
            return userRepository.save(existingUser);
        } else {
            throw new RuntimeException("User does not exist");
        }
    }

    public Role createRoleIfNotExist(UserRole roleName) {
        Role existingRoles = roleRepository.findByName(roleName);
       if (existingRoles != null) {
           return null;
       }
        Role role = new Role(roleName);
        return roleRepository.save(role);
    }


    @Override
    public void deleteUser(Long id) {

        userRepository.deleteById(id);
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }


    public Token createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = createdAt.plusHours(24); // Token valid for 24 hours

        Token verificationToken = new Token();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setCreatedAt(createdAt);
        verificationToken.setExpiresAt(expiresAt);
        verificationToken.setValidatedAt(null); // Token is not validated yet

        tokenRepository.save(verificationToken);
        return verificationToken;
    }
    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    public void changePassword(ChangePasswordRequest request) {
        User currentUser = getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());


        currentUser.setPassword(hashedNewPassword);
        userRepository.save(currentUser);
    }


}


