package com.example.contentplatform.api.article;

import com.example.contentplatform.service.article.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @Test
    void shouldCreateArticle() throws Exception {
        ArticleResponse response = new ArticleResponse(1L, "Title", "Body");

        given(articleService.create(any(ArticleRequest.class))).willReturn(response);

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Title",
                                  "content": "Body"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/articles/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.content").value("Body"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateInvalid() throws Exception {
        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.validationErrors.content").exists());
    }

    @Test
    void shouldUpdateArticle() throws Exception {
        ArticleResponse updated = new ArticleResponse(1L, "Updated", "Updated body");
        given(articleService.update(eq(1L), any(ArticleRequest.class)))
                .willReturn(Optional.of(updated));

        mockMvc.perform(put("/articles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated",
                                  "content": "Updated body"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenUpdateNonExistent() throws Exception {
        given(articleService.update(eq(99L), any(ArticleRequest.class)))
                .willReturn(Optional.empty());

        mockMvc.perform(put("/articles/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated",
                                  "content": "Updated body"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteArticle() throws Exception {
        given(articleService.delete(1L)).willReturn(true);

        mockMvc.perform(delete("/articles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeleteNonExistent() throws Exception {
        given(articleService.delete(99L)).willReturn(false);

        mockMvc.perform(delete("/articles/99"))
                .andExpect(status().isNotFound());
    }
}
