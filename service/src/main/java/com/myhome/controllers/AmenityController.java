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
 * Handles CRUD operations for amenities, mapping request and response data between
 * the API and service layers.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class AmenityController implements AmenitiesApi {

  private final AmenityService amenitySDJpaService;
  private final AmenityApiMapper amenityApiMapper;

  /**
   * Retrieves amenity details by ID from the database using `amenitySDJpaService`,
   * maps the result to a response object using `amenityApiMapper`, and returns a
   * `ResponseEntity` containing the response or a 404 status if no details are found.
   *
   * @param amenityId unique identifier of the amenity for which details are being retrieved.
   *
   * @returns a `ResponseEntity` containing `GetAmenityDetailsResponse` data or a
   * `NOT_FOUND` status.
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
   * Retrieves a set of amenities for a specified community ID, maps them to a set of
   * GetAmenityDetailsResponse objects, and returns a successful HTTP response with the
   * mapped amenities.
   *
   * @param communityId identifier for the community from which a list of all amenities
   * is retrieved.
   *
   * @returns a ResponseEntity containing a Set of GetAmenityDetailsResponse objects.
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
   * Adds amenities to a community by the provided ID, returns a response entity
   * containing the added amenities if successful, or a not found response if the
   * community does not exist.
   *
   * @param communityId identifier of the community to which amenities are being added.
   *
   * @param request AddAmenityRequest object containing the amenities to be added to
   * the specified community.
   *
   * @returns a `ResponseEntity` containing either a list of added amenities or a 404
   * Not Found response.
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
   * Deletes an amenity by its ID, returns a 204 (NO_CONTENT) response if deleted, and
   * a 404 (NOT_FOUND) response if the amenity does not exist. It relies on the
   * `amenitySDJpaService` to handle the deletion operation.
   *
   * @param amenityId identifier of the amenity to be deleted.
   *
   * @returns either a 204 No Content response or a 404 Not Found response.
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
   * Updates an amenity in the database based on its ID. It takes an `UpdateAmenityRequest`
   * object, maps it to an `AmenityDto` object, and then calls the `updateAmenity`
   * method of the `amenitySDJpaService` to perform the update.
   *
   * @param amenityId identifier of the amenity to be updated.
   *
   * @param request UpdateAmenityRequest object containing the updated amenity details.
   *
   * Extract.
   * The `UpdateAmenityRequest` object likely contains properties such as `id`, `name`,
   * `description`, and `type`, which are used to update an amenity.
   *
   * @returns a ResponseEntity with a HTTP status of 204 (NO_CONTENT) on success or 404
   * (NOT_FOUND) on failure.
   *
   * The ResponseEntity is returned with a status code of either HttpStatus.NO_CONTENT
   * (204) or HttpStatus.NOT_FOUND (404), indicating whether the amenity was updated
   * or not.
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
