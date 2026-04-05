package com.goblinbank.audit;

import com.goblinbank.account.HouseAccount;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "house_rename_audit")
public class HouseRenameAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "house_id", nullable = false)
  private HouseAccount house;

  @Column(name = "old_name", nullable = false)
  private String oldName;

  @Column(name = "new_name", nullable = false)
  private String newName;

  @Column(name = "changed_by", nullable = false)
  private String changedBy;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  public void setHouse(HouseAccount house) {
    this.house = house;
  }

  public void setOldName(String oldName) {
    this.oldName = oldName;
  }

  public void setNewName(String newName) {
    this.newName = newName;
  }

  public void setChangedBy(String changedBy) {
    this.changedBy = changedBy;
  }
}
