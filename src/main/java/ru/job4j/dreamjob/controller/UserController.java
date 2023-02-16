package ru.job4j.dreamjob.controller;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
@ThreadSafe
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String getRegistrationPage() {
        return "users/register";
    }

    @PostMapping("/register")
    public String register(Model model, @ModelAttribute User user) {
        var savedUser = userService.save(user);
        if (savedUser.isEmpty()) {
            model.addAttribute("message", "Пользователь с такой почтой уже существует");
            return "errors/404";
        }
        return "redirect:/vacancies";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "users/login";
    }

    /**
     * Объект HttpSession можно получить через HttpServletRequest.
     * Метод getSession возвращает объект HttpSession.
     * В нем можно хранить информацию о текущем пользователе.
     * Чтобы добавить данные в HttpSession,
     * используем метод setAttribute(key, value).
     * Чтобы получить данные из HttpSession,
     * используется метод getAttribute(key).
     * <br>
     * <br><b>Важно!</b>
     * <br>Обратите внимание, что внутри HttpSession используется
     * многопоточная коллекция ConcurrentHashMap.
     * Это связано с многопоточным окружением.
     * Напомню, что для работы с ConcurrentHashMap нельзя
     * использовать операции check-then-act.
     * То есть HttpSession можно использовать либо для записи,
     * либо для чтения, но нельзя делать это одновременно.
     *
     * @param user
     * @param model
     * @param request
     * @return
     */
    @PostMapping("/login")
    public String loginUser(@ModelAttribute User user, Model model, HttpServletRequest request) {
        var userOptional = userService.findByEmailAndPassword(user.getEmail(), user.getPassword());
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "Почта или пароль введены неверно");
            return "users/login";
        }
        var session = request.getSession();
        session.setAttribute("user", userOptional.get());
        return "redirect:/vacancies";
    }

    /**
     * Осталось настроить выход из системы.
     * Чтобы его сделать нужно использовать метод HttpSession.invalidate
     * - это метод удалит все связанные с этим пользователем данные.
     *
     * @param session
     * @return
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/users/login";
    }
}