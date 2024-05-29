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
 * Provides RESTful APIs for managing amenities in a community. The controller handles
 * requests to get all amenities, list all amenities for a specific community, add
 * new amenities, delete existing amenities, and update existing amenities. The
 * controller utilizes dependencies with the AmenityService and AmenityApiMapper
 * classes to perform these operations.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class AmenityController implements AmenitiesApi {

  private final AmenityService amenitySDJpaService;
  private final AmenityApiMapper amenityApiMapper;

  /**
   * Retrieves amenity details for a given ID using a JPA service and maps the response
   * to an `AmenityDetailsResponse`. It returns an `OK` response if the amenity is
   * found, or a `NOT_FOUND` response otherwise.
   * 
   * @param amenityId identifier of the amenity for which details are requested.
   * 
   * @returns an `ResponseEntity` object representing a successful response with an
   * `OK` status and the requested amenity details.
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
   * Retrieves a list of amenities from the database using `amenitySDJpaService`, maps
   * them to `GetAmenityDetailsResponse` set using `amenityApiMapper`, and returns an
   * `ResponseEntity` with the mapped response.
   * 
   * @param communityId ID of the community whose amenities will be listed.
   * 
   * @returns a set of `GetAmenityDetailsResponse` objects containing the details of
   * all amenities for a given community.
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
   * Adds amenities to a community using the `amenitySDJpaService`. It creates a list
   * of amenities, maps it to a response entity, and returns it as a successful response
   * if the amenities are added successfully.
   * 
   * @param communityId id of the community to which the amenities will be added.
   * 
   * @param request AddAmenityRequest object containing the amenities to be added to
   * the community.
   * 
   * @returns a `ResponseEntity` object representing a successful addition of amenities
   * to a community, with a status code of `ok`.
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
   * Deletes an amenity from the database based on the specified `amenityId`. If
   * successful, it returns a `ResponseEntity` with a status code of `NO_CONTENT`. If
   * unsuccessful, it returns a `ResponseEntity` with a status code of `NOT_FOUND`.
   * 
   * @param amenityId ID of an amenity to be deleted.
   * 
   * @returns a `ResponseEntity` object with a status code of either `NO_CONTENT` or
   * `NOT_FOUND`, depending on whether the amenity was successfully deleted.
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
   * Updates an amenity's details using a `UpdateAmenityRequest` object, and returns a
   * response entity indicating whether the update was successful or not.
   * 
   * @param amenityId ID of the amenity being updated.
   * 
   * @param request UpdateAmenityRequest object containing the details of the amenity
   * to be updated, which is then converted into an AmenityDto object and passed to the
   * updateAmenity method of the amenitySDJpaService for updates.
   * 
   * 	- `@Valid`: Indicates that the request body must contain a valid JSON object
   * representing an update amenity request.
   * 	- `@RequestBody`: Represents the request body as a JSON object, which is used to
   * map the request data to the `AmenityDto` class.
   * 
   * @returns a `ResponseEntity` object with a status code indicating whether the amenity
   * was successfully updated or not.
   * 
   * 	- The response entity is of type `ResponseEntity`, which indicates that the
   * operation was successful.
   * 	- The status code of the response entity is `HttpStatus.NO_CONTENT`, indicating
   * that the amenity was updated successfully.
   * 	- The `build()` method is used to create a new response entity with the specified
   * status code and headers.
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
