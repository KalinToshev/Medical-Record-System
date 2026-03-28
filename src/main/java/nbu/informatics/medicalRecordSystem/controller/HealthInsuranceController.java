package nbu.informatics.medicalRecordSystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.dto.healthInsurance.HealthInsuranceRequestDTO;
import nbu.informatics.medicalRecordSystem.service.HealthInsuranceService;
import nbu.informatics.medicalRecordSystem.service.PatientService;
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
@RequestMapping("/patients/{patientId}/insurances")
@RequiredArgsConstructor
public class HealthInsuranceController {

    private final HealthInsuranceService healthInsuranceService;
    private final PatientService patientService;

    @GetMapping
    public String list(@PathVariable Long patientId, Model model) {
        model.addAttribute("patient", patientService.findById(patientId));
        model.addAttribute("insurances", healthInsuranceService.findByPatient(patientId));
        model.addAttribute("insurance", new HealthInsuranceRequestDTO());
        return "insurances/list";
    }

    @PostMapping("/new")
    public String create(@PathVariable Long patientId,
                         @Valid @ModelAttribute("insurance") HealthInsuranceRequestDTO dto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("patient", patientService.findById(patientId));
            model.addAttribute("insurances", healthInsuranceService.findByPatient(patientId));
            return "insurances/list";
        }

        try {
            healthInsuranceService.create(patientId, dto);
        } catch (IllegalStateException e) {
            model.addAttribute("patient", patientService.findById(patientId));
            model.addAttribute("insurances", healthInsuranceService.findByPatient(patientId));
            model.addAttribute("duplicateError", e.getMessage());
            return "insurances/list";
        }

        return "redirect:/patients/" + patientId + "/insurances";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long patientId, @PathVariable Long id) {
        healthInsuranceService.delete(patientId, id);
        return "redirect:/patients/" + patientId + "/insurances";
    }
}
