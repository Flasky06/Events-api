package com.tritva.Evently.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String county;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organiser_id", nullable = false)
    private User organiser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Total capacity/seats for this event
     */
    @Column(nullable = false)
    private int capacity;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Ticket> tickets = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Calculate available tickets (capacity - tickets sold)
     * @return number of available tickets
     */
    @Transient
    public int getAvailableTickets() {
        return capacity - tickets.size();
    }

    /**
     * Check if tickets are available
     * @return true if tickets are still available
     */
    @Transient
    public boolean hasAvailableTickets() {
        return getAvailableTickets() > 0;
    }

    /**
     * Check if event is sold out
     * @return true if no tickets available
     */
    @Transient
    public boolean isSoldOut() {
        return getAvailableTickets() <= 0;
    }
}