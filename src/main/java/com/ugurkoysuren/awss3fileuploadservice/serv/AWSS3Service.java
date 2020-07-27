package com.ugurkoysuren.awss3fileuploadservice.serv;

import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.net.URL;

public interface AWSS3Service {

	void uploadFile(MultipartFile multipartFile);
	URL downloadFile(String key);
}
