package com.iskwhdys.project.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SitemapService {

  @Value("${nis.path.sitemap}")
  String sitemapPath;

  RestTemplate restTemplate = new RestTemplate();

  public void update() {
    try {
      String xml = new String(new ClassPathResource("sitemap.xml").getInputStream().readAllBytes());

      xml = xml.replace("{date}", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

      Files.write(Paths.get(sitemapPath), xml.getBytes(), StandardOpenOption.CREATE);

      ResponseEntity<String> response =
          restTemplate.getForEntity(
              "https://www.google.com/ping?sitemap=https://nijisanji-live.com/sitemap.xml",
              String.class);

      log.info("Sitemap Status:" + response.getStatusCode());

    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }
}
