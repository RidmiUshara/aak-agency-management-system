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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void saveCustomer_rejectsBlankName_andReturnsToForm() throws Exception {
        mockMvc.perform(post("/customers/save")
                        .with(user("sales1").roles("SALES"))
                        .with(csrf())
                        .param("customerName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/customer-form"));
    }

    @Test
    void saveCustomer_acceptsValidName_andRedirects() throws Exception {
        mockMvc.perform(post("/customers/save")
                        .with(user("sales1").roles("SALES"))
                        .with(csrf())
                        .param("customerName", "Valid Test Customer"))
                .andExpect(status().is3xxRedirection());
    }
}
