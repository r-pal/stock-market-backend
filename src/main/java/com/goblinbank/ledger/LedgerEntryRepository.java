package com.goblinbank.ledger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

  @Query(
      "select e from LedgerEntry e where (:accountId is null or e.account.id = :accountId) order by e.createdAt desc")
  Page<LedgerEntry> search(@Param("accountId") Long accountId, Pageable pageable);
}

