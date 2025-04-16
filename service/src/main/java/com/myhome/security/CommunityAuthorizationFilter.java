package com.myhome.security;

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
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enforces authorization for community administrators by validating user permissions
 * in real-time. It checks if the user is an administrator for a community based on
 * the community ID in the request URI. Unauthorized requests are responded with a
 * 401 status code.
 */
public class CommunityAuthorizationFilter extends BasicAuthenticationFilter {
    private final CommunityService communityService;
    private final String uuidPattern = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private final Pattern addAdminRequestPattern = Pattern.compile("/communities/" + uuidPattern + "/admins");


    public CommunityAuthorizationFilter(AuthenticationManager authenticationManager,
                                        CommunityService communityService) {
        super(authenticationManager);
        this.communityService = communityService;
    }

    /**
     * Checks if the current request matches a specific admin URL pattern and if the user
     * is a community admin. If the request matches the pattern and the user is not a
     * community admin, it sets the response status to 401 Unauthorized. Otherwise, it
     * proceeds with the filter chain.
     *
     * @param request HTTP request being filtered, providing access to information such
     * as the request URI.
     *
     * Extract the `request.getRequestURI()` and destructure it as follows:
     *
     * - `request.getRequestURI()` returns the part of a URL that identifies the resource
     * on the server.
     * - It is a string that represents the path to the requested resource.
     *
     * @param response HTTP response object that is used to set the status of the response
     * in case of unauthorized access.
     *
     * Set the status code of the response.
     *
     * @param chain chain of filters that the request will pass through after this filter
     * has completed its processing.
     *
     * Pass chain; chain is an instance of FilterChain, an interface that represents a
     * chain of filters.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        Matcher urlMatcher = addAdminRequestPattern.matcher(request.getRequestURI());

        if (urlMatcher.find() && !isUserCommunityAdmin(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        super.doFilterInternal(request, response, chain);
    }

    /**
     * Determines whether a user is a community admin based on their ID and the community
     * ID extracted from the request URI. It checks the community service for admins and
     * returns true if the user is found among them. Otherwise, it returns false.
     *
     * @param request HTTP request that contains information about the current user's
     * community ID.
     *
     * Extract the `request` object into its constituent parts, revealing its properties.
     * The `request` object is an instance of `HttpServletRequest` and contains the
     * following main properties:
     *
     * - `requestURI`: the part of the URL after the domain name
     * - `contextPath`: the root path of the web application
     * - `servletPath`: the path of the servlet that handles the request
     * - `pathInfo`: any additional path information after the servlet path
     * - `query`: the query string of the URL
     * - `headers`: a collection of HTTP headers sent with the request
     * - `session`: the HTTP session associated with the request
     * - `parameterMap`: a map of request parameters
     *
     * @returns a boolean indicating whether the user is a community admin or not.
     */
    private boolean isUserCommunityAdmin(HttpServletRequest request) {
        String userId = (String) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        String communityId = request
                .getRequestURI().split("/")[2];
        Optional<List<User>> optional = communityService
                .findCommunityAdminsById(communityId, null);

        if (optional.isPresent()) {
            List<User> communityAdmins = optional.get();
            User admin = communityAdmins
                    .stream()
                    .filter(communityAdmin -> communityAdmin.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);

            return admin != null;
        }

        return false;
    }
}