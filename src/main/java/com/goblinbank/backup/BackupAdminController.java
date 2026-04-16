package com.goblinbank.backup;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/banker/backups")
@SecurityRequirement(name = "bearerAuth")
public class BackupAdminController {

  private final BackupService backupService;

  public BackupAdminController(BackupService backupService) {
    this.backupService = backupService;
  }

  @GetMapping
  public List<String> list() {
    return backupService.listBackupsNewestFirst().stream().map(p -> p.getFileName().toString()).toList();
  }

  @PostMapping("/run")
  public String runNow(Authentication auth) {
    Path p = backupService.writeBackupNow("banker:" + auth.getName());
    return p.getFileName().toString();
  }

  @GetMapping("/latest")
  public ResponseEntity<byte[]> latest() throws IOException {
    Path p = backupService.latestBackup().orElseThrow(() -> new IllegalArgumentException("No backups found"));
    byte[] bytes = Files.readAllBytes(p);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + p.getFileName() + "\"")
        .contentType(MediaType.APPLICATION_JSON)
        .body(bytes);
  }
}

