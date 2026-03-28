package nbu.informatics.medicalRecordSystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.dto.diagnosis.DiagnosisRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.diagnosis.DiagnosisResponseDTO;
import nbu.informatics.medicalRecordSystem.service.DiagnosisService;
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
@RequestMapping("/diagnoses")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("diagnoses", diagnosisService.findAll());
        return "diagnoses/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("diagnosis", new DiagnosisRequestDTO());
        return "diagnoses/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("diagnosis") DiagnosisRequestDTO dto,
                         BindingResult result) {
        if (result.hasErrors()) return "diagnoses/form";
        diagnosisService.create(dto);
        return "redirect:/diagnoses";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        DiagnosisResponseDTO diagnosis = diagnosisService.findById(id);

        DiagnosisRequestDTO requestDTO = new DiagnosisRequestDTO();
        requestDTO.setName(diagnosis.getName());

        model.addAttribute("diagnosis", requestDTO);
        model.addAttribute("diagnosisId", id);
        return "diagnoses/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("diagnosis") DiagnosisRequestDTO dto,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("diagnosisId", id);
            return "diagnoses/form";
        }
        diagnosisService.update(id, dto);
        return "redirect:/diagnoses";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            diagnosisService.delete(id);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/diagnoses";
    }
}
