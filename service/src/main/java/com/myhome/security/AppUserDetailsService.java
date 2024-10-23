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
 * Is a custom implementation of UserDetailsService, responsible for retrieving user
 * details from a database and mapping them to a UserDetails object. It utilizes
 * UserRepository and UserMapper to fetch and transform data. The class provides two
 * methods: loadUserByUsername and getUserDetailsByUsername.
 */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  /**
   * Retrieves a user with a specified email address from a database using a repository,
   * and if found, returns a UserDetails object representing that user, otherwise throws
   * an exception. The returned object includes the user's email, encrypted password,
   * and boolean flags indicating account enabled and locked status.
   *
   * @param username email address of the user to be retrieved from the database and validated.
   *
   * @returns a `UserDetails` object representing the authenticated user.
   *
   * The returned object is an instance of the `UserDetails` class with the following
   * attributes - email address, encrypted password, and a collection of authorities
   * (empty in this case).
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
   * Retrieves a `User` entity from the database by its email address, which is provided
   * as a username parameter. If the user is not found, it throws a `UsernameNotFoundException`.
   * Otherwise, it converts the retrieved user to a `UserDto` object and returns it.
   *
   * @param username username to be used for searching and retrieving user details from
   * the database using the `userRepository`.
   *
   * @returns a `UserDto` object.
   */
  public UserDto getUserDetailsByUsername(String username) {
    com.myhome.domain.User user = userRepository.findByEmail(username);
    if (user == null) {
      throw new UsernameNotFoundException(username);
    }
    return userMapper.userToUserDto(user);
  }
}
