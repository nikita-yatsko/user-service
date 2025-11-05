package com.userservise.app.utils;

import com.userservise.app.model.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<User> hasFirstName(String firstName) {
        return (root, query, cb) ->
                firstName == null ? null : cb.like(cb.lower(root.get("name")), "%" + firstName.toLowerCase() + "%");
    }

    public static Specification<User> hasSurname(String surname) {
        return (root, query, cb) ->
                surname == null ? null : cb.like(cb.lower(root.get("name")), "%" + surname.toLowerCase() + "%");
    }
}
