package com.goblinbank.ticker;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseTickerBaselineRepository extends JpaRepository<HouseTickerBaseline, Long> {
  Optional<HouseTickerBaseline> findByHouseId(Long houseId);
}
