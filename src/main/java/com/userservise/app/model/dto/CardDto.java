package com.userservise.app.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.userservise.app.model.enums.ActiveStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CardDto {

    private Integer id;

    @NotNull
    private String number;

    @NotBlank(message = "Holder can not be empty")
    @Size(min = 2, max = 100, message = "Holder must be between 2 and 100 characters")
    private String holder;

    @NotNull(message = "Expiration date cannot be null")
    private LocalDate expirationDate;

    @NotNull(message = "Active status must be set")
    private ActiveStatus active;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedAt;
}
