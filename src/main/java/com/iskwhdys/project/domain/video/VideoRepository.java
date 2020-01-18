package com.iskwhdys.project.domain.video;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface VideoRepository extends JpaRepository<VideoEntity, String>, JpaSpecificationExecutor<VideoEntity> {

    @Query(value = "select thumbnail_mini from public.videos where id = ?1" , nativeQuery = true)
    String findByIdThumbnailMini(String id);

	List<VideoEntity> findByTypeInAndEnabledTrueOrderByLiveStartDesc(List<String> of);

}