package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.dto.patient.PatientCreateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.patient.PatientResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.patient.PatientUpdateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import nbu.informatics.medicalRecordSystem.repository.DoctorRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import nbu.informatics.medicalRecordSystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public List<PatientResponseDTO> findAll() {
        return patientRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PatientResponseDTO findById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пациент с id " + id + " не е намерен"));
        return toResponseDTO(patient);
    }

    public void create(PatientCreateRequestDTO dto) {
        Doctor gp = doctorRepository.findById(dto.getGpId())
                .orElseThrow(() -> new EntityNotFoundException("Личният лекар не е намерен"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Потребителят не е намерен"));

        Patient patient = new Patient();
        patient.setName(dto.getName());
        patient.setEgn(dto.getEgn());
        patient.setGp(gp);
        patient.setUser(user);

        patientRepository.save(patient);
    }

    public void update(Long id, PatientUpdateRequestDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пациент с id " + id + " не е намерен"));

        Doctor gp = doctorRepository.findById(dto.getGpId())
                .orElseThrow(() -> new EntityNotFoundException("Личният лекар не е намерен"));

        patient.setName(dto.getName());
        patient.setEgn(dto.getEgn());
        patient.setGp(gp);

        patientRepository.save(patient);
    }

    public void delete(Long id) {
        patientRepository.deleteById(id);
    }

    private PatientResponseDTO toResponseDTO(Patient patient) {
        return new PatientResponseDTO(
                patient.getId(),
                patient.getName(),
                patient.getEgn(),
                patient.getGp().getId(),
                patient.getGp().getName(),
                patient.getUser().getUsername()
        );
    }
}
