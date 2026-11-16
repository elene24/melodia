/*
 * MelodiaApiIntegrationTest.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * End-to-end tests of the REST API (runs on an in-memory H2 database).
 */
package ge.melodia;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet
        .AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Exercises the main flows: creating data, uploading a song,
 * pagination, playlists, play counter and validation errors.
 * (Field injection is only used here because JUnit creates test
 * classes itself; application code uses constructor injection.)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MelodiaApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullFlowWorks() throws Exception {
        long genreId = postJson("/api/genres", "{\"name\":\"Synthwave\"}");
        long artistId = postJson("/api/artists",
                "{\"name\":\"Test Artist\",\"bio\":\"bio\"}");
        long albumId = postJson("/api/albums", "{\"title\":\"First\","
                + "\"releaseDate\":\"2024-01-01\",\"artistId\":"
                + artistId + "}");
        long userId = postJson("/api/users", "{\"username\":\"tester\","
                + "\"email\":\"t@example.com\",\"displayName\":\"T\"}");

        MockMultipartFile audio = new MockMultipartFile("audioFile",
                "track.mp3", "audio/mpeg", new byte[] {1, 2, 3, 4});
        MvcResult upload = mockMvc.perform(multipart("/api/songs")
                        .file(audio)
                        .param("title", "Night Drive")
                        .param("durationSeconds", "200")
                        .param("albumId", String.valueOf(albumId))
                        .param("genreIds", String.valueOf(genreId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.genres[0]", is("Synthwave")))
                .andReturn();
        long songId = idOf(upload);

        mockMvc.perform(get("/api/songs")
                        .param("q", "night")
                        .param("genre", "synthwave")
                        .param("sort", "playCount,desc")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page.totalElements", is(1)));

        mockMvc.perform(post("/api/songs/" + songId + "/play"))
                .andExpect(jsonPath("$.playCount", is(1)));

        long playlistId = postJson("/api/users/" + userId + "/playlists",
                "{\"name\":\"Mix\"}");
        mockMvc.perform(post("/api/playlists/" + playlistId + "/songs/"
                        + songId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDurationSeconds", is(200)));
        mockMvc.perform(post("/api/playlists/" + playlistId + "/songs/"
                        + songId))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/users/" + userId + "/liked-songs/"
                        + songId))
                .andExpect(jsonPath("$.likedSongCount", is(1)));
        mockMvc.perform(get("/api/songs/most-liked"))
                .andExpect(jsonPath("$[0].likes", is(1)));
        mockMvc.perform(get("/api/genres/stats"))
                .andExpect(jsonPath("$[0].songCount", is(1)));

        mockMvc.perform(get("/api/songs/" + songId + "/stream"))
                .andExpect(status().isOk());
    }

    @Test
    void invalidInputIsRejected() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"x\",\"email\":\"bad\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.displayName").exists());

        MockMultipartFile text = new MockMultipartFile("audioFile",
                "notes.txt", "text/plain", new byte[] {1});
        mockMvc.perform(multipart("/api/songs").file(text)
                        .param("title", "Bad")
                        .param("durationSeconds", "10")
                        .param("albumId", "999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/songs/12345"))
                .andExpect(status().isNotFound());
    }

    private long postJson(String url, String json) throws Exception {
        MvcResult result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return idOf(result);
    }

    private long idOf(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(
                result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }
}
