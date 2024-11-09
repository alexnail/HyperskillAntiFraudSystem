package antifraud.entity;

import antifraud.service.ValidationResult;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@ToString
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private Long amount;

    private String ip;

    private String number;

    private String region;

    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private ValidationResult result;

    @Enumerated(EnumType.STRING)
    private ValidationResult feedback;
}
