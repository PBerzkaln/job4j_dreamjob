package ru.job4j.dreamjob.repository;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Candidate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@ThreadSafe
public class MemoryCandidateRepository implements CandidateRepository {

    private final AtomicInteger nextId = new AtomicInteger(0);

    private final Map<Integer, Candidate> candidates = new ConcurrentHashMap<>();

    public MemoryCandidateRepository() {
        save(new Candidate(0, "Vasiliy", "Intern java developer", 1, true, 0));
        save(new Candidate(0, "Oleg", "Junior java developer", 2, true, 0));
        save(new Candidate(0, "Petr", "Juior+ java developer", 3, true, 0));
        save(new Candidate(0, "Aleksey", "Middle java developer", 1, true, 0));
        save(new Candidate(0, "Igor", "Middle+ java developer", 2, true, 0));
        save(new Candidate(0, "Andrey", "Senior java developer", 3, true, 0));
    }

    @Override
    public Candidate save(Candidate candidate) {
        int id = nextId.incrementAndGet();
        candidate.setId(id);
        candidates.putIfAbsent(candidate.getId(), candidate);
        return candidate;
    }

    @Override
    public boolean deleteById(int id) {
        return candidates.remove(id) != null;
    }

    @Override
    public boolean update(Candidate candidate) {
        return candidates.computeIfPresent(candidate.getId(), (id, oldCandidate) ->
                new Candidate(oldCandidate.getId(), candidate.getName(),
                        candidate.getDescription(), candidate.getCityId(), candidate.getVisible(),
                        candidate.getFileId())) != null;
    }

    @Override
    public Optional<Candidate> findById(int id) {
        return Optional.ofNullable(candidates.get(id));
    }

    @Override
    public Collection<Candidate> findAll() {
        return candidates.values();
    }
}