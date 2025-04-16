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

package com.myhome.controllers.exceptionhandler;

import java.io.IOException;
import java.util.HashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Handles exceptions related to file uploads, providing custom error responses for
 * MaxUploadSizeExceededException and IOException. It returns HTTP status codes and
 * error messages to the client. The class uses Spring's @ControllerAdvice annotation
 * to globally handle exceptions.
 */
@ControllerAdvice
public class FileUploadExceptionAdvice {

  /**
   * Handles MaxUploadSizeExceededException exceptions by returning a HTTP response
   * with a 414 status code and a JSON body containing an error message. The error
   * message indicates that the file size exceeds the specified limit. It provides a
   * clear error response to the client.
   *
   * @param exc MaxUploadSizeExceededException thrown by the server, providing information
   * about the file size limit exceeded.
   *
   * @returns a ResponseEntity with a status code of PAYLOAD_TOO_LARGE and a JSON body
   * containing a message.
   */
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity handleMaxSizeException(MaxUploadSizeExceededException exc) {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new HashMap<String, String>() {{
      put("message", "File size exceeds limit!");
    }});
  }

  /**
   * Handles IOException exceptions by returning a 409 Conflict HTTP response with a
   * message indicating a problem with document saving.
   *
   * @param exc MaxUploadSizeExceededException exception that was thrown when the maximum
   * upload size was exceeded.
   *
   * @returns a ResponseEntity with a status of 409 Conflict and a HashMap containing
   * a message.
   */
  @ExceptionHandler(IOException.class)
  public ResponseEntity handleIOException(MaxUploadSizeExceededException exc) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new HashMap<String, String>() {{
      put("message", "Something go wrong with document saving!");
    }});
  }
}

