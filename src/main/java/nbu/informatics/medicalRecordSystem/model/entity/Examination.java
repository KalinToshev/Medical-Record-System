package nbu.informatics.medicalRecordSystem.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nbu.informatics.medicalRecordSystem.model.role.PaidBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "examinations")
@Getter
@Setter
@NoArgsConstructor
public class Examination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "diagnosis_id")
    private Diagnosis diagnosis;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaidBy paidBy;

    @OneToOne(mappedBy = "examination", cascade = CascadeType.ALL, orphanRemoval = true)
    private SickLeave sickLeave;
}
