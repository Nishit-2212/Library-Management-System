package com.library.management;

import com.library.management.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryManagementSystemApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RegistrationService registrationService;

	@Test
	void contextLoads() {
	}

	@Test
	void getSignup_showsForm() throws Exception {
		mockMvc.perform(get("/signup"))
				.andExpect(status().isOk())
				.andExpect(view().name("signup"))
				.andExpect(model().attributeExists("registrationDto"));
	}

	@Test
	void postSignup_success_redirects() throws Exception {
		doNothing().when(registrationService).registerUser(any());

		mockMvc.perform(post("/signup")
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("name", "Test User")
						.param("email", "test@example.com")
						.param("password", "P@ssw0rd")
						.param("role", "GUEST")
				)
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/login"));
	}

	@Test
	void postSignup_failure_showsError() throws Exception {
		doThrow(new RuntimeException("db error")).when(registrationService).registerUser(any());

		mockMvc.perform(post("/signup")
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("name", "Test User")
						.param("email", "test@example.com")
						.param("password", "P@ssw0rd")
						.param("role", "GUEST")
				)
				.andExpect(status().isOk())
				.andExpect(view().name("signup"))
				.andExpect(model().attributeExists("error"));
	}

	@Test
	void getLogin_showsLogin() throws Exception {
		mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"));
	}

}
