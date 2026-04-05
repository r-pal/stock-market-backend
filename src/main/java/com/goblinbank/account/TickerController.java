package com.goblinbank.account;

import com.goblinbank.ticker.TickerFormatterService;
import com.goblinbank.web.dto.TickerResponseDto;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class TickerController {

  private final TickerFormatterService formatter;

  public TickerController(TickerFormatterService formatter) {
    this.formatter = formatter;
  }

  @GetMapping("/ticker")
  public TickerResponseDto ticker() {
    return new TickerResponseDto(formatter.buildMessage(Instant.now()));
  }
}
