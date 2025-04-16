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

package com.myhome.services.unit;

import helpers.TestUtils;
import com.myhome.domain.CommunityHouse;
import com.myhome.domain.HouseMember;
import com.myhome.repositories.CommunityHouseRepository;
import com.myhome.repositories.HouseMemberDocumentRepository;
import com.myhome.repositories.HouseMemberRepository;
import com.myhome.services.springdatajpa.HouseSDJpaService;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Defines unit tests for the HouseSDJpaService class, covering house listing, member
 * addition, and deletion scenarios using Mockito for mocking and Spring Data JPA for
 * database interactions.
 */
class HouseSDJpaServiceTest {

  private final int TEST_HOUSES_COUNT = 10;
  private final int TEST_HOUSE_MEMBERS_COUNT = 10;
  private final String HOUSE_ID = "test-house-id";
  private final String MEMBER_ID = "test-member-id";

  @Mock
  private HouseMemberRepository houseMemberRepository;
  @Mock
  private HouseMemberDocumentRepository houseMemberDocumentRepository;
  @Mock
  private CommunityHouseRepository communityHouseRepository;
  @InjectMocks
  private HouseSDJpaService houseSDJpaService;

  /**
   * Initializes Mockito annotations, setting up the test environment for subsequent
   * tests. This function is typically called before each test to ensure a clean state
   * and avoid cross-talk between tests. It enables the use of Mockito's mocking capabilities.
   */
  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Tests the retrieval of all community houses from a database. It simulates a database
   * query using a mock repository, verifies the result matches the expected houses,
   * and checks the repository was called with the `findAll` method.
   */
  @Test
  void listAllHousesDefault() {
    // given
    Set<CommunityHouse> housesInDatabase = TestUtils.CommunityHouseHelpers.getTestHouses(TEST_HOUSES_COUNT);
    
    given(communityHouseRepository.findAll())
        .willReturn(housesInDatabase);

    // when
    Set<CommunityHouse> resultHouses = houseSDJpaService.listAllHouses();

    // then
    assertEquals(housesInDatabase, resultHouses);
    verify(communityHouseRepository).findAll();
  }

  /**
   * Tests the retrieval of all community houses with custom pageable functionality.
   * It mocks a repository to return a predefined page of houses and verifies that the
   * service layer returns the expected set of houses.
   */
  @Test
  void listAllHousesCustomPageable() {
    // given
    Set<CommunityHouse> housesInDatabase = TestUtils.CommunityHouseHelpers.getTestHouses(TEST_HOUSES_COUNT);
    Pageable pageRequest = PageRequest.of(0, TEST_HOUSES_COUNT);
    Page<CommunityHouse> housesPage = new PageImpl<>(
        new ArrayList<>(housesInDatabase),
        pageRequest,
        TEST_HOUSES_COUNT
    );
    given(communityHouseRepository.findAll(pageRequest))
        .willReturn(housesPage);

    // when
    Set<CommunityHouse> resultHouses = houseSDJpaService.listAllHouses(pageRequest);

    // then
    assertEquals(housesInDatabase, resultHouses);
    verify(communityHouseRepository).findAll(pageRequest);
  }

  /**
   * Adds a specified number of house members to a community house, updates the house
   * members count in the community house, and saves the updated community house and
   * its house members to the database.
   */
  @Test
  void addHouseMembers() {
    // given
    Set<HouseMember> membersToAdd = TestUtils.HouseMemberHelpers.getTestHouseMembers(TEST_HOUSE_MEMBERS_COUNT);
    int membersToAddSize = membersToAdd.size();
    CommunityHouse communityHouse = TestUtils.CommunityHouseHelpers.getTestCommunityHouse();

    given(communityHouseRepository.findByHouseIdWithHouseMembers(HOUSE_ID))
        .willReturn(Optional.of(communityHouse));
    given(houseMemberRepository.saveAll(membersToAdd))
        .willReturn(membersToAdd);

    // when
    Set<HouseMember> resultMembers = houseSDJpaService.addHouseMembers(HOUSE_ID, membersToAdd);

    // then
    assertEquals(membersToAddSize, resultMembers.size());
    assertEquals(membersToAddSize, communityHouse.getHouseMembers().size());
    verify(communityHouseRepository).save(communityHouse);
    verify(houseMemberRepository).saveAll(membersToAdd);
    verify(communityHouseRepository).findByHouseIdWithHouseMembers(HOUSE_ID);
  }

  /**
   * Tests the addition of house members when a house with the specified ID does not
   * exist in the database. It verifies that no members are added and the repository
   * is not saved, indicating a new house is not created.
   */
  @Test
  void addHouseMembersHouseNotExists() {
    // given
    Set<HouseMember> membersToAdd = TestUtils.HouseMemberHelpers.getTestHouseMembers(TEST_HOUSE_MEMBERS_COUNT);

    given(communityHouseRepository.findByHouseIdWithHouseMembers(HOUSE_ID))
        .willReturn(Optional.empty());

    // when
    Set<HouseMember> resultMembers = houseSDJpaService.addHouseMembers(HOUSE_ID, membersToAdd);

    // then
    assertTrue(resultMembers.isEmpty());
    verify(communityHouseRepository).findByHouseIdWithHouseMembers(HOUSE_ID);
    verify(communityHouseRepository, never()).save(any());
    verifyNoInteractions(houseMemberRepository);
  }

  /**
   * Deletes a member from a community house. It takes the house ID and member ID as
   * input, removes the member from the house, and saves the updated house and member
   * to the database.
   */
  @Test
  void deleteMemberFromHouse() {
    // given
    Set<HouseMember> houseMembers = TestUtils.HouseMemberHelpers.getTestHouseMembers(TEST_HOUSE_MEMBERS_COUNT);
    CommunityHouse communityHouse = TestUtils.CommunityHouseHelpers.getTestCommunityHouse();

    HouseMember memberToDelete = new HouseMember().withMemberId(MEMBER_ID);
    memberToDelete.setCommunityHouse(communityHouse);

    houseMembers.add(memberToDelete);
    communityHouse.setHouseMembers(houseMembers);

    given(communityHouseRepository.findByHouseIdWithHouseMembers(HOUSE_ID))
        .willReturn(Optional.of(communityHouse));

    // when
    boolean isMemberDeleted = houseSDJpaService.deleteMemberFromHouse(HOUSE_ID, MEMBER_ID);

    // then
    assertTrue(isMemberDeleted);
    assertNull(memberToDelete.getCommunityHouse());
    assertFalse(communityHouse.getHouseMembers().contains(memberToDelete));
    verify(communityHouseRepository).findByHouseIdWithHouseMembers(HOUSE_ID);
    verify(communityHouseRepository).save(communityHouse);
    verify(houseMemberRepository).save(memberToDelete);
  }

  /**
   * Tests the deletion of a member from a house that does not exist. It verifies that
   * the service returns false, does not save any changes, and does not interact with
   * the house member repository when given a non-existent house.
   */
  @Test
  void deleteMemberFromHouseNotExists() {
    // given
    given(communityHouseRepository.findByHouseIdWithHouseMembers(HOUSE_ID))
        .willReturn(Optional.empty());

    // when
    boolean isMemberDeleted = houseSDJpaService.deleteMemberFromHouse(HOUSE_ID, MEMBER_ID);

    // then
    assertFalse(isMemberDeleted);
    verify(communityHouseRepository).findByHouseIdWithHouseMembers(HOUSE_ID);
    verify(communityHouseRepository, never()).save(any());
    verifyNoInteractions(houseMemberRepository);
  }

  /**
   * Tests the deletion of a member from a community house when the member is not
   * present. It verifies that the member is not deleted and the community house
   * repository is queried but not saved.
   */
  @Test
  void deleteMemberFromHouseMemberNotPresent() {
    // given
    Set<HouseMember> houseMembers = TestUtils.HouseMemberHelpers.getTestHouseMembers(TEST_HOUSE_MEMBERS_COUNT);
    CommunityHouse communityHouse = TestUtils.CommunityHouseHelpers.getTestCommunityHouse();

    communityHouse.setHouseMembers(houseMembers);

    given(communityHouseRepository.findByHouseIdWithHouseMembers(HOUSE_ID))
        .willReturn(Optional.of(communityHouse));

    // when
    boolean isMemberDeleted = houseSDJpaService.deleteMemberFromHouse(HOUSE_ID, MEMBER_ID);

    // then
    assertFalse(isMemberDeleted);
    verify(communityHouseRepository).findByHouseIdWithHouseMembers(HOUSE_ID);
    verify(communityHouseRepository, never()).save(communityHouse);
    verifyNoInteractions(houseMemberRepository);
  }
}