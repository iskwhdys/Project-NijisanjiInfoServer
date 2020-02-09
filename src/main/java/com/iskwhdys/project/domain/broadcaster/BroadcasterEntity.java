package com.iskwhdys.project.domain.broadcaster;

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
@Table(name = "broadcasters")
@NoArgsConstructor
@AllArgsConstructor
public class BroadcasterEntity {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "name")
	private String name;

	@Column(name = "kana")
	private String kana;

	@Column(name = "groups")
	private String groups;

	@Column(name = "twitter")
	private String twitter;

	@Column(name = "youtube")
	private String youtube;

	@Column(name = "youtube2")
	private String youtube2;

	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "page")
	private String page;

	@Column(name = "icon_url")
	private String iconUrl;

	@Column(name = "icon")
	private String icon;

	@Column(name = "icon_mini")
	private String iconMini;

}
