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
 * Handles payment-related REST endpoints, including scheduling payments, retrieving
 * payment details, and listing payments for house members or administrators.
 * It interacts with payment and community services to perform these operations.
 * It implements the PaymentsApi interface.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController implements PaymentsApi {
  private final PaymentService paymentService;
  private final CommunityService communityService;
  private final SchedulePaymentApiMapper schedulePaymentApiMapper;

  /**
   * Processes a payment scheduling request. It retrieves house member and admin
   * information, validates the admin's authority, and schedules the payment if the
   * admin is authorized for the community house.
   *
   * @param request SchedulePaymentRequest object containing details of the scheduled
   * payment.
   *
   * Contain member id and admin id.
   *
   * @returns a `ResponseEntity` containing a `SchedulePaymentResponse` with a 201
   * status code or a 404 status code.
   *
   * The output is a `ResponseEntity` object with a `SchedulePaymentResponse` body and
   * a HTTP status code of `201 Created` upon successful execution.
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
   * Checks if a given `User` is an admin of a specified `CommunityHouse` by verifying
   * the user's presence in the community's admin list.
   *
   * @param communityHouse community house entity whose admin status is being queried.
   *
   * @param admin user to be checked for admin status in the specified community house.
   *
   * @returns a boolean indicating whether the specified admin user is associated with
   * the community house.
   */
  private boolean isUserAdminOfCommunityHouse(CommunityHouse communityHouse, User admin) {
    return communityHouse.getCommunity()
        .getAdmins()
        .contains(admin);
  }

  /**
   * Retrieves payment details by ID, maps the result to a response object, and returns
   * a ResponseEntity with a 200 status code if the payment is found, or a 404 status
   * code if it is not.
   *
   * @param paymentId unique identifier of the payment details to be retrieved.
   *
   * @returns a `ResponseEntity` containing a `SchedulePaymentResponse` object or a 404
   * not found response.
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
   * Returns a list of payments for a given member ID, mapping the payments to a REST
   * API response, and returns a 404 response if the member is not found.
   *
   * @param memberId identifier for a house member whose payments are to be retrieved
   * and returned.
   *
   * @returns a ResponseEntity containing a ListMemberPaymentsResponse object with
   * member payments data.
   *
   * Contain a ResponseEntity object with a HTTP status code of 200 (ok) or 404 (not
   * found) based on the presence of the member payments data.
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
   * Returns a list of scheduled payments made by an admin within a specified community,
   * along with pagination information, if the admin is found in the community. Otherwise,
   * it returns a 404 not found response.
   *
   * @param communityId community identifier for which the function checks if the
   * provided `adminId` is authorized.
   *
   * @param adminId identifier of the admin whose scheduled payments are to be listed.
   *
   * @param pageable pagination criteria for retrieving a subset of the scheduled payments.
   *
   * Destructure it to its main properties:
   * - `pageNumber`: The number of the page to return, zero-based.
   * - `pageSize`: The number of records to return in each page.
   * - `sort`: The sorting criteria for the records.
   *
   * @returns a `ListAdminPaymentsResponse` containing a set of `AdminPayment` objects
   * and page information.
   *
   * The output is a ResponseEntity object containing a ListAdminPaymentsResponse body.
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
   * Checks if a given admin ID exists in the admins list of a community with a specified
   * ID. If the community does not exist, it throws a RuntimeException.
   *
   * @param communityId identifier of the community for which the function checks whether
   * a specified admin exists.
   *
   * @param adminId identifier of the user to be checked as an admin in the specified
   * community.
   *
   * @returns a boolean indicating whether the adminId exists in the given community
   * or a RuntimeException.
   */
  private Boolean isAdminInGivenCommunity(String communityId, String adminId) {
    return communityService.getCommunityDetailsByIdWithAdmins(communityId)
        .map(Community::getAdmins)
        .map(admins -> admins.stream().anyMatch(admin -> admin.getUserId().equals(adminId)))
        .orElseThrow(
            () -> new RuntimeException("Community with given id not exists: " + communityId));
  }
}
