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

package com.myhome.security;

import com.myhome.controllers.dto.UserDto;
import com.myhome.controllers.dto.mapper.UserMapper;
import com.myhome.repositories.UserRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom {@link UserDetailsService} catering to the need of service logic.
 */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  /**
   * Loads a user based on the provided username, retrieves the corresponding user
   * object from the repository, and returns a UserDetails object containing the user's
   * email, password, and other attributes. If the user is not found, it throws a UsernameNotFoundException.
   *
   * @param username username to be searched for in the database, which is used to
   * retrieve the corresponding user object.
   *
   * @returns a `UserDetails` object containing the specified user's email and encrypted
   * password.
   *
   * Returned object is of type `UserDetails`, which has the following attributes:
   * - `username` (email)
   * - `password` (encrypted)
   * - `enabled`, `accountNonExpired`, `accountNonLocked`, and `credentialsNonExpired`
   * (all set to `true`)
   */
  @Override public UserDetails loadUserByUsername(String username)
      throws UsernameNotFoundException {

    com.myhome.domain.User user = userRepository.findByEmail(username);
    if (user == null) {
      throw new UsernameNotFoundException(username);
    }

    return new User(user.getEmail(),
        user.getEncryptedPassword(),
        true,
        true,
        true,
        true,
        Collections.emptyList());
  }

  /**
   * Retrieves a user object by their username from the database using the `userRepository`,
   * throws an exception if the user is not found, and then maps the user object to a
   * `UserDto` object using the `userMapper`.
   *
   * @param username username to search for in the database.
   *
   * @returns a UserDto object, mapped from a User domain object.
   */
  public UserDto getUserDetailsByUsername(String username) {
    com.myhome.domain.User user = userRepository.findByEmail(username);
    if (user == null) {
      throw new UsernameNotFoundException(username);
    }
    return userMapper.userToUserDto(user);
  }
}
