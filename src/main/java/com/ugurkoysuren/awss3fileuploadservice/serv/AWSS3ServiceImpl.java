package com.ugurkoysuren.awss3fileuploadservice.serv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Date;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
public class AWSS3ServiceImpl implements AWSS3Service {

	private static final Logger LOGGER = LoggerFactory.getLogger(AWSS3ServiceImpl.class);

	private AmazonS3 amazonS3;
	@Value("${aws.s3.bucket}")
	private String bucketName;

	@Autowired
	public AWSS3ServiceImpl(AmazonS3 amazonS3) {
		this.amazonS3 = amazonS3;
	}

	@Override
	// @Async annotation ensures that the method is executed in a different background thread 
	// but not consume the main thread.
	@Async
	public void uploadFile(final MultipartFile multipartFile) {
		LOGGER.info("File upload in progress.");
		try {
			final File file = convertMultiPartFileToFile(multipartFile);
			uploadFileToS3Bucket(bucketName, file);
			LOGGER.info("File upload is completed.");
			file.delete();	// To remove the file locally created in the project folder.
		} catch (final AmazonServiceException ex) {
			LOGGER.info("File upload is failed.");
			LOGGER.error("Error= {} while uploading file.", ex.getMessage());
		}
	}


	@Override
	// @Async annotation ensures that the method is executed in a different background thread
	// but not consume the main thread.
	@Async
	public URL downloadFile(final String key) {
		LOGGER.info("File download in progress.");
		URL url = null;
		try {
			url = new URL("https://www.publicdomainpictures.net/pictures/280000/velka/not-found-image-15383864787lu.jpg");
		} catch (MalformedURLException e) {
			LOGGER.info("Default URL is malformed");
		}
		try {
			url = downloadFileFromS3Bucket(bucketName, key);
			LOGGER.info("File upload is completed.");
		} catch (final AmazonServiceException ex) {
			LOGGER.info("File upload is failed.");
			LOGGER.error("Error= {} while uploading file.", ex.getMessage());
		}
		return url;
	}

	private File convertMultiPartFileToFile(final MultipartFile multipartFile) {
		final File file = new File(multipartFile.getOriginalFilename());
		try (final FileOutputStream outputStream = new FileOutputStream(file)) {
			outputStream.write(multipartFile.getBytes());
		} catch (final IOException ex) {
			LOGGER.error("Error converting the multi-part file to file= ", ex.getMessage());
		}
		return file;
	}

	private void uploadFileToS3Bucket(final String bucketName, final File file) {
		final String uniqueFileName = LocalDateTime.now() + "_" + file.getName();
		LOGGER.info("Uploading file with name= " + uniqueFileName);
		final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, uniqueFileName, file);
		amazonS3.putObject(putObjectRequest);
	}
	private URL downloadFileFromS3Bucket(final String bucketName, final String key) {
		Date expiration = new java.util.Date();
		long expTimeMillis = expiration.getTime();
		expTimeMillis += 1000 * 60 * 60;
		expiration.setTime(expTimeMillis);

		GeneratePresignedUrlRequest generatePresignedUrlRequest =
				new GeneratePresignedUrlRequest(bucketName, key)
						.withMethod(HttpMethod.GET)
						.withExpiration(expiration);
		URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

		LOGGER.info("Downloading file with url= " + url);
		return url;
	}
}
