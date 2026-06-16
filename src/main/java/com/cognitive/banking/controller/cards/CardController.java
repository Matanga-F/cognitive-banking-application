package com.cognitive.banking.controller.cards;

import com.cognitive.banking.annotation.RequiresPermission;
import com.cognitive.banking.annotation.RequiresRole;
import com.cognitive.banking.domain.enums.CardStatus;
import com.cognitive.banking.domain.enums.UserRole;
import com.cognitive.banking.dto.CardDTO;
import com.cognitive.banking.dto.CreateCardRequest;
import com.cognitive.banking.dto.UpdateCardRequest;
import com.cognitive.banking.monitoring.metrics.BankingMetrics;
import com.cognitive.banking.service.CardService;
import com.cognitive.banking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cards")
@Tag(name = "Card Management", description = "Endpoints for managing bank cards")
public class CardController {

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);

    @Autowired
    private CardService cardService;

    @Autowired
    private BankingMetrics bankingMetrics;

    @Autowired
    private UserService userService;

    // ============================================
    // CREATE CARD
    // ============================================
    @PostMapping
    @RequiresPermission("card:create")
    @Operation(summary = "Create a new card", description = "Creates a new card for an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CreateCardRequest request) {
        logger.info("REST request to create card for user: {}", request.getUserId());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        // Verify user can only create cards for themselves unless admin
        if (!isAdmin() && !currentUserEmail.equals(getUserEmailFromId(request.getUserId()))) {
            logger.warn("User {} attempted to create card for another user: {}", currentUserEmail, request.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CardDTO result = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ============================================
    // GET CARD BY ID
    // ============================================
    @GetMapping("/{cardId}")
    @RequiresPermission("card:read")
    @Operation(summary = "Get card by ID", description = "Retrieves card details by card ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card found"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CardDTO> getCardById(
            @Parameter(description = "Card ID", required = true)
            @PathVariable UUID cardId) {
        logger.debug("REST request to get card by ID: {}", cardId);
        bankingMetrics.recordCardQuery("by_id");

        return cardService.getCardById(cardId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================
    // GET CARD BY NUMBER
    // ============================================
    @GetMapping("/number/{cardNumber}")
    @RequiresPermission("card:read")
    @Operation(summary = "Get card by number", description = "Retrieves card details by card number")
    public ResponseEntity<CardDTO> getCardByNumber(
            @Parameter(description = "Card number", required = true)
            @PathVariable String cardNumber) {
        logger.debug("REST request to get card by number");
        bankingMetrics.recordCardQuery("by_number");

        return cardService.getCardByNumber(cardNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================
    // GET CARDS BY USER ID
    // ============================================
    @GetMapping("/user/{userId}")
    @RequiresPermission("card:read")
    @Operation(summary = "Get cards by user ID", description = "Retrieves all cards for a specific user")
    public ResponseEntity<List<CardDTO>> getCardsByUserId(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId) {
        logger.info("REST request to get cards for user: {}", userId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        // Users can only view their own cards unless admin
        if (!isAdmin() && !currentUserEmail.equals(getUserEmailFromId(userId))) {
            logger.warn("User {} attempted to view cards of user: {}", currentUserEmail, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        bankingMetrics.recordCardQuery("by_user_id");
        List<CardDTO> cards = cardService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);
    }

    // ============================================
    // GET MY CARDS - Convenience for current user
    // ============================================
    @GetMapping("/my-cards")
    @RequiresPermission("card:read")
    @Operation(summary = "Get my cards", description = "Retrieves all cards for the currently authenticated user")
    public ResponseEntity<List<CardDTO>> getMyCards() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        logger.info("REST request to get cards for current user: {}", email);

        UUID userId = getUserIdFromEmail(email);
        bankingMetrics.recordCardQuery("my_cards");
        List<CardDTO> cards = cardService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);
    }

    // ============================================
    // GET CARDS BY ACCOUNT ID
    // ============================================
    @GetMapping("/account/{accountId}")
    @RequiresPermission("card:read")
    @Operation(summary = "Get cards by account ID", description = "Retrieves all cards for a specific account")
    public ResponseEntity<List<CardDTO>> getCardsByAccountId(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId) {
        logger.debug("REST request to get cards for account: {}", accountId);
        bankingMetrics.recordCardQuery("by_account_id");

        List<CardDTO> cards = cardService.getCardsByAccountId(accountId);
        return ResponseEntity.ok(cards);
    }

    // ============================================
    // GET ALL CARDS - Admin only
    // ============================================
    @GetMapping("/all")
    @RequiresRole(UserRole.ADMIN)
    @RequiresPermission("card:read")
    @Operation(summary = "Get all cards", description = "Retrieves all cards in the system (Admin only)")
    public ResponseEntity<List<CardDTO>> getAllCards() {
        logger.info("REST request to get all cards (Admin)");
        bankingMetrics.recordCardQuery("all");

        List<CardDTO> cards = cardService.getAllCards();
        return ResponseEntity.ok(cards);
    }

    // ============================================
    // UPDATE CARD
    // ============================================
    @PutMapping("/{cardId}")
    @RequiresPermission("card:update")
    @Operation(summary = "Update card", description = "Updates card details")
    public ResponseEntity<CardDTO> updateCard(
            @Parameter(description = "Card ID", required = true)
            @PathVariable UUID cardId,
            @Valid @RequestBody UpdateCardRequest request) {
        logger.info("REST request to update card: {}", cardId);

        // Verify ownership or admin
        if (!canAccessCard(cardId)) {
            logger.warn("User attempted to update card without permission: {}", cardId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CardDTO result = cardService.updateCard(cardId, request);
        bankingMetrics.recordCardUpdate(result.getCardType().name());

        return ResponseEntity.ok(result);
    }

    // ============================================
    // UPDATE CARD STATUS - Admin only
    // ============================================
    @PatchMapping("/{cardId}/status")
    @RequiresRole(UserRole.ADMIN)
    @RequiresPermission("card:update")
    @Operation(summary = "Update card status", description = "Updates the status of a card (Admin only)")
    public ResponseEntity<CardDTO> updateCardStatus(
            @Parameter(description = "Card ID", required = true)
            @PathVariable UUID cardId,
            @Parameter(description = "New card status", required = true)
            @RequestParam CardStatus status) {
        logger.info("REST request to update status for card: {} to {}", cardId, status);

        CardDTO result = cardService.updateCardStatus(cardId, status);
        bankingMetrics.recordCardStatusChange("CURRENT", status.name());

        return ResponseEntity.ok(result);
    }

    // ============================================
    // ACTIVATE CARD
    // ============================================
    @PostMapping("/{cardId}/activate")
    @RequiresPermission("card:update")
    @Operation(summary = "Activate card", description = "Activates a card with PIN verification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card activated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid PIN"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<CardDTO> activateCard(
            @Parameter(description = "Card ID", required = true)
            @PathVariable UUID cardId,
            @Parameter(description = "Card PIN", required = true)
            @RequestParam String pin) {
        logger.info("REST request to activate card: {}", cardId);

        // Verify ownership or admin
        if (!canAccessCard(cardId)) {
            logger.warn("User attempted to activate card without permission: {}", cardId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CardDTO result = cardService.activateCard(cardId, pin);
        return ResponseEntity.ok(result);
    }

    // ============================================
    // BLOCK CARD
    // ============================================
    @PostMapping("/{cardId}/block")
    @RequiresPermission("card:update")
    @Operation(summary = "Block card", description = "Blocks a card permanently")
    public ResponseEntity<CardDTO> blockCard(
            @Parameter(description = "Card ID", required = true)
            @PathVariable UUID cardId) {
        logger.info("REST request to block card: {}", cardId);

        // Verify ownership or admin
        if (!canAccessCard(cardId)) {
            logger.warn("User attempted to block card without permission: {}", cardId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CardDTO result = cardService.blockCard(cardId);
        return ResponseEntity.ok(result);
    }

    // ============================================
    // UNBLOCK CARD - Admin only
    // ============================================
    @PostMapping("/{cardId}/unblock")
    @RequiresRole(UserRole.ADMIN)
    @RequiresPermission("card:update")
    @Operation(summary = "Unblock card", description = "Unblocks a card (Admin only)")
    public ResponseEntity<CardDTO> unblockCard(
            @Parameter(description = "Card ID", required = true)
            @PathVariable UUID cardId) {
        logger.info("REST request to unblock card: {}", cardId);

        CardDTO result = cardService.unblockCard(cardId);
        return ResponseEntity.ok(result);
    }

    // ============================================
    // CANCEL CARD
    // ============================================
    @DeleteMapping("/{cardId}")
    @RequiresPermission("card:delete")
    @Operation(summary = "Cancel card", description = "Cancels/deletes a card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "Card ID", required = true)
            @PathVariable UUID cardId) {
        logger.info("REST request to cancel card: {}", cardId);

        // Verify ownership or admin
        if (!canAccessCard(cardId)) {
            logger.warn("User attempted to cancel card without permission: {}", cardId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        cardService.deleteCard(cardId);
        bankingMetrics.recordCardCancelled("CANCELLED");

        return ResponseEntity.noContent().build();
    }

    // ============================================
    // CHANGE PIN
    // ============================================
    @PostMapping("/{cardId}/change-pin")
    @RequiresPermission("card:update")
    @Operation(summary = "Change PIN", description = "Changes the card PIN")
    public ResponseEntity<CardDTO> changePin(
            @Parameter(description = "Card ID", required = true)
            @PathVariable UUID cardId,
            @Parameter(description = "Old PIN", required = true)
            @RequestParam String oldPin,
            @Parameter(description = "New PIN", required = true)
            @RequestParam String newPin) {
        logger.info("REST request to change PIN for card: {}", cardId);

        // Verify ownership or admin
        if (!canAccessCard(cardId)) {
            logger.warn("User attempted to change PIN without permission: {}", cardId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CardDTO result = cardService.changePin(cardId, oldPin, newPin);
        return ResponseEntity.ok(result);
    }

    // ============================================
    // GET CARD BALANCE
    // ============================================
    @GetMapping("/{cardId}/balance")
    @RequiresPermission("card:read")
    @Operation(summary = "Get card balance", description = "Retrieves the available balance of a card")
    public ResponseEntity<BigDecimal> getCardBalance(
            @Parameter(description = "Card ID", required = true)
            @PathVariable UUID cardId) {
        logger.debug("REST request to get balance for card: {}", cardId);

        // Verify ownership or admin
        if (!canAccessCard(cardId)) {
            logger.warn("User attempted to get balance without permission: {}", cardId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        bankingMetrics.recordCardBalanceCheck();
        BigDecimal balance = cardService.getCardBalance(cardId);
        bankingMetrics.recordCardBalanceAmount(balance);

        return ResponseEntity.ok(balance);
    }

    // ============================================
    // REPLACE CARD (LOST/STOLEN)
    // ============================================
    @PostMapping("/{oldCardId}/replace")
    @RequiresPermission("card:create")
    @Operation(summary = "Replace card", description = "Replaces a lost/stolen card with a new one")
    public ResponseEntity<CardDTO> replaceCard(
            @Parameter(description = "Old card ID", required = true)
            @PathVariable UUID oldCardId,
            @Valid @RequestBody CreateCardRequest request) {
        logger.info("REST request to replace card: {}", oldCardId);

        // Verify ownership or admin
        if (!canAccessCard(oldCardId)) {
            logger.warn("User attempted to replace card without permission: {}", oldCardId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CardDTO result = cardService.replaceCard(oldCardId, request);
        return ResponseEntity.ok(result);
    }

    // ============================================
    // GET ACTIVE CARDS COUNT
    // ============================================
    @GetMapping("/my-active-count")
    @RequiresPermission("card:read")
    @Operation(summary = "Get my active cards count", description = "Retrieves count of active cards for current user")
    public ResponseEntity<Long> getMyActiveCardsCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        UUID userId = getUserIdFromEmail(email);
        long count = cardService.getActiveCardsCountByUserId(userId);

        return ResponseEntity.ok(count);
    }

    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean canAccessCard(UUID cardId) {
        if (isAdmin()) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        return cardService.getCardById(cardId)
                .map(card -> card.getUserEmail() != null &&
                        card.getUserEmail().equals(currentUserEmail))
                .orElse(false);
    }

    private UUID getUserIdFromEmail(String email) {
        return userService.getUserIdByEmail(email);
    }

    private String getUserEmailFromId(UUID userId) {
        return userService.getUserEmailById(userId);
    }
}