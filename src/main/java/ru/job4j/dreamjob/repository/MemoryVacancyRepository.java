package ru.job4j.dreamjob.repository;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Vacancy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@ThreadSafe
public class MemoryVacancyRepository implements VacancyRepository {

    private final AtomicInteger nextId = new AtomicInteger(0);

    private final Map<Integer, Vacancy> vacancies = new ConcurrentHashMap<>();

    public MemoryVacancyRepository() {
        save(new Vacancy(0, "Intern Java Developer", "Описание для интерна",
                true, 1, 0));
        save(new Vacancy(0, "Junior Java Developer", "Описание для джуна",
                true, 2, 0));
        save(new Vacancy(0, "Junior+ Java Developer", "Описание для джуна+",
                true, 3, 0));
        save(new Vacancy(0, "Middle Java Developer", "Описание для мидла",
                true, 1, 0));
        save(new Vacancy(0, "Middle+ Java Developer", "Описание для мидла+",
                true, 2, 0));
        save(new Vacancy(0, "Senior Java Developer", "Описание для сеньора",
                true, 3, 0));
    }

    @Override
    public Vacancy save(Vacancy vacancy) {
        int id = nextId.incrementAndGet();
        vacancy.setId(id);
        vacancies.put(vacancy.getId(), vacancy);
        return vacancy;
    }

    @Override
    public boolean deleteById(int id) {
        return vacancies.remove(id) != null;
    }

    @Override
    public boolean update(Vacancy vacancy) {
        return vacancies.computeIfPresent(vacancy.getId(), (id, oldVacancy) ->
                new Vacancy(oldVacancy.getId(), vacancy.getTitle(), vacancy.getDescription(),
                        vacancy.getVisible(), vacancy.getCityId(), vacancy.getFileId())) != null;
    }

    @Override
    public Optional<Vacancy> findById(int id) {
        return Optional.ofNullable(vacancies.get(id));
    }

    @Override
    public Collection<Vacancy> findAll() {
        return vacancies.values();
    }
}
