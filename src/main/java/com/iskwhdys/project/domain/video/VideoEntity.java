package com.iskwhdys.project.domain.video;

import java.util.Date;

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

	@Column(name = "thumbnail")
	private String thumbnail;

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

	public Boolean isUpload() { return getType().equals("Upload"); }

	public Boolean isPremierReserve() { return getType().equals("PremierReserve"); }

	public Boolean isPremierLive() { return getType().equals("PremierLive"); }

	public Boolean isPremierUpload() { return getType().equals("PremierUpload"); }

	public Boolean isLiveReserve() { return getType().equals("LiveReserve"); }

	public Boolean isLiveLive() { return getType().equals("LiveLive"); }

	public Boolean isLiveArchive() { return getType().equals("LiveArchive"); }

	public String toString() { return "Video:[" + getId() + "] [" + getTitle() + "]"; }

}
