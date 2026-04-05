package com.goblinbank.wealth;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HouseWealthSnapshotRepository extends JpaRepository<HouseWealthSnapshot, Long> {

  @Query(
      "select s from HouseWealthSnapshot s where s.house.id = :houseId and s.minutes <= :maxMinutesElapsed order by s.minutes desc")
  List<HouseWealthSnapshot> findLatestForHouseUpToMinute(Long houseId, int maxMinutesElapsed);

  void deleteByCapturedAtBefore(Instant cutoff);
  @Query(
      "select s from HouseWealthSnapshot s where s.house.id = :houseId and s.capturedAt <= :capturedAt order by s.capturedAt desc")
  List<HouseWealthSnapshot> findLatestAtOrBefore(Long houseId, Instant capturedAt);

  @Query(
      "select s from HouseWealthSnapshot s where s.capturedAt between :from and :to order by s.house.id asc, s.capturedAt asc")
  List<HouseWealthSnapshot> findAllBetween(Instant from, Instant to);

  @Query(
      "select s from HouseWealthSnapshot s where s.house.id in :houseIds and s.capturedAt between :from and :to order by s.house.id asc, s.capturedAt asc")
  List<HouseWealthSnapshot> findAllBetweenForHouses(List<Long> houseIds, Instant from, Instant to);

  boolean existsByHouse_IdAndCapturedAt(Long houseId, Instant capturedAt);

  @Query(
      "select s from HouseWealthSnapshot s where s.house.id in :houseIds and s.minutes between :fromMin and :toMin order by s.house.id asc, s.minutes asc")
  List<HouseWealthSnapshot> findForHistoryChart(List<Long> houseIds, int fromMin, int toMin);
}

