package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.*;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.File;
import ru.job4j.dreamjob.model.Vacancy;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class Sql2oCandidateRepositoryTest {

    private static Sql2oCandidateRepository sql2oCandidateRepository;

    private static Sql2oFileRepository sql2oFileRepository;

    private static File file;

    @BeforeAll
    public static void initConnections() throws Exception {
        var properties = new Properties();
        try (var input = Sql2oCandidateRepository.class.getClassLoader().getResourceAsStream("connection.properties")) {
            properties.load(input);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");

        var configuration = new DatasourceConfiguration();
        var dataSource = configuration.connectionPool(url, username, password);
        var sql2o = configuration.databaseClient(dataSource);

        sql2oFileRepository = new Sql2oFileRepository(sql2o);
        sql2oCandidateRepository = new Sql2oCandidateRepository(sql2o);

        file = new File("test", "test");
        sql2oFileRepository.save(file);
    }

    @AfterAll
    public static void deleteFile() {
        sql2oFileRepository.deleteById(file.getId());
    }

    @AfterEach
    public void clearCandidates() {
        var candidates = sql2oCandidateRepository.findAll();
        for (var candidate : candidates) {
            sql2oCandidateRepository.deleteById(candidate.getId());
        }
    }

    @Test
    void whenSaveThenGetSame() {
        var creationTime = now().truncatedTo(ChronoUnit.MINUTES);
        var candidateToSave = sql2oCandidateRepository.save(new Candidate(0, "name", "description", creationTime, 1, file.getId()));
        var savedCandidate = sql2oCandidateRepository.findById(candidateToSave.getId()).get();
        assertThat(candidateToSave).usingRecursiveAssertion().isEqualTo(savedCandidate);
    }

    @Test
    void whenDontSave() {
        assertThat(sql2oCandidateRepository.findAll()).isEqualTo(emptyList());
        assertThat(sql2oCandidateRepository.findById(0)).isEqualTo(empty());
    }

    @Test
    void whenDeleteThenGetEmptyOptional() {
        var candidateToSave = sql2oCandidateRepository.save(new Candidate(0, "name", "description", now(), 1, file.getId()));
        assertTrue(sql2oCandidateRepository.deleteById(candidateToSave.getId()));
        assertThat(sql2oCandidateRepository.findById(candidateToSave.getId())).isEqualTo(empty());
    }

    @Test
    void whenCreateSeveralThenOK() {
        var creationTime = now().truncatedTo(ChronoUnit.MINUTES);
        var candidate1 = sql2oCandidateRepository.save(new Candidate(0, "name1", "description1", creationTime, 1, file.getId()));
        var candidate2 = sql2oCandidateRepository.save(new Candidate(0, "name2", "description3", creationTime, 2, file.getId()));
        var candidate3 = sql2oCandidateRepository.save(new Candidate(0, "name3", "description2", creationTime, 1, file.getId()));
        assertThat(sql2oCandidateRepository.findAll()).isEqualTo(List.of(candidate1, candidate2, candidate3));
    }

    @Test
    void whenUpdate() {
        var creationTime = now().truncatedTo(ChronoUnit.MINUTES);
        var saved = sql2oCandidateRepository.save(new Candidate(0, "name1", "description1", creationTime, 1, file.getId()));
        var updatedCandidate = new Candidate(
                saved.getId(),
                "Changed",
                saved.getDescription(),
                creationTime.plusDays(3),
                saved.getCityId(),
                file.getId()
        );
        var isUpdated = sql2oCandidateRepository.update(updatedCandidate);
        assertThat(isUpdated).isTrue();
        assertThat(sql2oCandidateRepository.findById(saved.getId()).get()).usingRecursiveAssertion().isEqualTo(updatedCandidate);
    }

    @Test
    void whenUpdateNotExistingCandidateThenFalse() {
        var creationDate = now().truncatedTo(ChronoUnit.MINUTES);
        var vacancy = new Candidate(0, "title", "description", creationDate, 1, file.getId());
        var isUpdated = sql2oCandidateRepository.update(vacancy);
        assertThat(isUpdated).isFalse();
    }
}