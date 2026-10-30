package vn.yain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import vn.yain.entity.Branch;
import vn.yain.entity.User;
import vn.yain.repository.BranchRepository;
import vn.yain.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AdminUserManagementTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        if (branchRepository.findByBranchCode("CN01").isEmpty()) {
            Branch b = new Branch();
            b.setBranchCode("CN01");
            b.setBranchName("Chi nhánh 1 - Sân Cầu Lông Thủ Đức");
            b.setAddress("Số 1 Võ Văn Ngân, Thủ Đức");
            branchRepository.save(b);
        }
    }

    @Test
    @DisplayName("Admin View: Render trang /admin/users thanh cong")
    void testAdminUsersView_RendersSuccessfully() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    @DisplayName("Admin View: Render trang /admin/permissions thanh cong")
    void testAdminPermissionsView_RendersSuccessfully() throws Exception {
        mockMvc.perform(get("/admin/permissions"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/permissions"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("branches"));
    }

    @Test
    @DisplayName("Admin API: GET /api/admin/users tra ve danh sach nguoi dung")
    void testGetAdminUsersApi() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @DisplayName("Admin API: POST /api/admin/users tao moi va cap nhat tai khoan")
    void testSaveAdminUserApi() throws Exception {
        String username = "test_staff_day05";
        userRepository.findByUsername(username).ifPresent(userRepository::delete);

        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("password", "StaffPass@123");
        payload.put("fullName", "Nguyen Van Staff");
        payload.put("role", "POS");
        payload.put("branchCode", "CN01");
        payload.put("phone", "0911223344");

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.user.username", is(username)))
                .andExpect(jsonPath("$.user.role", is("POS")));

        User created = userRepository.findByUsername(username).orElse(null);
        assertNotNull(created);
        assertEquals("Nguyen Van Staff", created.getFullName());

        payload.put("fullName", "Nguyen Van Staff Updated");
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        User updated = userRepository.findByUsername(username).orElse(null);
        assertNotNull(updated);
        assertEquals("Nguyen Van Staff Updated", updated.getFullName());
    }

    @Test
    @DisplayName("Admin API: POST /api/admin/users/{username}/toggle-status khoa va mo khoa tai khoan")
    void testToggleAdminUserStatusApi() throws Exception {
        String username = "toggle_test_user";
        User u = userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword("Pass123456");
            newUser.setFullName("User Can Khoa");
            newUser.setRole("CUSTOMER");
            newUser.setStatus("Hoạt động");
            return userRepository.save(newUser);
        });

        mockMvc.perform(post("/api/admin/users/" + username + "/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is("Bị khóa")));

        mockMvc.perform(post("/api/admin/users/" + username + "/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is("Hoạt động")));
    }

    @Test
    @DisplayName("Admin API: GET & POST /api/admin/permissions quan ly ma tran phan quyen chi nhanh")
    void testAdminPermissionsApi() throws Exception {
        mockMvc.perform(get("/api/admin/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.users", not(empty())))
                .andExpect(jsonPath("$.branches", not(empty())));

        String username = "perm_test_user";
        User u = userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword("Pass123456");
            newUser.setFullName("Perm Test User");
            newUser.setRole("MANAGER");
            newUser.setBranchCode("CN01");
            return userRepository.save(newUser);
        });

        List<Map<String, String>> updates = List.of(
                Map.of("username", username, "branchCode", "CN02")
        );

        mockMvc.perform(post("/api/admin/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.updatedCount", is(1)));

        User reloaded = userRepository.findByUsername(username).orElseThrow();
        assertEquals("CN02", reloaded.getBranchCode());
    }
}
