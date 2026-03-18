package nbu.informatics.medicalRecordSystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.dto.doctor.DoctorCreateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.doctor.DoctorResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.doctor.DoctorUpdateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.role.Role;
import nbu.informatics.medicalRecordSystem.repository.UserRepository;
import nbu.informatics.medicalRecordSystem.service.DoctorService;
import nbu.informatics.medicalRecordSystem.service.SpecialityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final SpecialityService specialityService;
    private final UserRepository userRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("doctors", doctorService.findAll());
        return "doctors/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("doctor", new DoctorCreateRequestDTO());
        model.addAttribute("specialities", specialityService.findAll());
        model.addAttribute("users", userRepository.findByRole(Role.DOCTOR));
        return "doctors/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("doctor") DoctorCreateRequestDTO dto,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("specialities", specialityService.findAll());
            model.addAttribute("users", userRepository.findByRole(Role.DOCTOR));
            return "doctors/form";
        }

        doctorService.create(dto);
        return "redirect:/doctors";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        DoctorResponseDTO doctor = doctorService.findById(id);

        DoctorUpdateRequestDTO requestDTO = new DoctorUpdateRequestDTO();
        requestDTO.setName(doctor.getName());
        requestDTO.setGp(doctor.isGp());
        requestDTO.setSpecialityId(doctor.getSpecialityId());

        model.addAttribute("doctor", requestDTO);
        model.addAttribute("doctorId", id);
        model.addAttribute("doctorUsername", doctor.getUsername());
        model.addAttribute("specialities", specialityService.findAll());
        return "doctors/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("doctor") DoctorUpdateRequestDTO dto,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("specialities", specialityService.findAll());
            model.addAttribute("doctorId", id);
            model.addAttribute("doctorUsername", doctorService.findById(id).getUsername());
            return "doctors/form";
        }

        try {
            doctorService.update(id, dto);
        } catch (IllegalStateException ex) {
            result.rejectValue("gp", "gpChangeNotAllowed", ex.getMessage());
            model.addAttribute("specialities", specialityService.findAll());
            model.addAttribute("doctorId", id);
            model.addAttribute("doctorUsername", doctorService.findById(id).getUsername());
            return "doctors/form";
        }

        return "redirect:/doctors";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.delete(id);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/doctors";
    }
}
