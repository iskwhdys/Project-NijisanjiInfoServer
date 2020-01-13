package com.iskwhdys.project.domain.video;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VideoRepository extends JpaRepository<VideoEntity, String> {

    @Query(value = "select * from public.videos "
    		+ "where  (type = 'PremierLive' or type = 'LiveLive')"
    		+ " and   enabled = true"
    		+ " order by live_start desc" , nativeQuery = true)
    List<VideoEntity> findLive();

    @Query(value = "select * from public.videos "
    		+ "where (type = 'PremierUpload' or type = 'Upload')"
    		+ " and  enabled = true"
    		+ " and  (CURRENT_TIMESTAMP - upload_date) < '24:00:00'"
    		+ " order by upload_date desc" , nativeQuery = true)
    List<VideoEntity> find24HourUpload();


    @Query(value = "select * from public.videos "
    		+ "where (type = 'PremierUpload' or type = 'Upload')"
    		+ " and  enabled = true"
    		+ " and  upload_date < ?1"
    		+ " order by upload_date desc limit ?2" , nativeQuery = true)
    List<VideoEntity> findUpload(Date from, int count);


    @Query(value = "select * from public.videos "
    		+ "where (type = 'LiveArchive')"
    		+ " and  enabled = true"
    		+ " and  live_start < ?1"
    		+ " order by live_start desc limit ?2" , nativeQuery = true)
    List<VideoEntity> findArchive(Date from, int count);


    @Query(value = "select * from public.videos "
    		+ "where type = 'LiveArchive'"
    		+ " and  (CURRENT_TIMESTAMP - live_start) < '24:00:00'"
    		+ " and   enabled = true"
    		+ " order by live_start desc" , nativeQuery = true)
    List<VideoEntity> find24HourArchive();


}