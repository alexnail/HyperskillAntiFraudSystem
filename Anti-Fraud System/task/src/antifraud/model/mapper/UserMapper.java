package antifraud.model.mapper;

import antifraud.entity.User;
import antifraud.model.UserDTO;
import antifraud.security.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO toDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .password(user.getPassword())
                .locked(user.isLocked())
                .role(user.getRole())
                .authorities(List.of(roleToAuthority(user.getRole())))
                .build();
    }

    public User toEntity(UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.getName());
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setLocked(userDTO.isLocked());
        user.setRole(userDTO.getRole());
        return user;
    }

    public static GrantedAuthority roleToAuthority(UserRole role) {
        return () -> "ROLE_" + role.name();
    }
}
