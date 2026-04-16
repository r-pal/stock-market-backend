package com.goblinbank.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BackupService {

  private static final DateTimeFormatter TS =
      DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);

  private final BackupProperties props;
  private final JdbcTemplate jdbc;
  private final ObjectMapper mapper;

  public BackupService(BackupProperties props, JdbcTemplate jdbc, ObjectMapper mapper) {
    this.props = props;
    this.jdbc = jdbc;
    this.mapper = mapper;
  }

  public Path backupDir() {
    return Paths.get(props.getDir()).toAbsolutePath().normalize();
  }

  public List<Path> listBackupsNewestFirst() {
    Path dir = backupDir();
    if (!Files.exists(dir)) {
      return List.of();
    }
    try (var stream = Files.list(dir)) {
      return stream
          .filter(p -> p.getFileName().toString().startsWith("goblin-backup-"))
          .filter(p -> p.getFileName().toString().endsWith(".json"))
          .sorted(Comparator.comparing(Path::getFileName).reversed())
          .toList();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to list backups", e);
    }
  }

  public Optional<Path> latestBackup() {
    var all = listBackupsNewestFirst();
    return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
  }

  @Transactional(readOnly = true)
  public Path writeBackupNow(String performer) {
    if (!props.isEnabled()) {
      throw new IllegalStateException("Backups disabled");
    }
    Instant now = Instant.now();
    Path dir = backupDir();
    try {
      Files.createDirectories(dir);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create backup dir", e);
    }

    String filename = "goblin-backup-" + TS.format(now) + ".json";
    Path out = dir.resolve(filename);

    Map<String, Object> root = new LinkedHashMap<>();
    root.put("createdAt", now.toString());
    root.put("createdBy", performer);
    root.put("tables", dumpAllTables());

    try {
      Path tmp = dir.resolve(filename + ".tmp");
      mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), root);
      Files.move(tmp, out, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to write backup", e);
    }

    pruneOld(dir);
    return out;
  }

  private Map<String, Object> dumpAllTables() {
    Map<String, Object> tables = new LinkedHashMap<>();
    // Game core
    tables.put("house_account", dumpTable("house_account"));
    tables.put("tradable_stock", dumpTable("tradable_stock"));
    tables.put("investment_position", dumpTable("investment_position"));
    tables.put("ledger_entry", dumpTable("ledger_entry"));
    tables.put("house_wealth_snapshot", dumpTable("house_wealth_snapshot"));
    tables.put("house_ticker_baseline", dumpTable("house_ticker_baseline"));
    // Config/singletons
    tables.put("global_interest_config", dumpTable("global_interest_config"));
    tables.put("share_price_config", dumpTable("share_price_config"));
    tables.put("history_config", dumpTable("history_config"));
    tables.put("game_clock_config", dumpTable("game_clock_config"));
    // Auth/admin/audit
    tables.put("app_user", dumpTable("app_user"));
    tables.put("house_rename_audit", dumpTable("house_rename_audit"));
    // Scheduler bookkeeping (helps debug missed jobs)
    tables.put("scheduler_run", dumpTable("scheduler_run"));
    // Flyway history (helps verify schema)
    tables.put("flyway_schema_history", dumpTable("flyway_schema_history"));
    return tables;
  }

  private List<Map<String, Object>> dumpTable(String table) {
    return jdbc.queryForList("select * from " + table);
  }

  private void pruneOld(Path dir) {
    int retain = props.getRetain();
    if (retain <= 0) {
      return;
    }
    List<Path> all = listBackupsNewestFirst();
    if (all.size() <= retain) {
      return;
    }
    List<Path> toDelete = all.subList(retain, all.size());
    for (Path p : toDelete) {
      try {
        Files.deleteIfExists(p);
      } catch (IOException e) {
        // best-effort prune
      }
    }
  }
}

