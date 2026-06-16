package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.Account;
import com.cognitive.banking.domain.entity.Card;
import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.CardStatus;
import com.cognitive.banking.domain.enums.CardType;
import com.cognitive.banking.dto.CardDTO;
import com.cognitive.banking.dto.CreateCardRequest;
import com.cognitive.banking.dto.UpdateCardRequest;
import com.cognitive.banking.monitoring.metrics.BankingMetrics;
import com.cognitive.banking.repository.AccountRepository;
import com.cognitive.banking.repository.CardRepository;
import com.cognitive.banking.repository.UserRepository;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CardService {

    private static final Logger logger = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final BankingMetrics bankingMetrics;

    private static final String CARD_CACHE = "cards";

    public CardService(CardRepository cardRepository,
                       UserRepository userRepository,
                       AccountRepository accountRepository,
                       BankingMetrics bankingMetrics) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.bankingMetrics = bankingMetrics;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // ============================
    // CREATE CARD - With Metrics
    // ============================
    public CardDTO createCard(CreateCardRequest request) {
        logger.info("Creating new card for user ID: {}", request.getUserId());
        Timer.Sample sample = bankingMetrics.startCardCreationTimer();

        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found with ID: " + request.getAccountId()));

            if (!account.getUser().getUserId().equals(request.getUserId())) {
                bankingMetrics.recordCardCreationFailure(request.getCardType().name(), "account_mismatch");
                throw new RuntimeException("Account does not belong to the specified user");
            }

            String cardNumber = generateCardNumber();
            String cvv = generateCVV();
            String encryptedPin = passwordEncoder.encode(request.getPin());
            LocalDate expiryDate = LocalDate.now().plusYears(3);

            Card card = new Card();
            card.setCardNumber(cardNumber);
            card.setCardHolderName(request.getCardHolderName());
            card.setCardType(request.getCardType());
            card.setCardNetwork(request.getCardNetwork());  // FIXED: Added this line
            card.setExpiryDate(expiryDate);
            card.setCvv(encryptCVV(cvv));
            card.setPin(encryptedPin);
            card.setUser(user);
            card.setAccount(account);
            card.setCardStatus(request.getCardStatus() != null ? request.getCardStatus() : CardStatus.PENDING_ACTIVATION);
            card.setDailyLimit(request.getDailyLimit() != null ? request.getDailyLimit() : BigDecimal.valueOf(1000.00));
            card.setWeeklyLimit(request.getWeeklyLimit());
            card.setMonthlyLimit(request.getMonthlyLimit());
            card.setIssuedDate(LocalDate.now());
            card.setCreatedAt(LocalDateTime.now());
            card.setUpdatedAt(LocalDateTime.now());

            if (request.getCardType() == CardType.CREDIT) {
                BigDecimal creditLimit = request.getCreditLimit() != null ? request.getCreditLimit() : BigDecimal.valueOf(5000.00);
                card.setCreditLimit(creditLimit);
                card.setAvailableBalance(creditLimit);
                card.setCurrentBalance(BigDecimal.ZERO);
            } else {
                card.setAvailableBalance(account.getBalance());
                card.setCurrentBalance(account.getBalance());
            }

            Card savedCard = cardRepository.save(card);

            // Record metrics
            bankingMetrics.recordCardCreated(
                    savedCard.getCardType().name(),
                    savedCard.getCardStatus().name(),
                    savedCard.getDailyLimit()
            );
            bankingMetrics.updateCardMetrics(
                    cardRepository.count(),
                    cardRepository.countByCardStatus(CardStatus.ACTIVE),
                    cardRepository.countByCardType(CardType.CREDIT),
                    cardRepository.countByCardType(CardType.DEBIT)
            );

            bankingMetrics.stopCardCreationTimer(sample);

            logger.info("Card created successfully with number: {}", maskCardNumber(cardNumber));

            return convertToDTO(savedCard);

        } catch (Exception e) {
            bankingMetrics.stopCardCreationTimer(sample);
            bankingMetrics.recordCardCreationFailure(request.getCardType().name(), "general_error");
            logger.error("Card creation failed: {}", e.getMessage());
            throw e;
        }
    }

    // ============================
    // GET CARD BY ID - With Metrics
    // ============================
    @Cacheable(value = CARD_CACHE, key = "#cardId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<CardDTO> getCardById(UUID cardId) {
        logger.debug("Fetching card by ID: {}", cardId);
        bankingMetrics.recordCardQuery("by_id");

        Timer.Sample sample = bankingMetrics.startCardQueryTimer();
        try {
            Optional<CardDTO> result = cardRepository.findById(cardId).map(this::convertToDTO);
            bankingMetrics.stopCardQueryTimer(sample);
            return result;
        } catch (Exception e) {
            bankingMetrics.stopCardQueryTimer(sample);
            throw e;
        }
    }

    // ============================
    // GET CARD BY NUMBER - With Metrics
    // ============================
    @Cacheable(value = CARD_CACHE, key = "'number:' + #cardNumber", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<CardDTO> getCardByNumber(String cardNumber) {
        logger.debug("Fetching card by number: {}", maskCardNumber(cardNumber));
        bankingMetrics.recordCardQuery("by_number");

        return cardRepository.findByCardNumber(cardNumber).map(this::convertToDTO);
    }

    // ============================
    // GET CARDS BY USER ID - With Metrics
    // ============================
    @Transactional(readOnly = true)
    public List<CardDTO> getCardsByUserId(UUID userId) {
        logger.debug("Fetching cards for user ID: {}", userId);
        bankingMetrics.recordCardQuery("by_user_id");

        return cardRepository.findByUserUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============================
    // GET CARDS BY ACCOUNT ID - With Metrics
    // ============================
    @Transactional(readOnly = true)
    public List<CardDTO> getCardsByAccountId(UUID accountId) {
        logger.debug("Fetching cards for account ID: {}", accountId);
        bankingMetrics.recordCardQuery("by_account_id");

        return cardRepository.findByAccountAccountId(accountId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============================
    // GET ALL CARDS (ADMIN) - With Metrics
    // ============================
    @Transactional(readOnly = true)
    public List<CardDTO> getAllCards() {
        logger.info("Fetching all cards");
        bankingMetrics.recordCardQuery("all");

        return cardRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============================
    // UPDATE CARD - With Metrics
    // ============================
    @CacheEvict(value = CARD_CACHE, key = "#cardId")
    public CardDTO updateCard(UUID cardId, UpdateCardRequest request) {
        logger.info("Updating card with ID: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        if (request.getDailyLimit() != null) {
            card.setDailyLimit(request.getDailyLimit());
        }
        if (request.getCreditLimit() != null && card.getCardType() == CardType.CREDIT) {
            card.setCreditLimit(request.getCreditLimit());
            BigDecimal newAvailable = card.getCreditLimit().subtract(card.getCurrentBalance());
            card.setAvailableBalance(newAvailable);
        }
        if (request.getCardStatus() != null) {
            card.setCardStatus(request.getCardStatus());
        }
        card.setUpdatedAt(LocalDateTime.now());

        Card updatedCard = cardRepository.save(card);

        bankingMetrics.recordCardUpdate(card.getCardType().name());

        logger.info("Card updated successfully: {}", cardId);

        return convertToDTO(updatedCard);
    }

    // ============================
    // UPDATE CARD STATUS - With Metrics
    // ============================
    @CacheEvict(value = CARD_CACHE, key = "#cardId")
    public CardDTO updateCardStatus(UUID cardId, CardStatus status) {
        logger.info("Updating card status to {} for card ID: {}", status, cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        CardStatus oldStatus = card.getCardStatus();
        card.setCardStatus(status);
        card.setUpdatedAt(LocalDateTime.now());

        Card updatedCard = cardRepository.save(card);

        bankingMetrics.recordCardStatusChange(oldStatus.name(), status.name());
        bankingMetrics.updateCardMetrics(
                cardRepository.count(),
                cardRepository.countByCardStatus(CardStatus.ACTIVE),
                cardRepository.countByCardType(CardType.CREDIT),
                cardRepository.countByCardType(CardType.DEBIT)
        );

        logger.info("Card status updated successfully for card: {}", cardId);

        return convertToDTO(updatedCard);
    }

    // ============================
    // ACTIVATE CARD - With Metrics
    // ============================
    @CacheEvict(value = CARD_CACHE, key = "#cardId")
    public CardDTO activateCard(UUID cardId, String pin) {
        logger.info("Activating card with ID: {}", cardId);
        Timer.Sample sample = bankingMetrics.startCardActivationTimer();

        try {
            Card card = cardRepository.findById(cardId)
                    .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

            if (!passwordEncoder.matches(pin, card.getPin())) {
                bankingMetrics.recordCardActivationFailure("invalid_pin");
                logger.warn("Invalid PIN attempt for card activation: {}", maskCardNumber(card.getCardNumber()));
                throw new RuntimeException("Invalid PIN");
            }

            if (card.getExpiryDate().isBefore(LocalDate.now())) {
                bankingMetrics.recordCardActivationFailure("card_expired");
                throw new RuntimeException("Cannot activate expired card");
            }

            card.setCardStatus(CardStatus.ACTIVE);
            card.setActivatedAt(LocalDateTime.now());
            card.setUpdatedAt(LocalDateTime.now());

            Card activatedCard = cardRepository.save(card);

            bankingMetrics.recordCardActivation(true);
            bankingMetrics.updateCardMetrics(
                    cardRepository.count(),
                    cardRepository.countByCardStatus(CardStatus.ACTIVE),
                    cardRepository.countByCardType(CardType.CREDIT),
                    cardRepository.countByCardType(CardType.DEBIT)
            );
            bankingMetrics.stopCardActivationTimer(sample);

            logger.info("Card activated successfully: {}", maskCardNumber(card.getCardNumber()));

            return convertToDTO(activatedCard);

        } catch (Exception e) {
            bankingMetrics.stopCardActivationTimer(sample);
            throw e;
        }
    }

    // ============================
    // BLOCK CARD - With Metrics
    // ============================
    @CacheEvict(value = CARD_CACHE, key = "#cardId")
    public CardDTO blockCard(UUID cardId) {
        logger.info("Blocking card with ID: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        card.setCardStatus(CardStatus.BLOCKED);
        card.setUpdatedAt(LocalDateTime.now());

        Card blockedCard = cardRepository.save(card);

        bankingMetrics.recordCardBlocked(card.getCardType().name());
        bankingMetrics.updateCardMetrics(
                cardRepository.count(),
                cardRepository.countByCardStatus(CardStatus.ACTIVE),
                cardRepository.countByCardType(CardType.CREDIT),
                cardRepository.countByCardType(CardType.DEBIT)
        );

        logger.info("Card blocked: {}", maskCardNumber(card.getCardNumber()));

        return convertToDTO(blockedCard);
    }

    // ============================
    // UNBLOCK CARD - With Metrics
    // ============================
    @CacheEvict(value = CARD_CACHE, key = "#cardId")
    public CardDTO unblockCard(UUID cardId) {
        logger.info("Unblocking card with ID: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot unblock expired card");
        }

        card.setCardStatus(CardStatus.ACTIVE);
        card.setUpdatedAt(LocalDateTime.now());

        Card unblockedCard = cardRepository.save(card);

        bankingMetrics.recordCardUnblocked(card.getCardType().name());

        logger.info("Card unblocked: {}", maskCardNumber(card.getCardNumber()));

        return convertToDTO(unblockedCard);
    }

    // ============================
    // CANCEL/DELETE CARD - With Metrics
    // ============================
    @CacheEvict(value = CARD_CACHE, key = "#cardId")
    public void deleteCard(UUID cardId) {
        logger.info("Cancelling card with ID: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        card.setCardStatus(CardStatus.CANCELLED);
        card.setUpdatedAt(LocalDateTime.now());
        cardRepository.save(card);

        bankingMetrics.recordCardCancelled(card.getCardType().name());
        bankingMetrics.updateCardMetrics(
                cardRepository.count(),
                cardRepository.countByCardStatus(CardStatus.ACTIVE),
                cardRepository.countByCardType(CardType.CREDIT),
                cardRepository.countByCardType(CardType.DEBIT)
        );

        logger.info("Card cancelled: {}", maskCardNumber(card.getCardNumber()));
    }

    // ============================
    // CHANGE PIN - With Metrics
    // ============================
    @CacheEvict(value = CARD_CACHE, key = "#cardId")
    public CardDTO changePin(UUID cardId, String oldPin, String newPin) {
        logger.info("Changing PIN for card ID: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        if (!passwordEncoder.matches(oldPin, card.getPin())) {
            bankingMetrics.recordPinChangeFailure();
            logger.warn("Invalid old PIN for card: {}", maskCardNumber(card.getCardNumber()));
            throw new RuntimeException("Invalid current PIN");
        }

        card.setPin(passwordEncoder.encode(newPin));
        card.setUpdatedAt(LocalDateTime.now());

        Card updatedCard = cardRepository.save(card);

        bankingMetrics.recordPinChange(true);

        logger.info("PIN changed successfully for card: {}", maskCardNumber(card.getCardNumber()));

        return convertToDTO(updatedCard);
    }

    // ============================
    // REPLACE CARD (LOST/STOLEN) - With Metrics
    // ============================
    @Transactional
    public CardDTO replaceCard(UUID oldCardId, CreateCardRequest request) {
        logger.info("Replacing card with ID: {} with new card", oldCardId);

        Card oldCard = cardRepository.findById(oldCardId)
                .orElseThrow(() -> new RuntimeException("Old card not found"));

        oldCard.setCardStatus(CardStatus.LOST_STOLEN);
        oldCard.setUpdatedAt(LocalDateTime.now());
        cardRepository.save(oldCard);

        request.setAccountId(oldCard.getAccount().getAccountId());
        request.setUserId(oldCard.getUser().getUserId());

        if (request.getCardHolderName() == null || request.getCardHolderName().isEmpty()) {
            request.setCardHolderName(oldCard.getCardHolderName());
        }

        if (request.getCardType() == null) {
            request.setCardType(oldCard.getCardType());
        }

        CardDTO newCard = createCard(request);

        bankingMetrics.recordCardReplaced(oldCard.getCardType().name());

        logger.info("Card replaced successfully. Old card: {}, New card: {}",
                maskCardNumber(oldCard.getCardNumber()), maskCardNumber(newCard.getCardNumber()));

        return newCard;
    }

    // ============================
    // GET CARD BALANCE - With Metrics
    // ============================
    @Transactional(readOnly = true)
    public BigDecimal getCardBalance(UUID cardId) {
        bankingMetrics.recordCardBalanceCheck();

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        bankingMetrics.recordCardBalanceAmount(card.getAvailableBalance());
        return card.getAvailableBalance();
    }

    // ============================
    // GET ACTIVE CARDS COUNT
    // ============================
    @Transactional(readOnly = true)
    public long getActiveCardsCountByUserId(UUID userId) {
        return cardRepository.countActiveCardsByUserId(userId);
    }

    // ============================
    // CHECK IF CARD IS VALID
    // ============================
    @Transactional(readOnly = true)
    public boolean isCardValid(String cardNumber) {
        Optional<Card> cardOpt = cardRepository.findByCardNumber(cardNumber);
        if (cardOpt.isEmpty()) return false;

        Card card = cardOpt.get();
        return card.getCardStatus() == CardStatus.ACTIVE
                && card.getExpiryDate().isAfter(LocalDate.now());
    }

    // ============================
    // VALIDATE PIN
    // ============================
    @Transactional(readOnly = true)
    public boolean validatePin(String cardNumber, String pin) {
        Optional<Card> cardOpt = cardRepository.findByCardNumber(cardNumber);
        if (cardOpt.isEmpty()) return false;

        Card card = cardOpt.get();
        return passwordEncoder.matches(pin, card.getPin());
    }

    // ============================
    // PRIVATE HELPER METHODS
    // ============================

    private String generateCardNumber() {
        String cardNumber;
        do {
            String prefix = "4";
            long number = (long) (Math.random() * 1_000_000_000_000_000L);
            cardNumber = prefix + String.format("%015d", number);
            cardNumber = addLuhnCheckDigit(cardNumber);
        } while (cardRepository.existsByCardNumber(cardNumber));
        return cardNumber;
    }

    private String addLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        int checkDigit = (sum * 9) % 10;
        return number + checkDigit;
    }

    private String generateCVV() {
        return String.format("%03d", (int) (Math.random() * 1000));
    }

    private String encryptCVV(String cvv) {
        return passwordEncoder.encode(cvv);
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 12) {
            return cardNumber;
        }
        return cardNumber.substring(0, 4) + "********" + cardNumber.substring(cardNumber.length() - 4);
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
            return accountNumber;
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    // ============================
    // DTO CONVERSION
    // ============================

    private CardDTO convertToDTO(Card card) {
        CardDTO dto = new CardDTO();
        dto.setCardId(card.getCardId());
        dto.setCardNumber(maskCardNumber(card.getCardNumber()));
        dto.setFullCardNumber(card.getCardNumber());
        dto.setCardHolderName(card.getCardHolderName());
        dto.setCardType(card.getCardType());
        dto.setCardStatus(card.getCardStatus());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setDailyLimit(card.getDailyLimit());
        dto.setAvailableBalance(card.getAvailableBalance());
        dto.setCreditLimit(card.getCreditLimit());
        dto.setCurrentBalance(card.getCurrentBalance());
        dto.setUserId(card.getUser().getUserId());
        dto.setUserName(card.getUser().getFirstName() + " " + card.getUser().getLastName());
        dto.setAccountId(card.getAccount() != null ? card.getAccount().getAccountId() : null);
        dto.setAccountNumber(card.getAccount() != null ? maskAccountNumber(card.getAccount().getAccountNumber()) : null);
        dto.setFullAccountNumber(card.getAccount() != null ? card.getAccount().getAccountNumber() : null);
        dto.setIssuedDate(card.getIssuedDate());
        dto.setActivatedAt(card.getActivatedAt());
        dto.setCreatedAt(card.getCreatedAt());
        dto.setUpdatedAt(card.getUpdatedAt());
        return dto;
    }
}