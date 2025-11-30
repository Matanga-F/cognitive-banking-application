
// src/main/java/com/cognitive/banking/service/CardService.java
package com.cognitive.banking.service;

import com.cognitive.banking.domain.entity.Account;
import com.cognitive.banking.domain.entity.Card;
import com.cognitive.banking.domain.entity.User;
import com.cognitive.banking.domain.enums.CardStatus;
import com.cognitive.banking.domain.enums.CardType;
import com.cognitive.banking.dto.CardDTO;
import com.cognitive.banking.dto.CreateCardRequest;
import com.cognitive.banking.dto.UpdateCardRequest;
import com.cognitive.banking.repository.AccountRepository;
import com.cognitive.banking.repository.CardRepository;
import com.cognitive.banking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    public CardDTO createCard(CreateCardRequest request) {
        System.out.println("Creating new card for user ID: " + request.getUserId());

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        // Validate account exists and belongs to user
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + request.getAccountId()));

        if (!account.getUser().getUserId().equals(request.getUserId())) {
            throw new RuntimeException("Account does not belong to the specified user");
        }

        // Generate unique card number
        String cardNumber = generateCardNumber();
        String cvv = generateCVV();
        String pin = generatePIN();
        LocalDate expiryDate = LocalDate.now().plusYears(3); // 3 years from now

        // Create card entity
        Card card = new Card();
        card.setCardNumber(cardNumber);
        card.setCardHolderName(request.getCardHolderName());
        card.setCardType(request.getCardType());
        card.setExpiryDate(expiryDate);
        card.setCvv(cvv);
        card.setPin(pin);
        card.setUser(user);
        card.setAccount(account);

        // Set limits based on card type
        if (request.getDailyLimit() != null) {
            card.setDailyLimit(request.getDailyLimit());
        } else {
            card.setDailyLimit(BigDecimal.valueOf(1000.00)); // Default daily limit
        }

        if (request.getCardType() == CardType.CREDIT) {
            if (request.getCreditLimit() != null) {
                card.setCreditLimit(request.getCreditLimit());
                card.setAvailableBalance(request.getCreditLimit());
            } else {
                BigDecimal defaultCreditLimit = BigDecimal.valueOf(5000.00);
                card.setCreditLimit(defaultCreditLimit);
                card.setAvailableBalance(defaultCreditLimit);
            }
        } else if (request.getCardType() == CardType.DEBIT) {
            card.setAvailableBalance(account.getBalance());
        }

        Card savedCard = cardRepository.save(card);
        System.out.println("Card created successfully with number: " + savedCard.getCardNumber());

        return convertToDTO(savedCard);
    }

    @Transactional(readOnly = true)
    public Optional<CardDTO> getCardById(UUID cardId) {
        System.out.println("Fetching card by ID: " + cardId);
        return cardRepository.findById(cardId)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<CardDTO> getCardByNumber(String cardNumber) {
        System.out.println("Fetching card by number: " + cardNumber);
        return cardRepository.findByCardNumber(cardNumber)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<CardDTO> getCardsByUserId(UUID userId) {
        System.out.println("Fetching cards for user ID: " + userId);
        return cardRepository.findByUserUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CardDTO> getCardsByAccountId(UUID accountId) {
        System.out.println("Fetching cards for account ID: " + accountId);
        return cardRepository.findByAccountAccountId(accountId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CardDTO> getAllCards() {
        System.out.println("Fetching all cards");
        return cardRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CardDTO updateCard(UUID cardId, UpdateCardRequest request) {
        System.out.println("Updating card with ID: " + cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        // Update fields if provided
        if (request.getDailyLimit() != null) {
            card.setDailyLimit(request.getDailyLimit());
        }
        if (request.getCreditLimit() != null && card.getCardType() == CardType.CREDIT) {
            card.setCreditLimit(request.getCreditLimit());
        }
        if (request.getCardStatus() != null) {
            card.setCardStatus(request.getCardStatus());
        }

        Card updatedCard = cardRepository.save(card);
        System.out.println("Card updated successfully");

        return convertToDTO(updatedCard);
    }

    public CardDTO updateCardStatus(UUID cardId, CardStatus status) {
        System.out.println("Updating card status to " + status + " for card ID: " + cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        card.setCardStatus(status);
        Card updatedCard = cardRepository.save(card);

        System.out.println("Card status updated successfully");
        return convertToDTO(updatedCard);
    }

    public void deleteCard(UUID cardId) {
        System.out.println("Deleting card with ID: " + cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        // Instead of hard delete, set status to INACTIVE
        card.setCardStatus(CardStatus.INACTIVE);
        cardRepository.save(card);

        System.out.println("Card deactivated successfully");
    }

    public CardDTO activateCard(UUID cardId, String pin) {
        System.out.println("Activating card with ID: " + cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));

        if (!card.getPin().equals(pin)) {
            throw new RuntimeException("Invalid PIN");
        }

        card.setCardStatus(CardStatus.ACTIVE);
        Card updatedCard = cardRepository.save(card);

        System.out.println("Card activated successfully");
        return convertToDTO(updatedCard);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCardBalance(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));
        return card.getAvailableBalance();
    }

    @Transactional(readOnly = true)
    public long getActiveCardsCountByUserId(UUID userId) {
        return cardRepository.countActiveCardsByUserId(userId);
    }

    private String generateCardNumber() {
        String cardNumber;
        do {
            // Generate 16-digit card number starting with appropriate prefix
            String prefix = "4"; // Visa prefix
            long number = (long) (Math.random() * 1_000_000_000_000_000L);
            cardNumber = prefix + String.format("%015d", number);
        } while (cardRepository.existsByCardNumber(cardNumber));

        return cardNumber;
    }

    private String generateCVV() {
        // Generate 3-digit CVV
        return String.format("%03d", (int) (Math.random() * 1000));
    }

    private String generatePIN() {
        // Generate 4-digit PIN
        return String.format("%04d", (int) (Math.random() * 10000));
    }

    private CardDTO convertToDTO(Card card) {
        String userName = card.getUser().getFirstName() + " " + card.getUser().getLastName();
        String accountNumber = card.getAccount() != null ? card.getAccount().getAccountNumber() : null;
        UUID accountId = card.getAccount() != null ? card.getAccount().getAccountId() : null;

        return new CardDTO(
                card.getCardId(),
                maskCardNumber(card.getCardNumber()),
                card.getCardHolderName(),
                card.getCardType(),
                card.getCardStatus(),
                card.getExpiryDate(),
                card.getDailyLimit(),
                card.getAvailableBalance(),
                card.getCreditLimit(),
                card.getCurrentBalance(),
                card.getUser().getUserId(),
                userName,
                accountId,
                accountNumber,
                card.getIssuedDate(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 12) {
            return cardNumber;
        }
        // Show first 4 and last 4 digits, mask the rest
        String firstFour = cardNumber.substring(0, 4);
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return firstFour + "********" + lastFour;
    }
}