package nbu.informatics.medicalRecordSystem.service;

import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import nbu.informatics.medicalRecordSystem.model.role.Role;
import nbu.informatics.medicalRecordSystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }
}
