package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.dto.doctor.DoctorCreateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.doctor.DoctorResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.doctor.DoctorUpdateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Speciality;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import nbu.informatics.medicalRecordSystem.repository.DoctorRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import nbu.informatics.medicalRecordSystem.repository.SpecialityRepository;
import nbu.informatics.medicalRecordSystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SpecialityRepository specialityRepository;
    private final UserRepository userRepository;

    public List<DoctorResponseDTO> findAll() {
        return doctorRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public DoctorResponseDTO findById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Лекар с id " + id + " не е намерен"));
        return toResponseDTO(doctor);
    }

    public void create(DoctorCreateRequestDTO dto) {
        Speciality speciality = specialityRepository.findById(dto.getSpecialityId())
                .orElseThrow(() -> new EntityNotFoundException("Специалността не е намерена"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Потребителят не е намерен"));

        Doctor doctor = new Doctor();
        doctor.setName(dto.getName());
        doctor.setGp(dto.isGp());
        doctor.setSpeciality(speciality);
        doctor.setUser(user);

        doctorRepository.save(doctor);
    }

    public void update(Long id, DoctorUpdateRequestDTO dto) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Лекар с id " + id + " не е намерен"));

        if (doctor.isGp() && !dto.isGp()) {
            long linkedPatients = patientRepository.countByGpId(id);
            if (linkedPatients > 0) {
                throw new IllegalStateException(
                        "Лекарят не може да бъде премахнат като личен, защото има "
                                + linkedPatients + " обвързани пациента(и)"
                );
            }
        }

        Speciality speciality = specialityRepository.findById(dto.getSpecialityId())
                .orElseThrow(() -> new EntityNotFoundException("Специалността не е намерена"));

        doctor.setName(dto.getName());
        doctor.setGp(dto.isGp());
        doctor.setSpeciality(speciality);

        doctorRepository.save(doctor);
    }

    public void delete(Long id) {
        long linkedPatients = patientRepository.countByGpId(id);
        if (linkedPatients > 0) {
            throw new IllegalStateException(
                    "Лекарят не може да бъде изтрит, защото има "
                            + linkedPatients + " обвързани пациента(и)"
            );
        }

        doctorRepository.deleteById(id);
    }

    public List<DoctorResponseDTO> findAllGps() {
        return doctorRepository.findByIsGpTrue()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private DoctorResponseDTO toResponseDTO(Doctor doctor) {
        return new DoctorResponseDTO(
                doctor.getId(),
                doctor.getName(),
                doctor.isGp(),
                doctor.getSpeciality().getId(),
                doctor.getSpeciality().getName(),
                doctor.getUser().getUsername()
        );
    }
}
