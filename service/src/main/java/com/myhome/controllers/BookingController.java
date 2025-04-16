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
 * Handles booking deletion requests and returns HTTP responses accordingly. It relies
 * on the BookingService to perform the actual deletion. The class implements the
 * BookingsApi interface.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class BookingController implements BookingsApi {

  private final BookingService bookingSDJpaService;

  /**
   * Deletes a booking based on the provided `amenityId` and `bookingId`. If the booking
   * is successfully deleted, it returns a 204 (NO_CONTENT) response; otherwise, it
   * returns a 404 (NOT_FOUND) response indicating the booking was not found.
   *
   * @param amenityId identifier of the amenity associated with the booking being deleted.
   *
   * @param bookingId identifier of the booking to be deleted from the system.
   *
   * @returns either a HTTP 204 (NO_CONTENT) response if the booking is deleted or a
   * 404 (NOT_FOUND) response otherwise.
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
