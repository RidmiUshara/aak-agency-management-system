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
    void salesRepRole_canViewCustomers() throws Exception {
        mockMvc.perform(get("/customers").with(user("sales1").roles("SALES_REP")))
                .andExpect(status().isOk());
    }

    @Test
    void salesRepRole_isForbiddenFromPayments() throws Exception {
        mockMvc.perform(get("/payments").with(user("sales1").roles("SALES_REP")))
                .andExpect(status().isForbidden());
    }

    @Test
    void officeRole_canViewPayments() throws Exception {
        mockMvc.perform(get("/payments").with(user("office1").roles("OFFICE")))
                .andExpect(status().isOk());
    }

    @Test
    void salesRepRole_cannotDeleteCustomer_evenThoughModuleIsAllowed() throws Exception {
        mockMvc.perform(post("/customers/delete/1")
                        .with(user("sales1").roles("SALES_REP"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void officeRole_isForbiddenFromUserManagement() throws Exception {
        mockMvc.perform(get("/users").with(user("office1").roles("OFFICE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRole_canManageUsers() throws Exception {
        mockMvc.perform(get("/users").with(user("admin1").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void officeRole_canManageEmployeesAndVehicles() throws Exception {
        mockMvc.perform(get("/employees").with(user("office1").roles("OFFICE"))).andExpect(status().isOk());
        mockMvc.perform(get("/vehicles").with(user("office1").roles("OFFICE"))).andExpect(status().isOk());
    }

    @Test
    void officeRole_canManageRoutesAndDeliveryTrips() throws Exception {
        mockMvc.perform(get("/routes").with(user("office1").roles("OFFICE"))).andExpect(status().isOk());
        mockMvc.perform(get("/delivery-trips").with(user("office1").roles("OFFICE"))).andExpect(status().isOk());
    }

    @Test
    void salesRepRole_isForbiddenFromRoutesAndDeliveryTrips() throws Exception {
        mockMvc.perform(get("/routes").with(user("sales1").roles("SALES_REP"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/delivery-trips").with(user("sales1").roles("SALES_REP"))).andExpect(status().isForbidden());
    }

    @Test
    void salesRepRole_isForbiddenFromEmployeesAndVehicles() throws Exception {
        mockMvc.perform(get("/employees").with(user("sales1").roles("SALES_REP"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/vehicles").with(user("sales1").roles("SALES_REP"))).andExpect(status().isForbidden());
    }

    @Test
    void officeRole_canManageReturns() throws Exception {
        mockMvc.perform(get("/shop-returns").with(user("office1").roles("OFFICE"))).andExpect(status().isOk());
        mockMvc.perform(get("/supplier-returns").with(user("office1").roles("OFFICE"))).andExpect(status().isOk());
    }

    @Test
    void salesRepRole_isForbiddenFromReturns() throws Exception {
        mockMvc.perform(get("/shop-returns").with(user("sales1").roles("SALES_REP"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/supplier-returns").with(user("sales1").roles("SALES_REP"))).andExpect(status().isForbidden());
    }

    @Test
    void officeRole_canManageAttendanceAdvancesAndSalary() throws Exception {
        mockMvc.perform(get("/attendance").with(user("office1").roles("OFFICE"))).andExpect(status().isOk());
        mockMvc.perform(get("/advances").with(user("office1").roles("OFFICE"))).andExpect(status().isOk());
        mockMvc.perform(get("/salary").with(user("office1").roles("OFFICE"))).andExpect(status().isOk());
    }

    @Test
    void salesRepRole_isForbiddenFromAttendanceAdvancesAndSalary() throws Exception {
        mockMvc.perform(get("/attendance").with(user("sales1").roles("SALES_REP"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/advances").with(user("sales1").roles("SALES_REP"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/salary").with(user("sales1").roles("SALES_REP"))).andExpect(status().isForbidden());
    }

    @Test
    void adminRole_canReachEveryModule() throws Exception {
        mockMvc.perform(get("/customers").with(user("admin1").roles("ADMIN"))).andExpect(status().isOk());
        mockMvc.perform(get("/products").with(user("admin1").roles("ADMIN"))).andExpect(status().isOk());
        mockMvc.perform(get("/payments").with(user("admin1").roles("ADMIN"))).andExpect(status().isOk());
    }

    @Test
    void officeRole_canViewReports() throws Exception {
        mockMvc.perform(get("/reports").with(user("office1").roles("OFFICE"))).andExpect(status().isOk());
    }

    @Test
    void salesRepRole_isForbiddenFromReports() throws Exception {
        mockMvc.perform(get("/reports").with(user("sales1").roles("SALES_REP"))).andExpect(status().isForbidden());
    }

    @Test
    void adminRole_canViewAuditLog() throws Exception {
        mockMvc.perform(get("/audit-log").with(user("admin1").roles("ADMIN"))).andExpect(status().isOk());
    }

    @Test
    void officeRole_isForbiddenFromAuditLog() throws Exception {
        mockMvc.perform(get("/audit-log").with(user("office1").roles("OFFICE"))).andExpect(status().isForbidden());
    }
}
