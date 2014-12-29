package com.realtech.socialsurvey.core.services.organizationmanagement.impl;

// JIRA: SS-27: By RM05: BOC
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.realtech.socialsurvey.core.commons.CommonConstants;
import com.realtech.socialsurvey.core.dao.GenericDao;
import com.realtech.socialsurvey.core.dao.OrganizationUnitSettingsDao;
import com.realtech.socialsurvey.core.dao.impl.MongoOrganizationUnitSettingDaoImpl;
import com.realtech.socialsurvey.core.entities.Branch;
import com.realtech.socialsurvey.core.entities.CRMInfo;
import com.realtech.socialsurvey.core.entities.Company;
import com.realtech.socialsurvey.core.entities.ContactDetailsSettings;
import com.realtech.socialsurvey.core.entities.LicenseDetail;
import com.realtech.socialsurvey.core.entities.OrganizationUnitSettings;
import com.realtech.socialsurvey.core.entities.ProfilesMaster;
import com.realtech.socialsurvey.core.entities.Region;
import com.realtech.socialsurvey.core.entities.User;
import com.realtech.socialsurvey.core.entities.UserProfile;
import com.realtech.socialsurvey.core.enums.AccountType;
import com.realtech.socialsurvey.core.exception.FatalException;
import com.realtech.socialsurvey.core.exception.InvalidInputException;
import com.realtech.socialsurvey.core.exception.NonFatalException;
import com.realtech.socialsurvey.core.services.organizationmanagement.OrganizationManagementService;
import com.realtech.socialsurvey.core.services.organizationmanagement.UserManagementService;
import com.realtech.socialsurvey.core.services.registration.RegistrationService;

@Component
public class OrganizationManagementServiceImpl implements OrganizationManagementService {

	private static final Logger LOG = LoggerFactory.getLogger(OrganizationManagementServiceImpl.class);

	@Autowired
	private OrganizationUnitSettingsDao organizationUnitSettingsDao;

	@Autowired
	private GenericDao<Company, Long> companyDao;

	@Autowired
	private GenericDao<User, Long> userDao;

	@Autowired
	private GenericDao<Region, Long> regionDao;

	@Autowired
	private GenericDao<Branch, Long> branchDao;

	@Autowired
	private GenericDao<LicenseDetail, Long> licenceDetailDao;

	@Autowired
	private GenericDao<UserProfile, Long> userProfileDao;

	@Autowired
	private GenericDao<ProfilesMaster, Integer> profilesMasterDao;

	@Autowired
	private UserManagementService userManagementService;

	@Autowired
	private RegistrationService registrationService;

	/**
	 * This method adds a new company and updates the same for current user and all its user
	 * profiles.
	 */
	@Override
	@Transactional(rollbackFor = { NonFatalException.class, FatalException.class })
	public User addCompanyInformation(User user, Map<String, String> organizationalDetails) {
		LOG.info("Method addCompanyInformation started for user " + user.getLoginName());
		Company company = addCompany(user, organizationalDetails.get(CommonConstants.COMPANY_NAME), CommonConstants.STATUS_ACTIVE);

		LOG.debug("Calling method for updating company of user");
		updateCompanyForUser(user, company);

		LOG.debug("Calling method for updating company for user profiles");
		updateCompanyForUserProfile(user, company);

		LOG.debug("Calling method for adding organizational details");
		addOrganizationalDetails(user, company, organizationalDetails);

		LOG.info("Method addCompanyInformation finished for user " + user.getLoginName());
		return user;
	}

	// JIRA: SS-28: By RM05: BOC
	/*
	 * To add account as per the choice of User.
	 */
	@Override
	@Transactional(rollbackFor = { NonFatalException.class, FatalException.class })
	public AccountType addAccountTypeForCompany(User user, String strAccountType) throws InvalidInputException {
		LOG.info("Method addAccountTypeForCompany started for user : " + user.getLoginName());
		if (strAccountType == null || strAccountType.isEmpty()) {
			throw new InvalidInputException("account type is null or empty while adding account type fro company");
		}
		int accountTypeValue = 0;
		try {
			accountTypeValue = Integer.parseInt(strAccountType);
		}
		catch (NumberFormatException e) {
			LOG.error("NumberFormatException for account type :" + strAccountType);
			throw new InvalidInputException("account type is not valid while adding account type fro company");
		}
		AccountType accountType = AccountType.getAccountType(accountTypeValue);
		switch (accountType) {
			case INDIVIDUAL:
				addIndividualAccountType(user);
				break;
			case TEAM:
				addTeamAccountType(user);
				break;
			case COMPANY:
				addCompanyAccountType(user);
				break;
			case ENTERPRISE:
				LOG.debug("Selected account type as enterprise so no action required");
				break;
			default:
				throw new InvalidInputException("Account type is not valid");
		}
		LOG.info("Method addAccountTypeForCompany finished.");
		return accountType;
	}

	// JIRA: SS-28: By RM05: EOC

	/**
	 * Fetch the account type master id passing
	 * 
	 * @author RM-06
	 * @param company
	 * @return account master id
	 */
	@Override
	@Transactional
	public long fetchAccountTypeMasterIdForCompany(Company company) throws InvalidInputException {

		LOG.info("Fetch account type for company :" + company.getCompany());

		List<LicenseDetail> licenseDetails = licenceDetailDao.findByColumn(LicenseDetail.class, CommonConstants.COMPANY, company);
		if (licenseDetails == null || licenseDetails.isEmpty()) {
			LOG.error("No license object present for the company : " + company.getCompany());
			return 0;
		}
		LOG.info("Successfully fetched the License detail for the current user's company");

		// return the account type master ID
		return licenseDetails.get(0).getAccountsMaster().getAccountsMasterId();
	};

	/*
	 * This method adds a new company into the COMPANY table.
	 */
	private Company addCompany(User user, String companyName, int isRegistrationComplete) {
		LOG.debug("Method addCompany started for user " + user.getLoginName());
		Company company = new Company();
		company.setCompany(companyName);
		company.setIsRegistrationComplete(isRegistrationComplete);
		company.setStatus(CommonConstants.STATUS_ACTIVE);
		company.setCreatedBy(String.valueOf(user.getUserId()));
		company.setModifiedBy(String.valueOf(user.getUserId()));
		company.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		company.setModifiedOn(new Timestamp(System.currentTimeMillis()));
		LOG.debug("Method addCompany finished.");
		return companyDao.save(company);
	}

	/**
	 * This method updates company details for current user
	 * 
	 * @param user
	 * @param company
	 * @return
	 */
	private User updateCompanyForUser(User user, Company company) {
		LOG.debug("Method updateCompanyForUser started for user " + user.getLoginName());
		user.setCompany(company);
		user.setIsOwner(CommonConstants.IS_OWNER);
		userDao.update(user);
		LOG.debug("Method updateCompanyForUser finished for user " + user.getLoginName());
		return user;
	}

	/**
	 * This method updates company details in all the user profiles of current user.
	 * 
	 * @param user
	 * @param company
	 */
	private void updateCompanyForUserProfile(User user, Company company) {
		LOG.debug("Method updateCompanyForUserProfile started for user " + user.getLoginName());
		user = userDao.findById(User.class, user.getUserId());
		List<UserProfile> userProfiles = userProfileDao.findByColumn(UserProfile.class, "user", user);
		if (userProfiles != null) {
			for (UserProfile userProfile : userProfiles) {
				userProfile.setCompany(company);
				userProfileDao.update(userProfile);
			}
		}
		else {
			LOG.warn("No profiles found for user : " + user.getUserId());
		}
		LOG.debug("Method updateCompanyForUserProfile finished for user " + user.getLoginName());
	}

	/**
	 * This method adds all the key and value pairs into the ORGANIZATION_LEVEL_SETTINGS table.
	 * 
	 * @param user
	 * @param organizationalDetails
	 */
	private void addOrganizationalDetails(User user, Company company, Map<String, String> organizationalDetails) {
		LOG.debug("Method addOrganizationalDetails called.");
		// create a organization settings object
		OrganizationUnitSettings companySettings = new OrganizationUnitSettings();
		companySettings.setIden(company.getCompanyId());
		if (organizationalDetails.get(CommonConstants.LOGO_NAME) != null) {
			companySettings.setLogo(organizationalDetails.get(CommonConstants.LOGO_NAME));
		}
		ContactDetailsSettings contactDetailSettings = new ContactDetailsSettings();
		contactDetailSettings.setName(company.getCompany());
		contactDetailSettings.setAddress(organizationalDetails.get(CommonConstants.ADDRESS));
		contactDetailSettings.setZipcode(organizationalDetails.get(CommonConstants.ZIPCODE));
		companySettings.setContact_details(contactDetailSettings);
		companySettings.setCreatedOn(System.currentTimeMillis());
		companySettings.setCreatedBy(String.valueOf(user.getUserId()));
		// insert the company settings
		LOG.debug("Inserting company settings.");
		organizationUnitSettingsDao.insertOrganizationUnitSettings(companySettings, MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION);

		LOG.debug("Method addOrganizationalDetails finished");
	}

	/**
	 * Method to add an Individual. Makes entry in Region, Branch and UserProfile tables.
	 * 
	 * @param user
	 * @throws InvalidInputException
	 */
	private void addIndividualAccountType(User user) throws InvalidInputException {
		LOG.info("Method addIndividual started for user : " + user.getLoginName());

		LOG.debug("Adding a new region");
		Region region = addRegion(user, CommonConstants.YES, CommonConstants.DEFAULT_BRANCH_NAME);
		ProfilesMaster profilesMaster = userManagementService.getProfilesMasterById(CommonConstants.PROFILES_MASTER_REGION_ADMIN_PROFILE_ID);

		LOG.debug("Creating user profile for region admin");
		UserProfile userProfileRegionAdmin = createUserProfile(user, user.getCompany(), user.getEmailId(), CommonConstants.DEFAULT_AGENT_ID,
				CommonConstants.DEFAULT_BRANCH_ID, region.getRegionId(), profilesMaster.getProfileId(), CommonConstants.PROFILE_STAGES_COMPLETE,
				CommonConstants.STATUS_ACTIVE, String.valueOf(user.getUserId()), String.valueOf(user.getUserId()));
		userProfileDao.save(userProfileRegionAdmin);

		profilesMaster = userManagementService.getProfilesMasterById(CommonConstants.PROFILES_MASTER_BRANCH_ADMIN_PROFILE_ID);

		LOG.debug("Adding a new branch");
		Branch branch = addBranch(user, region, CommonConstants.DEFAULT_BRANCH_NAME, CommonConstants.YES);

		LOG.debug("Creating user profile for branch admin");
		UserProfile userProfileBranchAdmin = createUserProfile(user, user.getCompany(), user.getEmailId(), CommonConstants.DEFAULT_AGENT_ID,
				branch.getBranchId(), CommonConstants.DEFAULT_REGION_ID, profilesMaster.getProfileId(), CommonConstants.PROFILE_STAGES_COMPLETE,
				CommonConstants.STATUS_ACTIVE, String.valueOf(user.getUserId()), String.valueOf(user.getUserId()));
		userProfileDao.save(userProfileBranchAdmin);
		profilesMaster = userManagementService.getProfilesMasterById(CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID);

		LOG.debug("Creating user profile for agent");
		UserProfile userProfileAgent = createUserProfile(user, user.getCompany(), user.getEmailId(), user.getUserId(),
				CommonConstants.DEFAULT_BRANCH_ID, CommonConstants.DEFAULT_REGION_ID, profilesMaster.getProfileId(),
				CommonConstants.PROFILE_STAGES_COMPLETE, CommonConstants.STATUS_ACTIVE, String.valueOf(user.getUserId()),
				String.valueOf(user.getUserId()));
		userProfileDao.save(userProfileAgent);

		LOG.info("Method addIndividual finished.");
	}

	/**
	 * Method to add a Team. Makes entry in Region table.
	 * 
	 * @param user
	 * @throws InvalidInputException
	 */
	private void addTeamAccountType(User user) throws InvalidInputException {
		LOG.debug("Method addTeam started for user : " + user.getLoginName());

		LOG.debug("Adding a new region");
		Region region = addRegion(user, CommonConstants.YES, CommonConstants.DEFAULT_BRANCH_NAME);
		ProfilesMaster profilesMaster = userManagementService.getProfilesMasterById(CommonConstants.PROFILES_MASTER_REGION_ADMIN_PROFILE_ID);

		LOG.debug("Creating user profile for region admin");
		UserProfile userProfileRegionAdmin = createUserProfile(user, user.getCompany(), user.getEmailId(), CommonConstants.DEFAULT_AGENT_ID,
				CommonConstants.DEFAULT_BRANCH_ID, region.getRegionId(), profilesMaster.getProfileId(), CommonConstants.PROFILE_STAGES_COMPLETE,
				CommonConstants.STATUS_ACTIVE, String.valueOf(user.getUserId()), String.valueOf(user.getUserId()));
		userProfileDao.save(userProfileRegionAdmin);

		LOG.debug("Adding a new branch");
		Branch branch = addBranch(user, region, CommonConstants.DEFAULT_BRANCH_NAME, CommonConstants.YES);
		profilesMaster = userManagementService.getProfilesMasterById(CommonConstants.PROFILES_MASTER_BRANCH_ADMIN_PROFILE_ID);

		LOG.debug("Creating user profile for branch admin");
		UserProfile userProfileBranchAdmin = createUserProfile(user, user.getCompany(), user.getEmailId(), CommonConstants.DEFAULT_AGENT_ID,
				branch.getBranchId(), region.getRegionId(), profilesMaster.getProfileId(), CommonConstants.PROFILE_STAGES_COMPLETE,
				CommonConstants.STATUS_ACTIVE, String.valueOf(user.getUserId()), String.valueOf(user.getUserId()));
		userProfileDao.save(userProfileBranchAdmin);

		LOG.debug("Updating profile stage to payment stage for account type team");
		registrationService.updateProfileCompletionStage(user, CommonConstants.PROFILES_MASTER_COMPANY_ADMIN_PROFILE_ID,
				CommonConstants.DASHBOARD_STAGE);

		LOG.debug("Method addTeam finished.");
	}

	/**
	 * Method to add company account type
	 * 
	 * @param user
	 * @throws InvalidInputException
	 */
	private void addCompanyAccountType(User user) throws InvalidInputException {
		LOG.debug("Method addCompanyAccountType started for user : " + user.getLoginName());

		LOG.debug("Adding a new region");
		Region region = addRegion(user, CommonConstants.YES, CommonConstants.DEFAULT_BRANCH_NAME);
		ProfilesMaster profilesMaster = userManagementService.getProfilesMasterById(CommonConstants.PROFILES_MASTER_REGION_ADMIN_PROFILE_ID);

		LOG.debug("Creating user profile for region admin");
		UserProfile userProfile = createUserProfile(user, user.getCompany(), user.getEmailId(), CommonConstants.DEFAULT_AGENT_ID,
				CommonConstants.DEFAULT_BRANCH_ID, region.getRegionId(), profilesMaster.getProfileId(), CommonConstants.PROFILE_STAGES_COMPLETE,
				CommonConstants.STATUS_ACTIVE, String.valueOf(user.getUserId()), String.valueOf(user.getUserId()));
		userProfileDao.save(userProfile);

		LOG.debug("Method addCompanyAccountType finished.");

	}

	/**
	 * Method to add a new region
	 * 
	 * @param user
	 * @param isDefaultBySystem
	 * @param regionName
	 * @return
	 */
	@Override
	public Region addRegion(User user, int isDefaultBySystem, String regionName) {
		LOG.debug("Method addRegion started for user : " + user.getLoginName() + " isDefaultBySystem : " + isDefaultBySystem + " regionName :"
				+ regionName);
		Region region = new Region();
		region.setCompany(user.getCompany());
		region.setIsDefaultBySystem(isDefaultBySystem);
		region.setStatus(CommonConstants.STATUS_ACTIVE);
		region.setRegion(regionName);
		region.setCreatedBy(String.valueOf(user.getUserId()));
		region.setModifiedBy(String.valueOf(user.getUserId()));
		region.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		region.setModifiedOn(new Timestamp(System.currentTimeMillis()));
		region = regionDao.save(region);
		LOG.debug("Method addRegion finished.");
		return region;
	}

	/**
	 * Method to add a new Branch
	 * 
	 * @param user
	 * @param region
	 * @param branchName
	 * @param isDefaultBySystem
	 * @return
	 */
	@Override
	public Branch addBranch(User user, Region region, String branchName, int isDefaultBySystem) {
		LOG.debug("Method addBranch started for user : " + user.getLoginName());
		Branch branch = new Branch();
		branch.setCompany(user.getCompany());
		branch.setRegion(region);
		branch.setStatus(CommonConstants.STATUS_ACTIVE);
		branch.setBranch(branchName);
		branch.setIsDefaultBySystem(isDefaultBySystem);
		branch.setCreatedBy(String.valueOf(user.getUserId()));
		branch.setModifiedBy(String.valueOf(user.getUserId()));
		branch.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		branch.setModifiedOn(new Timestamp(System.currentTimeMillis()));
		branch = branchDao.save(branch);
		LOG.debug("Method addBranch finished.");
		return branch;
	}

	private UserProfile createUserProfile(User user, Company company, String emailId, long agentId, long branchId, long regionId,
			int profileMasterId, String profileCompletionStage, int isProfileComplete, String createdBy, String modifiedBy) {
		LOG.info("Method createUserProfile called for username : " + user.getLoginName());
		UserProfile userProfile = new UserProfile();
		userProfile.setAgentId(agentId);
		userProfile.setBranchId(branchId);
		userProfile.setCompany(company);
		userProfile.setEmailId(emailId);
		userProfile.setIsProfileComplete(isProfileComplete);
		userProfile.setProfilesMaster(profilesMasterDao.findById(ProfilesMaster.class, profileMasterId));
		userProfile.setProfileCompletionStage(profileCompletionStage);
		userProfile.setRegionId(regionId);
		userProfile.setStatus(CommonConstants.STATUS_ACTIVE);
		userProfile.setUser(user);
		Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
		userProfile.setCreatedOn(currentTimestamp);
		userProfile.setModifiedOn(currentTimestamp);
		userProfile.setCreatedBy(createdBy);
		userProfile.setModifiedBy(modifiedBy);
		LOG.debug("Method createUserProfile() finished");
		return userProfile;
	}

	@Override
	public void editCompanySettings(User user) {
		LOG.info("Editing company information by user: " + user.toString());
	}

	@Override
	public OrganizationUnitSettings getCompanySettings(User user) throws InvalidInputException {
		if (user == null) {
			throw new InvalidInputException("User is not set");
		}
		LOG.info("Get company settings for the user: " + user.toString());
		// get the company id
		if (user.getCompany() == null) {
			throw new InvalidInputException("User object is partially set. Could not find the comany details");
		}
		long companyId = user.getCompany().getCompanyId();
		OrganizationUnitSettings companySettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById(companyId,
				MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION);
		return companySettings;
	}

	@Override
	public Map<Long, OrganizationUnitSettings> getRegionSettingsForUserProfiles(List<UserProfile> userProfiles) throws InvalidInputException {
		Map<Long, OrganizationUnitSettings> regionSettings = null;
		if (userProfiles != null && userProfiles.size() > 0) {
			LOG.info("Get region settings for the user profiles: " + userProfiles.toString());
			regionSettings = new HashMap<Long, OrganizationUnitSettings>();
			OrganizationUnitSettings regionSetting = null;
			// get the region profiles and get the settings for each of them.
			for (UserProfile userProfile : userProfiles) {
				regionSetting = new OrganizationUnitSettings();
				if (userProfile.getProfilesMaster().getProfileId() == CommonConstants.PROFILES_MASTER_REGION_ADMIN_PROFILE_ID) {
					LOG.debug("Getting settings for " + userProfile);
					// get the region id and get the profile
					if (userProfile.getRegionId() > 0l) {
						regionSetting = getRegionSettings(userProfile.getRegionId());
						if (regionSetting != null) {
							regionSettings.put(userProfile.getRegionId(), regionSetting);
						}
					}
					else {
						LOG.warn("Not a valid region id for region profile: " + userProfile + ". Skipping the record");
					}
				}
			}
		}
		else {
			throw new InvalidInputException("User profiles are not set");
		}

		return regionSettings;
	}

	@Override
	public Map<Long, OrganizationUnitSettings> getBranchSettingsForUserProfiles(List<UserProfile> userProfiles) throws InvalidInputException {
		Map<Long, OrganizationUnitSettings> branchSettings = null;
		if (userProfiles != null && userProfiles.size() > 0) {
			LOG.info("Get branch settings for the user profiles: " + userProfiles.toString());
			branchSettings = new HashMap<Long, OrganizationUnitSettings>();
			OrganizationUnitSettings branchSetting = null;
			// get the branch profiles and get the settings for each of them.
			for (UserProfile userProfile : userProfiles) {
				branchSetting = new OrganizationUnitSettings();
				if (userProfile.getProfilesMaster().getProfileId() == CommonConstants.PROFILES_MASTER_BRANCH_ADMIN_PROFILE_ID) {
					LOG.debug("Getting settings for " + userProfile);
					// get the branch id and get the profile
					if (userProfile.getBranchId() > 0l) {
						branchSetting = getBranchSettings(userProfile.getBranchId());
						if (branchSetting != null) {
							branchSettings.put(userProfile.getBranchId(), branchSetting);
						}
					}
					else {
						LOG.warn("Not a valid branch id for branch profile: " + userProfile + ". Skipping the record");
					}
				}
			}
		}
		else {
			throw new InvalidInputException("User profiles are not set");
		}

		return branchSettings;
	}

	@Override
	public OrganizationUnitSettings getRegionSettings(long regionId) throws InvalidInputException {
		OrganizationUnitSettings regionSettings = null;
		if (regionId <= 0l) {
			throw new InvalidInputException("Invalid region id. :" + regionId);
		}
		LOG.info("Get the region settings for region id: " + regionId);
		regionSettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById(regionId,
				MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION);
		return regionSettings;
	}

	@Override
	public OrganizationUnitSettings getBranchSettings(long branchId) throws InvalidInputException {
		OrganizationUnitSettings branchSettings = null;
		if (branchId <= 0l) {
			throw new InvalidInputException("Invalid branch id. :" + branchId);
		}
		LOG.info("Get the branch settings for region id: " + branchId);
		branchSettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById(branchId,
				MongoOrganizationUnitSettingDaoImpl.BRANCH_SETTINGS_COLLECTION);
		return branchSettings;
	}

	@Override
	public void updateCRMDetails(OrganizationUnitSettings companySettings, CRMInfo crmInfo) throws InvalidInputException {
		if (companySettings == null) {
			throw new InvalidInputException("Company settings cannot be null.");
		}
		if (crmInfo == null) {
			throw new InvalidInputException("CRM info cannot be null.");
		}
		LOG.info("Updating comapnySettings: " + companySettings+" with crm info: "+crmInfo);
		organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(MongoOrganizationUnitSettingDaoImpl.KEY_CRM_INFO, crmInfo, companySettings, MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION);
		LOG.info("Updated the record successfully");
	}

}

// JIRA: SS-27: By RM05: EOC