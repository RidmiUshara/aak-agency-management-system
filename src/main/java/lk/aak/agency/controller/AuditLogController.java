package lk.aak.agency.controller;

import lk.aak.agency.model.AuditLog;
import lk.aak.agency.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Owner-only: read-only view of who deleted, approved or overrode what, and when. */
@Controller
@RequestMapping("/audit-log")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private static final int PAGE_SIZE = 25;

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String showAuditLog(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<AuditLog> logPage = auditLogService.getLogPage(search, page, PAGE_SIZE);

        model.addAttribute("logs", logPage.getContent());
        model.addAttribute("search", search);
        model.addAttribute("currentPage", logPage.getNumber());
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalRecords", logPage.getTotalElements());

        return "audit-log/audit-log-list";
    }
}
