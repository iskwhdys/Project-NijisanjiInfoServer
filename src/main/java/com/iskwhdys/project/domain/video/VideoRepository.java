package com.iskwhdys.project.domain.video;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface VideoRepository extends JpaRepository<VideoEntity, String>, JpaSpecificationExecutor<VideoEntity> {

	List<VideoEntity> findByTypeInAndEnabledTrueAndIdNotInOrderByLiveStartDesc(List<String> type, List<String> id);
	List<VideoEntity> findByTypeInAndEnabledTrueOrderByLiveStartDesc(List<String> type);


    @Query(value =
    		"select * from public.videos " +
    		"where" +
    		"	id not in ?1 and" +
    		"	((type='Upload' and (CURRENT_TIMESTAMP - upload_date) < '24:00:00') or " +
    		"	 (type='PremierUpload' and (CURRENT_TIMESTAMP - live_start) < '24:00:00') or " +
    		"	 (type='LiveArchive' and (CURRENT_TIMESTAMP - live_start) < '24:00:00')) and" +
    		"	enabled = true" , nativeQuery = true)
	List<VideoEntity> findByIdNotInAndTodayUploadVideoAndArchives(List<String> of);

}