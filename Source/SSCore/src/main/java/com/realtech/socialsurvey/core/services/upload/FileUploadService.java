package com.realtech.socialsurvey.core.services.upload;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.AmazonS3;
import com.realtech.socialsurvey.core.exception.InvalidInputException;

/**
 * Holds methods to upload file to the application server
 */
public interface FileUploadService {

	/**
	 * uploads image to server path specified
	 * 
	 * @throws InvalidInputException
	 */
	public String fileUploadHandler(MultipartFile fileLocal, String logoName) throws InvalidInputException;
	
	/**
	 * Method that returns a list of all the keys in a bucket.
	 * @return
	 */
	public List<String> listAllOjectsInBucket(AmazonS3 s3Client);
	
	/**
	 * Method to delete the object with a particular key from the S3 bucket.
	 * @param key
	 * @throws InvalidInputException
	 */
	public void deleteObjectFromBucket(String key, AmazonS3 s3Client) throws InvalidInputException;
	
	/**
	 * Method to create AmazonS3 client
	 */
	public AmazonS3 createAmazonClient(String endpoint, String bucket);

}
