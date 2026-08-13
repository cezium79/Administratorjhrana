package com.administratorjhrana.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "checkpoint_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckpointLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "checkpoint_id")
    private String checkpointId;

    @Column(name = "checkpoint_name")
    private String checkpointName;

    private LocalDateTime timestamp;

    @Column(name = "route_name")
    private String routeName;

    @Column(name = "sequence_index")
    private Integer sequenceIndex;

    @Column(name = "is_sequence_correct")
    private Boolean isSequenceCorrect;

    private String scanType;
    private String actionType;

    @Column(name = "sequence_error_type")
    private String sequenceErrorType;

    private String inputValue;
    private String photoPath;
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;
}