package antifraud.service;

import antifraud.model.StolenCardDTO;
import antifraud.model.SuspiciousIpDTO;
import antifraud.model.TransactionDTO;
import antifraud.model.ValidationResultDTO;
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
}
