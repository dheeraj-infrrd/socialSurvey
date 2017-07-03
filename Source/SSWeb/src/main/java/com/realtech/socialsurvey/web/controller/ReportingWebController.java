package com.realtech.socialsurvey.web.controller;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.realtech.socialsurvey.core.commons.CommonConstants;
import com.realtech.socialsurvey.core.dao.impl.MongoOrganizationUnitSettingDaoImpl;
import com.realtech.socialsurvey.core.entities.AgentSettings;
import com.realtech.socialsurvey.core.entities.OrganizationUnitSettings;
import com.realtech.socialsurvey.core.entities.OverviewBranch;
import com.realtech.socialsurvey.core.entities.OverviewCompany;
import com.realtech.socialsurvey.core.entities.OverviewRegion;
import com.realtech.socialsurvey.core.entities.OverviewUser;
import com.realtech.socialsurvey.core.entities.SettingsDetails;
import com.realtech.socialsurvey.core.entities.SurveyStatsReportCompany;
import com.realtech.socialsurvey.core.entities.User;
import com.realtech.socialsurvey.core.enums.DisplayMessageType;
import com.realtech.socialsurvey.core.enums.OrganizationUnit;
import com.realtech.socialsurvey.core.enums.SettingsForApplication;
import com.realtech.socialsurvey.core.exception.FatalException;
import com.realtech.socialsurvey.core.exception.InternalServerException;
import com.realtech.socialsurvey.core.exception.InvalidInputException;
import com.realtech.socialsurvey.core.exception.NoRecordsFetchedException;
import com.realtech.socialsurvey.core.exception.NonFatalException;
import com.realtech.socialsurvey.core.exception.ProfileServiceErrorCode;
import com.realtech.socialsurvey.core.services.organizationmanagement.OrganizationManagementService;
import com.realtech.socialsurvey.core.services.organizationmanagement.ProfileManagementService;
import com.realtech.socialsurvey.core.services.organizationmanagement.ProfileNotFoundException;
import com.realtech.socialsurvey.core.services.organizationmanagement.UserManagementService;
import com.realtech.socialsurvey.core.services.reportingmanagement.DashboardGraphManagement;
import com.realtech.socialsurvey.core.services.reportingmanagement.OverviewManagement;
import com.realtech.socialsurvey.core.services.reportingmanagement.ReportingDashboardManagement;
import com.realtech.socialsurvey.core.services.search.SolrSearchService;
import com.realtech.socialsurvey.core.services.settingsmanagement.SettingsManager;
import com.realtech.socialsurvey.core.services.settingsmanagement.impl.InvalidSettingsStateException;
import com.realtech.socialsurvey.core.utils.DisplayMessageConstants;
import com.realtech.socialsurvey.core.utils.MessageUtils;
import com.realtech.socialsurvey.web.common.JspResolver;

@Controller
public class ReportingWebController
{
    private static final Logger LOG = LoggerFactory.getLogger( AccountWebController.class );

    @Autowired
    private SessionHelper sessionHelper;
    
    @Autowired
    private OrganizationManagementService organizationManagementService;
    
    @Autowired
    private UserManagementService userManagementService;
    
    @Autowired
    private SolrSearchService solrSearchService;
    
    @Autowired
    private MessageUtils messageUtils;

    @Autowired
    private ProfileManagementService profileManagementService;
    
    @Autowired
    private SettingsManager settingsManager;
    
    @Autowired
    private OverviewManagement overviewManagement;
    
    @Autowired
    private DashboardGraphManagement DashboardGraphManagement;
    
    @Autowired
    private ReportingDashboardManagement ReportingDashboardManagement;
    

    @RequestMapping ( value = "/showreportingpage", method = RequestMethod.GET)
    public String openReportingPage(Model model, HttpServletRequest request) throws NonFatalException
    {
        
        LOG.info( "Reporting Dashboard Page started" );
        HttpSession session = request.getSession( false );
        User user = sessionHelper.getCurrentUser();
        if ( user == null ) {
            throw new NonFatalException( "NonFatalException while logging in. " );
        }
        
        long entityId = (long) session.getAttribute( CommonConstants.ENTITY_ID_COLUMN );
        String entityType = (String) session.getAttribute( CommonConstants.ENTITY_TYPE_COLUMN );
        Long adminUserid = (Long) session.getAttribute( CommonConstants.REALTECH_USER_ID );
        boolean modelSet = false;
        if ( user.getCompany() != null && user.getCompany().getLicenseDetails() != null
            && !user.getCompany().getLicenseDetails().isEmpty()
            && user.getCompany().getLicenseDetails().get( 0 ).getAccountsMaster() != null ) {
            if ( user.getCompany().getLicenseDetails().get( 0 ).getAccountsMaster()
                .getAccountsMasterId() == CommonConstants.ACCOUNTS_MASTER_INDIVIDUAL ) {
                model.addAttribute( "columnName", CommonConstants.AGENT_ID_COLUMN );
                model.addAttribute( "columnValue", entityId );
                modelSet = true;
            }
        }

        String profileName = "";
        boolean isPasswordSet = true;
        if ( user.getIsForcePassword() == 1 && user.getLoginPassword() == null ) {
            isPasswordSet = false;
        }
        if ( !modelSet ) {
            if ( entityType.equals( CommonConstants.COMPANY_ID_COLUMN ) ) {
                model.addAttribute( "columnName", entityType );
                model.addAttribute( "columnValue", entityId );
                model.addAttribute( "showSendSurveyPopupAdmin", String.valueOf( isPasswordSet ) );
                profileName = user.getCompany().getCompany();
            } else if ( entityType.equals( CommonConstants.REGION_ID_COLUMN ) ) {
                model.addAttribute( "columnName", entityType );
                model.addAttribute( "columnValue", entityId );
                model.addAttribute( "showSendSurveyPopupAdmin", String.valueOf( isPasswordSet ) );
                profileName = solrSearchService.searchRegionById( entityId );
            } else if ( entityType.equals( CommonConstants.BRANCH_ID_COLUMN ) ) {
                model.addAttribute( "columnName", entityType );
                model.addAttribute( "columnValue", entityId );
                model.addAttribute( "showSendSurveyPopupAdmin", String.valueOf( isPasswordSet ) );
                profileName = solrSearchService.searchBranchNameById( entityId );
            } else if ( entityType.equals( CommonConstants.AGENT_ID_COLUMN ) ) {
                model.addAttribute( "columnName", CommonConstants.AGENT_ID_COLUMN );
                model.addAttribute( "columnValue", entityId );
                profileName = user.getFirstName() + " " + user.getLastName();
            }
        }

        model.addAttribute( "profileName", profileName );
        model.addAttribute( "userId", user.getUserId() );
        boolean allowOverrideForSocialMedia = false;
        long branchId = 0;
        long regionId = 0;
        long companyId = 0;
        long agentId = 0;
        int profilesMaster = 0;
        List<SettingsDetails> settingsDetailsList = null;
        OrganizationUnitSettings profileSettings = null;
        Map<SettingsForApplication, OrganizationUnit> map = null;
        //Get the hierarchy details associated with the current profile get all the id's like companyId, regionId , branchId
        try {
            Map<String, Long> hierarchyDetails = profileManagementService.getHierarchyDetailsByEntity( entityType, entityId );
            if ( hierarchyDetails == null ) {
                LOG.error( "Unable to fetch primary profile for this user " );
                throw new FatalException(
                    "Unable to fetch primary profile for type : " + entityType + " and ID : " + entityId );
            }
            branchId = hierarchyDetails.get( CommonConstants.BRANCH_ID_COLUMN );
            regionId = hierarchyDetails.get( CommonConstants.REGION_ID_COLUMN );
            companyId = hierarchyDetails.get( CommonConstants.COMPANY_ID_COLUMN );
            agentId = hierarchyDetails.get( CommonConstants.AGENT_ID_COLUMN );
            //Sorting out the default region's and branche's in the list 
            settingsDetailsList = settingsManager.getScoreForCompleteHeirarchy( companyId, branchId, regionId );
            LOG.debug( "Company ID : " + companyId + " Region ID : " + regionId + " Branch ID : " + branchId + " Agent ID : "
                + agentId );
        } catch ( InvalidInputException e ) {
            LOG.error( "InvalidInputException while showing profile page. Reason :" + e.getMessage(), e );
            model.addAttribute( "message",
                messageUtils.getDisplayMessage( e.getErrorCode(), DisplayMessageType.ERROR_MESSAGE ) );
        } catch ( ProfileNotFoundException e ) {
            LOG.error( "No profile found for the user ", e );
            return JspResolver.NO_PROFILES_FOUND;
        }

        //get unitSetting's and set session attribute column's 
        sessionHelper.updateSelectedProfile( session, entityId, entityType );
        // fetching details from profile based on type 
        if ( entityType.equals( CommonConstants.COMPANY_ID_COLUMN ) ) {
            //If the profile is a company profile
            model.addAttribute( "columnName", entityType );
            profilesMaster = CommonConstants.PROFILES_MASTER_COMPANY_ADMIN_PROFILE_ID;
            OrganizationUnitSettings companyProfile = null;
            try {
                companyProfile = organizationManagementService.getCompanySettings( companyId );
                //set setting detail by company Setting
              //fetch nmls-id
                try {
                    Integer nmlsId = null;
                    nmlsId = profileManagementService.fetchAndSaveNmlsId( companyProfile,
                        CommonConstants.COMPANY_SETTINGS_COLLECTION, user.getCompany().getCompanyId(), false, true );
                  model.addAttribute( "NMLS", nmlsId );
                } catch ( UnavailableException e ) {
                    LOG.error( "UnavailableException: message : " + e.getMessage(), e );
                }
                setSettingSetByEntityInModel( model, companyProfile );
                String json = new Gson().toJson( companyProfile );
                model.addAttribute( "profileJson", json );
                double averageRating = profileManagementService.getAverageRatings( companyId,
                    CommonConstants.PROFILE_LEVEL_COMPANY, false );
                model.addAttribute( "averageRating", averageRating );

                long reviewsCount = profileManagementService.getReviewsCount( companyId, CommonConstants.MIN_RATING_SCORE,
                    CommonConstants.MAX_RATING_SCORE, CommonConstants.PROFILE_LEVEL_COMPANY, false, false );
                //Check if social media override is allowed
                allowOverrideForSocialMedia = companyProfile.isAllowOverrideForSocialMedia();
                model.addAttribute( "reviewsCount", reviewsCount );
            } catch ( InvalidInputException e ) {
                throw new InternalServerException(
                    new ProfileServiceErrorCode( CommonConstants.ERROR_CODE_COMPANY_PROFILE_SERVICE_FAILURE,
                        CommonConstants.SERVICE_CODE_COMPANY_PROFILE, "Error occured while fetching company profile" ),
                    e.getMessage() );
            }
            profileSettings = companyProfile;
            model.addAttribute( "companyProfileName", companyProfile.getProfileName() );
            model.addAttribute( "profileLevel", CommonConstants.PROFILE_LEVEL_COMPANY );
        } else if ( entityType.equals( CommonConstants.REGION_ID_COLUMN ) ) {
            //If the profile is a region profile
            model.addAttribute( "columnName", entityType );
            profilesMaster = CommonConstants.PROFILES_MASTER_REGION_ADMIN_PROFILE_ID;
            OrganizationUnitSettings regionProfile = null;
            OrganizationUnitSettings companyProfile = null;
            try {
                companyProfile = organizationManagementService.getCompanySettings( companyId );
                regionProfile = organizationManagementService.getRegionSettings( regionId );
                //fetch nmls-id
                try {
                    Integer nmlsId = null;
                    nmlsId = profileManagementService.fetchAndSaveNmlsId( regionProfile,
                        CommonConstants.REGION_SETTINGS_COLLECTION, user.getCompany().getCompanyId(), false, true );
                  model.addAttribute( "NMLS", nmlsId );
                } catch ( UnavailableException e ) {
                    LOG.error( "UnavailableException: message : " + e.getMessage(), e );
                }
                //set setting detail by region Setting
                setSettingSetByEntityInModel( model, regionProfile );
                //Check if social media override is allowed
                allowOverrideForSocialMedia = companyProfile.isAllowOverrideForSocialMedia();

                try {
                    map = profileManagementService.getPrimaryHierarchyByEntity( CommonConstants.REGION_ID,
                        regionProfile.getIden() );
                    if ( map == null ) {
                        LOG.error( "Unable to fetch primary profile for this user " );
                        throw new FatalException( "Unable to fetch primary profile this user " + regionProfile.getIden() );
                    }
                } catch ( InvalidSettingsStateException e ) {
                    throw new InternalServerException(
                        new ProfileServiceErrorCode( CommonConstants.ERROR_CODE_REGION_PROFILE_SERVICE_FAILURE,
                            CommonConstants.SERVICE_CODE_REGION_PROFILE, "Error occured while fetching region profile" ),
                        e.getMessage() );
                } catch ( ProfileNotFoundException e ) {
                    LOG.error( "No profile found for the user ", e );
                    return JspResolver.NO_PROFILES_FOUND;
                }

                regionProfile = profileManagementService.fillUnitSettings( regionProfile,
                    MongoOrganizationUnitSettingDaoImpl.REGION_SETTINGS_COLLECTION, companyProfile, regionProfile, null, null,
                    map, false );

                // aggregated disclaimer
                String disclaimer = profileManagementService.aggregateDisclaimer( regionProfile, CommonConstants.REGION_ID );
                regionProfile.setDisclaimer( disclaimer );
                regionProfile.setHiddenSection( companyProfile.isHiddenSection() );

                String json = new Gson().toJson( regionProfile );
                model.addAttribute( "profileJson", json );

                double averageRating = profileManagementService.getAverageRatings( regionId,
                    CommonConstants.PROFILE_LEVEL_REGION, false );
                model.addAttribute( "averageRating", averageRating );

                long reviewsCount = profileManagementService.getReviewsCount( regionId, CommonConstants.MIN_RATING_SCORE,
                    CommonConstants.MAX_RATING_SCORE, CommonConstants.PROFILE_LEVEL_REGION, false, false );
                model.addAttribute( "reviewsCount", reviewsCount );
            } catch ( InvalidInputException e ) {
                throw new InternalServerException(
                    new ProfileServiceErrorCode( CommonConstants.ERROR_CODE_REGION_PROFILE_SERVICE_FAILURE,
                        CommonConstants.SERVICE_CODE_REGION_PROFILE, "Error occured while fetching region profile" ),
                    e.getMessage() );
            }
            profileSettings = regionProfile;
            model.addAttribute( "companyProfileName", companyProfile.getProfileName() );
            model.addAttribute( "regionProfileName", regionProfile.getProfileName() );
            model.addAttribute( "profileLevel", CommonConstants.PROFILE_LEVEL_REGION );

        } else if ( entityType.equals( CommonConstants.BRANCH_ID_COLUMN ) ) {
            //If the profile is a branch profile
            model.addAttribute( "columnName", entityType );
            profilesMaster = CommonConstants.PROFILES_MASTER_BRANCH_ADMIN_PROFILE_ID;
            OrganizationUnitSettings companyProfile = null;
            OrganizationUnitSettings branchProfile = null;
            OrganizationUnitSettings regionProfile = null;

            try {
                companyProfile = organizationManagementService.getCompanySettings( companyId );
                regionProfile = organizationManagementService.getRegionSettings( regionId );
                branchProfile = organizationManagementService.getBranchSettingsDefault( branchId );
                //fetch nmls-id
                try {
                    Integer nmlsId = null;
                    nmlsId = profileManagementService.fetchAndSaveNmlsId( branchProfile,
                        CommonConstants.BRANCH_SETTINGS_COLLECTION, user.getCompany().getCompanyId(), false, true );
                  model.addAttribute( "NMLS", nmlsId );
                } catch ( UnavailableException e ) {
                    LOG.error( "UnavailableException: message : " + e.getMessage(), e );
                }

                //set setting detail by branch Setting
                setSettingSetByEntityInModel( model, branchProfile );

                //Check if social media override is allowed
                allowOverrideForSocialMedia = companyProfile.isAllowOverrideForSocialMedia();
                try {
                    map = profileManagementService.getPrimaryHierarchyByEntity( CommonConstants.BRANCH_ID_COLUMN,
                        branchProfile.getIden() );
                    if ( map == null ) {
                        LOG.error( "Unable to fetch primary profile for this user " );
                        throw new FatalException( "Unable to fetch primary profile this user " + branchProfile.getIden() );
                    }

                } catch ( InvalidSettingsStateException e ) {
                    throw new InternalServerException(
                        new ProfileServiceErrorCode( CommonConstants.ERROR_CODE_BRANCH_PROFILE_SERVICE_FAILURE,
                            CommonConstants.SERVICE_CODE_BRANCH_PROFILE, "Error occured while fetching branch profile" ),
                        e.getMessage() );
                } catch ( ProfileNotFoundException e ) {
                    LOG.error( "No profile found for the user ", e );
                    return JspResolver.NO_PROFILES_FOUND;
                }
                branchProfile = profileManagementService.fillUnitSettings( branchProfile,
                    MongoOrganizationUnitSettingDaoImpl.BRANCH_SETTINGS_COLLECTION, companyProfile, regionProfile,
                    branchProfile, null, map, false );
                // aggregated disclaimer
                String disclaimer = profileManagementService.aggregateDisclaimer( branchProfile, CommonConstants.BRANCH_ID );
                branchProfile.setDisclaimer( disclaimer );
                branchProfile.setHiddenSection( companyProfile.isHiddenSection() );

                String json = new Gson().toJson( branchProfile );
                model.addAttribute( "profileJson", json );

                double averageRating = profileManagementService.getAverageRatings( branchId,
                    CommonConstants.PROFILE_LEVEL_BRANCH, false );
                model.addAttribute( "averageRating", averageRating );

                long reviewsCount = profileManagementService.getReviewsCount( branchId, CommonConstants.MIN_RATING_SCORE,
                    CommonConstants.MAX_RATING_SCORE, CommonConstants.PROFILE_LEVEL_BRANCH, false, false );
                model.addAttribute( "reviewsCount", reviewsCount );
              
            } catch ( InvalidInputException e ) {
                throw new InternalServerException(
                    new ProfileServiceErrorCode( CommonConstants.ERROR_CODE_BRANCH_PROFILE_SERVICE_FAILURE,
                        CommonConstants.SERVICE_CODE_BRANCH_PROFILE, "Error occured while fetching branch profile" ),
                    e.getMessage() );
            } catch ( NoRecordsFetchedException e ) {
                LOG.error( "NoRecordsFetchedException: message : " + e.getMessage(), e );
            }
            profileSettings = branchProfile;
            model.addAttribute( "companyProfileName", companyProfile.getProfileName() );
            model.addAttribute( "branchProfileName", branchProfile.getProfileName() );
            model.addAttribute( "profileLevel", CommonConstants.PROFILE_LEVEL_BRANCH );

        } else if ( entityType.equals( CommonConstants.AGENT_ID_COLUMN ) ) {
            //If the profile is a individual profile
            model.addAttribute( "columnName", entityType );
            profilesMaster = CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID;
            OrganizationUnitSettings companyProfile = null;
            OrganizationUnitSettings regionProfile = null;
            OrganizationUnitSettings branchProfile = null;
            AgentSettings individualProfile = null;

            try {
                companyProfile = organizationManagementService.getCompanySettings( companyId );
                regionProfile = organizationManagementService.getRegionSettings( regionId );
                branchProfile = organizationManagementService.getBranchSettingsDefault( branchId );
                individualProfile = userManagementService.getAgentSettingsForUserProfiles( agentId );

                //set setting detail by agent Setting
                setSettingSetByEntityInModel( model, individualProfile );

                //Check if social media override is allowed
                allowOverrideForSocialMedia = companyProfile.isAllowOverrideForSocialMedia();

                try {
                    map = profileManagementService.getPrimaryHierarchyByEntity( CommonConstants.AGENT_ID_COLUMN,
                        individualProfile.getIden() );
                    if ( map == null ) {
                        LOG.error( "Unable to fetch primary profile for this user " );
                        throw new FatalException( "Unable to fetch primary profile this user " + branchProfile.getIden() );
                    }

                } catch ( InvalidSettingsStateException e ) {
                    LOG.error( "Error occured while fetching branch profile" + e.getMessage() );
                } catch ( ProfileNotFoundException e ) {
                    LOG.error( "No profile found for the user " );
                    return JspResolver.NO_PROFILES_FOUND;
                }

                if ( map == null ) {
                    LOG.error( "Unable to fetch primary profile for this user " );
                    throw new FatalException( "Unable to fetch primary profile this user " + individualProfile.getIden() );
                }

                individualProfile = (AgentSettings) profileManagementService.fillUnitSettings( individualProfile,
                    MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION, companyProfile, regionProfile, branchProfile,
                    individualProfile, map, false );
                individualProfile.setVertical( user.getCompany().getVerticalsMaster().getVerticalName() );
                String disclaimer = profileManagementService.aggregateDisclaimer( individualProfile, CommonConstants.AGENT_ID );
                individualProfile.setDisclaimer( disclaimer );
                individualProfile.setHiddenSection( companyProfile.isHiddenSection() );

                String json = new Gson().toJson( individualProfile );
                model.addAttribute( "profileJson", json );

                double averageRating = profileManagementService.getAverageRatings( agentId,
                    CommonConstants.PROFILE_LEVEL_INDIVIDUAL, false );
                model.addAttribute( "averageRating", averageRating );
                long reviewsCount = profileManagementService.getReviewsCount( agentId, CommonConstants.MIN_RATING_SCORE,
                    CommonConstants.MAX_RATING_SCORE, CommonConstants.PROFILE_LEVEL_INDIVIDUAL, false, false );
                model.addAttribute( "reviewsCount", reviewsCount );

                profileSettings = individualProfile;
                
              //fetch nmls-id
                try {
                    Integer nmlsId = null;
                    nmlsId = profileManagementService.fetchAndSaveNmlsId( individualProfile,
                        CommonConstants.AGENT_SETTINGS_COLLECTION, user.getCompany().getCompanyId(), false, true );
                  model.addAttribute( "NMLS", nmlsId );
                } catch ( UnavailableException e ) {
                    LOG.error( "UnavailableException: message : " + e.getMessage(), e );
                }
            } catch ( InvalidInputException e ) {
                LOG.error( "InvalidInputException: message : " + e.getMessage(), e );
                model.addAttribute( "message",
                    messageUtils.getDisplayMessage( DisplayMessageConstants.INVALID_INDIVIDUAL_PROFILENAME,
                        DisplayMessageType.ERROR_MESSAGE ).getMessage() );
                return JspResolver.NOT_FOUND_PAGE;
            } catch ( NoRecordsFetchedException e ) {
                LOG.error( "NoRecordsFetchedException: message : " + e.getMessage(), e );
            }

        }

        
        model.addAttribute( "allowOverrideForSocialMedia", allowOverrideForSocialMedia );
        model.addAttribute( "profileSettings", profileSettings );
        session.setAttribute( CommonConstants.USER_PROFILE_SETTINGS, profileSettings );
        return JspResolver.REPORTING_DASHBOARD; 
    }
    
    @ResponseBody
    @RequestMapping ( value = "/showreportingoverview", method = RequestMethod.GET)
    public String reportingOverviewStats(Model model, HttpServletRequest request) throws NonFatalException
    {
        LOG.info( "Reporting Dashboard Page started" );
        HttpSession session = request.getSession( false );
        User user = sessionHelper.getCurrentUser();

        String json = null;
        if ( user == null ) {
            throw new NonFatalException( "NonFatalException while logging in. " );
        }    
        long entityId = (long) session.getAttribute( CommonConstants.ENTITY_ID_COLUMN );
        String entityType = (String) session.getAttribute( CommonConstants.ENTITY_TYPE_COLUMN );
        List<Object> overview = new ArrayList<>();
        if ( entityType.equals( CommonConstants.AGENT_ID_COLUMN )) {
            OverviewUser overviewUser = overviewManagement.fetchOverviewUserDetails(entityId, entityType); 
            overview.add( overviewUser.getSpsScore() );
            overview.add( overviewUser.getDetractorPercentage());
            overview.add( overviewUser.getPassivesPercentage() );
            overview.add( overviewUser.getPromoterPercentage() );
            overview.add( overviewUser.getTotalIncompleteTransactions() );
            overview.add( overviewUser.getCorruptedPercentage() );
            overview.add( overviewUser.getDuplicatePercentage() );
            overview.add( overviewUser.getArchievedPercentage());
            overview.add( overviewUser.getMismatchedPercentage());
            overview.add( overviewUser.getTotalSurveySent() );
            overview.add( overviewUser.getTotalSurveyCompleted() );
            overview.add( overviewUser.getTotalSocialPost() );
            overview.add( overviewUser.getTotalZillowReviews() );
        }else if(entityType.equals( CommonConstants.BRANCH_ID_COLUMN )){
            OverviewBranch overviewBranch = overviewManagement.fetchOverviewBranchDetails( entityId, entityType );
            overview.add( overviewBranch.getSpsScore() );
            overview.add( overviewBranch.getDetractorPercentage());
            overview.add( overviewBranch.getPassivesPercentage() );
            overview.add( overviewBranch.getPromoterPercentage() );
            overview.add( overviewBranch.getTotalIncompleteTransactions() );
            overview.add( overviewBranch.getCorruptedPercentage() );
            overview.add( overviewBranch.getDuplicatePercentage() );
            overview.add( overviewBranch.getArchievedPercentage());
            overview.add( overviewBranch.getMismatchedPercentage());
            overview.add( overviewBranch.getTotalSurveySent() );
            overview.add( overviewBranch.getTotalSurveyCompleted() );
            overview.add( overviewBranch.getTotalSocialPost() );
            overview.add( overviewBranch.getTotalZillowReviews() );
        }else if(entityType.equals( CommonConstants.REGION_ID_COLUMN )){
           OverviewRegion overviewRegion = overviewManagement.fetchOverviewRegionDetails( entityId, entityType );
           overview.add( overviewRegion.getSpsScore() );
           overview.add( overviewRegion.getDetractorPercentage());
           overview.add( overviewRegion.getPassivesPercentage() );
           overview.add( overviewRegion.getPromoterPercentage() );
           overview.add( overviewRegion.getTotalIncompleteTransactions() );
           overview.add( overviewRegion.getCorruptedPercentage() );
           overview.add( overviewRegion.getDuplicatePercentage() );
           overview.add( overviewRegion.getArchievedPercentage());
           overview.add( overviewRegion.getMismatchedPercentage());
           overview.add( overviewRegion.getTotalSurveySent() );
           overview.add( overviewRegion.getTotalSurveyCompleted() );
           overview.add( overviewRegion.getTotalSocialPost() );
           overview.add( overviewRegion.getTotalZillowReviews() );
        }else if(entityType.equals( CommonConstants.COMPANY_ID_COLUMN )){
            OverviewCompany overviewCompany = overviewManagement.fetchOverviewCompanyDetails( entityId, entityType );
            overview.add( overviewCompany.getSpsScore() );
            overview.add( overviewCompany.getDetractorPercentage());
            overview.add( overviewCompany.getPassivesPercentage() );
            overview.add( overviewCompany.getPromoterPercentage() );
            overview.add( overviewCompany.getTotalIncompleteTransactions() );
            overview.add( overviewCompany.getCorruptedPercentage() );
            overview.add( overviewCompany.getDuplicatePercentage() );
            overview.add( overviewCompany.getArchievedPercentage());
            overview.add( overviewCompany.getMismatchedPercentage());
            overview.add( overviewCompany.getTotalSurveySent() );
            overview.add( overviewCompany.getTotalSurveyCompleted() );
            overview.add( overviewCompany.getTotalSocialPost() );
            overview.add( overviewCompany.getTotalZillowReviews() );
        }
        json = new Gson().toJson( overview );
        if(json == null && json.length() <= 0){
            throw new NonFatalException( "NonFatalException while fetching data. " );
        }
        return json; 
    }   
  
    /*
     * Method to get profile details for displaying
     */
    @RequestMapping ( value = "/showreportingprofiledetails")
    public String getProfileDetails( Model model, HttpServletRequest request )
    {
        LOG.info( "Method to get profile of company/region/branch/agent getProfileDetails() started" );
        User user = sessionHelper.getCurrentUser();

        // settings profile details
        String columnName = request.getParameter( "columnName" );
        String realtechAdminStr = request.getParameter( "realtechAdmin" );
        boolean realtechAdmin = false;

        OrganizationUnitSettings unitSettings = null;
        long columnValue = 0;
        try {
            if ( columnName.equalsIgnoreCase( CommonConstants.COMPANY_ID_COLUMN ) ) {
                columnValue = user.getCompany().getCompanyId();

                unitSettings = organizationManagementService.getCompanySettings( user );
                if ( unitSettings.getContact_details() != null && unitSettings.getContact_details().getName() != null ) {
                    model.addAttribute( "name", unitSettings.getContact_details().getName() );
                }
                model.addAttribute( "title", unitSettings.getContact_details().getTitle() );
                model.addAttribute( "location", unitSettings.getContact_details().getLocation() );
                model.addAttribute( "vertical", unitSettings.getVertical() );
            } else if ( columnName.equalsIgnoreCase( CommonConstants.REGION_ID_COLUMN ) ) {

                try {
                    columnValue = Long.parseLong( request.getParameter( "columnValue" ) );
                } catch ( NumberFormatException e ) {
                    LOG.error(
                        "NumberFormatException caught in getProfileDetails() while converting columnValue for regionId/branchId/agentId." );
                    throw e;
                }

                unitSettings = organizationManagementService.getRegionSettings( columnValue );
                if ( unitSettings.getContact_details() != null && unitSettings.getContact_details().getName() != null ) {
                    model.addAttribute( "name", unitSettings.getContact_details().getName() );
                }
                model.addAttribute( "title", unitSettings.getContact_details().getTitle() );
                model.addAttribute( "company", user.getCompany().getCompany() );
                model.addAttribute( "location", unitSettings.getContact_details().getLocation() );
                model.addAttribute( "vertical", unitSettings.getVertical() );
            } else if ( columnName.equalsIgnoreCase( CommonConstants.BRANCH_ID_COLUMN ) ) {

                try {
                    columnValue = Long.parseLong( request.getParameter( "columnValue" ) );
                } catch ( NumberFormatException e ) {
                    LOG.error(
                        "NumberFormatException caught in getProfileDetails() while converting columnValue for regionId/branchId/agentId." );
                    throw e;
                }

                unitSettings = organizationManagementService.getBranchSettingsDefault( columnValue );
                if ( unitSettings.getContact_details() != null && unitSettings.getContact_details().getName() != null ) {
                    model.addAttribute( "name", unitSettings.getContact_details().getName() );
                }
                model.addAttribute( "title", unitSettings.getContact_details().getTitle() );
                model.addAttribute( "company", user.getCompany().getCompany() );
                model.addAttribute( "location", unitSettings.getContact_details().getLocation() );
                model.addAttribute( "vertical", unitSettings.getVertical() );
            } else if ( columnName.equalsIgnoreCase( CommonConstants.AGENT_ID_COLUMN ) ) {
                columnValue = user.getUserId();

                unitSettings = userManagementService.getUserSettings( columnValue );
                model.addAttribute( "name",
                    user.getFirstName() + " " + ( user.getLastName() != null ? user.getLastName() : "" ) );
                model.addAttribute( "title", unitSettings.getContact_details().getTitle() );
                model.addAttribute( "company", user.getCompany().getCompany() );
                model.addAttribute( "location", unitSettings.getContact_details().getLocation() );
                model.addAttribute( "vertical", unitSettings.getVertical() );
            } else if ( realtechAdminStr != null && !realtechAdminStr.isEmpty() ) {
                realtechAdmin = Boolean.parseBoolean( realtechAdminStr );
            }

            boolean allowOverrideForSocialMedia = false;
            boolean hiddenSection = false;
            //Code to determine if social media can be overridden during autologin and the value for hiddenSection
            if ( columnName.equalsIgnoreCase( CommonConstants.COMPANY_ID_COLUMN ) ) {
                allowOverrideForSocialMedia = unitSettings.isAllowOverrideForSocialMedia();
                hiddenSection = unitSettings.isHiddenSection();
            } else {
                OrganizationUnitSettings companySettings = organizationManagementService
                    .getCompanySettings( user.getCompany().getCompanyId() );
                allowOverrideForSocialMedia = companySettings.isAllowOverrideForSocialMedia();
                hiddenSection = companySettings.isHiddenSection();
            }
            model.addAttribute( "allowOverrideForSocialMedia", allowOverrideForSocialMedia );
            model.addAttribute( "hiddenSection", hiddenSection );

            model.addAttribute( "columnName", columnName );
            model.addAttribute( "columnValue", columnValue );

            LOG.info( "Method to get profile of company/region/branch/agent getProfileDetails() finished" );
            return JspResolver.REPORTING_PROFILE;
        } catch ( InvalidInputException | NoRecordsFetchedException e ) {
            LOG.error( "NonFatalException while fetching profile details. Reason :" + e.getMessage(), e );
            model.addAttribute( "message",
                messageUtils.getDisplayMessage( DisplayMessageConstants.GENERAL_ERROR, DisplayMessageType.ERROR_MESSAGE ) );
            return JspResolver.REPORTING_PROFILE;
        }
    }

    @RequestMapping ( value = "/fetchreportingprofileimage", method = RequestMethod.GET)
    public String fetchProfileImage( Model model, HttpServletRequest request )
    {
        LOG.info( "Fetching profile image" );
        return JspResolver.REPORTING_PROFILE_IMAGE;
    }
    
   @ResponseBody
   @RequestMapping( value = "/fetchaveragereportingrating", method = RequestMethod.GET)
   public String fetchAverageRating( Model model, HttpServletRequest request ) throws NonFatalException 
   {
        LOG.info( "Fetching Average Rating Graph" );
        HttpSession session = request.getSession( false );
        User user = sessionHelper.getCurrentUser();
        String json = null;

        if ( user == null ) {
            throw new NonFatalException( "NonFatalException while logging in. " );
        }    
        long entityId = (long) session.getAttribute( CommonConstants.ENTITY_ID_COLUMN );
        String entityType = (String) session.getAttribute( CommonConstants.ENTITY_TYPE_COLUMN );
        if(entityType.equals( CommonConstants.COMPANY_ID_COLUMN )){
            List<List <Object>> averageRating = DashboardGraphManagement.getAverageReviewRating( entityId , entityType );
            json = new Gson().toJson( averageRating );
        }else if(entityType.equals( CommonConstants.REGION_ID_COLUMN )){
            List<List <Object>> averageRating = DashboardGraphManagement.getAverageReviewRating( entityId , entityType );
            json = new Gson().toJson( averageRating );
        }else if(entityType.equals( CommonConstants.BRANCH_ID_COLUMN )){
            List<List <Object>> averageRating = DashboardGraphManagement.getAverageReviewRating( entityId , entityType );
            json = new Gson().toJson( averageRating );
        }else if(entityType.equals( CommonConstants.AGENT_ID_COLUMN )){
            List<List <Object>> averageRating = DashboardGraphManagement.getAverageReviewRating( entityId , entityType );
            json = new Gson().toJson( averageRating );
        }
    return json;
        
   }
   
   @ResponseBody
   @RequestMapping( value = "/fetchreportingspsstats", method = RequestMethod.GET)
   public String fetchSpsStats( Model model, HttpServletRequest request ) throws NonFatalException 
   {
        LOG.info( "Fetching Sps Stats Graph" );
        HttpSession session = request.getSession( false );
        User user = sessionHelper.getCurrentUser();
        String json = null;

        if ( user == null ) {
            throw new NonFatalException( "NonFatalException while logging in. " );
        }    
        long entityId = (long) session.getAttribute( CommonConstants.ENTITY_ID_COLUMN );
        String entityType = (String) session.getAttribute( CommonConstants.ENTITY_TYPE_COLUMN );
        if(entityType.equals( CommonConstants.COMPANY_ID_COLUMN )){
            List<List <Object>> spsStats = DashboardGraphManagement.getSpsStatsGraph( entityId , entityType );
            json = new Gson().toJson( spsStats );
        }else if(entityType.equals( CommonConstants.REGION_ID_COLUMN )){
            List<List <Object>> spsStats = DashboardGraphManagement.getSpsStatsGraph( entityId , entityType );
            json = new Gson().toJson( spsStats );
        }else if(entityType.equals( CommonConstants.BRANCH_ID_COLUMN )){
            List<List <Object>> spsStats = DashboardGraphManagement.getSpsStatsGraph( entityId , entityType );
            json = new Gson().toJson( spsStats );
        }else if(entityType.equals( CommonConstants.AGENT_ID_COLUMN )){
            List<List <Object>> spsStats = DashboardGraphManagement.getSpsStatsGraph( entityId , entityType );
            json = new Gson().toJson( spsStats );
        }
    return json;
        
   }
   
   @ResponseBody
   @RequestMapping( value = "/fetchreportingcompletionrate", method = RequestMethod.GET)
   public String fetchCompletionRate( Model model, HttpServletRequest request ) throws NonFatalException 
   {
        LOG.info( "Fetching Completion Rate Graph" );
        HttpSession session = request.getSession( false );
        User user = sessionHelper.getCurrentUser();
        String json = null;

        if ( user == null ) {
            throw new NonFatalException( "NonFatalException while logging in. " );
        }    
        long entityId = (long) session.getAttribute( CommonConstants.ENTITY_ID_COLUMN );
        String entityType = (String) session.getAttribute( CommonConstants.ENTITY_TYPE_COLUMN );
        if(entityType.equals( CommonConstants.COMPANY_ID_COLUMN )){
            List<List <Object>> completionRate = DashboardGraphManagement.getCompletionRate( entityId , entityType );
            json = new Gson().toJson( completionRate );
        }else if(entityType.equals( CommonConstants.REGION_ID_COLUMN )){
            List<List <Object>> completionRate = DashboardGraphManagement.getCompletionRate( entityId , entityType );
            json = new Gson().toJson( completionRate );
        }else if(entityType.equals( CommonConstants.BRANCH_ID_COLUMN )){
            List<List <Object>> completionRate = DashboardGraphManagement.getCompletionRate( entityId , entityType );
            json = new Gson().toJson( completionRate );
        }else if(entityType.equals( CommonConstants.AGENT_ID_COLUMN )){
            List<List <Object>> completionRate = DashboardGraphManagement.getCompletionRate( entityId , entityType );
            json = new Gson().toJson( completionRate );
        }
    return json;
        
   }
   
   /*
    * Generate Reports For the reporting UI
    */
   @SuppressWarnings ( "deprecation")
   @ResponseBody
   @RequestMapping( value = "/generatereportingreports", method = RequestMethod.GET)
   public String generateReportingReports( Model model, HttpServletRequest request, HttpServletResponse response ) throws NonFatalException, ParseException{
       LOG.info( "the step to generate reporting reports :generateReportingReports started " );
       HttpSession session = request.getSession( false );
       User user = sessionHelper.getCurrentUser();
       String message = "";
       //since we need to store the current time stamp
       
       if ( user == null ) {
           throw new NonFatalException( "NonFatalException while logging in. " );
       } 
       Date currentDate = null;
       String currentDateStr = request.getParameter( "currentDate" );
       if ( currentDateStr != null && !currentDateStr.isEmpty() ) {
           currentDate =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse( currentDateStr ) ;
           //String formattedCurrentDate = new SimpleDateFormat("yyyyMMdd").format(currentDate);
       }
       Date startDate = null;
       String startDateStr = request.getParameter( "startDate" );
       if ( startDateStr != null && !startDateStr.isEmpty() ) {
           startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse( startDateStr );
       }
       Date endDate = null;
       String endDateStr = request.getParameter( "endDate" );
       if( endDateStr != null && !endDateStr.isEmpty()){
           endDate =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse( endDateStr ) ;
       }
       //check if only endDate is present
       if(endDate!= null && startDate == null){
           message = "The StartDate needs to be present";
       }
       //check if enddate is greater the start date 
       if( endDate != null && startDate != null){
           message = "The EndDate Should be lesser then the StartDate";
       }
       String reportIdString = request.getParameter( "reportId" );
       int reportId = Integer.parseInt( reportIdString );
       Long userId = user.getUserId();
       String firstName = user.getFirstName();
       String lastName = user.getLastName();
       long entityId = (long) session.getAttribute( CommonConstants.ENTITY_ID_COLUMN );
       String entityType = (String) session.getAttribute( CommonConstants.ENTITY_TYPE_COLUMN );
       ReportingDashboardManagement.generateReports( reportId, startDate, endDate, currentDate, firstName, lastName, entityId, entityType );
       message = "the report is being generated";
       return message;
       
   }
   
   //TO SHOW REPORTING UI
   @RequestMapping ( value = "/showreportspage", method = RequestMethod.GET)
   public String showReportsPage( Model model, HttpServletRequest request )
   {
       LOG.info( "Showing reports page" );
       return JspResolver.REPORTS;
   }
    /**
    *
    * @param model
    * @param entitySetting
    * @throws InvalidInputException
    */
   private void setSettingSetByEntityInModel( Model model, OrganizationUnitSettings entitySetting )
       throws InvalidInputException
   {
       LOG.debug( "method setSettingSetByEntityInModel() started " );
       boolean isLogoSetByEntity;
       boolean isContactNoSetByEntity;
       boolean isWebAddressSetByEntity;
       boolean isWorkEmailSetByEntity;

       if ( entitySetting == null ) {
           throw new InvalidInputException( "Passed entity setting is null" );
       }

       if ( entitySetting.getLogo() == null || entitySetting.getLogo().isEmpty() ) {
           isLogoSetByEntity = false;
       } else {
           isLogoSetByEntity = true;
       }

       if ( entitySetting.getContact_details() != null && entitySetting.getContact_details().getWeb_addresses() != null
           && entitySetting.getContact_details().getWeb_addresses().getWork() != null
           && !entitySetting.getContact_details().getWeb_addresses().getWork().isEmpty() ) {
           isWebAddressSetByEntity = true;
       } else {
           isWebAddressSetByEntity = false;
       }

       if ( entitySetting.getContact_details() != null && entitySetting.getContact_details().getContact_numbers() != null
           && entitySetting.getContact_details().getContact_numbers().getWork() != null
           && !entitySetting.getContact_details().getContact_numbers().getWork().isEmpty() ) {
           isContactNoSetByEntity = true;
       } else {
           isContactNoSetByEntity = false;
       }

       if ( entitySetting.getContact_details() != null && entitySetting.getContact_details().getMail_ids() != null
           && entitySetting.getContact_details().getMail_ids().getWork() != null
           && !entitySetting.getContact_details().getMail_ids().getWork().isEmpty() ) {
           isWorkEmailSetByEntity = true;
       } else {
           isWorkEmailSetByEntity = false;
       }

       model.addAttribute( "isLogoSetByEntity", isLogoSetByEntity );
       model.addAttribute( "isWebAddressSetByEntity", isWebAddressSetByEntity );
       model.addAttribute( "isContactNoSetByEntity", isContactNoSetByEntity );
       model.addAttribute( "isWorkEmailSetByEntity", isWorkEmailSetByEntity );

       LOG.debug( "method setSettingSetByEntityInModel() ended " );
   }
}


