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

}