package com.administratorjhrana.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rounds")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Round {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer roundNumber;

    @Column(name = "location")
    private String location;

    @Column(name = "start_time")
    private String startTime; // Храним как String или LocalDateTime, зависит от парсинга

    @Column(name = "end_time")
    private String endTime;

    @Column(name = "file_path")
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Violation> violations = new ArrayList<>();

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CheckpointLog> logs = new ArrayList<>();
    public void addViolation(Violation violation) {
        // если у вас есть список нарушений, например:
        this.violations.add(violation);
    }
    public void addLog(CheckpointLog log) {
        if (log != null) {
            logs.add(log);
            log.setRound(this);  // устанавливаем обратную ссылку
        }


}
}