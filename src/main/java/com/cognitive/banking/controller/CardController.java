// src/main/java/com/cognitive/banking/controller/CardController.java
package com.cognitive.banking.controller;

import com.cognitive.banking.domain.enums.CardStatus;
import com.cognitive.banking.dto.CardDTO;
import com.cognitive.banking.dto.CreateCardRequest;
import com.cognitive.banking.dto.UpdateCardRequest;
import com.cognitive.banking.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cards")
public class CardController {

    @Autowired
    private CardService cardService;

    @PostMapping
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CreateCardRequest request) {
        CardDTO cardDTO = cardService.createCard(request);
        return new ResponseEntity<>(cardDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardDTO> getCardById(@PathVariable UUID cardId) {
        return cardService.getCardById(cardId)
                .map(card -> new ResponseEntity<>(card, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/number/{cardNumber}")
    public ResponseEntity<CardDTO> getCardByNumber(@PathVariable String cardNumber) {
        return cardService.getCardByNumber(cardNumber)
                .map(card -> new ResponseEntity<>(card, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardDTO>> getCardsByUserId(@PathVariable UUID userId) {
        List<CardDTO> cards = cardService.getCardsByUserId(userId);
        return new ResponseEntity<>(cards, HttpStatus.OK);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<CardDTO>> getCardsByAccountId(@PathVariable UUID accountId) {
        List<CardDTO> cards = cardService.getCardsByAccountId(accountId);
        return new ResponseEntity<>(cards, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<CardDTO>> getAllCards() {
        List<CardDTO> cards = cardService.getAllCards();
        return new ResponseEntity<>(cards, HttpStatus.OK);
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CardDTO> updateCard(@PathVariable UUID cardId,
                                              @Valid @RequestBody UpdateCardRequest request) {
        try {
            CardDTO updatedCard = cardService.updateCard(cardId, request);
            return new ResponseEntity<>(updatedCard, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{cardId}/status")
    public ResponseEntity<CardDTO> updateCardStatus(@PathVariable UUID cardId,
                                                    @RequestParam CardStatus status) {
        try {
            CardDTO updatedCard = cardService.updateCardStatus(cardId, status);
            return new ResponseEntity<>(updatedCard, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{cardId}/activate")
    public ResponseEntity<CardDTO> activateCard(@PathVariable UUID cardId,
                                                @RequestParam String pin) {
        try {
            CardDTO activatedCard = cardService.activateCard(cardId, pin);
            return new ResponseEntity<>(activatedCard, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId) {
        try {
            cardService.deleteCard(cardId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BigDecimal> getCardBalance(@PathVariable UUID cardId) {
        try {
            BigDecimal balance = cardService.getCardBalance(cardId);
            return new ResponseEntity<>(balance, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}/active-count")
    public ResponseEntity<Long> getActiveCardsCount(@PathVariable UUID userId) {
        long count = cardService.getActiveCardsCountByUserId(userId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("Card Service is running", HttpStatus.OK);
    }
}