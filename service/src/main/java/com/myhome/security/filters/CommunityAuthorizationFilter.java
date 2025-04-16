package com.myhome.security.filters;

import com.myhome.domain.User;
import com.myhome.services.CommunityService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enforces authorization for community administrators by checking if the authenticated
 * user is an admin of the requested community. It filters requests to add amenities
 * to a community based on the user's role.
 */
public class CommunityAuthorizationFilter extends BasicAuthenticationFilter {
  private final CommunityService communityService;
  private static final String UUID_PATTERN =
      "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
  private static final Pattern ADD_AMENITY_REQUEST_PATTERN =
      Pattern.compile("/communities/" + UUID_PATTERN + "/amenities");

  public CommunityAuthorizationFilter(AuthenticationManager authenticationManager,
      CommunityService communityService) {
    super(authenticationManager);
    this.communityService = communityService;
  }

  /**
   * Filters incoming HTTP requests based on a URL pattern and user permissions. It
   * checks if the request URL matches a specific pattern and if the user is not a
   * community admin, it returns a 403 Forbidden response. Otherwise, it proceeds with
   * the filter chain.
   *
   * @param request HTTP request that is being filtered, providing access to information
   * about the client's request.
   *
   * Extract are:
   * - `HttpServletRequest request` is a standard Java Servlet API object.
   * - It has properties such as `getRequestURI()`, `setStatus()`, and methods like `isUserCommunityAdmin()`.
   *
   * @param response HTTP response being sent back to the client, which can be modified
   * by this function to indicate a forbidden status.
   *
   * Set, including the HTTP status code, is set to HttpServletResponse.SC_FORBIDDEN
   * when the user is not a community admin.
   *
   * @param chain sequence of filters that will be executed after this filter has
   * completed its processing.
   *
   * Pass `chain` as an object.
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    Matcher urlMatcher = ADD_AMENITY_REQUEST_PATTERN.matcher(request.getRequestURI());

    if (urlMatcher.find() && !isUserCommunityAdmin(request)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    super.doFilterInternal(request, response, chain);
  }

  /**
   * Determines if the current user is an admin of a community by comparing their ID
   * to a list of community admins. It extracts the user ID from the security context
   * and the community ID from the request URI, then checks for a match.
   *
   * @param request HTTP request from which the community ID is extracted by splitting
   * the request URI and taking the third part.
   *
   * Get the request URI, which is a string representing the path of the current request.
   * Get the request URI's components, which are an array of strings representing the
   * path components.
   *
   * @returns a boolean indicating whether the user is a community admin.
   */
  private boolean isUserCommunityAdmin(HttpServletRequest request) {
    String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String communityId = request.getRequestURI().split("/")[2];

    return communityService.findCommunityAdminsById(communityId, null)
        .flatMap(admins -> admins.stream()
            .map(User::getUserId)
            .filter(userId::equals)
            .findFirst()
        )
        .isPresent();
  }
}
