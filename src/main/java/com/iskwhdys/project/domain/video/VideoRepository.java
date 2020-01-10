package com.iskwhdys.project.domain.video;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoRepository extends JpaRepository<VideoEntity, String> {

    @Query(value = "select * from public.videos WHERE etag is NULL" , nativeQuery = true)
    List<VideoEntity> findNewData();

    @Query("select o from VideoEntity o where o.channelId = :channelId")
    List<VideoEntity> findChannelId(@Param("channelId")String channelId);

    @Query(value = "select * from public.today_upload_videos o where channel_id = :channelId" , nativeQuery = true)
    List<VideoEntity> findToday(@Param("channelId")String channelId);

    @Query(value = "select * from public.videos "
    		+ "where type = 'Upload'"
    		+ " and  enabled = true"
    		+ " and  (CURRENT_TIMESTAMP - upload_date) < '24:00:00'"
    		+ " order by upload_date desc" , nativeQuery = true)
    List<VideoEntity> findTodayUpload();

    @Query(value = "select * from public.videos "
    		+ "where type = 'Live'"
    		+ " and   enabled = true"
    		+ " and  (CURRENT_TIMESTAMP - upload_date) < '24:00:00'"
    		+ " order by live_start desc" , nativeQuery = true)
    List<VideoEntity> findLive();


    @Query(value = "select * from public.videos" , nativeQuery = true)
    List<VideoEntity> findUploadVideo();







}