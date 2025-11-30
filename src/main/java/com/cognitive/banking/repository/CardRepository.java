// src/main/java/com/cognitive/banking/repository/CardRepository.java
package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.Card;
import com.cognitive.banking.domain.enums.CardStatus;
import com.cognitive.banking.domain.enums.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByUserUserId(UUID userId);

    List<Card> findByAccountAccountId(UUID accountId);

    List<Card> findByCardType(CardType cardType);

    List<Card> findByCardStatus(CardStatus cardStatus);

    List<Card> findByUserUserIdAndCardStatus(UUID userId, CardStatus cardStatus);

    boolean existsByCardNumber(String cardNumber);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.user.userId = :userId AND c.cardStatus = 'ACTIVE'")
    long countActiveCardsByUserId(@Param("userId") UUID userId);

    @Query("SELECT c FROM Card c WHERE c.expiryDate < :currentDate AND c.cardStatus = 'ACTIVE'")
    List<Card> findExpiredActiveCards(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT c FROM Card c WHERE c.user.userId = :userId AND c.account.accountId = :accountId")
    List<Card> findByUserIdAndAccountId(@Param("userId") UUID userId, @Param("accountId") UUID accountId);
}