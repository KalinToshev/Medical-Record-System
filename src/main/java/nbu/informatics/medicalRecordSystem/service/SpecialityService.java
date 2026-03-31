package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.mapper.SpecialityMapper;
import nbu.informatics.medicalRecordSystem.model.dto.speciality.SpecialityRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.speciality.SpecialityResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Speciality;
import nbu.informatics.medicalRecordSystem.repository.DoctorRepository;
import nbu.informatics.medicalRecordSystem.repository.SpecialityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialityService {

    private final SpecialityRepository specialityRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialityMapper specialityMapper;

    public List<SpecialityResponseDTO> findAll() {
        return specialityRepository.findAll()
                .stream()
                .map(specialityMapper::toDto)
                .toList();
    }

    public SpecialityResponseDTO findById(Long id) {
        Speciality speciality = specialityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Специалност с id " + id + " не е намерена"));
        return specialityMapper.toDto(speciality);
    }

    @Transactional
    public void create(SpecialityRequestDTO specialityRequestDTO) {
        Speciality speciality = new Speciality();
        speciality.setName(specialityRequestDTO.getName());

        specialityRepository.save(speciality);
    }

    @Transactional
    public void update(Long id, SpecialityRequestDTO specialityRequestDTO) {
        Speciality speciality = specialityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Специалност с id " + id + " не е намерена"));

        speciality.setName(specialityRequestDTO.getName());

        specialityRepository.save(speciality);
    }

    @Transactional
    public void delete(Long id) {
        long linkedDoctors = doctorRepository.countBySpecialityId(id);
        if (linkedDoctors > 0) {
            throw new IllegalStateException(
                    "Специалността не може да бъде изтрита, защото има "
                            + linkedDoctors + " обвързан(и) лекар(и)"
            );
        }
        specialityRepository.deleteById(id);
    }
}
