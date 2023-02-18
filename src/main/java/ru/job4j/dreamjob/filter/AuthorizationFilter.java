package ru.job4j.dreamjob.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Component это то же самое, что и @Service, @Repository, @Configuration,
 * т.е. аннотация используемая Spring для определения,
 * что объект этого класса нужно создать.
 * <br>Order(1) указывает, что этот фильтр должен выполниться до SessionFilter.
 */
@Component
@Order(1)
public class AuthorizationFilter extends HttpFilter {
    /**
     * В методе doFilter() мы сначала проверяем обращается
     * ли пользователь к общедоступным адресам.
     * Если да, то сразу пропускаем запрос.
     * <br>Далее если пользователь обращается к адресам, требующим прав,
     * то мы проверяем вошел ли пользователь в систему.
     * Если не вошел, то перебрасываем его на страницу входа.
     * <br>Наконец, если пользователь залоггинен в системе,
     * то мы разрешаем дальнейшее выполнение запроса.
     * <br>
     * <br><b>Немного об API сервлетов:</b>
     * <br>chain.doFilter(request, response) вызывает следующий в цепочке фильтр.
     * Если его нет, то запрос уходит к контроллеру.
     * <br>response.sendRedirect(url) выполняет перенаправление по URL.
     * Это аналогично "redirect:/vacancies".
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
        var uri = request.getRequestURI();
        if (isAlwaysPermitted(uri)) {
            chain.doFilter(request, response);
            return;
        }
        var userLoggedIn = request.getSession().getAttribute("user") != null;
        if (!userLoggedIn) {
            var loginPageUrl = request.getContextPath() + "/users/login";
            response.sendRedirect(loginPageUrl);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isAlwaysPermitted(String uri) {
        return uri.startsWith("/users/register") || uri.startsWith("/users/login");
    }
}