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
 * Represents a data transfer object for payment information, encapsulating relevant
 * details in a structured format.
 *
 * - paymentId (String): is a string representing an identifier.
 *
 * - charge (BigDecimal): represents a monetary amount.
 *
 * - type (String): represents the type of payment.
 *
 * - description (String): stores a string of text.
 *
 * - recurring (boolean): indicates whether a payment is recurring.
 *
 * - dueDate (String): stores a date.
 *
 * - admin (UserDto): represents a UserDto.
 *
 * - member (HouseMemberDto): represents a HouseMemberDto object.
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
