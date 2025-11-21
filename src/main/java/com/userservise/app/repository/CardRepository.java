package com.userservise.app.repository;

import com.userservise.app.model.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CardRepository extends JpaRepository<Card, Integer>, JpaSpecificationExecutor<Card> {

    Optional<Card> findCardById(Integer id);

    List<Card> findCardsByOwnerId(Integer id);

    Boolean existsCardByNumber(String number);

    @Query("SELECT c.number FROM Card c")
    List<String> findAllCardsByNumber();
}
