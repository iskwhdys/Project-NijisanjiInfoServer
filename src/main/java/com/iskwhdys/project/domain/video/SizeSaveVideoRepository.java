package com.iskwhdys.project.domain.video;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SizeSaveVideoRepository extends JpaRepository<SizeSaveVideoEntity, String>, JpaSpecificationExecutor<SizeSaveVideoEntity> {

	List<SizeSaveVideoEntity> findByTypeInOrderByLiveStartDesc(List<String> of);

	List<SizeSaveVideoEntity> findByTypeInAndUploadDateBetweenOrderByUploadDateDesc(List<String> of, Date start, Date end);

	List<SizeSaveVideoEntity> findTop10ByTypeInAndUploadDateBeforeOrderByUploadDateDesc(List<String> of, Date from);

	List<SizeSaveVideoEntity> findByTypeEqualsAndLiveStartBetweenOrderByLiveStartDesc(String of, Date start, Date end);

	List<SizeSaveVideoEntity> findTop30ByTypeEqualsAndLiveStartBeforeOrderByLiveStartDesc(String of, Date from);


}