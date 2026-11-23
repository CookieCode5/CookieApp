package com.cookiecode.cookieapp.service;

import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.model.Recipe;
import com.cookiecode.cookieapp.model.Step;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.Workshop;
import com.cookiecode.cookieapp.repository.RecipeRepository;
import com.cookiecode.cookieapp.repository.UserRepository;
import com.cookiecode.cookieapp.repository.WorkshopRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// RG-W1 and RG-W3: private groups, joined only through the invitation link, deleted only by the organizer
@ExtendWith(MockitoExtension.class)
class WorkshopServiceTest {

    @Mock private WorkshopRepository workshopRepository;
    @Mock private RecipeRepository recipeRepository;
    @Mock private UserRepository userRepository;

    private WorkshopService service;
    private Workshop workshop;
    private final User organizer = user(1L);
    private final User guest = user(2L);

    @BeforeEach
    void setUp() {
        service = new WorkshopService(workshopRepository, recipeRepository, userRepository);
        Recipe recipe = new Recipe();
        recipe.getSteps().add(new Step());
        recipe.getSteps().add(new Step());   // 2 steps -> 2 cooks at most
        workshop = new Workshop();
        workshop.setId(10L);
        workshop.setRecipe(recipe);
        workshop.setOrganizer(organizer);
    }

    @Test
    void aGeneratedInviteCodeIsUniquePerWorkshop() {
        assertNotNull(workshop.getInviteCode());
        assertNotEquals(workshop.getInviteCode(), new Workshop().getInviteCode());
    }

    @Test
    void aNonMemberGets404OnTheGroupPage() {
        when(workshopRepository.findById(10L)).thenReturn(Optional.of(workshop));
        assertThrows(ResponseStatusException.class, () -> service.getForMember(10L, guest.getId()));
        assertSame(workshop, service.getForMember(10L, organizer.getId()));
    }

    @Test
    void theInvitationLinkAddsTheGuestUntilTheTeamIsFull() {
        String code = workshop.getInviteCode();
        when(workshopRepository.findByInviteCode(code)).thenReturn(Optional.of(workshop));
        when(userRepository.findById(2L)).thenReturn(Optional.of(guest));

        service.join(code, guest.getId());
        assertTrue(service.isMember(workshop, guest.getId()));

        // organizer + guest = 2 cooks = 2 steps: a third one is refused
        assertThrows(BusinessException.class, () -> service.join(code, 3L));
        // joining twice or your own group is refused too
        assertThrows(BusinessException.class, () -> service.join(code, guest.getId()));
        assertThrows(BusinessException.class, () -> service.join(code, organizer.getId()));
    }

    @Test
    void aWrongInviteCodeGives404() {
        when(workshopRepository.findByInviteCode("wrong")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.join("wrong", guest.getId()));
    }

    @Test
    void onlyTheOrganizerCanDelete() {
        when(workshopRepository.findById(10L)).thenReturn(Optional.of(workshop));

        assertThrows(BusinessException.class, () -> service.delete(10L, guest.getId()));
        verify(workshopRepository, never()).delete(any());

        service.delete(10L, organizer.getId());
        verify(workshopRepository).delete(workshop);
    }

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user" + id);
        return u;
    }
}
