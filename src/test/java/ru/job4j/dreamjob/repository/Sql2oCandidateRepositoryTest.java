package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.File;

import java.lang.reflect.Field;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class Sql2oCandidateRepositoryTest {
    private static Sql2oCandidateRepository sql2oCandidateRepository;
    private static Sql2oFileRepository sql2oFileRepository;
    private static File file;

    @BeforeAll
    public static void initRepositories() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oCandidateRepositoryTest.class.getClassLoader()
                .getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");
        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        var sql2o = configuration.databaseClient(datasource);
        sql2oCandidateRepository = new Sql2oCandidateRepository(sql2o);
        sql2oFileRepository = new Sql2oFileRepository(sql2o);
        file = new File("test", "test");
        sql2oFileRepository.save(file);
    }

    @AfterAll
    public static void deleteFile() {
        sql2oFileRepository.deleteById(file.getId());
    }

    @AfterEach
    public void clearVacancies() {
        var vacancies = sql2oCandidateRepository.findAll();
        for (var vacancy : vacancies) {
            sql2oCandidateRepository.deleteById(vacancy.getId());
        }
    }

    private Candidate candidateBuilderAndCreationDateSetter(int id, String title, String description, int cityId,
                                                            boolean visible, int fileId)
            throws IllegalAccessException, NoSuchFieldException {
        var creationDate = now().truncatedTo(ChronoUnit.MINUTES);
        var candidate = new Candidate(id, title, description, cityId, visible, fileId);
        Class<? extends Candidate> candidateClass = candidate.getClass();
        Field creationDateField = candidateClass.getDeclaredField("creationDate");
        creationDateField.setAccessible(true);
        creationDateField.set(candidate, creationDate);
        return candidate;
    }

    @Test
    public void whenSaveThenGetSame() throws NoSuchFieldException, IllegalAccessException {
        var candidate = candidateBuilderAndCreationDateSetter(0, "title", "description",
                1, true, file.getId());
        var candidateSaved = sql2oCandidateRepository.save(candidate);
        var candidateFromBD = sql2oCandidateRepository.findById(candidate.getId()).get();
        assertThat(candidateFromBD).usingRecursiveComparison().isEqualTo(candidateSaved);
    }

    @Test
    public void whenSaveSeveralThenGetAll() throws NoSuchFieldException, IllegalAccessException {
        var candidate1 = sql2oCandidateRepository.save(candidateBuilderAndCreationDateSetter(
                0, "title1", "description1", 1, true, file.getId()));
        var candidate2 = sql2oCandidateRepository.save(candidateBuilderAndCreationDateSetter(
                0, "title2", "description2", 1, false, file.getId()));
        var candidate3 = sql2oCandidateRepository.save(candidateBuilderAndCreationDateSetter(
                0, "title3", "description3", 1, true, file.getId()));
        var result = sql2oCandidateRepository.findAll();
        assertThat(result).isEqualTo(List.of(candidate1, candidate2, candidate3));
    }

    @Test
    public void whenDontSaveThenNothingFound() {
        assertThat(sql2oCandidateRepository.findAll()).isEqualTo(emptyList());
        assertThat(sql2oFileRepository.findById(0)).isEqualTo(empty());
    }

    @Test
    public void whenDeleteThenGetEmptyOptional() throws NoSuchFieldException, IllegalAccessException {
        var vacancy = sql2oCandidateRepository.save(candidateBuilderAndCreationDateSetter(
                0, "title", "description", 1, true, file.getId()));
        var isDeleted = sql2oCandidateRepository.deleteById(vacancy.getId());
        var savedVacancy = sql2oCandidateRepository.findById(vacancy.getId());
        assertThat(isDeleted).isTrue();
        assertThat(savedVacancy).isEqualTo(empty());
    }

    @Test
    public void whenDeleteByInvalidIdThenGetFalse() {
        assertThat(sql2oCandidateRepository.deleteById(0)).isFalse();
    }

    @Test
    public void whenUpdateThenGetUpdated() throws NoSuchFieldException, IllegalAccessException {
        var candidate = sql2oCandidateRepository.save(candidateBuilderAndCreationDateSetter(
                0, "title", "description", 1, true, file.getId()));
        var updatedVacancy = candidateBuilderAndCreationDateSetter(
                candidate.getId(), "new title", "new description",
                1, !candidate.getVisible(), file.getId()
        );
        var isUpdated = sql2oCandidateRepository.update(updatedVacancy);
        var savedVacancy = sql2oCandidateRepository.findById(updatedVacancy.getId()).get();
        assertThat(isUpdated).isTrue();
        assertThat(savedVacancy).usingRecursiveComparison().isEqualTo(updatedVacancy);
    }

    @Test
    public void whenUpdateUnExistingVacancyThenGetFalse() throws NoSuchFieldException, IllegalAccessException {
        var candidate = candidateBuilderAndCreationDateSetter(
                0, "title", "description", 1, true, file.getId());
        var isUpdated = sql2oCandidateRepository.update(candidate);
        assertThat(isUpdated).isFalse();
    }
}