package com.iskwhdys.project.interfaces;

import java.util.Base64;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@CrossOrigin
@Controller
public class ApiFunctionController {

	RestTemplate restTemplate = new RestTemplate();

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getIndex(Model model) {
		return "index";
	}

	@ResponseBody
	@RequestMapping(value = "/api/function/base64file", method = RequestMethod.GET)
	public String getBase64File(@RequestParam Map<String, String> params) {
		System.out.println(params);

		byte[] buf = restTemplate.getForObject(params.get("url"), byte[].class);

		String base64 = Base64.getEncoder().encodeToString(buf);

		return base64;
	}

	@RequestMapping(value = "/api/function/xml", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getRssXml(@RequestParam Map<String, String> params) {
		System.out.println(params);

		byte[] buf = restTemplate.getForObject(params.get("url"), byte[].class);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_RSS_XML);

		var res = new ResponseEntity<>(buf, headers, HttpStatus.OK);
		System.out.println(res);
		return res;

	}

}
