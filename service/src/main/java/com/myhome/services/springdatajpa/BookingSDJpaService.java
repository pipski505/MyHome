package com.myhome.services.springdatajpa;

import com.myhome.domain.AmenityBookingItem;
import com.myhome.repositories.AmenityBookingItemRepository;
import com.myhome.services.BookingService;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Provides a service for managing bookings using Spring Data JPA. It handles deletion
 * of bookings based on amenity and booking IDs. The deletion operation is transactional
 * and atomic.
 */
@Service
@RequiredArgsConstructor
public class BookingSDJpaService implements BookingService {

  private final AmenityBookingItemRepository bookingRepository;

  /**
   * Deletes a booking from the database if it matches the provided `amenityId` and
   * `bookingId`. It uses a repository to find the booking, checks if the amenity ID
   * matches, and then deletes the booking if it does.
   *
   * @param amenityId identifier used to verify if a booking is associated with the
   * specified amenity before it can be deleted.
   *
   * @param bookingId unique identifier of a booking item that is to be deleted.
   *
   * @returns a boolean indicating success (true) or failure (false) of the deletion operation.
   */
  @Transactional
  @Override
  public boolean deleteBooking(String amenityId, String bookingId) {
    Optional<AmenityBookingItem> booking =
        bookingRepository.findByAmenityBookingItemId(bookingId);
    return booking.map(bookingItem -> {
      boolean amenityFound =
          bookingItem.getAmenity().getAmenityId().equals(amenityId);
      if (amenityFound) {
        bookingRepository.delete(bookingItem);
        return true;
      } else {
        return false;
      }
    }).orElse(false);
  }
}
