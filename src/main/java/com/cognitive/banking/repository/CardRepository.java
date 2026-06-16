// src/main/java/com/cognitive/banking/repository/CardRepository.java
package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.Card;
import com.cognitive.banking.domain.enums.CardStatus;
import com.cognitive.banking.domain.enums.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    // ==================== BASIC QUERIES ====================
    Optional<Card> findByCardNumber(String cardNumber);
    List<Card> findByUserUserId(UUID userId);
    List<Card> findByAccountAccountId(UUID accountId);
    List<Card> findByCardType(CardType cardType);
    List<Card> findByCardStatus(CardStatus cardStatus);
    List<Card> findByUserUserIdAndCardStatus(UUID userId, CardStatus cardStatus);
    boolean existsByCardNumber(String cardNumber);

    // ==================== COUNT QUERIES FOR METRICS ====================
    @Query("SELECT COUNT(c) FROM Card c")
    long countTotalCards();

    @Query("SELECT COUNT(c) FROM Card c WHERE c.cardStatus = :status")
    long countByCardStatus(@Param("status") CardStatus status);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.cardType = :type")
    long countByCardType(@Param("type") CardType type);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.user.userId = :userId AND c.cardStatus = 'ACTIVE'")
    long countActiveCardsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.cardType = :cardType AND c.cardStatus = :status")
    long countByCardTypeAndStatus(@Param("cardType") CardType cardType, @Param("status") CardStatus status);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.cardStatus = :status AND c.cardType = :type")
    long countByStatusAndType(@Param("status") CardStatus status, @Param("type") CardType type);

    // ==================== EXPIRY QUERIES ====================
    @Query("SELECT c FROM Card c WHERE c.expiryDate < :currentDate AND c.cardStatus = 'ACTIVE'")
    List<Card> findExpiredActiveCards(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT c FROM Card c WHERE c.expiryDate < :currentDate")
    List<Card> findExpiredCards(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT c FROM Card c WHERE c.expiryDate BETWEEN :startDate AND :endDate")
    List<Card> findCardsExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // ==================== USER AND ACCOUNT RELATIONSHIPS ====================
    @Query("SELECT c FROM Card c WHERE c.user.userId = :userId AND c.account.accountId = :accountId")
    List<Card> findByUserIdAndAccountId(@Param("userId") UUID userId, @Param("accountId") UUID accountId);

    // ==================== RECENT QUERIES ====================
    @Query("SELECT c FROM Card c WHERE c.createdAt >= :sinceDate ORDER BY c.createdAt DESC")
    List<Card> findRecentCards(@Param("sinceDate") LocalDateTime sinceDate);

    // ==================== DISTRIBUTION QUERIES ====================
    @Query("SELECT c.cardType, COUNT(c) FROM Card c GROUP BY c.cardType")
    List<Object[]> getCardTypeDistribution();

    @Query("SELECT c.cardStatus, COUNT(c) FROM Card c GROUP BY c.cardStatus")
    List<Object[]> getCardStatusDistribution();

    // ==================== BULK OPERATIONS ====================
    @Modifying
    @Transactional
    @Query("UPDATE Card c SET c.cardStatus = 'EXPIRED' WHERE c.expiryDate < :currentDate AND c.cardStatus = 'ACTIVE'")
    int updateExpiredCards(@Param("currentDate") LocalDate currentDate);

    // ==================== SECURITY QUERIES ====================
    @Query("SELECT c.pin FROM Card c WHERE c.cardId = :cardId")
    String getPinByCardId(@Param("cardId") UUID cardId);

    // ==================== COMPREHENSIVE STATISTICS ====================
    @Query("SELECT " +
            "COUNT(c) as totalCards, " +
            "SUM(CASE WHEN c.cardStatus = 'ACTIVE' THEN 1 ELSE 0 END) as activeCards, " +
            "SUM(CASE WHEN c.cardStatus = 'INACTIVE' THEN 1 ELSE 0 END) as inactiveCards, " +
            "SUM(CASE WHEN c.cardStatus = 'BLOCKED' THEN 1 ELSE 0 END) as blockedCards, " +
            "SUM(CASE WHEN c.cardStatus = 'CANCELLED' THEN 1 ELSE 0 END) as cancelledCards, " +
            "SUM(CASE WHEN c.cardStatus = 'EXPIRED' THEN 1 ELSE 0 END) as expiredCards, " +
            "SUM(CASE WHEN c.cardType = 'CREDIT' THEN 1 ELSE 0 END) as creditCards, " +
            "SUM(CASE WHEN c.cardType = 'DEBIT' THEN 1 ELSE 0 END) as debitCards " +
            "FROM Card c")
    Object[] getCardStatistics();

    @Query("SELECT " +
            "COUNT(c) as userCards, " +
            "SUM(CASE WHEN c.cardStatus = 'ACTIVE' THEN 1 ELSE 0 END) as userActiveCards " +
            "FROM Card c WHERE c.user.userId = :userId")
    Object[] getCardStatisticsByUser(@Param("userId") UUID userId);
}