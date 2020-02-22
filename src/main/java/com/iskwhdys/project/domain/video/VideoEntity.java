package com.iskwhdys.project.domain.video;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "videos")
@NoArgsConstructor
@AllArgsConstructor
public class VideoEntity {

  @Id private String id;
  private String channelId;
  private String title;
  private String description;
  private Integer views;
  private Integer likes;
  private Integer dislikes;
  private Integer favorites;
  private Integer duration;
  private Integer comments;
  private String etag;
  private Date uploadDate;
  private Date liveStart;
  private Date liveEnd;
  private Date liveSchedule;
  private Integer liveViews;
  private Boolean enabled;
  private String type;
  private String uploadStatus;
  private String thumbnailUrl;
  private Date createDate;
  private Date updateDate;

  public static final String TYPE_UPLOAD = "Upload";
  public static final String TYPE_PREMIER_RESERVE = "PremierReserve";
  public static final String TYPE_PREMIER_LIVE = "PremierLive";
  public static final String TYPE_PREMIER_UPLOAD = "PremierUpload";
  public static final String TYPE_LIVE_RESERVE = "LiveReserve";
  public static final String TYPE_LIVE_LIVE = "LiveLive";
  public static final String TYPE_LIVE_ARCHIVE = "LiveArchive";
  public static final String TYPE_UNKNOWN = "Unknown";

  public static final List<String> TYPE_RESERVES = List.of(TYPE_PREMIER_RESERVE, TYPE_LIVE_RESERVE);
  public static final List<String> TYPE_UPLOADS = List.of(TYPE_PREMIER_UPLOAD, TYPE_UPLOAD);
  public static final List<String> TYPE_LIVES = List.of(TYPE_PREMIER_LIVE, TYPE_LIVE_LIVE);

  public boolean isUpload() {
    return TYPE_UPLOAD.equals(getType());
  }

  public boolean isPremierReserve() {
    return TYPE_PREMIER_RESERVE.equals(getType());
  }

  public boolean isPremierLive() {
    return TYPE_PREMIER_LIVE.equals(getType());
  }

  public boolean isPremierUpload() {
    return TYPE_PREMIER_UPLOAD.equals(getType());
  }

  public boolean isLiveReserve() {
    return TYPE_LIVE_RESERVE.equals(getType());
  }

  public boolean isLiveLive() {
    return TYPE_LIVE_LIVE.equals(getType());
  }

  public boolean isLiveArchive() {
    return TYPE_LIVE_ARCHIVE.equals(getType());
  }

  public boolean isUnknown() {
    return TYPE_UNKNOWN.equals(getType());
  }

  public String toString() {
    return "Video:[" + getId() + "] [" + getTitle() + "]";
  }
}
