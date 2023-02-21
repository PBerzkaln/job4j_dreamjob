package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.model.Vacancy;
import ru.job4j.dreamjob.service.CityService;
import ru.job4j.dreamjob.service.VacancyService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class VacancyControllerTest {

    private VacancyService vacancyService;

    private CityService cityService;

    private VacancyController vacancyController;

    private MultipartFile testFile;

    /**
     * Тестируем мы VacancyController,
     * а зависит он от VacancyService и CityService.
     * Чтобы не дублировать код в тестах,
     * мы до начала тестов создаем моки зависимостей и тестируемый класс.
     * Также для чистоты тестов, создаем мок отправляемого файла.
     * В Spring уже есть для этого готовый мок - MockMultipartFile.
     */
    @BeforeEach
    public void initServices() {
        vacancyService = mock(VacancyService.class);
        cityService = mock(CityService.class);
        vacancyController = new VacancyController(vacancyService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[]{1, 2, 3});
    }

    /**
     * Тест логически разделен согласно принципу AAA (Arrange, Act, Assert):
     * <br>1. Сначала мы создаем тестовые данные;
     * <br>2. Далее вызываем метод контроллера.
     * Для этого нам нужно создать Model.
     * В качестве Model используем готовый класс,
     * который используется Spring - ConcurrentModel.
     * Это реализация Model на основе ConcurrentHashMap;
     * <br>3. Наконец, сравниваем, что вернулось нам нужно представление и данные в моделе.
     */
    @Test
    public void whenRequestVacancyListPageThenGetPageWithVacancies() {
        var vacancy1 = new Vacancy(1, "test1", "desc1", true, 1, 2);
        var vacancy2 = new Vacancy(2, "test2", "desc2", false, 3, 4);
        var expectedVacancies = List.of(vacancy1, vacancy2);
        when(vacancyService.findAll()).thenReturn(expectedVacancies);

        var model = new ConcurrentModel();
        var view = vacancyController.getAll(model);
        var actualVacancies = model.getAttribute("vacancies");

        assertThat(view).isEqualTo("vacancies/list");
        assertThat(actualVacancies).isEqualTo(expectedVacancies);
    }

    /**
     * Аналогичен тесту выше.
     */
    @Test
    public void whenRequestVacancyCreationPageThenGetPageWithCities() {
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var expectedCities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();
        var view = vacancyController.getCreationPage(model);
        var actualVacancies = model.getAttribute("cities");

        assertThat(view).isEqualTo("vacancies/create");
        assertThat(actualVacancies).isEqualTo(expectedCities);
    }

    /**
     * Он аналогичен предыдущим тестам за исключением того,
     * что в нем используем ArgumentCaptor.
     * Этот класс позволяет "захватить" аргумент,
     * который передается в метод.
     * Он удобен при тестировании методов, аргументы
     * которых вычисляются как, например, при создании FileDto.
     * Но мы не можем получить их как возвращаемые значения.
     * <br>В месте вызова метода нам нужно вызывать метод capture().
     * <br>Для получения переловленного значения нужно вызвать метод getValue().
     *
     * @throws Exception
     */
    @Test
    public void whenPostVacancyWithFileThenSameDataAndRedirectToVacanciesPage() throws Exception {
        var vacancy = new Vacancy(1, "test1", "desc1", true, 1, 2);
        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        var vacancyArgumentCaptor = ArgumentCaptor.forClass(Vacancy.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(vacancyService.save(vacancyArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(vacancy);

        var model = new ConcurrentModel();
        var view = vacancyController.create(vacancy, testFile, model);
        var actualVacancy = vacancyArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualVacancy).isEqualTo(vacancy);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    /**
     * Тут все просто. Вместо thenReturn() мы используем thenThrow().
     * Таким образом, метод выкидывает исключение, а не возвращает значение.
     */
    @Test
    public void whenSomeExceptionThrownThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Failed to write file");
        when(vacancyService.save(any(), any())).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = vacancyController.create(new Vacancy(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenGetByIdAndRedirectToOneVacancyPage() {
        var vacancy = new Vacancy(1, "test1", "desc1", true, 1, 2);
        var city = new City(1, "Москва");
        when(vacancyService.findById(1)).thenReturn(Optional.of(vacancy));

        var model = new ConcurrentModel();
        var view = vacancyController.getById(model, vacancy.getId());
        var actualVacancy = model.getAttribute("vacancy");

        assertThat(view).isEqualTo("vacancies/one");
        assertThat(actualVacancy).isEqualTo(vacancy);
        assertThat(city.getId()).isEqualTo(vacancy.getCityId());
    }

    @Test
    public void whenSomeExceptionThrownThenFindById() {
        var expectedException = new RuntimeException("Вакансия с указанным идентификатором не найдена");
        when(vacancyService.findById(anyInt())).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = vacancyController.getById(model, anyInt());
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenVacancyUpdateWithFileThenSameDataAndRedirectToVacanciesPage() throws IOException {
        var vacancy = new Vacancy(1, "test1", "desc1", true, 1, 2);
        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        var vacancyArgumentCaptor = ArgumentCaptor.forClass(Vacancy.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(vacancyService.update(vacancyArgumentCaptor.capture(),
                fileDtoArgumentCaptor.capture())).thenReturn(true);

        var model = new ConcurrentModel();
        var view = vacancyController.update(vacancy, testFile, model);
        var actualVacancy = vacancyArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualVacancy).isEqualTo(vacancy);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    public void whenSomeExceptionThrownThenUpdate() {
        var expectedException = new RuntimeException("Вакансия с указанным идентификатором не найдена");
        when(vacancyService.update(any(), any())).thenReturn(false);

        var model = new ConcurrentModel();
        var view = vacancyController.update(new Vacancy(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenSomeVacancyDelete() {
        when(vacancyService.deleteById(anyInt())).thenReturn(true);

        var model = new ConcurrentModel();
        var view = vacancyController.delete(model, anyInt());

        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @Test
    public void whenSomeVacancyNotDeleted() {
        var expectedException = new RuntimeException("Вакансия с указанным идентификатором не найдена");
        when(vacancyService.deleteById(anyInt())).thenReturn(false);

        var model = new ConcurrentModel();
        var view = vacancyController.delete(model, anyInt());
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }
}