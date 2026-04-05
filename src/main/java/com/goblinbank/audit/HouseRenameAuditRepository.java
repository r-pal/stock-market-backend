package com.goblinbank.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseRenameAuditRepository extends JpaRepository<HouseRenameAudit, Long> {}
