package antifraud.service;

import antifraud.entity.Transaction;
import antifraud.exception.IpNotFoundException;
import antifraud.exception.StolenCardNotFound;
import antifraud.model.StolenCardDTO;
import antifraud.model.SuspiciousIpDTO;
import antifraud.model.TransactionDTO;
import antifraud.model.ValidationResultDTO;
import antifraud.model.mapper.StolenCardMapper;
import antifraud.model.mapper.SuspiciousIpMapper;
import antifraud.model.mapper.TransactionMapper;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIpRepository;
import antifraud.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static antifraud.service.ValidationResult.getStrictest;

@Slf4j
@Service
public class AntiFraudServiceImpl implements AntiFraudService {

    private final SuspiciousIpRepository suspiciousIpRepository;
    private final SuspiciousIpMapper suspiciousIpMapper;

    private final StolenCardRepository stolenCardRepository;
    private final StolenCardMapper stolenCardMapper;

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public AntiFraudServiceImpl(SuspiciousIpRepository suspiciousIpRepository, SuspiciousIpMapper suspiciousIpMapper,
                                StolenCardRepository stolenCardRepository, StolenCardMapper stolenCardMapper,
                                TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.suspiciousIpRepository = suspiciousIpRepository;
        this.suspiciousIpMapper = suspiciousIpMapper;
        this.stolenCardRepository = stolenCardRepository;
        this.stolenCardMapper = stolenCardMapper;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public ValidationResultDTO validate(TransactionDTO transactionDTO) {
        transactionRepository.save(transactionMapper.toEntity(transactionDTO));

        var ipValidationResult = validateIp(transactionDTO.ip());
        var cardValidationResult = validateCard(transactionDTO.number());
        var amountValidationResult = validateAmount(transactionDTO.amount());
        var ipCorrelationValidationResult = validateIpCorrelation(transactionDTO);
        var regionCorrelationValidationResult = validateRegionCorrelation(transactionDTO);

        return combineValidationResults(ipValidationResult, cardValidationResult, amountValidationResult,
                ipCorrelationValidationResult, regionCorrelationValidationResult);
    }

    private ValidationResultDTO combineValidationResults(ValidationResultDTO... results) {
        var aggregatedResults = Stream.of(results)
                .collect(Collectors.groupingBy(ValidationResultDTO::result));
        var strictestResultKey = getStrictest(aggregatedResults.keySet());
        var strictest = aggregatedResults.get(strictestResultKey);

        return new ValidationResultDTO(strictestResultKey,
                strictest.stream().map(ValidationResultDTO::info)
                        .distinct()
                        .sorted()
                        .collect(Collectors.joining(", ")));
    }

    private ValidationResultDTO validateRegionCorrelation(TransactionDTO transactionDTO) {
        var found = transactionRepository.findAllByNumber(transactionDTO.number());

        var uniqueRegions = found.stream()
                .filter(t -> !t.getRegion().equals(transactionDTO.region()))
                .filter(t -> t.getDate().isAfter(transactionDTO.date().minusHours(1))
                        && t.getDate().isBefore(transactionDTO.date()) )
                .map(Transaction::getRegion)
                .collect(Collectors.toSet());

        if (uniqueRegions.size() > 2) {
            return new ValidationResultDTO(ValidationResult.PROHIBITED, "region-correlation");
        } else if (uniqueRegions.size() == 2) {
            return new ValidationResultDTO(ValidationResult.MANUAL_PROCESSING, "region-correlation");
        } else {
            return new ValidationResultDTO(ValidationResult.ALLOWED, "none");
        }
    }

    private ValidationResultDTO validateIpCorrelation(TransactionDTO transactionDTO) {
        var found = transactionRepository.findAllByNumber(transactionDTO.number());

        var uniqueIps = found.stream()
                .filter(t -> !t.getIp().equals(transactionDTO.ip()))
                .filter(t -> t.getDate().isAfter(transactionDTO.date().minusHours(1))
                        && t.getDate().isBefore(transactionDTO.date()))
                .map(Transaction::getIp)
                .collect(Collectors.toSet());

        if (uniqueIps.size() > 2) {
            return new ValidationResultDTO(ValidationResult.PROHIBITED, "ip-correlation");
        } else if (uniqueIps.size() == 2) {
            return new ValidationResultDTO(ValidationResult.MANUAL_PROCESSING, "ip-correlation");
        } else {
            return new ValidationResultDTO(ValidationResult.ALLOWED, "none");
        }
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
