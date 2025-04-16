package com.myhome.services;

/**
 * Defines a contract for managing bookings by providing a method to delete bookings
 * based on amenity and booking IDs.
 */
public interface BookingService {

  boolean deleteBooking(String amenityId, String bookingId);

}
