package antifraud.service;

import antifraud.entity.User;
import antifraud.model.UserAccessDTO;
import antifraud.model.UserDTO;
import antifraud.model.UserRoleDTO;
import antifraud.model.mapper.UserMapper;
import antifraud.repository.UserRepository;
import antifraud.security.UserRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDetailsServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findUserByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return userMapper.toDto(user);
    }

    public UserDTO createUser(UserDTO userDTO) {
        Optional<User> existing = userRepository.findUserByUsernameIgnoreCase(userDTO.getUsername());
        if (existing.isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.count() == 0) {
            userDTO.setRole(UserRole.ADMINISTRATOR);
        } else {
            userDTO.setRole(UserRole.MERCHANT);
            userDTO.setLocked(true);
        }
        var saved = userRepository.save(userMapper.toEntity(userDTO));
        return userMapper.toDto(saved);
    }

    public List<UserDTO> getAllUsers() {
        List<UserDTO> users = new ArrayList<>();
        userRepository.findAll().forEach(user -> users.add(userMapper.toDto(user)));
        return users;
    }

    @Transactional
    public void deleteUser(String username) {
        var user = userRepository.findUserByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        userRepository.deleteUserByUsernameIgnoreCase(username);
    }

    @Transactional
    public UserDTO setUserRole(UserRoleDTO roleDTO) {
        var user = userRepository.findUserByUsernameIgnoreCase(roleDTO.username())
                .orElseThrow(() -> new UsernameNotFoundException(roleDTO.username()));
        if (roleDTO.role().equals(user.getRole().name())) {
            throw new RuntimeException("Role already assigned");
        }
        user.setRole(UserRole.valueOf(roleDTO.role()));
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public String changeUserAccess(UserAccessDTO accessDTO) {
        var user = userRepository.findUserByUsernameIgnoreCase(accessDTO.username())
                .orElseThrow(() -> new UsernameNotFoundException(accessDTO.username()));
        user.setLocked(accessDTO.operation().equals("LOCK"));
        var saved = userRepository.save(user);
        return saved.isLocked() ? "locked" : "unlocked";
    }
}
