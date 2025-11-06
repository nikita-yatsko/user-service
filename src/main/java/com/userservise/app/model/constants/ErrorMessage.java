package com.userservise.app.model.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorMessage {

    USER_NOT_FOUND_BY_ID("User with id: %s was not found"),
    USER_CANNOT_HAVE_MORE_THAN_5_CARDS("User with id: %s has more than 5 cards"),
    EMAIL_ALREADY_EXISTS("User with email: %s already exists"),

    CARD_NOT_FOUND_BY_ID("Card with id: %s was not found"),
    CARD_NUMBER_ALREADY_EXISTS("Card with number: %s already exists"),
    ;

    private final String message;

    public String getMessage(Object... args) {
        return String.format(message, args);
    }
}
