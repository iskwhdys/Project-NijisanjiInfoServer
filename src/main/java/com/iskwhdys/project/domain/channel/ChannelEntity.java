package com.iskwhdys.project.domain.channel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "channels")
@NoArgsConstructor
@AllArgsConstructor
public class ChannelEntity {

  @Id private String id;
  private String title;
  private String description;
  private Integer subscriberCount;
  private String thumbnailUrl;
  private Boolean enabled;

  public String toString() {
    return "Channel:[" + getId() + "] [" + getTitle() + "]";
  }
}
