package lk.aak.agency.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoleBasedAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousUser_isRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void salesRole_canViewCustomers() throws Exception {
        mockMvc.perform(get("/customers").with(user("sales1").roles("SALES")))
                .andExpect(status().isOk());
    }

    @Test
    void salesRole_isForbiddenFromPayments() throws Exception {
        mockMvc.perform(get("/payments").with(user("sales1").roles("SALES")))
                .andExpect(status().isForbidden());
    }

    @Test
    void accountsRole_canViewPayments() throws Exception {
        mockMvc.perform(get("/payments").with(user("accounts1").roles("ACCOUNTS")))
                .andExpect(status().isOk());
    }

    @Test
    void salesRole_cannotDeleteCustomer_evenThoughModuleIsAllowed() throws Exception {
        mockMvc.perform(post("/customers/delete/1")
                        .with(user("sales1").roles("SALES"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRole_canReachEveryModule() throws Exception {
        mockMvc.perform(get("/customers").with(user("admin1").roles("ADMIN"))).andExpect(status().isOk());
        mockMvc.perform(get("/products").with(user("admin1").roles("ADMIN"))).andExpect(status().isOk());
        mockMvc.perform(get("/payments").with(user("admin1").roles("ADMIN"))).andExpect(status().isOk());
    }
}
