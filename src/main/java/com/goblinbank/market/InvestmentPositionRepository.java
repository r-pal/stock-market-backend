package com.goblinbank.market;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InvestmentPositionRepository extends JpaRepository<InvestmentPosition, Long> {
  @Query("select p from InvestmentPosition p where p.buyerHouse.id = :houseId and p.status = :status")
  List<InvestmentPosition> findByBuyerHouseIdAndStatus(Long houseId, String status);

  @Query(
      "select distinct p from InvestmentPosition p join fetch p.targetHouse join fetch p.buyerHouse where p.buyerHouse.id = :houseId and p.status = :status")
  List<InvestmentPosition> findOpenForBuyerFetched(Long houseId, String status);

  @Query("select p from InvestmentPosition p where p.buyerHouse.id = :houseId")
  List<InvestmentPosition> findByBuyerHouseId(Long houseId);

  @Query(
      "select p from InvestmentPosition p where (p.buyerHouse.id = :houseId or p.targetHouse.id = :houseId) and p.status = :status")
  List<InvestmentPosition> findOpenInvolvingHouse(Long houseId, String status);

  @Query(
      "select count(p) from InvestmentPosition p where (p.buyerHouse.id = :houseId or p.targetHouse.id = :houseId) and p.status = :status")
  long countInvolvingHouseAndStatus(Long houseId, String status);

  @Query(
      "select p from InvestmentPosition p join fetch p.buyerHouse join fetch p.targetHouse where p.id = :id")
  Optional<InvestmentPosition> findByIdFetched(Long id);

  @Query(
      "select p from InvestmentPosition p join fetch p.buyerHouse join fetch p.targetHouse where p.buyerHouse.id = :houseId and p.status = :status")
  List<InvestmentPosition> findByBuyerHouseIdAndStatusFetched(Long houseId, String status);

  @Query(
      "select p from InvestmentPosition p join fetch p.buyerHouse join fetch p.targetHouse where p.buyerHouse.id = :houseId order by p.id desc")
  List<InvestmentPosition> findByBuyerHouseIdFetched(Long houseId);

  @Query(
      "select p from InvestmentPosition p join fetch p.buyerHouse join fetch p.targetHouse where (:houseId is null or p.buyerHouse.id = :houseId or p.targetHouse.id = :houseId) and (:status is null or p.status = :status) order by p.id desc")
  List<InvestmentPosition> searchForBanker(Long houseId, String status);
}

