package com.userservise.app.controllers;

import com.userservise.app.model.dto.CardDto;
import com.userservise.app.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/card")
public class CardController {

    private final CardService cardService;

    @GetMapping("/{id}")
    @PreAuthorize("@cardServiceImpl.isOwner(#id, principal.id) or hasRole('ADMIN')")
    public ResponseEntity<CardDto> getCardById(
            @PathVariable Integer id) {
        log.info("Received request to fetch card with ID: {}", id);
        CardDto card = cardService.getCardById(id);

        log.debug("Fetched card data: {}", card);
        return ResponseEntity.ok().body(card);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getAllCards(
            @RequestParam(required = false) String holder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Received request to fetch all cards");

        Pageable pageable = PageRequest.of(page, limit);
        Page<CardDto> cards = cardService.getAllCards(holder, pageable);

        log.debug("Fetched cards data: {}", cards);
        return ResponseEntity.ok().body(cards);
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("@cardServiceImpl.isOwner(#id, principal.id) or hasRole('ADMIN')")
    public ResponseEntity<List<CardDto>> getCardByUserId(
            @PathVariable("id") Integer userId){
        log.info("Received request to fetch card with UserId: {}", userId);

        List<CardDto> response = cardService.getAllByUserId(userId);
        log.debug("Fetched cards data by userId: {}", response);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create/{id}")
    @PreAuthorize("@cardServiceImpl.isOwner(#id, principal.id) or hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(
            @PathVariable("id") Integer userId) {
        log.info("Received request to create card for userId: {}", userId);

        CardDto response = cardService.createCard(userId);

        log.debug("Created card: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("@cardServiceImpl.isOwner(#id, principal.id) or hasRole('ADMIN')")
    public ResponseEntity<CardDto> updateCard(
            @PathVariable Integer id,
            @Valid @RequestBody CardDto cardDto) {
        log.info("Received request to update card with ID: {}", id);

        CardDto response = cardService.updateCard(id, cardDto);

        log.debug("Updated card: {}", response);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{id}/active")
    @PreAuthorize("@cardServiceImpl.isOwner(#id, principal.id) or hasRole('ADMIN')")
    public ResponseEntity<CardDto> setActiveCard(
            @PathVariable Integer id) {
        log.info("Received request to set active to card with ID: {}", id);

        CardDto response = cardService.activateCard(id);

        log.debug("Card {} was set active.", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/inactive")
    @PreAuthorize("@cardServiceImpl.isOwner(#id, principal.id) or hasRole('ADMIN')")
    public ResponseEntity<CardDto> setInactiveCard(
            @PathVariable Integer id) {
        log.info("Received request to set inactive to card with ID: {}", id);

        CardDto response = cardService.deactivateCard(id);

        log.debug("Card {} was set inactive.", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("@cardServiceImpl.isOwner(#id, principal.id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Integer id) {
        log.info("Received request to delete card with ID: {}", id);

        cardService.deleteCard(id);
        log.debug("Card {} was deleted.", id);
        return ResponseEntity.noContent().build();
    }
}