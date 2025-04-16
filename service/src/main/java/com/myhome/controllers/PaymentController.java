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

package com.myhome.controllers;

import com.myhome.api.PaymentsApi;
import com.myhome.controllers.dto.PaymentDto;
import com.myhome.controllers.mapper.SchedulePaymentApiMapper;
import com.myhome.controllers.request.EnrichedSchedulePaymentRequest;
import com.myhome.domain.Community;
import com.myhome.domain.CommunityHouse;
import com.myhome.domain.HouseMember;
import com.myhome.domain.Payment;
import com.myhome.domain.User;
import com.myhome.model.AdminPayment;
import com.myhome.model.ListAdminPaymentsResponse;
import com.myhome.model.ListMemberPaymentsResponse;
import com.myhome.model.SchedulePaymentRequest;
import com.myhome.model.SchedulePaymentResponse;
import com.myhome.services.CommunityService;
import com.myhome.services.PaymentService;
import com.myhome.utils.PageInfo;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller which provides endpoints for managing payments
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController implements PaymentsApi {
  private final PaymentService paymentService;
  private final CommunityService communityService;
  private final SchedulePaymentApiMapper schedulePaymentApiMapper;

  /**
   * Processes a payment schedule request by validating the request, retrieving relevant
   * data, and performing payment processing if the admin is a valid user of the community
   * house.
   *
   * @param request SchedulePaymentRequest containing details for the scheduled payment.
   *
   * Contain `memberId`, `adminId` and possibly other properties.
   *
   * @returns either a `SchedulePaymentResponse` with a 201 status code or a 404 response.
   *
   * The returned `ResponseEntity` contains a `SchedulePaymentResponse` object in its
   * body, with a status code of `HttpStatus.CREATED` if the payment is scheduled
   * successfully. Otherwise, it returns a `ResponseEntity` with a status code of `HttpStatus.NOT_FOUND`.
   */
  @Override
  public ResponseEntity<SchedulePaymentResponse> schedulePayment(@Valid
      SchedulePaymentRequest request) {
    log.trace("Received schedule payment request");

    HouseMember houseMember = paymentService.getHouseMember(request.getMemberId())
        .orElseThrow(() -> new RuntimeException(
            "House member with given id not exists: " + request.getMemberId()));
    User admin = communityService.findCommunityAdminById(request.getAdminId())
        .orElseThrow(
            () -> new RuntimeException("Admin with given id not exists: " + request.getAdminId()));

    if (isUserAdminOfCommunityHouse(houseMember.getCommunityHouse(), admin)) {
      final EnrichedSchedulePaymentRequest paymentRequest =
          schedulePaymentApiMapper.enrichSchedulePaymentRequest(request, admin, houseMember);
      final PaymentDto paymentDto =
          schedulePaymentApiMapper.enrichedSchedulePaymentRequestToPaymentDto(paymentRequest);
      final PaymentDto processedPayment = paymentService.schedulePayment(paymentDto);
      final SchedulePaymentResponse paymentResponse =
          schedulePaymentApiMapper.paymentToSchedulePaymentResponse(processedPayment);
      return ResponseEntity.status(HttpStatus.CREATED).body(paymentResponse);
    }

    return ResponseEntity.notFound().build();
  }

  /**
   * Checks if a specified user is an administrator of a given community house by
   * verifying their presence in the list of admins associated with the house's community.
   *
   * @param communityHouse community house for which the administration is being checked.
   *
   * It provides access to the community associated with the community house.
   *
   * @param admin user to check for administration rights in the specified community house.
   *
   * @returns a boolean value indicating whether the given admin is an admin of the
   * community house.
   */
  private boolean isUserAdminOfCommunityHouse(CommunityHouse communityHouse, User admin) {
    return communityHouse.getCommunity()
        .getAdmins()
        .contains(admin);
  }

  /**
   * Handles a request to retrieve payment details by ID, maps the response to a
   * SchedulePaymentResponse object, and returns a ResponseEntity with a 200 status
   * code if successful, or 404 if not found.
   *
   * @param paymentId identifier of the payment for which details are being requested.
   *
   * @returns a ResponseEntity containing a SchedulePaymentResponse object or a 404 Not
   * Found response.
   */
  @Override
  public ResponseEntity<SchedulePaymentResponse> listPaymentDetails(String paymentId) {
    log.trace("Received request to get details about a payment with id[{}]", paymentId);

    return paymentService.getPaymentDetails(paymentId)
        .map(schedulePaymentApiMapper::paymentToSchedulePaymentResponse)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * Retrieves all payments for a specified house member, maps the payments to a REST
   * API response, and returns a `ResponseEntity` containing the payment set. If the
   * member is not found, it returns a 404 response.
   *
   * @param memberId identifier for the house member whose payments are being retrieved
   * and returned.
   *
   * @returns a ListMemberPaymentsResponse object wrapped in a ResponseEntity with a
   * status code of 200 or 404.
   *
   * The output is a `ResponseEntity` containing a `ListMemberPaymentsResponse` object.
   */
  @Override
  public ResponseEntity<ListMemberPaymentsResponse> listAllMemberPayments(String memberId) {
    log.trace("Received request to list all the payments for the house member with id[{}]",
        memberId);

    return paymentService.getHouseMember(memberId)
        .map(payments -> paymentService.getPaymentsByMember(memberId))
        .map(schedulePaymentApiMapper::memberPaymentSetToRestApiResponseMemberPaymentSet)
        .map(memberPayments -> new ListMemberPaymentsResponse().payments(memberPayments))
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * Returns a list of payments scheduled by an admin within a given community, along
   * with pagination information. If the admin is not found in the community, it returns
   * a 404 response.
   *
   * @param communityId identifier for the community in which the admin's scheduled
   * payments are being retrieved.
   *
   * @param adminId identifier of the admin whose scheduled payments are being retrieved.
   *
   * @param pageable pagination criteria for retrieving a subset of scheduled payments,
   * allowing for the specification of page size, sorting, and other parameters.
   *
   * Deconstructing `pageable` reveals that it has several properties, including
   * `pageNumber`, `pageSize`, and `sort`.
   *
   * @returns a `ResponseEntity` containing a `ListAdminPaymentsResponse` object with
   * a set of scheduled payments and pagination information.
   *
   * The output is a `ResponseEntity` containing a `ListAdminPaymentsResponse` body.
   */
  @Override
  public ResponseEntity<ListAdminPaymentsResponse> listAllAdminScheduledPayments(
      String communityId, String adminId, Pageable pageable) {
    log.trace("Received request to list all the payments scheduled by the admin with id[{}]",
        adminId);

    final boolean isAdminInGivenCommunity = isAdminInGivenCommunity(communityId, adminId);

    if (isAdminInGivenCommunity) {
      final Page<Payment> paymentsForAdmin = paymentService.getPaymentsByAdmin(adminId, pageable);
      final List<Payment> payments = paymentsForAdmin.getContent();
      final Set<AdminPayment> adminPayments =
          schedulePaymentApiMapper.adminPaymentSetToRestApiResponseAdminPaymentSet(
              new HashSet<>(payments));
      final ListAdminPaymentsResponse response = new ListAdminPaymentsResponse()
          .payments(adminPayments)
          .pageInfo(PageInfo.of(pageable, paymentsForAdmin));
      return ResponseEntity.ok().body(response);
    }

    return ResponseEntity.notFound().build();
  }

  /**
   * Checks if a given admin ID exists within a specified community, throwing a
   * RuntimeException if the community does not exist.
   *
   * @param communityId identifier of a community whose administrative details are being
   * queried.
   *
   * @param adminId identifier of an administrator whose presence in a specified community
   * is being checked.
   *
   * @returns a boolean indicating whether the given admin ID exists in the specified
   * community.
   */
  private Boolean isAdminInGivenCommunity(String communityId, String adminId) {
    return communityService.getCommunityDetailsByIdWithAdmins(communityId)
        .map(Community::getAdmins)
        .map(admins -> admins.stream().anyMatch(admin -> admin.getUserId().equals(adminId)))
        .orElseThrow(
            () -> new RuntimeException("Community with given id not exists: " + communityId));
  }
}
