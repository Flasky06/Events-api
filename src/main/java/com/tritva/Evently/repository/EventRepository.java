package com.tritva.Evently.repository;

import com.tritva.Evently.model.entity.Category;
import com.tritva.Evently.model.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    // Find all events by organiser
    List<Event> findByOrganiser(User organiser);

    // Find events by category
    List<Event> findByCategory(Category category);

    // Search by name (case-insensitive)
    List<Event> findByNameContainingIgnoreCase(String name);

    // Find events by county
    List<Event> findByCountyIgnoreCase(String county);
}
