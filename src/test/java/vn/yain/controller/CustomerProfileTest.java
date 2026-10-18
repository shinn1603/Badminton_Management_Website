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
import vn.yain.entity.Customer;
import vn.yain.entity.User;
import vn.yain.repository.CustomerRepository;
import vn.yain.repository.UserRepository;
import vn.yain.security.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class CustomerProfileTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;
    private Customer testCustomer;
    private String validToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        String username = "cust_profile_test";
        String phone = "0987654321";

        userRepository.findByUsername(username).ifPresent(userRepository::delete);
        customerRepository.findByPhone(phone).ifPresent(customerRepository::delete);

        testUser = new User();
        testUser.setUsername(username);
        testUser.setPassword("$2a$10$abcdefghijklmnopqrstuvwxyz1234567890ABCDEF");
        testUser.setFullName("Nguyễn Văn Triết");
        testUser.setPhone(phone);
        testUser.setEmail("triet.test@utesport.vn");
        testUser.setRole("CUSTOMER");
        testUser.setBranchCode("CN01");
        testUser.setStatus("Hoạt động");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        testCustomer = new Customer();
        testCustomer.setCustomerCode("KH9999");
        testCustomer.setFullName("Nguyễn Văn Triết");
        testCustomer.setPhone(phone);
        testCustomer.setEmail("triet.test@utesport.vn");
        testCustomer.setPassword(testUser.getPassword());
        testCustomer.setMembershipTier("Vàng");
        testCustomer.setRewardPoints(150);
        testCustomer.setStatus("Hoạt động");
        testCustomer.setCreatedAt(LocalDateTime.now());
        testCustomer = customerRepository.save(testCustomer);

        validToken = jwtTokenProvider.generateToken(testUser.getUsername(), testUser.getRole(), testUser.getFullName(), testUser.getBranchCode());
    }

    @Test
    @DisplayName("Thymeleaf View: Render trang /customer/profile thanh cong")
    void testCustomerProfileView_RendersSuccessfully() throws Exception {
        mockMvc.perform(get("/customer/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/profile"));
    }

    @Test
    @DisplayName("API Profile: Tu choi truy cap khi khong co token JWT")
    void testGetCustomerProfile_UnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/customer/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("API Profile: Lay ho so khach hang thanh cong voi token hop le")
    void testGetCustomerProfile_Success() throws Exception {
        mockMvc.perform(get("/api/customer/profile")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.fullName", is("Nguyễn Văn Triết")))
                .andExpect(jsonPath("$.phone", is("0987654321")))
                .andExpect(jsonPath("$.email", is("triet.test@utesport.vn")))
                .andExpect(jsonPath("$.customerCode", is("KH9999")))
                .andExpect(jsonPath("$.membershipTier", is("Vàng")))
                .andExpect(jsonPath("$.rewardPoints", is(150)));
    }

    @Test
    @DisplayName("API Profile: Cap nhat ho so khach hang va dong bo User - Customer")
    void testUpdateCustomerProfile_Success() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("fullName", "Nguyễn Văn Triết (Updated)");
        payload.put("email", "triet.new@utesport.vn");
        payload.put("newPassword", "NewSecurePass123");

        mockMvc.perform(put("/api/customer/profile")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.fullName", is("Nguyễn Văn Triết (Updated)")))
                .andExpect(jsonPath("$.email", is("triet.new@utesport.vn")));

        User updatedUser = userRepository.findByUsername("cust_profile_test").orElseThrow();
        assertEquals("Nguyễn Văn Triết (Updated)", updatedUser.getFullName());
        assertEquals("triet.new@utesport.vn", updatedUser.getEmail());

        Customer updatedCustomer = customerRepository.findByPhone("0987654321").orElseThrow();
        assertEquals("Nguyễn Văn Triết (Updated)", updatedCustomer.getFullName());
        assertEquals("triet.new@utesport.vn", updatedCustomer.getEmail());
    }

    @Test
    @DisplayName("API Profile: Tu choi cap nhat khi khong co token JWT")
    void testUpdateCustomerProfile_UnauthorizedWithoutToken() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("fullName", "Attacker");

        mockMvc.perform(put("/api/customer/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }
}
