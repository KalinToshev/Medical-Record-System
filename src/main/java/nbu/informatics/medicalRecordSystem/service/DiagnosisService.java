package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.mapper.DiagnosisMapper;
import nbu.informatics.medicalRecordSystem.model.dto.diagnosis.DiagnosisRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.diagnosis.DiagnosisResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Diagnosis;
import nbu.informatics.medicalRecordSystem.repository.DiagnosisRepository;
import nbu.informatics.medicalRecordSystem.repository.ExaminationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final ExaminationRepository examinationRepository;
    private final DiagnosisMapper diagnosisMapper;

    public List<DiagnosisResponseDTO> findAll() {
        return diagnosisRepository.findAll()
                .stream()
                .map(diagnosisMapper::toDto)
                .toList();
    }

    public DiagnosisResponseDTO findById(Long id) {
        Diagnosis diagnosis = diagnosisRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Диагноза с id " + id + " не е намерена"));
        return diagnosisMapper.toDto(diagnosis);
    }

    @Transactional
    public void create(DiagnosisRequestDTO diagnosisRequestDTO) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setName(diagnosisRequestDTO.getName());

        diagnosisRepository.save(diagnosis);
    }

    @Transactional
    public void update(Long id, DiagnosisRequestDTO diagnosisRequestDTO) {
        Diagnosis diagnosis = diagnosisRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Диагноза с id " + id + " не е намерена"));

        diagnosis.setName(diagnosisRequestDTO.getName());

        diagnosisRepository.save(diagnosis);
    }

    @Transactional
    public void delete(Long id) {
        long linkedExaminations = examinationRepository.countByDiagnosisId(id);
        if (linkedExaminations > 0) {
            throw new IllegalStateException(
                    "Диагнозата не може да бъде изтрита, защото има "
                            + linkedExaminations + " обвързани преглед(а)"
            );
        }
        diagnosisRepository.deleteById(id);
    }
}
