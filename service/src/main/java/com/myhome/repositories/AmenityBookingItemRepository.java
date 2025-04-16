package com.myhome.repositories;

import com.myhome.domain.AmenityBookingItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Extends JpaRepository to provide data access for AmenityBookingItem entities with
 * custom method for retrieving an item by a specific ID.
 */
public interface AmenityBookingItemRepository extends JpaRepository<AmenityBookingItem, String> {
  Optional<AmenityBookingItem> findByAmenityBookingItemId(String amenityBookingItemId);
}
