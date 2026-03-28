package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.dto.auth.AssignRoleRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.auth.RegisterRequestDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.model.entity.Speciality;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import nbu.informatics.medicalRecordSystem.model.role.Role;
import nbu.informatics.medicalRecordSystem.repository.DoctorRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import nbu.informatics.medicalRecordSystem.repository.SpecialityRepository;
import nbu.informatics.medicalRecordSystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialityRepository specialityRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Потребителското име вече е заето");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.PENDING);
        userRepository.save(user);
    }

    public List<User> findPendingUsers() {
        return userRepository.findByRole(Role.PENDING);
    }

    @Transactional
    public void assignRole(AssignRoleRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Потребителят не е намерен"));

        user.setRole(dto.getRole());
        userRepository.save(user);

        if (dto.getRole() == Role.PATIENT) {
            Doctor gp = doctorRepository.findById(dto.getGpId())
                    .orElseThrow(() -> new EntityNotFoundException("Личният лекар не е намерен"));
            Patient patient = new Patient();
            patient.setName(dto.getName());
            patient.setEgn(dto.getEgn());
            patient.setGp(gp);
            patient.setUser(user);
            patientRepository.save(patient);

        } else if (dto.getRole() == Role.DOCTOR) {
            Speciality speciality = specialityRepository.findById(dto.getSpecialityId())
                    .orElseThrow(() -> new EntityNotFoundException("Специалността не е намерена"));
            Doctor doctor = new Doctor();
            doctor.setName(dto.getName());
            doctor.setGp(dto.isGp());
            doctor.setSpeciality(speciality);
            doctor.setUser(user);
            doctorRepository.save(doctor);
        }
    }
}
