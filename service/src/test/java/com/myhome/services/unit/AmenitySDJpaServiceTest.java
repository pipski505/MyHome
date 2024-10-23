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

import com.myhome.controllers.mapper.AmenityApiMapper;
import com.myhome.domain.Amenity;
import com.myhome.domain.Community;
import com.myhome.model.AmenityDto;
import com.myhome.repositories.AmenityRepository;
import com.myhome.repositories.CommunityRepository;
import com.myhome.services.CommunityService;
import com.myhome.services.springdatajpa.AmenitySDJpaService;
import helpers.TestUtils;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * This class is a unit test for the AmenitySDJpaService class, testing its various
 * methods for deleting, listing, adding, and updating amenities.
 */
class AmenitySDJpaServiceTest {

  private static final String TEST_AMENITY_NAME = "test-amenity-name";
  private static final BigDecimal TEST_AMENITY_PRICE = BigDecimal.valueOf(1);
  private final String TEST_AMENITY_ID = "test-amenity-id";
  private final String TEST_AMENITY_DESCRIPTION = "test-amenity-description";
  private final String TEST_COMMUNITY_ID = "test-community-id";
  private final int TEST_AMENITIES_COUNT = 2;
  @Mock
  private AmenityRepository amenityRepository;
  @Mock
  private CommunityRepository communityRepository;
  @Mock
  private CommunityService communityService;
  @Mock
  private AmenityApiMapper amenityApiMapper;

  @InjectMocks
  private AmenitySDJpaService amenitySDJpaService;

  /**
   * Initializes Mockito annotations for the current test class, setting up mock objects
   * for dependency injection.
   */
  @BeforeEach
  private void init() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Tests the deletion of an amenity from the database by a service. It verifies that
   * the amenity is successfully deleted and that the repository is called correctly
   * to find and delete the amenity.
   */
  @Test
  void deleteAmenity() {
    // given
    Amenity testAmenity =
        TestUtils.AmenityHelpers.getTestAmenity(TEST_AMENITY_ID, TEST_AMENITY_DESCRIPTION);

    given(amenityRepository.findByAmenityIdWithCommunity(TEST_AMENITY_ID))
        .willReturn(Optional.of(testAmenity));

    // when
    boolean amenityDeleted = amenitySDJpaService.deleteAmenity(TEST_AMENITY_ID);

    // then
    assertTrue(amenityDeleted);
    verify(amenityRepository).findByAmenityIdWithCommunity(TEST_AMENITY_ID);
    verify(amenityRepository).delete(testAmenity);
  }

  /**
   * Tests the deletion of a non-existent amenity. It checks if the service returns
   * false when trying to delete an amenity that does not exist in the repository. The
   * service is expected to not call the delete method on the repository.
   */
  @Test
  void deleteAmenityNotExists() {
    // given
    given(amenityRepository.findByAmenityIdWithCommunity(TEST_AMENITY_ID))
        .willReturn(Optional.empty());

    // when
    boolean amenityDeleted = amenitySDJpaService.deleteAmenity(TEST_AMENITY_ID);

    // then
    assertFalse(amenityDeleted);
    verify(amenityRepository).findByAmenityIdWithCommunity(TEST_AMENITY_ID);
    verify(amenityRepository, never()).delete(any());
  }

  /**
   * Retrieves a set of amenities for a specified community ID by calling the community
   * repository's `findByCommunityIdWithAmenities` method, and returns the result as a
   * set of amenities.
   */
  @Test
  void listAllAmenities() {
    // given
    Set<Amenity> testAmenities = TestUtils.AmenityHelpers.getTestAmenities(TEST_AMENITIES_COUNT);
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();
    testCommunity.setAmenities(testAmenities);

    given(communityRepository.findByCommunityIdWithAmenities(TEST_COMMUNITY_ID))
        .willReturn(Optional.of(testCommunity));

    // when
    Set<Amenity> resultAmenities = amenitySDJpaService.listAllAmenities(TEST_COMMUNITY_ID);

    // then
    assertEquals(testAmenities, resultAmenities);
    verify(communityRepository).findByCommunityIdWithAmenities(TEST_COMMUNITY_ID);
  }

  /**
   * Tests the behavior of the `amenitySDJpaService` when it is given a community ID
   * that does not have any amenities associated with it. It verifies that an empty set
   * of amenities is returned.
   */
  @Test
  void listAllAmenitiesNotExists() {
    // given
    given(communityRepository.findByCommunityIdWithAmenities(TEST_COMMUNITY_ID))
        .willReturn(Optional.empty());

    // when
    Set<Amenity> resultAmenities = amenitySDJpaService.listAllAmenities(TEST_COMMUNITY_ID);

    // then
    assertEquals(new HashSet<>(), resultAmenities);
    verify(communityRepository).findByCommunityIdWithAmenities(TEST_COMMUNITY_ID);
  }

  /**
   * Tests the functionality of adding an amenity to an existing community. It involves
   * creating a community and an amenity, mapping the amenity to a data transfer object,
   * and then verifying that the amenity is successfully added to the community.
   */
  @Test
  void shouldAddAmenityToExistingCommunity() {
    // given
    final String communityId = "communityId";
    final Community community = new Community().withCommunityId(communityId);
    final AmenityDto baseAmenityDto = new AmenityDto()
        .id(1L)
        .amenityId("amenityId")
        .name("name")
        .description("description")
        .price(BigDecimal.valueOf(12));
    final AmenityDto amenityDtoWithCommunity = baseAmenityDto.communityId(communityId);
    final Amenity baseAmenity = new Amenity();
    final Amenity amenityWithCommunity = new Amenity().withCommunity(community);
    final List<Amenity> amenitiesWithCommunity = singletonList(amenityWithCommunity);
    final HashSet<AmenityDto> requestAmenitiesDto = new HashSet<>(singletonList(baseAmenityDto));
    given(communityService.getCommunityDetailsById(communityId))
        .willReturn(Optional.of(community));
    given(amenityApiMapper.amenityDtoToAmenity(baseAmenityDto))
        .willReturn(baseAmenity);
    given(amenityRepository.saveAll(amenitiesWithCommunity))
        .willReturn(amenitiesWithCommunity);
    given(amenityApiMapper.amenityToAmenityDto(amenityWithCommunity))
        .willReturn(amenityDtoWithCommunity);

    // when
    final Optional<List<AmenityDto>> actualResult =
        amenitySDJpaService.createAmenities(requestAmenitiesDto, communityId);

    // then
    assertTrue(actualResult.isPresent());
    final List<AmenityDto> actualResultAmenitiesDtos = actualResult.get();
    assertEquals(singletonList(amenityDtoWithCommunity), actualResultAmenitiesDtos);
    verify(communityService).getCommunityDetailsById(communityId);
    verify(amenityApiMapper).amenityDtoToAmenity(baseAmenityDto);
    verify(amenityRepository).saveAll(amenitiesWithCommunity);
    verify(amenityApiMapper).amenityToAmenityDto(amenityWithCommunity);
  }

  /**
   * Tests the creation of amenities for a non-existent community by verifying that an
   * empty result is returned and interactions with the community service are performed.
   */
  @Test
  void shouldFailOnAddAmenityToNotExistingCommunity() {
    // given
    final String communityId = "communityId";
    final AmenityDto baseAmenityDto = new AmenityDto()
        .id(1L)
        .amenityId("amenityId")
        .name("name")
        .description("description")
        .price(BigDecimal.valueOf(12));
    final HashSet<AmenityDto> requestAmenitiesDto = new HashSet<>(singletonList(baseAmenityDto));
    given(communityService.getCommunityDetailsById(communityId))
        .willReturn(Optional.empty());

    // when
    final Optional<List<AmenityDto>> actualResult =
        amenitySDJpaService.createAmenities(requestAmenitiesDto, communityId);

    // then
    assertFalse(actualResult.isPresent());
    verify(communityService).getCommunityDetailsById(communityId);
    verifyNoInteractions(amenityApiMapper);
    verifyNoInteractions(amenityRepository);
  }

  /**
   * Tests the update functionality of community amenities by verifying that the
   * `updateAmenity` method of `amenitySDJpaService` successfully updates the amenity
   * and saves it to the database.
   */
  @Test
  void shouldUpdateCommunityAmenitySuccessfully() {
    // given
    Amenity communityAmenity =
        TestUtils.AmenityHelpers.getTestAmenity(TEST_AMENITY_ID, TEST_AMENITY_DESCRIPTION);
    Community testCommunity = TestUtils.CommunityHelpers.getTestCommunity();
    AmenityDto updated = getTestAmenityDto();
    Amenity updatedAmenity = getUpdatedCommunityAmenity();

    given(amenityRepository.findByAmenityId(TEST_AMENITY_ID))
        .willReturn(Optional.of(communityAmenity));
    given(communityRepository.findByCommunityId(TEST_COMMUNITY_ID))
        .willReturn(Optional.of(testCommunity));
    given(amenityRepository.save(updatedAmenity))
        .willReturn(updatedAmenity);

    // when
    boolean result = amenitySDJpaService.updateAmenity(updated);

    // then
    assertTrue(result);
    verify(amenityRepository).findByAmenityId(TEST_AMENITY_ID);
    verify(communityRepository).findByCommunityId(TEST_COMMUNITY_ID);
    verify(amenityRepository).save(updatedAmenity);
  }

  /**
   * Tests the update functionality of a community amenity service when the amenity
   * does not exist. It verifies that the service returns false and does not save the
   * updated amenity.
   */
  @Test
  void shouldNotUpdateCommunityAmenitySuccessfullyIfAmenityNotExists() {
    // given
    given(amenityRepository.findByAmenityId(TEST_AMENITY_ID))
        .willReturn(Optional.empty());

    // when
    boolean result = amenitySDJpaService.updateAmenity(getTestAmenityDto());

    // then
    assertFalse(result);
    verify(amenityRepository, times(0)).save(getUpdatedCommunityAmenity());
    verifyNoInteractions(communityRepository);
  }

  /**
   * Tests the functionality of updating a community amenity when the save operation
   * fails. It checks that the update operation returns false when the amenity repository's
   * save method returns null.
   */
  @Test
  void shouldNotUpdateCommunityAmenitySuccessfullyIfSavingFails() {
    // given
    Amenity testAmenity =
        TestUtils.AmenityHelpers.getTestAmenity(TEST_AMENITY_ID, TEST_AMENITY_DESCRIPTION);
    Amenity updatedAmenity = getUpdatedCommunityAmenity();
    AmenityDto updatedDto = getTestAmenityDto();
    Community community = TestUtils.CommunityHelpers.getTestCommunity();

    given(amenityRepository.findByAmenityId(TEST_AMENITY_ID))
        .willReturn(Optional.of(testAmenity));
    given(communityRepository.findByCommunityId(TEST_COMMUNITY_ID))
        .willReturn(Optional.of(community));
    given(amenityRepository.save(updatedAmenity))
        .willReturn(null);

    // when
    boolean result = amenitySDJpaService.updateAmenity(updatedDto);

    // then
    assertFalse(result);
    verify(amenityRepository).findByAmenityId(TEST_AMENITY_ID);
    verify(communityRepository).findByCommunityId(TEST_COMMUNITY_ID);
    verify(amenityRepository).save(updatedAmenity);
  }

  /**
   * Tests that an amenity is not updated if the associated community does not exist.
   * It uses a test service to update an amenity and verifies that the update was not
   * successful and that the community repository was called to check for the community's
   * existence.
   */
  @Test
  void shouldNotUpdateAmenityIfCommunityDoesNotExist() {
    // given
    Amenity communityAmenity =
        TestUtils.AmenityHelpers.getTestAmenity(TEST_AMENITY_ID, TEST_AMENITY_DESCRIPTION);
    AmenityDto updatedDto = getTestAmenityDto();

    given(amenityRepository.findByAmenityId(TEST_AMENITY_ID))
        .willReturn(Optional.of(communityAmenity));
    given(communityRepository.findByCommunityId(TEST_COMMUNITY_ID))
        .willReturn(Optional.empty());

    // when
    boolean result = amenitySDJpaService.updateAmenity(updatedDto);

    // then
    assertFalse(result);
    verify(amenityRepository).findByAmenityId(TEST_AMENITY_ID);
    verify(communityRepository).findByCommunityId(TEST_COMMUNITY_ID);
    verifyNoMoreInteractions(amenityRepository);
  }

  /**
   * Creates and returns an instance of `AmenityDto` with predefined attributes.
   * The `AmenityDto` instance is initialized with values from constants.
   *
   * @returns an `AmenityDto` object with predefined properties.
   *
   * The returned output is an instance of `AmenityDto` with the following properties:
   * id of type Long, amenityId of unspecified type, name of unspecified type, description
   * of unspecified type, price of unspecified type, and communityId of unspecified type.
   */
  private AmenityDto getTestAmenityDto() {
    Long TEST_AMENITY_ENTITY_ID = 1L;

    return new AmenityDto()
        .id(TEST_AMENITY_ENTITY_ID)
        .amenityId(TEST_AMENITY_ID)
        .name(TEST_AMENITY_NAME)
        .description(TEST_AMENITY_DESCRIPTION)
        .price(TEST_AMENITY_PRICE)
        .communityId(TEST_COMMUNITY_ID);
  }

  /**
   * Returns an updated `Amenity` object based on the data from a `communityAmenityDto`
   * object, with the community set to a test community. The function initializes an
   * `Amenity` object and sets its properties using the data from the `communityAmenityDto`
   * object.
   *
   * @returns an `Amenity` object with specified attributes, including an associated community.
   */
  private Amenity getUpdatedCommunityAmenity() {
    AmenityDto communityAmenityDto = getTestAmenityDto();
    return new Amenity()
        .withAmenityId(communityAmenityDto.getAmenityId())
        .withName(communityAmenityDto.getName())
        .withPrice(communityAmenityDto.getPrice())
        .withDescription(communityAmenityDto.getDescription())
        .withCommunity(TestUtils.CommunityHelpers.getTestCommunity());
  }
}