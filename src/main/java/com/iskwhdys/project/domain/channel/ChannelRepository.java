package com.iskwhdys.project.domain.channel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<ChannelEntity, String> {

  List<ChannelEntity> findByEnabledTrue();

}
