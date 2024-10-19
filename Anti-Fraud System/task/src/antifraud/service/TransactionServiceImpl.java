package antifraud.service;

import antifraud.model.TransactionDTO;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Override
    public Map<String, Object> validate(TransactionDTO transactionDTO) {
        if (transactionDTO.amount() <= 200) {
            return Map.of("result", ValidationResult.ALLOWED.name());
        } else if ( transactionDTO.amount() > 200 && transactionDTO.amount() <= 1500) {
            return Map.of("result", ValidationResult.MANUAL_PROCESSING.name());
        } else {
            return Map.of("result", ValidationResult.PROHIBITED.name());
        }
    }
}
