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

package com.myhome.services.springdatajpa;

import com.myhome.controllers.mapper.AmenityApiMapper;
import com.myhome.domain.Amenity;
import com.myhome.domain.Community;
import com.myhome.model.AmenityDto;
import com.myhome.repositories.AmenityRepository;
import com.myhome.repositories.CommunityRepository;
import com.myhome.services.AmenityService;
import com.myhome.services.CommunityService;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Provides data access and manipulation services for amenities using Spring Data JPA
 * and repositories.
 */
@Service
@RequiredArgsConstructor
public class AmenitySDJpaService implements AmenityService {

  private final AmenityRepository amenityRepository;
  private final CommunityRepository communityRepository;
  private final CommunityService communityService;
  private final AmenityApiMapper amenityApiMapper;

  /**
   * Creates a list of amenities for a specified community by mapping Dto objects to
   * entities, persisting them in the database, and then mapping the entities back to
   * Dtos. It returns an Optional containing the created amenities if the community
   * exists, otherwise it returns an empty Optional.
   *
   * @param amenities set of amenity objects to be created for a specified community.
   *
   * Contain elements of type `AmenityDto`, are a set of unique elements.
   *
   * @param communityId identifier used to retrieve community details from the
   * `communityService` and associate the created amenities with a specific community.
   *
   * @returns a list of created amenities in the form of `AmenityDto` objects.
   *
   * The output is an `Optional` containing a list of `AmenityDto` objects.
   */
  @Override
  public Optional<List<AmenityDto>> createAmenities(Set<AmenityDto> amenities, String communityId) {
    final Optional<Community> community = communityService.getCommunityDetailsById(communityId);
    if (!community.isPresent()) {
      return Optional.empty();
    }
    final List<Amenity> amenitiesWithCommunity = amenities.stream()
        .map(amenityApiMapper::amenityDtoToAmenity)
        .map(amenity -> {
          amenity.setCommunity(community.get());
          return amenity;
        })
        .collect(Collectors.toList());
    final List<AmenityDto> createdAmenities =
        amenityRepository.saveAll(amenitiesWithCommunity).stream()
            .map(amenityApiMapper::amenityToAmenityDto)
            .collect(Collectors.toList());
    return Optional.of(createdAmenities);
  }

  /**
   * Retrieves the details of an amenity from the database based on its ID.
   * It returns the result as an Optional object, indicating whether the amenity exists
   * or not.
   * The actual retrieval is delegated to the `amenityRepository`.
   *
   * @param amenityId identifier for the amenity whose details are to be retrieved.
   *
   * @returns an Optional containing an Amenity object, or an empty Optional if not found.
   */
  @Override
  public Optional<Amenity> getAmenityDetails(String amenityId) {
    return amenityRepository.findByAmenityId(amenityId);
  }

  /**
   * Deletes an amenity from the database and its associated community. It retrieves
   * the amenity by its ID, removes it from the community's amenities list, and then
   * deletes the amenity. The function returns true if successful, or false if the
   * amenity does not exist.
   *
   * @param amenityId unique identifier of the amenity to be deleted from the database.
   *
   * @returns a boolean indicating whether the amenity was successfully deleted from
   * the database.
   */
  @Override
  public boolean deleteAmenity(String amenityId) {
    return amenityRepository.findByAmenityIdWithCommunity(amenityId)
        .map(amenity -> {
          Community community = amenity.getCommunity();
          community.getAmenities().remove(amenity);
          amenityRepository.delete(amenity);
          return true;
        })
        .orElse(false);
  }

  /**
   * Retrieves a set of amenities for a community with the specified ID, returns an
   * empty set if the community does not exist, and maps the community's amenities
   * collection to a set.
   *
   * @param communityId identifier of a community for which all amenities are being retrieved.
   *
   * @returns a set of amenities associated with a community, or an empty set if none
   * found.
   */
  @Override
  public Set<Amenity> listAllAmenities(String communityId) {
    return communityRepository.findByCommunityIdWithAmenities(communityId)
        .map(Community::getAmenities)
        .orElse(new HashSet<>());
  }

  /**
   * Updates an amenity in the database. It retrieves the amenity and community by their
   * IDs, creates a new Amenity object with the updated information, and saves the
   * updated amenity.
   *
   * @param updatedAmenity updated amenity details to be persisted.
   *
   * Extract the properties of `updatedAmenity`:
   * 1/ `AmenityId` - The unique identifier of the amenity.
   * 2/ `Name` - The name of the amenity.
   * 3/ `Price` - The price of the amenity.
   * 4/ `Description` - A description of the amenity.
   * 5/ `CommunityId` - The identifier of the community to which the amenity belongs.
   *
   * @returns a boolean indicating whether the amenity update was successful.
   */
  @Override
  public boolean updateAmenity(AmenityDto updatedAmenity) {
    String amenityId = updatedAmenity.getAmenityId();
    return amenityRepository.findByAmenityId(amenityId)
        .map(amenity -> communityRepository.findByCommunityId(updatedAmenity.getCommunityId())
            .map(community -> {
              Amenity updated = new Amenity();
              updated.setName(updatedAmenity.getName());
              updated.setPrice(updatedAmenity.getPrice());
              updated.setId(amenity.getId());
              updated.setAmenityId(amenityId);
              updated.setDescription(updatedAmenity.getDescription());
              return updated;
            })
            .orElse(null))
        .map(amenityRepository::save).isPresent();
  }
}
