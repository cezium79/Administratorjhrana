package com.administratorjhrana.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "violations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Violation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String description;
    private String severity;

    @Column(name = "image_urls")
    private String imageUrls;

    @Column(name = "detected_at")
    private String detectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;

    @OneToMany(mappedBy = "violation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Incident> incidents = new ArrayList<>();

    public void addIncident(Incident i) {
        incidents.add(i);
        i.setViolation(this);
    }

    public void setType(String sequenceBreach) {
    }

    public void setDescription(String s) {
    }

    public void setSeverity(String high) {

    }
}