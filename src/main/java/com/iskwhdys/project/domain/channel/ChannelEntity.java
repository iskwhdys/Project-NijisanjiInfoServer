package com.iskwhdys.project.domain.channel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name="channels")
@NoArgsConstructor
@AllArgsConstructor
public class ChannelEntity {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "title")
	private String title;

	@Column(name = "description")
	private String description;

	@Column(name = "subscriber_count")
	private Integer subscriberCount;

	@Column(name = "thumbnail")
	private String thumbnail;

	public String toString() { return "Channel:[" + getId() + "] [" + getTitle() + "]"; }
}
