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

import com.myhome.domain.HouseMember;
import com.myhome.domain.HouseMemberDocument;
import com.myhome.repositories.HouseMemberDocumentRepository;
import com.myhome.repositories.HouseMemberRepository;
import com.myhome.services.HouseMemberDocumentService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

/**
 * Provides data access and manipulation services for house member documents, utilizing
 * Spring Data JPA repositories for persistence and image compression for file size
 * management.
 */
@Service
public class HouseMemberDocumentSDJpaService implements HouseMemberDocumentService {

  private final HouseMemberRepository houseMemberRepository;
  private final HouseMemberDocumentRepository houseMemberDocumentRepository;
  @Value("${files.compressionBorderSizeKBytes}")
  private int compressionBorderSizeKBytes;
  @Value("${files.maxSizeKBytes}")
  private int maxFileSizeKBytes;
  @Value("${files.compressedImageQuality}")
  private float compressedImageQuality;

  public HouseMemberDocumentSDJpaService(HouseMemberRepository houseMemberRepository,
      HouseMemberDocumentRepository houseMemberDocumentRepository) {
    this.houseMemberRepository = houseMemberRepository;
    this.houseMemberDocumentRepository = houseMemberDocumentRepository;
  }

  /**
   * Locates a HouseMemberDocument associated with a specified member ID by retrieving
   * the HouseMember from the repository and then extracting its HouseMemberDocument.
   *
   * @param memberId identifier used to locate a house member document in the database.
   *
   * @returns an Optional containing a HouseMemberDocument, or an empty Optional if not
   * found.
   */
  @Override
  public Optional<HouseMemberDocument> findHouseMemberDocument(String memberId) {
    return houseMemberRepository.findByMemberId(memberId)
        .map(HouseMember::getHouseMemberDocument);
  }

  /**
   * Deletes a house member document associated with a given member ID by updating the
   * corresponding member entity and saving the changes to the repository. It returns
   * true if the document is deleted successfully, false otherwise.
   *
   * @param memberId unique identifier of the house member whose document is to be deleted.
   *
   * @returns a boolean value indicating whether the document was successfully deleted
   * or not.
   */
  @Override
  public boolean deleteHouseMemberDocument(String memberId) {
    return houseMemberRepository.findByMemberId(memberId).map(member -> {
      if (member.getHouseMemberDocument() != null) {
        member.setHouseMemberDocument(null);
        houseMemberRepository.save(member);
        return true;
      }
      return false;
    }).orElse(false);
  }

  /**
   * Updates a HouseMemberDocument associated with a given memberId by creating a new
   * document from a MultipartFile and adding it to the HouseMember entity.
   *
   * @param multipartFile file being uploaded for a house member, passed to the
   * `tryCreateDocument` method to create a new document.
   *
   * @param memberId identifier of the house member whose document is being updated.
   *
   * @returns an Optional containing a HouseMemberDocument if the update is successful,
   * otherwise an empty Optional.
   */
  @Override
  public Optional<HouseMemberDocument> updateHouseMemberDocument(MultipartFile multipartFile,
      String memberId) {
    return houseMemberRepository.findByMemberId(memberId).map(member -> {
      Optional<HouseMemberDocument> houseMemberDocument = tryCreateDocument(multipartFile, member);
      houseMemberDocument.ifPresent(document -> addDocumentToHouseMember(document, member));
      return houseMemberDocument;
    }).orElse(Optional.empty());
  }

  /**
   * Creates a new document based on a multipart file and a member ID, adds it to the
   * corresponding house member, and returns the document as an optional value. It
   * relies on the `houseMemberRepository` to find the member and calls `tryCreateDocument`
   * to create the document.
   *
   * @param multipartFile file to be uploaded as a HouseMemberDocument.
   *
   * @param memberId identifier of a house member for whom a document is being created.
   *
   * @returns an Optional containing a HouseMemberDocument if a document is created
   * successfully, otherwise an empty Optional.
   */
  @Override
  public Optional<HouseMemberDocument> createHouseMemberDocument(MultipartFile multipartFile,
      String memberId) {
    return houseMemberRepository.findByMemberId(memberId).map(member -> {
      Optional<HouseMemberDocument> houseMemberDocument = tryCreateDocument(multipartFile, member);
      houseMemberDocument.ifPresent(document -> addDocumentToHouseMember(document, member));
      return houseMemberDocument;
    }).orElse(Optional.empty());
  }

  /**
   * Processes a multipart file, compresses or writes it to a byte stream, checks its
   * size, and saves it as a document if it meets size requirements, returning the
   * document if successful or an empty Optional otherwise.
   *
   * @param multipartFile file uploaded by the user, which is then processed to create
   * a document.
   *
   * Destructure: MultipartFile multipartFile =
   * - size: The size of the file in bytes.
   * - getName(): Returns the original file name.
   * - getContentType(): Returns the content type of the file.
   * - getInputStream(): Returns an InputStream to read the file.
   * - getBytes() is not used here, but it returns the contents of the file as a byte
   * array.
   *
   * @param member HouseMember to which a document is being associated.
   *
   * Extract.
   * The `member` object has a `getMemberId` method that returns a member ID.
   *
   * @returns an `Optional` containing a `HouseMemberDocument` object or an empty `Optional`.
   *
   * The returned output is an `Optional<HouseMemberDocument>`, which is a container
   * class representing a value that may or may not be present. If present, it contains
   * a `HouseMemberDocument` object, which represents a document associated with a house
   * member.
   */
  private Optional<HouseMemberDocument> tryCreateDocument(MultipartFile multipartFile,
      HouseMember member) {

    try (ByteArrayOutputStream imageByteStream = new ByteArrayOutputStream()) {
      BufferedImage documentImage = getImageFromMultipartFile(multipartFile);
      if (multipartFile.getSize() < DataSize.ofKilobytes(compressionBorderSizeKBytes).toBytes()) {
        writeImageToByteStream(documentImage, imageByteStream);
      } else {
        compressImageToByteStream(documentImage, imageByteStream);
      }
      if (imageByteStream.size() < DataSize.ofKilobytes(maxFileSizeKBytes).toBytes()) {
        HouseMemberDocument houseMemberDocument = saveHouseMemberDocument(imageByteStream,
            String.format("member_%s_document.jpg", member.getMemberId()));
        return Optional.of(houseMemberDocument);
      } else {
        return Optional.empty();
      }
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  /**
   * Associates a document with a house member by updating the member's repository
   * entry, and
   * returns the updated member after saving the changes to the database,
   * utilizing the `houseMemberRepository` to persist the data.
   *
   * @param houseMemberDocument document associated with a HouseMember, which is then
   * saved to the database.
   *
   * @param member HouseMember entity to which the document is being added.
   *
   * @returns a saved HouseMember object.
   */
  private HouseMember addDocumentToHouseMember(HouseMemberDocument houseMemberDocument,
      HouseMember member) {
    member.setHouseMemberDocument(houseMemberDocument);
    return houseMemberRepository.save(member);
  }

  /**
   * Creates a new `HouseMemberDocument` instance with a filename and the contents of
   * a byte array, then persists the document to the database via the `houseMemberDocumentRepository`.
   *
   * @param imageByteStream byte array of the image to be saved as a HouseMemberDocument.
   *
   * @param filename name of the file that the `HouseMemberDocument` object is associated
   * with.
   *
   * @returns a saved HouseMemberDocument entity.
   */
  private HouseMemberDocument saveHouseMemberDocument(ByteArrayOutputStream imageByteStream,
      String filename) {
    HouseMemberDocument newDocument =
        new HouseMemberDocument(filename, imageByteStream.toByteArray());
    return houseMemberDocumentRepository.save(newDocument);
  }

  /**
   * Writes a BufferedImage to a ByteArrayOutputStream in JPG format.
   *
   * @param documentImage image to be written to the byte stream in JPEG format.
   *
   * It is a BufferedImage object.
   *
   * @param imageByteStream output stream where the image data will be written.
   */
  private void writeImageToByteStream(BufferedImage documentImage,
      ByteArrayOutputStream imageByteStream)
      throws IOException {
    ImageIO.write(documentImage, "jpg", imageByteStream);
  }

  /**
   * Compresses a BufferedImage to a byte stream in JPEG format, allowing for explicit
   * compression quality control, and writes it to a ByteArrayOutputStream. It uses the
   * ImageIO library to create an ImageWriter and ImageOutputStream.
   *
   * @param bufferedImage image to be compressed and written to the byte stream.
   *
   * Contain a 2D array of pixels,
   * Represented as pixels of type `Color`,
   * Can be created from an image file or drawn manually.
   *
   * @param imageByteStream stream to which the compressed image will be written.
   *
   * Unwrap.
   * It is an instance of `ByteArrayOutputStream`.
   */
  private void compressImageToByteStream(BufferedImage bufferedImage,
      ByteArrayOutputStream imageByteStream) throws IOException {

    try (ImageOutputStream imageOutStream = ImageIO.createImageOutputStream(imageByteStream)) {

      ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("jpg").next();
      imageWriter.setOutput(imageOutStream);
      ImageWriteParam param = imageWriter.getDefaultWriteParam();

      if (param.canWriteCompressed()) {
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(compressedImageQuality);
      }
      imageWriter.write(null, new IIOImage(bufferedImage, null, null), param);
      imageWriter.dispose();
    }
  }

  /**
   * Reads the contents of a multipart file into an InputStream, and then uses ImageIO
   * to parse the InputStream into a BufferedImage, returning the resulting image.
   *
   * @param multipartFile multimedia file being uploaded, providing access to its input
   * stream.
   *
   * @returns a BufferedImage object representing the image from the uploaded multipart
   * file.
   */
  private BufferedImage getImageFromMultipartFile(MultipartFile multipartFile) throws IOException {
    try (InputStream multipartFileStream = multipartFile.getInputStream()) {
      return ImageIO.read(multipartFileStream);
    }
  }
}
