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

import com.myhome.api.CommunitiesApi;
import com.myhome.controllers.dto.CommunityDto;
import com.myhome.controllers.mapper.CommunityApiMapper;
import com.myhome.domain.Community;
import com.myhome.domain.CommunityHouse;
import com.myhome.domain.User;
import com.myhome.model.AddCommunityAdminRequest;
import com.myhome.model.AddCommunityAdminResponse;
import com.myhome.model.AddCommunityHouseRequest;
import com.myhome.model.AddCommunityHouseResponse;
import com.myhome.model.CommunityHouseName;
import com.myhome.model.CreateCommunityRequest;
import com.myhome.model.CreateCommunityResponse;
import com.myhome.model.GetCommunityDetailsResponse;
import com.myhome.model.GetCommunityDetailsResponseCommunity;
import com.myhome.model.GetHouseDetailsResponse;
import com.myhome.model.ListCommunityAdminsResponse;
import com.myhome.services.CommunityService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller which provides endpoints for managing community
 */
@RequiredArgsConstructor
@RestController
@Slf4j
public class CommunityController implements CommunitiesApi {
  private final CommunityService communityService;
  private final CommunityApiMapper communityApiMapper;

  /**
   * Processes a community creation request, maps it to a community DTO, creates a
   * community using the service layer, maps the created community to a response object,
   * and returns a 201 Created HTTP response with the community response.
   *
   * @param request CreateCommunityRequest object that contains the data for creating
   * a new community and is validated before being processed.
   *
   * Destructure the `request` object as a `CreateCommunityRequest` with the following
   * properties:
   *
   * - `name`:
   * - `description`:
   * - `tags`:
   *
   * @returns a `Community` object converted to a `CreateCommunityResponse` object.
   *
   * The returned object is of type `ResponseEntity<CreateCommunityResponse>`, which
   * contains a `status` and a `body`. The `body` is an instance of `CreateCommunityResponse`.
   */
  @Override
  public ResponseEntity<CreateCommunityResponse> createCommunity(@Valid @RequestBody
      CreateCommunityRequest request) {
    log.trace("Received create community request");
    CommunityDto requestCommunityDto =
        communityApiMapper.createCommunityRequestToCommunityDto(request);
    Community createdCommunity = communityService.createCommunity(requestCommunityDto);
    CreateCommunityResponse createdCommunityResponse =
        communityApiMapper.communityToCreateCommunityResponse(createdCommunity);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdCommunityResponse);
  }

  /**
   * Handles a request to retrieve all community details,
   * uses a service to list all communities based on pagination,
   * and returns a response containing the community details in a specific format.
   *
   * @param pageable pagination settings for the response, allowing for the retrieval
   * of a specific number of results at a time, in this case, 200 by default.
   *
   * Extract its `size` property, which is set to 200 by default.
   *
   * @returns a JSON response containing a list of community details in a paginated format.
   *
   * Contain a ResponseEntity object with a status of OK (200) and a body of type GetCommunityDetailsResponse.
   */
  @Override
  public ResponseEntity<GetCommunityDetailsResponse> listAllCommunity(
      @PageableDefault(size = 200) Pageable pageable) {
    log.trace("Received request to list all community");

    Set<Community> communityDetails = communityService.listAll(pageable);
    Set<GetCommunityDetailsResponseCommunity> communityDetailsResponse =
        communityApiMapper.communitySetToRestApiResponseCommunitySet(communityDetails);

    GetCommunityDetailsResponse response = new GetCommunityDetailsResponse();
    response.getCommunities().addAll(communityDetailsResponse);

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Handles a request to retrieve community details by ID,
   * mapping the response to a `GetCommunityDetailsResponse` object,
   * returning a `ResponseEntity` with a 200 status if found, or 404 if not found.
   *
   * @param communityId identifier of the community for which details are to be retrieved.
   *
   * @returns a list of community details wrapped in a ResponseEntity, or a 404 response
   * if not found.
   *
   * The output is a `ResponseEntity` object, specifically a `ResponseEntity` of type
   * `GetCommunityDetailsResponse`. It contains a list of community details in its body.
   */
  @Override
  public ResponseEntity<GetCommunityDetailsResponse> listCommunityDetails(
      @PathVariable String communityId) {
    log.trace("Received request to get details about community with id[{}]", communityId);

    return communityService.getCommunityDetailsById(communityId)
        .map(communityApiMapper::communityToRestApiResponseCommunity)
        .map(Arrays::asList)
        .map(HashSet::new)
        .map(communities -> new GetCommunityDetailsResponse().communities(communities))
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * Handles a request to retrieve a list of community administrators for a specified
   * community ID, returning a response with the list of admins if found, or a 404
   * response if not found.
   *
   * @param communityId identifier of the community for which all admins are to be listed.
   *
   * @param pageable pagination criteria, enabling the retrieval of a specified number
   * of results at a time.
   *
   * Extract the main properties of `pageable`:
   *
   *   - `pageable` is an object of type `Pageable`.
   *   - It has a default size of 200 elements.
   *   - Its properties are not explicitly used in the function.
   *
   * @returns a ResponseEntity containing a ListCommunityAdminsResponse object with a
   * set of community admins.
   *
   * The returned output is of type `ResponseEntity<ListCommunityAdminsResponse>`.
   */
  @Override
  public ResponseEntity<ListCommunityAdminsResponse> listCommunityAdmins(
      @PathVariable String communityId,
      @PageableDefault(size = 200) Pageable pageable) {
    log.trace("Received request to list all admins of community with id[{}]", communityId);

    return communityService.findCommunityAdminsById(communityId, pageable)
        .map(HashSet::new)
        .map(communityApiMapper::communityAdminSetToRestApiResponseCommunityAdminSet)
        .map(admins -> new ListCommunityAdminsResponse().admins(admins))
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * Receives a community ID and a pageable request, logs the request, and returns a
   * response containing a set of community houses in the specified page.
   *
   * @param communityId identifier of the community for which all houses are to be listed.
   *
   * @param pageable pagination settings, such as page size, for the list of community
   * houses.
   *
   * Deconstruct `pageable` to its main components:
   *
   * - `pageNumber`: The current page number.
   * - `pageSize`: The number of items per page, set to 200 by default.
   * - `sort`: A `Sort` object specifying the sort order.
   * - `offset`: The number of items to skip before returning the first item.
   * - `pageNumberOrOffset`: This is a boolean indicating whether `pageNumber` or
   * `offset` is used for pagination.
   * - `paged`: A boolean indicating whether pagination is enabled.
   * - `sortOrSortDefined`: This is a boolean indicating whether `sort` is defined or
   * not.
   *
   * @returns a `ResponseEntity` containing a `GetHouseDetailsResponse` object with a
   * set of houses or a 404 not found response.
   *
   * Include a ResponseEntity with a HTTP status code of 200 (OK) if houses are found,
   * or a status code of 404 (NOT FOUND) otherwise.
   */
  @Override
  public ResponseEntity<GetHouseDetailsResponse> listCommunityHouses(
      @PathVariable String communityId,
      @PageableDefault(size = 200) Pageable pageable) {
    log.trace("Received request to list all houses of community with id[{}]", communityId);

    return communityService.findCommunityHousesById(communityId, pageable)
        .map(HashSet::new)
        .map(communityApiMapper::communityHouseSetToRestApiResponseCommunityHouseSet)
        .map(houses -> new GetHouseDetailsResponse().houses(houses))
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * Adds community administrators to a community with the specified ID. It takes a
   * community ID and a list of administrators as input, updates the community's
   * administrators accordingly, and returns a response with the updated administrators.
   *
   * @param communityId identifier of the community to which administrators are to be
   * added.
   *
   * @param request request body containing the list of admins to be added to the
   * specified community.
   *
   * Contain a collection of admins.
   *
   * @returns either a `ResponseEntity` with a list of admins and a 201 status code,
   * or a 404 status code.
   *
   * The output is a `ResponseEntity` object.
   */
  @Override
  public ResponseEntity<AddCommunityAdminResponse> addCommunityAdmins(
      @PathVariable String communityId, @Valid @RequestBody
      AddCommunityAdminRequest request) {
    log.trace("Received request to add admin to community with id[{}]", communityId);
    Optional<Community> communityOptional =
        communityService.addAdminsToCommunity(communityId, request.getAdmins());
    return communityOptional.map(community -> {
      Set<String> adminsSet = community.getAdmins()
          .stream()
          .map(User::getUserId)
          .collect(Collectors.toSet());
      AddCommunityAdminResponse response = new AddCommunityAdminResponse().admins(adminsSet);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  /**
   * Adds houses to a community based on a request. It maps house names to community
   * houses, adds them to the community using the `communityService`, and returns a
   * response with the added house IDs if successful, or a bad request status otherwise.
   *
   * @param communityId identifier of the community to which houses are being added.
   *
   * @param request AddCommunityHouseRequest object containing the house names to be
   * added to a community.
   *
   * Contain a set of house names.
   *
   * @returns a ResponseEntity containing a list of house IDs in case of a successful
   * addition, or a 400 status code otherwise.
   *
   * The output is a `ResponseEntity` object with a `body` of type `AddCommunityHouseResponse`.
   * Its `body` contains a `Set<String>` of house IDs and a `Set<CommunityHouseName>`
   * of house names.
   */
  @Override
  public ResponseEntity<AddCommunityHouseResponse> addCommunityHouses(
      @PathVariable String communityId, @Valid @RequestBody
      AddCommunityHouseRequest request) {
    log.trace("Received request to add house to community with id[{}]", communityId);
    Set<CommunityHouseName> houseNames = request.getHouses();
    Set<CommunityHouse> communityHouses =
        communityApiMapper.communityHouseNamesSetToCommunityHouseSet(houseNames);
    Set<String> houseIds = communityService.addHousesToCommunity(communityId, communityHouses);
    if (houseIds.size() != 0 && houseNames.size() != 0) {
      AddCommunityHouseResponse response = new AddCommunityHouseResponse();
      response.setHouses(houseIds);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  /**
   * Removes a house from a community based on the provided community and house IDs.
   * It returns a 204 status code if the removal is successful, or a 404 status code
   * if the house or community is not found.
   *
   * @param communityId identifier of the community from which a house is to be removed.
   *
   * @param houseId identifier of the house to be removed from a community.
   *
   * @returns either a 204 No Content response if the house is removed, or a 404 Not
   * Found response if it is not.
   *
   * Returned output is a ResponseEntity, which is a container object that holds a
   * response from a server.
   * It is of type Void, indicating that it does not contain any response body.
   */
  @Override
  public ResponseEntity<Void> removeCommunityHouse(
      @PathVariable String communityId, @PathVariable String houseId
  ) {
    log.trace(
        "Received request to delete house with id[{}] from community with id[{}]",
        houseId, communityId);

    Optional<Community> communityOptional = communityService.getCommunityDetailsById(communityId);

    return communityOptional.filter(
        community -> communityService.removeHouseFromCommunityByHouseId(community, houseId))
        .map(removed -> ResponseEntity.noContent().<Void>build())
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * Removes an admin from a community based on the provided community and admin IDs,
   * then returns a ResponseEntity indicating success or failure.
   *
   * @param communityId identifier of the community from which an admin is to be removed.
   *
   * @param adminId identifier of the admin to be removed from the specified community.
   *
   * @returns either a NO_CONTENT (204) response or a NOT_FOUND (404) response.
   *
   * It is a ResponseEntity of type Void, indicating the function returns no data.
   */
  @Override
  public ResponseEntity<Void> removeAdminFromCommunity(
      @PathVariable String communityId, @PathVariable String adminId) {
    log.trace(
        "Received request to delete an admin from community with community id[{}] and admin id[{}]",
        communityId, adminId);
    boolean adminRemoved = communityService.removeAdminFromCommunity(communityId, adminId);
    if (adminRemoved) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Handles a request to delete a community by communityId. It checks if the deletion
   * is successful, returning a 204 status if deleted or a 404 status if the community
   * is not found.
   *
   * @param communityId identifier of the community to be deleted, which is passed to
   * the `communityService` for deletion.
   *
   * @returns either a NO_CONTENT response (204) or a NOT_FOUND response (404).
   */
  @Override
  public ResponseEntity<Void> deleteCommunity(@PathVariable String communityId) {
    log.trace("Received delete community request");
    boolean isDeleted = communityService.deleteCommunity(communityId);
    if (isDeleted) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
