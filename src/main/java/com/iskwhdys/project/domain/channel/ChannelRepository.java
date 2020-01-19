package com.iskwhdys.project.domain.channel;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChannelRepository extends JpaRepository<ChannelEntity, String> {

    @Query(value = "select small_thumbnail from public.channels where id = ?1" , nativeQuery = true)
    String findByIdThumbnailMini(String id);

    @Query(value = "select thumbnail from public.channels where id = ?1" , nativeQuery = true)
    String findByIdThumbnail(String id);

    @Query(value = "select id, title, description, subscriber_count, null as thumbnail, null as small_thumbnail from public.channels" , nativeQuery = true)
    List<ChannelEntity> findAllWithoutThumbnail();

    @Query(value = "select id, title, description, subscriber_count, null as thumbnail, null as small_thumbnail from public.channels where id = ?1" , nativeQuery = true)
    ChannelEntity findByIdWithoutThumbnail(String id);


}