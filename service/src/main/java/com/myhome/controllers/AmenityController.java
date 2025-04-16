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
 * Handles CRUD operations for amenities, providing endpoints for retrieving, creating,
 * updating, and deleting amenities, as well as listing all amenities for a given community.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class AmenityController implements AmenitiesApi {

  private final AmenityService amenitySDJpaService;
  private final AmenityApiMapper amenityApiMapper;

  /**
   * Retrieves amenity details based on the provided `amenityId`, maps the result to a
   * `GetAmenityDetailsResponse` object, and returns a `ResponseEntity` with a 200
   * status code if found, or a 404 status code if not found.
   *
   * @param amenityId identifier of the amenity for which details are to be retrieved.
   *
   * @returns a ResponseEntity containing an AmenityDetailsResponse if the amenity is
   * found, or a 404 response if not.
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
   * Returns a list of all amenities within a specified community as a ResponseEntity.
   *
   * @param communityId identifier for the community for which all amenities are to be
   * listed.
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
   * Adds amenities to a community, retrieves the added amenities, and returns them as
   * a response entity.
   *
   * @param communityId identifier of the community to which an amenity is being added.
   *
   * @param request AddAmenityRequest object containing a list of amenities to be added
   * to a community.
   *
   * @returns a ResponseEntity containing an AddAmenityResponse object with amenities,
   * or a 404 not found response.
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
   * Deletes an amenity based on the provided ID, checks the deletion status, and returns
   * a corresponding HTTP response. A successful deletion returns a 204 (NO_CONTENT)
   * response, while an unsuccessful deletion returns a 404 (NOT_FOUND) response.
   *
   * @param amenityId identifier for the amenity to be deleted.
   *
   * @returns a ResponseEntity with either a NO_CONTENT status or a NOT_FOUND status.
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
   * Updates an amenity in the database based on the provided `amenityId` and
   * `UpdateAmenityRequest` object. It returns a `ResponseEntity` with a status code
   * indicating whether the update was successful or not.
   *
   * @param amenityId ID of the amenity being updated, which is used to update the
   * amenity in the database.
   *
   * @param request data sent in the request body to update an amenity, validated by
   * the `@Valid` annotation.
   *
   * Extract.
   *
   * UpdateAmenityRequest
   *   - has properties for amenity name, description and price.
   *
   * @returns either a 204 (NO_CONTENT) response if the update is successful or a 404
   * (NOT_FOUND) response if not.
   *
   * The output returned by the `updateAmenity` function is of type `ResponseEntity<Void>`.
   * It contains a status code and no response body.
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
