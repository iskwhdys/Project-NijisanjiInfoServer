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
@Table(name = "enabled_videos_without_base64_v")
@NoArgsConstructor
@AllArgsConstructor
public class SizeSaveVideoEntity {

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

	@Column(name = "thumbnail_mini")
	private String thumbnailMini;

	@Column(name = "thumbnail_url")
	private String thumbnailUrl;

	@Column(name = "create_date")
	private Date createDate;

	@Column(name = "update_date")
	private Date updateDate;

}