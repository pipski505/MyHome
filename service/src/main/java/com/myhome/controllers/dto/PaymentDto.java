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

import com.myhome.model.HouseMemberDto;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a data transfer object for payment information, encapsulating key
 * attributes and relationships with other entities.
 *
 * - paymentId (String): stores a unique identifier for a payment.
 *
 * - charge (BigDecimal): is a monetary value represented as a BigDecimal.
 *
 * - type (String): represents the type of payment.
 *
 * - description (String): stores a string.
 *
 * - recurring (boolean): indicates whether a payment is recurring or not.
 *
 * - dueDate (String): represents a date.
 *
 * - admin (UserDto): represents a UserDto object.
 *
 * - member (HouseMemberDto): represents a HouseMemberDto.
 */
@Builder
@Getter
@Setter
public class PaymentDto {
  private String paymentId;
  private BigDecimal charge;
  private String type;
  private String description;
  private boolean recurring;
  private String dueDate;
  private UserDto admin;
  private HouseMemberDto member;
}
