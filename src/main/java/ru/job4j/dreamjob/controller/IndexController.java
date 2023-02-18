package ru.job4j.dreamjob.controller;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@ThreadSafe
public class IndexController {
    /**
     * Spring позволяет получить объект HttpSession как параметр.
     * Здесь это удобно сделать.
     * Если в HttpSession нет объекта user,
     * то мы создаем объект User с анонимным пользователем.
     *
     * @return
     */
    @GetMapping({"/", "/index"})
    public String getIndex() {
        return "index";
    }
}