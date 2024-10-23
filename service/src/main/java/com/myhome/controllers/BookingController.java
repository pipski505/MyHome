package com.myhome.controllers;

import com.myhome.api.BookingsApi;
import com.myhome.services.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides a RESTful API endpoint for deleting bookings based on amenity and booking
 * IDs.
 * It uses a BookingService to interact with the database and returns a response
 * indicating success or failure.
 * It implements the BookingsApi interface.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class BookingController implements BookingsApi {

  private final BookingService bookingSDJpaService;

  /**
   * Performs a deletion operation on a booking based on the provided `amenityId` and
   * `bookingId`. It calls the `bookingSDJpaService` to delete the booking and returns
   * a `ResponseEntity` with a status of NO_CONTENT if the deletion is successful, or
   * NOT_FOUND if the booking is not found.
   *
   * @param amenityId unique identifier of the amenity associated with the booking being
   * deleted.
   *
   * @param bookingId identifier of the booking to be deleted from the system.
   *
   * @returns a ResponseEntity with a status code of either NO_CONTENT (200) or NOT_FOUND
   * (404).
   */
  @Override
  public ResponseEntity<Void> deleteBooking(@PathVariable String amenityId,
      @PathVariable String bookingId) {
    boolean isBookingDeleted = bookingSDJpaService.deleteBooking(amenityId, bookingId);
    if (isBookingDeleted) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
