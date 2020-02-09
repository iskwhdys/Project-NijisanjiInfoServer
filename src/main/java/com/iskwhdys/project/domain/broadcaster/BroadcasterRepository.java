package com.iskwhdys.project.domain.broadcaster;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BroadcasterRepository extends JpaRepository<BroadcasterEntity, String> {

    @Query(value = "select id,name,kana,groups,twitter,youtube,youtube2,start_date,end_date,page,icon_url,null as icon,null as icon_mini from public.broadcasters" , nativeQuery = true)
    List<BroadcasterEntity> findAllWithoutIcon();

    @Query(value = "select icon from public.broadcasters where id = ?1" , nativeQuery = true)
    String findByIdIcon(String id);

}