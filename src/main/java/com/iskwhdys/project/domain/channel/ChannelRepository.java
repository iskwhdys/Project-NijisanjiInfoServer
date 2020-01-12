package com.iskwhdys.project.domain.channel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChannelRepository extends JpaRepository<ChannelEntity, String> {

    @Query(value = "select id, title, description, subscriber_count, small_thumbnail as thumbnail, null as small_thumbnail from public.channels where id = ?1" , nativeQuery = true)
    ChannelEntity findByIdThumbnailMini(String id);


}