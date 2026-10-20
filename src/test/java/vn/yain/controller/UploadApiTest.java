package vn.yain.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UploadApiTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("API Upload: Tải lên hình ảnh thành công trả về CDN URL")
    void testUploadImage_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "court-sample.jpg",
                "image/jpeg",
                "sample-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/upload/image")
                        .file(file)
                        .param("folder", "courts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.url", containsString("cloudinary.com")))
                .andExpect(jsonPath("$.folder", is("courts")));
    }

    @Test
    @DisplayName("API Upload: Báo lỗi khi tệp tải lên rỗng")
    void testUploadImage_EmptyFile_BadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/upload/image")
                        .file(emptyFile)
                        .param("folder", "products"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("không được để trống")));
    }
}
