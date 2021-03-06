package sop.ewallet.authentication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;
import sop.ewallet.authentication.exception.AppException;
import sop.ewallet.authentication.model.Role;
import sop.ewallet.authentication.model.RoleName;
import sop.ewallet.authentication.model.User;
import sop.ewallet.authentication.payload.ApiResponse;
import sop.ewallet.authentication.payload.JwtAuthenticationResponse;
import sop.ewallet.authentication.payload.LoginRequest;
import sop.ewallet.authentication.payload.SignUpRequest;
import sop.ewallet.authentication.payload.UserDetailResponse;
import sop.ewallet.authentication.repository.RoleRepository;
import sop.ewallet.authentication.repository.UserRepository;
import sop.ewallet.authentication.security.CurrentUser;
import sop.ewallet.authentication.security.JwtTokenProvider;
import sop.ewallet.authentication.security.UserPrincipal;

@RestController
@RequestMapping("/")
public class AuthenticationController {

  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Autowired
  JwtTokenProvider tokenProvider;

  @Value("${service.account}")
  private String accountUrl;

  private RestTemplate restTemplate = new RestTemplate();

  @RequestMapping("/me")
  public ResponseEntity<UserDetailResponse> getUserProfile(@CurrentUser UserPrincipal currentUser) {
    return ResponseEntity.ok(
        new UserDetailResponse(
            currentUser.getId(),
            currentUser.getName(),
            currentUser.getUsername(),
            currentUser.getEmail()
        )
    );
  }

  @PostMapping("/authenticate")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getUsername(),
            loginRequest.getPassword()
        )
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    String jwt = tokenProvider.generateToken(authentication);
    return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
  }

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return new ResponseEntity<>(new ApiResponse(false, "Username is already taken!"),
          HttpStatus.BAD_REQUEST);
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return new ResponseEntity<>(new ApiResponse(false, "Email Address already in use!"),
          HttpStatus.BAD_REQUEST);
    }

    // Creating user's account
    User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
        signUpRequest.getEmail(), signUpRequest.getPassword());

    user.setPassword(passwordEncoder.encode(user.getPassword()));

    Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
        .orElseThrow(() -> new AppException("User Role not set."));

    user.setRoles(Collections.singleton(userRole));

    User result = userRepository.save(user);

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            signUpRequest.getUsername(),
            signUpRequest.getPassword()
        )
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    String jwt = tokenProvider.generateToken(authentication);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + jwt);

    HttpEntity<String> entity = new HttpEntity<String>(null, headers);

    try {
      ApiResponse createAccount = this.restTemplate
          .postForObject(accountUrl + "/create", entity, ApiResponse.class);

      if (createAccount != null && !createAccount.getSuccess()) {
        userRepository.deleteById(result.getId());
        return new ResponseEntity<>(new ApiResponse(false, "Error creating account!"),
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } catch (Exception e) {
      userRepository.deleteById(result.getId());
      return new ResponseEntity<>(new ApiResponse(false, "Error creating account!"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }

    URI location = ServletUriComponentsBuilder
        .fromCurrentContextPath().path("/api/users/{username}")
        .buildAndExpand(result.getUsername()).toUri();

    return ResponseEntity.created(location)
        .body(new ApiResponse(true, "User registered successfully"));
  }
}