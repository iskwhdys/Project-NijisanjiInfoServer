package com.iskwhdys.project.application;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iskwhdys.project.Common;
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoRepository;

@Service
@Transactional
public class VideoDeliveryService {

  private @Autowired VideoRepository vr;

  public List<VideoEntity> live() {
    return vr.findByEnabledTrueAndTypeInOrderByLiveStartDesc(VideoEntity.TYPE_LIVES);
  }

  public List<VideoEntity> upload() {

    var list =
        vr.findByEnabledTrueAndTypeInAndUploadDateBetweenOrderByUploadDateDesc(
            VideoEntity.TYPE_UPLOADS, getDaysDate(2), new Date());
    if (list.isEmpty()) {
      list =
          vr.findTop10ByEnabledTrueAndTypeInAndUploadDateBeforeOrderByUploadDateDesc(
              VideoEntity.TYPE_UPLOADS, new Date());
    }

    return list;
  }

  public List<VideoEntity> upload(String from) {
    return vr.findTop10ByEnabledTrueAndTypeInAndUploadDateBeforeOrderByUploadDateDesc(
        VideoEntity.TYPE_UPLOADS, Common.toDate(from));
  }

  public List<VideoEntity> archive() {

    return vr.findTop30ByEnabledTrueAndTypeEqualsAndLiveStartBeforeOrderByLiveStartDesc(
        VideoEntity.TYPE_LIVE_ARCHIVE, new Date());
  }

  public List<VideoEntity> archive(String from) {
    return vr.findTop30ByEnabledTrueAndTypeEqualsAndLiveStartBeforeOrderByLiveStartDesc(
        VideoEntity.TYPE_LIVE_ARCHIVE, Common.toDate(from));
  }

  public List<VideoEntity> premier() {

    return vr.findByEnabledTrueAndTypeEqualsAndLiveScheduleBetweenOrderByLiveSchedule(
        VideoEntity.TYPE_PREMIER_RESERVE, getDaysDate(1), getDaysDate(-2));
  }

  public List<VideoEntity> premier(String from) {
    return vr.findTop10ByEnabledTrueAndTypeEqualsAndLiveScheduleAfterOrderByLiveSchedule(
        VideoEntity.TYPE_PREMIER_RESERVE, Common.toDate(from));
  }

  public List<VideoEntity> schedule() {

    return vr.findByEnabledTrueAndTypeEqualsAndLiveScheduleBetweenOrderByLiveSchedule(
        VideoEntity.TYPE_LIVE_RESERVE, getDaysDate(0.5), getDaysDate(-2));
  }

  public List<VideoEntity> schedule(String from) {
    return vr.findTop30ByEnabledTrueAndTypeEqualsAndLiveScheduleAfterOrderByLiveSchedule(
        VideoEntity.TYPE_LIVE_RESERVE, Common.toDate(from));
  }

  private Date getDaysDate(double day) {
    return new Date(new Date().getTime() - (int) (1000 * 60 * 60 * 24 * day));
  }
}
