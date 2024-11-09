package antifraud.controller;


import antifraud.model.*;
import antifraud.service.AntiFraudService;
import antifraud.validation.ValidCardNumber;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/antifraud")
@Validated
@Slf4j
public class AntiFraudController {

    private final AntiFraudService antiFraudService;

    public AntiFraudController(AntiFraudService antiFraudService) {
        this.antiFraudService = antiFraudService;
    }

    @PostMapping("/transaction")
    public ValidationResultDTO validateTransaction(@RequestBody @Validated TransactionDTO transactionDTO) {
        return antiFraudService.validate(transactionDTO);
    }

    @PutMapping("/transaction")
    public TransactionFeedbackOutDTO addTransactionFeedback(@RequestBody @Validated TransactionFeedbackInDto feedbackIn) {
        return antiFraudService.addFeedback(feedbackIn);
    }

    @GetMapping("/history")
    public List<TransactionFeedbackOutDTO> getTransactionHistory() {
        return antiFraudService.getTransactionsHistory();
    }

    @GetMapping("/history/{number}")
    public List<TransactionFeedbackOutDTO> getTransactionHistory(@PathVariable @ValidCardNumber String number) {
        return antiFraudService.getTransactionHistory(number);
    }

    @PostMapping("/suspicious-ip")
    public SuspiciousIpDTO addSuspiciousIp(@RequestBody @Validated SuspiciousIpDTO ip) {
        return antiFraudService.addSuspiciousIp(ip);
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public Map<String, Object> deleteSuspiciousIp(
            @PathVariable
            @Pattern(
                    regexp = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d{1,2})){3}$",
                    message = "Invalid IP address"
            ) String ip) {
        antiFraudService.removeSuspiciousIp(ip);
        return Map.of("status", "IP %s successfully removed!".formatted(ip));
    }

    @GetMapping("/suspicious-ip")
    public List<SuspiciousIpDTO> getSuspiciousIps() {
        return antiFraudService.getSuspiciousIps();
    }

    @PostMapping("/stolencard")
    public StolenCardDTO addStolenCard(@RequestBody @Validated StolenCardDTO stolenCardDTO) {
        return antiFraudService.addStolenCard(stolenCardDTO);
    }

    @GetMapping("/stolencard")
    public List<StolenCardDTO> getStolenCards() {
        return antiFraudService.getStolenCards();
    }

    @DeleteMapping("/stolencard/{number}")
    public Map<String, Object> deleteStolenCard(@PathVariable @ValidCardNumber String number) {
        antiFraudService.removeStolenCard(number);
        return Map.of("status", "Card %s successfully removed!".formatted(number));
    }
}

