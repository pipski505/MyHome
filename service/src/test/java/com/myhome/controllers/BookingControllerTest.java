package com.myhome.controllers;

import com.myhome.services.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Contains two unit tests for verifying the deleteBooking method of the BookingController
 * class. The tests simulate different scenarios: successful deletion and non-existent
 * booking.
 */
public class BookingControllerTest {

  private final String TEST_AMENITY_ID = "test-amenity-id";
  private static final String TEST_BOOKING_ID = "test-booking-id";

  @Mock
  private BookingService bookingSDJpaService;

  @InjectMocks
  private BookingController bookingController;

  /**
   * Initializes all mock objects associated with the current test instance using
   * MockitoAnnotations. This is typically done to ensure that all necessary dependencies
   * are properly set up before each test method execution. The result is a clean slate
   * for each test, reducing the risk of unwanted side effects.
   */
  @BeforeEach
  private void init() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Deletes a booking by amenity ID and booking ID. It returns a response with no
   * content and a status code of NO CONTENT (204). The deletion is verified to have
   * occurred through the mock service.
   */
  @Test
  void deleteBooking() {
    // given
    given(bookingSDJpaService.deleteBooking(TEST_AMENITY_ID, TEST_BOOKING_ID))
        .willReturn(true);

    // when
    ResponseEntity<Void> response =
        bookingController.deleteBooking(TEST_AMENITY_ID, TEST_BOOKING_ID);

    // then
    assertNull(response.getBody());
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(bookingSDJpaService).deleteBooking(TEST_AMENITY_ID, TEST_BOOKING_ID);
  }

  /**
   * Tests the deletion of a booking that does not exist. It expects the response to
   * be a 404 error and null body when trying to delete a non-existent booking. The
   * test also verifies that the service's `deleteBooking` method was called with the
   * correct parameters.
   */
  @Test
  void deleteBookingNotExists() {
    // given
    given(bookingSDJpaService.deleteBooking(TEST_AMENITY_ID, TEST_BOOKING_ID))
        .willReturn(false);

    // when
    ResponseEntity<Void> response =
        bookingController.deleteBooking(TEST_AMENITY_ID, TEST_BOOKING_ID);

    // then
    assertNull(response.getBody());
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(bookingSDJpaService).deleteBooking(TEST_AMENITY_ID, TEST_BOOKING_ID);
  }
}
