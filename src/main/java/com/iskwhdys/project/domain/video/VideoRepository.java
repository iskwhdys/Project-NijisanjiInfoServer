package com.iskwhdys.project.domain.video;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VideoRepository extends JpaRepository<VideoEntity, String>, JpaSpecificationExecutor<VideoEntity> {

	List<VideoEntity> findByTypeInAndEnabledTrueOrderByLiveStartDesc(List<String> of);

}