package com.ugurkoysuren.awss3fileuploadservice.ctrl;

import com.ugurkoysuren.awss3fileuploadservice.serv.AWSS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;

@RestController
@RequestMapping(value= "/s3")
public class AWSS3Ctrl {

	private AWSS3Service service;

	@Autowired
	public AWSS3Ctrl(AWSS3Service service) {
		this.service = service;
	}

	@PostMapping(value= "/upload")
	public ResponseEntity<String> uploadFile(@RequestPart(value= "file") final MultipartFile multipartFile) {
		service.uploadFile(multipartFile);
		final String response = "[" + multipartFile.getOriginalFilename() + "] uploaded successfully.";
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@GetMapping(value= "/download")
	public ResponseEntity<URL> downloadFile(@RequestPart(value= "key") final String key) {
		URL url = service.downloadFile(key);
		return new ResponseEntity<>(url, HttpStatus.OK);
	}
}
