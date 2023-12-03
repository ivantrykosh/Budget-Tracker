package com.ivantrykosh.app.budgettracker.server.mappers;

import org.springframework.stereotype.Component;

/**
 * Generic Mapper interface for converting between entities and DTOs.
 *
 * @param <Entity> The type of the entity class.
 * @param <Dto> The type of the DTO class.
 */
@Component
public interface Mapper<Entity, Dto> {

    /**
     * Convert from entity to DTO
     *
     * @param entity entity to convert
     * @return DTO of the entity
     */
    Dto convertToDto(Entity entity);

    /**
     * Convert from DTO to entity
     *
     * @param dto DTO to convert
     * @return Entity of the DTO
     */
    Entity convertToEntity(Dto dto);
}
