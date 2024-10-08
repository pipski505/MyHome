package com.myhome.repositories;

import com.myhome.domain.AmenityBookingItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Extends Spring Data JPA to provide database operations for the AmenityBookingItem
 * entity.
 */
public interface AmenityBookingItemRepository extends JpaRepository<AmenityBookingItem, String> {
  Optional<AmenityBookingItem> findByAmenityBookingItemId(String amenityBookingItemId);
}
