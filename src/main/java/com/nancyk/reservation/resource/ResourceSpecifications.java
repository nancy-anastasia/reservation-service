package com.nancyk.reservation.resource;

import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class ResourceSpecifications {

    private ResourceSpecifications() {
    }

    public static Specification<Resource> hasType(ResourceType type) {
        return (root, query, criteriaBuilder) ->
                type == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<Resource> hasActive(Boolean active) {
        return (root, query, criteriaBuilder) ->
                active == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("active"), active);
    }

    public static Specification<Resource> nameContains(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase(Locale.ROOT) + "%"
            );
        };
    }
}