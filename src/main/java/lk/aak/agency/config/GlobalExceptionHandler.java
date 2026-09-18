package lk.aak.agency.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;

/** Turns common unhandled exceptions into a redirect + flash message instead of a raw 500 page. */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        LOGGER.warn("Data integrity violation on {}: {}", request.getRequestURI(), exception.getMessage());

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "That action couldn't be completed because it conflicts with an existing record "
                        + "(duplicate code) or is still referenced by other records."
        );

        return "redirect:" + refererOrHome(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        LOGGER.warn("Rejected request to {}: {}", request.getRequestURI(), exception.getMessage());

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                exception.getMessage() != null ? exception.getMessage() : "The request could not be processed."
        );

        return "redirect:" + refererOrHome(request);
    }

    // Only redirect back to a same-origin path from Referer - never follow it as an open redirect.
    private String refererOrHome(HttpServletRequest request) {
        String referer = request.getHeader("Referer");

        if (referer == null || referer.isBlank()) {
            return "/";
        }

        try {
            URI refererUri = URI.create(referer);
            String requestScheme = request.getScheme();
            String requestHost = request.getServerName();
            int requestPort = request.getServerPort();

            boolean sameOrigin = refererUri.getHost() != null
                    && refererUri.getHost().equalsIgnoreCase(requestHost)
                    && refererUri.getScheme() != null
                    && refererUri.getScheme().equalsIgnoreCase(requestScheme)
                    && (refererUri.getPort() == requestPort
                        || (refererUri.getPort() == -1 && requestPort == (requestScheme.equals("https") ? 443 : 80)));

            if (!sameOrigin) {
                return "/";
            }

            String path = refererUri.getRawPath();
            return (path == null || path.isBlank()) ? "/" : path;

        } catch (IllegalArgumentException malformedUri) {
            return "/";
        }
    }
}
