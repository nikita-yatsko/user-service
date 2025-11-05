package com.userservise.app.model.dto;

import com.userservise.app.model.enums.ActiveStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class CardDto {

    private Integer id;

    @NotNull
    private long number;

    @NotBlank(message = "Holder can not be empty")
    @Size(min = 2, max = 100, message = "Holder must be between 2 and 100 characters")
    private String holder;

    @NotNull(message = "Expiration date cannot be null")
    private Date expirationDate;

    @NotNull(message = "Active status must be set")
    private ActiveStatus active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
