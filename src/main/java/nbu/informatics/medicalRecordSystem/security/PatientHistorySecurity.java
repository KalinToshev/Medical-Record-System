package nbu.informatics.medicalRecordSystem.security;

import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.model.role.Role;
import nbu.informatics.medicalRecordSystem.model.security.UserDetailsImpl;
import nbu.informatics.medicalRecordSystem.repository.DoctorRepository;
import nbu.informatics.medicalRecordSystem.repository.ExaminationRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("patientHistorySecurity")
@RequiredArgsConstructor
public class PatientHistorySecurity {

    private final ExaminationRepository examinationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public boolean canViewHistory(Long patientId, UserDetailsImpl principal) {
        if (principal == null || patientId == null) return false;
        Role role = principal.getUser().getRole();

        if (role == Role.ADMIN) return true;

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) return false;

        if (role == Role.PATIENT) {
            return patient.getUser().getId().equals(principal.getUser().getId());
        }

        if (role == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUser(principal.getUser()).orElse(null);
            if (doctor == null) return false;
            return examinationRepository.existsByDoctorAndPatient(doctor, patient);
        }

        return false;
    }
}
