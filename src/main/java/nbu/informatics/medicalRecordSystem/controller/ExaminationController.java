package nbu.informatics.medicalRecordSystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.dto.examination.ExaminationCreateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.examination.ExaminationResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.examination.ExaminationUpdateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.sickLeave.SickLeaveResponseDTO;
import nbu.informatics.medicalRecordSystem.model.security.UserDetailsImpl;
import nbu.informatics.medicalRecordSystem.service.DiagnosisService;
import nbu.informatics.medicalRecordSystem.service.ExaminationService;
import nbu.informatics.medicalRecordSystem.service.PatientService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/examinations")
@RequiredArgsConstructor
public class ExaminationController {

    private final ExaminationService examinationService;
    private final PatientService patientService;
    private final DiagnosisService diagnosisService;

    @GetMapping
    public String list(Model model,
                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        model.addAttribute("examinations",
                examinationService.findAllForUser(userDetails.getUser()));
        return "examinations/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("examination", new ExaminationCreateRequestDTO());
        model.addAttribute("patients", patientService.findAll());
        model.addAttribute("diagnoses", diagnosisService.findAll());
        return "examinations/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("examination") ExaminationCreateRequestDTO dto,
                         BindingResult result,
                         Model model,
                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (result.hasErrors()) {
            model.addAttribute("patients", patientService.findAll());
            model.addAttribute("diagnoses", diagnosisService.findAll());
            return "examinations/form";
        }

        examinationService.create(dto, userDetails.getUser());
        return "redirect:/examinations";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model,
                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ExaminationResponseDTO examination = examinationService.findByIdForDoctor(id, userDetails.getUser());

        ExaminationUpdateRequestDTO requestDTO = new ExaminationUpdateRequestDTO();
        requestDTO.setDiagnosisId(examinationService.getDiagnosisIdForDoctor(id, userDetails.getUser()));
        requestDTO.setTreatment(examination.getTreatment());
        requestDTO.setPrice(examination.getPrice());
        requestDTO.setIssueSickLeave(examination.isHasSickLeave());

        if (examination.isHasSickLeave()) {
            SickLeaveResponseDTO sickLeave = examinationService.getSickLeaveForDoctor(id, userDetails.getUser());
            requestDTO.setSickLeaveStartDate(sickLeave.getStartDate());
            requestDTO.setSickLeaveDays(sickLeave.getDays());
        }

        model.addAttribute("examination", requestDTO);
        model.addAttribute("examinationId", id);
        model.addAttribute("examinationInfo", examination);
        model.addAttribute("diagnoses", diagnosisService.findAll());
        return "examinations/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("examination") ExaminationUpdateRequestDTO dto,
                         BindingResult result,
                         Model model,
                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (result.hasErrors()) {
            model.addAttribute("examinationId", id);
            model.addAttribute("examinationInfo", examinationService.findById(id));
            model.addAttribute("diagnoses", diagnosisService.findAll());
            return "examinations/form";
        }

        examinationService.update(id, dto, userDetails.getUser());
        return "redirect:/examinations";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        examinationService.delete(id, userDetails.getUser());
        return "redirect:/examinations";
    }
}
