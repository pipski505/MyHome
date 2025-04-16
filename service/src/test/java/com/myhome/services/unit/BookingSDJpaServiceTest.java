package com.myhome.services.unit;

import com.myhome.domain.AmenityBookingItem;
import com.myhome.repositories.AmenityBookingItemRepository;
import com.myhome.services.springdatajpa.BookingSDJpaService;
import helpers.TestUtils;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Provides unit tests for the BookingSDJpaService class.
 */
public class BookingSDJpaServiceTest {

  private static final String TEST_BOOKING_ID = "test-booking-id";
  private static final String TEST_AMENITY_ID = "test-amenity-id";
  private static final String TEST_AMENITY_ID_2 = "test-amenity-id-2";
  private final String TEST_AMENITY_DESCRIPTION = "test-amenity-description";

  @Mock
  private AmenityBookingItemRepository bookingItemRepository;

  @InjectMocks
  private BookingSDJpaService bookingSDJpaService;

  /**
   * Initializes Mockito mocks for the current test class, making them available for
   * use in test methods. This setup is typically used with JUnit tests to simplify
   * mocking dependencies.
   */
  @BeforeEach
  private void init() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Tests the deletion of a booking item in a database. It verifies that the booking
   * item is successfully deleted and that the correct repository methods are called
   * to retrieve and delete the item.
   */
  @Test
  void deleteBookingItem() {
    // given
    AmenityBookingItem testBookingItem = getTestBookingItem();

    given(bookingItemRepository.findByAmenityBookingItemId(TEST_BOOKING_ID))
        .willReturn(Optional.of(testBookingItem));
    testBookingItem.setAmenity(TestUtils.AmenityHelpers
        .getTestAmenity(TEST_AMENITY_ID, TEST_AMENITY_DESCRIPTION));

    // when
    boolean bookingDeleted = bookingSDJpaService.deleteBooking(TEST_AMENITY_ID, TEST_BOOKING_ID);

    // then
    assertTrue(bookingDeleted);
    verify(bookingItemRepository).findByAmenityBookingItemId(TEST_BOOKING_ID);
    verify(bookingItemRepository).delete(testBookingItem);
  }

  /**
   * Tests the deletion of a non-existent booking. It simulates a repository that returns
   * an empty Optional when searching for the booking. The service is then called to
   * delete the booking, and the test verifies that the deletion is not performed.
   */
  @Test
  void deleteBookingNotExists() {
    // given
    given(bookingItemRepository.findByAmenityBookingItemId(TEST_BOOKING_ID))
        .willReturn(Optional.empty());

    // when
    boolean bookingDeleted = bookingSDJpaService.deleteBooking(TEST_AMENITY_ID, TEST_BOOKING_ID);

    // then
    assertFalse(bookingDeleted);
    verify(bookingItemRepository).findByAmenityBookingItemId(TEST_BOOKING_ID);
    verify(bookingItemRepository, never()).delete(any());
  }

  /**
   * Tests the deletion of a booking amenity that does not exist. It simulates a scenario
   * where an amenity with a specific ID does not match the one in the booking, resulting
   * in a failed deletion.
   */
  @Test
  void deleteBookingAmenityNotExists() {
    // given
    AmenityBookingItem testBookingItem = getTestBookingItem();

    given(bookingItemRepository.findByAmenityBookingItemId(TEST_BOOKING_ID))
        .willReturn(Optional.of(testBookingItem));
    testBookingItem.setAmenity(TestUtils.AmenityHelpers
        .getTestAmenity(TEST_AMENITY_ID_2, TEST_AMENITY_DESCRIPTION));
    // when
    boolean bookingDeleted = bookingSDJpaService.deleteBooking(TEST_AMENITY_ID, TEST_BOOKING_ID);

    // then
    assertFalse(bookingDeleted);
    assertNotEquals(TEST_AMENITY_ID, testBookingItem.getAmenity().getAmenityId());
    verify(bookingItemRepository).findByAmenityBookingItemId(TEST_BOOKING_ID);
    verify(bookingItemRepository, never()).delete(any());
  }

  /**
   * Creates a new instance of `AmenityBookingItem` and initializes it with a specific
   * `TEST_BOOKING_ID`.
   *
   * @returns an instance of `AmenityBookingItem` with `TEST_BOOKING_ID` as the `amenityBookingItemId`.
   */
  private AmenityBookingItem getTestBookingItem() {
    return new AmenityBookingItem()
        .withAmenityBookingItemId(TEST_BOOKING_ID);
  }
}
