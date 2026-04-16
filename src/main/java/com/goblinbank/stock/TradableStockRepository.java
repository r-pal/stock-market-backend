package com.goblinbank.stock;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TradableStockRepository extends JpaRepository<TradableStock, Long> {
  @Query("select s from TradableStock s left join fetch s.houseAccount where s.id = :id")
  Optional<TradableStock> findByIdFetched(Long id);

  @Query(
      "select s from TradableStock s left join fetch s.houseAccount where s.stockType = com.goblinbank.stock.StockType.HOUSE and s.houseAccount.id = :houseId")
  Optional<TradableStock> findHouseStockByHouseIdFetched(Long houseId);

  @Query("select s from TradableStock s left join fetch s.houseAccount where s.active = true order by s.id asc")
  List<TradableStock> findAllActiveFetched();

  @Query("select (count(s) > 0) from TradableStock s where s.active = true and lower(s.displayName) = lower(:displayName)")
  boolean existsActiveDisplayName(String displayName);
}

