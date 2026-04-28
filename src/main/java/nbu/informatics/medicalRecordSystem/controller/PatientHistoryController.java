package nbu.informatics.medicalRecordSystem.controller;

import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.service.ExaminationService;
import nbu.informatics.medicalRecordSystem.service.HealthInsuranceService;
import nbu.informatics.medicalRecordSystem.service.PatientService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/patients/{patientId}/history")
@RequiredArgsConstructor
public class PatientHistoryController {

    private final PatientService patientService;
    private final ExaminationService examinationService;
    private final HealthInsuranceService healthInsuranceService;

    @GetMapping
    @PreAuthorize("@patientHistorySecurity.canViewHistory(#patientId, principal)")
    public String history(@PathVariable Long patientId, Model model) {
        model.addAttribute("patient", patientService.findById(patientId));
        model.addAttribute("examinations", examinationService.findFullHistoryByPatient(patientId));
        model.addAttribute("insurances", healthInsuranceService.findLast6MonthsByPatient(patientId));
        return "patients/history";
    }
}
