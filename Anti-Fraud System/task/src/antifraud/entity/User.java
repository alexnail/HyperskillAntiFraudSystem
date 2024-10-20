package antifraud.entity;

import antifraud.security.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    private String username;

    private String password;

    private boolean locked;

    @Enumerated(EnumType.STRING)
    private UserRole role;
}
