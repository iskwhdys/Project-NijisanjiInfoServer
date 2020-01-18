package com.iskwhdys.project.domain.video;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VideoCardRepository extends JpaRepository<VideoCardEntity, String>, JpaSpecificationExecutor<VideoCardEntity> {

	List<VideoCardEntity> findByTypeInOrderByLiveStartDesc(List<String> of);

	List<VideoCardEntity> findByTypeInAndUploadDateBetweenOrderByUploadDateDesc(List<String> of, Date start, Date end);

	List<VideoCardEntity> findTop10ByTypeInAndUploadDateBeforeOrderByUploadDateDesc(List<String> of, Date from);


	List<VideoCardEntity> findByTypeEqualsAndLiveStartBetweenOrderByLiveStartDesc(String of, Date start, Date end);

	List<VideoCardEntity> findTop30ByTypeEqualsAndLiveStartBeforeOrderByLiveStartDesc(String of, Date from);


	List<VideoCardEntity> findByTypeEqualsAndLiveScheduleBetweenOrderByLiveSchedule(String  of, Date start, Date end);

	List<VideoCardEntity> findTop10ByTypeEqualsAndLiveScheduleAfterOrderByLiveSchedule(String of, Date from);
	List<VideoCardEntity> findTop30ByTypeEqualsAndLiveScheduleAfterOrderByLiveSchedule(String of, Date from);


	List<VideoCardEntity> findTop10ByChannelIdEqualsAndUploadDateBeforeOrderByUploadDateDesc(String of, Date from);
}