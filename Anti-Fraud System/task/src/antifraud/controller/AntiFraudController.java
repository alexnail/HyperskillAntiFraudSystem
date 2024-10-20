package antifraud.controller;


import antifraud.model.TransactionDTO;
import antifraud.service.AntiFraudService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/antifraud")
public class AntiFraudController {

    private final AntiFraudService antiFraudService;

    public AntiFraudController(AntiFraudService antiFraudService) {
        this.antiFraudService = antiFraudService;
    }

    @PostMapping("/transaction")
    public Map<String, Object> validateTransaction(@RequestBody @Validated TransactionDTO transactionDTO) {
        return antiFraudService.validate(transactionDTO);
    }
}
