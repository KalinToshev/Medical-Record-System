package nbu.informatics.medicalRecordSystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.dto.auth.AssignRoleRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.auth.RegisterRequestDTO;
import nbu.informatics.medicalRecordSystem.model.role.Role;
import nbu.informatics.medicalRecordSystem.model.security.UserDetailsImpl;
import nbu.informatics.medicalRecordSystem.service.AuthService;
import nbu.informatics.medicalRecordSystem.service.DoctorService;
import nbu.informatics.medicalRecordSystem.service.SpecialityService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final DoctorService doctorService;
    private final SpecialityService specialityService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegisterRequestDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterRequestDTO dto,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) return "auth/register";
        try {
            authService.register(dto);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
        return "redirect:/pending";
    }

    @GetMapping("/pending")
    public String pendingPage() {
        return "auth/pending";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Role role = userDetails.getUser().getRole();
        return switch (role) {
            case ADMIN -> "redirect:/doctors";
            case DOCTOR -> "redirect:/examinations";
            case PATIENT -> "redirect:/examinations";
            case PENDING -> "redirect:/pending";
        };
    }

    @GetMapping("/admin/users/pending")
    public String pendingUsers(Model model) {
        model.addAttribute("pendingUsers", authService.findPendingUsers());
        return "admin/pending-users";
    }

    @GetMapping("/admin/users/{userId}/assign")
    public String assignRoleForm(@PathVariable Long userId, Model model) {
        model.addAttribute("form", new AssignRoleRequestDTO());
        model.addAttribute("userId", userId);
        model.addAttribute("gps", doctorService.findAllGps());
        model.addAttribute("specialities", specialityService.findAll());
        return "admin/assign-role";
    }

    @PostMapping("/admin/users/{userId}/assign")
    public String assignRole(@PathVariable Long userId,
                             @Valid @ModelAttribute("form") AssignRoleRequestDTO dto,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("userId", userId);
            model.addAttribute("gps", doctorService.findAllGps());
            model.addAttribute("specialities", specialityService.findAll());
            return "admin/assign-role";
        }
        dto.setUserId(userId);
        authService.assignRole(dto);
        return "redirect:/admin/users/pending";
    }

    @GetMapping("/error/403")
    public String accessDenied(Model model) {
        model.addAttribute("errorMessage", "Нямате право да достъпите тази страница.");
        return "error/403";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }
}