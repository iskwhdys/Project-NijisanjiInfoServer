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
@Table(name = "video_cards_v")
@NoArgsConstructor
@AllArgsConstructor
public class VideoCardEntity {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "title")
	private String title;

	@Column(name = "upload_date")
	private Date uploadDate;

	@Column(name = "views")
	private Integer views;

	@Column(name = "likes")
	private Integer likes;

	@Column(name = "dislikes")
	private Integer dislikes;

	@Column(name = "duration")
	private Integer duration;

	@Column(name = "live_start")
	private Date liveStart;

	@Column(name = "live_views")
	private Integer liveViews;

	@Column(name = "live_schedule")
	private Date liveSchedule;

	@Column(name = "type")
	private String type;

	@Column(name = "channel_id")
	private String channelId;

	@Column(name = "channel_title")
	private String channelTitle;

}
