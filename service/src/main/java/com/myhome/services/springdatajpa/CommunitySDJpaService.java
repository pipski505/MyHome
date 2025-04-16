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

import com.myhome.controllers.dto.CommunityDto;
import com.myhome.controllers.dto.mapper.CommunityMapper;
import com.myhome.domain.Community;
import com.myhome.domain.CommunityHouse;
import com.myhome.domain.HouseMember;
import com.myhome.domain.User;
import com.myhome.repositories.CommunityHouseRepository;
import com.myhome.repositories.CommunityRepository;
import com.myhome.repositories.UserRepository;
import com.myhome.services.CommunityService;
import com.myhome.services.HouseService;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Provides data access and management operations for communities, their houses, and
 * administrators, utilizing Spring Data JPA for database interactions.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CommunitySDJpaService implements CommunityService {
  private final CommunityRepository communityRepository;
  private final UserRepository communityAdminRepository;
  private final CommunityMapper communityMapper;
  private final CommunityHouseRepository communityHouseRepository;
  private final HouseService houseService;

  /**
   * Creates a new community, assigns a unique ID, adds an admin user, and saves the
   * community to the repository. It uses the `communityMapper` to convert the DTO to
   * a community object and the `communityRepository` to persist the community.
   *
   * @param communityDto data from a community entity in a data transfer object format,
   * which is then used to create a community entity in the system.
   *
   * @returns a `Community` object with a unique ID and saved to the repository.
   */
  @Override
  public Community createCommunity(CommunityDto communityDto) {
    communityDto.setCommunityId(generateUniqueId());
    String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Community community = addAdminToCommunity(communityMapper.communityDtoToCommunity(communityDto),
        userId);
    Community savedCommunity = communityRepository.save(community);
    log.trace("saved community with id[{}] to repository", savedCommunity.getId());
    return savedCommunity;
  }

  /**
   * Adds an existing community administrator to a specified community by adding the
   * community to the administrator's managed communities and updating the community's
   * administrators. The function takes a community and a user ID as input and returns
   * the updated community.
   *
   * @param community community to which an administrator is being added.
   *
   * @param userId identifier of the user to be added as an admin to the specified community.
   *
   * @returns a `Community` object with the added administrator.
   */
  private Community addAdminToCommunity(Community community, String userId) {
    communityAdminRepository.findByUserIdWithCommunities(userId).ifPresent(admin -> {
      admin.getCommunities().add(community);
      Set<User> admins = new HashSet<>();
      admins.add(admin);
      community.setAdmins(admins);
    });
    return community;
  }

  /**
   * Retrieves a list of all communities from the database,
   * paginates the results based on the provided `Pageable` object,
   * and returns a set of unique community objects.
   *
   * @param pageable pagination criteria for retrieving a subset of data from the
   * database, allowing for efficient handling of large data sets.
   *
   * @returns a set of Community objects retrieved from the database.
   */
  @Override
  public Set<Community> listAll(Pageable pageable) {
    Set<Community> communityListSet = new HashSet<>();
    communityRepository.findAll(pageable).forEach(communityListSet::add);
    return communityListSet;
  }

  /**
   * Retrieves all communities from the community repository, adds them to a set, and
   * returns the set. The set contains unique community objects, eliminating duplicates.
   * The function is likely used for listing all available communities in an application.
   *
   * @returns a set of all communities stored in the community repository.
   */
  @Override public Set<Community> listAll() {
    Set<Community> communities = new HashSet<>();
    communityRepository.findAll().forEach(communities::add);
    return communities;
  }

  /**
   * Retrieves a list of community houses for a given community ID, paginated according
   * to the provided Pageable object. If the community with the specified ID exists,
   * it returns an Optional containing the list of community houses. Otherwise, it
   * returns an empty Optional.
   *
   * @param communityId identifier for a community to find associated community houses.
   *
   * @param pageable pagination criteria for retrieving a subset of community houses,
   * allowing for control over page size and sorting.
   *
   * @returns an Optional containing a List of CommunityHouse objects or an empty Optional.
   */
  @Override
  public Optional<List<CommunityHouse>> findCommunityHousesById(String communityId,
      Pageable pageable) {
    boolean exists = communityRepository.existsByCommunityId(communityId);
    if (exists) {
      return Optional.of(
          communityHouseRepository.findAllByCommunity_CommunityId(communityId, pageable));
    }
    return Optional.empty();
  }

  /**
   * Returns an Optional containing a list of community admins for a given community
   * ID, paginated according to the provided Pageable object, or an empty Optional if
   * the community does not exist.
   *
   * @param communityId identifier for a community, used to check its existence and
   * retrieve community administrators.
   *
   * @param pageable pagination criteria for retrieving a subset of community administrators
   * from the database.
   *
   * Destructure pageable into its properties.
   * pageable is an object with properties:
   * - Pageable.getPageable() is not explicitly used, but it can be used to get the
   * current page number and size.
   *
   * @returns an Optional containing a list of User objects or an empty Optional if the
   * community does not exist.
   *
   * The output is an Optional containing a list of User objects.
   */
  @Override
  public Optional<List<User>> findCommunityAdminsById(String communityId,
      Pageable pageable) {
    boolean exists = communityRepository.existsByCommunityId(communityId);
    if (exists) {
      return Optional.of(
          communityAdminRepository.findAllByCommunities_CommunityId(communityId, pageable)
      );
    }
    return Optional.empty();
  }

  /**
   * Returns an instance of the `Optional` class containing a `User` object if found,
   * or an empty instance if not found. It retrieves the `User` object from the
   * `communityAdminRepository` based on the provided `adminId`. The `adminId` is assumed
   * to be a unique identifier for the community admin.
   *
   * @param adminId identifier of the user to find the community admin for.
   *
   * @returns an Optional containing a User object if found, or an empty Optional otherwise.
   */
  @Override
  public Optional<User> findCommunityAdminById(String adminId) {
    return communityAdminRepository.findByUserId(adminId);
  }

  /**
   * Retrieves community details by a specified identifier and returns them as an
   * Optional object. It utilizes a repository to perform the database query. The result
   * is wrapped in an Optional to handle potential null values.
   *
   * @param communityId identifier for the community details to be retrieved.
   *
   * @returns an Optional instance containing a Community object if found, otherwise
   * an empty Optional.
   */
  @Override public Optional<Community> getCommunityDetailsById(String communityId) {
    return communityRepository.findByCommunityId(communityId);
  }

  /**
   * Retrieves community details by ID, including associated administrators.
   * It returns an Optional result, indicating the presence or absence of the community.
   * The community data is fetched from a community repository.
   *
   * @param communityId identifier used to retrieve community details from the database.
   *
   * @returns an Optional containing a Community object with associated admin details.
   */
  @Override
  public Optional<Community> getCommunityDetailsByIdWithAdmins(String communityId) {
    return communityRepository.findByCommunityIdWithAdmins(communityId);
  }

  /**
   * Adds administrators to a community by updating the community's administrators and
   * the administrators' communities in the database. It takes a community ID and a set
   * of administrator IDs as input. It returns the updated community if found, otherwise
   * an empty Optional.
   *
   * @param communityId identifier used to search for a community in the database.
   *
   * @param adminsIds set of user IDs to be added as admins to the community specified
   * by the `communityId`.
   *
   * Unwrap.
   * It is a Set of String IDs.
   *
   * @returns an Optional containing the updated Community object if successful, otherwise
   * an empty Optional.
   *
   * The returned output is an `Optional` containing a `Community` object.
   */
  @Override
  public Optional<Community> addAdminsToCommunity(String communityId, Set<String> adminsIds) {
    Optional<Community> communitySearch =
        communityRepository.findByCommunityIdWithAdmins(communityId);

    return communitySearch.map(community -> {
      adminsIds.forEach(adminId -> {
        communityAdminRepository.findByUserIdWithCommunities(adminId).map(admin -> {
          admin.getCommunities().add(community);
          community.getAdmins().add(communityAdminRepository.save(admin));
          return admin;
        });
      });
      return Optional.of(communityRepository.save(community));
    }).orElseGet(Optional::empty);
  }

  /**
   * Adds new houses to a community if they do not already exist, generates unique IDs
   * for new houses, saves the updated community and houses to the database, and returns
   * a set of IDs of the added houses.
   *
   * @param communityId identifier for the community to which houses are being added.
   *
   * @param houses set of community houses to be added to a specified community.
   *
   * Contain a set of `CommunityHouse` objects.
   *
   * @returns a set of unique IDs of newly added houses to the specified community.
   *
   * The returned output is a Set of unique String IDs representing the newly added houses.
   */
  @Override
  public Set<String> addHousesToCommunity(String communityId, Set<CommunityHouse> houses) {
    Optional<Community> communitySearch =
        communityRepository.findByCommunityIdWithHouses(communityId);

    return communitySearch.map(community -> {
      Set<String> addedIds = new HashSet<>();

      houses.forEach(house -> {
        if (house != null) {
          boolean houseExists = community.getHouses().stream()
              .noneMatch(communityHouse ->
                  communityHouse.getHouseId().equals(house.getHouseId())
                      && communityHouse.getName().equals(house.getName())
              );
          if (houseExists) {
            house.setHouseId(generateUniqueId());
            house.setCommunity(community);
            addedIds.add(house.getHouseId());
            communityHouseRepository.save(house);
            community.getHouses().add(house);
          }
        }
      });

      communityRepository.save(community);

      return addedIds;
    }).orElse(new HashSet<>());
  }

  /**
   * Removes an admin from a community and saves the updated community if the admin
   * exists, returning true if successful, otherwise false.
   *
   * @param communityId identifier of the community from which an administrator is to
   * be removed.
   *
   * @param adminId identifier of the admin to be removed from the specified community.
   *
   * @returns a boolean indicating whether the admin was successfully removed from the
   * community.
   */
  @Override
  public boolean removeAdminFromCommunity(String communityId, String adminId) {
    Optional<Community> communitySearch =
        communityRepository.findByCommunityIdWithAdmins(communityId);
    return communitySearch.map(community -> {
      boolean adminRemoved =
          community.getAdmins().removeIf(admin -> admin.getUserId().equals(adminId));
      if (adminRemoved) {
        communityRepository.save(community);
        return true;
      } else {
        return false;
      }
    }).orElse(false);
  }

  /**
   * Deletes a community and all its associated houses from the database. It first
   * retrieves the community with its houses, then removes each house from the community
   * and finally deletes the community itself.
   *
   * @param communityId identifier used to locate the community within the database for
   * deletion.
   *
   * @returns a boolean value indicating whether the community deletion was successful.
   */
  @Override
  @Transactional
  public boolean deleteCommunity(String communityId) {
    return communityRepository.findByCommunityIdWithHouses(communityId)
        .map(community -> {
          Set<String> houseIds = community.getHouses()
              .stream()
              .map(CommunityHouse::getHouseId)
              .collect(Collectors.toSet());

          houseIds.forEach(houseId -> removeHouseFromCommunityByHouseId(community, houseId));
          communityRepository.delete(community);

          return true;
        })
        .orElse(false);
  }

  /**
   * Generates a unique identifier as a string.
   * It uses the `UUID.randomUUID()` method to create a random UUID.
   * The UUID is then converted to a string using the `toString()` method.
   *
   * @returns a 128-bit random unique identifier in the form of a string.
   */
  private String generateUniqueId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Removes a house from a community by its ID, deletes its members, and saves the
   * updated community.
   *
   * @param community community from which the house with the specified `houseId` is
   * to be removed.
   *
   * Destructure contains CommunityHouse houses and CommunityRepository community.
   *
   * @param houseId identifier of the house to be removed from a community.
   *
   * @returns a boolean value indicating whether the house was successfully removed
   * from the community.
   */
  @Transactional
  @Override
  public boolean removeHouseFromCommunityByHouseId(Community community, String houseId) {
    if (community == null) {
      return false;
    } else {
      Optional<CommunityHouse> houseOptional =
          communityHouseRepository.findByHouseIdWithHouseMembers(houseId);
      return houseOptional.map(house -> {
        Set<CommunityHouse> houses = community.getHouses();
        houses.remove(
            house); //remove the house before deleting house members because otherwise the Set relationship would be broken and remove would not work

        Set<String> memberIds = house.getHouseMembers()
            .stream()
            .map(HouseMember::getMemberId)
            .collect(
                Collectors.toSet()); //streams are immutable so need to collect all the member IDs and then delete them from the house

        memberIds.forEach(id -> houseService.deleteMemberFromHouse(houseId, id));

        communityRepository.save(community);
        communityHouseRepository.deleteByHouseId(houseId);
        return true;
      }).orElse(false);
    }
  }
}
