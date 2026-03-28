package nbu.informatics.medicalRecordSystem.controller;

import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.service.DiagnosisService;
import nbu.informatics.medicalRecordSystem.service.DoctorService;
import nbu.informatics.medicalRecordSystem.service.PatientService;
import nbu.informatics.medicalRecordSystem.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final DiagnosisService diagnosisService;
    private final DoctorService doctorService;
    private final PatientService patientService;

    @GetMapping
    public String reports(
            @RequestParam(required = false) Long diagnosisId,
            @RequestParam(required = false) Long gpId,
            @RequestParam(required = false) Long historyPatientId,
            @RequestParam(required = false) Long reportDoctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {

        model.addAttribute("diagnoses", diagnosisService.findAll());
        model.addAttribute("gps", doctorService.findAllGps());
        model.addAttribute("allPatients", patientService.findAll());
        model.addAttribute("allDoctors", doctorService.findAll());

        model.addAttribute("countPerGp", reportService.countPatientsPerGp());
        model.addAttribute("countPerDoctor", reportService.countExaminationsPerDoctor());
        model.addAttribute("totalPaidByPatients", reportService.totalPaidByPatients());
        model.addAttribute("totalPerDoctor", reportService.totalPaidByPatientsPerDoctor());
        model.addAttribute("mostCommonDiagnosis", reportService.mostCommonDiagnosis());
        model.addAttribute("monthWithMostSickLeaves", reportService.monthWithMostSickLeaves());
        model.addAttribute("doctorsWithMostSickLeaves", reportService.doctorsWithMostSickLeaves());

        if (diagnosisId != null) {
            model.addAttribute("diagnosisId", diagnosisId);
            model.addAttribute("patientsByDiagnosis", reportService.findByDiagnosis(diagnosisId));
        }

        if (gpId != null) {
            model.addAttribute("gpId", gpId);
            model.addAttribute("patientsByGp", reportService.findByGp(gpId));
        }

        if (historyPatientId != null) {
            model.addAttribute("historyPatientId", historyPatientId);
            model.addAttribute("patientHistory", reportService.findPatientHistory(historyPatientId));
        }

        if (reportDoctorId != null || from != null || to != null) {
            model.addAttribute("reportDoctorId", reportDoctorId);
            model.addAttribute("from", from);
            model.addAttribute("to", to);
            LocalDateTime fromDT = from != null ? from.atStartOfDay() : null;
            LocalDateTime toDT = to != null ? to.atTime(23, 59, 59) : null;
            model.addAttribute("examinationsByDoctorAndPeriod",
                    reportService.findByDoctorAndPeriod(reportDoctorId, fromDT, toDT));
        }

        return "reports/index";
    }
}
