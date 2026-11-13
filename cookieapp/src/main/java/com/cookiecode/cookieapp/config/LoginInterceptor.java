package com.cookiecode.cookieapp.config;

import com.cookiecode.cookieapp.dto.PublicUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Runs before every route listed in WebConfig: no user in session → /login, /admin/** → ADMIN only.
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static final String REDIRECT_KEY = "redirectAfterLogin";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        PublicUser user = session == null ? null : (PublicUser) session.getAttribute(PublicUser.SESSION_KEY);
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (user == null) {
            // Only a GET can be replayed after login, not a form submission
            if ("GET".equals(request.getMethod())) {
                String query = request.getQueryString();
                request.getSession().setAttribute(REDIRECT_KEY, query == null ? path : path + "?" + query);
            }
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        if (path.startsWith("/admin") && !user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
