package com.cookiecode.cookieapp.dto;

import java.util.List;

import com.cookiecode.cookieapp.model.Step;
import com.cookiecode.cookieapp.model.User;

/**
 * Result of the split: the steps of each cook, the time alone and the time as a group.
 */
public record CookingPlan(List<CookTasks> cooks, int soloMinutes, int groupMinutes) {

    public record CookTasks(User cook, List<Step> steps, int minutes) {}

    public int savedMinutes() {
        return soloMinutes - groupMinutes;
    }
}