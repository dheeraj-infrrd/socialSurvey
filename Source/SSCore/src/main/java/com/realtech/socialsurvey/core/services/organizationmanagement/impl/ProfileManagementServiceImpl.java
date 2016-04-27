package com.realtech.socialsurvey.core.services.organizationmanagement.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.UnavailableException;

import com.realtech.socialsurvey.core.services.batchtracker.BatchTrackerService;
import com.realtech.socialsurvey.core.services.upload.FileUploadService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.realtech.socialsurvey.core.commons.CommonConstants;
import com.realtech.socialsurvey.core.commons.Utils;
import com.realtech.socialsurvey.core.dao.BranchDao;
import com.realtech.socialsurvey.core.dao.ExternalApiCallDetailsDao;
import com.realtech.socialsurvey.core.dao.GenericDao;
import com.realtech.socialsurvey.core.dao.OrganizationUnitSettingsDao;
import com.realtech.socialsurvey.core.dao.SocialPostDao;
import com.realtech.socialsurvey.core.dao.SurveyDetailsDao;
import com.realtech.socialsurvey.core.dao.SurveyPreInitiationDao;
import com.realtech.socialsurvey.core.dao.UserDao;
import com.realtech.socialsurvey.core.dao.UserProfileDao;
import com.realtech.socialsurvey.core.dao.ZillowHierarchyDao;
import com.realtech.socialsurvey.core.dao.ZillowTempPostDao;
import com.realtech.socialsurvey.core.dao.impl.MongoOrganizationUnitSettingDaoImpl;
import com.realtech.socialsurvey.core.entities.Achievement;
import com.realtech.socialsurvey.core.entities.AgentRankingReport;
import com.realtech.socialsurvey.core.entities.AgentSettings;
import com.realtech.socialsurvey.core.entities.Association;
import com.realtech.socialsurvey.core.entities.Branch;
import com.realtech.socialsurvey.core.entities.BreadCrumb;
import com.realtech.socialsurvey.core.entities.Company;
import com.realtech.socialsurvey.core.entities.CompanyPositions;
import com.realtech.socialsurvey.core.entities.CompanyProfileData;
import com.realtech.socialsurvey.core.entities.ContactDetailsSettings;
import com.realtech.socialsurvey.core.entities.ContactNumberSettings;
import com.realtech.socialsurvey.core.entities.ExternalAPICallDetails;
import com.realtech.socialsurvey.core.entities.FacebookToken;
import com.realtech.socialsurvey.core.entities.GoogleToken;
import com.realtech.socialsurvey.core.entities.LendingTreeToken;
import com.realtech.socialsurvey.core.entities.Licenses;
import com.realtech.socialsurvey.core.entities.LinkedInProfileData;
import com.realtech.socialsurvey.core.entities.LinkedInToken;
import com.realtech.socialsurvey.core.entities.LockSettings;
import com.realtech.socialsurvey.core.entities.MailContent;
import com.realtech.socialsurvey.core.entities.MailContentSettings;
import com.realtech.socialsurvey.core.entities.MailIdSettings;
import com.realtech.socialsurvey.core.entities.MiscValues;
import com.realtech.socialsurvey.core.entities.OrganizationUnitSettings;
import com.realtech.socialsurvey.core.entities.ProfileStage;
import com.realtech.socialsurvey.core.entities.RealtorToken;
import com.realtech.socialsurvey.core.entities.Region;
import com.realtech.socialsurvey.core.entities.SettingsDetails;
import com.realtech.socialsurvey.core.entities.SocialMediaTokens;
import com.realtech.socialsurvey.core.entities.SocialPost;
import com.realtech.socialsurvey.core.entities.SocialProfileToken;
import com.realtech.socialsurvey.core.entities.SurveyDetails;
import com.realtech.socialsurvey.core.entities.SurveyPreInitiation;
import com.realtech.socialsurvey.core.entities.TwitterToken;
import com.realtech.socialsurvey.core.entities.User;
import com.realtech.socialsurvey.core.entities.UserCompositeEntity;
import com.realtech.socialsurvey.core.entities.UserFromSearch;
import com.realtech.socialsurvey.core.entities.UserProfile;
import com.realtech.socialsurvey.core.entities.UserSettings;
import com.realtech.socialsurvey.core.entities.VerticalsMaster;
import com.realtech.socialsurvey.core.entities.WebAddressSettings;
import com.realtech.socialsurvey.core.entities.YelpToken;
import com.realtech.socialsurvey.core.entities.ZillowTempPost;
import com.realtech.socialsurvey.core.entities.ZillowToken;
import com.realtech.socialsurvey.core.enums.AccountType;
import com.realtech.socialsurvey.core.enums.OrganizationUnit;
import com.realtech.socialsurvey.core.enums.SettingsForApplication;
import com.realtech.socialsurvey.core.exception.InvalidInputException;
import com.realtech.socialsurvey.core.exception.NoRecordsFetchedException;
import com.realtech.socialsurvey.core.exception.NonFatalException;
import com.realtech.socialsurvey.core.integration.zillow.ZillowIntegrationApi;
import com.realtech.socialsurvey.core.integration.zillow.ZillowIntergrationApiBuilder;
import com.realtech.socialsurvey.core.services.generator.URLGenerator;
import com.realtech.socialsurvey.core.services.mail.EmailServices;
import com.realtech.socialsurvey.core.services.mail.UndeliveredEmailException;
import com.realtech.socialsurvey.core.services.organizationmanagement.OrganizationManagementService;
import com.realtech.socialsurvey.core.services.organizationmanagement.ProfileManagementService;
import com.realtech.socialsurvey.core.services.organizationmanagement.ProfileNotFoundException;
import com.realtech.socialsurvey.core.services.organizationmanagement.UserManagementService;
import com.realtech.socialsurvey.core.services.search.SolrSearchService;
import com.realtech.socialsurvey.core.services.search.exception.SolrException;
import com.realtech.socialsurvey.core.services.settingsmanagement.SettingsLocker;
import com.realtech.socialsurvey.core.services.settingsmanagement.SettingsManager;
import com.realtech.socialsurvey.core.services.settingsmanagement.SettingsSetter;
import com.realtech.socialsurvey.core.services.settingsmanagement.impl.InvalidSettingsStateException;
import com.realtech.socialsurvey.core.services.social.SocialManagementService;
import com.realtech.socialsurvey.core.services.surveybuilder.SurveyHandler;
import com.realtech.socialsurvey.core.utils.DisplayMessageConstants;
import com.realtech.socialsurvey.core.utils.EmailFormatHelper;
import com.realtech.socialsurvey.core.utils.UrlValidationHelper;


@DependsOn ( "generic")
@Component
public class ProfileManagementServiceImpl implements ProfileManagementService, InitializingBean
{

    private static final Logger LOG = LoggerFactory.getLogger( ProfileManagementServiceImpl.class );

    // Zillow JSON response map key
    public static final String ZILLOW_JSON_CODE_KEY = "code";
    public static final String ZILLOW_JSON_TEXT_KEY = "text";

    // Zillow JSON response map error text prefix
    public static final String ZILLOW_JSON_ERROR_TEXT_PREFIX = "Error";

    @Autowired
    private OrganizationUnitSettingsDao organizationUnitSettingsDao;

    @Autowired
    private OrganizationManagementService organizationManagementService;

    @Autowired
    private BatchTrackerService batchTrackerService;

    @Autowired
    private SettingsSetter settingsSetter;

    @Autowired
    private UserProfileDao userProfileDao;

    @Autowired
    private GenericDao<Company, Long> companyDao;

    @Autowired
    private GenericDao<Region, Long> regionDao;

    @Resource
    @Qualifier ( "branch")
    private BranchDao branchDao;

    @Resource
    @Qualifier ( "user")
    private UserDao usersDao;

    @Autowired
    private GenericDao<User, Long> userDao;

    @Autowired
    private GenericDao<VerticalsMaster, Long> verticalsMasterDao;

    @Autowired
    private SurveyDetailsDao surveyDetailsDao;

    @Autowired
    private SocialPostDao socialPostDao;

    @Autowired
    private Utils utils;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private SocialManagementService socialManagementService;
    @Autowired
    private SolrSearchService solrSearchService;

    @Autowired
    private SurveyPreInitiationDao surveyPreInitiationDao;

    @Autowired
    private EmailServices emailServices;

    @Autowired
    private URLGenerator urlGenerator;

    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private SettingsLocker settingsLocker;

    @Autowired
    private UrlValidationHelper urlValidationHelper;

    @Autowired
    private ZillowIntergrationApiBuilder zillowIntegrationApiBuilder;

    @Autowired
    private SurveyHandler surveyHandler;

    @Value ( "${ZILLOW_WEBSERVICE_ID}")
    private String zwsId;

    @Value ( "${APPLICATION_BASE_URL}")
    private String applicationBaseUrl;

    @Value ( "${FB_CLIENT_ID}")
    private String facebookAppId;

    @Value ( "${GOOGLE_API_KEY}")
    private String googlePlusId;

    @Value ( "${APPLICATION_ADMIN_NAME}")
    private String applicationAdminName;

    @Value ( "${APPLICATION_ADMIN_EMAIL}")
    private String applicationAdminEmail;

    @Autowired
    private EmailFormatHelper emailFormatHelper;

    //    @Autowired
    //    private ZillowUpdateService zillowUpdateService;

    @Autowired
    private ZillowHierarchyDao zillowHierarchyDao;

    @Value ( "${PARAM_ORDER_TAKE_SURVEY}")
    String paramOrderTakeSurvey;
    @Value ( "${PARAM_ORDER_TAKE_SURVEY_CUSTOMER}")
    String paramOrderTakeSurveyCustomer;
    @Value ( "${PARAM_ORDER_TAKE_SURVEY_REMINDER}")
    String paramOrderTakeSurveyReminder;

    @Value ( "${ZILLOW_ENDPOINT}")
    private String zillowEndpoint;

    @Value( "${AMAZON_IMAGE_BUCKET}" )
    private String amazonImageBucket;

    @Value( "${CDN_PATH}" )
    private String cdnUrl;

    @Autowired
    private ExternalApiCallDetailsDao externalApiCallDetailsDao;

    @Autowired
    private ZillowTempPostDao zillowTempPostDao;


    @Override
    public void afterPropertiesSet() throws Exception
    {
        LOG.info( "afterPropertiesSet called for profile management service" );
    }


    @Override
    public LockSettings aggregateParentLockSettings( User user, AccountType accountType, UserSettings settings, long branchId,
        long regionId, int profilesMaster ) throws InvalidInputException, NoRecordsFetchedException
    {
        LOG.info( "Method aggregateParentLockSettings() called from ProfileManagementService" );
        if ( user == null ) {
            throw new InvalidInputException( "User is not set." );
        }
        if ( settings == null ) {
            throw new InvalidInputException( "Invalid user settings." );
        }
        if ( accountType == null ) {
            throw new InvalidInputException( "Invalid account type." );
        }

        LockSettings parentLockSettings = null;
        // If user is Company Admin, Lock settings would be default
        if ( profilesMaster == CommonConstants.PROFILES_MASTER_COMPANY_ADMIN_PROFILE_ID ) {
            LOG.debug( "Setting default LockSettings for Company Admin" );
            parentLockSettings = new LockSettings();
        }

        // If user is not Company Admin, Lock settings need to be aggregated
        else {
            OrganizationUnitSettings branchSettings = null;
            OrganizationUnitSettings regionSettings = null;
            switch ( accountType ) {
                case FREE:
                case INDIVIDUAL:
                case TEAM:
                    // Individual
                    if ( profilesMaster == CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID ) {
                        LOG.debug( "Setting company LockSettings for Agent of Individual/Team account type" );
                        parentLockSettings = settings.getCompanySettings().getLockSettings();
                    }
                    break;

                case COMPANY:
                    // Branch Admin
                    if ( profilesMaster == CommonConstants.PROFILES_MASTER_BRANCH_ADMIN_PROFILE_ID ) {
                        LOG.debug( "Setting company LockSettings for Branch Admin of Company account type" );
                        parentLockSettings = settings.getCompanySettings().getLockSettings();
                    }

                    // Individual
                    else if ( profilesMaster == CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID ) {
                        LOG.debug( "Aggregating LockSettings till Branch for Agent of Company account type" );
                        branchSettings = organizationManagementService.getBranchSettingsDefault( branchId );
                        parentLockSettings = lockSettingsTillBranch( settings.getCompanySettings(), null, branchSettings );
                    }
                    break;

                case ENTERPRISE:
                    // Region Admin
                    if ( profilesMaster == CommonConstants.PROFILES_MASTER_REGION_ADMIN_PROFILE_ID ) {
                        LOG.debug( "Setting company LockSettings for Region Admin of Enterprise account type" );
                        parentLockSettings = settings.getCompanySettings().getLockSettings();
                    }

                    // Branch Admin
                    else if ( profilesMaster == CommonConstants.PROFILES_MASTER_BRANCH_ADMIN_PROFILE_ID ) {
                        LOG.debug( "Aggregating LockSettings till Region for Branch Admin of Enterprise account type" );
                        if ( branchId > 0l ) {
                            branchSettings = organizationManagementService.getBranchSettingsDefault( branchId );

                            Branch branch = branchDao.findById( Branch.class, branchId );
                            regionId = branch.getRegion().getRegionId();
                        }
                        if ( regionId > 0l ) {
                            regionSettings = organizationManagementService.getRegionSettings( regionId );
                        }
                        parentLockSettings = lockSettingsTillRegion( settings.getCompanySettings(), regionSettings );
                    }

                    // Individual
                    else if ( profilesMaster == CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID ) {
                        LOG.debug( "Aggregating LockSettings till Branch for Agent of Enterprise account type" );
                        if ( branchId > 0l ) {
                            branchSettings = organizationManagementService.getBranchSettingsDefault( branchId );

                            Branch branch = branchDao.findById( Branch.class, branchId );
                            regionId = branch.getRegion().getRegionId();
                        }
                        if ( regionId > 0l ) {
                            regionSettings = organizationManagementService.getRegionSettings( regionId );
                        }
                        parentLockSettings = lockSettingsTillBranch( settings.getCompanySettings(), regionSettings,
                            branchSettings );
                    }
                    break;

                default:
                    throw new InvalidInputException( "Account type is invalid in aggregateParentLockSettings" );
            }
        }
        LOG.info( "Method aggregateParentLockSettings() finished from ProfileManagementService" );
        return parentLockSettings;
    }


    LockSettings lockSettingsTillRegion( OrganizationUnitSettings companySettings, OrganizationUnitSettings regionSettings )
        throws InvalidInputException
    {
        LOG.debug( "Method lockSettingsTillRegion() called from ProfileManagementService" );
        if ( companySettings == null ) {
            throw new InvalidInputException( "No Settings found" );
        }

        // Fetching Company Lock settings
        LockSettings parentLock = new LockSettings();
        parentLock = aggregateLockSettings( companySettings.getLockSettings(), parentLock );

        // Aggregate Region Lock settings if exists
        if ( regionSettings != null ) {
            parentLock = aggregateLockSettings( regionSettings.getLockSettings(), parentLock );
        }
        LOG.debug( "Method lockSettingsTillRegion() finished from ProfileManagementService" );
        return parentLock;
    }


    LockSettings lockSettingsTillBranch( OrganizationUnitSettings companySettings, OrganizationUnitSettings regionSettings,
        OrganizationUnitSettings branchSettings ) throws InvalidInputException
    {
        LOG.debug( "Method lockSettingsTillBranch() called from ProfileManagementService" );
        if ( companySettings == null ) {
            throw new InvalidInputException( "No Settings found" );
        }

        // Fetching Company Lock settings
        LockSettings parentLock = new LockSettings();
        parentLock = aggregateLockSettings( companySettings.getLockSettings(), parentLock );

        // Aggregate Region Lock settings if exists
        if ( regionSettings != null ) {
            parentLock = aggregateLockSettings( regionSettings.getLockSettings(), parentLock );
        }

        // Aggregate Branch Lock settings if exists
        if ( branchSettings != null ) {
            parentLock = aggregateLockSettings( branchSettings.getLockSettings(), parentLock );
        }
        LOG.debug( "Method lockSettingsTillBranch() finished from ProfileManagementService" );
        return parentLock;
    }


    LockSettings aggregateLockSettings( LockSettings higherLock, LockSettings parentLock )
    {
        LOG.debug( "Method aggregateLockSettings() called from ProfileManagementService" );

        // Aggregate parentLockSettings with higherLockSettings
        if ( higherLock != null ) {
            if ( higherLock.getIsLogoLocked() ) {
                parentLock.setLogoLocked( true );
            }
            if ( higherLock.getIsWebAddressLocked() ) {
                parentLock.setWebAddressLocked( true );
            }
            if ( higherLock.getIsBlogAddressLocked() ) {
                parentLock.setBlogAddressLocked( true );
            }
            if ( higherLock.getIsWorkPhoneLocked() ) {
                parentLock.setWorkPhoneLocked( true );
            }
            if ( higherLock.getIsPersonalPhoneLocked() ) {
                parentLock.setPersonalPhoneLocked( true );
            }
            if ( higherLock.getIsFaxPhoneLocked() ) {
                parentLock.setFaxPhoneLocked( true );
            }
            if ( higherLock.getIsAboutMeLocked() ) {
                parentLock.setAboutMeLocked( true );
            }
        }
        LOG.debug( "Method aggregateLockSettings() finished from ProfileManagementService" );
        return parentLock;
    }


    @Override
    public OrganizationUnitSettings aggregateUserProfile( User user, AccountType accountType, UserSettings settings,
        long branchId, long regionId, int profilesMaster ) throws InvalidInputException, NoRecordsFetchedException
    {
        LOG.info( "Method aggregateUserProfile() called from ProfileManagementService" );
        if ( user == null ) {
            throw new InvalidInputException( "User is not set." );
        }
        if ( settings == null ) {
            throw new InvalidInputException( "Invalid user settings." );
        }
        if ( accountType == null ) {
            throw new InvalidInputException( "Invalid account type." );
        }

        OrganizationUnitSettings userProfile = null;
        // If user is Company Admin, returning CompanyAdmin Profile
        if ( profilesMaster == CommonConstants.PROFILES_MASTER_COMPANY_ADMIN_PROFILE_ID ) {
            LOG.debug( "Setting Company Profile for Company Admin" );
            userProfile = settings.getCompanySettings();
        }

        // If user is not Company Admin, Profile need to be aggregated
        else {
            OrganizationUnitSettings branchSettings = null;
            OrganizationUnitSettings regionSettings = null;
            switch ( accountType ) {
                case FREE:
                case INDIVIDUAL:
                case TEAM:
                    // Individual
                    if ( profilesMaster == CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID ) {
                        LOG.debug( "Aggregate Profile for Agent of Individual/Team account type" );
                        userProfile = aggregateAgentProfile( settings.getCompanySettings(), null, null,
                            settings.getAgentSettings() );
                    }
                    break;

                case COMPANY:
                    LOG.info( "Company account type" );
                    branchSettings = organizationManagementService.getBranchSettingsDefault( branchId );

                    // Branch Admin
                    if ( profilesMaster == CommonConstants.PROFILES_MASTER_BRANCH_ADMIN_PROFILE_ID ) {
                        LOG.debug( "Aggregate Profile for BranchAdmin of Company account type" );
                        userProfile = aggregateBranchProfile( settings.getCompanySettings(), null, branchSettings );
                    }

                    // Individual
                    else if ( profilesMaster == CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID ) {
                        LOG.debug( "Aggregate Profile for Agent of Company account type" );
                        userProfile = aggregateAgentProfile( settings.getCompanySettings(), null, branchSettings,
                            settings.getAgentSettings() );
                    }
                    break;

                case ENTERPRISE:
                    LOG.info( "Enterprise account type" );
                    if ( branchId > 0l ) {
                        branchSettings = organizationManagementService.getBranchSettingsDefault( branchId );

                        Branch branch = branchDao.findById( Branch.class, branchId );
                        regionId = branch.getRegion().getRegionId();
                    }
                    if ( regionId > 0l ) {
                        regionSettings = organizationManagementService.getRegionSettings( regionId );
                    }

                    // Region Admin
                    if ( profilesMaster == CommonConstants.PROFILES_MASTER_REGION_ADMIN_PROFILE_ID ) {
                        LOG.debug( "Aggregate Profile for RegionAdmin of Enterprise account type" );
                        userProfile = aggregateRegionProfile( settings.getCompanySettings(), regionSettings );
                    }

                    // Branch Admin
                    else if ( profilesMaster == CommonConstants.PROFILES_MASTER_BRANCH_ADMIN_PROFILE_ID ) {
                        LOG.debug( "Aggregate Profile for BranchAdmin of Enterprise account type" );
                        userProfile = aggregateBranchProfile( settings.getCompanySettings(), regionSettings, branchSettings );
                    }

                    // Individual
                    else if ( profilesMaster == CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID ) {
                        LOG.debug( "Aggregate Profile for Agent of Enterprise account type" );
                        userProfile = aggregateAgentProfile( settings.getCompanySettings(), regionSettings, branchSettings,
                            settings.getAgentSettings() );
                    }
                    break;

                default:
                    throw new InvalidInputException( "Account type is invalid in aggregateUserProfile" );
            }
        }
        LOG.info( "Method aggregateUserProfile() finished from ProfileManagementService" );
        return userProfile;
    }


    OrganizationUnitSettings aggregateRegionProfile( OrganizationUnitSettings companySettings,
        OrganizationUnitSettings regionSettings ) throws InvalidInputException
    {
        LOG.debug( "Method aggregateRegionProfile() called from ProfileManagementService" );
        if ( companySettings == null || regionSettings == null ) {
            throw new InvalidInputException( "No Settings found" );
        }

        // Aggregate Company Profile settings
        LockSettings userLock = new LockSettings();
        regionSettings = aggregateProfileData( companySettings, regionSettings, userLock );

        // Aggregate Region Profile Settings
        // to reflect lockSettings of Region
        regionSettings = aggregateProfileData( regionSettings, regionSettings, userLock );
        regionSettings.setLockSettings( userLock );

        LOG.debug( "Method aggregateRegionProfile() finished from ProfileManagementService" );
        return regionSettings;
    }


    OrganizationUnitSettings aggregateBranchProfile( OrganizationUnitSettings companySettings,
        OrganizationUnitSettings regionSettings, OrganizationUnitSettings branchSettings ) throws InvalidInputException
    {
        LOG.debug( "Method aggregateBranchProfile() called from ProfileManagementService" );
        if ( companySettings == null || branchSettings == null ) {
            throw new InvalidInputException( "No Settings found" );
        }

        // Aggregate Company Profile settings
        LockSettings userLock = new LockSettings();
        branchSettings = aggregateProfileData( companySettings, branchSettings, userLock );

        // Aggregate Region Profile settings if exists
        if ( regionSettings != null ) {
            branchSettings = aggregateProfileData( regionSettings, branchSettings, userLock );
        }

        // Aggregate Branch Profile Settings
        // to reflect lockSettings of Branch
        branchSettings = aggregateProfileData( branchSettings, branchSettings, userLock );
        branchSettings.setLockSettings( userLock );

        LOG.debug( "Method aggregateBranchProfile() finished from ProfileManagementService" );
        return branchSettings;
    }


    OrganizationUnitSettings aggregateAgentProfile( OrganizationUnitSettings companySettings,
        OrganizationUnitSettings regionSettings, OrganizationUnitSettings branchSettings, OrganizationUnitSettings agentSettings )
        throws InvalidInputException
    {
        LOG.debug( "Method aggregateAgentProfile() called from ProfileManagementService" );
        if ( companySettings == null || agentSettings == null ) {
            throw new InvalidInputException( "No Settings found" );
        }

        // Aggregate Company Profile settings
        LockSettings userLock = new LockSettings();
        agentSettings = (AgentSettings) aggregateProfileData( companySettings, agentSettings, userLock );

        AgentSettings agentSettingsType = null;
        if ( agentSettings instanceof AgentSettings ) {
            agentSettingsType = (AgentSettings) agentSettings;
            // sort the company positions. since we are type casting the settings here, we are
            // sorting the same here
            sortCompanyPositions( agentSettingsType.getPositions() );
        }

        // Aggregate Region Profile settings if exists
        if ( regionSettings != null ) {
            agentSettings = aggregateProfileData( regionSettings, agentSettings, userLock );
        }

        // Aggregate Branch Profile settings if exists
        if ( branchSettings != null ) {
            agentSettings = aggregateProfileData( branchSettings, agentSettings, userLock );
        }

        // No Aggregation needed Agent Profile Settings
        // manully setting since agent do not have lockSettings
        agentSettings.setLockSettings( userLock );

        LOG.debug( "Method aggregateAgentProfile() finished from ProfileManagementService" );
        return ( agentSettingsType != null ? agentSettingsType : agentSettings );
    }


    OrganizationUnitSettings aggregateProfileData( OrganizationUnitSettings parentProfile,
        OrganizationUnitSettings userProfile, LockSettings userLock )
    {
        LOG.debug( "Method aggregateProfileData() called from ProfileManagementService" );

        if ( userProfile.getContact_details() == null ) {
            userProfile.setContact_details( new ContactDetailsSettings() );
        }
        if ( userProfile.getContact_details().getWeb_addresses() == null ) {
            userProfile.getContact_details().setWeb_addresses( new WebAddressSettings() );
        }
        if ( userProfile.getContact_details().getContact_numbers() == null ) {
            userProfile.getContact_details().setContact_numbers( new ContactNumberSettings() );
        }
        if ( userProfile.getSurvey_settings() == null ) {
            userProfile.setSurvey_settings( parentProfile.getSurvey_settings() );
        }

        // Aggregate parentProfile data with userProfile
        LockSettings parentLock = parentProfile.getLockSettings();
        if ( parentLock != null ) {
            // Logo
            //JIRA SS-1363 begin
            /*if ( parentProfile.getLogoThumbnail() != null ) {
                if ( parentLock.getIsLogoLocked() && !userLock.getIsLogoLocked() ) {
                    userProfile.setLogo( parentProfile.getLogoThumbnail() );
                    userLock.setLogoLocked( true );
                }
                if ( !parentLock.getIsLogoLocked() && !userLock.getIsLogoLocked() ) {
                    if ( userProfile.getLogoThumbnail() == null || userProfile.getLogoThumbnail().equals( "" ) ) {
                        userProfile.setLogo( parentProfile.getLogoThumbnail() );
                    }
                }
            }*/
            if ( parentProfile.getLogo() != null ) {
                if ( parentLock.getIsLogoLocked() && !userLock.getIsLogoLocked() ) {
                    userProfile.setLogo( parentProfile.getLogo() );
                    userLock.setLogoLocked( true );
                }
                if ( !parentLock.getIsLogoLocked() && !userLock.getIsLogoLocked() ) {
                    if ( userProfile.getLogo() == null || userProfile.getLogo().equals( "" ) ) {
                        userProfile.setLogo( parentProfile.getLogo() );
                    }
                }
            }
            //JIRA SS-1363 end

            // Basic Contact details
            if ( parentProfile.getContact_details() != null ) {
                if ( parentLock.getIsAboutMeLocked() && !userLock.getIsAboutMeLocked()
                    && parentProfile.getContact_details().getAbout_me() != null ) {
                    userProfile.getContact_details().setAbout_me( parentProfile.getContact_details().getAbout_me() );
                    userLock.setAboutMeLocked( true );
                }
            }

            // Web addresses
            if ( parentProfile.getContact_details().getWeb_addresses() != null ) {
                if ( parentLock.getIsWebAddressLocked() && !userLock.getIsWebAddressLocked()
                    && userProfile.getContact_details().getWeb_addresses() != null ) {
                    userProfile.getContact_details().getWeb_addresses()
                        .setWork( parentProfile.getContact_details().getWeb_addresses().getWork() );
                    userLock.setWebAddressLocked( true );
                }
                if ( parentLock.getIsBlogAddressLocked() && !userLock.getIsBlogAddressLocked()
                    && userProfile.getContact_details().getWeb_addresses() != null ) {
                    userProfile.getContact_details().getWeb_addresses()
                        .setBlogs( parentProfile.getContact_details().getWeb_addresses().getBlogs() );
                    userLock.setBlogAddressLocked( true );
                }
            }

            // Phone numbers
            if ( parentProfile.getContact_details().getContact_numbers() != null ) {
                if ( parentLock.getIsWorkPhoneLocked() && !userLock.getIsWorkPhoneLocked()
                    && userProfile.getContact_details().getContact_numbers() != null ) {
                    userProfile.getContact_details().getContact_numbers()
                        .setWork( parentProfile.getContact_details().getContact_numbers().getWork() );
                    userLock.setWorkPhoneLocked( true );
                }
                if ( parentLock.getIsPersonalPhoneLocked() && !userLock.getIsPersonalPhoneLocked()
                    && userProfile.getContact_details().getContact_numbers() != null ) {
                    userProfile.getContact_details().getContact_numbers()
                        .setPersonal( parentProfile.getContact_details().getContact_numbers().getPersonal() );
                    userLock.setPersonalPhoneLocked( true );
                }
                if ( parentLock.getIsFaxPhoneLocked() && !userLock.getIsFaxPhoneLocked()
                    && userProfile.getContact_details().getContact_numbers() != null ) {
                    userProfile.getContact_details().getContact_numbers()
                        .setFax( parentProfile.getContact_details().getContact_numbers().getFax() );
                    userLock.setFaxPhoneLocked( true );
                }
            }
        }
        LOG.debug( "Method aggregateProfileData() finished from ProfileManagementService" );
        return userProfile;
    }


    // Logo
    @Override
    public void updateLogo( String collection, OrganizationUnitSettings companySettings, String logo )
        throws InvalidInputException
    {
        if ( logo == null || logo.isEmpty() ) {
            throw new InvalidInputException( "Logo passed can not be null or empty" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( companySettings == null ) {
            throw new InvalidInputException( "Company settings passed can not be null" );
        }
        LOG.info( "Updating logo" );
        /*organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings( MongoOrganizationUnitSettingDaoImpl.KEY_LOGO,
            logo, companySettings, collection );*/
        organizationManagementService.updateImageForOrganizationUnitSetting( companySettings.getIden(), logo, collection,
            CommonConstants.IMAGE_TYPE_LOGO, false, false );
        /*organizationUnitSettingsDao.updateImageForOrganizationUnitSetting( companySettings.getIden(), logo, collection,
            CommonConstants.IMAGE_TYPE_LOGO, false, false );*/
        LOG.info( "Logo updated successfully" );
    }


    // ProfileImage
    @Override
    public void updateProfileImage( String collection, OrganizationUnitSettings companySettings, String image )
        throws InvalidInputException
    {
        if ( image == null || image.isEmpty() ) {
            throw new InvalidInputException( "image passed can not be null or empty" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( companySettings == null ) {
            throw new InvalidInputException( "Company settings passed can not be null" );
        }
        LOG.info( "Updating image" );
        /*organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_PROFILE_IMAGE, image, companySettings, collection );*/
        /*organizationUnitSettingsDao.updateImageForOrganizationUnitSetting( companySettings.getIden(), image, collection,
            CommonConstants.IMAGE_TYPE_PROFILE, false, false );*/
        organizationManagementService.updateImageForOrganizationUnitSetting( companySettings.getIden(), image, collection,
            CommonConstants.IMAGE_TYPE_PROFILE, false, false );
        LOG.info( "Image updated successfully" );
    }


    // vertical
    @Override
    @Transactional
    public void updateVertical( String collection, OrganizationUnitSettings companySettings, String vertical )
        throws InvalidInputException
    {
        if ( vertical == null || vertical.isEmpty() ) {
            throw new InvalidInputException( "vertical passed can not be null or empty" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( companySettings == null ) {
            throw new InvalidInputException( "Company settings passed can not be null" );
        }
        LOG.info( "Updating vertical" );
        if ( collection.equals( MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION ) ) {

            List<VerticalsMaster> verticalsMasters = organizationManagementService.getAllVerticalsMaster();
            VerticalsMaster verticalsMaster = null;

            for ( VerticalsMaster vm : verticalsMasters ) {
                if ( vertical.equals( vm.getVerticalName() ) ) {
                    verticalsMaster = vm;
                    break;
                }
            }

            if ( verticalsMaster == null ) {
                throw new InvalidInputException( "Invalid vertial name passed" );
            }

            Company company = companyDao.findById( Company.class, companySettings.getIden() );
            company.setVerticalsMaster( verticalsMaster );
            companyDao.update( company );
        }
        organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_VERTICAL, vertical, companySettings, collection );
        LOG.info( "vertical updated successfully" );
    }


    // Associations
    @Override
    public List<Association> addAssociations( String collection, OrganizationUnitSettings unitSettings,
        List<Association> associations ) throws InvalidInputException
    {
        if ( associations == null ) {
            throw new InvalidInputException( "Association name passed can not be null" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( unitSettings == null ) {
            throw new InvalidInputException( "Unit settings passed can not be null" );
        }
        LOG.info( "Adding associations" );
        organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_ASSOCIATION, associations, unitSettings,
            MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION );
        LOG.info( "Associations added successfully" );
        return associations;
    }


    @Override
    public List<Association> addAgentAssociations( String collection, AgentSettings agentSettings,
        List<Association> associations ) throws InvalidInputException
    {
        if ( associations == null ) {
            throw new InvalidInputException( "Association name passed can not be null" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( agentSettings == null ) {
            throw new InvalidInputException( "Agent settings passed can not be null" );
        }
        LOG.info( "Adding associations" );
        organizationUnitSettingsDao.updateParticularKeyAgentSettings( MongoOrganizationUnitSettingDaoImpl.KEY_ASSOCIATION,
            associations, agentSettings );
        LOG.info( "Associations added successfully" );
        return associations;
    }


    // Lock Settings
    @Override
    public LockSettings updateLockSettings( String collection, OrganizationUnitSettings unitSettings, LockSettings lockSettings )
        throws InvalidInputException
    {
        if ( lockSettings == null ) {
            throw new InvalidInputException( "LockSettings passed can not be null" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( unitSettings == null ) {
            throw new InvalidInputException( "Unit settings passed can not be null" );
        }
        LOG.info( "Updating lock detail information" );
        organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_LOCK_SETTINGS, lockSettings, unitSettings, collection );
        LOG.info( "lock details updated successfully" );
        return lockSettings;
    }


    // Contact details
    @Override
    public ContactDetailsSettings updateContactDetails( String collection, OrganizationUnitSettings unitSettings,
        ContactDetailsSettings contactDetailsSettings ) throws InvalidInputException
    {
        if ( contactDetailsSettings == null ) {
            throw new InvalidInputException( "Contact details passed can not be null" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( unitSettings == null ) {
            throw new InvalidInputException( "Unit settings passed can not be null" );
        }
        LOG.info( "Updating contact detail information" );
        organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_CONTACT_DETAIL_SETTINGS, contactDetailsSettings, unitSettings, collection );
        // Update the seo content flag to true
        organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_SEO_CONTENT_MODIFIED, true, unitSettings, collection );
        LOG.info( "Contact details updated successfully" );
        return contactDetailsSettings;
    }


    @Override
    public ContactDetailsSettings updateAgentContactDetails( String collection, AgentSettings agentSettings,
        ContactDetailsSettings contactDetailsSettings ) throws InvalidInputException
    {
        if ( contactDetailsSettings == null ) {
            throw new InvalidInputException( "Contact details passed can not be null" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( agentSettings == null ) {
            throw new InvalidInputException( "Agent settings passed can not be null" );
        }
        LOG.info( "Updating contact detail information" );
        organizationUnitSettingsDao.updateParticularKeyAgentSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_CONTACT_DETAIL_SETTINGS, contactDetailsSettings, agentSettings );
        // Update the seo content flag to true
        organizationUnitSettingsDao.updateParticularKeyAgentSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_SEO_CONTENT_MODIFIED, true, agentSettings );
        LOG.info( "Contact details updated successfully" );
        return contactDetailsSettings;
    }


    // Achievements
    @Override
    public List<Achievement> addAchievements( String collection, OrganizationUnitSettings unitSettings,
        List<Achievement> achievements ) throws InvalidInputException
    {
        if ( achievements == null ) {
            throw new InvalidInputException( "Achievements passed can not be null or empty" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( unitSettings == null ) {
            throw new InvalidInputException( "Unit settings passed can not be null" );
        }
        LOG.info( "Adding achievements" );
        organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_ACHIEVEMENTS, achievements, unitSettings, collection );
        LOG.info( "Achievements added successfully" );
        return achievements;
    }


    @Override
    public List<Achievement> addAgentAchievements( String collection, AgentSettings agentSettings,
        List<Achievement> achievements ) throws InvalidInputException
    {
        if ( achievements == null ) {
            throw new InvalidInputException( "Achievements passed can not be null or empty" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( agentSettings == null ) {
            throw new InvalidInputException( "Agent settings passed can not be null" );
        }
        LOG.info( "Adding achievements" );
        organizationUnitSettingsDao.updateParticularKeyAgentSettings( MongoOrganizationUnitSettingDaoImpl.KEY_ACHIEVEMENTS,
            achievements, agentSettings );
        LOG.info( "Achievements added successfully" );
        return achievements;
    }


    // Licenses
    @Override
    public Licenses addLicences( String collection, OrganizationUnitSettings unitSettings, List<String> authorisedIn )
        throws InvalidInputException
    {
        if ( authorisedIn == null ) {
            throw new InvalidInputException( "Authorised In list passed can not be null" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( unitSettings == null ) {
            throw new InvalidInputException( "Unit settings passed can not be null" );
        }

        Licenses licenses = unitSettings.getLicenses();
        if ( licenses == null ) {
            LOG.debug( "Licenses not present for current profile, create a new license object" );
            licenses = new Licenses();
        }
        licenses.setAuthorized_in( authorisedIn );
        LOG.info( "Adding Licences list" );
        organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_LICENCES, licenses, unitSettings, collection );
        LOG.info( "Licence authorisations added successfully" );
        return licenses;
    }


    @Override
    public Licenses addAgentLicences( String collection, AgentSettings agentSettings, List<String> authorisedIn )
        throws InvalidInputException
    {
        if ( authorisedIn == null ) {
            throw new InvalidInputException( "Authorised In list passed can not be null" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( agentSettings == null ) {
            throw new InvalidInputException( "Agent settings passed can not be null" );
        }

        Licenses licenses = agentSettings.getLicenses();
        if ( licenses == null ) {
            LOG.debug( "Licenses not present for current profile, create a new license object" );
            licenses = new Licenses();
        }
        licenses.setAuthorized_in( authorisedIn );
        LOG.info( "Adding Licences list" );
        organizationUnitSettingsDao.updateParticularKeyAgentSettings( MongoOrganizationUnitSettingDaoImpl.KEY_LICENCES,
            licenses, agentSettings );
        LOG.info( "Licence authorisations added successfully" );
        return licenses;
    }


    // Social Tokens
    @Override
    public void updateSocialMediaTokens( String collection, OrganizationUnitSettings unitSettings, SocialMediaTokens mediaTokens )
        throws InvalidInputException
    {
        if ( mediaTokens == null ) {
            throw new InvalidInputException( "Media tokens passed was null" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( unitSettings == null ) {
            throw new InvalidInputException( "Unit settings passed can not be null" );
        }
        LOG.info( "Updating the social media tokens in profile." );
        organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_SOCIAL_MEDIA_TOKENS, mediaTokens, unitSettings, collection );
        LOG.info( "Successfully updated the social media tokens." );
    }


    // Disclaimer
    @Override
    public void updateDisclaimer( String collection, OrganizationUnitSettings unitSettings, String disclaimer )
        throws InvalidInputException
    {
        if ( disclaimer == null || disclaimer.isEmpty() ) {
            throw new InvalidInputException( "disclaimer passed can not be null or empty" );
        }
        if ( collection == null || collection.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed can not be null or empty" );
        }
        if ( unitSettings == null ) {
            throw new InvalidInputException( "Unit settings passed can not be null" );
        }
        LOG.info( "Updating disclaimer" );
        organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_DISCLAIMER, disclaimer, unitSettings, collection );
        LOG.info( "Disclaimer updated successfully" );
    }


    /**
     * Method to fetch all users under the specified branch of specified company
     */
    @Override
    @Transactional
    public List<AgentSettings> getIndividualsForBranch( String companyProfileName, String branchProfileName )
        throws InvalidInputException, ProfileNotFoundException
    {
        if ( companyProfileName == null || companyProfileName.isEmpty() ) {
            throw new ProfileNotFoundException( "companyProfileName is null or empty in getIndividualsForBranch" );
        }
        if ( branchProfileName == null || branchProfileName.isEmpty() ) {
            throw new ProfileNotFoundException( "branchProfileName is null or empty in getIndividualsForBranch" );
        }
        LOG.info( "Method getIndividualsForBranch called for companyProfileName: " + companyProfileName + " branchProfileName:"
            + branchProfileName );
        List<AgentSettings> users = null;
        OrganizationUnitSettings branchSettings = getBranchByProfileName( companyProfileName, branchProfileName );
        if ( branchSettings != null ) {
            LOG.debug( "Fetching user profiles for branchId: " + branchSettings.getIden() );
            users = getIndividualsByBranchId( branchSettings.getIden() );
        }
        LOG.info( "Method getIndividualsForBranch executed successfully" );
        return users;
    }


    /**
     * Method to fetch all users under the specified region of specified company
     * 
     * @throws NoRecordsFetchedException
     */
    @Override
    @Transactional
    public List<AgentSettings> getIndividualsForRegion( String companyProfileName, String regionProfileName )
        throws InvalidInputException, NoRecordsFetchedException, ProfileNotFoundException
    {
        if ( companyProfileName == null || companyProfileName.isEmpty() ) {
            throw new InvalidInputException( "companyProfileName is null or empty in getIndividualsForRegion" );
        }
        if ( regionProfileName == null || regionProfileName.isEmpty() ) {
            throw new InvalidInputException( "regionProfileName is null or empty in getIndividualsForRegion" );
        }
        LOG.info( "Method getIndividualsForRegion called for companyProfileName:" + companyProfileName
            + " and branchProfileName:" + regionProfileName );
        List<AgentSettings> users = null;
        OrganizationUnitSettings regionSettings = getRegionByProfileName( companyProfileName, regionProfileName );
        if ( regionSettings != null ) {
            users = getIndividualsByRegionId( regionSettings.getIden() );
        }

        LOG.info( "Method getIndividualsForRegion executed successfully" );
        return users;
    }


    /**
     * Method to fetch all individuals directly linked to a company
     * @throws NoRecordsFetchedException
     * @throws InvalidInputException
     * @throws ProfileNotFoundException
     */
    @Override
    @Transactional
    public List<AgentSettings> getIndividualsForCompany( String companyProfileName ) throws InvalidInputException,
        NoRecordsFetchedException, ProfileNotFoundException
    {
        if ( companyProfileName == null || companyProfileName.isEmpty() ) {
            throw new InvalidInputException( "companyProfileName is null or empty in getIndividualsForCompany" );
        }
        LOG.info( "Method getIndividualsForCompany called for companyProfileName: " + companyProfileName );
        List<AgentSettings> users = null;
        OrganizationUnitSettings companySettings = getCompanyProfileByProfileName( companyProfileName );
        if ( companySettings != null ) {
            Region defaultRegion = organizationManagementService.getDefaultRegionForCompany( companyDao.findById(
                Company.class, companySettings.getIden() ) );
            if ( defaultRegion != null ) {
                Branch defaultBranch = organizationManagementService.getDefaultBranchForRegion( defaultRegion.getRegionId() );
                users = getIndividualsByBranchId( defaultBranch.getBranchId() );
            }
        }
        LOG.info( "Method getIndividualsForCompany executed successfully" );
        return users;
    }


    /**
     * Method to fetch all individuals directly linked to a company
     * @throws NoRecordsFetchedException
     * @throws InvalidInputException
     * @throws ProfileNotFoundException
     */
    @Override
    @Transactional
    public List<AgentSettings> getIndividualsForCompany( long companyId ) throws InvalidInputException,
        NoRecordsFetchedException, ProfileNotFoundException
    {
        if ( companyId <= 0l ) {
            throw new InvalidInputException( "Invalid companyId passed in getIndividualsForCompany" );
        }
        LOG.info( "Method getIndividualsForCompany called for companyId: " + companyId );
        List<AgentSettings> users = null;
        Region defaultRegion = organizationManagementService.getDefaultRegionForCompany( companyDao.findById( Company.class,
            companyId ) );
        if ( defaultRegion != null ) {
            Branch defaultBranch = organizationManagementService.getDefaultBranchForRegion( defaultRegion.getRegionId() );
            users = getIndividualsByBranchId( defaultBranch.getBranchId() );
        }
        LOG.info( "Method getIndividualsForCompany executed successfully" );
        return users;
    }


    /**
     * Method to get the region based on profile name
     */
    @Override
    @Transactional
    public OrganizationUnitSettings getRegionByProfileName( String companyProfileName, String regionProfileName )
        throws ProfileNotFoundException, InvalidInputException
    {
        LOG.info( "Method getRegionByProfileName called for companyProfileName:" + companyProfileName
            + " and regionProfileName:" + regionProfileName );
        OrganizationUnitSettings companySettings = null;
        if ( companyProfileName == null || companyProfileName.isEmpty() ) {
            throw new ProfileNotFoundException( "companyProfileName is null or empty in getRegionByProfileName" );
        }
        if ( regionProfileName == null || regionProfileName.isEmpty() ) {
            throw new ProfileNotFoundException( "regionProfileName is null or empty in getRegionByProfileName" );
        }

        companySettings = getCompanyProfileByProfileName( companyProfileName );
        if ( companySettings == null ) {
            LOG.error( "Unable to fetch company settings, invalid input provided by the user" );
            throw new ProfileNotFoundException( "Unable to get company settings " );
        }
        /**
         * generate profileUrl and fetch the region by profileUrl since profileUrl for any region is
         * unique, whereas profileName is unique only within a company
         */
        String profileUrl = utils.generateRegionProfileUrl( companyProfileName, regionProfileName );

        OrganizationUnitSettings regionSettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsByProfileUrl(
            profileUrl, MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION );
        if ( regionSettings == null ) {
            throw new ProfileNotFoundException( "Unable to get region settings " );
        }

        LOG.debug( "Generating final region settings based on lock settings" );
        regionSettings = aggregateRegionProfile( companySettings, regionSettings );
        LOG.info( "Method getRegionByProfileName excecuted successfully" );
        return regionSettings;
    }


    /**
     * Method to get the branch based on profile name
     */
    @Override
    @Transactional
    public OrganizationUnitSettings getBranchByProfileName( String companyProfileName, String branchProfileName )
        throws ProfileNotFoundException, InvalidInputException
    {
        LOG.info( "Method getBranchByProfileName called for companyProfileName:" + companyProfileName
            + " and branchProfileName:" + branchProfileName );

        OrganizationUnitSettings companySettings = getCompanyProfileByProfileName( companyProfileName );
        if ( companySettings == null ) {
            LOG.error( "Unable to fetch company settings, invalid input provided by the user" );
            throw new ProfileNotFoundException( "Unable to get company settings " );
        }
        /**
         * generate profileUrl and fetch the branch by profileUrl since profileUrl for any branch is
         * unique, whereas profileName is unique only within a company
         */
        String profileUrl = utils.generateBranchProfileUrl( companyProfileName, branchProfileName );
        OrganizationUnitSettings branchSettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsByProfileUrl(
            profileUrl, MongoOrganizationUnitSettingDaoImpl.BRANCH_SETTINGS_COLLECTION );

        if ( branchSettings == null ) {
            LOG.error( "Unable to fetch branch settings, invalid input provided by the user" );
            throw new ProfileNotFoundException( "Unable to get branch settings " );
        }

        LOG.debug( "Fetching branch from db to identify the region" );
        Branch branch = branchDao.findById( Branch.class, branchSettings.getIden() );
        if ( branch == null ) {
            LOG.error( "Unable to get branch with this iden " + branchSettings.getIden() );
            throw new ProfileNotFoundException( "Unable to get branch with this iden " + branchSettings.getIden() );

        }
        OrganizationUnitSettings regionSettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById( branch
            .getRegion().getRegionId(), MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION );

        branchSettings = aggregateBranchProfile( companySettings, regionSettings, branchSettings );

        LOG.info( "Method getBranchByProfileName excecuted successfully" );
        return branchSettings;
    }


    /**
     * JIRA:SS-117 by RM02 Method to get the company details based on profile name
     */
    @Override
    @Transactional
    public OrganizationUnitSettings getCompanyProfileByProfileName( String profileName ) throws ProfileNotFoundException
    {
        LOG.info( "Method getCompanyDetailsByProfileName called for profileName : " + profileName );
        if ( profileName == null || profileName.isEmpty() ) {
            throw new ProfileNotFoundException( "profile name is null or empty while getting company details" );
        }
        OrganizationUnitSettings companySettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsByProfileName(
            profileName, MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION );
        if ( companySettings == null ) {
            LOG.error( "Unable to find company settings with profile name : " + profileName );
            throw new ProfileNotFoundException( "Unable to find company settings with profile name : " + profileName );
        }

        LOG.info( "Successfully executed method getCompanyDetailsByProfileName. Returning :" + companySettings );
        return companySettings;
    }


    /**
     * Method to get profile of an individual
     * 
     * @throws NoRecordsFetchedException
     */
    @Override
    @Transactional
    public OrganizationUnitSettings getIndividualByProfileName( String agentProfileName ) throws ProfileNotFoundException,
        InvalidInputException, NoRecordsFetchedException
    {
        LOG.info( "Method getIndividualByProfileName called for agentProfileName:" + agentProfileName );

        OrganizationUnitSettings agentSettings = null;
        if ( agentProfileName == null || agentProfileName.isEmpty() ) {
            throw new ProfileNotFoundException( "agentProfileName is null or empty while getting agent settings" );
        }

        agentSettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsByProfileName( agentProfileName,
            MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION );
        if ( agentSettings == null ) {
            throw new ProfileNotFoundException( "No settings found for agent while fetching agent profile" );
        }

        User user = userDao.findById( User.class, agentSettings.getIden() );

        LOG.debug( "Fetching user profiles for agentId: " + agentSettings.getIden() );
        UserProfile userProfile = null;
        for ( UserProfile profile : user.getUserProfiles() ) {
            if ( profile.getProfilesMaster().getProfileId() == CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID
                && profile.getStatus() == CommonConstants.STATUS_ACTIVE ) {
                userProfile = profile;
                break;
            }
        }
        if ( userProfile == null ) {
            throw new ProfileNotFoundException( "User profiles not found while fetching agent profile" );
        }

        long companyId = userProfile.getCompany().getCompanyId();
        LOG.debug( "Fetching company settings for companyId: " + companyId );
        OrganizationUnitSettings companySettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById( companyId,
            MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION );

        long regionId = userProfile.getRegionId();
        OrganizationUnitSettings regionSettings = null;
        if ( regionId > 0l ) {
            LOG.debug( "Fetching region settings for regionId: " + regionId );
            regionSettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById( regionId,
                MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION );
        }

        long branchId = userProfile.getBranchId();
        OrganizationUnitSettings branchSettings = null;
        if ( branchId > 0l ) {
            LOG.debug( "Fetching branch settings for regionId: " + branchId );
            branchSettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById( branchId,
                MongoOrganizationUnitSettingDaoImpl.BRANCH_SETTINGS_COLLECTION );
        }

        // LockSettings parentLock = lockSettingsTillBranch(companySettings, regionSettings,
        // branchSettings);
        agentSettings = aggregateAgentProfile( companySettings, regionSettings, branchSettings, agentSettings );
        agentSettings = aggregateAgentDetails( user, agentSettings, agentSettings.getLockSettings() );

        LOG.info( "Method getIndividualByProfileName executed successfully" );
        return agentSettings;
    }


    @Override
    @Transactional
    public OrganizationUnitSettings getIndividualSettingsByProfileName( String agentProfileName )
        throws ProfileNotFoundException, InvalidInputException, NoRecordsFetchedException
    {
        LOG.info( "Method getIndividualByProfileName called for agentProfileName:" + agentProfileName );

        OrganizationUnitSettings agentSettings = null;
        if ( agentProfileName == null || agentProfileName.isEmpty() ) {
            throw new ProfileNotFoundException( "agentProfileName is null or empty while getting agent settings" );
        }
        agentSettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsByProfileName( agentProfileName,
            MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION );
        if ( agentSettings == null ) {
            throw new ProfileNotFoundException( "No settings found for agent while fetching agent profile" );
        }
        return agentSettings;
    }


    @Override
    @Transactional
    public Map<String, Long> getPrimaryHierarchyByAgentProfile( OrganizationUnitSettings agentSettings )
        throws InvalidInputException, ProfileNotFoundException
    {
        LOG.info( "Inside method getPrimaryHierarchyByAgentProfile " );
        Map<String, Long> hierarchyMap = userManagementService.getPrimaryUserProfileByAgentId( agentSettings.getIden() );
        LOG.info( "Returning from getPrimaryHierarchyByAgentProfile " );
        return hierarchyMap;
    }


    @Override
    @Transactional
    public SocialMediaTokens aggregateSocialProfiles( OrganizationUnitSettings unitSettings, String entity )
        throws InvalidInputException, NoRecordsFetchedException
    {
        LOG.info( "Method aggregateSocialProfiles called for agentProfileName:" + unitSettings.getProfileName() );

        long companyId = 0l;
        if ( entity.equals( CommonConstants.AGENT_ID ) ) {
            User user = userDao.findById( User.class, unitSettings.getIden() );
            companyId = user.getCompany().getCompanyId();
        } else if ( entity.equals( CommonConstants.BRANCH_ID ) ) {
            Branch branch = branchDao.findById( Branch.class, unitSettings.getIden() );
            companyId = branch.getCompany().getCompanyId();
        } else if ( entity.equals( CommonConstants.REGION_ID ) ) {
            Region region = regionDao.findById( Region.class, unitSettings.getIden() );
            companyId = region.getCompany().getCompanyId();
        }

        LOG.debug( "Fetching company settings for companyId: " + companyId );
        OrganizationUnitSettings companySettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById( companyId,
            MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION );

        // Check if unit settings or company settings have any tokens
        // if company social token and entity tokens are null return null
        if ( unitSettings.getSocialMediaTokens() == null && companySettings.getSocialMediaTokens() == null ) {
            return null;
        }

        // Aggregate urls
        SocialMediaTokens entityTokens = validateSocialMediaTokens( unitSettings );

        if ( companySettings.getSocialMediaTokens() != null ) {
            SocialMediaTokens companyTokens = validateSocialMediaTokens( companySettings );

            if ( ( entityTokens.getFacebookToken().getFacebookPageLink() == null || entityTokens.getFacebookToken()
                .getFacebookPageLink().equals( "" ) )
                && companyTokens.getFacebookToken().getFacebookPageLink() != null
                && !companyTokens.getFacebookToken().getFacebookPageLink().equals( "" ) ) {
                entityTokens.getFacebookToken().setFacebookPageLink( companyTokens.getFacebookToken().getFacebookPageLink() );
            }
            if ( ( entityTokens.getGoogleToken().getProfileLink() == null || entityTokens.getGoogleToken().getProfileLink()
                .equals( "" ) )
                && companyTokens.getGoogleToken().getProfileLink() != null
                && !companyTokens.getGoogleToken().getProfileLink().equals( "" ) ) {
                entityTokens.getGoogleToken().setProfileLink( companyTokens.getGoogleToken().getProfileLink() );
            }
            if ( ( entityTokens.getLinkedInToken().getLinkedInPageLink() == null || entityTokens.getLinkedInToken()
                .getLinkedInPageLink().equals( "" ) )
                && companyTokens.getLinkedInToken().getLinkedInPageLink() != null
                && !companyTokens.getLinkedInToken().getLinkedInPageLink().equals( "" ) ) {
                entityTokens.getLinkedInToken().setLinkedInPageLink( companyTokens.getLinkedInToken().getLinkedInPageLink() );
            }
            if ( ( entityTokens.getRssToken().getProfileLink() == null || entityTokens.getRssToken().getProfileLink()
                .equals( "" ) )
                && companyTokens.getRssToken().getProfileLink() != null
                && !companyTokens.getRssToken().getProfileLink().equals( "" ) ) {
                entityTokens.getRssToken().setProfileLink( companyTokens.getRssToken().getProfileLink() );
            }
            if ( ( entityTokens.getTwitterToken().getTwitterPageLink() == null || entityTokens.getTwitterToken()
                .getTwitterPageLink().equals( "" ) )
                && companyTokens.getTwitterToken().getTwitterPageLink() != null
                && !companyTokens.getTwitterToken().getTwitterPageLink().equals( "" ) ) {
                entityTokens.getTwitterToken().setTwitterPageLink( companyTokens.getTwitterToken().getTwitterPageLink() );
            }
            if ( ( entityTokens.getYelpToken().getYelpPageLink() == null || entityTokens.getYelpToken().getYelpPageLink()
                .equals( "" ) )
                && companyTokens.getYelpToken().getYelpPageLink() != null
                && !companyTokens.getYelpToken().getYelpPageLink().equals( "" ) ) {
                entityTokens.getYelpToken().setYelpPageLink( companyTokens.getYelpToken().getYelpPageLink() );
            }
            if ( ( entityTokens.getZillowToken().getZillowProfileLink() == null || entityTokens.getZillowToken()
                .getZillowProfileLink().equals( "" ) )
                && companyTokens.getZillowToken().getZillowProfileLink() != null
                && !companyTokens.getZillowToken().getZillowProfileLink().equals( "" ) ) {
                entityTokens.getZillowToken().setZillowProfileLink( companyTokens.getZillowToken().getZillowProfileLink() );
            }
            if ( ( entityTokens.getLendingTreeToken().getLendingTreeProfileLink() == null || entityTokens.getLendingTreeToken()
                .getLendingTreeProfileLink().equals( "" ) )
                && companyTokens.getLendingTreeToken().getLendingTreeProfileLink() != null
                && !companyTokens.getLendingTreeToken().getLendingTreeProfileLink().equals( "" ) ) {
                entityTokens.getLendingTreeToken().setLendingTreeProfileLink(
                    companyTokens.getLendingTreeToken().getLendingTreeProfileLink() );
            }
            if ( ( entityTokens.getRealtorToken().getRealtorProfileLink() == null || entityTokens.getRealtorToken()
                .getRealtorProfileLink().equals( "" ) )
                && companyTokens.getRealtorToken().getRealtorProfileLink() != null
                && !companyTokens.getRealtorToken().getRealtorProfileLink().equals( "" ) ) {
                entityTokens.getRealtorToken().setRealtorProfileLink( companyTokens.getRealtorToken().getRealtorProfileLink() );
            }
        }

        LOG.info( "Method aggregateSocialProfiles executed successfully: " + entityTokens.toString() );
        return entityTokens;
    }


    SocialMediaTokens validateSocialMediaTokens( OrganizationUnitSettings unitSettings )
    {
        SocialMediaTokens mediaTokens;
        if ( unitSettings.getSocialMediaTokens() == null ) {
            mediaTokens = new SocialMediaTokens();
        } else {
            mediaTokens = unitSettings.getSocialMediaTokens();
        }

        if ( mediaTokens.getFacebookToken() == null ) {
            mediaTokens.setFacebookToken( new FacebookToken() );
        }
        if ( mediaTokens.getGoogleToken() == null ) {
            mediaTokens.setGoogleToken( new GoogleToken() );
        }
        if ( mediaTokens.getLinkedInToken() == null ) {
            mediaTokens.setLinkedInToken( new LinkedInToken() );
        }
        if ( mediaTokens.getRssToken() == null ) {
            mediaTokens.setRssToken( new SocialProfileToken() );
        }
        if ( mediaTokens.getTwitterToken() == null ) {
            mediaTokens.setTwitterToken( new TwitterToken() );
        }
        if ( mediaTokens.getYelpToken() == null ) {
            mediaTokens.setYelpToken( new YelpToken() );
        }
        if ( mediaTokens.getZillowToken() == null ) {
            mediaTokens.setZillowToken( new ZillowToken() );
        }
        if ( mediaTokens.getLendingTreeToken() == null ) {
            mediaTokens.setLendingTreeToken( new LendingTreeToken() );
        }
        if ( mediaTokens.getRealtorToken() == null ) {
            mediaTokens.setRealtorToken( new RealtorToken() );
        }
        return mediaTokens;
    }


    /**
     * Method to get User by profileName
     * 
     * @throws NoRecordsFetchedException
     */
    @Override
    @Transactional
    public User getUserByProfileName( String agentProfileName, boolean checkStatus ) throws ProfileNotFoundException
    {
        LOG.info( "Method getUserProfilesByProfileName called for agentProfileName:" + agentProfileName );

        OrganizationUnitSettings agentSettings = null;
        if ( agentProfileName == null || agentProfileName.isEmpty() ) {
            throw new ProfileNotFoundException( "agentProfileName is null or empty while getting agent settings" );
        }

        agentSettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsByProfileName( agentProfileName,
            MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION );
        if ( agentSettings == null ) {
            throw new ProfileNotFoundException( "No settings found for agent while fetching agent profile" );
        }

        User user = userDao.findById( User.class, agentSettings.getIden() );
        if ( user.getStatus() == CommonConstants.STATUS_INACTIVE && checkStatus ) {
            throw new ProfileNotFoundException( "No active agent found." );
        }

        LOG.info( "Method getUserProfilesByProfileName executed successfully" );
        return user;
    }


    @Override
    @Transactional
    public UserCompositeEntity getCompositeUserObjectByProfileName( String agentProfileName, boolean checkStatus )
        throws ProfileNotFoundException
    {
        LOG.info( "Getting the user composite object by profile name: " + agentProfileName + " and check status: "
            + checkStatus );
        if ( agentProfileName == null || agentProfileName.isEmpty() ) {
            LOG.error( "agentProfileName is null or empty while getting agent settings" );
            throw new ProfileNotFoundException( "agentProfileName is null or empty while getting agent settings" );
        }
        UserCompositeEntity compositeUserObject = null;
        AgentSettings agentSettings = null;
        User user = null;
        OrganizationUnitSettings organizationUnitSettings = organizationUnitSettingsDao
            .fetchOrganizationUnitSettingsByProfileName( agentProfileName,
                MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION );
        if ( organizationUnitSettings == null
            || ( organizationUnitSettings.getStatus() != null && organizationUnitSettings.getStatus().equalsIgnoreCase(
                CommonConstants.STATUS_DELETED_MONGO ) ) ) {
            LOG.warn( "No profile found with profile name: " + agentProfileName );
            throw new ProfileNotFoundException( "No profile found with profile name: " + agentProfileName );
        } else {
            LOG.debug( "Found the setting. Converting into agent settings" );
            agentSettings = (AgentSettings) organizationUnitSettings;
            // handle the cases where record is present in the mongo but not in SQL
            try {
                user = userDao.findById( User.class, agentSettings.getIden() );
            } catch ( HibernateException e ) {
                LOG.error( "No active agent found in SQL.", e );
                throw new ProfileNotFoundException( "No active agent found in SQL." );
            }
            if ( user == null || ( user.getStatus() == CommonConstants.STATUS_INACTIVE && checkStatus ) ) {
                LOG.error( "No active agent found." );
                throw new ProfileNotFoundException( "No active agent found." );
            }
            compositeUserObject = new UserCompositeEntity();
            compositeUserObject.setUser( user );
            compositeUserObject.setAgentSettings( agentSettings );
        }
        LOG.info( "Returning the user composite object." );
        return compositeUserObject;
    }


    @Override
    @Transactional
    public List<AgentSettings> getIndividualsByBranchId( long branchId ) throws InvalidInputException
    {
        LOG.info( "Method getIndividualsByBranchId called for branchId:" + branchId );
        List<AgentSettings> users = null;
        Map<String, Object> queries = new HashMap<String, Object>();
        queries.put( CommonConstants.STATUS_COLUMN, CommonConstants.STATUS_ACTIVE );
        queries.put( CommonConstants.BRANCH_ID_COLUMN, branchId );
        queries.put( CommonConstants.PROFILE_MASTER_COLUMN,
            userManagementService.getProfilesMasterById( CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID ) );
        List<UserProfile> userProfiles = userProfileDao.findByKeyValueAscendingWithAlias( UserProfile.class, queries,
            Arrays.asList( new String[] { "firstName", "lastName" } ), "user" );
        if ( userProfiles != null && !userProfiles.isEmpty() ) {
            users = new ArrayList<AgentSettings>();
            for ( UserProfile userProfile : userProfiles ) {
                users.add( organizationUnitSettingsDao.fetchAgentSettingsById( userProfile.getUser().getUserId() ) );
            }
            LOG.debug( "Returning :" + users.size() + " individuals for branch : " + branchId );
        }
        LOG.info( "Method getIndividualsByBranchId executed successfully" );
        return users;
    }


    /**
     * Method to get individuals by branchId
     * 
     * @param branchId
     * @param startIndex
     * @param batchSize
     * @return List of {AgentSettings}
     */
    @Override
    @Transactional
    public List<AgentSettings> getIndividualsByBranchId( long branchId, int startIndex, int batchSize )
        throws InvalidInputException
    {
        LOG.info( "Method getIndividualsByBranchId called for branchId:" + branchId + ", startIndex: " + startIndex
            + ", batchSize: " + batchSize );
        List<AgentSettings> users = null;
        Map<String, Object> queries = new HashMap<String, Object>();
        queries.put( CommonConstants.STATUS_COLUMN, CommonConstants.STATUS_ACTIVE );
        queries.put( CommonConstants.BRANCH_ID_COLUMN, branchId );
        queries.put( CommonConstants.PROFILE_MASTER_COLUMN,
            userManagementService.getProfilesMasterById( CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID ) );
        List<UserProfile> userProfiles = userProfileDao.findUserProfilesInBatch( queries, startIndex, batchSize );
        if ( userProfiles != null && !userProfiles.isEmpty() ) {
            users = new ArrayList<AgentSettings>();
            for ( UserProfile userProfile : userProfiles ) {
                users.add( organizationUnitSettingsDao.fetchAgentSettingsById( userProfile.getUser().getUserId() ) );
            }
            LOG.debug( "Returning :" + users.size() + " individuals for branch : " + branchId );
        }
        LOG.info( "Method getIndividualsByBranchId executed successfully" );
        return users;
    }


    @Override
    public long getReviewsCountForCompany( long companyId, double minScore, double maxScore, boolean fetchAbusive,
        boolean notRecommended )
    {
        LOG.info( "Method getReviewsCountForCompany called for companyId:" + companyId + " minscore:" + minScore + " maxscore:"
            + maxScore );
        long reviewsCount = 0;
        reviewsCount = surveyDetailsDao.getFeedBacksCount( CommonConstants.COMPANY_ID_COLUMN, companyId, minScore, maxScore,
            fetchAbusive, notRecommended, false, 0l );
        LOG.info( "Method getReviewsCountForCompany executed successfully" );
        return reviewsCount;
    }


    /**
     * Method to fetch all users under the specified region
     */
    @Override
    @Transactional
    public List<AgentSettings> getIndividualsByRegionId( long regionId ) throws InvalidInputException,
        NoRecordsFetchedException
    {
        LOG.info( "Method getIndividualsByRegionId called for regionId: " + regionId );
        List<AgentSettings> users = null;
        if ( regionId <= 0l ) {
            throw new InvalidInputException( "Region id is not set for getIndividualsByRegionId" );
        }
        Branch defaultBranch = organizationManagementService.getDefaultBranchForRegion( regionId );

        Map<String, Object> queries = new HashMap<String, Object>();
        queries.put( CommonConstants.STATUS_COLUMN, CommonConstants.STATUS_ACTIVE );
        queries.put( CommonConstants.REGION_ID_COLUMN, regionId );
        queries.put( CommonConstants.BRANCH_ID_COLUMN, defaultBranch.getBranchId() );
        queries.put( CommonConstants.PROFILE_MASTER_COLUMN,
            userManagementService.getProfilesMasterById( CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID ) );

        LOG.debug( "calling method to fetch user profiles under region :" + regionId );
        List<UserProfile> userProfiles = userProfileDao.findByKeyValueAscendingWithAlias( UserProfile.class, queries,
            Arrays.asList( new String[] { "firstName", "lastName" } ), "user" );

        if ( userProfiles != null && !userProfiles.isEmpty() ) {
            LOG.debug( "Obtained userProfiles with size : " + userProfiles.size() );
            users = new ArrayList<AgentSettings>();
            for ( UserProfile userProfile : userProfiles ) {
                users.add( organizationUnitSettingsDao.fetchAgentSettingsById( userProfile.getUser().getUserId() ) );
            }
        }
        LOG.info( "Method getIndividualsByRegionId executed successfully" );
        return users;
    }


    @Override
    @Transactional
    public List<AgentSettings> getIndividualsByRegionId( long regionId, int startIndex, int batchSize )
        throws InvalidInputException, NoRecordsFetchedException
    {
        LOG.info( "Method getIndividualsByRegionId called for regionId: " + regionId );
        List<AgentSettings> users = null;
        if ( regionId <= 0l ) {
            throw new InvalidInputException( "Region id is not set for getIndividualsByRegionId" );
        }
        Branch defaultBranch = organizationManagementService.getDefaultBranchForRegion( regionId );

        Map<String, Object> queries = new HashMap<String, Object>();
        queries.put( CommonConstants.STATUS_COLUMN, CommonConstants.STATUS_ACTIVE );
        queries.put( CommonConstants.REGION_ID_COLUMN, regionId );
        queries.put( CommonConstants.BRANCH_ID_COLUMN, defaultBranch.getBranchId() );
        queries.put( CommonConstants.PROFILE_MASTER_COLUMN,
            userManagementService.getProfilesMasterById( CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID ) );

        LOG.debug( "calling method to fetch user profiles under region :" + regionId );
        List<UserProfile> userProfiles = userProfileDao.findUserProfilesInBatch( queries, startIndex, batchSize );
        ;

        if ( userProfiles != null && !userProfiles.isEmpty() ) {
            LOG.debug( "Obtained userProfiles with size : " + userProfiles.size() );
            users = new ArrayList<AgentSettings>();
            for ( UserProfile userProfile : userProfiles ) {
                users.add( organizationUnitSettingsDao.fetchAgentSettingsById( userProfile.getUser().getUserId() ) );
            }
        }
        LOG.info( "Method getIndividualsByRegionId executed successfully" );
        return users;
    }


    /**
     * Method to fetch reviews based on the profile level specified, iden is one of
     * agentId/branchId/regionId or companyId based on the profile level
     */
    @Override
    public List<SurveyDetails> getReviews( long iden, double startScore, double limitScore, int startIndex, int numOfRows,
        String profileLevel, boolean fetchAbusive, Date startDate, Date endDate, String sortCriteria )
        throws InvalidInputException
    {
        LOG.info( "Method getReviews called for iden:" + iden + " startScore:" + startScore + " limitScore:" + limitScore
            + " startIndex:" + startIndex + " numOfRows:" + numOfRows + " profileLevel:" + profileLevel );
        List<SurveyDetails> surveyDetails = null;
        if ( iden <= 0l ) {
            throw new InvalidInputException( "iden is invalid while fetching reviews" );
        }

        Calendar calendar = Calendar.getInstance();
        if ( startDate != null ) {
            calendar.setTime( startDate );
            calendar.add( Calendar.DATE, 0 );
            startDate = calendar.getTime();
        }
        if ( endDate != null ) {
            calendar.setTime( endDate );
            calendar.add( Calendar.DATE, 1 );
            endDate = calendar.getTime();
        }

        String idenColumnName = getIdenColumnNameFromProfileLevel( profileLevel );
        surveyDetails = surveyDetailsDao.getFeedbacks( idenColumnName, iden, startIndex, numOfRows, startScore, limitScore,
            fetchAbusive, startDate, endDate, sortCriteria );

        // This is not needed. Commenting out
        /*for (SurveyDetails review : surveyDetails) {
            OrganizationUnitSettings agentSettings = organizationUnitSettingsDao.fetchAgentSettingsById(review.getAgentId());
            if (agentSettings != null && agentSettings.getSocialMediaTokens() != null) {
                SocialMediaTokens mediaTokens = agentSettings.getSocialMediaTokens();

                // adding yelpUrl
                if (mediaTokens.getYelpToken() != null && mediaTokens.getYelpToken().getYelpPageLink() != null) {
                    review.setYelpProfileUrl(mediaTokens.getYelpToken().getYelpPageLink());
                }

                // adding zillowUrl
                if (mediaTokens.getZillowToken() != null && mediaTokens.getZillowToken().getZillowProfileLink() != null) {
                    review.setZillowProfileUrl(mediaTokens.getZillowToken().getZillowProfileLink());
                }

                // adding lendingTreeUrl
                if (mediaTokens.getLendingTreeToken() != null && mediaTokens.getLendingTreeToken().getLendingTreeProfileLink() != null) {
                    review.setLendingTreeProfileUrl(mediaTokens.getLendingTreeToken().getLendingTreeProfileLink());
                }
                if (mediaTokens.getRealtorToken() != null && mediaTokens.getRealtorToken().getRealtorProfileLink() != null) {
                    review.setRealtorProfileUrl(mediaTokens.getRealtorToken().getRealtorProfileLink());
                }
            }
        }*/

        return surveyDetails;
    }


    @Override
    public List<SurveyDetails> getReviewsForReports( long iden, double startScore, double limitScore, int startIndex,
        int numOfRows, String profileLevel, boolean fetchAbusive, Date startDate, Date endDate, String sortCriteria )
        throws InvalidInputException
    {
        LOG.info( "Method getReviews called for iden:" + iden + " startScore:" + startScore + " limitScore:" + limitScore
            + " startIndex:" + startIndex + " numOfRows:" + numOfRows + " profileLevel:" + profileLevel );
        List<SurveyDetails> surveyDetails = null;
        if ( iden <= 0l ) {
            throw new InvalidInputException( "iden is invalid while fetching reviews" );
        }

        Calendar calendar = Calendar.getInstance();
        if ( startDate != null ) {
            calendar.setTime( startDate );
            calendar.add( Calendar.DATE, 0 );
            startDate = calendar.getTime();
        }
        if ( endDate != null ) {
            calendar.setTime( endDate );
            calendar.add( Calendar.DATE, 1 );
            endDate = calendar.getTime();
        }

        String idenColumnName = getIdenColumnNameFromProfileLevel( profileLevel );
        surveyDetails = surveyDetailsDao.getFeedbacksForReports( idenColumnName, iden, startIndex, numOfRows, startScore,
            limitScore, fetchAbusive, startDate, endDate, sortCriteria );

        return surveyDetails;
    }


    /**
     * Method to get average ratings based on the profile level specified, iden is one of
     * agentId/branchId/regionId or companyId based on the profile level
     */
    @Override
    public double getAverageRatings( long iden, String profileLevel, boolean aggregateAbusive ) throws InvalidInputException
    {
        return getAverageRatings( iden, profileLevel, aggregateAbusive, false, 0, 0 );
    }


    @Override
    public double getAverageRatings( long iden, String profileLevel, boolean aggregateAbusive, boolean includeZillow,
        long zillowTotalScore, long zillowReviewCount ) throws InvalidInputException
    {
        LOG.info( "Method getAverageRatings called for iden :" + iden + " profilelevel:" + profileLevel );
        if ( iden <= 0l ) {
            throw new InvalidInputException( "iden is invalid for getting average rating os a company" );
        }
        String idenColumnName = getIdenColumnNameFromProfileLevel( profileLevel );
        double averageRating = surveyDetailsDao.getRatingForPastNdays( idenColumnName, iden, -1, aggregateAbusive, false,
            includeZillow, zillowReviewCount, zillowTotalScore );

        //get formatted survey score using rating format  
        averageRating = surveyHandler.getFormattedSurveyScore( averageRating );

        LOG.info( "Method getAverageRatings executed successfully.Returning: " + averageRating );
        return averageRating;
    }


    /**
     * Method to get iden column name from profile level
     * 
     * @param profileLevel
     * @return
     * @throws InvalidInputException
     */
    String getIdenColumnNameFromProfileLevel( String profileLevel ) throws InvalidInputException
    {
        LOG.debug( "Getting iden column name for profile level:" + profileLevel );
        String idenColumnName = null;
        if ( profileLevel == null || profileLevel.isEmpty() ) {
            throw new InvalidInputException( "profile level is null or empty while getting iden column name" );
        }
        switch ( profileLevel ) {
            case CommonConstants.PROFILE_LEVEL_COMPANY:
                idenColumnName = CommonConstants.COMPANY_ID_COLUMN;
                break;
            case CommonConstants.PROFILE_LEVEL_REGION:
                idenColumnName = CommonConstants.REGION_ID_COLUMN;
                break;
            case CommonConstants.PROFILE_LEVEL_BRANCH:
                idenColumnName = CommonConstants.BRANCH_ID_COLUMN;
                break;
            case CommonConstants.PROFILE_LEVEL_INDIVIDUAL:
                idenColumnName = CommonConstants.AGENT_ID_COLUMN;
                break;
            case CommonConstants.PROFILE_LEVEL_REALTECH_ADMIN:
                break;
            default:
                throw new InvalidInputException( "Invalid profile level while getting iden column name" );
        }
        LOG.debug( "Returning column name:" + idenColumnName + " for profile level:" + profileLevel );
        return idenColumnName;
    }


    /**
     * Method to get reviews count based on the profile level specified, iden is one of
     * agentId/branchId/regionId or companyId based on the profile level within limit of rating
     * score specified
     */
    @Override
    public long getReviewsCount( long iden, double minScore, double maxScore, String profileLevel, boolean fetchAbusive,
        boolean notRecommended ) throws InvalidInputException
    {
        return getReviewsCount( iden, minScore, maxScore, profileLevel, fetchAbusive, notRecommended, false, 0 );
    }


    @Override
    public long getReviewsCount( long iden, double minScore, double maxScore, String profileLevel, boolean fetchAbusive,
        boolean notRecommended, boolean includeZillow, long zillowReviewCount ) throws InvalidInputException
    {
        LOG.info( "Method getReviewsCount called for iden:" + iden + " minscore:" + minScore + " maxscore:" + maxScore
            + " profilelevel:" + profileLevel );
        if ( iden <= 0l ) {
            throw new InvalidInputException( "Iden is invalid for getting reviews count" );
        }
        long reviewsCount = 0;
        String idenColumnName = getIdenColumnNameFromProfileLevel( profileLevel );
        reviewsCount = surveyDetailsDao.getFeedBacksCount( idenColumnName, iden, minScore, maxScore, fetchAbusive,
            notRecommended, includeZillow, zillowReviewCount );
        LOG.info( "Method getReviewsCount executed successfully. Returning reviewsCount:" + reviewsCount );
        return reviewsCount;
    }


    /**
     * Method to get the list of individuals for branch/region or company as specified ide in one of
     * branchId/regionId/companyId
     * 
     * @throws SolrException
     */
    @Override
    public Collection<UserFromSearch> getProListByProfileLevel( long iden, String profileLevel, int start, int numOfRows )
        throws InvalidInputException, SolrException
    {
        LOG.info( "Method getProListByProfileLevel called for iden: " + iden + " profileLevel:" + profileLevel + " start:"
            + start + " numOfRows:" + numOfRows );
        if ( iden <= 0l ) {
            throw new InvalidInputException( "iden is invalid in getProListByProfileLevel" );
        }
        if ( profileLevel == null || profileLevel.isEmpty() ) {
            throw new InvalidInputException( "profile level is null in getProListByProfileLevel" );
        }
        String idenFieldName = null;
        Collection<UserFromSearch> solrSearchResult = null;
        switch ( profileLevel ) {
            case CommonConstants.PROFILE_LEVEL_COMPANY:
                idenFieldName = CommonConstants.COMPANY_ID_SOLR;
                break;
            case CommonConstants.PROFILE_LEVEL_REGION:
                idenFieldName = CommonConstants.REGIONS_SOLR;
                break;
            case CommonConstants.PROFILE_LEVEL_BRANCH:
                idenFieldName = CommonConstants.BRANCHES_SOLR;
                break;
            default:
                throw new InvalidInputException( "profile level is invalid in getProListByProfileLevel" );
        }
        solrSearchResult = solrSearchService.searchUsersByIden( iden, idenFieldName, true, start, numOfRows );

        LOG.info( "Method getProListByProfileLevel finished successfully" );
        return solrSearchResult;
    }


    @Override
    public void generateVerificationUrl( Map<String, String> urlParams, String applicationUrl, String recipientMailId,
        String recipientName ) throws InvalidInputException, UndeliveredEmailException
    {
        String verficationUrl = urlGenerator.generateUrl( urlParams, applicationUrl );
        emailServices.sendEmailVerificationMail( verficationUrl, recipientMailId, recipientName );
    }


    /**
     * 
     * @param mailIds
     * @param entityType
     * @param userSettings
     * @throws InvalidInputException
     * @throws UndeliveredEmailException
     */
    @Override
    public void generateAndSendEmailVerificationRequestLinkToAdmin( List<MiscValues> mailIds, long companyId,
        String entityType, OrganizationUnitSettings entitySettings ) throws InvalidInputException, UndeliveredEmailException
    {
        LOG.info( "Method generateAndSendEmailVerificationRequestLinkToAdmin started " );
        Map<String, String> urlParams = null;

        if ( entitySettings == null ) {
            throw new InvalidInputException( "Invalid argument passed , passed entity setting is null: " );
        }


        User companyAdmin = userManagementService.getCompanyAdmin( companyId );
        if ( companyAdmin == null ) {
            throw new InvalidInputException( "No admin found for passed company id : " + companyId );
        }

        String adminName = companyAdmin.getFirstName();
        if ( companyAdmin.getLastName() != null && !companyAdmin.getLastName().isEmpty() ) {
            adminName = companyAdmin.getFirstName() + " " + companyAdmin.getLastName();
        }

        for ( MiscValues mailId : mailIds ) {
            String key = mailId.getKey();
            String emailId = mailId.getValue();
            if ( key.equalsIgnoreCase( CommonConstants.EMAIL_TYPE_WORK ) ) {
                urlParams = new HashMap<String, String>();
                urlParams.put( CommonConstants.EMAIL_ID, emailId );
                urlParams.put( CommonConstants.EMAIL_TYPE, CommonConstants.EMAIL_TYPE_WORK );
                urlParams.put( CommonConstants.ENTITY_ID_COLUMN, entitySettings.getIden() + "" );
                urlParams.put( CommonConstants.ENTITY_TYPE_COLUMN, entityType );
                urlParams.put( CommonConstants.URL_PARAM_VERIFICATION_REQUEST_TYPE,
                    CommonConstants.URL_PARAM_VERIFICATION_REQUEST_TYPE_TO_ADMIN );

                String verficationUrl = urlGenerator.generateUrl( urlParams, applicationBaseUrl
                    + CommonConstants.REQUEST_MAPPING_EMAIL_EDIT_VERIFICATION );
                emailServices.sendEmailVerificationRequestMailToAdmin( verficationUrl, companyAdmin.getEmailId(), adminName,
                    emailId, entitySettings.getContact_details().getName() );
            }
        }


    }


    @Override
    @Transactional
    public String updateEmailVerificationStatus( String urlParamsStr ) throws InvalidInputException, NonFatalException
    {
        Map<String, String> urlParams = urlGenerator.decryptParameters( urlParamsStr );
        if ( urlParams == null || urlParams.isEmpty() ) {
            throw new InvalidInputException( "Url params are invalid for email verification" );
        }

        String emailAddress = urlParams.get( CommonConstants.EMAIL_ID );
        String emailType = urlParams.get( CommonConstants.EMAIL_TYPE );
        long iden = Long.parseLong( urlParams.get( CommonConstants.ENTITY_ID_COLUMN ) );
        String collection = urlParams.get( CommonConstants.ENTITY_TYPE_COLUMN );
        String verificationType = urlParams.get( CommonConstants.URL_PARAM_VERIFICATION_REQUEST_TYPE );

        OrganizationUnitSettings unitSettings = organizationUnitSettingsDao
            .fetchOrganizationUnitSettingsById( iden, collection );
        ContactDetailsSettings contactDetails = unitSettings.getContact_details();
        MailIdSettings mailIds = contactDetails.getMail_ids();
        User companyAdmin = null;

        if ( verificationType == null || verificationType.isEmpty() ) {
            throw new InvalidInputException(
                "Url params are invalid for email verification. Parameter Verification type missing" );
        }

        if ( emailType.equals( CommonConstants.EMAIL_TYPE_WORK ) ) {
            String emailVerified = mailIds.getWorkEmailToVerify();

            if ( emailVerified == null || emailVerified.isEmpty() || !emailVerified.equals( emailAddress ) ) {
                throw new InvalidInputException( "Email Id to verify does not match with our records" );
            }

            mailIds.setWork( emailVerified );
            mailIds.setWorkEmailToVerify( null );
            mailIds.setWorkEmailVerified( true );

            if ( collection.equals( MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION ) ) {
                updateCompanyEmail( iden, emailVerified );

                Company company = userManagementService.getCompanyById( iden );
                if ( company != null ) {
                    settingsSetter.setSettingsValueForCompany( company, SettingsForApplication.EMAIL_ID_WORK, true );
                    userManagementService.updateCompany( company );
                }

            } else if ( collection.equals( MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION ) ) {

                Region region = userManagementService.getRegionById( iden );
                if ( region != null ) {
                    settingsSetter.setSettingsValueForRegion( region, SettingsForApplication.EMAIL_ID_WORK, true );
                    userManagementService.updateRegion( region );
                    companyAdmin = userManagementService.getCompanyAdmin( region.getCompany().getCompanyId() );
                }

            } else if ( collection.equals( MongoOrganizationUnitSettingDaoImpl.BRANCH_SETTINGS_COLLECTION ) ) {
                Branch branch = userManagementService.getBranchById( iden );
                if ( branch != null ) {
                    settingsSetter.setSettingsValueForBranch( branch, SettingsForApplication.EMAIL_ID_WORK, true );
                    userManagementService.updateBranch( branch );
                    companyAdmin = userManagementService.getCompanyAdmin( branch.getCompany().getCompanyId() );
                }
            } else if ( collection.equals( MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION ) ) {

                // Update User login name and email id
                User user = userManagementService.getUserByUserId( iden );
                user.setEmailId( emailVerified );
                user.setLoginName( emailVerified );
                userManagementService.updateUser( user, iden, true );

                updateIndividualEmail( iden, emailVerified );

                // Fix for JIRA: SS-1358 - Updating email address should update SOLR records as well
                // BEGIN
                updateEmailIdInSolr( emailVerified, iden );
                // Fix for JIRA: SS-1358 - Updating email address should update SOLR records as well
                // END
                //get company admin
                companyAdmin = userManagementService.getCompanyAdmin( user.getCompany().getCompanyId() );
            }

            //send email verified mail to admin
            if ( verificationType.equalsIgnoreCase( CommonConstants.URL_PARAM_VERIFICATION_REQUEST_TYPE_TO_ADMIN ) ) {
                //send mail to entity
                emailServices.sendEmailVerifiedNotificationMail( emailVerified, unitSettings.getContact_details().getName() );
                //send mail to admin
                if ( companyAdmin != null ) {
                    String adminName = companyAdmin.getFirstName();
                    if ( companyAdmin.getLastName() != null && !companyAdmin.getLastName().isEmpty() ) {
                        adminName = companyAdmin.getFirstName() + " " + companyAdmin.getLastName();
                    }
                    emailServices.sendEmailVerifiedNotificationMailToAdmin( companyAdmin.getLoginName(),
                        adminName, emailVerified, unitSettings.getContact_details().getName() );
                }
            }
        } else if ( emailType.equals( CommonConstants.EMAIL_TYPE_PERSONAL ) ) {
            String emailVerified = mailIds.getPersonalEmailToVerify();

            if ( emailVerified == null || emailVerified.isEmpty() || !emailVerified.equals( emailAddress ) ) {
                throw new InvalidInputException( "Email Id to verify does not match with our records" );
            }

            if ( collection.equals( MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION ) ) {
                Company company = userManagementService.getCompanyById( iden );
                if ( company != null ) {
                    settingsSetter.setSettingsValueForCompany( company, SettingsForApplication.EMAIL_ID_PERSONAL, true );
                    userManagementService.updateCompany( company );
                }
            } else if ( collection.equals( MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION ) ) {
                Region region = userManagementService.getRegionById( iden );
                if ( region != null ) {
                    settingsSetter.setSettingsValueForRegion( region, SettingsForApplication.EMAIL_ID_PERSONAL, true );
                    userManagementService.updateRegion( region );
                }
            } else if ( collection.equals( MongoOrganizationUnitSettingDaoImpl.BRANCH_SETTINGS_COLLECTION ) ) {
                Branch branch = userManagementService.getBranchById( iden );
                if ( branch != null ) {
                    settingsSetter.setSettingsValueForBranch( branch, SettingsForApplication.EMAIL_ID_PERSONAL, true );
                    userManagementService.updateBranch( branch );
                }
            }

            mailIds.setPersonal( mailIds.getPersonalEmailToVerify() );
            mailIds.setPersonalEmailToVerify( null );
            mailIds.setPersonalEmailVerified( true );
        }
        contactDetails.setMail_ids( mailIds );

        organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_CONTACT_DETAIL_SETTINGS, contactDetails, unitSettings, collection );

        return verificationType;
    }


    /**
     * Method to fetch reviews based on the profile level specified, iden is one of
     * agentId/branchId/regionId or companyId based on the profile level
     */
    @Override
    @Transactional
    public List<SurveyPreInitiation> getIncompleteSurvey( long iden, double startScore, double limitScore, int startIndex,
        int numOfRows, String profileLevel, Date startDate, Date endDate, boolean realtechAdmin ) throws InvalidInputException
    {
        LOG.info( "Method getIncompleteSurvey() called for iden:" + iden + " startScore:" + startScore + " limitScore:"
            + limitScore + " startIndex:" + startIndex + " numOfRows:" + numOfRows + " profileLevel:" + profileLevel );
        if ( iden <= 0l ) {
            throw new InvalidInputException( "iden is invalid while fetching incomplete reviews" );
        }
        boolean isCompanyAdmin = false;
        Set<Long> agentIds = new HashSet<>();
        if ( profileLevel.equalsIgnoreCase( CommonConstants.PROFILE_LEVEL_COMPANY ) ) {
            isCompanyAdmin = true;
        } else {
            agentIds = getAgentIdsByProfileLevel( profileLevel, iden );
        }
        Timestamp startTime = null;
        Timestamp endTime = null;
        if ( startDate != null )
            startTime = new Timestamp( startDate.getTime() );
        if ( endDate != null )
            endTime = new Timestamp( endDate.getTime() );
        List<SurveyPreInitiation> surveys = surveyPreInitiationDao.getIncompleteSurvey( startTime, endTime, startIndex,
            numOfRows, agentIds, isCompanyAdmin, iden, realtechAdmin );
        return surveys;
    }


    /**
     * Method to fetch all users for the list of branches specified
     */
    @Override
    public List<AgentSettings> getIndividualsByBranchIds( Set<Long> branchIds ) throws InvalidInputException
    {
        LOG.info( "Method getIndividualsByBranchIds called for branchIds:" + branchIds );
        List<AgentSettings> users = null;
        if ( branchIds != null && !branchIds.isEmpty() ) {
            users = new ArrayList<AgentSettings>();
            for ( long branchId : branchIds ) {
                List<AgentSettings> tempUsers = getIndividualsByBranchId( branchId );
                if ( tempUsers != null && !tempUsers.isEmpty() ) {
                    users.addAll( tempUsers );
                }
            }
        }
        LOG.info( "Method getIndividualsByBranchIds executed successfully" );
        return users;
    }


    /**
     * Method to fetch all users under the specified list of regions
     */
    @Override
    public List<AgentSettings> getIndividualsByRegionIds( Set<Long> regionIds ) throws InvalidInputException,
        NoRecordsFetchedException
    {
        LOG.info( "Method getIndividualsByBranchIds called for regionIds:" + regionIds );
        List<AgentSettings> users = null;
        if ( regionIds != null && !regionIds.isEmpty() ) {
            users = new ArrayList<AgentSettings>();
            for ( long regionId : regionIds ) {
                List<AgentSettings> tempUsers = getIndividualsByRegionId( regionId );
                if ( tempUsers != null && !tempUsers.isEmpty() ) {
                    users.addAll( tempUsers );
                }
            }
        }
        LOG.info( "Method getIndividualsByRegionIds executed successfully" );
        return users;
    }


    /**
     * Method that mails the contact us message to the respective individual,branch,region,company
     * 
     * @param agentProfileName
     * @param message
     * @param senderMailId
     * @param profileType
     * @throws InvalidInputException
     * @throws NoRecordsFetchedException
     * @throws UndeliveredEmailException
     */
    @Override
    public void findProfileMailIdAndSendMail( String profileName, String message, String senderName, String senderMailId,
        String profileType ) throws InvalidInputException, NoRecordsFetchedException, UndeliveredEmailException
    {
        if ( profileName == null || profileName.isEmpty() ) {
            LOG.error( "contactAgent : profileName parameter is empty or null!" );
            throw new InvalidInputException( "contactAgent : profileName parameter is empty or null!" );
        }
        if ( message == null || message.isEmpty() ) {
            LOG.error( "contactAgent : message parameter is empty or null!" );
            throw new InvalidInputException( "contactAgent : message parameter is empty or null!" );
        }
        if ( senderName == null || senderName.isEmpty() ) {
            LOG.error( "contactAgent : senderName parameter is empty or null!" );
            throw new InvalidInputException( "contactAgent : senderName parameter is empty or null!" );
        }
        if ( senderMailId == null || senderMailId.isEmpty() ) {
            LOG.error( "contactAgent : senderMailId parameter is empty or null!" );
            throw new InvalidInputException( "contactAgent : senderMailId parameter is empty or null!" );
        }
        if ( profileType == null || profileType.isEmpty() ) {
            LOG.error( "contactAgent : profileType parameter is empty or null!" );
            throw new InvalidInputException( "contactAgent : profileType parameter is empty or null!" );
        }

        OrganizationUnitSettings settings = null;
        if ( profileType.equals( CommonConstants.PROFILE_LEVEL_INDIVIDUAL ) ) {
            LOG.debug( "Fetching the agent settings from mongo for the agent with profile name : " + profileName );
            settings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsByProfileName( profileName,
                CommonConstants.AGENT_SETTINGS_COLLECTION );
            LOG.debug( "Settings fetched from mongo!" );
        } else if ( profileType.equals( CommonConstants.PROFILE_LEVEL_COMPANY ) ) {
            LOG.debug( "Fetching the company settings from mongo for the company with profile name : " + profileName );
            settings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsByProfileName( profileName,
                CommonConstants.COMPANY_SETTINGS_COLLECTION );
            LOG.debug( "Settings fetched from mongo!" );
        } else if ( profileType.equals( CommonConstants.PROFILE_LEVEL_REGION ) ) {
            LOG.debug( "Fetching the region settings from mongo for the region with profile name : " + profileName );
            settings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsByProfileName( profileName,
                CommonConstants.REGION_SETTINGS_COLLECTION );
            LOG.debug( "Settings fetched from mongo!" );
        } else if ( profileType.equals( CommonConstants.PROFILE_LEVEL_BRANCH ) ) {
            LOG.debug( "Fetching the branch settings from mongo for the branch with profile name : " + profileName );
            settings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsByProfileName( profileName,
                CommonConstants.BRANCH_SETTINGS_COLLECTION );
            LOG.debug( "Settings fetched from mongo!" );
        } else {
            LOG.error( "Profile level not known!" );
            throw new InvalidInputException( "Profile level not known!" );
        }

        if ( settings != null ) {
            LOG.debug( "Sending the contact us mail to the agent" );
            emailServices.sendContactUsMail( settings.getContact_details().getMail_ids().getWork(), settings
                .getContact_details().getName(), senderName, senderMailId, message );
            LOG.debug( "Contact us mail sent!" );
        } else {
            LOG.error( "No records found for agent settings of profile name : " + profileName + " in mongo" );
            throw new NoRecordsFetchedException( "No records found for agent settings of profile name : " + profileName
                + " in mongo" );
        }
    }


    /*
     * Method to store status of a user into the mongo.
     */
    @Override
    public void addSocialPosts( User user, long entityId, String entityType, String postText ) throws InvalidInputException
    {
        LOG.info( "Method to add post to a user's profile started." );
        SocialPost socialPost = new SocialPost();
        socialPost.setPostedBy( user.getFirstName() + " " + user.getLastName() );
        socialPost.setPostText( postText );
        socialPost.setSource( "SocialSurvey" );

        if ( entityType.equals( CommonConstants.COMPANY_ID_COLUMN ) ) {
            socialPost.setCompanyId( user.getCompany().getCompanyId() );
        } else if ( entityType.equals( CommonConstants.REGION_ID_COLUMN ) ) {
            socialPost.setRegionId( entityId );
            socialPost.setCompanyId( user.getCompany().getCompanyId() );
        } else if ( entityType.equals( CommonConstants.BRANCH_ID_COLUMN ) ) {
            socialPost.setBranchId( entityId );
            socialPost.setCompanyId( user.getCompany().getCompanyId() );
        } else if ( entityType.equals( CommonConstants.AGENT_ID_COLUMN ) ) {
            entityId = user.getUserId();
            socialPost.setAgentId( entityId );
            socialPost.setCompanyId( user.getCompany().getCompanyId() );
        }

        socialPost.setTimeInMillis( System.currentTimeMillis() );
        socialPostDao.addPostToUserProfile( socialPost );
        LOG.info( "Updating modified on column in aagent hierarchy fro agent " + user.getFirstName() );
        surveyHandler.updateModifiedOnColumnForEntity( entityType, entityId );
        LOG.info( "Method to add post to a user's profile finished." );
    }


    /*
     * Method to delete status of a user into the mongo.
     */
    @Override
    public void deleteSocialPost( String postMongoId ) throws InvalidInputException
    {
        LOG.info( "Method to delete post to a user's profile started." );
        SocialPost socialPost = socialPostDao.getPostByMongoObjectId( postMongoId );
        if ( socialPost == null ) {
            throw new InvalidInputException( "No Status Found", DisplayMessageConstants.GENERAL_ERROR );
        }

        if ( !socialPost.getSource().equals( CommonConstants.POST_SOURCE_SOCIAL_SURVEY ) ) {
            throw new InvalidInputException( "Not a SocialSurvey Status", DisplayMessageConstants.GENERAL_ERROR );
        }

        long agentId = socialPost.getAgentId();
        long regionId = socialPost.getRegionId();
        long companyId = socialPost.getCompanyId();
        long branchId = socialPost.getBranchId();
        socialPostDao.removePostFromUsersProfile( socialPost );
        //JIRA SS-1329
        try {
            solrSearchService.removeSocialPostFromSolr( postMongoId );
        } catch ( SolrException e ) {
            throw new InvalidInputException( "Error removing social post from Solr. Reason : ", e );
        }
        LOG.info( "Updating modified on column in aagent hierarchy fro agent " );
        if ( companyId > 0 ) {
            surveyHandler.updateModifiedOnColumnForEntity( CommonConstants.COMPANY_ID_COLUMN, companyId );
        }
        if ( regionId > 0 ) {
            surveyHandler.updateModifiedOnColumnForEntity( CommonConstants.REGION_ID_COLUMN, regionId );
        }
        if ( branchId > 0 ) {
            surveyHandler.updateModifiedOnColumnForEntity( CommonConstants.BRANCH_ID_COLUMN, branchId );
        }
        if ( agentId > 0 ) {
            surveyHandler.updateModifiedOnColumnForEntity( CommonConstants.AGENT_ID_COLUMN, agentId );
        }
        LOG.info( "Method to delete post to a user's profile finished." );
    }


    /*
     * Method to fetch social posts for a particular user.
     */
    @Override
    public List<SocialPost> getSocialPosts( long entityId, String entityType, int startIndex, int batchSize )
        throws InvalidInputException
    {
        LOG.info( "Method to fetch social posts , getSocialPosts() started." );
        if ( entityType == null ) {
            throw new InvalidInputException( "No entity type found in session", DisplayMessageConstants.GENERAL_ERROR );
        }

        String key = CommonConstants.AGENT_ID;
        long iden = entityId;

        if ( entityType.equals( CommonConstants.COMPANY_ID_COLUMN ) ) {
            key = CommonConstants.COMPANY_ID;
        } else if ( entityType.equals( CommonConstants.REGION_ID_COLUMN ) ) {
            key = CommonConstants.REGION_ID;
        } else if ( entityType.equals( CommonConstants.BRANCH_ID_COLUMN ) ) {
            key = CommonConstants.BRANCH_ID;
        }

        List<SocialPost> posts = socialPostDao.getSocialPosts( iden, key, startIndex, batchSize );
        LOG.info( "Method to fetch social posts , getSocialPosts() finished." );
        return posts;
    }


    /*
     * Method to fetch social posts for a particular user.
     */
    @Override
    public List<SocialPost> getCumulativeSocialPosts( long entityId, String entityType, int startIndex, int numOfRows,
        String profileLevel, Date startDate, Date endDate ) throws InvalidInputException, NoRecordsFetchedException
    {
        LOG.info( "Method to fetch social posts , getCumulativeSocialPosts() started." );
        List<SocialPost> posts = new ArrayList<SocialPost>();
        if ( entityType == null ) {
            throw new InvalidInputException( "No entity type found in session", DisplayMessageConstants.GENERAL_ERROR );
        }

        //If agent, get social posts for only that agent.
        if ( entityType.equals( CommonConstants.AGENT_ID_COLUMN ) ) {
            posts = socialPostDao
                .getSocialPosts( entityId, CommonConstants.AGENT_ID, startIndex, numOfRows, startDate, endDate );
            //If company, get social posts for that company, all the regions, branches and agents in that company.
        } else if ( entityType.equals( CommonConstants.COMPANY_ID_COLUMN ) ) {
            //Get social posts for company
            posts = socialPostDao.getSocialPosts( entityId, CommonConstants.COMPANY_ID, startIndex, numOfRows, startDate,
                endDate );
            Company company = organizationManagementService.getCompanyById( entityId );
            //Get social posts for all the regions in the company.
            for ( Region region : company.getRegions() ) {
                posts.addAll( socialPostDao.getSocialPosts( region.getRegionId(), CommonConstants.REGION_ID, startIndex,
                    numOfRows, startDate, endDate ) );
            }
            //Get social posts for all the branches in the company
            for ( Branch branch : company.getBranches() ) {
                posts.addAll( socialPostDao.getSocialPosts( branch.getBranchId(), CommonConstants.BRANCH_ID, startIndex,
                    numOfRows, startDate, endDate ) );
            }
            //Get social posts for all the users in the company
            for ( User user : company.getUsers() ) {
                posts.addAll( socialPostDao.getSocialPosts( user.getUserId(), CommonConstants.AGENT_ID, startIndex, numOfRows,
                    startDate, endDate ) );
            }
            //Get all social posts for region
        } else if ( entityType.equals( CommonConstants.REGION_ID_COLUMN ) ) {
            Region region = userManagementService.getRegionById( entityId );
            //Get social posts for the region
            posts = socialPostDao.getSocialPosts( entityId, CommonConstants.REGION_ID, startIndex, numOfRows, startDate,
                endDate );
            //Get social posts for all the branches in the region
            for ( Branch branch : region.getBranches() ) {
                posts.addAll( socialPostDao.getSocialPosts( branch.getBranchId(), CommonConstants.BRANCH_ID, startIndex,
                    numOfRows, startDate, endDate ) );
            }
            //Get social posts for all the users in the region

            if ( getIndividualsByRegionId( entityId ) != null ) {
                for ( AgentSettings user : getIndividualsByRegionId( entityId ) ) {
                    posts.addAll( socialPostDao.getSocialPosts( user.getIden(), CommonConstants.AGENT_ID, startIndex,
                        numOfRows, startDate, endDate ) );
                }
            }
            //Get all social posts for branch
        } else if ( entityType.equals( CommonConstants.BRANCH_ID_COLUMN ) ) {
            //Get social posts for the branch
            posts = socialPostDao.getSocialPosts( entityId, CommonConstants.BRANCH_ID, startIndex, numOfRows, startDate,
                endDate );
            //Get social posts for all the users in the branch
            if ( getIndividualsByBranchId( entityId ) != null ) {
                for ( AgentSettings user : getIndividualsByBranchId( entityId ) ) {
                    posts.addAll( socialPostDao.getSocialPosts( user.getIden(), CommonConstants.AGENT_ID, startIndex,
                        numOfRows, startDate, endDate ) );
                }
            }
        }
        LOG.info( "Method to fetch social posts , getCumulativeSocialPosts() finished." );
        return posts;
    }


    /*
     * Method to fetch social posts for a particular user.
     */
    @Override
    public long getPostsCountForUser( String columnName, long columnValue )
    {
        LOG.info( "Method to fetch count of social posts for a particular user, getPostsCountForUser() started." );
        long postsCount = socialPostDao.getPostsCountByUserId( columnName, columnValue );
        LOG.info( "Method to fetch count of social posts for a particular user, getPostsCountForUser() finished." );
        return postsCount;
    }


    @Override
    public void updateLinkedInProfileData( String collectionName, OrganizationUnitSettings organizationUnitSettings,
        LinkedInProfileData linkedInProfileData ) throws InvalidInputException
    {
        LOG.info( "Updating linked in profile data into " + collectionName );
        if ( linkedInProfileData == null ) {
            throw new InvalidInputException( "LinkedInProfile details passed can not be null" );
        }
        LOG.info( "Updating linkedin profile detail information" );
        organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_LINKEDIN_PROFILEDATA, linkedInProfileData, organizationUnitSettings,
            collectionName );

        String profileImageUrl = organizationUnitSettings.getProfileImageUrl();

        if ( ( profileImageUrl == null || profileImageUrl.trim().isEmpty() ) && linkedInProfileData.getPictureUrls() != null
            && linkedInProfileData.getPictureUrls().get_total() > 0 ) {
            profileImageUrl = linkedInProfileData.getPictureUrls().getValues().get( 0 );
            //Set profileImage and thumbnail
            updateProfileImage( collectionName, organizationUnitSettings, profileImageUrl );
            /*organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
                MongoOrganizationUnitSettingDaoImpl.KEY_PROFILE_IMAGE, profileImageUrl, organizationUnitSettings,
                collectionName );*/
        }

        LOG.info( "Updated the linkedin profile data." );

    }


    @Override
    public void updateAgentExpertise( AgentSettings agentSettings, List<String> expertise ) throws InvalidInputException
    {
        if ( expertise == null || expertise.isEmpty() ) {
            throw new InvalidInputException( "Expertise list is not proper" );
        }
        LOG.info( "Updating agent expertise" );
        organizationUnitSettingsDao.updateParticularKeyAgentSettings( MongoOrganizationUnitSettingDaoImpl.KEY_EXPERTISE,
            expertise, agentSettings );
        LOG.info( "Updated agent expertise." );
    }


    @Override
    public void updateAgentHobbies( AgentSettings agentSettings, List<String> hobbies ) throws InvalidInputException
    {
        if ( hobbies == null || hobbies.isEmpty() ) {
            throw new InvalidInputException( "Hobbies list is not proper" );
        }
        LOG.info( "Updating agent hobbies" );
        organizationUnitSettingsDao.updateParticularKeyAgentSettings( MongoOrganizationUnitSettingDaoImpl.KEY_HOBBIES, hobbies,
            agentSettings );
        LOG.info( "Updated agent hobbies." );
    }


    @Override
    public void updateAgentCompanyPositions( AgentSettings agentSettings, List<CompanyPositions> companyPositions )
        throws InvalidInputException
    {
        if ( companyPositions == null || companyPositions.isEmpty() ) {
            throw new InvalidInputException( "Company positions passed are not proper" );
        }
        LOG.info( "Updating company positions" );
        organizationUnitSettingsDao.updateParticularKeyAgentSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_COMPANY_POSITIONS, companyPositions, agentSettings );
        LOG.info( "Updated company positions." );
    }


    @Override
    public void updateProfileStages( List<ProfileStage> profileStages, OrganizationUnitSettings settings, String collectionName )
    {
        LOG.info( "Method to update profile stages started." );
        organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
            MongoOrganizationUnitSettingDaoImpl.KEY_PROFILE_STAGES, profileStages, settings, collectionName );
        LOG.info( "Method to update profile stages finished." );
    }


    @Override
    public void setAgentProfileUrlForReview( List<SurveyDetails> reviews ) throws InvalidInputException
    {
        String profileUrl;
        String baseProfileUrl = applicationBaseUrl + CommonConstants.AGENT_PROFILE_FIXED_URL;
        String facebookShareUrl = "app_id=" + facebookAppId;
        String googleApiKey = googlePlusId;
        if ( reviews != null && !reviews.isEmpty() ) {
            for ( SurveyDetails review : reviews ) {

                //JIRA SS-1286
                /*Collection<UserFromSearch> documents = solrSearchService.searchUsersByIden( review.getAgentId(),
                    CommonConstants.USER_ID_SOLR, true, 0, 1 );*/
                // adding completeProfileUrl
                OrganizationUnitSettings unitSetting = null;
                if ( review.getSource() != null && !review.getSource().isEmpty()
                    && review.getSource().equals( CommonConstants.SURVEY_SOURCE_ZILLOW ) ) {
                    if ( review.getCompanyId() > 0 ) {
                        unitSetting = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById( review.getCompanyId(),
                            CommonConstants.COMPANY_SETTINGS_COLLECTION );
                    } else if ( review.getRegionId() > 0 ) {
                        unitSetting = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById( review.getRegionId(),
                            CommonConstants.REGION_SETTINGS_COLLECTION );
                    } else if ( review.getBranchId() > 0 ) {
                        unitSetting = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById( review.getBranchId(),
                            CommonConstants.BRANCH_SETTINGS_COLLECTION );
                    } else if ( review.getAgentId() > 0 ) {
                        unitSetting = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById( review.getAgentId(),
                            CommonConstants.AGENT_SETTINGS_COLLECTION );
                    } else {
                        throw new InvalidInputException( "The zillow review with ID : " + review.get_id()
                            + "does not have any hierarchy ID set" );
                    }
                } else {
                    unitSetting = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById( review.getAgentId(),
                        CommonConstants.AGENT_SETTINGS_COLLECTION );
                }
                if ( unitSetting != null ) {
                    profileUrl = (String) unitSetting.getProfileUrl();
                    review.setCompleteProfileUrl( baseProfileUrl + profileUrl );
                    review.setGoogleApi( googleApiKey );
                    review.setFaceBookShareUrl( facebookShareUrl );
                } else {
                    throw new InvalidInputException( "An agent with ID : " + review.getAgentId() + " does not exist" );
                }

                /*OrganizationUnitSettings agentSettings = organizationUnitSettingsDao.fetchAgentSettingsById( review
                    .getAgentId() );*/
                if ( unitSetting != null && unitSetting.getSocialMediaTokens() != null ) {
                    SocialMediaTokens mediaTokens = unitSetting.getSocialMediaTokens();

                    // adding yelpUrl
                    if ( mediaTokens.getYelpToken() != null && mediaTokens.getYelpToken().getYelpPageLink() != null ) {
                        review.setYelpProfileUrl( mediaTokens.getYelpToken().getYelpPageLink() );
                    }

                    // adding zillowUrl
                    if ( mediaTokens.getZillowToken() != null && mediaTokens.getZillowToken().getZillowProfileLink() != null ) {
                        review.setZillowProfileUrl( mediaTokens.getZillowToken().getZillowProfileLink() );
                    }

                    // adding lendingTreeUrl
                    if ( mediaTokens.getLendingTreeToken() != null
                        && mediaTokens.getLendingTreeToken().getLendingTreeProfileLink() != null ) {
                        review.setLendingTreeProfileUrl( mediaTokens.getLendingTreeToken().getLendingTreeProfileLink() );
                    }
                    if ( mediaTokens.getRealtorToken() != null && mediaTokens.getRealtorToken().getRealtorProfileLink() != null ) {
                        review.setRealtorProfileUrl( mediaTokens.getRealtorToken().getRealtorProfileLink() );
                    }
                }
            }
        }
    }


    List<CompanyPositions> sortCompanyPositions( List<CompanyPositions> positions )
    {
        LOG.debug( "Sorting company positions" );
        if ( positions != null && positions.size() > 0 ) {
            Collections.sort( positions );
        }
        return positions;
    }


    Set<Long> getAgentIdsByProfileLevel( String profileLevel, long iden ) throws InvalidInputException
    {
        if ( profileLevel == null || profileLevel.isEmpty() ) {
            throw new InvalidInputException( "profile level is null or empty while getting agents" );
        }
        Set<Long> userIds = new HashSet<>();
        switch ( profileLevel ) {
            case CommonConstants.PROFILE_LEVEL_REGION:
                userIds = userProfileDao.findUserIdsByRegion( iden );
                return userIds;
            case CommonConstants.PROFILE_LEVEL_BRANCH:
                userIds = userProfileDao.findUserIdsByBranch( iden );
                return userIds;
            case CommonConstants.PROFILE_LEVEL_INDIVIDUAL:
                userIds.add( iden );
                return userIds;
            default:
                throw new InvalidInputException( "Invalid profile level while getting iden column name" );
        }
    }


    @Override
    @Transactional
    public String aggregateDisclaimer( OrganizationUnitSettings unitSettings, String entity ) throws InvalidInputException
    {
        LOG.info( "Method aggregateDisclaimer() called from ProfileManagementService" );
        String disclaimer = "";

        if ( unitSettings.getDisclaimer() != null && !unitSettings.getDisclaimer().isEmpty() ) {
            return unitSettings.getDisclaimer();
        }

        OrganizationUnitSettings entitySetting = null;
        if ( entity.equals( CommonConstants.AGENT_ID ) ) {
            User user = userManagementService.getUserByUserId( unitSettings.getIden() );

            for ( UserProfile userProfile : user.getUserProfiles() ) {
                if ( userProfile.getProfilesMaster().getProfileId() == CommonConstants.PROFILES_MASTER_BRANCH_ADMIN_PROFILE_ID ) {
                    if ( userProfile.getBranchId() > 0l ) {
                        entitySetting = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById(
                            userProfile.getBranchId(), MongoOrganizationUnitSettingDaoImpl.BRANCH_SETTINGS_COLLECTION );
                    } else {
                        LOG.warn( "Not a valid branch id for branch profile: " + userProfile + ". Skipping the record" );
                    }
                }
                if ( userProfile.getProfilesMaster().getProfileId() == CommonConstants.PROFILES_MASTER_REGION_ADMIN_PROFILE_ID ) {
                    if ( userProfile.getRegionId() > 0l ) {
                        entitySetting = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById(
                            userProfile.getRegionId(), MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION );
                    } else {
                        LOG.warn( "Not a valid region id for region profile: " + userProfile + ". Skipping the record" );
                    }
                }
                if ( userProfile.getProfilesMaster().getProfileId() == CommonConstants.PROFILES_MASTER_COMPANY_ADMIN_PROFILE_ID ) {
                    if ( userProfile.getRegionId() > 0l ) {
                        entitySetting = organizationManagementService.getCompanySettings( user );
                    } else {
                        LOG.warn( "Not a valid company" );
                    }
                }

                if ( entitySetting != null && entitySetting.getDisclaimer() != null && !entitySetting.getDisclaimer().isEmpty() ) {
                    return entitySetting.getDisclaimer();
                }
                entitySetting = null;
            }

            if ( disclaimer.isEmpty() ) {
                entitySetting = organizationManagementService.getCompanySettings( user );
                disclaimer = entitySetting.getDisclaimer();
            }
        } else if ( entity.equals( CommonConstants.BRANCH_ID ) ) {
            Branch branch = branchDao.findById( Branch.class, unitSettings.getIden() );

            // check for region
            entitySetting = organizationManagementService.getRegionSettings( branch.getRegion().getRegionId() );
            if ( entitySetting != null && entitySetting.getDisclaimer() != null && !entitySetting.getDisclaimer().isEmpty() ) {
                return entitySetting.getDisclaimer();
            }

            // check for company
            entitySetting = organizationManagementService.getCompanySettings( branch.getCompany().getCompanyId() );
            if ( entitySetting != null && entitySetting.getDisclaimer() != null && !entitySetting.getDisclaimer().isEmpty() ) {
                return entitySetting.getDisclaimer();
            }
        } else if ( entity.equals( CommonConstants.REGION_ID ) ) {
            Region region = regionDao.findById( Region.class, unitSettings.getIden() );

            // check for company
            entitySetting = organizationManagementService.getCompanySettings( region.getCompany().getCompanyId() );
            if ( entitySetting != null && entitySetting.getDisclaimer() != null && !entitySetting.getDisclaimer().isEmpty() ) {
                return entitySetting.getDisclaimer();
            }
        }

        LOG.info( "Method aggregateDisclaimer() called from ProfileManagementService" );
        return disclaimer;
    }


    @Override
    @Transactional
    public List<AgentRankingReport> getAgentReport( long iden, String columnName, Date startDate, Date endDate, Object object )
        throws InvalidInputException
    {
        LOG.info( "Method to get Agent's Report for a specific time and all time started." );
        if ( columnName == null || columnName.isEmpty() ) {
            throw new InvalidInputException( "Null/Empty value passed for profile level." );
        }
        if ( iden < 0 ) {
            throw new InvalidInputException( "Invalid value passed for iden of profile level." );
        }
        Map<Long, AgentRankingReport> agentReportData = new HashMap<>();
        // Generate entries for all active users in the company
        initializeAgentReportData( agentReportData, columnName, iden );
        surveyDetailsDao.getAverageScore( startDate, endDate, agentReportData, columnName, iden, false );
        surveyDetailsDao.getCompletedSurveysCount( startDate, endDate, agentReportData, columnName, iden, false );
        // FIX for JIRA: SS-1112: BOC
        surveyPreInitiationDao.getIncompleteSurveysCount( startDate, endDate, agentReportData );
        // FIX for JIRA: SS-1112: EOC
        organizationUnitSettingsDao.setAgentDetails( agentReportData );

        LOG.info( "Method to get Agent's Report for a specific time and all time finished." );
        return new ArrayList<>( agentReportData.values() );
    }


    /**
     * Method to initialize agent report data(to include all active agent in the company)
     * @param agentReportData
     * @param columnName
     * @param iden
     */
    void initializeAgentReportData( Map<Long, AgentRankingReport> agentReportData, String columnName, long iden )
    {
        Set<Long> agentIds = new HashSet<Long>();
        switch ( columnName ) {
            case CommonConstants.COMPANY_ID_COLUMN:
                Company company = companyDao.findById( Company.class, iden );
                agentIds = usersDao.getActiveUserIdsForCompany( company );
                break;
            case CommonConstants.REGION_ID_COLUMN:
                agentIds = userProfileDao.findUserIdsByRegion( iden );
                break;
            case CommonConstants.BRANCH_ID_COLUMN:
                agentIds = userProfileDao.findUserIdsByBranch( iden );
                break;
        }
        for ( Long agentId : agentIds ) {
            agentReportData.put( agentId, new AgentRankingReport() );
        }
    }


    @Override
    @Transactional
    public List<BreadCrumb> getIndividualsBreadCrumb( Long userId ) throws InvalidInputException, NoRecordsFetchedException,
        ProfileNotFoundException
    {
        User user = userDao.findById( User.class, userId );

        List<UserProfile> userProfiles = user.getUserProfiles();
        UserProfile userProfile = null;
        for ( UserProfile element : userProfiles ) {
            if ( element.getIsPrimary() == CommonConstants.IS_PRIMARY_TRUE ) {
                userProfile = element;
                break;
            }
        }
        if ( userProfile == null ) {
            throw new ProfileNotFoundException( "No records found  " );
        }

        Company company = userProfile.getCompany();
        AccountType accountType = AccountType.getAccountType( company.getLicenseDetails().get( 0 ).getAccountsMaster()
            .getAccountsMasterId() );

        LOG.info( "Method getIndividualsBreadCrumb called :" );
        List<BreadCrumb> breadCrumbList = new ArrayList<>();

        switch ( accountType.getValue() ) {
            case CommonConstants.ACCOUNTS_MASTER_INDIVIDUAL:
                updateCrumbListWithVerticalName( breadCrumbList, company );
                break;
            case CommonConstants.ACCOUNTS_MASTER_TEAM:
                updateCrumbListWithCompanyName( breadCrumbList, company );
                updateCrumbListWithVerticalName( breadCrumbList, company );
                break;
            case CommonConstants.ACCOUNTS_MASTER_COMPANY:
                Branch compBranch = branchDao.findById( Branch.class, userProfile.getBranchId() );
                updateCrumbListWithBranchName( breadCrumbList, compBranch );

                updateCrumbListWithCompanyName( breadCrumbList, company );
                updateCrumbListWithVerticalName( breadCrumbList, company );
                break;
            case CommonConstants.ACCOUNTS_MASTER_ENTERPRISE:
                Branch branch = branchDao.findById( Branch.class, userProfile.getBranchId() );
                updateCrumbListWithBranchName( breadCrumbList, branch );

                //JIRA SS-1337
                if ( branch == null ) {
                    throw new InvalidInputException( "No branch with ID : " + userProfile.getBranchId() + " was found" );
                } else if ( branch.getRegion() == null ) {
                    throw new InvalidInputException( "No region associated to branch with ID : " + userProfile.getBranchId()
                        + " was found" );
                }
                Region region = branch.getRegion();
                //Region region = regionDao.findById( Region.class, userProfile.getRegionId() );
                updateCrumbListWithRegionName( breadCrumbList, region );

                updateCrumbListWithCompanyName( breadCrumbList, company );
                updateCrumbListWithVerticalName( breadCrumbList, company );
                break;
            default:
                throw new InvalidInputException( "Invalid account type detected" );
        }

        Collections.reverse( breadCrumbList );
        LOG.info( "Method getIndividualsBreadCrumb finished :" );
        return breadCrumbList;
    }


    @Override
    @Transactional
    public List<BreadCrumb> getRegionsBreadCrumb( OrganizationUnitSettings regionProfile ) throws InvalidInputException,
        NoRecordsFetchedException
    {
        LOG.info( "Method getRegionsBreadCrumb called :" );
        List<BreadCrumb> breadCrumbList = new ArrayList<>();

        Region region = regionDao.findById( Region.class, regionProfile.getIden() );
        Company company = region.getCompany();
        updateCrumbListWithCompanyName( breadCrumbList, company );
        updateCrumbListWithVerticalName( breadCrumbList, company );

        Collections.reverse( breadCrumbList );
        LOG.info( "Method getRegionsBreadCrumb finished :" );
        return breadCrumbList;
    }


    @Override
    @Transactional
    public List<BreadCrumb> getBranchsBreadCrumb( OrganizationUnitSettings branchProfile ) throws InvalidInputException,
        NoRecordsFetchedException
    {
        LOG.info( "Method getBranchsBreadCrumb called :" );
        List<BreadCrumb> breadCrumbList = new ArrayList<>();

        Branch branch = branchDao.findById( Branch.class, branchProfile.getIden() );

        Region region = branch.getRegion();
        updateCrumbListWithRegionName( breadCrumbList, region );

        Company company = branch.getCompany();
        updateCrumbListWithCompanyName( breadCrumbList, company );
        updateCrumbListWithVerticalName( breadCrumbList, company );

        Collections.reverse( breadCrumbList );
        LOG.info( "Method getBranchsBreadCrumb finished :" );
        return breadCrumbList;
    }


    void updateCrumbListWithCompanyName( List<BreadCrumb> breadCrumbList, Company company ) throws InvalidInputException
    {
        BreadCrumb breadCrumb = new BreadCrumb();
        breadCrumb.setBreadCrumbProfile( company.getCompany() );
        breadCrumb.setBreadCrumbUrl( organizationManagementService.getCompanySettings( company.getCompanyId() )
            .getCompleteProfileUrl() );
        breadCrumbList.add( breadCrumb );
    }


    void updateCrumbListWithBranchName( List<BreadCrumb> breadCrumbList, Branch branch ) throws InvalidInputException,
        NoRecordsFetchedException
    {
        if ( branch.getIsDefaultBySystem() != CommonConstants.IS_DEFAULT_BY_SYSTEM_YES ) {
            BreadCrumb breadCrumb = new BreadCrumb();
            breadCrumb.setBreadCrumbProfile( branch.getBranch() );
            breadCrumb.setBreadCrumbUrl( organizationManagementService.getBranchSettings( branch.getBranchId() )
                .getOrganizationUnitSettings().getCompleteProfileUrl() );
            breadCrumbList.add( breadCrumb );
        }
    }


    void updateCrumbListWithRegionName( List<BreadCrumb> breadCrumbList, Region region ) throws InvalidInputException
    {
        if ( region.getIsDefaultBySystem() != CommonConstants.IS_DEFAULT_BY_SYSTEM_YES ) {
            BreadCrumb breadCrumb = new BreadCrumb();
            breadCrumb.setBreadCrumbProfile( region.getRegion() );
            breadCrumb.setBreadCrumbUrl( organizationManagementService.getRegionSettings( region.getRegionId() )
                .getCompleteProfileUrl() );
            breadCrumbList.add( breadCrumb );
        }
    }


    void updateCrumbListWithVerticalName( List<BreadCrumb> breadCrumbList, Company company )
    {
        BreadCrumb breadCrumb = new BreadCrumb();
        breadCrumb.setBreadCrumbProfile( company.getVerticalsMaster().getVerticalName() );
        breadCrumbList.add( breadCrumb );
    }


    @Override
    @Transactional
    public List<OrganizationUnitSettings> getCompanyList( String verticalName ) throws InvalidInputException,
        ProfileNotFoundException
    {
        LOG.info( "Method getCompanyList called :" );
        List<OrganizationUnitSettings> companyList = organizationUnitSettingsDao.getCompanyListByVerticalName( verticalName );
        LOG.info( "Method getCompanyList finished :" );
        return companyList;
    }


    @Override
    @Transactional
    public void updateCompanyName( long userId, long companyId, String companyName ) throws InvalidInputException
    {
        LOG.info( "Method updateCompanyName of profileManagementService called for companyId : " + companyId );

        Company company = companyDao.findById( Company.class, companyId );
        if ( company == null ) {
            throw new InvalidInputException( "No company present for the specified companyId" );
        }
        company.setCompany( companyName );
        company.setModifiedBy( String.valueOf( userId ) );
        company.setModifiedOn( new Timestamp( System.currentTimeMillis() ) );
        companyDao.update( company );

        LOG.info( "Successfully completed method to update company name" );
    }


    @Override
    @Transactional
    public void updateRegionName( long userId, long regionId, String regionName ) throws InvalidInputException
    {
        LOG.info( "Method updateRegionName of profileManagementService called for regionId : " + regionId );

        Region region = regionDao.findById( Region.class, regionId );
        if ( region == null ) {
            throw new InvalidInputException( "No region present for the specified regionId" );
        }
        region.setRegion( regionName );
        region.setModifiedBy( String.valueOf( userId ) );
        region.setModifiedOn( new Timestamp( System.currentTimeMillis() ) );
        regionDao.update( region );

        LOG.info( "Successfully completed method to update region name" );
    }


    @Override
    @Transactional
    public void updateBranchName( long userId, long branchId, String branchName ) throws InvalidInputException
    {
        LOG.info( "Method updateBranchName of profileManagementService called for branchId : " + branchId );

        Branch branch = branchDao.findById( Branch.class, branchId );
        if ( branch == null ) {
            throw new InvalidInputException( "No branch present for the specified branchId" );
        }
        branch.setBranch( branchName );
        branch.setModifiedBy( String.valueOf( userId ) );
        branch.setModifiedOn( new Timestamp( System.currentTimeMillis() ) );
        branchDao.update( branch );

        LOG.info( "Successfully completed method to update branch name" );
    }


    @Override
    @Transactional
    public void updateIndividualName( long userId, long individualId, String individualName ) throws InvalidInputException
    {
        LOG.info( "Method updateIndividualName of profileManagementService called for individualId : " + individualId );

        User user = userDao.findById( User.class, individualId );
        if ( user == null ) {
            throw new InvalidInputException( "No user present for the specified individualId" );
        }
        String nameArray[] = null;
        if ( individualName != null && !individualName.equalsIgnoreCase( "" ) ) {
            nameArray = individualName.split( " " );
        }

        if ( nameArray == null ) {
            throw new InvalidInputException( "Invalid name, please provide a valid name " );
        }

        user.setFirstName( nameArray[0] );
        String lastName = "";
        if ( nameArray.length > 1 ) {
            for ( int i = 1; i <= nameArray.length - 1; i++ ) {
                lastName += nameArray[i] + " ";
            }
        }
        if ( lastName != null && !lastName.equalsIgnoreCase( "" ) ) {
            lastName = lastName.trim();
            user.setLastName( lastName );
        } else {
            // Fix for SS-1442 : Last name is not updated to blank when updated agent name contains only first name
            user.setLastName( "" );
        }
        user.setModifiedBy( String.valueOf( userId ) );
        user.setModifiedOn( new Timestamp( System.currentTimeMillis() ) );
        userDao.update( user );

        LOG.info( "Successfully completed method to update individual name" );
    }


    @Override
    @Transactional
    public void updateCompanyEmail( long companyId, String emailId ) throws NonFatalException
    {
        LOG.info( "Method updateCompanyEmail of profileManagementService called for companyId : " + companyId );

        Company company = companyDao.findById( Company.class, companyId );
        if ( company == null ) {
            throw new InvalidInputException( "No company present for the specified companyId" );
        }

        User companyAdmin = null;
        // fetch company admin from users table
        Map<String, Object> queries = new HashMap<>();
        queries.put( CommonConstants.IS_OWNER_COLUMN, CommonConstants.IS_OWNER );
        queries.put( CommonConstants.COMPANY_COLUMN, company );
        List<User> users = userDao.findByKeyValue( User.class, queries );
        if ( users != null && users.size() > 0 ) {
            companyAdmin = users.get( CommonConstants.INITIAL_INDEX );
        }
        if ( companyAdmin != null ) {
            // Fix for JIRA: SS-1198: Updating of email addres should change the login id as well:
            // BEGIN
            companyAdmin.setLoginName( emailId );
            // Fix for JIRA: SS-1198: Updating of email addres should change the login id as well:
            // END
            companyAdmin.setEmailId( emailId );
            companyAdmin.setModifiedBy( String.valueOf( companyAdmin.getUserId() ) );
            companyAdmin.setModifiedOn( new Timestamp( System.currentTimeMillis() ) );
            userDao.update( companyAdmin );

            for ( UserProfile userProfile : companyAdmin.getUserProfiles() ) {
                userProfile.setEmailId( emailId );
                userProfile.setModifiedBy( String.valueOf( companyAdmin.getUserId() ) );
                userProfile.setModifiedOn( new Timestamp( System.currentTimeMillis() ) );
                userProfileDao.update( userProfile );
            }

            company.setModifiedBy( String.valueOf( companyAdmin.getUserId() ) );
            company.setModifiedOn( new Timestamp( System.currentTimeMillis() ) );
            companyDao.update( company );

            // Fix for JIRA: SS-1358 - Updating email address should update SOLR records as well
            // BEGIN
            updateEmailIdInSolr( emailId, companyAdmin.getUserId() );
            // Fix for JIRA: SS-1358 - Updating email address should update SOLR records as well
            // END

            LOG.info( "Successfully completed method to update company email" );
        } else {
            LOG.error( "Could not find the owner of the company" );
        }
    }


    @Override
    @Transactional
    public void updateIndividualEmail( long userId, String emailId ) throws InvalidInputException
    {
        LOG.info( "Method updateIndividualEmail of profileManagementService called for userId : " + userId );

        User user = userDao.findById( User.class, userId );
        if ( user == null ) {
            throw new InvalidInputException( "No user present for the specified userId" );
        }
        user.setEmailId( emailId );
        user.setModifiedBy( String.valueOf( userId ) );
        user.setModifiedOn( new Timestamp( System.currentTimeMillis() ) );
        userDao.update( user );

        for ( UserProfile userProfile : user.getUserProfiles() ) {
            userProfile.setEmailId( emailId );
            userProfile.setModifiedBy( String.valueOf( userId ) );
            userProfile.setModifiedOn( new Timestamp( System.currentTimeMillis() ) );
            userProfileDao.update( userProfile );
        }

        LOG.info( "Successfully completed method to update individual email" );
    }


    @Override
    public Map<String, String> findNamesfromProfileName( String profileName )
    {
        String nameArray[] = null;
        List<String> list = new ArrayList<String>();
        Map<String, String> map = new HashMap<String, String>();
        String firstName = "";
        String lastName = "";
        if ( profileName.contains( "-" ) ) {
            nameArray = profileName.split( "-" );
            for ( String name : nameArray ) {
                list.add( name );
            }
            if ( isNumeric( list.get( list.size() - 1 ) ) ) {
                list.remove( list.size() - 1 );
            }
        }
        if ( list.size() < 2 ) {
            firstName = profileName;

        } else if ( list.size() == 2 ) {
            firstName = list.get( 0 );
            lastName = list.get( 1 );
        } else {
            for ( int i = 0; i < list.size() - 1; i++ ) {
                firstName = firstName + list.get( i ) + " ";
            }
            firstName = firstName.trim();
            lastName = list.get( list.size() - 1 );
        }

        map.put( CommonConstants.PATTERN_FIRST, firstName );
        map.put( CommonConstants.PATTERN_LAST, lastName );
        return map;
    }


    static boolean isNumeric( String str )
    {
        try {
            Double.parseDouble( str );
        } catch ( NumberFormatException nfe ) {
            return false;
        }
        return true;
    }


    @Override
    public OrganizationUnitSettings aggregateAgentDetails( User user, OrganizationUnitSettings profileSettings,
        LockSettings parentLockSettings ) throws InvalidInputException, NoRecordsFetchedException
    {
        LOG.debug( "Method aggregateAgentDetails() called from ProfileManagementService" );
        if ( profileSettings == null ) {
            throw new InvalidInputException( "No aggregated Settings found" );
        }

        String logoUrl = "";
        OrganizationUnitSettings entitySettings = null;
        ContactDetailsSettings contactDetails = null;
        AgentSettings agentSettings = null;
        if ( profileSettings instanceof AgentSettings ) {
            agentSettings = (AgentSettings) profileSettings;
        }

        // checking all assigned branches for address
        for ( UserProfile userProfile : user.getUserProfiles() ) {
            if ( userProfile.getProfilesMaster().getProfileId() == CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID
                && userProfile.getStatus() == CommonConstants.STATUS_ACTIVE ) {
                // get the branch profile if it is not present in the branch settings
                if ( userProfile.getBranchId() > 0l ) {
                    Branch branch = userManagementService.getBranchById( userProfile.getBranchId() );
                    if ( branch.getIsDefaultBySystem() == CommonConstants.IS_DEFAULT_BY_SYSTEM_YES )
                        break;
                    entitySettings = organizationManagementService.getBranchSettingsDefault( userProfile.getBranchId() );
                    contactDetails = entitySettings.getContact_details();
                    if ( contactDetails != null && contactDetails.getAddress1() != null ) {
                        if ( !parentLockSettings.getIsLogoLocked() && entitySettings.getLogoThumbnail() != null
                            && !entitySettings.getLogoThumbnail().isEmpty() ) {
                            logoUrl = entitySettings.getLogoThumbnail();
                        }
                        break;
                    }
                }
            }
        }

        // check logo url in region of branch
        if ( !parentLockSettings.getIsLogoLocked() && entitySettings != null && contactDetails != null ) {
            if ( logoUrl == null || logoUrl.isEmpty() ) {
                Branch branch = branchDao.findById( Branch.class, entitySettings.getIden() );
                if ( branch.getRegion().getIsDefaultBySystem() == CommonConstants.IS_DEFAULT_BY_SYSTEM_NO ) {
                    OrganizationUnitSettings regionSettings = organizationManagementService
                        .getRegionSettings( branch.getRegion().getRegionId() );
                    if ( regionSettings.getLogoThumbnail() != null && !regionSettings.getLogoThumbnail().isEmpty() ) {
                        logoUrl = regionSettings.getLogoThumbnail();
                    }
                }
            }
        }

        // checking all company for address if null
        if ( contactDetails == null ) {
            entitySettings = organizationManagementService.getCompanySettings( user );
            contactDetails = entitySettings.getContact_details();
        }
        if ( !parentLockSettings.getIsLogoLocked() && ( logoUrl == null || logoUrl.isEmpty() ) ) {
            OrganizationUnitSettings companySettings = organizationManagementService.getCompanySettings( user );
            //JIRA SS-1363 begin
            /*if ( companySettings.getLogoThumbnail() != null && !companySettings.getLogoThumbnail().isEmpty() ) {
                logoUrl = companySettings.getLogoThumbnail();
            }*/
            if ( companySettings.getLogo() != null && !companySettings.getLogo().isEmpty() ) {
                logoUrl = companySettings.getLogo();
            }
            //JIRA SS-1363 end
        }

        // add the company profile data into agent settings
        CompanyProfileData companyProfileData = new CompanyProfileData();
        companyProfileData.setName( contactDetails.getName() );
        companyProfileData.setAddress1( contactDetails.getAddress1() );
        companyProfileData.setAddress2( contactDetails.getAddress2() );
        companyProfileData.setCity( contactDetails.getCity() );
        companyProfileData.setState( contactDetails.getState() );
        companyProfileData.setCountry( contactDetails.getCountry() );
        companyProfileData.setCountryCode( contactDetails.getCountryCode() );
        companyProfileData.setZipcode( contactDetails.getZipcode() );
        companyProfileData.setCompanyLogo( logoUrl );

        if ( agentSettings != null ) {
            if ( !parentLockSettings.getIsLogoLocked() && logoUrl != null && !logoUrl.isEmpty() ) {
                agentSettings.setLogo( logoUrl );
            }
            agentSettings.setCompanyProfileData( companyProfileData );
        }

        LOG.debug( "Method aggregateAgentDetails() finished from ProfileManagementService" );
        return ( agentSettings != null ? agentSettings : profileSettings );
    }


    @Override
    public void addOrUpdateAgentPositions( List<CompanyPositions> companyPositions, AgentSettings agentSettings )
    {
        LOG.debug( "Method addOrUpdateAgentPositions() called to update agent positions" );

        organizationUnitSettingsDao.updateParticularKeyAgentSettings( MongoOrganizationUnitSettingDaoImpl.KEY_POSTIONS,
            companyPositions, agentSettings );
    }


    @Override
    @Transactional
    public Map<String, Long> getHierarchyDetailsByEntity( String entityType, long entityId ) throws InvalidInputException,
        ProfileNotFoundException
    {
        Map<String, Long> hierarchyDetials = new HashMap<String, Long>();
        Map<String, Long> hierarchyMap = new HashMap<String, Long>();
        long companyId = 0;
        long regionId = 0;
        long branchId = 0;
        long agentId = 0;

        if ( entityType.equalsIgnoreCase( CommonConstants.COMPANY_ID ) ) {
            companyId = entityId;
        } else if ( entityType.equalsIgnoreCase( CommonConstants.REGION_ID ) ) {
            Company company = organizationManagementService.getPrimaryCompanyByRegion( entityId );
            if ( company == null ) {
                throw new InvalidInputException( "Company not found for this region " );
            }
            companyId = company.getCompanyId();
            regionId = entityId;

        } else if ( entityType.equalsIgnoreCase( CommonConstants.BRANCH_ID ) ) {
            Region region = organizationManagementService.getPrimaryRegionByBranch( entityId );
            if ( region == null ) {
                throw new InvalidInputException( "Region not found for this branch " );
            }
            Company company = region.getCompany();
            if ( company == null ) {
                throw new InvalidInputException( "Company not found for this region " );
            }
            companyId = company.getCompanyId();
            regionId = region.getRegionId();
            branchId = entityId;
        } else if ( entityType.equalsIgnoreCase( CommonConstants.AGENT_ID ) ) {
            hierarchyMap = userManagementService.getPrimaryUserProfileByAgentId( entityId );
            agentId = entityId;
            companyId = hierarchyMap.get( CommonConstants.COMPANY_ID_COLUMN );
            regionId = hierarchyMap.get( CommonConstants.REGION_ID_COLUMN );
            branchId = hierarchyMap.get( CommonConstants.BRANCH_ID_COLUMN );
        } else {
            throw new InvalidInputException( "Entity Type Is Invalid " );
        }
        hierarchyDetials.put( CommonConstants.COMPANY_ID_COLUMN, companyId );
        hierarchyDetials.put( CommonConstants.REGION_ID_COLUMN, regionId );
        hierarchyDetials.put( CommonConstants.BRANCH_ID_COLUMN, branchId );
        hierarchyDetials.put( CommonConstants.AGENT_ID_COLUMN, agentId );
        return hierarchyDetials;
    }


    @Override
    @Transactional
    public Map<SettingsForApplication, OrganizationUnit> getPrimaryHierarchyByEntity( String entityType, long entityId )
        throws InvalidInputException, InvalidSettingsStateException, ProfileNotFoundException
    {
        boolean logoLocked = true;
        boolean webAddressLocked = true;
        boolean phoneNumberLocked = true;
        AgentSettings unitSettings = null;
        LOG.info( "Inside method getPrimaryHeirarchyByEntity for entity " + entityType );
        Map<String, Long> hierarchyDetails = getHierarchyDetailsByEntity( entityType, entityId );
        long companyId = hierarchyDetails.get( CommonConstants.COMPANY_ID_COLUMN );
        long regionId = hierarchyDetails.get( CommonConstants.REGION_ID_COLUMN );
        long branchId = hierarchyDetails.get( CommonConstants.BRANCH_ID_COLUMN );
        List<SettingsDetails> settingsDetailsList = settingsManager
            .getScoreForCompleteHeirarchy( companyId, branchId, regionId );

        LOG.info( "Calculate lock and setting score " );
        Map<String, Long> totalScore = settingsManager.calculateSettingsScore( settingsDetailsList );
        long currentLockAggregateValue = totalScore.get( CommonConstants.LOCK_SCORE );
        long currentSetAggregateValue = totalScore.get( CommonConstants.SETTING_SCORE );

        if ( entityType.equalsIgnoreCase( CommonConstants.AGENT_ID ) ) {
            if ( !settingsLocker.isSettingsValueLocked( OrganizationUnit.COMPANY, currentLockAggregateValue,
                SettingsForApplication.LOGO ) ) {
                if ( !settingsLocker.isSettingsValueLocked( OrganizationUnit.REGION, currentLockAggregateValue,
                    SettingsForApplication.LOGO ) ) {
                    if ( !settingsLocker.isSettingsValueLocked( OrganizationUnit.BRANCH, currentLockAggregateValue,
                        SettingsForApplication.LOGO ) ) {
                        logoLocked = false;
                    }
                }
            }
            if ( !settingsLocker.isSettingsValueLocked( OrganizationUnit.COMPANY, currentLockAggregateValue,
                SettingsForApplication.PHONE ) ) {
                if ( !settingsLocker.isSettingsValueLocked( OrganizationUnit.REGION, currentLockAggregateValue,
                    SettingsForApplication.PHONE ) ) {
                    if ( !settingsLocker.isSettingsValueLocked( OrganizationUnit.BRANCH, currentLockAggregateValue,
                        SettingsForApplication.PHONE ) ) {
                        phoneNumberLocked = false;
                    }
                }
            }
            if ( !settingsLocker.isSettingsValueLocked( OrganizationUnit.COMPANY, currentLockAggregateValue,
                SettingsForApplication.WEB_ADDRESS_WORK ) ) {
                if ( !settingsLocker.isSettingsValueLocked( OrganizationUnit.REGION, currentLockAggregateValue,
                    SettingsForApplication.WEB_ADDRESS_WORK ) ) {
                    if ( !settingsLocker.isSettingsValueLocked( OrganizationUnit.BRANCH, currentLockAggregateValue,
                        SettingsForApplication.WEB_ADDRESS_WORK ) ) {
                        webAddressLocked = false;
                    }
                }
            }
            unitSettings = userManagementService.getAgentSettingsForUserProfiles( entityId );
            if ( unitSettings == null ) {
                LOG.error( "unit settings is null" );
            }
        }


        //check if work email is locked by company
        if ( settingsLocker.isSettingsValueLocked( OrganizationUnit.COMPANY, currentLockAggregateValue,
            SettingsForApplication.EMAIL_ID_WORK ) ) {
            //update currentLockAggregateValue
            String sCurrentLockValue = String.valueOf( currentLockAggregateValue );
            if ( sCurrentLockValue.length() >= SettingsForApplication.EMAIL_ID_WORK.getIndex() ) {
                String preIndexLockValueSubString = sCurrentLockValue.substring( 0, sCurrentLockValue.length()
                    - SettingsForApplication.EMAIL_ID_WORK.getIndex() );
                String indexLockValueSubString = String.valueOf( CommonConstants.LOCKED_BY_NONE );
                String postIndexLockValueSubString = sCurrentLockValue.substring( sCurrentLockValue.length()
                    - SettingsForApplication.EMAIL_ID_WORK.getIndex() + 1 );

                sCurrentLockValue = preIndexLockValueSubString + indexLockValueSubString + postIndexLockValueSubString;
                currentLockAggregateValue = Long.parseLong( sCurrentLockValue );

            }

        }


        Map<SettingsForApplication, OrganizationUnit> closestSettings = settingsManager.getClosestSettingLevel(
            String.valueOf( currentSetAggregateValue ), String.valueOf( currentLockAggregateValue ) );


        if ( entityType.equalsIgnoreCase( CommonConstants.AGENT_ID ) ) {
            if ( unitSettings != null ) {
                if ( !logoLocked ) {
                    if ( unitSettings.getLogo() != null && !unitSettings.getLogo().isEmpty() ) {
                        closestSettings.put( SettingsForApplication.LOGO, OrganizationUnit.AGENT );
                    }
                }
                if ( !webAddressLocked ) {
                    if ( unitSettings.getContact_details() != null ) {
                        if ( unitSettings.getContact_details().getWeb_addresses() != null ) {
                            if ( unitSettings.getContact_details().getWeb_addresses().getWork() != null
                                && !unitSettings.getContact_details().getWeb_addresses().getWork().isEmpty() ) {
                                closestSettings.put( SettingsForApplication.WEB_ADDRESS_WORK, OrganizationUnit.AGENT );
                            }
                        }
                    }
                }
                if ( !phoneNumberLocked ) {
                    if ( unitSettings.getContact_details() != null ) {
                        if ( unitSettings.getContact_details().getContact_numbers() != null ) {
                            if ( unitSettings.getContact_details().getContact_numbers().getWork() != null
                                && !unitSettings.getContact_details().getContact_numbers().getWork().isEmpty() ) {
                                closestSettings.put( SettingsForApplication.PHONE, OrganizationUnit.AGENT );
                            }
                        }
                    }
                }

                if ( unitSettings.getContact_details() != null ) {
                    if ( ( unitSettings.getContact_details().getAddress1() != null && !unitSettings.getContact_details()
                        .getAddress1().isEmpty() )
                        || ( unitSettings.getContact_details().getAddress2() != null && !unitSettings.getContact_details()
                            .getAddress2().isEmpty() ) ) {
                        closestSettings.put( SettingsForApplication.ADDRESS, OrganizationUnit.AGENT );
                    }
                }
                if ( unitSettings.getContact_details() != null ) {
                    if ( unitSettings.getContact_details().getLocation() != null
                        && !unitSettings.getContact_details().getLocation().isEmpty() ) {
                        closestSettings.put( SettingsForApplication.LOCATION, OrganizationUnit.AGENT );
                    }
                }

                if ( unitSettings.getContact_details() != null ) {
                    if ( unitSettings.getContact_details().getAbout_me() != null
                        && !unitSettings.getContact_details().getAbout_me().isEmpty() ) {
                        closestSettings.put( SettingsForApplication.ABOUT_ME, OrganizationUnit.AGENT );
                    }
                }

                if ( unitSettings.getContact_details() != null ) {
                    if ( unitSettings.getContact_details().getMail_ids() != null ) {
                        if ( unitSettings.getContact_details().getMail_ids().getWork() != null
                            && !unitSettings.getContact_details().getMail_ids().getWork().isEmpty() ) {
                            closestSettings.put( SettingsForApplication.EMAIL_ID_WORK, OrganizationUnit.AGENT );
                        }
                    }
                }

                if ( unitSettings.getContact_details() != null ) {
                    if ( unitSettings.getContact_details().getMail_ids() != null ) {
                        if ( unitSettings.getContact_details().getMail_ids().getPersonal() != null
                            && !unitSettings.getContact_details().getMail_ids().getPersonal().isEmpty() ) {
                            closestSettings.put( SettingsForApplication.EMAIL_ID_PERSONAL, OrganizationUnit.AGENT );
                        }
                    }
                }

                if ( unitSettings.getSocialMediaTokens() != null ) {
                    if ( unitSettings.getSocialMediaTokens().getFacebookToken() != null ) {
                        closestSettings.put( SettingsForApplication.FACEBOOK, OrganizationUnit.AGENT );
                    }
                }
                if ( unitSettings.getSocialMediaTokens() != null ) {
                    if ( unitSettings.getSocialMediaTokens().getTwitterToken() != null ) {
                        closestSettings.put( SettingsForApplication.TWITTER, OrganizationUnit.AGENT );
                    }
                }
                if ( unitSettings.getSocialMediaTokens() != null ) {
                    if ( unitSettings.getSocialMediaTokens().getLinkedInToken() != null ) {
                        closestSettings.put( SettingsForApplication.LINKED_IN, OrganizationUnit.AGENT );
                    }
                }
                if ( unitSettings.getSocialMediaTokens() != null ) {
                    if ( unitSettings.getSocialMediaTokens().getGoogleToken() != null ) {
                        closestSettings.put( SettingsForApplication.GOOGLE_PLUS, OrganizationUnit.AGENT );
                    }
                }
                if ( unitSettings.getSocialMediaTokens() != null ) {
                    if ( unitSettings.getSocialMediaTokens().getYelpToken() != null ) {
                        closestSettings.put( SettingsForApplication.YELP, OrganizationUnit.AGENT );
                    }
                }
                if ( unitSettings.getSocialMediaTokens() != null ) {
                    if ( unitSettings.getSocialMediaTokens().getZillowToken() != null ) {
                        closestSettings.put( SettingsForApplication.ZILLOW, OrganizationUnit.AGENT );
                    }
                }
                if ( unitSettings.getSocialMediaTokens() != null ) {
                    if ( unitSettings.getSocialMediaTokens().getRealtorToken() != null ) {
                        closestSettings.put( SettingsForApplication.REALTOR, OrganizationUnit.AGENT );
                    }
                }
                if ( unitSettings.getSocialMediaTokens() != null ) {
                    if ( unitSettings.getSocialMediaTokens().getLendingTreeToken() != null ) {
                        closestSettings.put( SettingsForApplication.LENDING_TREE, OrganizationUnit.AGENT );
                    }
                }

            }
        }
        return closestSettings;

    }


    @Override
    @Transactional
    public OrganizationUnitSettings getRegionSettingsByProfileName( String companyProfileName, String regionProfileName )
        throws ProfileNotFoundException, InvalidInputException
    {
        LOG.info( "Method getRegionByProfileName called for companyProfileName:" + companyProfileName
            + " and regionProfileName:" + regionProfileName );
        if ( regionProfileName == null || regionProfileName.isEmpty() ) {
            throw new ProfileNotFoundException( "regionProfileName is null or empty in getRegionByProfileName" );
        }
        /**
         * generate profileUrl and fetch the region by profileUrl since profileUrl for any region is
         * unique, whereas profileName is unique only within a company
         */
        String profileUrl = utils.generateRegionProfileUrl( companyProfileName, regionProfileName );

        OrganizationUnitSettings regionSettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsByProfileUrl(
            profileUrl, MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION );

        return regionSettings;
    }


    @Override
    @Transactional
    public OrganizationUnitSettings getBranchSettingsByProfileName( String companyProfileName, String branchProfileName )
        throws ProfileNotFoundException, InvalidInputException
    {
        LOG.info( "Method getBranchSettingsByProfileName called for companyProfileName:" + companyProfileName
            + " and branchProfileName:" + branchProfileName );

        OrganizationUnitSettings companySettings = getCompanyProfileByProfileName( companyProfileName );
        if ( companySettings == null ) {
            LOG.error( "Unable to fetch company settings, invalid input provided by the user" );
            throw new ProfileNotFoundException( "Unable to get company settings " );
        }

        /**
         * generate profileUrl and fetch the branch by profileUrl since profileUrl for any branch is
         * unique, whereas profileName is unique only within a company
         */
        String profileUrl = utils.generateBranchProfileUrl( companyProfileName, branchProfileName );
        OrganizationUnitSettings branchSettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsByProfileUrl(
            profileUrl, MongoOrganizationUnitSettingDaoImpl.BRANCH_SETTINGS_COLLECTION );

        if ( branchSettings == null ) {
            LOG.error( "Unable to fetch branch settings, invalid input provided by the user" );
            throw new ProfileNotFoundException( "Unable to get branch settings " );
        }

        return branchSettings;
    }


    @Override
    @Transactional
    public OrganizationUnitSettings getRegionProfileByBranch( OrganizationUnitSettings branchSettings )
        throws ProfileNotFoundException
    {

        LOG.debug( "Fetching branch from db to identify the region" );
        Branch branch = branchDao.findById( Branch.class, branchSettings.getIden() );
        if ( branch == null ) {
            LOG.error( "Unable to get branch with this iden " + branchSettings.getIden() );
            throw new ProfileNotFoundException( "Unable to get branch with this iden " + branchSettings.getIden() );

        }

        OrganizationUnitSettings regionSettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById( branch
            .getRegion().getRegionId(), MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION );
        return regionSettings;
    }


    @Override
    public OrganizationUnitSettings fillUnitSettings( OrganizationUnitSettings unitSettings, String currentProfileName,
        OrganizationUnitSettings companyUnitSettings, OrganizationUnitSettings regionUnitSettings,
        OrganizationUnitSettings branchUnitSettings, OrganizationUnitSettings agentUnitSettings,
        Map<SettingsForApplication, OrganizationUnit> map, boolean isFetchRequiredDataFromHierarchy )
    {

        if ( currentProfileName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION ) ) {
            companyUnitSettings = unitSettings;
            return companyUnitSettings;
        } else if ( currentProfileName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION ) ) {
            regionUnitSettings = unitSettings;
            regionUnitSettings = setAggregateBasicData( regionUnitSettings, companyUnitSettings );
            regionUnitSettings = setAggregateProfileData( regionUnitSettings, companyUnitSettings, regionUnitSettings, null,
                null, map, isFetchRequiredDataFromHierarchy );
            return regionUnitSettings;
        } else if ( currentProfileName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.BRANCH_SETTINGS_COLLECTION ) ) {
            branchUnitSettings = setAggregateBasicData( branchUnitSettings, regionUnitSettings );
            branchUnitSettings = setAggregateProfileData( branchUnitSettings, companyUnitSettings, regionUnitSettings,
                branchUnitSettings, null, map, isFetchRequiredDataFromHierarchy );
            return branchUnitSettings;
        } else if ( currentProfileName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION ) ) {
            agentUnitSettings = setAggregateProfileData( agentUnitSettings, companyUnitSettings, regionUnitSettings,
                branchUnitSettings, agentUnitSettings, map, isFetchRequiredDataFromHierarchy );
            return agentUnitSettings;
        } else {
            return null;
        }

    }


    OrganizationUnitSettings setAggregateBasicData( OrganizationUnitSettings userProfile, OrganizationUnitSettings parentProfile )
    {

        if ( userProfile.getContact_details() == null ) {
            userProfile.setContact_details( new ContactDetailsSettings() );
        }
        if ( userProfile.getContact_details().getWeb_addresses() == null ) {
            userProfile.getContact_details().setWeb_addresses( new WebAddressSettings() );
        }
        if ( userProfile.getContact_details().getContact_numbers() == null ) {
            userProfile.getContact_details().setContact_numbers( new ContactNumberSettings() );
        }
        if ( userProfile.getSurvey_settings() == null ) {
            userProfile.setSurvey_settings( parentProfile.getSurvey_settings() );
        }
        return userProfile;
    }


    OrganizationUnitSettings setAggregateProfileData( OrganizationUnitSettings userProfile,
        OrganizationUnitSettings companyUnitSettings, OrganizationUnitSettings regionUnitSettings,
        OrganizationUnitSettings branchUnitSettings, OrganizationUnitSettings agentUnitSettings,
        Map<SettingsForApplication, OrganizationUnit> map, boolean isFetchRequiredDataFromHierarchy )
    {
        //Set logoThumbnail along with logo
        for ( Map.Entry<SettingsForApplication, OrganizationUnit> entry : map.entrySet() ) {
            if ( entry.getKey() == SettingsForApplication.LOGO ) {
                //JIRA SS-1363 begin
                /*if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                    userProfile.setLogo( companyUnitSettings.getLogoThumbnail() );
                    userProfile.setLogoThumbnail( companyUnitSettings.getLogoThumbnail() );
                } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                    userProfile.setLogo( regionUnitSettings.getLogoThumbnail() );
                    userProfile.setLogoThumbnail( regionUnitSettings.getLogoThumbnail() );
                } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                    userProfile.setLogo( branchUnitSettings.getLogoThumbnail() );
                    userProfile.setLogoThumbnail( branchUnitSettings.getLogoThumbnail() );
                } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                    userProfile.setLogo( agentUnitSettings.getLogoThumbnail() );
                    userProfile.setLogoThumbnail( agentUnitSettings.getLogoThumbnail() );
                }*/
                if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                    userProfile.setLogo( companyUnitSettings.getLogo() );
                    userProfile.setLogoThumbnail( companyUnitSettings.getLogoThumbnail() );
                } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                    userProfile.setLogo( regionUnitSettings.getLogo() );
                    userProfile.setLogoThumbnail( regionUnitSettings.getLogoThumbnail() );
                } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                    userProfile.setLogo( branchUnitSettings.getLogo() );
                    userProfile.setLogoThumbnail( branchUnitSettings.getLogoThumbnail() );
                } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                    userProfile.setLogo( agentUnitSettings.getLogo() );
                    userProfile.setLogoThumbnail( agentUnitSettings.getLogoThumbnail() );
                }
                //JIRA SS-1363 end

            } else if ( entry.getKey() == SettingsForApplication.LOCATION ) {
                ContactDetailsSettings contactDetails = userProfile.getContact_details();
                if ( contactDetails == null ) {
                    contactDetails = new ContactDetailsSettings();
                }
                if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                    contactDetails.setLocation( companyUnitSettings.getContact_details().getLocation() );
                } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                    contactDetails.setLocation( regionUnitSettings.getContact_details().getLocation() );
                } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                    contactDetails.setLocation( branchUnitSettings.getContact_details().getLocation() );
                } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                    contactDetails.setLocation( agentUnitSettings.getContact_details().getLocation() );
                }
                userProfile.setContact_details( contactDetails );
            } else if ( entry.getKey() == SettingsForApplication.WEB_ADDRESS_WORK ) {
                ContactDetailsSettings contactDetails = userProfile.getContact_details();
                if ( contactDetails == null ) {
                    contactDetails = new ContactDetailsSettings();
                }
                WebAddressSettings webAddressSettings = contactDetails.getWeb_addresses();
                if ( webAddressSettings == null ) {
                    webAddressSettings = new WebAddressSettings();
                }
                if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                    webAddressSettings.setWork( companyUnitSettings.getContact_details().getWeb_addresses().getWork() );
                } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                    webAddressSettings.setWork( regionUnitSettings.getContact_details().getWeb_addresses().getWork() );
                } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                    webAddressSettings.setWork( branchUnitSettings.getContact_details().getWeb_addresses().getWork() );
                } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                    webAddressSettings.setWork( agentUnitSettings.getContact_details().getWeb_addresses().getWork() );
                }
                contactDetails.setWeb_addresses( webAddressSettings );
                userProfile.setContact_details( contactDetails );
            } else if ( entry.getKey() == SettingsForApplication.WEB_ADDRESS_PERSONAL ) {
                ContactDetailsSettings contactDetails = userProfile.getContact_details();
                if ( contactDetails == null ) {
                    contactDetails = new ContactDetailsSettings();
                }
                WebAddressSettings webAddressSettings = contactDetails.getWeb_addresses();
                if ( webAddressSettings == null ) {
                    webAddressSettings = new WebAddressSettings();
                }
                if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                    webAddressSettings.setPersonal( companyUnitSettings.getContact_details().getWeb_addresses().getPersonal() );
                } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                    webAddressSettings.setPersonal( regionUnitSettings.getContact_details().getWeb_addresses().getPersonal() );
                } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                    webAddressSettings.setPersonal( branchUnitSettings.getContact_details().getWeb_addresses().getPersonal() );
                } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                    webAddressSettings.setPersonal( agentUnitSettings.getContact_details().getWeb_addresses().getPersonal() );
                }
                contactDetails.setWeb_addresses( webAddressSettings );
                userProfile.setContact_details( contactDetails );
            } else if ( entry.getKey() == SettingsForApplication.EMAIL_ID_WORK ) {
                ContactDetailsSettings contactDetails = userProfile.getContact_details();
                if ( contactDetails == null ) {
                    contactDetails = new ContactDetailsSettings();
                }
                MailIdSettings mailIdSettings = contactDetails.getMail_ids();
                if ( mailIdSettings == null ) {
                    mailIdSettings = new MailIdSettings();
                }
                if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                    mailIdSettings.setWork( companyUnitSettings.getContact_details().getMail_ids().getWork() );
                } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                    mailIdSettings.setWork( regionUnitSettings.getContact_details().getMail_ids().getWork() );
                } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                    mailIdSettings.setWork( branchUnitSettings.getContact_details().getMail_ids().getWork() );
                } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                    mailIdSettings.setWork( agentUnitSettings.getContact_details().getMail_ids().getWork() );
                }
                contactDetails.setMail_ids( mailIdSettings );
                userProfile.setContact_details( contactDetails );
            } else if ( entry.getKey() == SettingsForApplication.EMAIL_ID_PERSONAL ) {
                ContactDetailsSettings contactDetails = userProfile.getContact_details();
                if ( contactDetails == null ) {
                    contactDetails = new ContactDetailsSettings();
                }
                MailIdSettings mailIdSettings = contactDetails.getMail_ids();
                if ( mailIdSettings == null ) {
                    mailIdSettings = new MailIdSettings();
                }
                if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                    mailIdSettings.setPersonal( companyUnitSettings.getContact_details().getMail_ids().getPersonal() );
                } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                    mailIdSettings.setPersonal( regionUnitSettings.getContact_details().getMail_ids().getPersonal() );
                } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                    mailIdSettings.setPersonal( branchUnitSettings.getContact_details().getMail_ids().getPersonal() );
                } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                    mailIdSettings.setPersonal( agentUnitSettings.getContact_details().getMail_ids().getPersonal() );
                }
                contactDetails.setMail_ids( mailIdSettings );
                userProfile.setContact_details( contactDetails );
            } else if ( entry.getKey() == SettingsForApplication.ADDRESS ) {
                ContactDetailsSettings contactDetails = userProfile.getContact_details();
                if ( contactDetails == null ) {
                    contactDetails = new ContactDetailsSettings();
                }
                if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                    contactDetails.setAddress( companyUnitSettings.getContact_details().getAddress() );
                    contactDetails.setAddress1( companyUnitSettings.getContact_details().getAddress1() );
                    contactDetails.setAddress2( companyUnitSettings.getContact_details().getAddress2() );
                    contactDetails.setZipcode( companyUnitSettings.getContact_details().getZipcode() );
                    contactDetails.setState( companyUnitSettings.getContact_details().getState() );
                    contactDetails.setCity( companyUnitSettings.getContact_details().getCity() );
                    contactDetails.setCountry( companyUnitSettings.getContact_details().getCountry() );
                    contactDetails.setCountryCode( companyUnitSettings.getContact_details().getCountryCode() );
                } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                    contactDetails.setAddress( regionUnitSettings.getContact_details().getAddress() );
                    contactDetails.setAddress1( regionUnitSettings.getContact_details().getAddress1() );
                    contactDetails.setAddress2( regionUnitSettings.getContact_details().getAddress2() );
                    contactDetails.setZipcode( regionUnitSettings.getContact_details().getZipcode() );
                    contactDetails.setState( regionUnitSettings.getContact_details().getState() );
                    contactDetails.setCity( regionUnitSettings.getContact_details().getCity() );
                    contactDetails.setCountry( regionUnitSettings.getContact_details().getCountry() );
                    contactDetails.setCountryCode( regionUnitSettings.getContact_details().getCountryCode() );

                } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                    contactDetails.setAddress( branchUnitSettings.getContact_details().getAddress() );
                    contactDetails.setAddress1( branchUnitSettings.getContact_details().getAddress1() );
                    contactDetails.setAddress2( branchUnitSettings.getContact_details().getAddress2() );
                    contactDetails.setZipcode( branchUnitSettings.getContact_details().getZipcode() );
                    contactDetails.setState( branchUnitSettings.getContact_details().getState() );
                    contactDetails.setCity( branchUnitSettings.getContact_details().getCity() );
                    contactDetails.setCountry( branchUnitSettings.getContact_details().getCountry() );
                    contactDetails.setCountryCode( branchUnitSettings.getContact_details().getCountryCode() );
                } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                    contactDetails.setAddress( agentUnitSettings.getContact_details().getAddress() );
                    contactDetails.setAddress1( agentUnitSettings.getContact_details().getAddress1() );
                    contactDetails.setAddress2( agentUnitSettings.getContact_details().getAddress2() );
                    contactDetails.setZipcode( agentUnitSettings.getContact_details().getZipcode() );
                    contactDetails.setState( agentUnitSettings.getContact_details().getState() );
                    contactDetails.setCity( agentUnitSettings.getContact_details().getCity() );
                    contactDetails.setCountry( agentUnitSettings.getContact_details().getCountry() );
                    contactDetails.setCountryCode( agentUnitSettings.getContact_details().getCountryCode() );
                }
                userProfile.setContact_details( contactDetails );
            } else if ( entry.getKey() == SettingsForApplication.PHONE ) {
                ContactDetailsSettings contactDetails = userProfile.getContact_details();
                if ( contactDetails == null ) {
                    contactDetails = new ContactDetailsSettings();
                }
                ContactNumberSettings contactNumberSettings = contactDetails.getContact_numbers();
                if ( contactNumberSettings == null ) {
                    contactNumberSettings = new ContactNumberSettings();
                }
                if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                    contactNumberSettings.setWork( companyUnitSettings.getContact_details().getContact_numbers().getWork() );
                } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                    contactNumberSettings.setWork( regionUnitSettings.getContact_details().getContact_numbers().getWork() );
                } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                    contactNumberSettings.setWork( branchUnitSettings.getContact_details().getContact_numbers().getWork() );
                } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                    contactNumberSettings.setWork( agentUnitSettings.getContact_details().getContact_numbers().getWork() );
                }
                contactDetails.setContact_numbers( contactNumberSettings );
                userProfile.setContact_details( contactDetails );
            } else if ( entry.getKey() == SettingsForApplication.FACEBOOK ) {
                SocialMediaTokens socialMediaTokens = userProfile.getSocialMediaTokens();
                if ( socialMediaTokens == null ) {
                    socialMediaTokens = new SocialMediaTokens();
                }
                //get facebook token from upper hierarchy in case of public profile page.
                if ( isFetchRequiredDataFromHierarchy ) {
                    if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                        socialMediaTokens.setFacebookToken( companyUnitSettings.getSocialMediaTokens().getFacebookToken() );
                    } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                        socialMediaTokens.setFacebookToken( regionUnitSettings.getSocialMediaTokens().getFacebookToken() );
                    } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                        socialMediaTokens.setFacebookToken( branchUnitSettings.getSocialMediaTokens().getFacebookToken() );
                    } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                        socialMediaTokens.setFacebookToken( agentUnitSettings.getSocialMediaTokens().getFacebookToken() );
                    }
                }
                userProfile.setSocialMediaTokens( socialMediaTokens );
            } else if ( entry.getKey() == SettingsForApplication.GOOGLE_PLUS ) {
                SocialMediaTokens socialMediaTokens = userProfile.getSocialMediaTokens();
                if ( socialMediaTokens == null ) {
                    socialMediaTokens = new SocialMediaTokens();
                }
                //get google plus token from upper hierarchy in case of public profile page.
                if ( isFetchRequiredDataFromHierarchy ) {
                    if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                        socialMediaTokens.setGoogleToken( companyUnitSettings.getSocialMediaTokens().getGoogleToken() );
                    } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                        socialMediaTokens.setGoogleToken( regionUnitSettings.getSocialMediaTokens().getGoogleToken() );
                    } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                        socialMediaTokens.setGoogleToken( branchUnitSettings.getSocialMediaTokens().getGoogleToken() );
                    } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                        socialMediaTokens.setGoogleToken( agentUnitSettings.getSocialMediaTokens().getGoogleToken() );
                    }
                }
                userProfile.setSocialMediaTokens( socialMediaTokens );
            } else if ( entry.getKey() == SettingsForApplication.TWITTER ) {
                SocialMediaTokens socialMediaTokens = userProfile.getSocialMediaTokens();
                if ( socialMediaTokens == null ) {
                    socialMediaTokens = new SocialMediaTokens();
                }
                //get twitter token from upper hierarchy in case of public profile page.
                if ( isFetchRequiredDataFromHierarchy ) {
                    if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                        socialMediaTokens.setTwitterToken( companyUnitSettings.getSocialMediaTokens().getTwitterToken() );
                    } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                        socialMediaTokens.setTwitterToken( regionUnitSettings.getSocialMediaTokens().getTwitterToken() );
                    } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                        socialMediaTokens.setTwitterToken( branchUnitSettings.getSocialMediaTokens().getTwitterToken() );
                    } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                        socialMediaTokens.setTwitterToken( agentUnitSettings.getSocialMediaTokens().getTwitterToken() );
                    }
                }
                userProfile.setSocialMediaTokens( socialMediaTokens );
            } else if ( entry.getKey() == SettingsForApplication.LINKED_IN ) {
                SocialMediaTokens socialMediaTokens = userProfile.getSocialMediaTokens();
                if ( socialMediaTokens == null ) {
                    socialMediaTokens = new SocialMediaTokens();
                }
                //get twitter token from upper hierarchy in case of public profile page.
                if ( isFetchRequiredDataFromHierarchy ) {
                    if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                        socialMediaTokens.setLinkedInToken( companyUnitSettings.getSocialMediaTokens().getLinkedInToken() );
                    } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                        socialMediaTokens.setLinkedInToken( regionUnitSettings.getSocialMediaTokens().getLinkedInToken() );
                    } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                        socialMediaTokens.setLinkedInToken( branchUnitSettings.getSocialMediaTokens().getLinkedInToken() );
                    } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                        socialMediaTokens.setLinkedInToken( agentUnitSettings.getSocialMediaTokens().getLinkedInToken() );
                    }
                }
                userProfile.setSocialMediaTokens( socialMediaTokens );
            } else if ( entry.getKey() == SettingsForApplication.LENDING_TREE ) {
                SocialMediaTokens socialMediaTokens = userProfile.getSocialMediaTokens();
                if ( socialMediaTokens == null ) {
                    socialMediaTokens = new SocialMediaTokens();
                }
                //get lending tree token from upper hierarchy in case of public profile page.
                if ( isFetchRequiredDataFromHierarchy ) {
                    if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                        socialMediaTokens
                            .setLendingTreeToken( companyUnitSettings.getSocialMediaTokens().getLendingTreeToken() );
                    } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                        socialMediaTokens.setLendingTreeToken( regionUnitSettings.getSocialMediaTokens().getLendingTreeToken() );
                    } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                        socialMediaTokens.setLendingTreeToken( branchUnitSettings.getSocialMediaTokens().getLendingTreeToken() );
                    } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                        socialMediaTokens.setLendingTreeToken( agentUnitSettings.getSocialMediaTokens().getLendingTreeToken() );
                    }
                }
                userProfile.setSocialMediaTokens( socialMediaTokens );
            } else if ( entry.getKey() == SettingsForApplication.YELP ) {
                SocialMediaTokens socialMediaTokens = userProfile.getSocialMediaTokens();
                if ( socialMediaTokens == null ) {
                    socialMediaTokens = new SocialMediaTokens();
                }
                //get yelp token from upper hierarchy in case of public profile page.
                if ( isFetchRequiredDataFromHierarchy ) {
                    if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                        socialMediaTokens.setYelpToken( companyUnitSettings.getSocialMediaTokens().getYelpToken() );
                    } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                        socialMediaTokens.setYelpToken( regionUnitSettings.getSocialMediaTokens().getYelpToken() );
                    } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                        socialMediaTokens.setYelpToken( branchUnitSettings.getSocialMediaTokens().getYelpToken() );
                    } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                        socialMediaTokens.setYelpToken( agentUnitSettings.getSocialMediaTokens().getYelpToken() );
                    }
                }
                userProfile.setSocialMediaTokens( socialMediaTokens );
            } else if ( entry.getKey() == SettingsForApplication.REALTOR ) {
                SocialMediaTokens socialMediaTokens = userProfile.getSocialMediaTokens();
                if ( socialMediaTokens == null ) {
                    socialMediaTokens = new SocialMediaTokens();
                }
                //get realtor token from upper hierarchy in case of public profile page.
                if ( isFetchRequiredDataFromHierarchy ) {
                    if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                        socialMediaTokens.setRealtorToken( companyUnitSettings.getSocialMediaTokens().getRealtorToken() );
                    } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                        socialMediaTokens.setRealtorToken( regionUnitSettings.getSocialMediaTokens().getRealtorToken() );
                    } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                        socialMediaTokens.setRealtorToken( branchUnitSettings.getSocialMediaTokens().getRealtorToken() );
                    } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                        socialMediaTokens.setRealtorToken( agentUnitSettings.getSocialMediaTokens().getRealtorToken() );
                    }
                }
                userProfile.setSocialMediaTokens( socialMediaTokens );
            } else if ( entry.getKey() == SettingsForApplication.ZILLOW ) {
                SocialMediaTokens socialMediaTokens = userProfile.getSocialMediaTokens();
                if ( socialMediaTokens == null ) {
                    socialMediaTokens = new SocialMediaTokens();
                }
                //get zillow token from upper hierarchy in case of public profile page.
                if ( isFetchRequiredDataFromHierarchy ) {
                    if ( entry.getValue() == OrganizationUnit.COMPANY ) {
                        socialMediaTokens.setZillowToken( companyUnitSettings.getSocialMediaTokens().getZillowToken() );
                    } else if ( entry.getValue() == OrganizationUnit.REGION ) {
                        socialMediaTokens.setZillowToken( regionUnitSettings.getSocialMediaTokens().getZillowToken() );
                    } else if ( entry.getValue() == OrganizationUnit.BRANCH ) {
                        socialMediaTokens.setZillowToken( branchUnitSettings.getSocialMediaTokens().getZillowToken() );
                    } else if ( entry.getValue() == OrganizationUnit.AGENT ) {
                        socialMediaTokens.setZillowToken( agentUnitSettings.getSocialMediaTokens().getZillowToken() );
                    }
                }
                userProfile.setSocialMediaTokens( socialMediaTokens );
            }
        }
        return userProfile;
    }


    public void updateEmailsWithLogo( OrganizationUnitSettings unitSettings, String logoUrl, String collectionName )
    {
        LOG.debug( "Inside method updateEmailsWithLogo for UnitSettings " );
        List<String> paramOrder = null;

        MailContentSettings mailContentSettings = unitSettings.getMail_content();
        if ( mailContentSettings != null ) {
            MailContent takeSurveyMailContent = mailContentSettings.getTake_survey_mail();
            if ( takeSurveyMailContent != null ) {
                String mailBody = takeSurveyMailContent.getMail_body();
                try {
                    mailBody = organizationManagementService
                        .readMailContentFromFile( CommonConstants.SURVEY_REQUEST_MAIL_FILENAME );
                    paramOrder = new ArrayList<String>( Arrays.asList( paramOrderTakeSurvey.split( "," ) ) );
                } catch ( IOException e ) {

                }
                mailBody = emailFormatHelper.replaceEmailBodyWithParams( mailBody, paramOrder );
                mailBody = mailBody.replaceAll( "\\[LogoUrl\\]", logoUrl );
                takeSurveyMailContent.setMail_body( mailBody );
            }
            mailContentSettings.setTake_survey_mail( takeSurveyMailContent );
            MailContent takeSureyMailCustomerContent = mailContentSettings.getTake_survey_mail_customer();
            if ( takeSureyMailCustomerContent != null ) {
                String mailBody = takeSureyMailCustomerContent.getMail_body();
                try {
                    mailBody = organizationManagementService
                        .readMailContentFromFile( CommonConstants.SURVEY_CUSTOMER_REQUEST_MAIL_FILENAME );
                    paramOrder = new ArrayList<String>( Arrays.asList( paramOrderTakeSurveyCustomer.split( "," ) ) );
                } catch ( IOException e ) {
                }
                mailBody = emailFormatHelper.replaceEmailBodyWithParams( mailBody, paramOrder );
                mailBody = mailBody.replaceAll( "\\[LogoUrl\\]", logoUrl );
                takeSureyMailCustomerContent.setMail_body( mailBody );
            }
            mailContentSettings.setTake_survey_mail_customer( takeSureyMailCustomerContent );
            MailContent surveyReminderMailContent = mailContentSettings.getTake_survey_reminder_mail();
            if ( surveyReminderMailContent != null ) {
                String mailBody = surveyReminderMailContent.getMail_body();
                try {
                    mailBody = organizationManagementService
                        .readMailContentFromFile( CommonConstants.SURVEY_REMINDER_MAIL_FILENAME );
                    paramOrder = new ArrayList<String>( Arrays.asList( paramOrderTakeSurveyReminder.split( "," ) ) );
                } catch ( IOException e ) {
                }
                mailBody = emailFormatHelper.replaceEmailBodyWithParams( mailBody, paramOrder );
                mailBody = mailBody.replaceAll( "\\[LogoUrl\\]", logoUrl );
                surveyReminderMailContent.setMail_body( mailBody );
            }
            mailContentSettings.setTake_survey_reminder_mail( surveyReminderMailContent );
            MailContent restartSurveyMailContent = mailContentSettings.getRestart_survey_mail();
            if ( restartSurveyMailContent != null ) {
                String mailBody = restartSurveyMailContent.getMail_body();
                try {
                    mailBody = organizationManagementService
                        .readMailContentFromFile( CommonConstants.SURVEY_REQUEST_MAIL_FILENAME );
                    paramOrder = new ArrayList<String>( Arrays.asList( paramOrderTakeSurvey.split( "," ) ) );
                } catch ( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mailBody = emailFormatHelper.replaceEmailBodyWithParams( mailBody, paramOrder );
                mailBody = mailBody.replaceAll( "\\[LogoUrl\\]", logoUrl );
                restartSurveyMailContent.setMail_body( mailBody );
            }
            mailContentSettings.setRestart_survey_mail( restartSurveyMailContent );
        }
        unitSettings.setMail_content( mailContentSettings );
        organizationManagementService.updateMailContentForOrganizationUnit( mailContentSettings, unitSettings, collectionName );
    }


    Map<String, Object> convertJsonStringToMap( String jsonString ) throws JsonParseException, JsonMappingException,
        IOException
    {
        Map<String, Object> map = new ObjectMapper().readValue( jsonString, new TypeReference<HashMap<String, Object>>() {} );
        return map;
    }


    @SuppressWarnings ( "unchecked")
    List<SurveyDetails> fetchAndSaveZillowFeeds( OrganizationUnitSettings profile, String collectionName, long companyId,
        boolean fromBatch, boolean fromPublicPage ) throws InvalidInputException, UnavailableException
    {
        if ( profile == null )
            throw new InvalidInputException( "Profile setting passed cannot be null" );
        if ( collectionName == null || collectionName.isEmpty() ) {
            throw new InvalidInputException( "Collection name passed cannot be null or empty" );
        }
        LOG.debug( "Fetching social feed for " + collectionName + " with iden: " + profile.getIden() );
        List<SurveyDetails> surveyDetailsList = new ArrayList<SurveyDetails>();
        if ( profile != null && profile.getSocialMediaTokens() != null ) {
            LOG.debug( "Starting to fetch the feed." );

            SocialMediaTokens token = profile.getSocialMediaTokens();
            if ( token != null ) {
                if ( token.getZillowToken() != null ) {
                    ZillowIntegrationApi zillowIntegrationApi = zillowIntegrationApiBuilder.getZellowIntegrationApi();
                    String responseString = null;
                    ZillowToken zillowToken = token.getZillowToken();
                    String zillowScreenName = zillowToken.getZillowScreenName();
                    if ( zillowScreenName == null || zillowScreenName.isEmpty() ) {
                        LOG.debug( "Old zillow url. Modify and get the proper screen name. But for now bypass and do nothing" );
                        // TODO: Convert to proper format from the old url format
                    } else {
                        Response response = null;

                        try {
                            // Replace - with spaces in zillow screen name
                            zillowScreenName = zillowScreenName.replaceAll( "-", " " );
                            response = zillowIntegrationApi
                                .fetchZillowReviewsByScreennameWithMaxCount( zwsId, zillowScreenName );
                        } catch ( Exception e ) {
                            LOG.error( "Exception caught while fetching zillow reviews" + e.getMessage() );
                            reportBugOnZillowFetchFail( profile.getProfileName(), zillowScreenName, e );
                            throw new UnavailableException( "Zillow reviews could not be fetched for " + profile.getIden()
                                + " zillow account " + zillowScreenName );
                        }

                        ExternalAPICallDetails zillowAPICallDetails = new ExternalAPICallDetails();
                        zillowAPICallDetails.setHttpMethod( CommonConstants.HTTP_METHOD_GET );
                        zillowAPICallDetails.setRequest( zillowEndpoint + CommonConstants.ZILLOW_CALL_REQUEST + "&zws-id="
                            + zwsId + "&screenname=" + zillowScreenName );

                        if ( response != null ) {
                            responseString = new String( ( (TypedByteArray) response.getBody() ).getBytes() );
                        }

                        zillowAPICallDetails.setResponse( responseString );
                        zillowAPICallDetails.setRequestTime( new Date( System.currentTimeMillis() ) );
                        zillowAPICallDetails.setSource( CommonConstants.ZILLOW_SOCIAL_SITE );
                        //Store this record in mongo
                        externalApiCallDetailsDao.insertApiCallDetails( zillowAPICallDetails );

                        if ( responseString != null ) {
                            Map<String, Object> map = null;
                            try {
                                map = convertJsonStringToMap( responseString );
                                if ( checkMapForError( map ) ) {
                                    reportBugOnZillowFetchFail( profile.getProfileName(), zillowScreenName, new Exception(
                                        (String) map.get( ZILLOW_JSON_TEXT_KEY ) ) );
                                    // return new ArrayList<SurveyDetails>();
                                }
                            } catch ( JsonParseException e ) {
                                LOG.error( "Exception caught while parsing zillow reviews" + e.getMessage() );
                                reportBugOnZillowFetchFail( profile.getProfileName(), zillowScreenName, e );
                                throw new UnavailableException( "Zillow reviews could not be fetched for " + profile.getIden()
                                    + " zillow account " + zillowScreenName );
                            } catch ( JsonMappingException e ) {
                                LOG.error( "Exception caught while parsing zillow reviews" + e.getMessage() );
                                reportBugOnZillowFetchFail( profile.getProfileName(), zillowScreenName, e );
                                throw new UnavailableException( "Zillow reviews could not be fetched for " + profile.getIden()
                                    + " zillow account " + zillowScreenName );
                            } catch ( IOException e ) {
                                LOG.error( "Exception caught while parsing zillow reviews" + e.getMessage() );
                                reportBugOnZillowFetchFail( profile.getProfileName(), zillowScreenName, e );
                                throw new UnavailableException( "Zillow reviews could not be fetched for " + profile.getIden()
                                    + " zillow account " + zillowScreenName );
                            }

                            if ( map != null ) {
                                Map<String, Object> responseMap = new HashMap<String, Object>();
                                Map<String, Object> resultMap = new HashMap<String, Object>();
                                Map<String, Object> proReviews = new HashMap<String, Object>();
                                Map<String, Object> messageMap = new HashMap<String, Object>();
                                List<HashMap<String, Object>> reviews = new ArrayList<HashMap<String, Object>>();
                                responseMap = (HashMap<String, Object>) map.get( "response" );
                                messageMap = (HashMap<String, Object>) map.get( "message" );
                                String code = (String) messageMap.get( "code" );
                                if ( code.equalsIgnoreCase( "7" ) ) {
                                    String errorMessage = (String) messageMap.get( "text" );
                                    int count = socialManagementService.fetchZillowCallCount();
                                    if ( count != 0 ) {
                                        LOG.debug( "Zillow API call count exceeded limit. Sending mail to admin." );
                                        try {
                                            emailServices.sendZillowCallExceededMailToAdmin( count );
                                            surveyDetailsDao.resetZillowCallCount();
                                        } catch ( InvalidInputException e ) {
                                            LOG.error( "Sending the mail to the admin failed due to invalid input. Reason : ",
                                                e );
                                        } catch ( UndeliveredEmailException e ) {
                                            LOG.error( "The email failed to get delivered. Reason : ", e );
                                        }
                                    }
                                    LOG.error( "Error code : " + code + " Error description : " + errorMessage );
                                } else if ( !code.equalsIgnoreCase( "0" ) ) {
                                    String errorMessage = (String) messageMap.get( "text" );
                                    LOG.error( "Error code : " + code + " Error description : " + errorMessage );
                                } else {
                                    surveyDetailsDao.updateZillowCallCount();
                                }

                                if ( responseMap != null ) {
                                    resultMap = (HashMap<String, Object>) responseMap.get( "results" );
                                    if ( resultMap != null ) {
                                        proReviews = (HashMap<String, Object>) resultMap.get( "proReviews" );
                                        if ( proReviews != null ) {
                                            reviews = (List<HashMap<String, Object>>) proReviews.get( "review" );
                                            if ( reviews != null ) {
                                                surveyDetailsList = buildSurveyDetailsFromReviewMap( reviews, collectionName,
                                                    profile, companyId, fromBatch, fromPublicPage );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LOG.error( "No social media token present for " + collectionName + " with iden: " + profile.getIden() );
        }

        // Commencted as Zillow Surveys will be saved in Social Survey Database, SS-307
        //        if ( surveyDetailsList.size() > 0 && zillowReviewScoreTotal > -1 ) {
        //            zillowUpdateService.updateZillowReviewCountAndAverage( collectionName, profile.getIden(), surveyDetailsList.size(),
        //                ( zillowReviewScoreTotal / surveyDetailsList.size() ) );
        //        }
        // return the fetched zillow reviews
        return surveyDetailsList;
    }


    @Override
    public Date convertStringToDate( String dateString )
    {

        DateFormat format = new SimpleDateFormat( "MM/dd/yyyy", Locale.ENGLISH );
        Date date;
        try {
            date = format.parse( dateString );
        } catch ( ParseException e ) {
            return null;
        }
        return date;
    }


    @Override
    public LockSettings fetchHierarchyLockSettings( long companyId, long branchId, long regionId, String entityType )
        throws NonFatalException
    {
        LOG.debug( "Method fetchHierarchyLockSettings() called from ProfileManagementService" );
        boolean logoLocked = true;
        boolean webAddressLocked = true;
        boolean phoneNumberLocked = true;
        boolean workEmailLocked = true;
        List<SettingsDetails> settingsDetailsList = settingsManager
            .getScoreForCompleteHeirarchy( companyId, branchId, regionId );
        Map<String, Long> totalScore = settingsManager.calculateSettingsScore( settingsDetailsList );
        long currentLockAggregateValue = totalScore.get( CommonConstants.LOCK_SCORE );
        LockSettings parentLock = new LockSettings();

        if ( entityType.equalsIgnoreCase( CommonConstants.COMPANY_ID_COLUMN ) ) {
            logoLocked = false;
            webAddressLocked = false;
            phoneNumberLocked = false;
            workEmailLocked = false;
        } else if ( entityType.equalsIgnoreCase( CommonConstants.REGION_ID_COLUMN ) ) {
            if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.LOGO,
                currentLockAggregateValue ) ) {
                logoLocked = false;
            }
            if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.WEB_ADDRESS_WORK,
                currentLockAggregateValue ) ) {
                webAddressLocked = false;
            }
            if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.PHONE,
                currentLockAggregateValue ) ) {
                phoneNumberLocked = false;
            }
            if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.EMAIL_ID_WORK,
                currentLockAggregateValue ) ) {
                workEmailLocked = false;
            }

            //check only for company
            if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.EMAIL_ID_WORK,
                currentLockAggregateValue ) ) {
                workEmailLocked = false;
            }
        } else if ( entityType.equalsIgnoreCase( CommonConstants.BRANCH_ID_COLUMN ) ) {

            if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.LOGO,
                currentLockAggregateValue ) ) {
                logoLocked = false;
            }
            if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.WEB_ADDRESS_WORK,
                currentLockAggregateValue ) ) {
                webAddressLocked = false;
            }
            if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.PHONE,
                currentLockAggregateValue ) ) {
                phoneNumberLocked = false;
            }

            if ( !logoLocked ) {
                if ( !checkIfSettingLockedByOrganization( OrganizationUnit.REGION, SettingsForApplication.LOGO,
                    currentLockAggregateValue ) ) {
                    logoLocked = false;
                }
            }
            if ( !webAddressLocked ) {
                if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.WEB_ADDRESS_WORK,
                    currentLockAggregateValue ) ) {
                    webAddressLocked = false;
                }
            }
            if ( !phoneNumberLocked ) {
                if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.PHONE,
                    currentLockAggregateValue ) ) {
                    phoneNumberLocked = false;
                }
            }

            //check only for company
            if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.EMAIL_ID_WORK,
                currentLockAggregateValue ) ) {
                workEmailLocked = false;
            }
        } else if ( entityType.equalsIgnoreCase( CommonConstants.AGENT_ID_COLUMN ) ) {
            if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.LOGO,
                currentLockAggregateValue ) ) {
                logoLocked = false;
            }
            if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.WEB_ADDRESS_WORK,
                currentLockAggregateValue ) ) {
                webAddressLocked = false;
            }
            if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.PHONE,
                currentLockAggregateValue ) ) {
                phoneNumberLocked = false;
            }

            if ( !logoLocked ) {
                if ( !checkIfSettingLockedByOrganization( OrganizationUnit.REGION, SettingsForApplication.LOGO,
                    currentLockAggregateValue ) ) {
                    logoLocked = false;
                }

            }
            if ( !webAddressLocked ) {
                if ( !checkIfSettingLockedByOrganization( OrganizationUnit.REGION, SettingsForApplication.WEB_ADDRESS_WORK,
                    currentLockAggregateValue ) ) {
                    webAddressLocked = false;
                }
            }
            if ( !phoneNumberLocked ) {
                if ( !checkIfSettingLockedByOrganization( OrganizationUnit.REGION, SettingsForApplication.PHONE,
                    currentLockAggregateValue ) ) {
                    phoneNumberLocked = false;
                }
            }


            if ( !logoLocked ) {
                if ( !checkIfSettingLockedByOrganization( OrganizationUnit.BRANCH, SettingsForApplication.LOGO,
                    currentLockAggregateValue ) ) {
                    logoLocked = false;
                }

            }
            if ( !webAddressLocked ) {
                if ( !checkIfSettingLockedByOrganization( OrganizationUnit.BRANCH, SettingsForApplication.WEB_ADDRESS_WORK,
                    currentLockAggregateValue ) ) {
                    webAddressLocked = false;
                }
            }
            if ( !phoneNumberLocked ) {
                if ( !checkIfSettingLockedByOrganization( OrganizationUnit.BRANCH, SettingsForApplication.PHONE,
                    currentLockAggregateValue ) ) {
                    phoneNumberLocked = false;
                }

            }

            //check only if company locked
            if ( !checkIfSettingLockedByOrganization( OrganizationUnit.COMPANY, SettingsForApplication.EMAIL_ID_WORK,
                currentLockAggregateValue ) ) {
                workEmailLocked = false;
            }
        }
        parentLock.setLogoLocked( logoLocked );
        parentLock.setWebAddressLocked( webAddressLocked );
        parentLock.setWorkPhoneLocked( phoneNumberLocked );
        parentLock.setWorkEmailLocked( workEmailLocked );

        return parentLock;
    }


    boolean checkIfSettingLockedByOrganization( OrganizationUnit unit, SettingsForApplication settingsforApplications,
        long currentLockValue )
    {
        LOG.debug( "Inside method getLogoLockedByCompany " );
        if ( settingsLocker.isSettingsValueLocked( unit, currentLockValue, settingsforApplications ) ) {
            return true;
        } else {
            return false;
        }
    }


    void updateEmailIdInSolr( String emailId, long iden ) throws NonFatalException
    {
        if ( iden <= 0 ) {
            LOG.error( "Invalid iden passed in updateEmailIdInSolr" );
            throw new InvalidInputException( "Invalid iden passed in updateEmailIdInSolr" );
        }
        if ( emailId == null || emailId.isEmpty() ) {
            throw new InvalidInputException( "Email id passed cannot be null or empty in updateEmailIdInSolr" );
        }
        LOG.info( "Updating verified email id info into solr for user id : " + iden );
        Map<String, Object> editKeys = new HashMap<String, Object>();
        editKeys.put( CommonConstants.USER_LOGIN_NAME_SOLR, emailId );
        editKeys.put( CommonConstants.USER_EMAIL_ID_SOLR, emailId );
        try {
            solrSearchService.editUserInSolrWithMultipleValues( iden, editKeys );
        } catch ( SolrException se ) {
            throw new NonFatalException( se.getMessage() );
        }
        LOG.info( "Updated verified email id info into solr for user id : " + iden );
    }


    /**
     * Method to fetch Zillow data
     * @param profile
     * @param collection
     * @throws InvalidInputException
     * @throws UnavailableException
     * */
    @Override
    public List<SurveyDetails> fetchAndSaveZillowData( OrganizationUnitSettings profile, String collection, long companyId,
        boolean fromBatch, boolean fromPublicPage ) throws InvalidInputException, UnavailableException
    {

        if ( profile == null || collection == null || collection.isEmpty() ) {
            LOG.info( "Invalid parameters passed to fetchZillowData for fetching zillow feed" );
            throw new InvalidInputException( "Invalid parameters passed to fetchZillowData for fetching zillow feed" );
        }
        LOG.info( "Method to fetch zillow feed called for ID :" + profile.getIden() + " of collection : " + collection );
        if ( profile.getSocialMediaTokens() != null && profile.getSocialMediaTokens().getZillowToken() != null ) {
            // fetching zillow feed
            LOG.debug( "Fetching zillow feed for " + profile.getId() + " from " + collection );
            List<SurveyDetails> surveyDetailsList = fetchAndSaveZillowFeeds( profile, collection, companyId, fromBatch,
                fromPublicPage );
            LOG.info( "Method to fetch zillow feed finished." );
            return surveyDetailsList;
        } else {
            LOG.info( "Zillow is not added for the profile" );
            throw new InvalidInputException( "Zillow is not added for the profile" );
        }
    }


    @Override
    @Transactional
    public Map<String, Long> getZillowTotalScoreAndReviewCountForProfileLevel( String profileLevel, long iden )
    {
        if ( profileLevel == null || profileLevel.isEmpty() ) {
            LOG.error( "column name is null or empty while getting total review count and score for a column name and id" );
            return null;
        }
        if ( iden <= 0l ) {
            LOG.error( "Invalid id passed while getting total review count and score for a column name and id" );
            return null;
        }
        try {
            switch ( profileLevel ) {
                case CommonConstants.PROFILE_LEVEL_COMPANY:
                    return zillowHierarchyDao.getZillowReviewCountAndTotalScoreForAllUnderCompany( iden );
                case CommonConstants.PROFILE_LEVEL_REGION:
                    return zillowHierarchyDao.getZillowReviewCountAndTotalScoreForAllUnderRegion( iden );
                case CommonConstants.PROFILE_LEVEL_BRANCH:
                    return zillowHierarchyDao.getZillowReviewCountAndTotalScoreForAllUnderBranch( iden );
                case CommonConstants.PROFILE_LEVEL_INDIVIDUAL:
                    User user = userDao.findById( User.class, iden );
                    long zillowReviewCount = 0;
                    long zillowTotalScore = 0;
                    if ( user != null && user.getIsZillowConnected() == CommonConstants.YES ) {
                        zillowReviewCount = user.getZillowReviewCount();
                        zillowTotalScore = (long) ( user.getZillowAverageScore() * zillowReviewCount );
                    }
                    Map<String, Long> zillowTotalScoreAndAverageMap = new HashMap<String, Long>();
                    zillowTotalScoreAndAverageMap.put( CommonConstants.ZILLOW_REVIEW_COUNT_COLUMN, zillowReviewCount );
                    zillowTotalScoreAndAverageMap.put( CommonConstants.ZILLOW_TOTAL_SCORE, zillowTotalScore );
                    return zillowTotalScoreAndAverageMap;
                default:
                    LOG.error( "Invalid profile level passed while getting ids under a profile level" );
            }
        } catch ( Exception e ) {
            LOG.error( "Exception occurred while fetching zillow total score and average for profile level and id. Reason : ",
                e );
        }
        return null;
    }


    /**
     * Method to get ids under a unit based on profile level
     * @param unitName
     * @param iden
     * @param exception
     * */
    void reportBugOnZillowFetchFail( String unitName, String zillowScreenName, Exception exception )
    {
        try {
            LOG.info( "Building error message for the zillow review fetch failure" );
            String errorMsg = "<br>" + exception.getMessage()
                + "<br><br>Error while fetching zillow reviews for a unit/Agent<br>";
            errorMsg += "<br>Social Application : Zillow<br>";
            errorMsg += "<br>Unit/Agent Name : " + unitName + "<br>";
            errorMsg += "<br>Zillow Screen Name : " + zillowScreenName + "<br>";
            errorMsg += "<br>StackTrace : <br>" + ExceptionUtils.getStackTrace( exception ).replaceAll( "\n", "<br>" ) + "<br>";
            LOG.info( "Error message built for zillow review fetch failure" );
            LOG.info( "Sending bug mail to admin for zillow review fetch failure" );
            emailServices.sendReportBugMailToAdmin( applicationAdminName, errorMsg, applicationAdminEmail );
            LOG.info( "Sent bug mail to admin for zillow review fetch failure" );
        } catch ( UndeliveredEmailException ude ) {
            LOG.error( "error while sending report bug mail to admin ", ude );
        } catch ( InvalidInputException iie ) {
            LOG.error( "error while sending report bug mail to admin ", iie );
        }
    }


    boolean checkMapForError( Map<String, Object> map )
    {
        if ( map != null && map.containsKey( ZILLOW_JSON_CODE_KEY ) ) {
            int code = Integer.parseInt( ( String.valueOf( map.get( ZILLOW_JSON_CODE_KEY ) ) ) );
            if ( code > 0 && map.containsKey( ZILLOW_JSON_TEXT_KEY ) && map.get( ZILLOW_JSON_TEXT_KEY ) != null
                && ( (String) map.get( ZILLOW_JSON_TEXT_KEY ) ).startsWith( ZILLOW_JSON_ERROR_TEXT_PREFIX ) ) {
                return true;
            }
        }
        return false;
    }


    @Override
    public List<SurveyDetails> buildSurveyDetailsFromReviewMap( List<HashMap<String, Object>> reviews, String collectionName,
        OrganizationUnitSettings profile, long companyId, boolean fromBatch, boolean fromPublicPage )
        throws InvalidInputException
    {
        List<SurveyDetails> surveyDetailsList = new ArrayList<SurveyDetails>();
        String idenColumnName = "";
        if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION ) ) {
            idenColumnName = CommonConstants.COMPANY_ID_COLUMN;
        } else if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION ) ) {
            idenColumnName = CommonConstants.REGION_ID_COLUMN;
        } else if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.BRANCH_SETTINGS_COLLECTION ) ) {
            idenColumnName = CommonConstants.BRANCH_ID_COLUMN;
        } else if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION ) ) {
            idenColumnName = CommonConstants.AGENT_ID_COLUMN;
        }
        Map<String, Object> queries = new HashMap<String, Object>();
        List<String> latestSurveyIdList = new ArrayList<String>();
        queries.put( idenColumnName, profile.getIden() );

        // LOG.info( "Deleting existing reviews for profile type : " + idenColumnName + " and profile id : " + profile.getIden() );
        // surveyHandler.deleteExistingZillowSurveysByEntity( idenColumnName, profile.getIden() );
        // LOG.info( "Deleted existing reviews for profile type : " + idenColumnName + " and profile id : " + profile.getIden() );
        for ( Map<String, Object> review : reviews ) {
            String sourceId = (String) review.get( "reviewURL" );
            String reviewDescription = (String) review.get( "description" );
            String summary = (String) review.get( "reviewSummary" );
            queries.put( CommonConstants.SURVEY_SOURCE_ID_COLUMN, sourceId );
            boolean isAbusive = false;
            if ( fromBatch ) {
                utils.checkReviewForSwearWords( reviewDescription, surveyHandler.getSwearList() );
            }
            SurveyDetails surveyDetails = surveyDetailsDao.getZillowReviewByQueryMap( queries );
            if ( surveyDetails == null ) {
                surveyDetails = new SurveyDetails();
                if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION ) ) {
                    surveyDetails.setCompanyId( profile.getIden() );
                } else if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION ) ) {
                    surveyDetails.setRegionId( profile.getIden() );
                    surveyDetails.setCompanyId( companyId );
                } else if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.BRANCH_SETTINGS_COLLECTION ) ) {
                    try {
                        Branch branch = branchDao.findById( Branch.class, profile.getIden() );
                        if ( branch != null ) {
                            surveyDetails.setRegionId( branch.getRegion().getRegionId() );
                        }
                    } catch ( Exception e ) {
                        LOG.error( "Could not find by branch details for id : " + profile.getIden(), e );
                    }
                    surveyDetails.setBranchId( profile.getIden() );
                    surveyDetails.setCompanyId( companyId );
                } else if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION ) ) {
                    try {
                        Map<String, Long> agentDetailsMap = userProfileDao.findPrimaryUserProfileByAgentId( profile.getIden() );
                        if ( agentDetailsMap != null && agentDetailsMap.size() > 0 ) {
                            surveyDetails.setRegionId( agentDetailsMap.get( CommonConstants.REGION_ID_COLUMN ) );
                            surveyDetails.setBranchId( agentDetailsMap.get( CommonConstants.BRANCH_ID_COLUMN ) );
                        }
                    } catch ( Exception e ) {
                        LOG.error( "Could not find by agent hierarchy details for id : " + profile.getIden(), e );
                    }
                    surveyDetails.setAgentId( profile.getIden() );
                    if ( profile.getContact_details() != null && profile.getContact_details().getName() != null
                        && profile.getContact_details().getName().trim().length() > 0 ) {
                        surveyDetails.setAgentName( profile.getContact_details().getName() );
                    }
                    surveyDetails.setCompanyId( companyId );
                }
                String createdDate = (String) review.get( "reviewDate" );
                surveyDetails.setCompleteProfileUrl( (String) review.get( "reviewerLink" ) );
                surveyDetails.setCustomerFirstName( (String) review.get( "reviewer" ) );
                surveyDetails.setReview( reviewDescription );
                surveyDetails.setEditable( false );
                surveyDetails.setStage( CommonConstants.SURVEY_STAGE_COMPLETE );
                surveyDetails.setScore( Double.valueOf( (String) review.get( "rating" ) ) );
                surveyDetails.setSource( CommonConstants.SURVEY_SOURCE_ZILLOW );
                surveyDetails.setSourceId( sourceId );
                surveyDetails.setModifiedOn( convertStringToDate( createdDate ) );
                surveyDetails.setCreatedOn( convertStringToDate( createdDate ) );
                surveyDetails.setAgreedToShare( "true" );
                surveyDetails.setAbusive( isAbusive );
                surveyDetails.setAbuseRepByUser( false );
                surveyDetails.setShowSurveyOnUI( true );

                // saving zillow review summary
                surveyDetails.setSummary( summary );

                surveyHandler.insertSurveyDetails( surveyDetails );

                // Commented as Zillow reviews are saved in Social Survey, SS-307
                // if ( zillowReviewScoreTotal == -1 )
                //    zillowReviewScoreTotal = surveyDetails.getScore();
                // else
                //    zillowReviewScoreTotal += surveyDetails.getScore();
            } else if ( ( surveyDetails.getSummary() == null || surveyDetails.getSummary().trim().length() == 0 )
                && ( summary != null && summary.length() > 0 ) ) {
                surveyDetails.setSummary( summary );
                surveyDetails.setReview( reviewDescription );

                surveyHandler.updateZillowSummaryInExistingSurveyDetails( surveyDetails );
            }

            if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION ) && fromBatch ) {
                postToTempTable( collectionName, profile, surveyDetails, review );
            }

            /* if ( fromPublicPage ) {
                 String reviewDesc = surveyDetails.getReview();
                 if ( reviewDescription != null && !reviewDescription.isEmpty() ) {
                     reviewDesc = reviewDesc + "<br>" + reviewDescription;
                     surveyDetails.setReview( reviewDesc );
                 }
             }*/
            surveyDetailsList.add( surveyDetails );
            latestSurveyIdList.add( surveyDetails.get_id() );
        }
        if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION ) ) {
            long reviewCount = getReviewsCount( profile.getIden(), -1, -1, CommonConstants.PROFILE_LEVEL_INDIVIDUAL, false,
                false, false, 0 );
            try {
                solrSearchService.editUserInSolr( profile.getIden(), CommonConstants.REVIEW_COUNT_SOLR,
                    String.valueOf( reviewCount ) );
            } catch ( SolrException e ) {
                LOG.error( "Exception occurred while updating zillow review count in solr for agent id : " + profile.getIden()
                    + ". Reason : " + e );
            }
        }
        try {
            LOG.debug( "Resetting showSurveyOnUI property for review ids not in list :" + latestSurveyIdList );
            surveyDetailsDao.resetShowSurveyOnUIPropertyForNonLatestReviews( idenColumnName, profile.getIden(),
                latestSurveyIdList );
            LOG.debug( "Reset showSurveyOnUI property for review ids not in list successfull." );
        } catch ( Exception e ) {
            LOG.error( "Exception occurred while resetting showSurveyOnUI property for review ids not in list :"
                + latestSurveyIdList + ". Reason :", e );
        }
        return surveyDetailsList;
    }


    void pushToZillowPostTemp( OrganizationUnitSettings profile, String collectionName, SurveyDetails surveyDetails,
        Map<String, Object> review ) throws InvalidInputException
    {
        if ( profile == null ) {
            throw new InvalidInputException( "Profile passed as argument in pushToZillowPostTemp cannot be null" );
        }

        if ( collectionName == null || collectionName.isEmpty() ) {
            throw new InvalidInputException(
                "Collection Name passed as argument in pushToZillowPostTemp cannot be null or empty" );
        }

        if ( surveyDetails == null ) {
            throw new InvalidInputException( "Survey Details passed as argument in pushToZillowPostTemp cannot be null" );
        }
        if ( review == null || review.isEmpty() ) {
            throw new InvalidInputException( "Review passed as argument in pushToZillowPostTemp cannot be null or empty" );
        }

        LOG.info( "Method called to push fetched Zillow Review into temp table,pushToZillowPostTemp started" );
        String columnName = null;
        if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION ) ) {
            columnName = CommonConstants.COMPANY_ID_COLUMN;
        } else if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION ) ) {
            columnName = CommonConstants.REGION_ID_COLUMN;
        } else if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.BRANCH_SETTINGS_COLLECTION ) ) {
            columnName = CommonConstants.BRANCH_ID_COLUMN;
        } else if ( collectionName.equalsIgnoreCase( MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION ) ) {
            columnName = CommonConstants.AGENT_ID_COLUMN;
        }

        // Building Zillow Temp Post Object
        ZillowTempPost zillowTempPost = new ZillowTempPost();
        zillowTempPost.setEntityColumnName( columnName );
        zillowTempPost.setEntityId( profile.getIden() );
        zillowTempPost.setZillowReviewUrl( surveyDetails.getSourceId() );
        if ( profile.getSocialMediaTokens() != null && profile.getSocialMediaTokens().getZillowToken() != null
            && profile.getSocialMediaTokens().getZillowToken().getZillowProfileLink() != null ) {
            zillowTempPost.setZillowReviewSourceLink( profile.getSocialMediaTokens().getZillowToken().getZillowProfileLink() );
        } else {
            zillowTempPost.setZillowReviewSourceLink( "" );
        }
        zillowTempPost.setZillowReviewRating( surveyDetails.getScore() );
        zillowTempPost.setZillowReviewerName( surveyDetails.getCustomerFirstName() );
        zillowTempPost.setZillowReviewSummary( surveyDetails.getSummary() );
        zillowTempPost.setZillowReviewDescription( (String) review.get( "description" ) );
        zillowTempPost.setZillowReviewDate( new Timestamp( surveyDetails.getCreatedOn().getTime() ) );
        zillowTempPost.setZillowSurveyId( surveyDetails.get_id() );

        // Persisting Zillow Temp Post Object
        zillowTempPostDao.saveOrUpdateZillowTempPost( zillowTempPost );

        LOG.info( "Method called to push fetched Zillow Review into temp table,pushToZillowPostTemp ended" );
    }


    @Transactional
    public void postToTempTable( String collectionName, OrganizationUnitSettings profile, SurveyDetails surveyDetails,
        Map<String, Object> review )
    {
        try {
            pushToZillowPostTemp( profile, collectionName, surveyDetails, review );
        } catch ( Exception e ) {
            LOG.error( "Exception occurred while pushing Zillow review into temp table. Reason :", e );
        }
    }


    /**
     * method to remove tokens from profile detail
     * @param profile
     */
    @Override
    public void removeTokensFromProfile( OrganizationUnitSettings profile )
    {
        LOG.debug( "Inside method removeTokensFromProfile" );
        if ( profile != null ) {
            if ( profile.getSocialMediaTokens() != null ) {
                SocialMediaTokens socialMediaTokens = profile.getSocialMediaTokens();
                if ( socialMediaTokens != null ) {
                    if ( socialMediaTokens.getFacebookToken() != null ) {
                        socialMediaTokens.getFacebookToken().setFacebookAccessToken( null );
                        socialMediaTokens.getFacebookToken().setFacebookAccessTokenToPost( null );
                        socialMediaTokens.getFacebookToken().setFacebookPages( null );
                    }
                    if ( socialMediaTokens.getLinkedInToken() != null ) {
                        socialMediaTokens.getLinkedInToken().setLinkedInAccessToken( null );
                    }
                    if ( socialMediaTokens.getTwitterToken() != null ) {
                        socialMediaTokens.getTwitterToken().setTwitterAccessToken( null );
                        socialMediaTokens.getTwitterToken().setTwitterAccessTokenSecret( null );
                    }
                    if ( socialMediaTokens.getGoogleToken() != null ) {
                        socialMediaTokens.getGoogleToken().setGoogleAccessToken( null );
                        socialMediaTokens.getGoogleToken().setGoogleRefreshToken( null );
                    }
                }
            }
        }
    }


    @Override
    public void imageLoader() {
        try {
            new File( CommonConstants.TEMP_FOLDER ).mkdir();

            // update last start time
            batchTrackerService.getLastRunEndTimeAndUpdateLastStartTimeByBatchType( CommonConstants.BATCH_TYPE_IMAGE_LOADER,
                CommonConstants.BATCH_NAME_IMAGE_LOADER );

            // Fetch all the profile images pointing to linkedin for company, regions, branches and individuals.
            Map<Long, OrganizationUnitSettings> companySettings = organizationManagementService
                .getSettingsMapWithLinkedinImage( CommonConstants.COMPANY );
            Map<Long, OrganizationUnitSettings> regionSettings = organizationManagementService
                .getSettingsMapWithLinkedinImage( CommonConstants.REGION_COLUMN );
            Map<Long, OrganizationUnitSettings> branchSettings = organizationManagementService
                .getSettingsMapWithLinkedinImage( CommonConstants.BRANCH_NAME_COLUMN );
            Map<Long, OrganizationUnitSettings> agentSettings = organizationManagementService
                .getSettingsMapWithLinkedinImage( "agent" );

            // Process all the company profile images.
            for ( Map.Entry<Long, OrganizationUnitSettings> companySetting : companySettings.entrySet() ) {
                try {
                    String image = loadImages( companySetting.getValue() );
                    if ( image != null ) {
                        updateProfileImage(
                            MongoOrganizationUnitSettingDaoImpl.COMPANY_SETTINGS_COLLECTION, companySetting.getValue(), image );
                    }
                } catch ( Exception e ) {
                    LOG.error( "Exception caught in ImageLoader while copying image from linkedin to SocialSurvey server. "
                        + "Nested exception is ", e );
                    continue;
                }
            }

            // Process all the region profile images.
            for ( Map.Entry<Long, OrganizationUnitSettings> regionSetting : regionSettings.entrySet() ) {
                try {
                    String image = loadImages( regionSetting.getValue() );
                    if ( image != null ) {
                        updateProfileImage(
                            MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION, regionSetting.getValue(), image );
                    }
                } catch ( Exception e ) {
                    LOG.error( "Exception caught in ImageLoader while copying image from linkedin to SocialSurvey server. "
                        + "Nested exception is ", e );
                    continue;
                }
            }

            // Process all the branch profile images.
            for ( Map.Entry<Long, OrganizationUnitSettings> branchSetting : branchSettings.entrySet() ) {
                try {
                    String image = loadImages( branchSetting.getValue() );
                    if ( image != null ) {
                        updateProfileImage(
                            MongoOrganizationUnitSettingDaoImpl.BRANCH_SETTINGS_COLLECTION, branchSetting.getValue(), image );
                    }
                } catch ( Exception e ) {
                    LOG.error( "Exception caught in ImageLoader while copying image from linkedin to SocialSurvey server. "
                        + "Nested exception is ", e );
                    continue;
                }
            }

            // Process all the individual profile images.
            for ( Map.Entry<Long, OrganizationUnitSettings> agentSetting : agentSettings.entrySet() ) {
                try {
                    String image = loadImages( agentSetting.getValue() );
                    if ( image != null ) {
                        updateProfileImage(
                            MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION, agentSetting.getValue(), image );
                    }
                } catch ( Exception e ) {
                    LOG.error( "Exception caught in ImageLoader while copying image from linkedin to SocialSurvey server. "
                        + "Nested exception is ", e );
                    continue;
                }
            }

            //updating last run time for batch in database
            batchTrackerService.updateLastRunEndTimeByBatchType( CommonConstants.BATCH_TYPE_IMAGE_LOADER );
            LOG.info( "Completed ImageUploader" );
        } catch ( Exception e ) {
            LOG.error( "Error in ImageUploader", e );
            try {
                //update batch tracker with error message
                batchTrackerService.updateErrorForBatchTrackerByBatchType( CommonConstants.BATCH_TYPE_IMAGE_LOADER,
                    e.getMessage() );
                //send report bug mail to admin
                batchTrackerService.sendMailToAdminRegardingBatchError( CommonConstants.BATCH_NAME_IMAGE_LOADER,
                    System.currentTimeMillis(), e );
            } catch ( NoRecordsFetchedException | InvalidInputException e1 ) {
                LOG.error( "Error while updating error message in ImageUploader " );
            } catch ( UndeliveredEmailException e1 ) {
                LOG.error( "Error while sending report excption mail to admin " );
            }
        }
    }

    private String loadImages( OrganizationUnitSettings setting ) throws Exception
    {
        String linkedinImageUrl = setting.getProfileImageUrl();
        String imageName = java.util.UUID.randomUUID().toString();
        if ( linkedinImageUrl.contains( ".png" ) || linkedinImageUrl.contains( ".PNG" ) ) {
            imageName = imageName + ".png";
        } else if ( linkedinImageUrl.contains( ".jpg" ) || linkedinImageUrl.contains( ".JPG" ) ) {
            imageName = imageName + ".jpg";
        } else if ( linkedinImageUrl.contains( ".jpeg" ) || linkedinImageUrl.contains( ".JPEG" ) ) {
            imageName = imageName + ".jpeg";
        }

        String destination = copyImage( linkedinImageUrl, imageName );
        return destination;
    }


    private BufferedImage getImageFromUrl( String imageUrl )
    {
        BufferedImage image = null;
        try {
            URL url = new URL( imageUrl );
            image = ImageIO.read( url );
        } catch ( IOException e ) {
            LOG.error( "Exception caught " + e.getMessage() );
        }
        return image;
    }


    private String copyImage( String source, String imageName ) throws Exception
    {

        String fileName = null;
        try {
            BufferedImage image = getImageFromUrl( source );
            if ( image != null ) {
                File tempImage = new File( CommonConstants.TEMP_FOLDER + CommonConstants.FILE_SEPARATOR + imageName );
                tempImage.createNewFile();
                if ( tempImage.exists() ) {
                    if ( imageName.endsWith( ".jpg" ) || imageName.endsWith( ".JPG" ) ) {
                        ImageIO.write( image, "jpg", tempImage );
                    } else if ( imageName.endsWith( ".jpeg" ) || imageName.endsWith( ".JPEG" ) ) {
                        ImageIO.write( image, "png", tempImage );
                    } else if ( imageName.endsWith( ".png" ) || imageName.endsWith( ".PNG" ) ) {
                        ImageIO.write( image, "png", tempImage );
                    }
                    fileName = fileUploadService.uploadProfileImageFile( tempImage, imageName, false );
                    FileUtils
                        .deleteQuietly( new File( CommonConstants.TEMP_FOLDER + CommonConstants.FILE_SEPARATOR + imageName ) );
                    LOG.info( "Successfully retrieved photo of contact" );
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch ( Exception e ) {
            LOG.error( e.getMessage() + ": " + source );
            throw e;
        }

        return cdnUrl + CommonConstants.FILE_SEPARATOR + amazonImageBucket + CommonConstants.FILE_SEPARATOR + fileName;

    }
}