package antifraud.service;

import antifraud.entity.CardLimits;
import antifraud.entity.Transaction;
import antifraud.exception.FeedbackEqualToResultException;
import antifraud.exception.IpNotFoundException;
import antifraud.exception.StolenCardNotFound;
import antifraud.exception.TransactionNotFoundException;
import antifraud.model.*;
import antifraud.model.mapper.StolenCardMapper;
import antifraud.model.mapper.SuspiciousIpMapper;
import antifraud.model.mapper.TransactionMapper;
import antifraud.repository.CardLimitsRepository;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIpRepository;
import antifraud.repository.TransactionRepository;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static antifraud.service.ValidationResult.getStrictest;

@Slf4j
@Service
public class AntiFraudServiceImpl implements AntiFraudService {

    private static final long DEFAULT_ALLOWED_MAX = 200L;
    private static final long DEFAULT_MANUAL_PROCESSING_MAX = 1500L;

    private final SuspiciousIpRepository suspiciousIpRepository;
    private final SuspiciousIpMapper suspiciousIpMapper;

    private final StolenCardRepository stolenCardRepository;
    private final StolenCardMapper stolenCardMapper;

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    private final CardLimitsRepository cardLimitsRepository;

    public AntiFraudServiceImpl(SuspiciousIpRepository suspiciousIpRepository, SuspiciousIpMapper suspiciousIpMapper,
                                StolenCardRepository stolenCardRepository, StolenCardMapper stolenCardMapper,
                                TransactionRepository transactionRepository, TransactionMapper transactionMapper,
                                CardLimitsRepository cardLimitsRepository) {
        this.suspiciousIpRepository = suspiciousIpRepository;
        this.suspiciousIpMapper = suspiciousIpMapper;
        this.stolenCardRepository = stolenCardRepository;
        this.stolenCardMapper = stolenCardMapper;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.cardLimitsRepository = cardLimitsRepository;
    }

    @Override
    public ValidationResultDTO validate(TransactionDTO transactionDTO) {
        var saved = transactionRepository.save(transactionMapper.toEntity(transactionDTO));

        var ipValidationResult = validateIp(transactionDTO.ip());
        var cardValidationResult = validateCard(transactionDTO.number());
        var amountValidationResult = validateAmount(transactionDTO.number(), transactionDTO.amount());
        var ipCorrelationValidationResult = validateIpCorrelation(transactionDTO);
        var regionCorrelationValidationResult = validateRegionCorrelation(transactionDTO);

        var validationResultDTO = combineValidationResults(ipValidationResult, cardValidationResult, amountValidationResult,
                ipCorrelationValidationResult, regionCorrelationValidationResult);

        saved.setResult(validationResultDTO.result());
        transactionRepository.save(saved);

        return validationResultDTO;
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

    private ValidationResultDTO validateAmount(@NotEmpty String cardNumber, double amount) {
        var cardLimits = cardLimitsRepository.findById(cardNumber);
        if (amount <= getAllowedMax(cardLimits)) {
            return new ValidationResultDTO(ValidationResult.ALLOWED, "none");
        } else if ( amount > getAllowedMax(cardLimits) && amount <= getManualProcessingMax(cardLimits)) {
            return new ValidationResultDTO(ValidationResult.MANUAL_PROCESSING, "amount");
        } else {
            return new ValidationResultDTO(ValidationResult.PROHIBITED, "amount");
        }
    }

    private long getManualProcessingMax(Optional<CardLimits> cardLimits) {
        return cardLimits.map(this::getManualProcessingMax).orElse(DEFAULT_MANUAL_PROCESSING_MAX);
    }

    private long getManualProcessingMax(CardLimits cardLimits) {
        var manualProcessingMax = cardLimits.getManualProcessingMax();
        return manualProcessingMax == null ? DEFAULT_MANUAL_PROCESSING_MAX : manualProcessingMax;
    }

    private long getAllowedMax(Optional<CardLimits> cardLimits) {
        return cardLimits.map(this::getAllowedMax).orElse(DEFAULT_ALLOWED_MAX);
    }

    private long getAllowedMax(CardLimits cardLimits) {
        var allowedMax = cardLimits.getAllowedMax();
        return allowedMax == null ? DEFAULT_ALLOWED_MAX : allowedMax;
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

    @Override
    public TransactionFeedbackOutDTO addFeedback(TransactionFeedbackInDto feedback) {
        var transaction = transactionRepository.findById(feedback.getTransactionId())
                .orElseThrow(TransactionNotFoundException::new);
        if (null != transaction.getFeedback()) {
            throw new RuntimeException("Feedback already exists");
        }

        var transactionResult = transaction.getResult();
        var feedbackResult = ValidationResult.valueOf(feedback.getFeedback());
        if (transactionResult == feedbackResult) {
            throw new FeedbackEqualToResultException();
        }

        updateCardLimits(transaction, transactionResult, feedbackResult);
        transaction.setFeedback(feedbackResult);
        var saved = transactionRepository.save(transaction);

        return transactionMapper.toFeedbackOut(saved);
    }

    private void updateCardLimits(Transaction transaction,
                                  ValidationResult transactionResult, ValidationResult feedbackResult) {
        var cardLimits = cardLimitsRepository.findById(transaction.getNumber()).orElse(new CardLimits());

        var transactionAmount = transaction.getAmount();
        if (feedbackResult == ValidationResult.PROHIBITED) {
            if (transactionResult == ValidationResult.ALLOWED) {
                cardLimits.setAllowedMax(decreaseLimit(getAllowedMax(cardLimits), transactionAmount));
                cardLimits.setManualProcessingMax(decreaseLimit(getManualProcessingMax(cardLimits), transactionAmount));
            } else if (transactionResult == ValidationResult.MANUAL_PROCESSING) {
                cardLimits.setManualProcessingMax(decreaseLimit(getManualProcessingMax(cardLimits), transactionAmount));
            }
        } else if (feedbackResult == ValidationResult.MANUAL_PROCESSING) {
            if (transactionResult == ValidationResult.ALLOWED) {
                cardLimits.setAllowedMax(decreaseLimit(getAllowedMax(cardLimits), transactionAmount));
            } else if (transactionResult == ValidationResult.PROHIBITED) {
                cardLimits.setManualProcessingMax(increaseLimit(getManualProcessingMax(cardLimits), transactionAmount));
            }
        } else if (feedbackResult == ValidationResult.ALLOWED) {
            if (transactionResult == ValidationResult.MANUAL_PROCESSING) {
                cardLimits.setAllowedMax(increaseLimit(getAllowedMax(cardLimits), transactionAmount));
            } else if (transactionResult == ValidationResult.PROHIBITED) {
                cardLimits.setAllowedMax(increaseLimit(getAllowedMax(cardLimits), transactionAmount));
                cardLimits.setManualProcessingMax(increaseLimit(getManualProcessingMax(cardLimits), transactionAmount));
            }
        }

        if (cardLimits.getCardNumber() == null) {
            cardLimits.setCardNumber(transaction.getNumber());
        }
        cardLimitsRepository.save(cardLimits);
    }

    private long decreaseLimit(long currentLimit, long transactionAmount) {
        return (long) Math.ceil(0.8 * currentLimit - 0.2 * transactionAmount);
    }

    private long increaseLimit(long currentLimit, long transactionAmount) {
        return (long) Math.ceil(0.8 * currentLimit + 0.2 * transactionAmount);
    }

    @Override
    public List<TransactionFeedbackOutDTO> getTransactionsHistory() {
        List<TransactionFeedbackOutDTO> feedbacks = new ArrayList<>();
        transactionRepository.findAllByOrderByIdAsc()
                .forEach(transaction -> feedbacks.add(transactionMapper.toFeedbackOut(transaction)));
        return feedbacks;
    }

    @Override
    public List<TransactionFeedbackOutDTO> getTransactionHistory(String number) {
        List<TransactionFeedbackOutDTO> feedbacks = new ArrayList<>();
        transactionRepository.findAllByNumber(number)
                .forEach(transaction -> feedbacks.add(transactionMapper.toFeedbackOut(transaction)));
        if (feedbacks.isEmpty()) {
            throw new TransactionNotFoundException();
        }
        return feedbacks;
    }
}
