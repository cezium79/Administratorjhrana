package com.administratorjhrana.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;
    private String notes;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "resolved_at")
    private String resolvedAt;

    // Новые поля из JSON
    private String timestamp;
    @Column(name = "incident_type")
    private String incidentType;
    private String description;
    @Column(name = "photo_path")
    private String photoPath;

    // Связь с Round (может быть null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;

    // Связь с Violation (может быть null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "violation_id")
    private Violation violation;
}