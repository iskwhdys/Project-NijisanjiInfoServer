package com.iskwhdys.project.domain.channel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChannelRepository extends JpaRepository<ChannelEntity, String> {

  List<ChannelEntity> findByEnabledTrue();

  @Query(
      value =
          "select * from public.channels "
              + "where"
              + "     rss_expires < CURRENT_TIMESTAMP"
              + " and enabled = true",
      nativeQuery = true)
  List<ChannelEntity> findByEnabledTrueAndRssExpires();
}
