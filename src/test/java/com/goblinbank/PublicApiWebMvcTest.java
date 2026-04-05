package com.goblinbank;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goblinbank.account.PublicAccountController;
import com.goblinbank.account.PublicAccountQueryService;
import com.goblinbank.account.TickerController;
import com.goblinbank.security.JwtAuthenticationFilter;
import com.goblinbank.ticker.TickerFormatterService;
import com.goblinbank.web.dto.HistoryResponseDto;
import com.goblinbank.web.dto.PublicAccountsResponseDto;
import com.goblinbank.web.dto.TickerResponseDto;
import com.goblinbank.wealth.WealthHistoryController;
import com.goblinbank.wealth.WealthHistoryQueryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = {
      PublicAccountController.class,
      TickerController.class,
      WealthHistoryController.class
    },
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicApiWebMvcTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PublicAccountQueryService publicAccountQueryService;
  @MockBean private TickerFormatterService tickerFormatterService;
  @MockBean private WealthHistoryQueryService wealthHistoryQueryService;
  @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  void accountsReturnsOk() throws Exception {
    when(publicAccountQueryService.listAll(any()))
        .thenReturn(new PublicAccountsResponseDto("0.05", List.of()));
    mockMvc
        .perform(get("/api/public/accounts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.baseRatePerHour").value("0.05"));
  }

  @Test
  void tickerReturnsOk() throws Exception {
    when(tickerFormatterService.buildMessage(any()))
        .thenReturn("House1 ₲1.00 - 0.0%");
    mockMvc
        .perform(get("/api/public/ticker"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void historyReturnsOk() throws Exception {
    when(wealthHistoryQueryService.buildHistory(isNull(), isNull(), isNull(), any()))
        .thenReturn(
            new HistoryResponseDto(20, 1440, 0, 1440, "₲", List.of()));
    mockMvc
        .perform(get("/api/public/history"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.intervalMinutes").value(20));
  }
}
