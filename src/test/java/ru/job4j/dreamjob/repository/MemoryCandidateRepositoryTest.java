package ru.job4j.dreamjob.repository;

import org.junit.Test;
import ru.job4j.dreamjob.model.Candidate;

import static org.assertj.core.api.Assertions.*;

public class MemoryCandidateRepositoryTest {
    private CandidateRepository memoryCandidateRepository = new MemoryCandidateRepository();


    @Test
    public void whenUpdateThrowException() {
        Candidate candidate = new Candidate(0, "Vasiliy", "Intern java developer");
        memoryCandidateRepository.save(candidate);
        Candidate candidate1 = candidate;
        Candidate candidate2 = candidate;
        memoryCandidateRepository.update(candidate1);
        assertThatThrownBy(() -> memoryCandidateRepository.update(candidate2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Versions are not equal");
    }
}