package nbu.informatics.medicalRecordSystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.dto.patient.PatientCreateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.patient.PatientResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.patient.PatientUpdateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.role.Role;
import nbu.informatics.medicalRecordSystem.service.DoctorService;
import nbu.informatics.medicalRecordSystem.service.PatientService;
import nbu.informatics.medicalRecordSystem.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final UserService userService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("patients", patientService.findAll());
        return "patients/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("patient", new PatientCreateRequestDTO());
        model.addAttribute("gps", doctorService.findAllGps());
        model.addAttribute("users", userService.findByRole(Role.PATIENT));
        return "patients/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("patient") PatientCreateRequestDTO dto,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("gps", doctorService.findAllGps());
            model.addAttribute("users", userService.findByRole(Role.PATIENT));
            return "patients/form";
        }

        patientService.create(dto);
        return "redirect:/patients";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PatientResponseDTO patient = patientService.findById(id);

        PatientUpdateRequestDTO requestDTO = new PatientUpdateRequestDTO();
        requestDTO.setName(patient.getName());
        requestDTO.setEgn(patient.getEgn());
        requestDTO.setGpId(patient.getGpId());

        model.addAttribute("patient", requestDTO);
        model.addAttribute("patientId", id);
        model.addAttribute("patientUsername", patient.getUsername());
        model.addAttribute("gps", doctorService.findAllGps());
        return "patients/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("patient") PatientUpdateRequestDTO dto,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("patientId", id);
            model.addAttribute("patientUsername", patientService.findById(id).getUsername());
            model.addAttribute("gps", doctorService.findAllGps());
            return "patients/form";
        }

        patientService.update(id, dto);
        return "redirect:/patients";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            patientService.delete(id);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/patients";
    }
}
