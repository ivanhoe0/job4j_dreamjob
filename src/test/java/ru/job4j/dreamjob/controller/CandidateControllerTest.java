package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import java.time.LocalDateTime;
import java.util.List;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CandidateControllerTest {

    private CandidateService candidateService;

    private CityService cityService;

    private CandidateController candidateController;

    private MultipartFile testFile;

    @BeforeEach
    public void initServices() {
        candidateService = mock(CandidateService.class);
        cityService = mock(CityService.class);
        testFile = new MockMultipartFile("testFile.img", new byte[] {1, 2, 3});
        candidateController = new CandidateController(candidateService, cityService);
    }

    @Test
    void whenGetDeleteByIdThenOk() {
        when(candidateService.deleteById(1)).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.deleteById(1, model);

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @Test
    void whenGetDeleteByIdThenError() {
        when(candidateService.deleteById(1)).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.deleteById(1, model);
        var message = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(message).isEqualTo("Кандидат с указанным идентификатором не найден");
    }

    @Test
    void whenRequestCandidatesPageThenGetCandidatesList() {
        var candidate1 = new Candidate(1, "TestName1", "TestDesc", LocalDateTime.now(), 1, 1);
        var candidate2 = new Candidate(2, "TestName2", "TestDesc", LocalDateTime.now(), 2, 3);
        var candidates = List.of(candidate1, candidate2);
        when(candidateService.findAll()).thenReturn(candidates);

        var model = new ConcurrentModel();
        var view = candidateController.getAll(model);
        var expectedCandidates = model.getAttribute("candidates");

        assertThat(view).isEqualTo("candidates/list");
        assertThat(expectedCandidates).isEqualTo(candidates);
    }
}