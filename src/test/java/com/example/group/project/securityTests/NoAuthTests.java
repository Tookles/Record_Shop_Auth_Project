package com.example.group.project.securityTests;

import com.example.group.project.controller.PurchaseController;
import com.example.group.project.controller.RecordController;
import com.example.group.project.controller.UserController;
import com.example.group.project.security.WebSecurityConfig;
import com.example.group.project.service.impl.PurchaseServiceImpl;
import com.example.group.project.service.impl.RecordServiceImpl;
import com.example.group.project.service.impl.UserDetailsServiceImpl;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.lang.Assert;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({UserController.class, RecordController.class, PurchaseController.class})
@Import(WebSecurityConfig.class)
@ExtendWith(SpringExtension.class)
public class NoAuthTests {

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockBean
    private PurchaseServiceImpl purchaseServiceImpl;

    @MockBean
    private RecordServiceImpl recordServiceImpl;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() throws Exception {
        UserDetails staffDetails = User.builder()
                .username("staffUser")
                .password("$2a$10$kTJMa8NiWpcGW8GC.NtUy.q28VUCpO5H1/v9exYNr8BgUgN2yWi4q")
                .roles("STAFF")
                .build();


        when(userDetailsServiceImpl.loadUserByUsername("staffUser"))
                .thenReturn(staffDetails);


    }

    @Test
    public void GetWelcomeRequest_NoAuth_200Response()  throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    public void GetRecords_NoAuth_403Response()  throws Exception {
        mockMvc.perform(get("/auth/records"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void GetPurchases_NoAuth_403Response()  throws Exception {
        mockMvc.perform(get("/auth/getPurchases"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void PostPurchase_NoAuth_403Response()  throws Exception {
        mockMvc.perform(post("/auth/purchase"))
                .andExpect(status().isForbidden());
    }


    @Test
    public void LoginRequest_BadCredentials_404Response()  throws Exception {
        mockMvc.perform(post("/login") .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"username\": \"wrongUser\", \"password\": \"wrongpassword\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void LoginRequest_CorrectCredentials_200Response()  throws Exception {
        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"staffUser\", \"password\": \"goodbye\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void Login_CorrectCredentials_ReceiveJWTToken()  throws Exception {
        String tokenResult = mockMvc.perform(post("/login") .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"staffUser\", \"password\": \"goodbye\"}"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = "Bearer " + JsonPath.read(tokenResult, "$.accessToken");
        Assert.notNull(token);
    }

    @Test
    public void Login_IncorrectCredentials_DoNotReceiveJWTToken()  throws Exception {
        mockMvc.perform(post("/login") .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"staffUser\", \"password\": \"hello\"}"))
                .andExpect(jsonPath("$.accessToken").doesNotExist());

    }


    @Test
    public void AuthGetRequest_NoToken_403Response()  throws Exception {
        mockMvc.perform(get("/auth/records"))
                .andExpect(status().isForbidden());
    }


    @Test
    public void AuthGetRequest_InvalidToken_403Response()  throws Exception {
        mockMvc.perform(get("/auth/records")
                        .header("Authorization", "1234"))
                .andExpect(status().isForbidden());
    }


    @Test
    public void AuthDeleteRequest_NoTokens_403Response()  throws Exception {
        mockMvc.perform(delete("/auth/deletePurchase")
                        .param("id","1"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void AuthDeleteRequest_InvalidTokens_403Response()  throws Exception {
        mockMvc.perform(delete("/auth/deletePurchase")
                        .param("id","1")
                        .header("Authorization", "test")
                        .header("X-XSRF-TOKEN", "test")
                        .cookie(new Cookie("XSRF-TOKEN", "test")))
                .andExpect(status().isForbidden());
    }


}
