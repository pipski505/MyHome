package com.myhome.utils;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Provides a simple data model for representing information about a page of data,
 * such as the current page number, total pages, and total elements.
 */
@EqualsAndHashCode
@ToString
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PageInfo {
  private final int currentPage;
  private final int pageLimit;
  private final int totalPages;
  private final long totalElements;

  /**
   * Constructs a PageInfo object from the given Pageable and Page objects. It extracts
   * the page number, page size, total pages, and total elements from the input objects
   * and uses them to initialize the PageInfo object.
   *
   * @param pageable persistence layer's pagination information, such as the current
   * page number and size.
   *
   * @param page pooled result of a database query or other data retrieval operation.
   *
   * @returns a PageInfo object containing page number, page size, total pages, and
   * total elements.
   */
  public static PageInfo of(Pageable pageable, Page<?> page) {
    return new PageInfo(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        page.getTotalPages(),
        page.getTotalElements()
    );
  }
}
