package antifraud.service;

import antifraud.exception.IpNotFoundException;
import antifraud.exception.StolenCardNotFound;
import antifraud.model.StolenCardDTO;
import antifraud.model.SuspiciousIpDTO;
import antifraud.model.TransactionDTO;
import antifraud.model.ValidationResultDTO;
import antifraud.model.mapper.StolenCardMapper;
import antifraud.model.mapper.SuspiciousIpMapper;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIpRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static antifraud.service.ValidationResult.getStrictest;

@Service
public class AntiFraudServiceImpl implements AntiFraudService {

    private final SuspiciousIpRepository suspiciousIpRepository;
    private final SuspiciousIpMapper suspiciousIpMapper;

    private final StolenCardRepository stolenCardRepository;
    private final StolenCardMapper stolenCardMapper;

    public AntiFraudServiceImpl(SuspiciousIpRepository suspiciousIpRepository, SuspiciousIpMapper suspiciousIpMapper,
                                StolenCardRepository stolenCardRepository, StolenCardMapper stolenCardMapper) {
        this.suspiciousIpRepository = suspiciousIpRepository;
        this.suspiciousIpMapper = suspiciousIpMapper;
        this.stolenCardRepository = stolenCardRepository;
        this.stolenCardMapper = stolenCardMapper;
    }

    @Override
    public ValidationResultDTO validate(TransactionDTO transactionDTO) {
        var ipValidationResult = validateIp(transactionDTO.ip());

        var cardValidationResult = validateCard(transactionDTO.number());

        var amountValidationResult = validateAmount(transactionDTO.amount());

        return combineValidationResults(ipValidationResult, cardValidationResult, amountValidationResult);
    }

    private ValidationResultDTO combineValidationResults(ValidationResultDTO ipValidationResult,
                                                         ValidationResultDTO cardValidationResult,
                                                         ValidationResultDTO amountValidationResult) {
        var aggregatedResults = Stream.of(ipValidationResult, cardValidationResult, amountValidationResult)
                .collect(Collectors.groupingBy(ValidationResultDTO::result));
        var strictestResultKey = getStrictest(aggregatedResults.keySet());
        var strictest = aggregatedResults.get(strictestResultKey);

        return new ValidationResultDTO(strictestResultKey,
                strictest.stream().map(ValidationResultDTO::info)
                        .distinct()
                        .sorted()
                        .collect(Collectors.joining(", ")));
    }

    private ValidationResultDTO validateIp(String ip) {
        if (suspiciousIpRepository.findSuspiciousIpByIp(ip).isPresent()) {
            return new ValidationResultDTO(ValidationResult.PROHIBITED, "ip");
        }
        return new ValidationResultDTO(ValidationResult.ALLOWED, "none");
    }

    private ValidationResultDTO validateCard(String number) {
        if (stolenCardRepository.findStolenCardByNumber(number).isPresent()) {
            return new ValidationResultDTO(ValidationResult.PROHIBITED, "card-number");
        }
        return new ValidationResultDTO(ValidationResult.ALLOWED, "none");
    }

    private ValidationResultDTO validateAmount(double amount) {
        if (amount <= 200) {
            return new ValidationResultDTO(ValidationResult.ALLOWED, "none");
        } else if ( amount > 200 && amount <= 1500) {
            return new ValidationResultDTO(ValidationResult.MANUAL_PROCESSING, "amount");
        } else {
            return new ValidationResultDTO(ValidationResult.PROHIBITED, "amount");
        }
    }

    @Override
    public SuspiciousIpDTO addSuspiciousIp(SuspiciousIpDTO ip) {
        if (suspiciousIpRepository.findSuspiciousIpByIp(ip.getIp()).isPresent()) {
            throw new RuntimeException("IP already exists");
        }
        return suspiciousIpMapper.toDto(suspiciousIpRepository.save(suspiciousIpMapper.toEntity(ip)));
    }

    @Override
    public void removeSuspiciousIp(String ip) {
        var ipToDelete = suspiciousIpRepository.findSuspiciousIpByIp(ip)
                .orElseThrow(IpNotFoundException::new);

        suspiciousIpRepository.deleteSuspiciousIpById(ipToDelete.getId());
    }

    @Override
    public List<SuspiciousIpDTO> getSuspiciousIps() {
        List<SuspiciousIpDTO> ips = new ArrayList<>();
        suspiciousIpRepository.findAll()
                .forEach(ip -> ips.add(suspiciousIpMapper.toDto(ip)));
        return ips;
    }

    @Override
    public StolenCardDTO addStolenCard(StolenCardDTO stolenCardDTO) {
        if (stolenCardRepository.findStolenCardByNumber(stolenCardDTO.getNumber()).isPresent()) {
            throw new RuntimeException("Card already exists");
        }
        return stolenCardMapper.toDto(stolenCardRepository.save(stolenCardMapper.toEntity(stolenCardDTO)));
    }

    @Override
    public List<StolenCardDTO> getStolenCards() {
        List<StolenCardDTO> stolenCards = new ArrayList<>();
        stolenCardRepository.findAll().forEach(card -> stolenCards.add(stolenCardMapper.toDto(card)));
        return stolenCards;
    }

    @Override
    public void removeStolenCard(String number) {
        var stolenCard = stolenCardRepository.findStolenCardByNumber(number)
                .orElseThrow(StolenCardNotFound::new);
        stolenCardRepository.deleteStolenCardById(stolenCard.getId());
    }
}
