package com.cookiecode.cookieapp.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cookiecode.cookieapp.dto.CookingPlan;
import com.cookiecode.cookieapp.dto.CookingPlan.CookTasks;
import com.cookiecode.cookieapp.dto.WorkshopForm;
import com.cookiecode.cookieapp.exception.BusinessException;
import com.cookiecode.cookieapp.model.Step;
import com.cookiecode.cookieapp.model.User;
import com.cookiecode.cookieapp.model.Workshop;
import com.cookiecode.cookieapp.repository.RecipeRepository;
import com.cookiecode.cookieapp.repository.UserRepository;
import com.cookiecode.cookieapp.repository.WorkshopRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkshopService {

    private final WorkshopRepository workshopRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Workshop> all() {
        return workshopRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Workshop get(Long id) {
        return workshopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workshop not found"));
    }

    @Transactional
    public Workshop create(WorkshopForm form, Long organizerId) {
        Workshop w = new Workshop();
        w.setRecipe(recipeRepository.findById(form.getRecipeId())
                .orElseThrow(() -> new BusinessException("recipeId", "Recette introuvable.")));
        w.setOrganizer(userRepository.getReferenceById(organizerId));
        w.setTitle(form.getTitle().trim());
        return workshopRepository.save(w);
    }

    // RG-W1: not your own group, not twice, and no more cooks than steps
    @Transactional
    public void join(Long workshopId, Long userId) {
        Workshop w = get(workshopId);
        if (w.getOrganizer().getId() == userId) {
            throw new BusinessException("Vous organisez cet atelier : vous en faites déjà partie.");
        }
        if (isParticipant(w, userId)) {
            throw new BusinessException("Vous faites déjà partie de cet atelier.");
        }
        if (w.isFull()) {
            throw new BusinessException("L'équipe est complète : la recette n'a que "
                    + w.maxTeamSize() + " étape(s) à se répartir.");
        }
        w.getParticipants().add(userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @Transactional
    public void leave(Long workshopId, Long userId) {
        if (!get(workshopId).getParticipants().removeIf(p -> p.getId() == userId)) {
            throw new BusinessException("Vous ne faites pas partie de cet atelier.");
        }
    }

    public boolean isParticipant(Workshop w, Long userId) {
        return w.getParticipants().stream().anyMatch(p -> p.getId() == userId);
    }

    /**
     * RG-W2: split the recipe steps between the cooks.
     * Steps are read in recipe order. A sequential step goes to the cook with the least minutes so far.
     * Consecutive "parallelizable" steps form a block done at the same time: inside the block, each step goes
     * to the cook with the least minutes in the block (then the least minutes overall).
     * Group time = sum of the sequential steps + longest share of each parallel block.
     */
    @Transactional(readOnly = true)
    public CookingPlan plan(Workshop w) {
        List<User> team = w.team();
        Map<User, List<Step>> tasks = new LinkedHashMap<>();
        Map<User, Integer> load = new HashMap<>();
        for (User cook : team) {
            tasks.put(cook, new ArrayList<>());
            load.put(cook, 0);
        }

        List<Step> steps = w.getRecipe().getSteps();
        int solo = 0;
        int group = 0;
        int i = 0;
        while (i < steps.size()) {
            Step step = steps.get(i);
            if (!step.isParallelizable()) {
                assign(leastBusy(team, load, Map.of()), step, tasks, load);
                solo += step.getDurationMinutes();
                group += step.getDurationMinutes();
                i++;
            } else {
                Map<User, Integer> blockLoad = new HashMap<>();
                while (i < steps.size() && steps.get(i).isParallelizable()) {
                    Step parallel = steps.get(i);
                    User cook = leastBusy(team, load, blockLoad);
                    assign(cook, parallel, tasks, load);
                    blockLoad.merge(cook, parallel.getDurationMinutes(), Integer::sum);
                    solo += parallel.getDurationMinutes();
                    i++;
                }
                group += blockLoad.values().stream().max(Integer::compare).orElse(0);
            }
        }

        List<CookTasks> cooks = team.stream()
                .map(cook -> new CookTasks(cook, tasks.get(cook), load.get(cook)))
                .toList();
        return new CookingPlan(cooks, solo, group);
    }

    // Ids of the groups the user belongs to (organizer or participant), for the list page
    @Transactional(readOnly = true)
    public Set<Long> myWorkshopIds(Long userId) {
        return all().stream()
                .filter(w -> w.getOrganizer().getId() == userId || isParticipant(w, userId))
                .map(Workshop::getId)
                .collect(Collectors.toSet());
    }

    // Least busy cook: least minutes in the current parallel block, then least minutes overall, then team order
    private static User leastBusy(List<User> team, Map<User, Integer> load, Map<User, Integer> blockLoad) {
        return team.stream()
                .min(Comparator.comparingInt((User cook) -> blockLoad.getOrDefault(cook, 0))
                        .thenComparingInt(load::get))
                .orElseThrow();
    }

    private static void assign(User cook, Step step, Map<User, List<Step>> tasks, Map<User, Integer> load) {
        tasks.get(cook).add(step);
        load.merge(cook, step.getDurationMinutes(), Integer::sum);
    }
}