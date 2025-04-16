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

package com.myhome.controllers.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a data transfer object for user information, encapsulating user attributes
 * and their respective data types.
 *
 * - id (Long): is a unique identifier of type Long for a user.
 *
 * - userId (String): is a unique identifier for a user.
 *
 * - name (String): Contains a user's name.
 *
 * - email (String): stores a user's email address.
 *
 * - password (String): is a plain text password.
 *
 * - encryptedPassword (String): stores a user's encrypted password.
 *
 * - communityIds (Set<String>): holds a collection of community IDs.
 *
 * - emailConfirmed (boolean): indicates if the user's email address has been confirmed.
 */
@Builder
@Getter
@Setter
public class UserDto {
  private Long id;
  private String userId;
  private String name;
  private String email;
  private String password;
  private String encryptedPassword;
  private Set<String> communityIds;
  private boolean emailConfirmed;
}
