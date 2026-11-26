package com.cookiecode.cookieapp.config;

import com.cookiecode.cookieapp.dto.PublicUser;
import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.repository.RecipeRepository;
import com.cookiecode.cookieapp.service.SubscriptionService;
import com.cookiecode.cookieapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Runs before GET /recipes/{id}: a premium recipe is only shown to accounts allowed by
 * SubscriptionService.canView. Visitors go to /login, free accounts go to /offers.
 */
@Component
@RequiredArgsConstructor
public class PremiumRecipeInterceptor implements HandlerInterceptor {

    private final RecipeRepository recipeRepository;
    private final UserService userService;
    private final SubscriptionService subscriptionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String id = path.substring(path.lastIndexOf('/') + 1);
        // /recipes/new and other words are not recipe pages
        if (!"GET".equals(request.getMethod()) || !id.matches("\\d+")) {
            return true;
        }
        Recipe recipe = recipeRepository.findById(Long.valueOf(id)).orElse(null);
        // Unknown id: the controller answers with its own 404 page
        if (recipe == null) {
            return true;
        }

        HttpSession session = request.getSession(false);
        PublicUser me = session == null ? null : (PublicUser) session.getAttribute(PublicUser.SESSION_KEY);
        User user = me == null ? null : userService.findById(me.getId());
        if (subscriptionService.canView(user, recipe)) {
            return true;
        }

        if (me == null) {
            request.getSession().setAttribute(LoginInterceptor.REDIRECT_KEY, path);
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        FlashMap flash = RequestContextUtils.getOutputFlashMap(request);
        flash.put("error", "Cette recette est réservée aux comptes Premium.");
        RequestContextUtils.saveOutputFlashMap("/offers", request, response);
        response.sendRedirect(request.getContextPath() + "/offers");
        return false;
    }
}
