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
import com.myhome.controllers.dto.CommunityDto;
import com.myhome.controllers.dto.mapper.CommunityMapper;
import com.myhome.domain.Community;
import com.myhome.domain.CommunityHouse;
import com.myhome.domain.HouseMember;
import com.myhome.domain.User;
import com.myhome.repositories.CommunityHouseRepository;
import com.myhome.repositories.CommunityRepository;
import com.myhome.repositories.UserRepository;
import com.myhome.services.HouseService;
import com.myhome.services.springdatajpa.CommunitySDJpaService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Provides a
 */
public class CommunitySDJpaServiceTest {

  private final String TEST_COMMUNITY_ID = "test-community-id";
  private final String TEST_COMMUNITY_NAME = "test-community-name";
  private final String TEST_COMMUNITY_DISTRICT = "test-community-name";

  private final int TEST_ADMINS_COUNT = 2;
  private final int TEST_HOUSES_COUNT = 2;
  private final int TEST_HOUSE_MEMBERS_COUNT = 2;
  private final int TEST_COMMUNITIES_COUNT = 2;

  private final String TEST_ADMIN_ID = "test-admin-id";
  private final String TEST_ADMIN_NAME = "test-user-name";
  private final String TEST_ADMIN_EMAIL = "test-user-email";
  private final String TEST_ADMIN_PASSWORD = "test-user-password";
  private final String TEST_HOUSE_ID = "test-house-id";

  @Mock
  private CommunityRepository communityRepository;
  @Mock
  private UserRepository communityAdminRepository;
  @Mock
  private CommunityMapper communityMapper;
  @Mock
  private CommunityHouseRepository communityHouseRepository;
  @Mock
  private HouseService houseService;

  @InjectMocks
  private CommunitySDJpaService communitySDJpaService;

  /**
   * Initializes Mockito annotations for the test class. It uses `MockitoAnnotations.initMocks(this)`
   * to enable mocking. This is typically done before each test to ensure mock objects
   * are properly set up.
   */
  @BeforeEach
  private void init() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Creates a new `User` object with predefined attributes, including name, ID, email,
   * and password, and returns it. The user is not activated and has empty sets for
   * roles and permissions.
   *
   * @returns a User object with specified properties.
   */
  private User getTestAdmin() {
    return new User(
        TEST_ADMIN_NAME,
        TEST_ADMIN_ID,
        TEST_ADMIN_EMAIL,
        false,
        TEST_ADMIN_PASSWORD,
        new HashSet<>(),
        new HashSet<>());
  }

  /**
   * Tests the `communitySDJpaService` to retrieve all communities from the database.
   * It uses Mockito to mock the `communityRepository` and returns a set of test
   * communities. The function asserts that the retrieved communities match the expected
   * set and verifies that the `findAll` method was called on the repository.
   */
  @Test
  void listAllCommunities() {
    // given
    Set<Community> communities = TestUtils.CommunityHelpers.getTestCommunities(TEST_COMMUNITIES_COUNT);
    given(communityRepository.findAll())
        .willReturn(communities);

    // when
    Set<Community> resultCommunities = communitySDJpaService.listAll();

    // then
    assertEquals(communities, resultCommunities);
    verify(communityRepository).findAll();
  }

  /**
   * Simulates the creation of a community by a system administrator. It verifies that
   * a community is successfully created with the expected name and district from a
   * provided `CommunityDto`.
   */
  @Test
  void createCommunity() {
    // given
    CommunityDto testCommunityDto = getTestCommunityDto();
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity(TEST_COMMUNITY_ID, TEST_COMMUNITY_NAME, TEST_COMMUNITY_DISTRICT, 0, 0);
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(TEST_ADMIN_ID,
            null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    given(communityMapper.communityDtoToCommunity(testCommunityDto))
        .willReturn(testCommunity);
    given(communityAdminRepository.findByUserIdWithCommunities(TEST_ADMIN_ID))
            .willReturn(Optional.of(getTestAdmin()));
    given(communityRepository.save(testCommunity))
        .willReturn(testCommunity);

    // when
    Community createdCommunity = communitySDJpaService.createCommunity(testCommunityDto);

    // then
    assertNotNull(createdCommunity);
    assertEquals(testCommunityDto.getName(), createdCommunity.getName());
    assertEquals(testCommunityDto.getDistrict(), createdCommunity.getDistrict());
    verify(communityMapper).communityDtoToCommunity(testCommunityDto);
    verify(communityAdminRepository).findByUserIdWithCommunities(TEST_ADMIN_ID);
    verify(communityRepository).save(testCommunity);
  }

  /**
   * Retrieves a list of community houses associated with a given community ID. It uses
   * a service, `communitySDJpaService`, to interact with repositories, which in turn
   * query the database for the required information.
   */
  @Test
  void findCommunityHousesById() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();
    List<CommunityHouse> testCommunityHouses = new ArrayList<>(testCommunity.getHouses());
    given(communityRepository.existsByCommunityId(TEST_COMMUNITY_ID))
        .willReturn(true);
    given(communityHouseRepository.findAllByCommunity_CommunityId(TEST_COMMUNITY_ID, null))
        .willReturn(testCommunityHouses);

    // when
    Optional<List<CommunityHouse>> resultCommunityHousesOptional =
        communitySDJpaService.findCommunityHousesById(TEST_COMMUNITY_ID, null);

    // then
    assertTrue(resultCommunityHousesOptional.isPresent());
    List<CommunityHouse> resultCommunityHouses = resultCommunityHousesOptional.get();
    assertEquals(testCommunityHouses, resultCommunityHouses);
    verify(communityRepository).existsByCommunityId(TEST_COMMUNITY_ID);
    verify(communityHouseRepository).findAllByCommunity_CommunityId(TEST_COMMUNITY_ID, null);
  }

  /**
   * Tests the behavior of the `communitySDJpaService` when a community with the specified
   * ID does not exist. It verifies that the service returns an empty optional and does
   * not call the `findAllByCommunity_CommunityId` method.
   */
  @Test
  void findCommunityHousesByIdNotExist() {
    // given
    given(communityRepository.existsByCommunityId(TEST_COMMUNITY_ID))
        .willReturn(false);

    // when
    Optional<List<CommunityHouse>> resultCommunityHousesOptional =
        communitySDJpaService.findCommunityHousesById(TEST_COMMUNITY_ID, null);

    // then
    assertFalse(resultCommunityHousesOptional.isPresent());
    verify(communityRepository).existsByCommunityId(TEST_COMMUNITY_ID);
    verify(communityHouseRepository, never()).findAllByCommunity_CommunityId(TEST_COMMUNITY_ID,
        null);
  }

  /**
   * Retrieves a list of community administrators by community ID from the database
   * using the `communityAdminRepository` and validates the result against a test
   * community's administrators.
   */
  @Test
  void findCommunityAdminsById() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();
    List<User> testCommunityAdmins = new ArrayList<>(testCommunity.getAdmins());
    given(communityRepository.existsByCommunityId(TEST_COMMUNITY_ID))
        .willReturn(true);
    given(communityAdminRepository.findAllByCommunities_CommunityId(TEST_COMMUNITY_ID, null))
        .willReturn(testCommunityAdmins);

    // when
    Optional<List<User>> resultAdminsOptional =
        communitySDJpaService.findCommunityAdminsById(TEST_COMMUNITY_ID, null);

    // then
    assertTrue((resultAdminsOptional.isPresent()));
    List<User> resultAdmins = resultAdminsOptional.get();
    assertEquals(testCommunityAdmins, resultAdmins);
    verify(communityRepository).existsByCommunityId(TEST_COMMUNITY_ID);
    verify(communityAdminRepository).findAllByCommunities_CommunityId(TEST_COMMUNITY_ID, null);
  }

  /**
   * Tests the retrieval of community administrators by ID when the community does not
   * exist. It verifies that no administrators are found and that the community
   * repository's `existsByCommunityId` method is called.
   */
  @Test
  void findCommunityAdminsByIdNotExists() {
    // given
    given(communityRepository.existsByCommunityId(TEST_COMMUNITY_ID))
        .willReturn(false);

    // when
    Optional<List<User>> resultAdminsOptional =
        communitySDJpaService.findCommunityAdminsById(TEST_COMMUNITY_ID, null);

    // then
    assertFalse((resultAdminsOptional.isPresent()));
    verify(communityRepository).existsByCommunityId(TEST_COMMUNITY_ID);
  }

  /**
   * Adds administrators to a community by their IDs, updates the community's admin
   * list, and associates the administrators with the community.
   */
  @Test
  void addAdminsToCommunity() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();
    Set<User> adminToAdd = TestUtils.UserHelpers.getTestUsers(TEST_ADMINS_COUNT);
    Set<String> adminToAddIds = adminToAdd.stream()
        .map(admin -> admin.getUserId())
        .collect(Collectors.toSet());

    given(communityRepository.findByCommunityIdWithAdmins(TEST_COMMUNITY_ID))
        .willReturn(Optional.of(testCommunity));
    given(communityRepository.save(testCommunity))
        .willReturn(testCommunity);
    adminToAdd.forEach(admin -> {
      given(communityAdminRepository.findByUserIdWithCommunities(admin.getUserId()))
          .willReturn(Optional.of(admin));
    });
    adminToAdd.forEach(admin -> {
      given(communityAdminRepository.save(admin))
          .willReturn(admin);
    });
    // when
    Optional<Community> updatedCommunityOptional =
        communitySDJpaService.addAdminsToCommunity(TEST_COMMUNITY_ID, adminToAddIds);

    // then
    assertTrue(updatedCommunityOptional.isPresent());
    adminToAdd.forEach(admin -> assertTrue(admin.getCommunities().contains(testCommunity)));
    verify(communityRepository).findByCommunityIdWithAdmins(TEST_COMMUNITY_ID);
    adminToAdd.forEach(
        admin -> verify(communityAdminRepository).findByUserIdWithCommunities(admin.getUserId()));
  }

  /**
   * Tests the behavior when adding admins to a non-existent community. It checks that
   * the community repository is called to retrieve the community, and that no community
   * is updated when the community does not exist.
   */
  @Test
  void addAdminsToCommunityNotExist() {
    // given
    given(communityRepository.findByCommunityIdWithAdmins(TEST_COMMUNITY_ID))
        .willReturn(Optional.empty());

    // when
    Optional<Community> updatedCommunityOptional =
        communitySDJpaService.addAdminsToCommunity(TEST_COMMUNITY_ID, any());

    // then
    assertFalse(updatedCommunityOptional.isPresent());
    verify(communityRepository).findByCommunityIdWithAdmins(TEST_COMMUNITY_ID);
  }

  /**
   * Tests the retrieval of community details by ID using the `communitySDJpaService`.
   * It verifies that the service returns the expected community details when provided
   * with a valid ID, utilizing a mock repository to simulate database interactions.
   */
  @Test
  void communityDetailsById() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();
    given(communityRepository.findByCommunityId(TEST_COMMUNITY_ID))
        .willReturn(Optional.of(testCommunity));

    // when
    Optional<Community> communityOptional =
        communitySDJpaService.getCommunityDetailsById(TEST_COMMUNITY_ID);

    // then
    assertTrue(communityOptional.isPresent());
    assertEquals(testCommunity, communityOptional.get());
    verify(communityRepository).findByCommunityId(TEST_COMMUNITY_ID);
  }

  /**
   * Retrieves community details along with its administrators by community ID, utilizing
   * a repository to fetch the data and returns it as an optional value.
   */
  @Test
  void communityDetailsByIdWithAdmins() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();
    given(communityRepository.findByCommunityIdWithAdmins(TEST_COMMUNITY_ID))
        .willReturn(Optional.of(testCommunity));

    // when
    Optional<Community> communityOptional =
        communitySDJpaService.getCommunityDetailsByIdWithAdmins(TEST_COMMUNITY_ID);

    // then
    assertTrue(communityOptional.isPresent());
    assertEquals(testCommunity, communityOptional.get());
    verify(communityRepository).findByCommunityIdWithAdmins(TEST_COMMUNITY_ID);
  }

  /**
   * Adds houses to a community by their IDs, updates the community's houses, and returns
   * the added houses' IDs. It uses repositories to interact with the database.
   */
  @Test
  void addHousesToCommunity() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();
    Set<CommunityHouse> housesToAdd = TestUtils.CommunityHouseHelpers.getTestHouses(TEST_HOUSES_COUNT);

    given(communityRepository.findByCommunityIdWithHouses(TEST_COMMUNITY_ID))
        .willReturn(Optional.of(testCommunity));
    given(communityRepository.save(testCommunity))
        .willReturn(testCommunity);
    housesToAdd.forEach(house -> {
      given(communityHouseRepository.save(house))
          .willReturn(house);
    });

    // when
    Set<String> addedHousesIds =
        communitySDJpaService.addHousesToCommunity(TEST_COMMUNITY_ID, housesToAdd);

    // then
    assertEquals(housesToAdd.size(), addedHousesIds.size());
    housesToAdd.forEach(house -> {
      assertEquals(house.getCommunity(), testCommunity);
    });
    verify(communityRepository).findByCommunityIdWithHouses(TEST_COMMUNITY_ID);
    housesToAdd.forEach(house -> {
      verify(communityHouseRepository).save(house);
    });
  }

  /**
   * Tests the behavior of adding houses to a non-existent community. It verifies that
   * no houses are added when the community does not exist, and that the repositories
   * are called correctly.
   */
  @Test
  void addHousesToCommunityNotExist() {
    // given
    Set<CommunityHouse> housesToAdd = TestUtils.CommunityHouseHelpers.getTestHouses(TEST_HOUSES_COUNT);

    given(communityRepository.findByCommunityIdWithHouses(TEST_COMMUNITY_ID))
        .willReturn(Optional.empty());

    // when
    Set<String> addedHousesIds =
        communitySDJpaService.addHousesToCommunity(TEST_COMMUNITY_ID, housesToAdd);

    // then
    assertTrue(addedHousesIds.isEmpty());
    verify(communityRepository).findByCommunityIdWithHouses(TEST_COMMUNITY_ID);
    verify(communityRepository, never()).save(any());
    verify(communityHouseRepository, never()).save(any());
  }

  /**
   * Tests the functionality of adding houses to a community that already exists. It
   * verifies that no new houses are added to the community, which implies that the
   * houses are already existing in the community database.
   */
  @Test
  void addHousesToCommunityHouseExists() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();
    Set<CommunityHouse> houses = TestUtils.CommunityHouseHelpers.getTestHouses(TEST_HOUSES_COUNT);
    testCommunity.setHouses(houses);

    given(communityRepository.findByCommunityIdWithHouses(TEST_COMMUNITY_ID))
        .willReturn(Optional.of(testCommunity));
    given(communityRepository.save(testCommunity))
        .willReturn(testCommunity);
    houses.forEach(house -> given(communityHouseRepository.save(house)).willReturn(house));

    // when
    Set<String> addedHousesIds =
        communitySDJpaService.addHousesToCommunity(TEST_COMMUNITY_ID, houses);

    // then
    assertTrue(addedHousesIds.isEmpty());
    verify(communityRepository).findByCommunityIdWithHouses(TEST_COMMUNITY_ID);
    verify(communityRepository).save(testCommunity);
    verify(communityHouseRepository, never()).save(any());
  }

  /**
   * Removes an admin from a community, modifies the community repository accordingly,
   * and returns a boolean indicating whether the admin was successfully removed.
   */
  @Test
  void removeAdminFromCommunity() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();
    User testAdmin = getTestAdmin();
    testCommunity.getAdmins().add(testAdmin);

    given(communityRepository.findByCommunityIdWithAdmins(TEST_COMMUNITY_ID))
        .willReturn(Optional.of(testCommunity));
    given(communityRepository.save(testCommunity))
        .willReturn(testCommunity);

    // when
    boolean adminRemoved =
        communitySDJpaService.removeAdminFromCommunity(TEST_COMMUNITY_ID, TEST_ADMIN_ID);

    // then
    assertTrue(adminRemoved);
    verify(communityRepository).findByCommunityIdWithAdmins(TEST_COMMUNITY_ID);
    verify(communityRepository).save(testCommunity);
  }

  /**
   * Tests the removal of an admin from a non-existent community. It checks if the
   * function correctly returns false when the community does not exist and does not
   * attempt to save the community.
   */
  @Test
  void removeAdminFromCommunityNotExists() {
    // given
    given(communityRepository.findByCommunityIdWithAdmins(TEST_COMMUNITY_ID))
        .willReturn(Optional.empty());

    // when
    boolean adminRemoved =
        communitySDJpaService.removeAdminFromCommunity(TEST_COMMUNITY_ID, TEST_ADMIN_ID);

    // then
    assertFalse(adminRemoved);
    verify(communityRepository).findByCommunityIdWithAdmins(TEST_COMMUNITY_ID);
    verify(communityRepository, never()).save(any());
  }

  /**
   * Tests the removal of an admin from a community when the admin does not exist. It
   * checks that the community service returns false, indicating no admin was removed,
   * and that the community repository was queried but no changes were saved.
   */
  @Test
  void removeAdminFromCommunityAdminNotExists() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();

    given(communityRepository.findByCommunityIdWithAdmins(TEST_COMMUNITY_ID))
        .willReturn(Optional.of(testCommunity));
    given(communityRepository.save(testCommunity))
        .willReturn(testCommunity);

    // when
    boolean adminRemoved =
        communitySDJpaService.removeAdminFromCommunity(TEST_COMMUNITY_ID, TEST_ADMIN_ID);

    // then
    assertFalse(adminRemoved);
    verify(communityRepository).findByCommunityIdWithAdmins(TEST_COMMUNITY_ID);
    verify(communityRepository, never()).save(testCommunity);
  }

  /**
   * Tests the deletion of a community with associated houses from a database. It uses
   * mock repositories to simulate the retrieval and deletion of the community and its
   * houses. The test verifies that the community is successfully deleted and the
   * associated repository methods are called.
   */
  @Test
  void deleteCommunity() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();
    Set<CommunityHouse> testCommunityHouses = TestUtils.CommunityHouseHelpers.getTestHouses(TEST_HOUSES_COUNT);
    testCommunity.setHouses(testCommunityHouses);

    given(communityRepository.findByCommunityIdWithHouses(TEST_COMMUNITY_ID))
        .willReturn(Optional.of(testCommunity));
    testCommunityHouses.forEach(house -> {
      given(communityHouseRepository.findByHouseId(house.getHouseId()))
          .willReturn(Optional.of(house));
    });

    testCommunityHouses.forEach(house -> {
      given(communityHouseRepository.findByHouseId(house.getHouseId()))
          .willReturn(Optional.of(house));
    });

    // when
    boolean communityDeleted = communitySDJpaService.deleteCommunity(TEST_COMMUNITY_ID);

    // then
    assertTrue(communityDeleted);
    verify(communityRepository).findByCommunityIdWithHouses(TEST_COMMUNITY_ID);
    verify(communityRepository).delete(testCommunity);
  }

  /**
   * Tests the deletion of a non-existent community using a service that interacts with
   * repositories. It checks if the service returns false for deletion and verifies
   * that the repositories are not called unnecessarily.
   */
  @Test
  void deleteCommunityNotExists() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();

    given(communityRepository.findByCommunityIdWithHouses(TEST_COMMUNITY_ID))
        .willReturn(Optional.empty());

    // when
    boolean communityDeleted = communitySDJpaService.deleteCommunity(TEST_COMMUNITY_ID);

    // then
    assertFalse(communityDeleted);
    verify(communityRepository).findByCommunityIdWithHouses(TEST_COMMUNITY_ID);
    verify(communityHouseRepository, never()).deleteByHouseId(any());
    verify(communityRepository, never()).delete(testCommunity);
  }

  /**
   * Removes a house from a community based on the house ID, deletes its members, and
   * saves the updated community.
   */
  @Test
  void removeHouseFromCommunityByHouseId() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();
    CommunityHouse testHouse = TestUtils.CommunityHouseHelpers.getTestCommunityHouse(TEST_HOUSE_ID);
    Set<HouseMember> testHouseMembers = TestUtils.HouseMemberHelpers.getTestHouseMembers(TEST_HOUSE_MEMBERS_COUNT);
    testHouse.setHouseMembers(testHouseMembers);
    testCommunity.getHouses().add(testHouse);

    given(communityRepository.findByCommunityIdWithHouses(TEST_COMMUNITY_ID))
        .willReturn(Optional.of(testCommunity));
    given(communityHouseRepository.findByHouseIdWithHouseMembers(TEST_HOUSE_ID))
        .willReturn(Optional.of(testHouse));

    // when
    boolean houseDeleted =
        communitySDJpaService.removeHouseFromCommunityByHouseId(testCommunity, TEST_HOUSE_ID);

    // then
    assertTrue(houseDeleted);
    assertFalse(testCommunity.getHouses().contains(testHouse));
    verify(communityRepository).save(testCommunity);
    testHouse.getHouseMembers()
        .forEach(houseMember -> verify(houseService).deleteMemberFromHouse(TEST_HOUSE_ID,
            houseMember.getMemberId()));
    verify(communityHouseRepository).findByHouseIdWithHouseMembers(TEST_HOUSE_ID);
    verify(communityHouseRepository).deleteByHouseId(TEST_HOUSE_ID);
  }

  /**
   * Tests the removal of a house from a community when the community does not exist.
   * It verifies that the house is not deleted and that certain repository and service
   * interactions are not made.
   */
  @Test
  void removeHouseFromCommunityByHouseIdCommunityNotExists() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();

    given(communityRepository.findByCommunityIdWithHouses(TEST_COMMUNITY_ID))
        .willReturn(Optional.empty());

    // when
    boolean houseDeleted =
        communitySDJpaService.removeHouseFromCommunityByHouseId(null, TEST_HOUSE_ID);

    // then
    assertFalse(houseDeleted);
    verify(communityHouseRepository, never()).findByHouseId(TEST_HOUSE_ID);
    verifyNoInteractions(houseService);
    verify(communityRepository, never()).save(testCommunity);
  }

  /**
   * Tests the removal of a non-existent house from a community. It simulates a repository
   * that returns an empty optional for a given house ID and verifies that the community
   * is not saved, interactions with the house service are skipped, and the repository
   * method is called as expected.
   */
  @Test
  void removeHouseFromCommunityByHouseIdHouseNotExists() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();

    given(communityHouseRepository.findByHouseIdWithHouseMembers(TEST_HOUSE_ID))
        .willReturn(Optional.empty());

    // when
    boolean houseDeleted =
        communitySDJpaService.removeHouseFromCommunityByHouseId(testCommunity, TEST_HOUSE_ID);

    // then
    assertFalse(houseDeleted);
    verify(communityHouseRepository).findByHouseIdWithHouseMembers(TEST_HOUSE_ID);
    verifyNoInteractions(houseService);
    verify(communityRepository, never()).save(testCommunity);
  }

  /**
   * Tests the removal of a house from a community by house ID when the house is not
   * in the community. It verifies that the house deletion is successful, no interactions
   * occur with the house service, and the community repository is not called to save
   * the community.
   */
  @Test
  void removeHouseFromCommunityByHouseIdHouseNotInCommunity() {
    // given
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();

    given(communityHouseRepository.findByHouseIdWithHouseMembers(TEST_HOUSE_ID))
        .willReturn(Optional.empty());

    // when
    boolean houseDeleted =
        communitySDJpaService.removeHouseFromCommunityByHouseId(testCommunity, TEST_HOUSE_ID);

    // then
    assertFalse(houseDeleted);
    verify(communityHouseRepository).findByHouseIdWithHouseMembers(TEST_HOUSE_ID);
    verifyNoInteractions(houseService);
    verify(communityRepository, never()).save(testCommunity);
  }

  /**
   * Creates a CommunityDto object with predefined attributes: communityId, district,
   * and name, and returns it. The attributes are set to fixed values, suggesting a
   * test or sample data purpose.
   *
   * @returns a CommunityDto object populated with predefined community ID, district,
   * and name values.
   */
  private CommunityDto getTestCommunityDto() {
    CommunityDto testCommunityDto = new CommunityDto();
    testCommunityDto.setCommunityId(TEST_COMMUNITY_ID);
    testCommunityDto.setDistrict(TEST_COMMUNITY_DISTRICT);
    testCommunityDto.setName(TEST_COMMUNITY_NAME);
    return testCommunityDto;
  }

}
