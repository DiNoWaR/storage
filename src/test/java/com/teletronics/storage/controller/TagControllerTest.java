package com.teletronics.storage.controller;

import com.teletronics.storage.constants.Constants;
import com.teletronics.storage.service.FileService;
import com.teletronics.storage.service.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagController.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @MockBean
    private TagService tagService;

    @Test
    void shouldReturnListOfTags() throws Exception {
        when(tagService.getTags()).thenReturn(List.of("java", "spring", "go"));

        mockMvc.perform(get("/tags/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0]").value("java"))
                .andExpect(jsonPath("$.tags[1]").value("spring"))
                .andExpect(jsonPath("$.tags[2]").value("go"));

        verify(tagService, times(1)).getTags();
    }

    @Test
    void shouldCreateTag() throws Exception {
        when(tagService.tagExists("java")).thenReturn(false);
        when(tagService.createTag(anyString())).thenReturn("java");

        mockMvc.perform(post("/tags/")
                        .param("tagName", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['Tag created']").value("java"));

        verify(tagService, times(1)).tagExists("java");
        verify(tagService, times(1)).createTag("java");
    }

    @Test
    void shouldNotCreateTagWhenEmpty() throws Exception {
        mockMvc.perform(post("/tags/")
                        .param("tagName", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Constants.TAG_IS_EMPTY_ERROR));

        verify(tagService, never()).createTag(anyString());
    }

    @Test
    void shouldNotCreateTagWhenAlreadyExists() throws Exception {
        when(tagService.tagExists("java")).thenReturn(true);

        mockMvc.perform(post("/tags/")
                        .param("tagName", "java"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(Constants.TAG_EXISTS_ERROR));

        verify(tagService, times(1)).tagExists("java");
        verify(tagService, never()).createTag(anyString());
    }

    @Test
    void shouldDeleteTag() throws Exception {
        doNothing().when(tagService).deleteTag("java");

        mockMvc.perform(delete("/tags/delete")
                        .param("tagName", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['Tag deleted']").value("java"));

        verify(tagService, times(1)).deleteTag("java");
    }

    @Test
    void shouldHandleInternalServerError() throws Exception {
        when(tagService.getTags()).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/tags/list"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$['server error']").value("Unexpected error"));

        verify(tagService, times(1)).getTags();
    }
}
