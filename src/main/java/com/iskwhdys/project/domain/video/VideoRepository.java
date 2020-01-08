package com.iskwhdys.project.domain.video;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoRepository extends JpaRepository<VideoEntity, String> {

    @Query("select o from VideoEntity o where o.channelId = :channelId AND etag is NULL")
    List<VideoEntity> findNewData(@Param("channelId")String channelId);

    @Query("select o from VideoEntity o where o.channelId = :channelId")
    List<VideoEntity> findChannelId(@Param("channelId")String channelId);

    @Query(value = "select * from public.today_upload_videos o where channel_id = :channelId" , nativeQuery = true)
    List<VideoEntity> findToday(@Param("channelId")String channelId);

    @Query(value = "select * from public.today_upload_videos" , nativeQuery = true)
    List<VideoEntity> findTodayUpload();




}