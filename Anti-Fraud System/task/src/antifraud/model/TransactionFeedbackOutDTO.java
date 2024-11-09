package antifraud.model;

import antifraud.service.ValidationResult;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TransactionFeedbackOutDTO {

    private Long transactionId;
    private Long amount;
    private String ip;
    private String number;
    private String region;
    private LocalDateTime date;
    private ValidationResult result;
    private String feedback;

}
