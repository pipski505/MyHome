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

package com.myhome.services.springdatajpa;

import com.myhome.controllers.dto.PaymentDto;
import com.myhome.controllers.dto.mapper.PaymentMapper;
import com.myhome.domain.HouseMember;
import com.myhome.domain.Payment;
import com.myhome.domain.User;
import com.myhome.repositories.HouseMemberRepository;
import com.myhome.repositories.PaymentRepository;
import com.myhome.repositories.UserRepository;
import com.myhome.services.PaymentService;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Implements {@link PaymentService} and uses Spring Data JPA Repository to do its work
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentSDJpaService implements PaymentService {
  private final PaymentRepository paymentRepository;
  private final UserRepository adminRepository;
  private final PaymentMapper paymentMapper;
  private final HouseMemberRepository houseMemberRepository;

  /**
   * Generates a payment ID for a given request,
   * then creates a payment in the repository with the generated ID and request details.
   *
   * @param request input data for creating a payment.
   *
   * @returns an instance of `PaymentDto` representing the scheduled payment.
   */
  @Override
  public PaymentDto schedulePayment(PaymentDto request) {
    generatePaymentId(request);
    return createPaymentInRepository(request);
  }

  /**
   * Retrieves payment details by payment ID,
   * returns the details as an Optional object,
   * maps the payment object to a PaymentDto object.
   *
   * @param paymentId identifier used to retrieve payment details from the repository.
   *
   * @returns an Optional instance containing a PaymentDto object if a matching payment
   * is found.
   */
  @Override
  public Optional<PaymentDto> getPaymentDetails(String paymentId) {
    return paymentRepository.findByPaymentId(paymentId)
        .map(paymentMapper::paymentToPaymentDto);
  }

  /**
   * Returns an Optional containing a HouseMember object based on a provided memberId.
   * The function delegates the search to a houseMemberRepository, which is presumably
   * responsible for database interactions. The result is an Optional, indicating the
   * presence or absence of a matching HouseMember.
   *
   * @param memberId unique identifier of the house member to be retrieved.
   *
   * @returns an Optional containing a HouseMember object if found, otherwise an empty
   * Optional.
   */
  @Override
  public Optional<HouseMember> getHouseMember(String memberId) {
    return houseMemberRepository.findByMemberId(memberId);
  }

  /**
   * Returns a set of payments made by a member with the specified ID, ignoring case
   * when matching the ID. It excludes certain payment fields from the search. The
   * results are retrieved from a payment repository.
   *
   * @param memberId identifier for a member whose payments are to be retrieved.
   *
   * @returns a set of Payment objects associated with the specified member ID.
   *
   * Consists of a set of Payment objects.
   */
  @Override
  public Set<Payment> getPaymentsByMember(String memberId) {
    ExampleMatcher ignoringMatcher = ExampleMatcher.matchingAll()
        .withMatcher("memberId",
            ExampleMatcher.GenericPropertyMatchers.startsWith().ignoreCase())
        .withIgnorePaths("paymentId", "charge", "type", "description", "recurring", "dueDate",
            "admin");

    Example<Payment> paymentExample =
        Example.of(new Payment(null, null, null, null, false, null, null,
                new HouseMember().withMemberId(memberId)),
            ignoringMatcher);

    return new HashSet<>(paymentRepository.findAll(paymentExample));
  }

  /**
   * Retrieves a paginated list of payments based on a given admin ID, ignoring certain
   * properties in the payment entity.
   *
   * @param adminId identifier used to filter payments made by a specific administrator.
   *
   * @param pageable pagination criteria, allowing the function to return a paginated
   * list of payments.
   *
   * Sort: Sort the results in ascending or descending order based on one or more fields.
   * PageSize: The maximum number of items to return in the response.
   * PageNumber: The page number to return.
   * Offset: The number of items to skip before returning results.
   * Size: The number of items to return.
   *
   * @returns a paginated list of `Payment` objects.
   *
   * The returned output is a Page object containing a list of Payment objects. Each
   * Payment object has attributes such as adminId, paymentId, charge, type, description,
   * recurring, dueDate, and memberId.
   */
  @Override
  public Page<Payment> getPaymentsByAdmin(String adminId, Pageable pageable) {
    ExampleMatcher ignoringMatcher = ExampleMatcher.matchingAll()
        .withMatcher("adminId",
            ExampleMatcher.GenericPropertyMatchers.startsWith().ignoreCase())
        .withIgnorePaths("paymentId", "charge", "type", "description", "recurring", "dueDate",
            "memberId");

    Example<Payment> paymentExample =
        Example.of(
            new Payment(null, null, null, null, false, null, new User().withUserId(adminId), null),
            ignoringMatcher);

    return paymentRepository.findAll(paymentExample, pageable);
  }

  /**
   * Creates a payment entity from a payment DTO, saves an associated admin entity and
   * payment entity to the repository, and then returns the payment entity as a payment
   * DTO.
   *
   * @param request data to be converted into a `Payment` object.
   *
   * @returns a `PaymentDto` object representing the newly saved payment.
   */
  private PaymentDto createPaymentInRepository(PaymentDto request) {
    Payment payment = paymentMapper.paymentDtoToPayment(request);

    adminRepository.save(payment.getAdmin());
    paymentRepository.save(payment);

    return paymentMapper.paymentToPaymentDto(payment);
  }

  /**
   * Generates a unique payment identifier for a payment request.
   * It uses the `UUID` class to create a random UUID, converts it to a string, and
   * assigns it to the `paymentId` field of the `PaymentDto` object.
   * The payment identifier is then persisted in the `PaymentDto` object.
   *
   * @param request object to which a unique payment ID is assigned.
   */
  private void generatePaymentId(PaymentDto request) {
    request.setPaymentId(UUID.randomUUID().toString());
  }
}
