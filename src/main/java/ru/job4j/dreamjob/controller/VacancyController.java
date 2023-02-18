package ru.job4j.dreamjob.controller;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Vacancy;
import ru.job4j.dreamjob.service.CityService;
import ru.job4j.dreamjob.service.VacancyService;

@Controller
@RequestMapping("/vacancies")
@ThreadSafe
public class VacancyController {
    private final VacancyService vacancyService;
    private final CityService cityService;


    public VacancyController(VacancyService vacancyService, CityService cityService) {
        this.vacancyService = vacancyService;
        this.cityService = cityService;
    }

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("vacancies", vacancyService.findAll());
        return "vacancies/list";
    }

    @GetMapping("/create")
    public String getCreationPage(Model model) {
        model.addAttribute("cities", cityService.findAll());
        return "vacancies/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Vacancy vacancy, @RequestParam MultipartFile file, Model model) {
        try {
            vacancyService.save(vacancy, new FileDto(file.getOriginalFilename(), file.getBytes()));
            return "redirect:/vacancies";
        } catch (Exception exception) {
            model.addAttribute("message", exception.getMessage());
            return "errors/404";
        }
    }

    /**
     * Извлекает вакансию из репозитория и возвращает на страницу.
     * Если вакансия не найдена возвращают страницу с ошибкой.
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public String getById(Model model, @PathVariable int id) {
        var vacancyOptional = vacancyService.findById(id);
        if (vacancyOptional.isEmpty()) {
            model.addAttribute("message", "Вакансия с указанным идентификатором не найдена");
            return "errors/404";
        }
        model.addAttribute("cities", cityService.findAll());
        model.addAttribute("vacancy", vacancyOptional.get());
        return "vacancies/one";
    }

    /**
     * Производит обновление и если оно произошло,
     * то делает перенаправление на страницу со всеми вакансиями.
     * <br>Если вакансия не найдена возвращают страницу с ошибкой.
     * <br>RequestParam MultipartFile file - так мы получаем файл из формы.
     * Название параметра соответствует name из формы.
     * <br>new FileDto(file.getOriginalFilename(), file.getBytes())
     * - так мы передаем "упакованные" в DTO данные для обработки в сервисе.
     *
     * @param vacancy
     * @param model
     * @return
     */
    @PostMapping("/update")
    public String update(@ModelAttribute Vacancy vacancy, @RequestParam MultipartFile file, Model model) {
        try {
            var isUpdated = vacancyService.update(vacancy,
                    new FileDto(file.getOriginalFilename(), file.getBytes()));
            if (!isUpdated) {
                model.addAttribute("message",
                        "Вакансия с указанным идентификатором не найдена");
                return "errors/404";
            }
            return "redirect:/vacancies";
        } catch (Exception exception) {
            model.addAttribute("message", exception.getMessage());
            return "errors/404";
        }
    }

    /**
     * Производит удаление и если оно произошло,
     * то делает перенаправление на страницу со всеми вакансиями.
     * Если вакансия не найдена возвращают страницу с ошибкой.
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/delete/{id}")
    public String delete(Model model, @PathVariable int id) {
        var isDeleted = vacancyService.deleteById(id);
        if (!isDeleted) {
            model.addAttribute("message", "Вакансия с указанным идентификатором не найдена");
            return "errors/404";
        }
        return "redirect:/vacancies";
    }
}
