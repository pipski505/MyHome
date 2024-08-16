/*
 * Copyright 2020 Prathab Murugan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myhome.controllers;

import com.myhome.api.AmenitiesApi;
import com.myhome.controllers.mapper.AmenityApiMapper;
import com.myhome.domain.Amenity;
import com.myhome.model.AddAmenityRequest;
import com.myhome.model.AddAmenityResponse;
import com.myhome.model.AmenityDto;
import com.myhome.model.GetAmenityDetailsResponse;
import com.myhome.model.UpdateAmenityRequest;
import com.myhome.services.AmenityService;
import java.util.Set;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Is responsible for handling RESTful API requests related to amenities, providing
 * CRUD (Create, Read, Update, Delete) operations and retrieving amenity details.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class AmenityController implements AmenitiesApi {

  private final AmenityService amenitySDJpaService;
  private final AmenityApiMapper amenityApiMapper;

  /**
   * Retrieves an amenity by ID, maps it to a response object using `amenityApiMapper`,
   * and returns a ResponseEntity containing the response. If no matching amenity is
   * found, it returns a ResponseEntity with a NOT_FOUND status code.
   *
   * @param amenityId identifier of an amenity for which details are requested and
   * passed to the `amenitySDJpaService` to retrieve the corresponding data.
   *
   * @returns a ResponseEntity containing GetAmenityDetailsResponse.
   */
  @Override
  public ResponseEntity<GetAmenityDetailsResponse> getAmenityDetails(
      @PathVariable String amenityId) {
    return amenitySDJpaService.getAmenityDetails(amenityId)
        .map(amenityApiMapper::amenityToAmenityDetailsResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  /**
   * Retrieves a set of amenities for a given community ID, maps them to a set of
   * `GetAmenityDetailsResponse` objects, and returns the response as an HTTP OK response
   * entity.
   *
   * @param communityId String identifier for a community, used to filter and retrieve
   * a set of amenities associated with it.
   *
   * @returns a ResponseEntity containing a Set of GetAmenityDetailsResponses.
   */
  @Override
  public ResponseEntity<Set<GetAmenityDetailsResponse>> listAllAmenities(
      @PathVariable String communityId) {
    Set<Amenity> amenities = amenitySDJpaService.listAllAmenities(communityId);
    Set<GetAmenityDetailsResponse> response =
        amenityApiMapper.amenitiesSetToAmenityDetailsResponseSet(amenities);
    return ResponseEntity.ok(response);
  }

  /**
   * Creates amenities for a community specified by `communityId`. It accepts an
   * `AddAmenityRequest` object containing amenities to be created, and returns a
   * `ResponseEntity` with a list of added amenities. If no amenities are found, it
   * returns a `ResponseEntity` with a 404 status code.
   *
   * @param communityId identifying value for a community, which is used to create
   * amenities within that specific community.
   *
   * @param request request data containing amenities to be added to a community, which
   * is retrieved from the body of the HTTP request and passed to the `createAmenities`
   * method for processing.
   *
   * @returns a ResponseEntity containing an AddAmenityResponse.
   */
  @Override
  public ResponseEntity<AddAmenityResponse> addAmenityToCommunity(
      @PathVariable String communityId,
      @RequestBody AddAmenityRequest request) {
    return amenitySDJpaService.createAmenities(request.getAmenities(), communityId)
        .map(amenityList -> new AddAmenityResponse().amenities(amenityList))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Deletes an amenity with a specified ID and returns a response indicating success
   * or failure. It calls the `deleteAmenity` method of `amenitySDJpaService` to perform
   * the deletion, and based on its result, returns a `ResponseEntity` with either a
   * 204 No Content status or a 404 Not Found status.
   *
   * @param amenityId ID of an amenity to be deleted, which is passed as a path variable
   * and used by the `amenitySDJpaService` to delete the corresponding amenity record.
   *
   * @returns a response entity with either a HTTP NO CONTENT or NOT FOUND status.
   */
  @Override
  public ResponseEntity deleteAmenity(@PathVariable String amenityId) {
    boolean isAmenityDeleted = amenitySDJpaService.deleteAmenity(amenityId);
    if (isAmenityDeleted) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Updates an amenity based on a provided ID and updated request data. It maps the
   * request to an AmenityDto object, sets the ID, and updates the amenity using a
   * service method. If successful, it returns a HTTP response with a status of NO_CONTENT;
   * otherwise, NOT_FOUND.
   *
   * @param amenityId identifier of the amenity to be updated, which is used to set the
   * corresponding ID on the `AmenityDto` object.
   *
   * @param request UpdateAmenityRequest object, which is validated and then used to
   * create an AmenityDto object through the amenityApiMapper's updateAmenityRequestToAmenityDto
   * method.
   *
   * The `request` object has no direct properties to destructure as it's an instance
   * of `UpdateAmenityRequest`, which is a custom class not provided in the code snippet.
   *
   * @returns a HTTP response with either NO CONTENT or NOT FOUND status.
   *
   * Returns a ResponseEntity with Void data type. The response entity's status is
   * either HTTP NO_CONTENT (204) or NOT_FOUND (404).
   */
  @Override
  public ResponseEntity<Void> updateAmenity(@PathVariable String amenityId,
      @Valid @RequestBody UpdateAmenityRequest request) {
    AmenityDto amenityDto = amenityApiMapper.updateAmenityRequestToAmenityDto(request);
    amenityDto.setAmenityId(amenityId);
    boolean isUpdated = amenitySDJpaService.updateAmenity(amenityDto);
    if (isUpdated) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
