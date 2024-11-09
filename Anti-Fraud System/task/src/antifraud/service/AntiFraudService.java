package antifraud.service;

import antifraud.model.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AntiFraudService {
    ValidationResultDTO validate(TransactionDTO transactionDTO);

    @Transactional
    SuspiciousIpDTO addSuspiciousIp(SuspiciousIpDTO ip);

    @Transactional
    void removeSuspiciousIp(String ip);

    @Transactional(readOnly = true)
    List<SuspiciousIpDTO> getSuspiciousIps();

    @Transactional
    StolenCardDTO addStolenCard(StolenCardDTO stolenCardDTO);

    @Transactional(readOnly = true)
    List<StolenCardDTO> getStolenCards();

    @Transactional
    void removeStolenCard(String number);

    @Transactional
    TransactionFeedbackOutDTO addFeedback(TransactionFeedbackInDto feedbackIn);

    @Transactional(readOnly = true)
    List<TransactionFeedbackOutDTO> getTransactionsHistory();

    @Transactional(readOnly = true)
    List<TransactionFeedbackOutDTO> getTransactionHistory(String number);
}
