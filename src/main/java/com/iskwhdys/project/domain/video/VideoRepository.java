package com.iskwhdys.project.domain.video;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface VideoRepository
    extends JpaRepository<VideoEntity, String>, JpaSpecificationExecutor<VideoEntity> {

  List<VideoEntity> findByEnabledTrueAndIdNotIn(List<String> id);

  List<VideoEntity> findByTypeInAndEnabledTrueAndIdNotInOrderByLiveStartDesc(
      List<String> type, List<String> id);

  List<VideoEntity> findByTypeInAndEnabledTrueOrderByLiveStartDesc(List<String> type);

  @Query(
      value =
          "select * from public.videos "
              + "where"
              + "	id not in ?1 and"
              + "	((type='Upload' and (CURRENT_TIMESTAMP - upload_date) < '24:00:00') or "
              + "	 (type='PremierUpload' and (CURRENT_TIMESTAMP - live_start) < '24:00:00') or "
              + "	 (type='LiveArchive' and (CURRENT_TIMESTAMP - live_start) < '24:00:00')) and"
              + "	enabled = true",
      nativeQuery = true)
  List<VideoEntity> findByIdNotInAndTodayVideos(List<String> of);

  @Query(
      value =
          "select * from public.videos "
              + "where"
              + "   id not in ?1 and"
              + "   (type='LiveReserve' or type='PremierReserve') and"
              + "   enabled = true",
      nativeQuery = true)
  List<VideoEntity> findByIdNotInAndTypeAllReserve(List<String> of);



  List<VideoEntity> findByEnabledTrueAndTypeInOrderByLiveStartDescIdAsc(List<String> of);

  List<VideoEntity> findByEnabledTrueAndTypeInAndUploadDateBetweenOrderByUploadDateDescIdAsc(
      List<String> of, Date start, Date end);

  List<VideoEntity> findTop10ByEnabledTrueAndTypeInAndUploadDateBeforeOrderByUploadDateDescIdAsc(
      List<String> of, Date from);

  List<VideoEntity> findByEnabledTrueAndTypeEqualsAndLiveStartBetweenOrderByLiveStartDescIdAsc(
      String of, Date start, Date end);

  List<VideoEntity> findTop30ByEnabledTrueAndTypeEqualsAndLiveStartBeforeOrderByLiveStartDescIdAsc(
      String of, Date from);
  List<VideoEntity> findTop60ByEnabledTrueAndTypeEqualsAndLiveStartBeforeOrderByLiveStartDescIdAsc(
      String of, Date from);

  List<VideoEntity> findByEnabledTrueAndTypeEqualsAndLiveScheduleBetweenOrderByLiveScheduleAscIdAsc(
      String of, Date start, Date end);

  List<VideoEntity> findByEnabledTrueAndTypeInAndLiveScheduleBetweenOrderByLiveScheduleAscIdAsc(
      List<String> of, Date start, Date end);

  List<VideoEntity> findByEnabledTrueAndTypeIn(List<String> of);

  List<VideoEntity> findTop10ByEnabledTrueAndTypeEqualsAndLiveScheduleAfterOrderByLiveScheduleAscIdAsc(
      String of, Date from);

  List<VideoEntity> findTop30ByEnabledTrueAndTypeEqualsAndLiveScheduleAfterOrderByLiveScheduleAscIdAsc(
      String of, Date from);

  List<VideoEntity>
      findTop10ByEnabledTrueAndChannelIdEqualsAndUploadDateBeforeOrderByUploadDateDescIdAsc(
          String of, Date from);
  List<VideoEntity>
  findTop30ByEnabledTrueAndChannelIdEqualsAndUploadDateBeforeOrderByUploadDateDescIdAsc(
      String of, Date from);
}
