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
 * Provides a set of mappings between domain objects and DTOs for scheduling payments.
 */
@Mapper
public interface SchedulePaymentApiMapper {

  /**
   * Converts a given admin ID into a UserDto object, populating it with the admin ID
   * as the user ID. It uses a builder pattern to create the UserDto object. The resulting
   * object is returned as the function's result.
   *
   * @param adminId ID of an administrator user, used to populate the `userId` field
   * of the `UserDto` object.
   *
   * @returns a `UserDto` instance with the specified `adminId` and default properties.
   */
  @Named("adminIdToAdmin")
  static UserDto adminIdToAdminDto(String adminId) {
    return UserDto.builder()
        .userId(adminId)
        .build();
  }

  /**
   * Creates a new instance of `HouseMemberDto` and initializes its `memberId` field
   * with the provided `memberId` parameter. It then returns the populated `HouseMemberDto`
   * object. The function is annotated with `@Named` for injection purposes.
   *
   * @param memberId identifier of a member to be converted into a `HouseMemberDto` object.
   *
   * @returns a HouseMemberDto object with the given memberId property set.
   */
  @Named("memberIdToMember")
  static HouseMemberDto memberIdToMemberDto(String memberId) {
    return new HouseMemberDto()
        .memberId(memberId);
  }

  /**
   * Returns a string representing the user ID based on the input `UserDto` object.
   * It extracts the user ID from the provided `UserDto` object.
   * It is annotated with `@Named("adminToAdminId")` for injection purposes.
   *
   * @param userDto data transfer object containing user information.
   *
   * @returns the user ID of the input `UserDto` object.
   */
  @Named("adminToAdminId")
  static String adminToAdminId(UserDto userDto) {
    return userDto.getUserId();
  }

  /**
   * Extracts the `memberId` from a `HouseMemberDto` object and returns it as a string.
   *
   * @param houseMemberDto data transfer object containing information about a house member.
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
   * Maps user details from an `enrichedSchedulePaymentRequest` to an instance of
   * `PaymentDto` using a builder, converting user details to admin and member fields.
   *
   * The `getEnrichedRequestMember` and `getEnrichedRequestAdmin` methods are used to
   * extract the relevant information.
   *
   * @param paymentDto Builder instance of the `PaymentDto` class, used to construct
   * the `PaymentDto` object.
   *
   * @param enrichedSchedulePaymentRequest payment request that has been enriched with
   * additional details, which are then used to populate the `PaymentDto` fields.
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
   * Enhances a `SchedulePaymentRequest` object by adding additional details from an
   * `admin` and a `member`. It combines information from both to create an
   * `EnrichedSchedulePaymentRequest` object, which includes community IDs, member
   * details, and other relevant data.
   *
   * @param request SchedulePaymentRequest object that is enriched and returned as an
   * EnrichedSchedulePaymentRequest object.
   *
   * Extract the properties of `request`:
   * Type, Description, Recurring, Charge, DueDate, AdminId.
   *
   * @param admin administrator of a community, from which the function retrieves
   * community IDs, and uses the administrator's information to enrich the
   * `EnrichedSchedulePaymentRequest` object.
   *
   * Extract its main properties:
   * id, name, email, encrypted password, and communities.
   *
   * @param member house member associated with the schedule payment request.
   *
   * Explain the properties of the input `member`.
   *
   * The `member` has properties: `memberId`, `id`, `name`, a `HouseMemberDocument`
   * with `documentFilename`, and a `communityHouse` with `houseId`, or null.
   *
   * @returns an `EnrichedSchedulePaymentRequest` object with enriched data.
   *
   * The EnrichedSchedulePaymentRequest object contains the following properties:
   * type, description, recurring, charge, dueDate, adminId, adminName, adminEmail,
   * adminEncryptedPassword, communityIds, memberId, memberDocument, memberName, communityHouseId.
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
   * Constructs a `UserDto` object from the provided `EnrichedSchedulePaymentRequest`
   * data, mapping specific fields to the corresponding `UserDto` properties. It returns
   * a fully populated `UserDto` object. The function relies on the `UserDto` builder
   * for construction.
   *
   * @param enrichedSchedulePaymentRequest input data from which a `UserDto` object is
   * constructed.
   *
   * @returns a UserDto object populated with admin data from the EnrichedSchedulePaymentRequest.
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
   * Returns an enriched HouseMemberDto object based on the provided EnrichedSchedulePaymentRequest.
   * It extracts and populates the id, memberId, and name fields of the HouseMemberDto
   * from the EnrichedSchedulePaymentRequest.
   *
   * @param enrichedSchedulePaymentRequest object containing the enriched schedule
   * payment request data.
   *
   * @returns an instance of `HouseMemberDto` with specified properties.
   */
  default HouseMemberDto getEnrichedRequestMember(EnrichedSchedulePaymentRequest enrichedSchedulePaymentRequest) {
    return new HouseMemberDto()
        .id(enrichedSchedulePaymentRequest.getMemberEntityId())
        .memberId(enrichedSchedulePaymentRequest.getMemberId())
        .name(enrichedSchedulePaymentRequest.getHouseMemberName());
  }
}
