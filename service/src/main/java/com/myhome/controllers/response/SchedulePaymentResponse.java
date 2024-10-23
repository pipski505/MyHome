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

package com.myhome.controllers.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a payment schedule response object.
 *
 * - paymentId (String): represents a unique identifier for a payment.
 *
 * - charge (BigDecimal): is a BigDecimal representing a monetary amount.
 *
 * - type (String): represents a string value.
 *
 * - description (String): stores a description.
 *
 * - recurring (boolean): is a boolean indicating whether the payment is recurring.
 *
 * - dueDate (String): stores a date.
 *
 * - adminId (String): is a string representing the identifier of an administrator.
 *
 * - memberId (String): stores an identifier for a member.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SchedulePaymentResponse {
  private String paymentId;
  private BigDecimal charge;
  private String type;
  private String description;
  private boolean recurring;
  private String dueDate;
  private String adminId;
  private String memberId;
}
