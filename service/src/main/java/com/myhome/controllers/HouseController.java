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
 * Handles RESTful API endpoints for managing houses, including listing all houses,
 * retrieving house details, listing house members, adding house members, and deleting
 * house members. It interacts with a HouseService and uses mappers to transform data
 * between domain and API response objects.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class HouseController implements HousesApi {
  private final HouseMemberMapper houseMemberMapper;
  private final HouseService houseService;
  private final HouseApiMapper houseApiMapper;

  /**
   * Handles a request to retrieve a list of all houses, returning a response with up
   * to 200 items per page. It uses a service to retrieve the house details and maps
   * them to a REST API response.
   *
   * @param pageable pagination criteria for the list of houses, enabling the retrieval
   * of a specified number of items at a time.
   *
   * Destructure: Pageable pageable = new Pageable();
   *
   * The main properties of pageable are:
   * - Pageable has a Page property that contains information about the page of results.
   * - The Page object has properties:
   *   - Content: the actual results
   *   - Number: The page number
   *   - PageSize: The size of the page
   *   - TotalPages: The total number of pages
   *   - TotalElements: The total number of elements
   *
   * @returns a ResponseEntity containing a GetHouseDetailsResponse object with a set
   * of CommunityHouse data.
   *
   * The output is a ResponseEntity object with a GetHouseDetailsResponse body. The
   * GetHouseDetailsResponse body contains a set of GetHouseDetailsResponseCommunityHouse
   * objects.
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
   * Fetches house details by ID, maps the result to a REST API response, and returns
   * it in a ResponseEntity. If the result is not found, it returns a 404 response.
   *
   * @param houseId identifier of the house for which details are being retrieved.
   *
   * @returns A `ResponseEntity` containing a `GetHouseDetailsResponse` object with a
   * list of community houses.
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
   * Retrieves a list of house members based on a provided house ID, paginates the
   * results and maps them to a REST API response, returning a successful response if
   * found or a not found response if not.
   *
   * @param houseId identifier of the house for which all members are to be listed.
   *
   * @param pageable pagination settings for the response, allowing the function to
   * return a paginated list of house members.
   *
   * Destructure:
   * - `pageable` has a `size` property, which is set to 200 by default.
   * - It also has `pageNumber` and `pageSize` properties, but they are not explicitly
   * specified in the code snippet.
   *
   * @returns a ResponseEntity containing a ListHouseMembersResponse with a set of house
   * members.
   *
   * The output is a `ResponseEntity` containing a `ListHouseMembersResponse` object.
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
   * Adds members to a house by converting the request to a set of house members, saving
   * them, and returning a successful response if the operation is successful, or a not
   * found response if the house does not exist.
   *
   * @param houseId identifier for the house to which new members are being added.
   *
   * @param request AddHouseMemberRequest object containing the members to be added to
   * the house, retrieved via the `getMembers()` method.
   *
   * Contain members of type Set<HouseMemberDto>.
   *
   * @returns a ResponseEntity containing an AddHouseMemberResponse object with the
   * added members.
   *
   * The returned output is a `ResponseEntity` object, specifically a
   * `ResponseEntity<AddHouseMemberResponse>`. This object has a status code (either
   * `HttpStatus.NOT_FOUND` or `HttpStatus.CREATED`) and a body containing an
   * `AddHouseMemberResponse` object.
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
   * Deletes a member from a house based on the provided house and member IDs. It logs
   * the deletion request and checks the result of the deletion operation.
   *
   * @param houseId identifier of the house from which a member is to be deleted.
   *
   * @param memberId identifier of the member to be deleted from the specified house.
   *
   * @returns a ResponseEntity with a status code of either NO_CONTENT (204) or NOT_FOUND
   * (404).
   *
   * The returned `ResponseEntity` is of type `Void`, indicating it does not carry a
   * response body.
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