package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.service.FileService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class FileControllerTest {
    private FileService fileService;
    private FileController fileController;
    private FileDto fileDto;

    @BeforeEach
    public void initService() {
        fileService = mock(FileService.class);
        fileController = new FileController(fileService);
        fileDto = new FileDto("file", new byte[]{1, 2, 3});
    }

    @Test
    public void whenRequestGetFileByIdThenReturnResponseEntityStatusOk() {
        when(fileService.getFileById(anyInt())).thenReturn(Optional.of(fileDto));
        var expectedResponseEntity = ResponseEntity.ok(fileDto.getContent());

        var actualResponseEntity = fileController.getById(anyInt());

        assertThat(actualResponseEntity)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseEntity);
    }

    @Test
    public void whenRequestGetFileByIdThenReturnResponseEntityNotFound() {
        when(fileService.getFileById(anyInt())).thenReturn(Optional.empty());
        var expectedResponseEntity = ResponseEntity.notFound().build();

        var actualResponseEntity = fileController.getById(anyInt());

        assertThat(actualResponseEntity)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseEntity);
    }
}