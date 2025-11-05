package com.userservise.app.repository;

import com.userservise.app.model.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CardRepository extends JpaRepository<Card, Integer> {

}
