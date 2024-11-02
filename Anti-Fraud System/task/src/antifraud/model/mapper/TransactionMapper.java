package antifraud.model.mapper;

import antifraud.entity.Transaction;
import antifraud.model.TransactionDTO;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    public Transaction toEntity(TransactionDTO transactionDTO) {
        Transaction entity = new Transaction();
        entity.setAmount(transactionDTO.amount());
        entity.setIp(transactionDTO.ip());
        entity.setNumber(transactionDTO.number());
        entity.setRegion(transactionDTO.region());
        entity.setDate(transactionDTO.date());
        return entity;
    }
}
