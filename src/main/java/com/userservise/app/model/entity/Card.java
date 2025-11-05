package com.userservise.app.model.entity;

import com.userservise.app.model.enums.ActiveStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "payment_cards")
public class Card extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User owner;

    @Column(name = "number")
    private String number;

    @Column(name = "holder")
    private String holder;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "active")
    private ActiveStatus active;
}
