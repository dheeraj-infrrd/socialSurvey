package com.realtech.socialsurvey.core.services.organizationmanagement.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.realtech.socialsurvey.core.commons.CommonConstants;
import com.realtech.socialsurvey.core.dao.GenericDao;
import com.realtech.socialsurvey.core.dao.UserDao;
import com.realtech.socialsurvey.core.dao.UserInviteDao;
import com.realtech.socialsurvey.core.dao.UserProfileDao;
import com.realtech.socialsurvey.core.entities.Branch;
import com.realtech.socialsurvey.core.entities.Company;
import com.realtech.socialsurvey.core.entities.LicenseDetail;
import com.realtech.socialsurvey.core.entities.ProfilesMaster;
import com.realtech.socialsurvey.core.entities.User;
import com.realtech.socialsurvey.core.entities.UserInvite;
import com.realtech.socialsurvey.core.entities.UserProfile;
import com.realtech.socialsurvey.core.exception.DatabaseException;
import com.realtech.socialsurvey.core.exception.FatalException;
import com.realtech.socialsurvey.core.exception.InvalidInputException;
import com.realtech.socialsurvey.core.exception.NoRecordsFetchedException;
import com.realtech.socialsurvey.core.exception.NonFatalException;
import com.realtech.socialsurvey.core.exception.UserAlreadyExistsException;
import com.realtech.socialsurvey.core.services.generator.URLGenerator;
import com.realtech.socialsurvey.core.services.mail.EmailServices;
import com.realtech.socialsurvey.core.services.mail.UndeliveredEmailException;
import com.realtech.socialsurvey.core.services.organizationmanagement.UserManagementService;
import com.realtech.socialsurvey.core.utils.EncryptionHelper;

/**
 * JIRA:SS-34 BY RM02 Implementation for User management services
 */
@DependsOn("generic")
@Component
public class UserManagementServiceImpl implements UserManagementService, InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(UserManagementServiceImpl.class);
	private static Map<Integer, ProfilesMaster> profileMasters = new HashMap<Integer, ProfilesMaster>();

	@Autowired
	private URLGenerator urlGenerator;

	@Value("${APPLICATION_BASE_URL}")
	private String applicationBaseUrl;

	@Autowired
	private EmailServices emailServices;

	@Autowired
	private EncryptionHelper encryptionHelper;

	@Resource
	@Qualifier("userInvite")
	private UserInviteDao userInviteDao;

	@Autowired
	private GenericDao<Company, Long> companyDao;

	@Autowired
	private GenericDao<ProfilesMaster, Integer> profilesMasterDao;

	@Resource
	@Qualifier("user")
	private UserDao userDao;

	@Resource
	@Qualifier("userProfile")
	private UserProfileDao userProfileDao;

	@Autowired
	private GenericDao<LicenseDetail, Long> licenseDetailsDao;

	@Autowired
	private GenericDao<Branch, Long> branchDao;

	/**
	 * Method to get profile master based on profileId, gets the profile master from Map which is
	 * pre-populated with afterPropertiesSet method
	 */
	@Override
	@Transactional
	public ProfilesMaster getProfilesMasterById(int profileId) throws InvalidInputException {
		LOG.info("Method getProfilesMasterById called for profileId : " + profileId);
		if (profileId <= 0) {
			throw new InvalidInputException("profile Id is not set for getting profile master");
		}
		ProfilesMaster profilesMaster = null;
		if (profileMasters.containsKey(profileId)) {
			profilesMaster = profileMasters.get(profileId);
		}
		else {
			throw new InvalidInputException("No profile master detected for profileID : " + profileId);
		}
		LOG.info("Method getProfilesMasterById finished for profileId : " + profileId);
		return profilesMaster;
	}

	// Moved code from RegistrationServiceImpl By RM-05:BOC

	@Override
	@Transactional(rollbackFor = { NonFatalException.class, FatalException.class })
	public void inviteCorporateToRegister(String firstName, String lastName, String emailId) throws InvalidInputException, UndeliveredEmailException,
			NonFatalException {
		LOG.info("Inviting corporate to register. Details\t first name:" + firstName + "\t lastName: " + lastName + "\t email id: " + emailId);

		Map<String, String> urlParams = new HashMap<String, String>();
		urlParams.put(CommonConstants.FIRST_NAME, firstName);
		urlParams.put(CommonConstants.LAST_NAME, lastName);
		urlParams.put(CommonConstants.EMAIL_ID, emailId);

		LOG.debug("Generating URL");
		String url = urlGenerator.generateUrl(urlParams, applicationBaseUrl + CommonConstants.REQUEST_MAPPING_SHOW_REGISTRATION);
		LOG.debug("Sending invitation for registration");
		inviteUser(url, emailId, firstName, lastName);

		LOG.info("Successfully sent invitation to :" + emailId + " for registration");
	}

	/*
	 * This method contains the process to be done after URL is hit by the User. It involves
	 * decrypting URL and validating parameters.
	 */
	@Override
	@Transactional(rollbackFor = { NonFatalException.class, FatalException.class })
	public Map<String, String> validateRegistrationUrl(String encryptedUrlParameter) throws InvalidInputException {
		LOG.info("Method validateRegistrationUrl() called ");

		Map<String, String> urlParameters = urlGenerator.decryptParameters(encryptedUrlParameter);
		validateCompanyRegistrationUrlParameters(encryptedUrlParameter);

		LOG.info("Method validateRegistrationUrl() finished ");
		return urlParameters;
	}

	// JIRA: SS-27: By RM05: BOC
	/**
	 * This method creates a new user, user profile post validation of URL and also invalidates the
	 * registration link used by the user to register
	 * 
	 * @throws UserAlreadyExistsException
	 * @throws UndeliveredEmailException
	 */
	@Override
	@Transactional(rollbackFor = { NonFatalException.class, FatalException.class })
	public User addCorporateAdminAndUpdateStage(String firstName, String lastName, String emailId, String password, boolean isDirectRegistration)
			throws InvalidInputException, UserAlreadyExistsException, UndeliveredEmailException {
		LOG.info("Method to add corporate admin called for emailId : " + emailId);
		if (userExists(emailId)) {
			throw new UserAlreadyExistsException("User with User ID : " + emailId + " already exists");
		}
		Company company = companyDao.findById(Company.class, CommonConstants.DEFAULT_COMPANY_ID);
		String encryptedPassword = encryptionHelper.encryptSHA512(password);
		int status = CommonConstants.STATUS_ACTIVE;

		/**
		 * If the registration is not through an invite, status of the user is "not verified" and a
		 * verification link is sent. For an invitation, email is already verified hence status is
		 * active
		 */
		if (isDirectRegistration) {
			status = CommonConstants.STATUS_NOT_VERIFIED;
		}

		LOG.debug("Creating new user with emailId : " + emailId + " and verification status : " + status);
		User user = createUser(company, encryptedPassword, emailId, firstName, lastName, status);
		user = userDao.save(user);

		LOG.debug("Creating user profile for :" + emailId + " with profile completion stage : " + CommonConstants.ADD_COMPANY_STAGE);
		UserProfile userProfile = createUserProfile(user, company, emailId, CommonConstants.DEFAULT_AGENT_ID, CommonConstants.DEFAULT_BRANCH_ID,
				CommonConstants.DEFAULT_REGION_ID, CommonConstants.PROFILES_MASTER_COMPANY_ADMIN_PROFILE_ID, CommonConstants.ADD_COMPANY_STAGE,
				CommonConstants.STATUS_INACTIVE, String.valueOf(user.getUserId()), String.valueOf(user.getUserId()));
		userProfileDao.save(userProfile);

		/**
		 * if it is direct registration, send verification link else invalidate the invitation link
		 */
		if (isDirectRegistration) {
			LOG.debug("Calling method for sending verification link for user : " + user.getUserId());
			sendVerificationLink(user);
		}
		else {
			LOG.debug("Invalidating registration link for emailId : " + emailId);
			invalidateRegistrationInvite(emailId);
		}

		LOG.info("Successfully executed method to add corporate admin for emailId : " + emailId);
		return user;
	}

	/**
	 * JIRA SS-35 BY RM02 Method to update the profile completion stage of user i.e the stage which
	 * user has completed while registration, stores the next step to be taken by user while
	 * registration process
	 * 
	 * @throws InvalidInputException
	 */
	@Override
	@Transactional
	public void updateProfileCompletionStage(User user, int profileMasterId, String profileCompletionStage) throws InvalidInputException {
		if (profileCompletionStage == null || profileCompletionStage.isEmpty()) {
			throw new InvalidInputException("Profile completion stage is not set for updation");
		}
		if (user == null) {
			throw new InvalidInputException("UserId is not null for updation of Profile completion stage");
		}
		if (profileMasterId <= 0) {
			throw new InvalidInputException("Profile master id is not set for updation of Profile completion stage");
		}
		LOG.info("Mehtod updateProfileCompletionStage called for profileCompletionStage : " + profileCompletionStage + " and profileMasterId : "
				+ profileMasterId + " and userId : " + user.getUserId());
		Map<String, Object> queries = new HashMap<String, Object>();
		queries.put(CommonConstants.USER_COLUMN, user);
		queries.put(CommonConstants.PROFILE_MASTER_COLUMN, getProfilesMasterById(profileMasterId));
		List<UserProfile> userProfiles = userProfileDao.findByKeyValue(UserProfile.class, queries);

		if (userProfiles != null && !userProfiles.isEmpty()) {
			for (UserProfile userProfile : userProfiles) {
				userProfile.setProfileCompletionStage(profileCompletionStage);
				userProfile.setModifiedOn(new Timestamp(System.currentTimeMillis()));
				userProfile.setModifiedBy(String.valueOf(user.getUserId()));
				userProfileDao.update(userProfile);
			}
		}
		else {
			LOG.warn("No profile found for updating profile completion stage");
		}
		LOG.info("Mehtod updateProfileCompletionStage finished for profileCompletionStage : " + profileCompletionStage);
	}

	/**
	 * Method to verify a user's account
	 * 
	 * @param encryptedUrlParams
	 * @throws InvalidInputException
	 */
	@Transactional(rollbackFor = { NonFatalException.class, FatalException.class })
	public void verifyAccount(String encryptedUrlParams) throws InvalidInputException {
		LOG.info("Method to verify account called for encryptedUrlParams");
		Map<String, String> urlParams = urlGenerator.decryptParameters(encryptedUrlParams);
		if (urlParams == null || urlParams.isEmpty()) {
			throw new InvalidInputException("Url params are invalid for account verification");
		}
		if (urlParams.containsKey(CommonConstants.USER_ID)) {
			long userId = Long.parseLong(urlParams.get(CommonConstants.USER_ID));

			LOG.debug("Calling user management service for updating user status to active");
			updateUserStatus(userId, CommonConstants.STATUS_ACTIVE);
		}
		else {
			throw new InvalidInputException("User id field not present in url params");
		}
		LOG.info("Successfully completed method to verify account");
	}

	// Moved code from RegistrationServiceImpl By RM-05: EOC

	// JIRA SS-42 BY RM05 BOC

	/*
	 * Method to add a new user into a company. Admin sends the invite to user for registering.
	 */
	@Transactional
	@Override
	public void inviteUserToRegister(User admin, String firstName, String lastName, String emailId) throws InvalidInputException,
			UserAlreadyExistsException, UndeliveredEmailException {
		if (firstName == null || firstName.isEmpty()) {
			throw new InvalidInputException("First name is either null or empty in inviteUserToRegister().");
		}
		if (lastName == null || lastName.isEmpty()) {
			throw new InvalidInputException("Last name is either null or empty in inviteUserToRegister().");
		}
		if (emailId == null || emailId.isEmpty()) {
			throw new InvalidInputException("Email Id is either null or empty in inviteUserToRegister().");
		}
		LOG.info("Method to add a new user, inviteUserToRegister() called for email id : " + emailId);

		if (userExists(emailId)) {
			throw new UserAlreadyExistsException("User with User ID : " + emailId + " already exists");
		}

		User user = createUser(admin.getCompany(), null, emailId, firstName, lastName, CommonConstants.STATUS_NOT_VERIFIED);
		user = userDao.save(user);

		LOG.debug("Creating user profile for :" + emailId + " with profile completion stage : " + CommonConstants.ADD_COMPANY_STAGE);
		UserProfile userProfile = createUserProfile(user, user.getCompany(), emailId, CommonConstants.DEFAULT_AGENT_ID,
				CommonConstants.DEFAULT_BRANCH_ID, CommonConstants.DEFAULT_REGION_ID, CommonConstants.PROFILES_MASTER_COMPANY_ADMIN_PROFILE_ID,
				CommonConstants.ADD_COMPANY_STAGE, CommonConstants.STATUS_INACTIVE, String.valueOf(user.getUserId()),
				String.valueOf(user.getUserId()));
		userProfileDao.save(userProfile);

		sendVerificationLink(user);
		LOG.info("Method to add a new user, inviteUserToRegister() finished for email id : " + emailId);
	}

	/*
	 * Method to deactivate an existing user.
	 */
	@Transactional
	@Override
	public void removeExistingUser(User admin, long userIdToRemove) throws InvalidInputException {

		if (admin == null) {
			throw new InvalidInputException("Admin user is null in deactivateExistingUser");
		}
		if (userIdToRemove <= 0l) {
			throw new InvalidInputException("User id is invalid in deactivateExistingUser");
		}

		LOG.info("Method to deactivate user " + userIdToRemove + " called.");

		User userToBeDeactivated = userDao.findById(User.class, userIdToRemove);

		userToBeDeactivated.setStatus(CommonConstants.STATUS_INACTIVE);
		userToBeDeactivated.setModifiedBy(String.valueOf(admin.getUserId()));
		userToBeDeactivated.setModifiedOn(new Timestamp(System.currentTimeMillis()));

		LOG.info("Deactivating user " + userToBeDeactivated.getFirstName());
		userDao.update(userToBeDeactivated);

		// Marks all the user profiles for given user as inactive.
		userProfileDao.deactivateAllUserProfilesForUser(admin, userToBeDeactivated);

		LOG.info("Method to deactivate user " + userToBeDeactivated.getFirstName() + " finished.");
	}

	@Transactional
	@Override
	public User getUserByEmailId(User admin, String emailId) throws InvalidInputException, NoRecordsFetchedException {
		LOG.info("Method to fetch list of users on the basis of email id is called.");

		if (admin == null) {
			throw new InvalidInputException("Admin user is null in getUsersByEmailId()");
		}
		if (emailId == null || emailId.isEmpty()) {
			throw new InvalidInputException("Email id is null or empty in getUsersByEmailId()");
		}

		User user = userDao.fetchUserByEmailId(admin, emailId);
		LOG.info("Method to fetch list of users on the basis of email id is finished.");
		return user;
	}

	@Transactional
	@Override
	public List<User> getUsersBySimilarEmailId(User admin, String emailId) throws InvalidInputException {
		LOG.info("Method to fetch list of users on the basis of email id is called.");

		if (admin == null) {
			throw new InvalidInputException("Admin user is null in getUsersByEmailId()");
		}
		if (emailId == null || emailId.isEmpty()) {
			throw new InvalidInputException("Email id is null or empty in getUsersByEmailId()");
		}

		List<User> users = userDao.fetchUsersBySimilarEmailId(admin, emailId);

		LOG.info("Method to fetch list of users on the basis of email id is finished.");
		return users;
	}

	@Transactional
	@Override
	public boolean isUserAdditionAllowed(User user) throws NoRecordsFetchedException {
		LOG.info("Method to check whether users can be added or not started.");
		boolean isUserAdditionAllowed = false;

		List<LicenseDetail> licenseDetails = licenseDetailsDao.findByColumn(LicenseDetail.class, CommonConstants.COMPANY, user.getCompany());

		if (licenseDetails == null || licenseDetails.isEmpty()) {
			LOG.error("Could not find any record in License_Details for : " + user.getCompany().getCompany());
			throw new NoRecordsFetchedException("Could not find any record in License_Details for : " + user.getCompany().getCompany());
		}

		int maxUsersAllowed = licenseDetails.get(CommonConstants.INITIAL_INDEX).getAccountsMaster().getMaxUsersAllowed();
		int currentNumberOfUsers = userDao.getUsersCountForCompany(user.getCompany());

		isUserAdditionAllowed = (currentNumberOfUsers < maxUsersAllowed) ? true : false;

		LOG.info("Method to check whether users can be added or not finished.");
		return isUserAdditionAllowed;
	}

	// JIRA SS-42 BY RM05 EOC

	/**
	 * Method to fetch profile masters from db and store in the map
	 */
	private void populateProfileMastersMap() {
		LOG.debug("Getting all profile masters from database and storing in map");
		List<ProfilesMaster> profileMasterList = profilesMasterDao.findAll(ProfilesMaster.class);
		if (profileMasterList != null && !profileMasterList.isEmpty()) {
			for (ProfilesMaster profilesMaster : profileMasterList) {
				profileMasters.put(profilesMaster.getProfileId(), profilesMaster);
			}
		}
		else {
			LOG.warn("No profile master found in database");
		}
		LOG.debug("Successfully populated profile masters from database into map");
	}

	/**
	 * JIRA SS-42 BY RM05 BOC Method to remove profile of a branch admin.
	 */
	@Transactional
	@Override
	public void unassignBranchAdmin(User admin, long branchId, long userIdToRemove) throws InvalidInputException {
		if (admin == null) {
			throw new InvalidInputException("Admin user is null in removeBranchAdmin");
		}
		if (branchId <= 0l) {
			throw new InvalidInputException("Branch id is invalid in removeBranchAdmin");
		}
		if (userIdToRemove <= 0l) {
			throw new InvalidInputException("User id is invalid in removeBranchAdmin");
		}
		LOG.info("Method to removeBranchAdmin called for branchId : " + branchId + " and userId : " + userIdToRemove);

		LOG.debug("Selecting user for the userId provided for branch admin : " + userIdToRemove);
		User userToBeDeactivated = userDao.findById(User.class, userIdToRemove);
		if (userToBeDeactivated == null) {
			throw new InvalidInputException("No user found for userId specified in createBranchAdmin");
		}
		/**
		 * admin is the logged in user, userToBeDeactivated is the user passed by admin to
		 * deactivate.
		 */
		userProfileDao.deactivateUserProfileForBranch(admin, branchId, userToBeDeactivated);

		LOG.info("Method to removeBranchAdmin finished for branchId : " + branchId + " and userId : " + userIdToRemove);
	}

	/**
	 * Method to remove profile of a region admin.
	 */
	@Transactional
	@Override
	public void unassignRegionAdmin(User admin, long regionId, long userId) throws InvalidInputException {
		if (admin == null) {
			throw new InvalidInputException("Admin user is null in unassignRegionAdmin");
		}
		if (regionId <= 0l) {
			throw new InvalidInputException("Region id is invalid in unassignRegionAdmin");
		}
		if (userId <= 0l) {
			throw new InvalidInputException("User id is invalid in unassignRegionAdmin");
		}
		LOG.info("Method to removeRegionAdmin called for regionId : " + regionId + " and userId : " + userId);

		LOG.debug("Selecting user for the userId provided for region admin : " + userId);
		User userToBeDeactivated = userDao.findById(User.class, userId);
		if (userToBeDeactivated == null) {
			throw new InvalidInputException("No user found for userId specified in unassignRegionAdmin");
		}

		userProfileDao.deactivateUserProfileForRegion(admin, regionId, userToBeDeactivated);

		LOG.info("Method to unassignRegionAdmin finished for regionId : " + regionId + " and userId : " + userId);

	}

	/*
	 * Method to return User on the basis of user id provided.
	 */
	@Transactional
	@Override
	public User getUserByUserId(long userId) {
		LOG.info("Method to find user on the basis of user id started for user id " + userId);
		User user = null;
		user = userDao.findById(User.class, userId);
		LOG.info("Method to find user on the basis of user id finished for user id " + userId);
		return user;
	}

	/*
	 * Method to return list of branches assigned to the user passed.
	 */
	@Transactional
	@Override
	public List<Branch> getBranchesAssignedToUser(User user) throws NoRecordsFetchedException {
		LOG.info("Method to find branches assigned to the user started for " + user.getFirstName());
		List<Long> branchIds = userProfileDao.getBranchIdsForUser(user);
		if (branchIds == null || branchIds.isEmpty()) {
			LOG.error("No branch found for user : " + user.getUserId());
			throw new NoRecordsFetchedException("No branch found for user : " + user.getUserId());
		}
		List<Branch> branches = branchDao.findByColumnForMultipleValues(Branch.class, "branchId", branchIds);
		LOG.info("Method to find branches assigned to the user finished for " + user.getFirstName());
		return branches;
	}

	/*
	 * Method to return list of users belonging to the same company as that of user passed.
	 */
	@Transactional
	@Override
	public List<User> getUsersForCompany(long userId) throws InvalidInputException, NoRecordsFetchedException {
		LOG.info("Method getUsersForCompany() started for " + userId);
		User user = userDao.findById(User.class, userId);
		if (user == null) {
			LOG.error("No user found for user id : " + userId);
			throw new InvalidInputException("No user found for userId specified in getUsersForCompany()");
		}
		List<User> users = userDao.getUsersForCompany(user.getCompany());
		if (users == null || users.isEmpty()) {
			LOG.error("No user found for company : " + user.getCompany().getCompany());
			throw new NoRecordsFetchedException("No user found for company : " + user.getCompany().getCompany());
		}
		LOG.info("Method getUsersForCompany() started for " + userId);
		return users;
	}

	// JIRA SS-42 BY RM05 EOC

	@Override
	public void afterPropertiesSet() throws Exception {
		LOG.info("afterPropertiesSet for UserManagementServiceImpl called");

		LOG.debug("Populating profile master from db into the hashMap");
		populateProfileMastersMap();
		LOG.debug("Successfully populated profile master from db into the hashMap");

		LOG.info("afterPropertiesSet for UserManagementServiceImpl completed");
	}

	/**
	 * Method to create profile for a branch admin
	 */
	@Override
	@Transactional
	public User assignBranchAdmin(User admin, long branchId, long userId) throws InvalidInputException {
		if (admin == null) {
			throw new InvalidInputException("Company is null in assignBranchAdmin");
		}
		if (branchId <= 0l) {
			throw new InvalidInputException("Branch id is invalid in assignBranchAdmin");
		}
		if (userId <= 0l) {
			throw new InvalidInputException("User id is invalid in assignBranchAdmin");
		}
		LOG.info("Method to assign branch admin called for branchId : " + branchId + " and userId : " + userId);

		LOG.debug("Selecting user for the userId provided for branch admin : " + userId);

		User user = userDao.findById(User.class, userId);
		if (user == null) {
			throw new InvalidInputException("No user found for userId specified in createBranchAdmin");
		}

		// Re-activate the existing user profile if user for given branch
		// already exists.
		UserProfile userProfile = getUserProfileForBranch(branchId, user);
		if (userProfile == null) {
			LOG.debug("Creating new User profile as User does not exist for given branch.");
			/**
			 * created and modified by are of the logged in user, rest user attributes come from
			 */
			userProfile = createUserProfile(user, admin.getCompany(), user.getEmailId(), CommonConstants.DEFAULT_AGENT_ID, branchId,
					CommonConstants.DEFAULT_REGION_ID, CommonConstants.PROFILES_MASTER_BRANCH_ADMIN_PROFILE_ID, CommonConstants.DASHBOARD_STAGE,
					CommonConstants.STATUS_ACTIVE, String.valueOf(admin.getUserId()), String.valueOf(admin.getUserId()));
			userProfileDao.save(userProfile);
		}
		else if (userProfile.getStatus() == CommonConstants.STATUS_INACTIVE) {
			LOG.info("User profile for same user and branch already exists. Activating the same.");
			userProfile.setStatus(CommonConstants.STATUS_ACTIVE);
			userProfile.setModifiedBy(String.valueOf(admin.getUserId()));
			userProfile.setModifiedOn(new Timestamp(System.currentTimeMillis()));
			userProfileDao.update(userProfile);
		}

		LOG.info("Method to assignBranchAdmin finished for branchId : " + branchId + " and userId : " + userId);

		return user;
	}

	/**
	 * Method to create profile for a region admin
	 */
	@Override
	public User assignRegionAdmin(User assigneeUser, long regionId, long userId) throws InvalidInputException {
		if (assigneeUser == null) {
			throw new InvalidInputException("Company is null in createRegionAdmin");
		}
		if (regionId <= 0l) {
			throw new InvalidInputException("Region id is invalid in createRegionAdmin");
		}
		if (userId <= 0l) {
			throw new InvalidInputException("User id is invalid in createRegionAdmin");
		}
		LOG.info("Method to createRegionAdmin called for regionId : " + regionId + " and userId : " + userId);

		LOG.debug("Selecting user for the userId provided for region admin : " + userId);
		User user = userDao.findById(User.class, userId);
		if (user == null) {
			throw new InvalidInputException("No user found for userId specified in createRegionAdmin");
		}
		UserProfile userProfile = createUserProfile(user, assigneeUser.getCompany(), user.getEmailId(), CommonConstants.DEFAULT_AGENT_ID,
				CommonConstants.DEFAULT_BRANCH_ID, regionId, CommonConstants.PROFILES_MASTER_REGION_ADMIN_PROFILE_ID,
				CommonConstants.DASHBOARD_STAGE, CommonConstants.STATUS_ACTIVE, String.valueOf(assigneeUser.getUserId()),
				String.valueOf(assigneeUser.getUserId()));
		userProfileDao.save(userProfile);

		LOG.info("Method to createRegionAdmin finished for regionId : " + regionId + " and userId : " + userId);

		return user;
	}

	private UserProfile createUserProfile(User user, Company company, String emailId, long agentId, long branchId, long regionId,
			int profileMasterId, String profileCompletionStage, int isProfileComplete, String createdBy, String modifiedBy) {
		LOG.debug("Method createUserProfile called for username : " + user.getLoginName());
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

	// Method to check if a user is already assigned to a branch.
	// Returns the status of user if found, -1 otherwise.
	private UserProfile getUserProfileForBranch(long branchId, User user) {
		LOG.debug("Method to check whether same user is already present in user profile with inactive state started.");

		UserProfile userProfile = null;

		Map<String, Object> queries = new HashMap<>();
		queries.put(CommonConstants.USER_COLUMN, user);
		queries.put(CommonConstants.BRANCH_ID_COLUMN, branchId);
		List<UserProfile> userProfiles = userProfileDao.findByKeyValue(UserProfile.class, queries);

		if (userProfiles == null || userProfiles.isEmpty()) {
			LOG.debug("User " + user.getFirstName() + " has never been assigned to branch id " + branchId + " earlier.");
		}

		else {
			userProfile = userProfiles.get(CommonConstants.INITIAL_INDEX);
			LOG.debug("User " + user.getFirstName() + " is already present for branch " + branchId + " with status " + userProfile.getStatus());
		}
		LOG.debug("Method to check whether same user is already present in user profile with inactive state completed.");
		return userProfile;
	}

	/**
	 * Method to update a user's status
	 */
	@Override
	@Transactional
	public void updateUserStatus(long userId, int status) throws InvalidInputException {
		LOG.info("Method updateUserStatus of user management services called for userId : " + userId + " and status :" + status);
		User user = userDao.findById(User.class, userId);
		if (user == null) {
			throw new InvalidInputException("No user present for the specified userId");
		}
		user.setStatus(status);
		user.setModifiedBy(String.valueOf(userId));
		user.setModifiedOn(new Timestamp(System.currentTimeMillis()));
		userDao.update(user);

		LOG.info("Successfully completed method to update user status");
	}

	/*
	 * Method to assign a user to a particular branch.
	 */
	@Transactional
	@Override
	public void assignUserToBranch(User admin, long userId, long branchId) throws InvalidInputException {
		
		if (admin == null) {
			throw new InvalidInputException("No admin user present.");
		}
		LOG.info("Method to assign user to a branch called for user : " + admin.getUserId());
		User user = userDao.findById(User.class, userId);

		if (user == null) {
			throw new InvalidInputException("No user present for the specified userId");
		}
		
		Map<String, Object> queries = new HashMap<>();
		queries.put(CommonConstants.USER_COLUMN, user);
		queries.put(CommonConstants.BRANCH_ID_COLUMN, branchId);
		List<UserProfile> userProfiles = userProfileDao.findByKeyValue(UserProfile.class, queries);
		UserProfile userProfile;
		if (userProfiles == null || userProfiles.isEmpty()) {
			// Create a new entry in UserProfile to map user to the branch.
			userProfile = createUserProfile(user, user.getCompany(), user.getEmailId(), CommonConstants.DEFAULT_AGENT_ID, branchId,
					CommonConstants.DEFAULT_REGION_ID, CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID, CommonConstants.DASHBOARD_STAGE,
					CommonConstants.STATUS_INACTIVE, String.valueOf(admin.getUserId()), String.valueOf(admin.getUserId()));
		}
		else {
			userProfile = userProfiles.get(CommonConstants.INITIAL_INDEX);
			if (userProfile.getStatus() == CommonConstants.STATUS_INACTIVE) {
				userProfile.setStatus(CommonConstants.STATUS_ACTIVE);
				userProfile.setModifiedBy(String.valueOf(admin.getUserId()));
				userProfile.setModifiedOn(new Timestamp(System.currentTimeMillis()));
			}
		}
		userProfileDao.saveOrUpdate(userProfile);
		LOG.info("Method to assign user to a branch finished for user : " + admin.getUserId());
	}

	/**
	 * Method to generate and send verification link
	 * 
	 * @param user
	 * @throws InvalidInputException
	 * @throws UndeliveredEmailException
	 */
	private void sendVerificationLink(User user) throws InvalidInputException, UndeliveredEmailException {
		LOG.debug("Method sendVerificationLink of Registration service called");
		String verificationUrl = null;
		try {

			Map<String, String> params = new HashMap<String, String>();
			params.put(CommonConstants.EMAIL_ID, user.getEmailId());
			params.put(CommonConstants.USER_ID, String.valueOf(user.getUserId()));

			LOG.debug("Calling url generator to generate verification link");
			verificationUrl = urlGenerator.generateUrl(params, applicationBaseUrl + CommonConstants.REQUEST_MAPPING_MAIL_VERIFICATION);
		}
		catch (InvalidInputException e) {
			throw new InvalidInputException("Could not generate url for verification.Reason : " + e.getMessage(), e);
		}

		try {
			LOG.debug("Calling email services to send verification mail for user " + user.getEmailId());
			emailServices.sendVerificationMail(verificationUrl, user.getEmailId(), user.getFirstName());
		}
		catch (InvalidInputException e) {
			throw new InvalidInputException("Could not send mail for verification.Reason : " + e.getMessage(), e);
		}
		catch (UndeliveredEmailException e) {
			throw new UndeliveredEmailException("Could not send mail for verification.Reason : " + e.getMessage(), e);
		}

		LOG.debug("Method sendVerificationLink of Registration service finished");
	}

	// JIRA: SS-27: By RM05: EOC
	/**
	 * Method to invite user for registration,includes storing invite in db, calling services to
	 * send mail
	 * 
	 * @param url
	 * @param emailId
	 * @param firstName
	 * @param lastName
	 * @throws UserAlreadyExistsException
	 * @throws InvalidInputException
	 * @throws UndeliveredEmailException
	 * @throws NonFatalException
	 */
	private void inviteUser(String url, String emailId, String firstName, String lastName) throws UserAlreadyExistsException, InvalidInputException,
			UndeliveredEmailException {
		LOG.debug("Method inviteUser called with url : " + url + " emailId : " + emailId + " firstname : " + firstName + " lastName : " + lastName);

		String queryParam = extractUrlQueryParam(url);
		if (doesUserWithEmailIdExists(emailId)) {
			throw new UserAlreadyExistsException("user with specified email id already exists");
		}

		LOG.debug("Calling method to store the registration invite");
		storeCompanyAdminInvitation(queryParam, emailId);

		LOG.debug("Calling email services to send registration invitation mail");
		emailServices.sendRegistrationInviteMail(url, emailId, firstName, lastName);

		LOG.debug("Method inviteUser finished successfully");

	}

	/**
	 * Method to extract the query parameter from encrypted url
	 * 
	 * @param url
	 * @return queryParam
	 * @throws InvalidInputException
	 */
	private String extractUrlQueryParam(String url) throws InvalidInputException {

		if (url == null || url.isEmpty()) {
			throw new InvalidInputException("Url is found null or empty while extracting the query param");
		}
		LOG.debug("Getting query param from the encrypted url " + url);
		String queryParam = url.substring(url.indexOf("q=") + 2, url.length());

		LOG.debug("Returning query param : " + queryParam);
		return queryParam;

	}

	/**
	 * Method to store a registration invite, inserts a new invite if it doesn't exist otherwise
	 * updates it with new timestamp values
	 * 
	 * @param queryParam
	 * @param emailId
	 * @throws NonFatalException
	 */
	private void storeCompanyAdminInvitation(String queryParam, String emailId) {
		LOG.debug("Method storeInvitation called with query param : " + queryParam + " and emailId : " + emailId);
		UserInvite userInvite = null;
		Company company = companyDao.findById(Company.class, CommonConstants.DEFAULT_COMPANY_ID);
		ProfilesMaster profilesMaster = profilesMasterDao.findById(ProfilesMaster.class, CommonConstants.PROFILES_MASTER_NO_PROFILE_ID);

		LOG.debug("Checking if an invite already exists for queryParam :" + queryParam);
		userInvite = checkExistingInviteWithSameParams(queryParam);
		/**
		 * if invite doesn't exist create a new one and store itF
		 */
		if (userInvite == null) {
			userInvite = new UserInvite();
			userInvite.setCompany(company);
			userInvite.setProfilesMaster(profilesMaster);
			userInvite.setInvitationEmailId(emailId);
			userInvite.setInvitationParameters(queryParam);
			userInvite.setInvitationTime(new Timestamp(System.currentTimeMillis()));
			userInvite.setInvitationValidUntil(new Timestamp(CommonConstants.EPOCH_TIME_IN_MILLIS));
			userInvite.setStatus(CommonConstants.STATUS_ACTIVE);
			userInvite.setModifiedBy(CommonConstants.GUEST_USER_NAME);
			userInvite.setModifiedOn(new Timestamp(System.currentTimeMillis()));
			userInvite.setCreatedBy(CommonConstants.GUEST_USER_NAME);
			userInvite.setCreatedOn(new Timestamp(System.currentTimeMillis()));

			LOG.debug("Inserting user invite");
			userInvite = userInviteDao.save(userInvite);
		}
		/**
		 * else update the timestamp of existing invite
		 */
		else {
			userInvite.setStatus(CommonConstants.STATUS_ACTIVE);
			userInvite.setModifiedBy(CommonConstants.GUEST_USER_NAME);
			userInvite.setModifiedOn(new Timestamp(System.currentTimeMillis()));
			userInvite.setInvitationTime(new Timestamp(System.currentTimeMillis()));
			userInvite.setInvitationValidUntil(new Timestamp(CommonConstants.EPOCH_TIME_IN_MILLIS));

			LOG.debug("Updating user invite");
			userInviteDao.update(userInvite);
		}

		LOG.debug("Method storeInvitation finished");
	}

	// JIRA: SS-27: By RM05: BOC
	/*
	 * This method takes the URL query parameter sent for authentication to a new user. Validates
	 * the Registration URL and returns true if matches. Throws InvalidInputException, if URL does
	 * not match.
	 */
	private boolean validateCompanyRegistrationUrlParameters(String encryptedUrlParameter) throws InvalidInputException {
		LOG.debug("Method validateUrlParameters called.");
		List<UserInvite> userInvites = userInviteDao.findByUrlParameter(encryptedUrlParameter);
		if (userInvites == null || userInvites.isEmpty()) {
			LOG.error("Exception caught while validating company registration URL parameters.");
			throw new InvalidInputException("URL parameter provided is inappropriate.");
		}
		LOG.debug("Method validateUrlParameters finished.");
		return true;
	}

	/**
	 * Method to create a new user
	 * 
	 * @param company
	 * @param password
	 * @param emailId
	 * @param displayName
	 * @return
	 */
	private User createUser(Company company, String password, String emailId, String firstName, String lastName, int status) {
		LOG.debug("Method createUser called for email-id : " + emailId + " and status : " + status);
		User user = new User();
		user.setCompany(company);
		user.setLoginName(emailId);
		user.setLoginPassword(password);
		user.setEmailId(emailId);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setSource(CommonConstants.DEFAULT_SOURCE_APPLICATION);
		user.setIsAtleastOneUserprofileComplete(CommonConstants.STATUS_ACTIVE);
		user.setStatus(status);
		Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
		user.setCreatedOn(currentTimestamp);
		user.setModifiedOn(currentTimestamp);
		user.setCreatedBy(CommonConstants.ADMIN_USER_NAME);
		user.setModifiedBy(CommonConstants.ADMIN_USER_NAME);
		LOG.debug("Method createUser finished for email-id : " + emailId);
		return user;
	}

	/**
	 * Method to check if an invite already exists for the same query parameters, if yes returns the
	 * invite
	 * 
	 * @param queryParam
	 * @return
	 */
	private UserInvite checkExistingInviteWithSameParams(String queryParam) {
		LOG.debug("Method checkExistingInviteWithSameParams started for queryparam : " + queryParam);
		List<UserInvite> userinvites = userInviteDao.findByUrlParameter(queryParam);
		UserInvite userInvite = null;
		if (userinvites != null && !userinvites.isEmpty()) {
			userInvite = userinvites.get(0);
		}
		LOG.debug("Method checkExistingInviteWithSameParams finished.Returning : " + userInvite);
		return userInvite;
	}

	/**
	 * Method to invalidate the registration invite link based on emailIds
	 * 
	 * @param emailId
	 * @throws InvalidInputException
	 */
	private void invalidateRegistrationInvite(String emailId) throws InvalidInputException {
		if (emailId == null || emailId.isEmpty()) {
			throw new InvalidInputException("Email id is null for invalidating registration invite");
		}
		LOG.debug("Method to invalidate registration invite called for emailId : " + emailId);
		List<UserInvite> userInvites = userInviteDao.findByColumn(UserInvite.class, CommonConstants.INVITATION_EMAIL_ID_COLUMN, emailId);
		if (userInvites != null && !userInvites.isEmpty()) {
			for (UserInvite userInvite : userInvites) {
				userInvite.setStatus(CommonConstants.STATUS_INACTIVE);
				userInvite.setModifiedBy(CommonConstants.GUEST_USER_NAME);
				userInvite.setModifiedOn(new Timestamp(System.currentTimeMillis()));
				userInviteDao.update(userInvite);
			}
		}
		else {
			LOG.debug("Registration invite link to be invalidated is not present");
		}
		LOG.debug("Method to invalidate registration invite finished for emailId : " + emailId);

	}

	/**
	 * Method to check whether a user with selected user name exists
	 * 
	 * @param userName
	 * @return
	 */
	private boolean userExists(String userName) {
		LOG.debug("Method to check if user exists called for username : " + userName);
		try {
			List<User> users = userDao.findByColumn(User.class, CommonConstants.USER_LOGIN_NAME_COLUMN, userName);
			if (!users.isEmpty() && users != null)
				return true;
		}
		catch (DatabaseException databaseException) {
			LOG.error("Exception caught in method userExists() while trying to fetch list of users with same username.", databaseException);
			return false;
		}
		LOG.debug("Method to check if user exists finished for username : " + userName);
		return false;
	}

	/*
	 * Method to tell whether email id is already present in users table.
	 */
	private boolean doesUserWithEmailIdExists(String emailId) {
		LOG.debug("Method isEmailIdAlreadyPresent started.");
		try {
			Map<String, Object> columns = new HashMap<>();
			columns.put("emailId", emailId);
			columns.put(CommonConstants.STATUS_COLUMN, CommonConstants.STATUS_ACTIVE);
			List<User> users = userDao.findByKeyValue(User.class, columns);
			LOG.debug("Method isEmailIdAlreadyPresent finished.");
			if (users != null && !users.isEmpty())
				return true;
		}
		catch (DatabaseException databaseException) {
			LOG.error("Exception caughr while chnecking for email id in USERS.");
			return false;
		}
		return false;
	}

	// JIRA: SS-27: By RM05: EOC
}
