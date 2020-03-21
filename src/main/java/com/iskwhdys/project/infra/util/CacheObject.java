package com.iskwhdys.project.infra.util;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheObject {
  private byte[] bytes;
  private Date lastUpdate;


}
