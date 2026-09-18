package lk.aak.agency.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminCanCreateSalesRepLogin() throws Exception {
        mockMvc.perform(post("/users/save")
                        .with(user("admin1").roles("ADMIN"))
                        .with(csrf())
                        .param("username", "rep_kamal")
                        .param("fullName", "Kamal Perera")
                        .param("role", "SALES_REP")
                        .param("password", "SecurePass123"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void creatingDuplicateUsername_isRejectedGracefully() throws Exception {
        mockMvc.perform(post("/users/save")
                        .with(user("admin1").roles("ADMIN"))
                        .with(csrf())
                        .param("username", "rep_duplicate")
                        .param("fullName", "First Attempt")
                        .param("role", "OFFICE")
                        .param("password", "SecurePass123"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/users/save")
                        .with(user("admin1").roles("ADMIN"))
                        .with(csrf())
                        .param("username", "rep_duplicate")
                        .param("fullName", "Second Attempt")
                        .param("role", "OFFICE")
                        .param("password", "SecurePass123"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void officeRoleCannotAccessUserManagement() throws Exception {
        mockMvc.perform(post("/users/save")
                        .with(user("office1").roles("OFFICE"))
                        .with(csrf())
                        .param("username", "someone")
                        .param("fullName", "Someone")
                        .param("role", "OFFICE")
                        .param("password", "SecurePass123"))
                .andExpect(status().isForbidden());
    }
}
