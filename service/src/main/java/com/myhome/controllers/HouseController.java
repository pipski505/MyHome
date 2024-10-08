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

import com.myhome.api.HousesApi;
import com.myhome.controllers.dto.mapper.HouseMemberMapper;
import com.myhome.controllers.mapper.HouseApiMapper;
import com.myhome.domain.CommunityHouse;
import com.myhome.domain.HouseMember;
import com.myhome.model.AddHouseMemberRequest;
import com.myhome.model.AddHouseMemberResponse;
import com.myhome.model.GetHouseDetailsResponse;
import com.myhome.model.GetHouseDetailsResponseCommunityHouse;
import com.myhome.model.ListHouseMembersResponse;
import com.myhome.services.HouseService;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles RESTful API requests for managing houses, including listing all houses,
 * retrieving house details, listing house members, adding house members, and deleting
 * house members.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class HouseController implements HousesApi {
  private final HouseMemberMapper houseMemberMapper;
  private final HouseService houseService;
  private final HouseApiMapper houseApiMapper;

  /**
   * Retrieves a list of community houses based on the provided pageable criteria, maps
   * the results to a GetHouseDetailsResponse object, and returns a ResponseEntity with
   * a status of OK and the response object as the body.
   *
   * @param pageable pagination requirements for the list of houses, allowing the
   * function to return a specified number of results at a time.
   *
   * Destructure pageable into its main properties.
   * - Pageable has a parameter named `sort` to specify the sort order.
   * - It has an `offset` to specify the starting point of the page.
   * - `pageNumber` and `pageSize` are used to calculate the page number and size.
   * - Pageable also has `size` which is used to specify the number of records per page.
   *
   * @returns a ResponseEntity containing a GetHouseDetailsResponse object with a set
   * of community house details.
   *
   * The returned output is a `ResponseEntity` object, containing a `GetHouseDetailsResponse`
   * object in its body. The `GetHouseDetailsResponse` object has a single property,
   * `houses`, which is a set of `GetHouseDetailsResponseCommunityHouse` objects.
   */
  @Override
  public ResponseEntity<GetHouseDetailsResponse> listAllHouses(
      @PageableDefault(size = 200) Pageable pageable) {
    log.trace("Received request to list all houses");

    Set<CommunityHouse> houseDetails =
        houseService.listAllHouses(pageable);
    Set<GetHouseDetailsResponseCommunityHouse> getHouseDetailsResponseSet =
        houseApiMapper.communityHouseSetToRestApiResponseCommunityHouseSet(houseDetails);

    GetHouseDetailsResponse response = new GetHouseDetailsResponse();

    response.setHouses(getHouseDetailsResponseSet);

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Retrieves house details by ID, maps the result to a REST API response, wraps it
   * in a singleton list, and returns it as a ResponseEntity. If no house is found, it
   * returns a ResponseEntity with a 404 status code.
   *
   * @param houseId identifier for the house whose details are being requested.
   *
   * @returns a ResponseEntity containing a GetHouseDetailsResponse object with a list
   * of community houses.
   */
  @Override
  public ResponseEntity<GetHouseDetailsResponse> getHouseDetails(String houseId) {
    log.trace("Received request to get details of a house with id[{}]", houseId);
    return houseService.getHouseDetailsById(houseId)
        .map(houseApiMapper::communityHouseToRestApiResponseCommunityHouse)
        .map(Collections::singleton)
        .map(getHouseDetailsResponseCommunityHouses -> new GetHouseDetailsResponse().houses(getHouseDetailsResponseCommunityHouses))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Retrieves members of a house with the specified ID, maps the result to a REST API
   * response, and returns it as a ResponseEntity. If no members are found, it returns
   * a 404 error response.
   *
   * @param houseId identifier for the house from which all members are to be retrieved.
   *
   * @param pageable pagination settings for retrieving a subset of the total results,
   * with a default page size of 200.
   *
   * Expose its properties:
   * - `pageNumber`: the current page number
   * - `pageSize`: the size of the page
   *
   * @returns a list of house members in the form of a `ListHouseMembersResponse` entity.
   *
   * The output is a `ResponseEntity` containing a `ListHouseMembersResponse`.
   */
  @Override
  public ResponseEntity<ListHouseMembersResponse> listAllMembersOfHouse(
      String houseId,
      @PageableDefault(size = 200) Pageable pageable) {
    log.trace("Received request to list all members of the house with id[{}]", houseId);

    return houseService.getHouseMembersById(houseId, pageable)
        .map(HashSet::new)
        .map(houseMemberMapper::houseMemberSetToRestApiResponseHouseMemberSet)
        .map(houseMembers -> new ListHouseMembersResponse().members(houseMembers))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Adds members to a house, converting a request object to a set of `HouseMember`
   * objects, saving them to the database, and returning a response with the saved
   * members in a specific format if the operation is successful.
   *
   * @param houseId identifier of the house to which new members are being added.
   *
   * @param request AddHouseMemberRequest object containing the members to be added to
   * the specified house.
   *
   * Contain a set of house member DTOs.
   *
   * @returns a `ResponseEntity` with a `CREATED` status code and an `AddHouseMemberResponse`
   * body.
   *
   * The returned output is a `ResponseEntity` object. It contains a `status` attribute,
   * which is an HTTP status code, and a `body` attribute, which is an `AddHouseMemberResponse`
   * object.
   */
  @Override
  public ResponseEntity<AddHouseMemberResponse> addHouseMembers(
      @PathVariable String houseId, @Valid AddHouseMemberRequest request) {

    log.trace("Received request to add member to the house with id[{}]", houseId);
    Set<HouseMember> members =
        houseMemberMapper.houseMemberDtoSetToHouseMemberSet(request.getMembers());
    Set<HouseMember> savedHouseMembers = houseService.addHouseMembers(houseId, members);

    if (savedHouseMembers.size() == 0 && request.getMembers().size() != 0) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } else {
      AddHouseMemberResponse response = new AddHouseMemberResponse();
      response.setMembers(
          houseMemberMapper.houseMemberSetToRestApiResponseAddHouseMemberSet(savedHouseMembers));
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
  }

  /**
   * Processes a request to delete a member from a house, logs the request, calls the
   * `houseService` to delete the member, and returns a `ResponseEntity` based on the
   * result, indicating success or not found.
   *
   * @param houseId identifier for the house from which a member is to be deleted.
   *
   * @param memberId identifier of a house member to be deleted from a house with the
   * specified `houseId`.
   *
   * @returns a ResponseEntity with a status code of 204 (NO_CONTENT) if the member is
   * deleted, or 404 (NOT_FOUND) otherwise.
   *
   * The returned output is a `ResponseEntity` object with a status code of either
   * `NO_CONTENT` or `NOT_FOUND`.
   */
  @Override
  public ResponseEntity<Void> deleteHouseMember(String houseId, String memberId) {
    log.trace("Received request to delete a member from house with house id[{}] and member id[{}]",
        houseId, memberId);
    boolean isMemberDeleted = houseService.deleteMemberFromHouse(houseId, memberId);
    if (isMemberDeleted) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}