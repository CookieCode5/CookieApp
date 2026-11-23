package com.cookiecode.cookieapp.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A group cooking one recipe together: the recipe steps are split between the organizer and the participants.
 */
@Getter
@Setter
@Entity
@ToString
public class Workshop {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String title;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // RG-W3: secret code of the invitation link, the only way to join a group
    @Column(name = "invite_code", unique = true, updatable = false)
    private String inviteCode = UUID.randomUUID().toString();

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    @ToString.Exclude
    private Recipe recipe;

    // The user who created the group, always the first cook of the team
    @ManyToOne
    @JoinColumn(name = "organizer_id")
    @ToString.Exclude
    private User organizer;

    // N-N: users who joined the group
    @ManyToMany
    @JoinTable(name = "workshop_participant",
            joinColumns = @JoinColumn(name = "workshop_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @ToString.Exclude
    private Set<User> participants = new HashSet<>();

    // Everyone who cooks: the organizer first, then the participants by username (stable order for the split)
    public List<User> team() {
        List<User> team = new ArrayList<>();
        team.add(organizer);
        participants.stream()
                .sorted(Comparator.comparing(User::getUsername))
                .forEach(team::add);
        return team;
    }

    // RG-W1: no more cooks than steps, otherwise someone would have nothing to do
    public int maxTeamSize() {
        return Math.max(1, recipe.getSteps().size());
    }

    public boolean isFull() {
        return team().size() >= maxTeamSize();
    }
}