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
 * Handles RESTful API endpoints for managing communities, including creating, listing,
 * and deleting communities, as well as managing community administrators and houses.
 */
@RequiredArgsConstructor
@RestController
@Slf4j
public class CommunityController implements CommunitiesApi {
  private final CommunityService communityService;
  private final CommunityApiMapper communityApiMapper;

  /**
   * Handles a community creation request by mapping the request to a community DTO,
   * creating a community using the community service, mapping the created community
   * to a response, and returning a created response entity with the community details.
   *
   * @param request CreateCommunityRequest object that contains the data required to
   * create a new community.
   *
   * Deconstruct `request` into its main properties:
   *
   * - `@Valid` indicates validation is applied to the request.
   * - `@RequestBody` indicates the request body is expected to be the input.
   * - `CreateCommunityRequest` is the class of the input request.
   *
   * @returns a `CreateCommunityResponse` object with status `HttpStatus.CREATED`.
   *
   * The returned output is a `ResponseEntity` object with a `HttpStatus.CREATED` status
   * code and a `CreateCommunityResponse` object in its body. The `CreateCommunityResponse`
   * object contains information about the newly created community.
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
   * Handles a request to retrieve a list of all communities,
   * maps community details to a response API,
   * and returns the response with a 200 status code.
   *
   * @param pageable pagination criteria for retrieving a subset of data, allowing for
   * features such as page size and sorting.
   *
   * Extract the properties of `pageable`:
   * - `pageable` is a `Pageable` object.
   * - It has a `size` property of 200, which represents the number of items per page.
   *
   * @returns a collection of community details wrapped in a `GetCommunityDetailsResponse`
   * object.
   *
   * Include a `GetCommunityDetailsResponse` object, containing a set of
   * `GetCommunityDetailsResponseCommunity` objects, each representing a community with
   * its details.
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
   * Returns a list of community details in response to a request with a specified
   * community ID. It retrieves the details from the `communityService`, maps them to
   * a REST API response, and returns a ResponseEntity with a 200 status code if the
   * details are found, or a 404 status code if not.
   *
   * @param communityId identifier of the community for which details are requested.
   *
   * @returns a list of community details wrapped in a ResponseEntity with HTTP status
   * code 200 or 404.
   *
   * Contain a ResponseEntity object.
   * The ResponseEntity object contains a GetCommunityDetailsResponse object.
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
   * Handles a request to retrieve community administrators. It takes a community ID
   * and pagination settings, retrieves the corresponding administrators, maps the
   * result to a REST API response, and returns it as a `ResponseEntity` with a 200
   * status code if found, or a 404 status code if not.
   *
   * @param communityId identifier of the community for which to retrieve all admins.
   *
   * @param pageable pagination settings for the response, allowing for the retrieval
   * of a specified number of results at a time.
   *
   * Extract its `size` property, which specifies the number of records to return in a
   * single response.
   *
   * @returns a ResponseEntity containing a ListCommunityAdminsResponse object with a
   * set of community admins.
   *
   * The output is a `ResponseEntity` containing a `ListCommunityAdminsResponse` object,
   * which has a single property `admins` of type `Set<CommunityAdmin>` mapped from a
   * `Set<CommunityAdminSet>`.
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
   * Receives a community ID and a pageable request,
   * finds houses for the community,
   * and returns a response with the houses as a set.
   *
   * @param communityId identifier for a community for which all associated houses are
   * to be listed.
   *
   * @param pageable pagination settings for the response data, allowing it to be split
   * into multiple pages with a specified size of 200 items per page.
   *
   * Destructure:
   * - `pageable` is an object that contains information about the page being requested.
   * - `pageable` has properties such as `pageNumber`, `pageSize`, and `sort` that can
   * be used to customize the data being retrieved.
   *
   * Main properties:
   * - `pageNumber`: The number of the page being requested.
   * - `pageSize`: The number of items per page.
   *
   * @returns a ResponseEntity containing a GetHouseDetailsResponse with a set of
   * community houses.
   *
   * The output is a `ResponseEntity` containing a `GetHouseDetailsResponse` object.
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
   * Adds administrators to a community based on the provided community ID and a list
   * of administrators. It returns a response with the updated list of community
   * administrators or a 404 status if the community is not found.
   *
   * @param communityId identifier of the community to which administrators are being
   * added.
   *
   * @param request AddCommunityAdminRequest object containing the IDs of admins to be
   * added to the community.
   *
   * Contain admins.
   *
   * @returns a ResponseEntity containing either an AddCommunityAdminResponse with added
   * admins or a 404 response.
   *
   * The returned output is a `ResponseEntity` object with a `AddCommunityAdminResponse`
   * body, which contains a set of user IDs.
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
   * Adds houses to a community, mapping a set of house names to a set of community
   * houses using `communityApiMapper`, and returns a response with the added house IDs
   * if successful, otherwise returns a bad request response.
   *
   * @param communityId identifier of a community to which new houses are being added.
   *
   * @param request AddCommunityHouseRequest object containing the community house names
   * to be added to the community.
   *
   * Extract the main properties of `request` into separate sentences.
   *
   *   1/ `request` is of type `AddCommunityHouseRequest`.
   *   2/ It contains a set of `houseNames` of type `CommunityHouseName`.
   *
   * @returns a ResponseEntity containing an AddCommunityHouseResponse with a list of
   * house IDs.
   *
   * The returned output is a `ResponseEntity` object with a `200 OK` status code,
   * containing an `AddCommunityHouseResponse` object with a `Set<String>` of house IDs.
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
   * Deletes a house from a community based on the provided community and house IDs.
   * It checks if the community exists and if the house can be removed from it, returning
   * a 204 response if successful or a 404 response if not found.
   *
   * @param communityId identifier of the community from which a house is to be removed.
   *
   * @param houseId identifier of the house to be removed from a community.
   *
   * @returns Either a 204 No Content response or a 404 Not Found response.
   *
   * The returned output is a `ResponseEntity` object of type `Void`. It has a status
   * code that can be either 204 (No Content) or 404 (Not Found).
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
   * Removes an admin from a community based on the provided community and admin IDs.
   * It logs the request, calls the `communityService` to perform the removal, and
   * returns a NO_CONTENT response if successful or a NOT_FOUND response if the admin
   * is not found.
   *
   * @param communityId identifier of the community from which an admin is being removed.
   *
   * @param adminId identifier of the admin to be removed from the specified community.
   *
   * @returns either a 204 (NO_CONTENT) response indicating successful removal or a 404
   * (NOT_FOUND) response indicating failure.
   *
   * The returned output is a ResponseEntity, a type of HTTP response. It is of type
   * Void, indicating no data is returned.
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
   * Handles a delete community request by calling the `communityService` to delete the
   * community with the provided `communityId`. It returns a `NO_CONTENT` response if
   * successful and a `NOT_FOUND` response if the community does not exist.
   *
   * @param communityId identifier of the community to be deleted.
   *
   * @returns either a NO_CONTENT response (200) or a NOT_FOUND response (404).
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
