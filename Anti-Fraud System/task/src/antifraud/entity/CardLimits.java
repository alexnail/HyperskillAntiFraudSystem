package antifraud.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "card_limits")
@Getter
@Setter
public class CardLimits {

    @Id
    private String cardNumber;

    private Long allowedMax;

    private Long manualProcessingMax;
}
