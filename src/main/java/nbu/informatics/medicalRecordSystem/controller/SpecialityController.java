package nbu.informatics.medicalRecordSystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.dto.speciality.SpecialityRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.speciality.SpecialityResponseDTO;
import nbu.informatics.medicalRecordSystem.service.SpecialityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/specialities")
@RequiredArgsConstructor
public class SpecialityController {

    private final SpecialityService specialityService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("specialities", specialityService.findAll());
        return "specialities/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("speciality", new SpecialityRequestDTO());
        return "specialities/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("speciality") SpecialityRequestDTO dto,
                         BindingResult result) {
        if (result.hasErrors()) return "specialities/form";
        specialityService.create(dto);
        return "redirect:/specialities";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        SpecialityResponseDTO speciality = specialityService.findById(id);

        SpecialityRequestDTO requestDTO = new SpecialityRequestDTO();
        requestDTO.setName(speciality.getName());

        model.addAttribute("speciality", requestDTO);
        model.addAttribute("specialityId", id);
        return "specialities/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("speciality") SpecialityRequestDTO dto,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("specialityId", id);
            return "specialities/form";
        }
        specialityService.update(id, dto);
        return "redirect:/specialities";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        specialityService.delete(id);
        return "redirect:/specialities";
    }
}
