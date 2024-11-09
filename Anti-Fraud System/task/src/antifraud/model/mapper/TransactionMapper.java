package antifraud.model.mapper;

import antifraud.entity.Transaction;
import antifraud.model.TransactionDTO;
import antifraud.model.TransactionFeedbackOutDTO;
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


    public TransactionFeedbackOutDTO toFeedbackOut(Transaction entity) {
        return TransactionFeedbackOutDTO.builder()
                .transactionId(entity.getId())
                .amount(entity.getAmount())
                .ip(entity.getIp())
                .number(entity.getNumber())
                .region(entity.getRegion())
                .date(entity.getDate())
                .result(entity.getResult())
                .feedback(entity.getFeedback() == null ? "" : entity.getFeedback().name())
                .build();
    }
}
