package antifraud.controller;

import antifraud.model.UserAccessDTO;
import antifraud.model.UserDTO;
import antifraud.model.UserRoleDTO;
import antifraud.service.UserDetailsServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserDetailsServiceImpl userDetailsService;

    public AuthController(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/user")
    public ResponseEntity<UserDTO> createUser(@RequestBody @Validated UserDTO userDTO) {
        return new ResponseEntity<>(userDetailsService.createUser(userDTO), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public List<UserDTO> getAllUsers() {
        return userDetailsService.getAllUsers();
    }

    @DeleteMapping("/user/{username}")
    public Map<String, Object> deleteUser(@PathVariable String username) {
        userDetailsService.deleteUser(username);
        return Map.of("username", username,
                "status", "Deleted successfully!");
    }

    @PutMapping("/role")
    public UserDTO setUserRole(@RequestBody @Validated UserRoleDTO userRoleDTO) {
        return userDetailsService.setUserRole(userRoleDTO);
    }

    @PutMapping("/access")
    public Map<String, Object> changeUserAccess(@RequestBody @Validated UserAccessDTO userAccessDTO) {
        String status = userDetailsService.changeUserAccess(userAccessDTO);
        return Map.of("status", "User %s %s!".formatted(userAccessDTO.username(), status));
    }
}
