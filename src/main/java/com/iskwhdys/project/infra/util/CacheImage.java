package com.iskwhdys.project.infra.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheImage {

  private Map<String, CacheObject> cache = new HashMap<>();
  private RestTemplate restTemplate = new RestTemplate();

  private UnaryOperator<byte[]> resizeFunction;
  private String directory;
  private String extension;
  private boolean isOriginCache;

  public CacheImage(String dir, String ext) {
    directory = dir;
    extension = ext;
  }

  public CacheImage(String dir, String ext, UnaryOperator<byte[]> resize, boolean orgCache) {
    directory = dir;
    extension = ext;
    resizeFunction = resize;
    isOriginCache = orgCache;
  }

  public CacheObject readOriginal(String key) {
    return read(key, false);
  }

  public CacheObject readMini(String key) {
    return read(key, true);
  }

  public CacheObject read(String key, boolean mini) {
    String file = getFileName(key, mini);
    if (cache.containsKey(file)) {
      return cache.get(file);
    }

    Path filePath = Paths.get(directory, file);
    if (Files.exists(filePath)) {
      try {
        put(file, Files.readAllBytes(filePath));
      } catch (IOException e) {
        throw new ResourceAccessException("File read error", e);
      }
      return cache.get(file);
    }

    return null;
  }

  public boolean download(String key, String url) {
    try {
      Files.createDirectories(Paths.get(directory));

      String file = getFileName(key, false);
      byte[] bytes = restTemplate.getForObject(url, byte[].class);
      Files.write(Paths.get(directory, file), bytes, StandardOpenOption.CREATE);
      if (isOriginCache) {
        put(file, bytes);
      }

      if (resizeFunction != null) {
        file = getFileName(key, true);
        bytes = resizeFunction.apply(bytes);
        Files.write(Paths.get(directory, file), bytes, StandardOpenOption.CREATE);
        put(file, bytes);
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private void put(String key, byte[] bytes) {
    if (cache.size() > 512) {
      cache.clear();
      log.info(directory + "のキャッシュを削除しました。");
    }
    cache.put(key, new CacheObject(bytes, new Date()));
  }

  private String getFileName(String key, boolean mini) {
    String file = key + extension;
    if (mini) {
      file = "mini_" + file;
    }
    return file;
  }
}
