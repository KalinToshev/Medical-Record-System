package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.dto.speciality.SpecialityRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.speciality.SpecialityResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Speciality;
import nbu.informatics.medicalRecordSystem.repository.SpecialityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialityService {

    private final SpecialityRepository specialityRepository;

    public List<SpecialityResponseDTO> findAll() {
        return specialityRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public SpecialityResponseDTO findById(Long id) {
        Speciality speciality = specialityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Специалност с id " + id + " не е намерена"));
        return toResponseDTO(speciality);
    }

    public void create(SpecialityRequestDTO specialityRequestDTO) {
        Speciality speciality = new Speciality();
        speciality.setName(specialityRequestDTO.getName());

        specialityRepository.save(speciality);
    }

    public void update(Long id, SpecialityRequestDTO specialityRequestDTO) {
        Speciality speciality = specialityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Специалност с id " + id + " не е намерена"));

        speciality.setName(specialityRequestDTO.getName());

        specialityRepository.save(speciality);
    }

    public void delete(Long id) {
        specialityRepository.deleteById(id);
    }

    private SpecialityResponseDTO toResponseDTO(Speciality speciality) {
        return new SpecialityResponseDTO(
                speciality.getId(),
                speciality.getName()
        );
    }
}
