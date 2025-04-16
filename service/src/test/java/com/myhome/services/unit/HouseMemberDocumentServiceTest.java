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

package com.myhome.services.unit;

import helpers.TestUtils;
import com.myhome.domain.HouseMember;
import com.myhome.domain.HouseMemberDocument;
import com.myhome.repositories.HouseMemberDocumentRepository;
import com.myhome.repositories.HouseMemberRepository;
import com.myhome.services.springdatajpa.HouseMemberDocumentSDJpaService;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Provides unit tests for the HouseMemberDocumentService class, covering various
 * scenarios for finding, deleting, updating, and creating house member documents.
 */
public class HouseMemberDocumentServiceTest {

  private static final String MEMBER_ID = "test-member-id";
  private static final String MEMBER_NAME = "test-member-name";
  private static final HouseMemberDocument MEMBER_DOCUMENT =
      new HouseMemberDocument("test-file-name", new byte[0]);
  private static final int COMPRESSION_BORDER_SIZE_KB = 99;
  private static final int MAX_FILE_SIZE_KB = 1;
  private static final long COMPRESSED_IMAGE_QUALITY = (long) 0.99;

  @Mock
  private HouseMemberRepository houseMemberRepository;

  @Mock
  private HouseMemberDocumentRepository houseMemberDocumentRepository;

  @InjectMocks
  private HouseMemberDocumentSDJpaService houseMemberDocumentService;

  /**
   * Initializes Mockito and sets several fields of the `houseMemberDocumentService`
   * object, including compression border size, max file size, and compressed image
   * quality. This setup is likely used for testing purposes to ensure consistent conditions.
   */
  @BeforeEach
  private void init() {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(houseMemberDocumentService, "compressionBorderSizeKBytes",
        COMPRESSION_BORDER_SIZE_KB);
    ReflectionTestUtils.setField(houseMemberDocumentService, "maxFileSizeKBytes", MAX_FILE_SIZE_KB);
    ReflectionTestUtils.setField(houseMemberDocumentService, "compressedImageQuality",
        COMPRESSED_IMAGE_QUALITY);
  }

  /**
   * Tests the retrieval of a House Member document from a repository. It verifies that
   * the service returns the correct document when the member ID is valid and the
   * repository returns the expected result.
   */
  @Test
  void findMemberDocumentSuccess() {
    // given
    HouseMember testMember = new HouseMember(MEMBER_ID, MEMBER_DOCUMENT, MEMBER_NAME, null);
    given(houseMemberRepository.findByMemberId(MEMBER_ID))
        .willReturn(Optional.of(testMember));
    // when
    Optional<HouseMemberDocument> houseMemberDocument =
        houseMemberDocumentService.findHouseMemberDocument(MEMBER_ID);

    // then
    assertTrue(houseMemberDocument.isPresent());
    assertEquals(MEMBER_DOCUMENT, houseMemberDocument.get());
    verify(houseMemberRepository).findByMemberId(MEMBER_ID);
  }

  /**
   * Tests the `findHouseMemberDocument` service. It creates a test member with no
   * document, retrieves the member from the repository, and then calls the
   * `findHouseMemberDocument` service. The test asserts that no document is found and
   * verifies that the repository was queried by the service.
   */
  @Test
  void findMemberDocumentNoDocumentPresent() {
    // given
    HouseMember testMember = new HouseMember(MEMBER_ID, null, MEMBER_NAME, null);
    given(houseMemberRepository.findByMemberId(MEMBER_ID))
        .willReturn(Optional.of(testMember));
    // when
    Optional<HouseMemberDocument> houseMemberDocument =
        houseMemberDocumentService.findHouseMemberDocument(MEMBER_ID);

    // then
    assertFalse(houseMemberDocument.isPresent());
    verify(houseMemberRepository).findByMemberId(MEMBER_ID);
  }

  /**
   * Tests the retrieval of a house member document when the member does not exist.
   * It uses Mockito to simulate a repository return of an empty Optional.
   * The function verifies that the repository is called with the correct member ID.
   */
  @Test
  void findMemberDocumentMemberNotExists() {
    // given
    given(houseMemberRepository.findByMemberId(MEMBER_ID))
        .willReturn(Optional.empty());
    // when
    Optional<HouseMemberDocument> houseMemberDocument =
        houseMemberDocumentService.findHouseMemberDocument(MEMBER_ID);

    // then
    assertFalse(houseMemberDocument.isPresent());
    verify(houseMemberRepository).findByMemberId(MEMBER_ID);
  }

  /**
   * Tests the deletion of a house member document. It verifies that a document is
   * successfully deleted by a service when given a member ID. The document is removed
   * from the repository and the member object.
   */
  @Test
  void deleteMemberDocumentSuccess() {
    // given
    HouseMember testMember = new HouseMember(MEMBER_ID, MEMBER_DOCUMENT, MEMBER_NAME, null);
    given(houseMemberRepository.findByMemberId(MEMBER_ID))
        .willReturn(Optional.of(testMember));
    // when
    boolean isDocumentDeleted = houseMemberDocumentService.deleteHouseMemberDocument(MEMBER_ID);

    // then
    assertTrue(isDocumentDeleted);
    assertNull(testMember.getHouseMemberDocument());
    verify(houseMemberRepository).findByMemberId(MEMBER_ID);
    verify(houseMemberRepository).save(testMember);
  }

  /**
   * Tests the deletion of a house member document when no document exists. It verifies
   * that the deletion process returns false, the member's document remains null, and
   * the repository's save method is not called.
   */
  @Test
  void deleteMemberDocumentNoDocumentPresent() {
    // given
    HouseMember testMember = new HouseMember(MEMBER_ID, null, MEMBER_NAME, null);
    given(houseMemberRepository.findByMemberId(MEMBER_ID))
        .willReturn(Optional.of(testMember));
    // when
    boolean isDocumentDeleted = houseMemberDocumentService.deleteHouseMemberDocument(MEMBER_ID);

    // then
    assertFalse(isDocumentDeleted);
    assertNull(testMember.getHouseMemberDocument());
    verify(houseMemberRepository).findByMemberId(MEMBER_ID);
    verify(houseMemberRepository, never()).save(testMember);
  }

  /**
   * Tests the deletion of a house member document when the member does not exist. It
   * calls the `deleteHouseMemberDocument` service method with a member ID, verifying
   * that no document is deleted and the repository is queried but not saved.
   */
  @Test
  void deleteMemberDocumentMemberNotExists() {
    // given
    given(houseMemberRepository.findByMemberId(MEMBER_ID))
        .willReturn(Optional.empty());
    // when
    boolean isDocumentDeleted = houseMemberDocumentService.deleteHouseMemberDocument(MEMBER_ID);

    // then
    assertFalse(isDocumentDeleted);
    verify(houseMemberRepository).findByMemberId(MEMBER_ID);
    verify(houseMemberRepository, never()).save(any());
  }

  /**
   * Tests the successful update of a house member document. It verifies that a new
   * document is saved to the repository, the existing house member is updated with the
   * new document, and the correct repository methods are called.
   */
  @Test
  void updateHouseMemberDocumentSuccess() throws IOException {
    // given
    byte[] imageBytes = TestUtils.General.getImageAsByteArray(10, 10);
    MockMultipartFile newDocumentFile = new MockMultipartFile("new-test-file-name", imageBytes);
    HouseMemberDocument savedDocument =
        new HouseMemberDocument(String.format("member_%s_document.jpg", MEMBER_ID), imageBytes);
    HouseMember testMember = new HouseMember(MEMBER_ID, MEMBER_DOCUMENT, MEMBER_NAME, null);

    given(houseMemberRepository.findByMemberId(MEMBER_ID))
        .willReturn(Optional.of(testMember));
    given(houseMemberDocumentRepository.save(savedDocument))
        .willReturn(savedDocument);
    // when
    Optional<HouseMemberDocument> houseMemberDocument =
        houseMemberDocumentService.updateHouseMemberDocument(newDocumentFile, MEMBER_ID);

    // then
    assertTrue(houseMemberDocument.isPresent());
    assertEquals(testMember.getHouseMemberDocument(), houseMemberDocument.get());
    verify(houseMemberRepository).findByMemberId(MEMBER_ID);
    verify(houseMemberDocumentRepository).save(savedDocument);
    verify(houseMemberRepository).save(testMember);
  }

  /**
   * Tests the functionality of updating a house member document when the member does
   * not exist. It checks that the document is not saved and the repository is not
   * updated. The test verifies that the expected behavior occurs when the member is
   * not found.
   */
  @Test
  void updateHouseMemberDocumentMemberNotExists() throws IOException {
    // given
    byte[] imageBytes = TestUtils.General.getImageAsByteArray(10, 10);
    MockMultipartFile newDocumentFile = new MockMultipartFile("new-test-file-name", imageBytes);

    given(houseMemberRepository.findByMemberId(MEMBER_ID))
        .willReturn(Optional.empty());

    // when
    Optional<HouseMemberDocument> houseMemberDocument =
        houseMemberDocumentService.updateHouseMemberDocument(newDocumentFile, MEMBER_ID);

    // then
    assertFalse(houseMemberDocument.isPresent());
    verify(houseMemberRepository).findByMemberId(MEMBER_ID);
    verify(houseMemberDocumentRepository, never()).save(any());
    verify(houseMemberRepository, never()).save(any());
  }

  /**
   * Verifies that a too-large file cannot be saved as a house member document, ensuring
   * that the file size constraint is enforced. It checks if the document repository
   * is not called to save the file and the member's document remains unchanged.
   */
  @Test
  void updateHouseMemberDocumentTooLargeFile() throws IOException {
    // given
    byte[] imageBytes = TestUtils.General.getImageAsByteArray(1000, 1000);
    MockMultipartFile tooLargeDocumentFile =
        new MockMultipartFile("new-test-file-name", imageBytes);
    HouseMemberDocument savedDocument =
        new HouseMemberDocument(String.format("member_%s_document.jpg", MEMBER_ID), imageBytes);
    HouseMember testMember = new HouseMember(MEMBER_ID, MEMBER_DOCUMENT, MEMBER_NAME, null);

    given(houseMemberRepository.findByMemberId(MEMBER_ID))
        .willReturn(Optional.of(testMember));
    given(houseMemberDocumentRepository.save(savedDocument))
        .willReturn(savedDocument);
    // when
    Optional<HouseMemberDocument> houseMemberDocument =
        houseMemberDocumentService.updateHouseMemberDocument(tooLargeDocumentFile, MEMBER_ID);

    // then
    assertFalse(houseMemberDocument.isPresent());
    assertEquals(testMember.getHouseMemberDocument(), MEMBER_DOCUMENT);
    verify(houseMemberRepository).findByMemberId(MEMBER_ID);
    verify(houseMemberDocumentRepository, never()).save(any());
    verify(houseMemberRepository, never()).save(any());
  }

  /**
   * Tests the creation of a new house member document by a service, given a new file
   * and a member ID. It verifies the document is saved and the member's document is updated.
   */
  @Test
  void createHouseMemberDocumentSuccess() throws IOException {
    // given
    byte[] imageBytes = TestUtils.General.getImageAsByteArray(10, 10);
    HouseMemberDocument savedDocument =
        new HouseMemberDocument(String.format("member_%s_document.jpg", MEMBER_ID), imageBytes);
    MockMultipartFile newDocumentFile = new MockMultipartFile("new-test-file-name", imageBytes);
    HouseMember testMember = new HouseMember(MEMBER_ID, MEMBER_DOCUMENT, MEMBER_NAME, null);

    given(houseMemberRepository.findByMemberId(MEMBER_ID))
        .willReturn(Optional.of(testMember));
    given(houseMemberDocumentRepository.save(savedDocument))
        .willReturn(savedDocument);
    // when
    Optional<HouseMemberDocument> houseMemberDocument =
        houseMemberDocumentService.createHouseMemberDocument(newDocumentFile, MEMBER_ID);

    // then
    assertTrue(houseMemberDocument.isPresent());
    assertNotEquals(testMember.getHouseMemberDocument().getDocumentFilename(),
        MEMBER_DOCUMENT.getDocumentFilename());
    verify(houseMemberRepository).findByMemberId(MEMBER_ID);
    verify(houseMemberDocumentRepository).save(savedDocument);
    verify(houseMemberRepository).save(testMember);
  }

  /**
   * Tests the creation of a house member document when a member with the specified ID
   * does not exist in the database. It verifies that a new document is not created and
   * that the repository is not saved.
   */
  @Test
  void createHouseMemberDocumentMemberNotExists() throws IOException {
    // given
    byte[] imageBytes = TestUtils.General.getImageAsByteArray(10, 10);
    MockMultipartFile newDocumentFile = new MockMultipartFile("new-test-file-name", imageBytes);

    given(houseMemberRepository.findByMemberId(MEMBER_ID))
        .willReturn(Optional.empty());
    // when
    Optional<HouseMemberDocument> houseMemberDocument =
        houseMemberDocumentService.createHouseMemberDocument(newDocumentFile, MEMBER_ID);

    // then
    assertFalse(houseMemberDocument.isPresent());
    verify(houseMemberRepository).findByMemberId(MEMBER_ID);
    verify(houseMemberDocumentRepository, never()).save(any());
    verify(houseMemberRepository, never()).save(any());
  }

  /**
   * Tests the creation of a house member document with a file that exceeds the maximum
   * allowed size. It verifies that the document is not created and the repository is
   * not saved.
   */
  @Test
  void createHouseMemberDocumentTooLargeFile() throws IOException {
    // given
    byte[] imageBytes = TestUtils.General.getImageAsByteArray(1000, 1000);
    MockMultipartFile tooLargeDocumentFile =
        new MockMultipartFile("new-test-file-name", imageBytes);
    HouseMember testMember = new HouseMember(MEMBER_ID, MEMBER_DOCUMENT, MEMBER_NAME, null);

    given(houseMemberRepository.findByMemberId(MEMBER_ID))
        .willReturn(Optional.of(testMember));
    // when
    Optional<HouseMemberDocument> houseMemberDocument =
        houseMemberDocumentService.createHouseMemberDocument(tooLargeDocumentFile, MEMBER_ID);

    // then
    assertFalse(houseMemberDocument.isPresent());
    assertEquals(testMember.getHouseMemberDocument(), MEMBER_DOCUMENT);
    verify(houseMemberRepository).findByMemberId(MEMBER_ID);
    verify(houseMemberDocumentRepository, never()).save(any());
    verify(houseMemberRepository, never()).save(any());
  }
}
