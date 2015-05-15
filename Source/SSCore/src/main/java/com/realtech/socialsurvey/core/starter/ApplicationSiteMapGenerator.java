package com.realtech.socialsurvey.core.starter;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.realtech.socialsurvey.core.exception.NonFatalException;
import com.realtech.socialsurvey.core.services.upload.FileUploadService;
import com.realtech.socialsurvey.core.utils.sitemap.SiteMapGenerator;

/**
 * Started app to generate sitemap for the application
 *
 */
@Component("appsitemapgenerator")
public class ApplicationSiteMapGenerator {
	
	public static final Logger LOG = LoggerFactory.getLogger(ApplicationSiteMapGenerator.class);
	
	@Autowired
	private SiteMapGenerator companySiteMapGenerator;
	@Autowired
	private SiteMapGenerator regionSiteMapGenerator;
	@Autowired
	private SiteMapGenerator branchSiteMapGenerator;
	@Autowired
	private SiteMapGenerator agentSiteMapGenerator;
	@Autowired
	private FileUploadService uploadService;
	
	@Value("${AMAZON_ENV_PREFIX}")
	private String envPrefix;
	
	@Value("${COMPANY_SITEMAP_PATH}")
	private String companySiteMapPath;
	@Value("${REGION_SITEMAP_PATH}")
	private String regionSiteMapPath;
	@Value("${BRANCH_SITEMAP_PATH}")
	private String branchSiteMapPath;
	@Value("${INDIVIDUAL_SITEMAP_PATH}")
	private String individualSiteMapPath;
	
	public void execute(){
		LOG.info("Starting up the ApplicationSiteMapGenerator.");
		companySiteMapGenerator.setInterval(SiteMapGenerator.DAILY_CONTENT);
		companySiteMapGenerator.setOrganizationUnit(SiteMapGenerator.ORG_COMPANY);
		Thread companySiteMapGeneratorThread = new Thread(companySiteMapGenerator);
		companySiteMapGeneratorThread.start();
		regionSiteMapGenerator.setInterval(SiteMapGenerator.DAILY_CONTENT);
		regionSiteMapGenerator.setOrganizationUnit(SiteMapGenerator.ORG_REGION);
		Thread regionSiteMapGeneratorThread = new Thread(regionSiteMapGenerator);
		regionSiteMapGeneratorThread.start();
		branchSiteMapGenerator.setInterval(SiteMapGenerator.DAILY_CONTENT);
		branchSiteMapGenerator.setOrganizationUnit(SiteMapGenerator.ORG_BRANCH);
		Thread branchSiteMapGeneratorThread = new Thread(branchSiteMapGenerator);
		branchSiteMapGeneratorThread.start();
		agentSiteMapGenerator.setInterval(SiteMapGenerator.DAILY_CONTENT);
		agentSiteMapGenerator.setOrganizationUnit(SiteMapGenerator.ORG_INDIVIDUAL);
		Thread agentSiteMapGeneratorThread = new Thread(agentSiteMapGenerator);
		agentSiteMapGeneratorThread.start();
		try {
			companySiteMapGeneratorThread.join();
			regionSiteMapGeneratorThread.join();
			branchSiteMapGeneratorThread.join();
			agentSiteMapGeneratorThread.join();
		}
		catch (InterruptedException e) {
			LOG.error("Exception while joining to sitemap threads. ", e);
		}
		LOG.info("Done creating sitemaps. Now dumping the sitemaps");
		// upload company sitemap
		try {
			uploadFile(companySiteMapPath, uploadService, envPrefix);
			uploadFile(regionSiteMapPath, uploadService, envPrefix);
			uploadFile(branchSiteMapPath, uploadService, envPrefix);
			uploadFile(individualSiteMapPath, uploadService, envPrefix);
		}
		catch (NonFatalException e) {
			LOG.error("Could not upload file to amazon", e);
		}
		
	}
	
	public void uploadFile(String filePath, FileUploadService uploadService, String envPrefix) throws NonFatalException{
		LOG.info("Uploading "+filePath+" to Amazon");
		uploadService.uploadFile(new File(filePath), envPrefix+File.separator+filePath.substring(filePath.lastIndexOf(File.separator)+1));
		
	}

}
