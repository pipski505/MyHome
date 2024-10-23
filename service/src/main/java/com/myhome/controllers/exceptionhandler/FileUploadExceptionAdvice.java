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
 * Handles exceptions related to file uploads.
 */
@ControllerAdvice
public class FileUploadExceptionAdvice {

  /**
   * Handles MaxUploadSizeExceededException exceptions,
   * returns a ResponseEntity with a 414 status code and a JSON body containing an error
   * message.
   *
   * @param exc exception thrown when the file size exceeds the maximum allowed size.
   *
   * @returns a ResponseEntity with a 414 status code and a JSON body containing a message.
   */
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity handleMaxSizeException(MaxUploadSizeExceededException exc) {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new HashMap<String, String>() {{
      put("message", "File size exceeds limit!");
    }});
  }

  /**
   * Handles IOException exceptions, specifically MaxUploadSizeExceededException, and
   * returns a ResponseEntity with a status code of HttpStatus.CONFLICT and a JSON body
   * containing an error message.
   *
   * @param exc exception that triggered the exception handler, specifically a `MaxUploadSizeExceededException`.
   *
   * @returns a ResponseEntity with HTTP status 409 and a JSON body containing a "message".
   */
  @ExceptionHandler(IOException.class)
  public ResponseEntity handleIOException(MaxUploadSizeExceededException exc) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new HashMap<String, String>() {{
      put("message", "Something go wrong with document saving!");
    }});
  }
}

