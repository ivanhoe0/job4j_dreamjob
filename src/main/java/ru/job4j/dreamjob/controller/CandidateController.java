package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpSession;
import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.model.Vacancy;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

@ThreadSafe
@Controller
@RequestMapping("/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    private final CityService cityService;

    public CandidateController(CandidateService candidateService, CityService cityService) {
        this.candidateService = candidateService;
        this.cityService = cityService;
    }

    @GetMapping
    public String getAll(Model model, HttpSession session) {
        var sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            sessionUser = new User();
            sessionUser.setName("Гость");
        }
        model.addAttribute("user", sessionUser);
        model.addAttribute("candidates", candidateService.findAll());
        return "candidates/list";
    }

    @GetMapping("/create")
    public String getCreationPage(Model model, HttpSession session) {
        var sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            sessionUser = new User();
            sessionUser.setName("Гость");
        }
        model.addAttribute("user", sessionUser);
        model.addAttribute("cities", cityService.findAll());
        return "candidates/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Candidate candidate, @RequestParam MultipartFile file, Model model, HttpSession session) {
        var sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            sessionUser = new User();
            sessionUser.setName("Гость");
        }
        model.addAttribute("user", sessionUser);
        try {
            candidateService.save(candidate, new FileDto(file.getOriginalFilename(), file.getBytes()));
            return "redirect:/candidates";
        } catch (Exception exception) {
            model.addAttribute("message", exception.getMessage());
            return "errors/404";
        }
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Candidate candidate, @RequestParam MultipartFile file, Model model, HttpSession session) {
        var sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            sessionUser = new User();
            sessionUser.setName("Гость");
        }
        model.addAttribute("user", sessionUser);
        try {
            var isUpdated = candidateService.update(candidate, new FileDto(file.getOriginalFilename(), file.getBytes()));
            if (!isUpdated) {
                model.addAttribute("message", "Вакансия с указанным идентификатором не найдена");
                return "errors/404";
            }
            return "redirect:/vacancies";
        } catch (Exception exception) {
            model.addAttribute("message", exception.getMessage());
            return "errors/404";
        }
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable int id, Model model, HttpSession session) {
        var sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            sessionUser = new User();
            sessionUser.setName("Гость");
        }
        model.addAttribute("user", sessionUser);
        var optionalCandidate = candidateService.findById(id);
        if (optionalCandidate.isEmpty()) {
            model.addAttribute("message", "Кандидат с указанным идентификатором не найден");
            return "errors/404";
        }
        model.addAttribute("candidate", optionalCandidate.get());
        model.addAttribute("cities", cityService.findAll());
        return "candidates/one";
    }

    @GetMapping("/delete/{id}")
    public String deleteById(@PathVariable int id, Model model, HttpSession session) {
        var sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            sessionUser = new User();
            sessionUser.setName("Гость");
        }
        model.addAttribute("user", sessionUser);
        var isDeleted = candidateService.deleteById(id);
        if (!isDeleted) {
            model.addAttribute("message", "Кандидат с указанным идентификатором не найден");
            return "errors/404";
        }
        return "redirect:/candidates";
    }

}
