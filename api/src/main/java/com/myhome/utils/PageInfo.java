package com.myhome.utils;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Is a data structure that encapsulates information about a page of results in a
 * larger dataset. It contains the current page being viewed, the maximum number of
 * pages allowed, and the total number of pages and elements in the dataset. The class
 * also provides a method for creating a new instance of PageInfo from a Pageable
 * object and its associated page.
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
   * Generates a `PageInfo` object that contains information about the number of pages
   * and elements in a paginated collection, given a `Pageable` object and an instance
   * of the page type.
   * 
   * @param pageable pagination information for the data being processed, providing the
   * page number, page size, total pages, and total elements.
   * 
   * @param page current page being processed, providing information on its position
   * and size within the overall dataset.
   * 
   * @returns a `PageInfo` object containing page number, page size, total pages, and
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
