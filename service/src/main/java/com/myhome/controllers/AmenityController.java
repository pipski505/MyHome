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
 * CRUD operations (create, read, update, delete) on amenities, including getting all
 * amenities for a given community, adding new amenities to a community, updating
 * existing amenities, and deleting amenities.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class AmenityController implements AmenitiesApi {

  private final AmenityService amenitySDJpaService;
  private final AmenityApiMapper amenityApiMapper;

  /**
   * Retrieves amenity details for a given ID using JPA service and maps the response
   * to `AmenityDetailsResponse`. If the amenity is not found, it returns a `ResponseEntity`
   * with a `NOT_FOUND` status.
   * 
   * @param amenityId ID of the amenity for which details are requested.
   * 
   * @returns an `ResponseEntity` object representing a successful response with the
   * details of the requested amenity.
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
   * Retrieves a set of amenities associated with a given community ID using JPA service,
   * maps them to `GetAmenityDetailsResponse` set format using API mapper, and returns
   * an `Ok` response entity with the mapped set.
   * 
   * @param communityId ID of the community for which the list of amenities is being retrieved.
   * 
   * @returns a `ResponseEntity` object containing a set of `GetAmenityDetailsResponse`
   * objects.
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
   * Adds amenities to a community by creating them in the database and returning an
   * response entity indicating whether the operation was successful or not.
   * 
   * @param communityId identifier of the community to which the amenities will be added.
   * 
   * @param request AddAmenityRequest object containing the amenities to be added to
   * the community.
   * 
   * @returns a `ResponseEntity` object representing the response to the request, with
   * the status code and body containing the added amenities.
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
   * Deletes an amenity from the database based on the provided amenity ID, returning
   * a HTTP status code indicating the result of the operation.
   * 
   * @param amenityId ID of the amenity to be deleted.
   * 
   * @returns a HTTP `NO_CONTENT` status code indicating successful deletion of the amenity.
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
   * Updates an amenity by mapping the request to an AmenityDto object, setting its ID,
   * and then updating it using the amenity SDJpa service. If the update is successful,
   * a NO_CONTENT status code is returned. If not, a NOT_FOUND status code is returned.
   * 
   * @param amenityId ID of the amenity being updated.
   * 
   * @param request UpdateAmenityRequest object containing the details of the amenity
   * to be updated, which is then converted into an AmenityDto object by the amenityApiMapper
   * and used for updating the amenity in the database.
   * 
   * 	- `@Valid` - Indicates that the request body must be valid according to the schema
   * defined in the `@ValidationBean` annotation.
   * 	- `@RequestBody` - Represents the request body as a whole, rather than individual
   * fields or parameters.
   * 
   * @returns a `ResponseEntity` object with a status code indicating whether the amenity
   * was updated successfully or not.
   * 
   * 	- `isUpdated`: A boolean value indicating whether the amenity was updated
   * successfully or not.
   * 	- `HttpStatus`: The HTTP status code of the response, which can be either NO_CONTENT
   * (204) or NOT_FOUND (404).
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
