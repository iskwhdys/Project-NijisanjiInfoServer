package com.iskwhdys.project.domain.broadcaster;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BroadcasterRepository extends JpaRepository<BroadcasterEntity, String> {


  BroadcasterEntity findByYoutubeOrYoutube2(String channelId,String channelId2);

}
