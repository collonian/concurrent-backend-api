package com.example.demo.api;

import com.example.demo.api.config.JacksonConfig;
import com.example.demo.service.investment.InvestmentService;
import com.example.demo.service.investment.vo.Investment;
import com.example.demo.service.investment.vo.InvestmentParam;
import com.example.demo.service.product.ProductService;
import com.example.demo.service.user.DemoUserDetailsService;
import com.example.demo.service.user.vo.DemoUserDetails;
import com.example.demo.service.user.vo.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvestApi.class)
@ActiveProfiles("test")
@Import(JacksonConfig.class)
class InvestApiTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mvc;
    @MockBean
    private InvestmentService investmentService;
    @MockBean
    private ProductService productService;
    @MockBean
    private DemoUserDetailsService demoUserDetailsService;

    @BeforeEach
    public void beforeAll() {
        when(demoUserDetailsService.loadUserDetails(any()))
                .thenReturn(new DemoUserDetails(new User(BigDecimal.ONE)));
    }

    @Test
    public void shouldThrowInvalidAmountException_whenInvest_givenNegativeAmount() throws Exception {
        // when
        String content = objectMapper.writeValueAsString(
                InvestmentParam.create(BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("-123"))
        );
        ResultActions result = mvc
                .perform(
                        post("/api/investments")
                                .header("X-USER-ID", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        // then
        result
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("error_code").value("INVALID_INVESTMENT_AMOUNT"));
    }

    @Test
    public void shouldThrowUnmatchedUser_whenInvest_givenUnmatchedUser() throws Exception {
        // when
        String content = objectMapper.writeValueAsString(
                InvestmentParam.create(BigDecimal.ONE, BigDecimal.TEN, new BigDecimal("123"))
        );
        ResultActions result = mvc
                .perform(
                        post("/api/investments")
                                .header("X-USER-ID", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        // then
        result
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("error_code").value("UNMATCHED_USER"));
    }

    @Test
    public void shouldSucceed_whenInvest_givenNormalInvestment() throws Exception {
        // given
        when(investmentService.invest(any()))
                .thenReturn(Investment.create(
                        BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("123"),
                        "some product", new BigDecimal("5432")
                ));

        // when
        String content = objectMapper.writeValueAsString(
                InvestmentParam.create(BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("123"))
        );
        ResultActions result = mvc
                .perform(
                        post("/api/investments")
                                .header("X-USER-ID", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );


        // then
        result
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("productId").value(10))
                .andExpect(jsonPath("investingAmount").value(123))
                .andExpect(jsonPath("totalInvestingAmount").value(5432))
        ;
    }
}