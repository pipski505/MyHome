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
package com.myhome.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents an entity for managing payments in a service, encompassing various
 * payment details and relationships with other entities.
 *
 * - paymentId (String): is a unique, non-nullable string field.
 *
 * - charge (BigDecimal): represents the amount of the payment.
 *
 * - type (String): represents the type of payment.
 *
 * - description (String): stores a payment description.
 *
 * - recurring (boolean): is a boolean indicating whether a payment is recurring.
 *
 * - dueDate (LocalDate): represents the date a payment is due.
 *
 * - admin (User): represents a User entity.
 *
 * - member (HouseMember): represents a HouseMember.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
public class Payment extends BaseEntity {
  @Column(unique = true, nullable = false)
  private String paymentId;
  @Column(nullable = false)
  private BigDecimal charge;
  @Column(nullable = false)
  private String type;
  @Column(nullable = false)
  private String description;
  @Column(nullable = false)
  private boolean recurring;
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate dueDate;
  @ManyToOne(fetch = FetchType.LAZY)
  private User admin;
  @ManyToOne(fetch = FetchType.LAZY)
  private HouseMember member;
}
