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

package com.myhome.controllers.mapper;

import com.myhome.controllers.dto.PaymentDto;
import com.myhome.controllers.dto.UserDto;
import com.myhome.controllers.request.EnrichedSchedulePaymentRequest;
import com.myhome.domain.Community;
import com.myhome.domain.HouseMember;
import com.myhome.domain.Payment;
import com.myhome.domain.User;
import com.myhome.model.AdminPayment;
import com.myhome.model.HouseMemberDto;
import com.myhome.model.MemberPayment;
import com.myhome.model.SchedulePaymentRequest;
import com.myhome.model.SchedulePaymentResponse;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

/**
 * Defines a set of mappings between various payment-related data transfer objects
 * and domain objects in a Java application.
 */
@Mapper
public interface SchedulePaymentApiMapper {

  /**
   * Converts an admin ID to a UserDto object with the given admin ID as the user ID.
   * The UserDto object is created using a builder pattern.
   * The resulting object is returned by the function.
   *
   * @param adminId identifier for an admin user, used to construct a `UserDto` object.
   *
   * @returns a `UserDto` object with the given `adminId` as the `userId`.
   */
  @Named("adminIdToAdmin")
  static UserDto adminIdToAdminDto(String adminId) {
    return UserDto.builder()
        .userId(adminId)
        .build();
  }

  /**
   * Converts a given `memberId` into a `HouseMemberDto` object with the `memberId`
   * property set to the input value. The function uses the `@Named` annotation to
   * specify the name of the function. The returned object is a new instance of `HouseMemberDto`.
   *
   * @param memberId identifier of a HouseMember entity to be mapped to a HouseMemberDto
   * object.
   *
   * @returns a `HouseMemberDto` object with the specified `memberId` property set.
   */
  @Named("memberIdToMember")
  static HouseMemberDto memberIdToMemberDto(String memberId) {
    return new HouseMemberDto()
        .memberId(memberId);
  }

  /**
   * Returns the user ID of a user based on a provided UserDto object. It appears to
   * be a simple getter method for the user ID. The function is annotated with @Named,
   * suggesting it may be used for dependency injection.
   *
   * @param userDto object containing user data being processed by the function.
   *
   * @returns the user ID of the given `UserDto` object as a string.
   */
  @Named("adminToAdminId")
  static String adminToAdminId(UserDto userDto) {
    return userDto.getUserId();
  }

  /**
   * Returns the member ID of a given `HouseMemberDto` object. It is annotated with
   * `@Named("memberToMemberId")`, indicating its purpose. The function takes a
   * `HouseMemberDto` as input and extracts the `memberId` property.
   *
   * @param houseMemberDto data transfer object containing information about a house
   * member, which is used to retrieve the member's ID.
   *
   * @returns the member ID of the provided `HouseMemberDto` object.
   */
  @Named("memberToMemberId")
  static String memberToMemberId(HouseMemberDto houseMemberDto) {
    return houseMemberDto.getMemberId();
  }

  @Mappings({
      @Mapping(source = "adminId", target = "admin", qualifiedByName = "adminIdToAdmin"),
      @Mapping(source = "memberId", target = "member", qualifiedByName = "memberIdToMember")
  })
  PaymentDto schedulePaymentRequestToPaymentDto(SchedulePaymentRequest schedulePaymentRequest);

  PaymentDto enrichedSchedulePaymentRequestToPaymentDto(
      EnrichedSchedulePaymentRequest enrichedSchedulePaymentRequest);

  /**
   * Maps user details from an enriched payment request to admin and member fields in
   * a payment DTO, using the `getEnrichedRequestMember` and `getEnrichedRequestAdmin`
   * methods to retrieve the details.
   *
   * @param paymentDto Builder instance of the `PaymentDto` class, allowing for the
   * mapping of enriched data from the `enrichedSchedulePaymentRequest` object.
   *
   * @param enrichedSchedulePaymentRequest  enriched payment request object containing
   * user details to be converted to admin and house member.
   */
  @AfterMapping
  default void setUserFields(@MappingTarget PaymentDto.PaymentDtoBuilder paymentDto, EnrichedSchedulePaymentRequest enrichedSchedulePaymentRequest) {
    // MapStruct and Lombok requires you to pass in the Builder instance of the class if that class is annotated with @Builder, or else the AfterMapping method is not used.
    // required to use AfterMapping to convert the user details of the payment request to admin, and same with house member
    paymentDto.member(getEnrichedRequestMember(enrichedSchedulePaymentRequest));
    paymentDto.admin(getEnrichedRequestAdmin(enrichedSchedulePaymentRequest));
  }

  Set<MemberPayment> memberPaymentSetToRestApiResponseMemberPaymentSet(
      Set<Payment> memberPaymentSet);

  @Mapping(target = "memberId", expression = "java(payment.getMember().getMemberId())")
  MemberPayment paymentToMemberPayment(Payment payment);

  Set<AdminPayment> adminPaymentSetToRestApiResponseAdminPaymentSet(
      Set<Payment> memberPaymentSet);

  @Mapping(target = "adminId", expression = "java(payment.getAdmin().getUserId())")
  AdminPayment paymentToAdminPayment(Payment payment);

  @Mappings({
      @Mapping(source = "admin", target = "adminId", qualifiedByName = "adminToAdminId"),
      @Mapping(source = "member", target = "memberId", qualifiedByName = "memberToMemberId")
  })
  SchedulePaymentResponse paymentToSchedulePaymentResponse(PaymentDto payment);

  /**
   * Combines a `SchedulePaymentRequest` with additional data from a `User` and a
   * `HouseMember`, creating an `EnrichedSchedulePaymentRequest` object with enhanced
   * information.
   *
   * @param request SchedulePaymentRequest object from which other properties are copied.
   *
   * Copy properties of `request` into the `EnrichedSchedulePaymentRequest` constructor:
   * type, description, recurring, charge, due date, admin ID.
   *
   * @param admin administrator of a community and is used to retrieve various details
   * about the admin, such as their communities, name, email, and ID.
   *
   * Extract the properties of the `admin` object as follows:
   * Id, name, email, encrypted password, and communities.
   *
   * @param member HouseMember associated with the enriched schedule payment request,
   * providing additional details such as their ID, name, and community house information.
   *
   * Deconstruct `member` into its main properties:
   * - `memberId`
   * - `id`
   * - `houseMemberDocument` (with its property `documentFilename`)
   * - `name`
   * - `communityHouse` (with its property `houseId`)
   *
   * @returns an EnrichedSchedulePaymentRequest object containing enriched payment
   * request data.
   *
   * The output is an instance of `EnrichedSchedulePaymentRequest` with properties
   * including type, description, recurring status, charge, due date, admin ID, admin
   * name, admin email, admin encrypted password, community IDs, member ID, member
   * document filename, member name, and community house ID.
   */
  default EnrichedSchedulePaymentRequest enrichSchedulePaymentRequest(
      SchedulePaymentRequest request, User admin, HouseMember member) {
    Set<String> communityIds = admin.getCommunities()
        .stream()
        .map(Community::getCommunityId)
        .collect(Collectors.toSet());
    return new EnrichedSchedulePaymentRequest(request.getType(),
        request.getDescription(),
        request.isRecurring(),
        request.getCharge(),
        request.getDueDate(),
        request.getAdminId(),
        admin.getId(),
        admin.getName(),
        admin.getEmail(),
        admin.getEncryptedPassword(),
        communityIds,
        member.getMemberId(),
        member.getId(),
        member.getHouseMemberDocument() != null ? member.getHouseMemberDocument()
            .getDocumentFilename() : "",
        member.getName(),
        member.getCommunityHouse() != null ? member.getCommunityHouse().getHouseId() : "");
  }

  /**
   * Constructs a `UserDto` object from an `EnrichedSchedulePaymentRequest` object,
   * extracting and mapping relevant fields such as `userId`, `name`, `email`, and
   * `encryptedPassword`. The resulting object contains enriched information about the
   * admin entity.
   *
   * @param enrichedSchedulePaymentRequest source of data used to populate the `UserDto`
   * object.
   *
   * @returns a UserDto object containing admin's id, name, email, encrypted password,
   * and other details.
   */
  default UserDto getEnrichedRequestAdmin(EnrichedSchedulePaymentRequest enrichedSchedulePaymentRequest) {
    return UserDto.builder()
        .userId(enrichedSchedulePaymentRequest.getAdminId())
        .id(enrichedSchedulePaymentRequest.getAdminEntityId())
        .name(enrichedSchedulePaymentRequest.getAdminName())
        .email(enrichedSchedulePaymentRequest.getAdminEmail())
        .encryptedPassword(enrichedSchedulePaymentRequest.getAdminEncryptedPassword())
        .build();
  }

  /**
   * Populates a `HouseMemberDto` object with data from an `EnrichedSchedulePaymentRequest`
   * object. It extracts and sets the `id`, `memberId`, and `name` properties of the
   * `HouseMemberDto` based on the corresponding properties in the `EnrichedSchedulePaymentRequest`.
   *
   * @param enrichedSchedulePaymentRequest data source for the enriched house member information.
   *
   * @returns a `HouseMemberDto` object populated with member ID, name, and entity ID
   * from the input request.
   */
  default HouseMemberDto getEnrichedRequestMember(EnrichedSchedulePaymentRequest enrichedSchedulePaymentRequest) {
    return new HouseMemberDto()
        .id(enrichedSchedulePaymentRequest.getMemberEntityId())
        .memberId(enrichedSchedulePaymentRequest.getMemberId())
        .name(enrichedSchedulePaymentRequest.getHouseMemberName());
  }
}
