package ru.job4j.dreamjob.repository;

import org.junit.Test;
import ru.job4j.dreamjob.model.Vacancy;

import static org.assertj.core.api.Assertions.*;

public class MemoryVacancyRepositoryTest {
    private VacancyRepository memoryVacancyRepository = new MemoryVacancyRepository();

    @Test
    public void whenUpdateThrowException() {
        Vacancy vacancy = new Vacancy(0, "Intern Java Developer", "Описание для интерна");
        memoryVacancyRepository.save(vacancy);
        Vacancy vacancy1 = vacancy;
        Vacancy vacancy2 = vacancy;
        memoryVacancyRepository.update(vacancy1);
        assertThatThrownBy(() -> memoryVacancyRepository.update(vacancy2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Versions are not equal");
    }
}
