package lk.aak.agency.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void savePayment_withBlankReceiptNumber_returnsToFormWithFieldError() throws Exception {

        mockMvc.perform(post("/payments/save")
                        .with(user("office1").roles("OFFICE"))
                        .with(csrf())
                        .param("receiptNumber", "")
                        .param("paymentDate", "2026-09-19")
                        .param("amount", "500")
                        .param("paymentMethod", "CASH")
                        .param("salesInvoiceId", "999999"))
                .andExpect(status().isOk())
                .andExpect(view().name("payments/payment-form"))
                .andExpect(model().attributeHasFieldErrors("payment", "receiptNumber"));
    }

    @Test
    void savePayment_withZeroAmount_returnsToFormWithFieldError() throws Exception {

        mockMvc.perform(post("/payments/save")
                        .with(user("office1").roles("OFFICE"))
                        .with(csrf())
                        .param("receiptNumber", "REC-TEST-001")
                        .param("paymentDate", "2026-09-19")
                        .param("amount", "0")
                        .param("paymentMethod", "CASH")
                        .param("salesInvoiceId", "999999"))
                .andExpect(status().isOk())
                .andExpect(view().name("payments/payment-form"))
                .andExpect(model().attributeHasFieldErrors("payment", "amount"));
    }
}
