package com.goblinbank.account;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface HouseAccountRepository extends JpaRepository<HouseAccount, Long> {
  @Query("select a from HouseAccount a where a.deletedAt is null order by a.id asc")
  List<HouseAccount> findAllActive();

  @Query("select a from HouseAccount a where a.id = :id and a.deletedAt is null")
  Optional<HouseAccount> findActiveById(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from HouseAccount a where a.id = :id and a.deletedAt is null")
  Optional<HouseAccount> findActiveByIdForUpdate(Long id);

  boolean existsByDeletedAtIsNullAndHouseNameIgnoreCase(String houseName);
}

