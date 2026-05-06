package com.group24.projectselection;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class Pbi1FormComponentContractTest {

    private static final Path TEMPLATE_ROOT = Path.of("src/main/resources/templates");

    @Test
    void reusableFormComponents_existForInputSelectAndButton() throws IOException {
        String register = readTemplate("register.html");
        assertThat(register).contains("class=\"modern-input\"");
        assertThat(register).contains("class=\"modern-select\"");
        assertThat(register).contains("class=\"modern-btn modern-btn-primary");
    }

    @Test
    void requiredFields_blockEmptySubmitAtBrowserLevel() throws IOException {
        String register = readTemplate("register.html");
        String login = readTemplate("login.html");

        assertThat(register).contains("id=\"name\"");
        assertThat(register).contains("id=\"email\"");
        assertThat(register).contains("id=\"password\"");
        assertThat(register).contains("required");

        assertThat(login).contains("id=\"username\"");
        assertThat(login).contains("id=\"password\"");
        assertThat(login).contains("required");
    }

    @Test
    void sharedStyles_areConsistentAcrossMultipleModules() throws IOException {
        String register = readTemplate("register.html");
        String login = readTemplate("login.html");
        String forgotPassword = readTemplate("forgot-password.html");

        assertThat(register).contains("class=\"modern-form-group\"");
        assertThat(login).contains("class=\"modern-form-group\"");
        assertThat(forgotPassword).contains("class=\"modern-form-group\"");

        assertThat(register).contains("class=\"modern-btn modern-btn-primary");
        assertThat(login).contains("class=\"modern-btn modern-btn-primary");
        assertThat(forgotPassword).contains("class=\"modern-btn modern-btn-primary");
    }

    private String readTemplate(String fileName) throws IOException {
        return Files.readString(TEMPLATE_ROOT.resolve(fileName), StandardCharsets.UTF_8);
    }
}
