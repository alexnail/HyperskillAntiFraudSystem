package antifraud.service;

import antifraud.model.TransactionDTO;

import java.util.Map;

public interface AntiFraudService {
    Map<String, Object> validate(TransactionDTO transactionDTO);
}
