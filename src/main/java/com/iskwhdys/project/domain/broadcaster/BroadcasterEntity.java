package com.iskwhdys.project.domain.broadcaster;


import java.util.Date;
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

  @Id private String id;
  private String name;
  private String kana;
  private String group;
  private String twitter;
  private String twitter2;
  private String youtube;
  private String youtube2;
  private Date startDate;
  private Date endDate;
  private String official;
  private String icon;
  private String wiki;

  public String toString() {
    return "Broadcaster:[" + getId() + "] [" + getName() + "]";
  }
}

