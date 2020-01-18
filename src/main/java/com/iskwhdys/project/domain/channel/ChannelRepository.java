package com.iskwhdys.project.domain.channel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChannelRepository extends JpaRepository<ChannelEntity, String> {

    @Query(value = "select small_thumbnail from public.channels where id = ?1" , nativeQuery = true)
    String findByIdThumbnailMini(String id);


}