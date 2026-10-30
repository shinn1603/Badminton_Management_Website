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
import vn.yain.entity.AuditLog;
import vn.yain.entity.SystemSetting;
import vn.yain.entity.User;
import vn.yain.repository.AuditLogRepository;
import vn.yain.repository.BranchRepository;
import vn.yain.repository.SystemSettingRepository;
import vn.yain.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class SystemSettingsAndAuditLogsTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ApiController apiController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Thymeleaf Views: Render /admin/settings, /admin/logs va /customer/policy thanh cong")
    void testViewsRenderSuccessfully() throws Exception {
        mockMvc.perform(get("/admin/settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/settings"));

        mockMvc.perform(get("/admin/logs"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/logs"));

        mockMvc.perform(get("/customer/policy"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/policy"));

        mockMvc.perform(get("/policy"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/policy"));
    }

    @Test
    @DisplayName("API Policy Settings: GET /api/policy/settings tra ve cau hinh va chi nhanh")
    void testGetPublicPolicySettings() throws Exception {
        mockMvc.perform(get("/api/policy/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.settings.CFG_DEPOSIT_RATE", notNullValue()))
                .andExpect(jsonPath("$.branches", notNullValue()));
    }

    @Test
    @DisplayName("API Admin Settings: GET & POST /api/admin/settings cap nhat thong so va ghi audit log")
    void testAdminSettingsCrudAndAudit() throws Exception {
        mockMvc.perform(get("/api/admin/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.settings", notNullValue()));

        Map<String, String> payload = new HashMap<>();
        payload.put("CFG_DEPOSIT_RATE", "35");
        payload.put("CFG_HOLDING_MINUTES", "15");

        mockMvc.perform(post("/api/admin/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        SystemSetting depSetting = systemSettingRepository.findBySettingCode("CFG_DEPOSIT_RATE").orElse(null);
        assertNotNull(depSetting);
        assertEquals("35", depSetting.getSettingValue());

        mockMvc.perform(post("/api/admin/settings/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        SystemSetting resetSetting = systemSettingRepository.findBySettingCode("CFG_DEPOSIT_RATE").orElse(null);
        assertNotNull(resetSetting);
        assertEquals("30", resetSetting.getSettingValue());
    }

    @Test
    @DisplayName("API Audit Logs & Backup: GET /api/admin/logs va POST /api/admin/logs/backup")
    void testAdminAuditLogsAndBackup() throws Exception {
        Map<String, String> backupPayload = Map.of("filename", "backup_unit_test.sql.gz");
        mockMvc.perform(post("/api/admin/logs/backup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(backupPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.backup.actionType", containsString("Sao lưu")));

        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.logs", not(empty())));

        mockMvc.perform(get("/api/admin/logs/backups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.backups", not(empty())));
    }

    @Test
    @DisplayName("Password Recovery: Quen mat khau va Dat lai mat khau qua email OTP")
    void testForgotPasswordAndResetPasswordFlow() throws Exception {
        String username = "user_pwd_recovery_test";
        String email = "recovery_test@utesport.vn";

        userRepository.findByUsername(username).ifPresent(userRepository::delete);

        User testUser = new User();
        testUser.setUsername(username);
        testUser.setPassword("OldPassword@123");
        testUser.setFullName("User Recovery Test");
        testUser.setEmail(email);
        testUser.setPhone("0977889900");
        testUser.setRole("CUSTOMER");
        testUser.setStatus("Hoạt động");
        userRepository.save(testUser);

        Map<String, String> forgotPayload = Map.of("identifier", username);
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.email", containsString("***")));

        java.lang.reflect.Field otpField = ApiController.class.getDeclaredField("forgotPasswordOtpMap");
        otpField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> otpMap = (Map<String, Map<String, Object>>) otpField.get(apiController);
        String validOtp = (String) otpMap.get(username.toLowerCase()).get("otp");

        Map<String, String> resetPayload = Map.of(
                "identifier", username,
                "newPassword", "NewPassword@2026",
                "otp", validOtp
        );
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }
}
