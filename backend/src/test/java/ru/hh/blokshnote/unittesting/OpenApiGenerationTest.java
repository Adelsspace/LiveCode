package ru.hh.blokshnote.unittesting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OpenApiGenerationTest extends AbstractIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private final static Path outputPath = Paths.get("./openapi/openapi.json");
  private final static String DOCS_URI = "/v3/api-docs";

  @BeforeAll
  public static void setUp() throws Exception {
    Files.createDirectories(outputPath.getParent());
    Files.deleteIfExists(outputPath);
  }

  @Test
  public void shouldGenerateOpenApiSpec() throws Exception {
    MvcResult result = mockMvc.perform(get(DOCS_URI))
        .andExpect(status().isOk())
        .andReturn();
    String responseContent = result.getResponse().getContentAsString();
    JsonNode json = objectMapper.readTree(responseContent);
    objectMapper.writeValue(outputPath.toFile(), json);
  }
}
