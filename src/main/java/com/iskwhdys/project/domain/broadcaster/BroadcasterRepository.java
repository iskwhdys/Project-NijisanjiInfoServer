package com.iskwhdys.project.domain.broadcaster;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BroadcasterRepository extends JpaRepository<BroadcasterEntity, String> {


  List<BroadcasterEntity> findByYoutubeOrYoutube2(String channelId,String channelId2);

}
