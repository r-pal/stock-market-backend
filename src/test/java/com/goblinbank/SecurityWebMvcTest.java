package com.goblinbank;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goblinbank.account.AccountService;
import com.goblinbank.account.PublicAccountQueryService;
import com.goblinbank.banker.BankerController;
import com.goblinbank.security.JwtAuthenticationFilter;
import com.goblinbank.security.JwtProperties;
import com.goblinbank.security.JwtService;
import com.goblinbank.game.GameAdminService;
import com.goblinbank.game.GameClockConfig;
import com.goblinbank.game.GameClockService;
import com.goblinbank.game.HistoryConfigRepository;
import com.goblinbank.house.HouseStockController;
import com.goblinbank.ledger.LedgerEntryRepository;
import com.goblinbank.market.InvestmentPositionRepository;
import com.goblinbank.market.InvestmentService;
import com.goblinbank.shareprice.SharePriceConfigRepository;
import com.goblinbank.ticker.TickerBaselineService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {BankerController.class, HouseStockController.class})
@Import({
  com.goblinbank.security.SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
})
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(
    properties = {"goblin.jwt.secret=test-secret-at-least-32-characters-long!!"})
class SecurityWebMvcTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AccountService accountService;
  @MockBean private InvestmentService investmentService;
  @MockBean private InvestmentPositionRepository investmentPositionRepository;
  @MockBean private SharePriceConfigRepository sharePriceConfigRepository;
  @MockBean private HistoryConfigRepository historyConfigRepository;
  @MockBean private GameAdminService gameAdminService;
  @MockBean private GameClockService gameClockService;
  @MockBean private LedgerEntryRepository ledgerEntryRepository;
  @MockBean private TickerBaselineService tickerBaselineService;
  @MockBean private PublicAccountQueryService publicAccountQueryService;

  @BeforeEach
  void setupGameStatus() {
    GameClockConfig g = new GameClockConfig();
    g.setGameDurationMinutes(1440);
    g.setGameStartAt(null);
    when(gameAdminService.getConfig()).thenReturn(g);
    when(gameClockService.currentElapsedMinutes(any(Instant.class))).thenReturn(0);
  }

  @Test
  void bankerForbiddenWithoutAuth() throws Exception {
    mockMvc.perform(get("/api/banker/game/status")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "BANKER")
  void bankerOkWithRole() throws Exception {
    mockMvc.perform(get("/api/banker/game/status")).andExpect(status().isOk());
  }

  @Test
  void houseForbiddenWithoutAuth() throws Exception {
    mockMvc.perform(get("/api/house/account")).andExpect(status().isForbidden());
  }
}
