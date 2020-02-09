package com.iskwhdys.project.domain.video;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
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

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "channel_id")
	private String channelId;

	@Column(name = "title")
	private String title;

	@Column(name = "description")
	private String description;

	@Column(name = "views")
	private Integer views;

	@Column(name = "likes")
	private Integer likes;

	@Column(name = "dislikes")
	private Integer dislikes;

	@Column(name = "favorites")
	private Integer favorites;

	@Column(name = "duration")
	private Integer duration;

	@Column(name = "comments")
	private Integer comments;

	@Column(name = "etag")
	private String etag;

	@Column(name = "upload_date")
	private Date uploadDate;

	@Column(name = "live_start")
	private Date liveStart;

	@Column(name = "live_end")
	private Date liveEnd;

	@Column(name = "live_schedule")
	private Date liveSchedule;

	@Column(name = "live_views")
	private Integer liveViews;

	@Column(name = "enabled")
	private Boolean enabled;

	@Column(name = "type")
	private String type;

	@Column(name = "upload_status")
	private String uploadStatus;

	@Column(name = "thumbnail_url")
	private String thumbnailUrl;

	@Column(name = "create_date")
	private Date createDate;

	@Column(name = "update_date")
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

	public boolean isUpload() { return TYPE_UPLOAD.equals(getType()); }

	public boolean isPremierReserve() { return TYPE_PREMIER_RESERVE.equals(getType()); }

	public boolean isPremierLive() { return TYPE_PREMIER_LIVE.equals(getType()); }

	public boolean isPremierUpload() { return TYPE_PREMIER_UPLOAD.equals(getType()); }

	public boolean isLiveReserve() { return TYPE_LIVE_RESERVE.equals(getType()); }

	public boolean isLiveLive() { return TYPE_LIVE_LIVE.equals(getType()); }

	public boolean isLiveArchive() { return TYPE_LIVE_ARCHIVE.equals(getType()); }

	public boolean isUnknown() { return TYPE_UNKNOWN.equals(getType()); }

	public String toString() { return "Video:[" + getId() + "] [" + getTitle() + "]"; }

}
