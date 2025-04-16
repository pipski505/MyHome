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
 * This class provides unit tests for a BookingController, specifically testing the
 * deleteBooking method under different scenarios.
 */
public class BookingControllerTest {

  private final String TEST_AMENITY_ID = "test-amenity-id";
  private static final String TEST_BOOKING_ID = "test-booking-id";

  @Mock
  private BookingService bookingSDJpaService;

  @InjectMocks
  private BookingController bookingController;

  /**
   * Initializes Mockito annotations in the current test class.
   * Mockito is a mocking framework used for unit testing.
   * It creates mock objects for dependencies.
   */
  @BeforeEach
  private void init() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Tests the deletion of a booking by a controller. It verifies the response status
   * as NO_CONTENT and the absence of a response body.
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
   * Tests the deletion of a non-existent booking by checking the response status and
   * body when a booking with a specified ID does not exist in the database. It verifies
   * that the service method is called and returns a 404 status code with no response
   * body.
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
