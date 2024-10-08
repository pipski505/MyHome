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

package com.myhome.controllers.request;

import com.myhome.model.SchedulePaymentRequest;
import java.math.BigDecimal;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Extends and enriches the SchedulePaymentRequest class with additional admin and
 * house member details.
 *
 * - adminEntityId (Long): represents the ID of an admin entity.
 *
 * - adminName (String): stores the name of the admin.
 *
 * - adminEmail (String): stores the email address of the admin.
 *
 * - adminEncryptedPassword (String): stores the encrypted password of the admin.
 *
 * - adminCommunityIds (Set<String>): stores a set of community IDs.
 *
 * - memberEntityId (Long): represents the ID of a house member.
 *
 * - houseMemberDocumentName (String): stores the document name of a house member.
 *
 * - houseMemberName (String): stores the name of a house member.
 *
 * - houseMemberHouseID (String): Stores a house ID.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class EnrichedSchedulePaymentRequest extends SchedulePaymentRequest {
  private Long adminEntityId;
  private String adminName;
  private String adminEmail;
  private String adminEncryptedPassword;
  private Set<String> adminCommunityIds;
  private Long memberEntityId;
  private String houseMemberDocumentName;
  private String houseMemberName;
  private String houseMemberHouseID;

  public EnrichedSchedulePaymentRequest(String type, String description, boolean recurring,
      BigDecimal charge, String dueDate, String adminId, Long adminEntityId, String adminName,
      String adminEmail, String adminEncryptedPassword, Set<String> adminCommunityIds,
      String memberId, Long memberEntityId, String houseMemberDocumentName, String houseMemberName,
      String houseMemberHouseID) {

    super.type(type).description(description).recurring(recurring).charge(charge).dueDate(dueDate).adminId(adminId).memberId(memberId);

    this.adminName = adminName;
    this.adminEmail = adminEmail;
    this.adminEncryptedPassword = adminEncryptedPassword;
    this.adminCommunityIds = adminCommunityIds;
    this.adminEntityId = adminEntityId;
    this.memberEntityId = memberEntityId;
    this.houseMemberDocumentName = houseMemberDocumentName;
    this.houseMemberName = houseMemberName;
    this.houseMemberHouseID = houseMemberHouseID;
  }
}
