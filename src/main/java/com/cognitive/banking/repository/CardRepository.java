// src/main/java/com/cognitive/banking/repository/CardRepository.java
package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.Card;
import com.cognitive.banking.domain.enums.CardStatus;
import com.cognitive.banking.domain.enums.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardNumber(String cardNumber);
    List<Card> findByAccountId(Long accountId);
    List<Card> findByStatus(CardStatus status);
    List<Card> findByCardType(CardType cardType);
    boolean existsByCardNumber(String cardNumber);
}