package ru.job4j.dreamjob.controller;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/candidates")
@ThreadSafe
public class CandidateController {
    private final CandidateService candidateService;
    private final CityService cityService;

    public CandidateController(CandidateService candidateService, CityService cityService) {
        this.candidateService = candidateService;
        this.cityService = cityService;
    }

    private void getUser(Model model, HttpSession session) {
        var user = (User) session.getAttribute("user");
        if (user == null) {
            user = new User();
            user.setName("Гость");
        }
        model.addAttribute("user", user);
    }

    @GetMapping
    public String getAll(Model model, HttpSession session) {
        model.addAttribute("candidates", candidateService.findAll());
        getUser(model, session);
        return "candidates/list";
    }

    @GetMapping("/create")
    public String getCreationPage(Model model, HttpSession session) {
        model.addAttribute("cities", cityService.findAll());
        getUser(model, session);
        return "candidates/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Candidate candidate, @RequestParam MultipartFile file, Model model,
                         HttpSession session) {
        try {
            candidateService.save(candidate, new FileDto(file.getOriginalFilename(), file.getBytes()));
            return "redirect:/candidates";
        } catch (Exception exception) {
            model.addAttribute("message", exception.getMessage());
            getUser(model, session);
            return "errors/404";
        }
    }

    /**
     * Извлекает резюме из репозитория и возвращает на страницу.
     * Если резюме не найдено возвращают страницу с ошибкой.
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public String getById(Model model, @PathVariable int id, HttpSession session) {
        var candidateOptional = candidateService.findById(id);
        if (candidateOptional.isEmpty()) {
            model.addAttribute("message", "Кандидат с указанным идентификатором не найден");
            getUser(model, session);
            return "errors/404";
        }
        model.addAttribute("cities", cityService.findAll());
        model.addAttribute("candidate", candidateOptional.get());
        getUser(model, session);
        return "candidates/one";
    }

    /**
     * Производит обновние и если оно произошло,
     * то делает перенаправление на страницу со всеми резюме.
     * Если резюме не найдено возвращают страницу с ошибкой.
     *
     * @param candidate
     * @param model
     * @return
     */
    @PostMapping("/update")
    public String update(@ModelAttribute Candidate candidate, @RequestParam MultipartFile file, Model model,
                         HttpSession session) {
        try {
            var isUpdated = candidateService.update(candidate,
                    new FileDto(file.getOriginalFilename(), file.getBytes()));
            if (!isUpdated) {
                model.addAttribute("message",
                        "Кандидат с указанным идентификатором не найден");
                getUser(model, session);
                return "errors/404";
            }
            return "redirect:/candidates";
        } catch (Exception exception) {
            model.addAttribute("message", exception.getMessage());
            getUser(model, session);
            return "errors/404";
        }
    }

    /**
     * Производит удаление и если оно произошло,
     * то делает перенаправление на страницу со всеми резюме.
     * Если резюме не найдено возвращают страницу с ошибкой.
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/delete/{id}")
    public String delete(Model model, @PathVariable int id, HttpSession session) {
        var isDeleted = candidateService.deleteById(id);
        if (!isDeleted) {
            model.addAttribute("message", "Кандидат с указанным идентификатором не найден");
            getUser(model, session);
            return "errors/404";
        }
        return "redirect:/candidates";
    }
}
