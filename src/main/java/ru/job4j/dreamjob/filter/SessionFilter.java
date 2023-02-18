package ru.job4j.dreamjob.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.job4j.dreamjob.model.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
@Order(2)
public class SessionFilter extends HttpFilter {
    /**
     * Добавляем пользователя в model.
     * Только обратите внимание, что доступа к объекту типа Model у нас нет.
     * Дело в том, что сервлеты это более низкий уровень,
     * а Spring MVC более высокий уровень абстракции с точки зрения реализации MVC.
     * Model существует только в Spring MVC.
     * Под капотом Spring MVC использует Servlet API.
     * В частности, метод request.setAttribute(),
     * когда мы вызываем model.addAttribute(),
     * поэтому вызвав request.setAttibute() мы можем получить данные
     * уже в самом контроллере в объекте Model.
     * <br>Делаем вызов следующего в цепочке фильтра.
     *
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        var session = request.getSession();
        addUserToSession(session, request);
        chain.doFilter(request, response);
    }

    private void addUserToSession(HttpSession session, HttpServletRequest request) {
        var user = (User) session.getAttribute("user");
        if (user == null) {
            user = new User();
            user.setName("Гость");
        }
        request.setAttribute("user", user);
    }
}