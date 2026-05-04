package com.group24.projectselection.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error/403")
    public String forbiddenPage() {
        return "error/403";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode == null) {
            return "error/500";
        }

        int status = Integer.parseInt(statusCode.toString());
        if (status == HttpStatus.FORBIDDEN.value()) {
            return "error/403";
        }
        if (status == HttpStatus.NOT_FOUND.value()) {
            return "error/404";
        }
        return "error/500";
    }
}
