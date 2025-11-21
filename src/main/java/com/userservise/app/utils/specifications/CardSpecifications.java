package com.userservise.app.utils.specifications;

import com.userservise.app.model.entity.Card;
import org.springframework.data.jpa.domain.Specification;

public class CardSpecifications {

    public static Specification<Card> hasHolder(String holder) {
        return (root, query, cb) ->
                holder == null ? null : cb.like(cb.lower(root.get("holder")), "%" + holder.toLowerCase() + "%");
    }
}
