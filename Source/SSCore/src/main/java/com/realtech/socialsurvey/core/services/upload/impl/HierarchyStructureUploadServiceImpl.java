package com.realtech.socialsurvey.core.services.upload.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.realtech.socialsurvey.core.commons.CommonConstants;
import com.realtech.socialsurvey.core.commons.Utils;
import com.realtech.socialsurvey.core.dao.BranchDao;
import com.realtech.socialsurvey.core.dao.HierarchyUploadDao;
import com.realtech.socialsurvey.core.dao.OrganizationUnitSettingsDao;
import com.realtech.socialsurvey.core.dao.RegionDao;
import com.realtech.socialsurvey.core.dao.UserDao;
import com.realtech.socialsurvey.core.dao.UserProfileDao;
import com.realtech.socialsurvey.core.dao.impl.MongoOrganizationUnitSettingDaoImpl;
import com.realtech.socialsurvey.core.entities.AgentSettings;
import com.realtech.socialsurvey.core.entities.BooleanUploadHistory;
import com.realtech.socialsurvey.core.entities.Branch;
import com.realtech.socialsurvey.core.entities.BranchUploadVO;
import com.realtech.socialsurvey.core.entities.Company;
import com.realtech.socialsurvey.core.entities.ContactDetailsSettings;
import com.realtech.socialsurvey.core.entities.ContactNumberSettings;
import com.realtech.socialsurvey.core.entities.HierarchyUpload;
import com.realtech.socialsurvey.core.entities.LicenseDetail;
import com.realtech.socialsurvey.core.entities.Licenses;
import com.realtech.socialsurvey.core.entities.LongUploadHistory;
import com.realtech.socialsurvey.core.entities.OrganizationUnitSettings;
import com.realtech.socialsurvey.core.entities.Region;
import com.realtech.socialsurvey.core.entities.RegionUploadVO;
import com.realtech.socialsurvey.core.entities.StringListUploadHistory;
import com.realtech.socialsurvey.core.entities.StringUploadHistory;
import com.realtech.socialsurvey.core.entities.User;
import com.realtech.socialsurvey.core.entities.UserProfile;
import com.realtech.socialsurvey.core.entities.UserUploadVO;
import com.realtech.socialsurvey.core.entities.WebAddressSettings;
import com.realtech.socialsurvey.core.exception.BranchAdditionException;
import com.realtech.socialsurvey.core.exception.InvalidInputException;
import com.realtech.socialsurvey.core.exception.NoRecordsFetchedException;
import com.realtech.socialsurvey.core.exception.RegionAdditionException;
import com.realtech.socialsurvey.core.exception.UserAdditionException;
import com.realtech.socialsurvey.core.services.organizationmanagement.OrganizationManagementService;
import com.realtech.socialsurvey.core.services.organizationmanagement.ProfileManagementService;
import com.realtech.socialsurvey.core.services.organizationmanagement.UserAssignmentException;
import com.realtech.socialsurvey.core.services.organizationmanagement.UserManagementService;
import com.realtech.socialsurvey.core.services.search.SolrSearchService;
import com.realtech.socialsurvey.core.services.search.exception.SolrException;
import com.realtech.socialsurvey.core.services.social.SocialManagementService;
import com.realtech.socialsurvey.core.services.upload.HierarchyStructureUploadService;
import com.realtech.socialsurvey.core.utils.DisplayMessageConstants;


@Component
public class HierarchyStructureUploadServiceImpl implements HierarchyStructureUploadService
{

    private static Logger LOG = LoggerFactory.getLogger( HierarchyStructureUploadServiceImpl.class );

    @Autowired
    private OrganizationManagementService organizationManagementService;

    @Autowired
    private RegionDao regionDao;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private UserDao userDao;

    @Resource
    @Qualifier ( "branch")
    private BranchDao branchDao;

    @Autowired
    private ProfileManagementService profileManagementService;

    @Autowired
    private OrganizationUnitSettingsDao organizationUnitSettingsDao;

    @Autowired
    private SolrSearchService solrSearchService;

    @Autowired
    private SocialManagementService socialManagementService;
    
    @Value ( "${MASK_EMAIL_ADDRESS}")
    private String maskEmail;
    
    @Autowired
    private Utils utils;
    
    @Autowired
    private UserProfileDao userProfileDao;
    
    @Autowired
    private HierarchyUploadDao hierarchyUploadDao;


    @Override
    public void uploadHierarchy( HierarchyUpload upload, Company company, User user ) throws InvalidInputException
    {
        // the upload object should have the current value as well the changes made by the user in the sheet/ UI
        if ( upload == null ) {
            LOG.error( "No upload object to upload." );
            throw new InvalidInputException( "No upload object to upload." );
        }
        if ( company == null ) {
            LOG.error( "No company object to upload." );
            throw new InvalidInputException( "No company object to upload." );
        }
        if ( user == null ) {
            LOG.error( "Invalid user details to upload." );
            throw new InvalidInputException( "Invalid user details to upload." );
        }
        if ( !user.isCompanyAdmin() ) {
            LOG.error( "User is not authorized to upload hierarchy." );
            throw new InvalidInputException( "User is not authorized to upload hierarchy." );
        }
        LOG.info( "Uploading hierarchy for company " + upload.getCompanyId() );
        // start with addition and modification of each unit starting from the highest hierarchy and then deletion starting from the lowest hierarchy
        // uploading regions
        uploadRegions( upload, user, company );
        // Uploading branches
        uploadBranches( upload, user, company );
        // Uploading users
        uploadUsers( upload, user );
        // Delete users
        deleteUsers( upload, user, company );
        // Delete branches
        deleteBranches( upload, user, company );
        // Delete regions
        deleteRegions( upload, user, company );
        
        hierarchyUploadDao.saveHierarchyUploadObject( upload );
    }


    @Transactional
    void deleteUsers( HierarchyUpload upload, User adminUser, Company company )
    {
        LOG.debug( "Deleting removed users" );
        List<UserUploadVO> userList = upload.getUsers();
        if ( userList == null || userList.isEmpty() ) {
            LOG.warn( "Empty userList" );
            return;
        }
        for ( UserUploadVO user : userList ) {
            if ( user.isDeletedRecord() ) {
                // Delete the user
                try {
                    userManagementService.removeExistingUser( adminUser, user.getUserId() );
                    // update the user count modificaiton notification
                    userManagementService.updateUserCountModificationNotification( adminUser.getCompany() );
                    LOG.debug( "Removing user {} from solr.", user.getUserId() );
                    solrSearchService.removeUserFromSolr( user.getUserId() );
                    upload.getUsers().remove( user );
                    upload.getUserSourceMapping().remove( user.getSourceUserId() );
                } catch ( Exception e ) {
                    // TODO:process errors and return them to the user
                }
            }
        }
        LOG.debug( "Finished deleting removed users" );
    }


    void deleteBranches( HierarchyUpload upload, User adminUser, Company company )
    {
        LOG.info( "Deleting branches" );
        List<BranchUploadVO> branches = upload.getBranches();
        if ( branches == null || branches.isEmpty() ) {
            LOG.warn( "Empty branch list" );
            return;
        }
        for ( BranchUploadVO branch : branches ) {
            if ( branch.isDeletedRecord() ) {
                try {
                    // Check if branch can be deleted
                    LOG.debug( "Calling service to get the count of users in branch" );
                    long usersCount = organizationManagementService.getCountUsersInBranch( branch.getBranchId() );
                    LOG.debug( "Successfully executed service to get the count of users in branch : " + usersCount );

                    if ( usersCount > 0l ) {
                        LOG.error( "Cannot delete branch : " + branch.getBranchName()
                            + ". There are active users in the branch." );
                        throw new InvalidInputException( "Cannot delete branch : " + branch.getBranchName()
                            + ". There are active users in the branch." );
                    } else {
                        //Delete the branch
                        LOG.debug( "Calling service to deactivate branch" );
                        organizationManagementService.updateBranchStatus( adminUser, branch.getBranchId(),
                            CommonConstants.STATUS_INACTIVE );
                        //TODO: Remove branch from session?
                        //update profile name and url
                        organizationManagementService.updateProfileUrlAndStatusForDeletedEntity(
                            CommonConstants.BRANCH_ID_COLUMN, branch.getBranchId() );
                        //remove social media connections
                        socialManagementService.disconnectAllSocialConnections( CommonConstants.BRANCH_ID_COLUMN,
                            branch.getBranchId() );
                        upload.getBranches().remove( branch );
                        upload.getBranchSourceMapping().remove( branch.getSourceBranchId() );
                    }

                } catch ( Exception e ) {
                    //TODO: process errors and return them to the user
                    e.printStackTrace();
                }
            }
        }
        LOG.info( "Finished deleting branches" );
    }


    void deleteRegions( HierarchyUpload upload, User adminUser, Company company )
    {
        LOG.info( "Deleting regions" );
        List<RegionUploadVO> regions = upload.getRegions();
        if ( regions == null || regions.isEmpty() ) {
            LOG.warn( "Empty region list" );
            return;
        }
        for ( RegionUploadVO region : regions ) {
            if ( region.isDeletedRecord() ) {
                try {

                    //Check if the region can be deleted
                    LOG.debug( "Calling service to get the count of branches in region" );
                    long branchCount = organizationManagementService.getCountBranchesInRegion( region.getRegionId() );
                    LOG.debug( "Successfully executed service to get the count of branches in region : " + branchCount );

                    if ( branchCount > 0l ) {
                        LOG.error( "Cannot delete region : " + region.getRegionName()
                            + ". There are active branches in the region." );
                        throw new InvalidInputException( "Cannot delete region: " + region.getRegionName()
                            + ". There are active branches in the region." );
                    } else {
                        // Delete the region
                        LOG.debug( "Calling service to deactivate region" );
                        organizationManagementService.updateRegionStatus( adminUser, region.getRegionId(),
                            CommonConstants.STATUS_INACTIVE );

                        //TODO: Remove region from session?

                        //update profile name and url
                        organizationManagementService.updateProfileUrlAndStatusForDeletedEntity(
                            CommonConstants.REGION_ID_COLUMN, region.getRegionId() );
                        //remove social media connections
                        socialManagementService.disconnectAllSocialConnections( CommonConstants.REGION_ID_COLUMN,
                            region.getRegionId() );
                        upload.getRegions().remove( region );
                        upload.getRegionSourceMapping().remove( region.getSourceRegionId() );
                    }

                } catch ( Exception e ) {
                    //TODO: process errors and return them to the user
                    e.printStackTrace();
                }
            }
        }
    }


    @Transactional
    void uploadBranches( HierarchyUpload upload, User user, Company company )
    {
        LOG.debug( "Uploading new branches" );
        List<BranchUploadVO> branchesToBeUploaded = upload.getBranches();
        if ( branchesToBeUploaded != null && !branchesToBeUploaded.isEmpty() ) {
            Branch branch = null;
            for ( BranchUploadVO branchUpload : branchesToBeUploaded ) {

                try {
                    //If branch waasn't added, modified, nor deleted, skip to the next step
                    if ( !( branchUpload.isBranchAdded() || branchUpload.isBranchModified() ) ) {
                        continue;
                    }

                    //Get region Id for branch. If null, set to default region of company
                    long regionId;
                    if ( upload.getRegionSourceMapping().get( branchUpload.getSourceRegionId() ) == null ) {
                        regionId = organizationManagementService.getDefaultRegionForCompany( company ).getRegionId();
                    } else {
                        regionId = upload.getRegionSourceMapping().get( branchUpload.getSourceRegionId() );
                    }
                    branchUpload.setRegionId( regionId );

                    if ( branchUpload.isBranchAdded() ) {
                        // Add branch
                        branch = createBranch( user, branchUpload, upload );
                        branchUpload.setBranchId( branch.getBranchId() );
                    } else if ( branchUpload.isBranchModified() ) {
                        // Modify branch
                        branch = modifyBranch( user, branchUpload );

                    }

                    // map the history records
                    //mapBranchModificationHistory( branchUpload, branch );

                    // map the id mapping
                    if ( branchUpload.getSourceRegionId() != null && !branchUpload.getSourceRegionId().isEmpty() ) {
                        upload.getRegionSourceMapping().put( branchUpload.getSourceRegionId(), branch.getRegion().getRegionId() );
                    }
                    if ( branchUpload.getSourceBranchId() != null && !branchUpload.getSourceBranchId().isEmpty() ) {
                        upload.getBranchSourceMapping().put( branchUpload.getSourceBranchId(), branch.getBranchId() );
                    }
                    
                    upload.setBranches( branchesToBeUploaded );

                } catch ( InvalidInputException | BranchAdditionException | SolrException | NoRecordsFetchedException
                    | UserAssignmentException e ) {
                    //TODO: Add error records
                    e.printStackTrace();
                }
            }
        }
    }


    @Transactional
    Branch modifyBranch( User adminUser, BranchUploadVO branch ) throws InvalidInputException, SolrException,
        NoRecordsFetchedException, UserAssignmentException
    {
        Branch newBranch = null;
        if ( adminUser == null ) {
            LOG.error( "admin user parameter is null!" );
            throw new InvalidInputException( "admin user parameter is null!" );
        }
        if ( branch == null ) {
            LOG.error( "branch parameter is null!" );
            throw new InvalidInputException( "branch parameter is null!" );
        }

        LOG.info( "ModifyBranch called for branch : " + branch.getBranchName() );
        LOG.debug( "Updating branch with BranchId : " + branch.getBranchId() );
        String country, countryCode;
        if ( branch.getBranchCountry() != null && branch.getBranchCountryCode() != null ) {
            country = branch.getBranchCountry();
            countryCode = branch.getBranchCountryCode();
        } else {
            OrganizationUnitSettings companySettings = organizationManagementService.getCompanySettings( adminUser );
            country = companySettings.getContact_details().getCountry();
            countryCode = companySettings.getContact_details().getCountryCode();
        }
        Map<String, Object> map = organizationManagementService.updateBranch( adminUser, branch.getBranchId(),
            branch.getRegionId(), branch.getBranchName(), branch.getBranchAddress1(), branch.getBranchAddress2(), country,
            countryCode, branch.getBranchState(), branch.getBranchCity(), branch.getBranchZipcode(), 0, null, false, false );
        newBranch = (Branch) map.get( CommonConstants.BRANCH_OBJECT );
        if ( newBranch == null ) {
            LOG.error( "No branch found with branchId :" + branch.getBranchId() );
            throw new InvalidInputException( "No branch found with branchId :" + branch.getBranchId() );
        }

        LOG.info( "ModifyBranch finished for branch : " + branch.getBranchName() );
        return newBranch;
    }


    /**
     * Creates a branch and assigns it under the appropriate region or company
     * 
     * @param adminUser
     * @param branch
     * @throws InvalidInputException
     * @throws BranchAdditionException
     * @throws SolrException
     * @throws NoRecordsFetchedException
     */
    @Transactional
    Branch createBranch( User adminUser, BranchUploadVO branch, HierarchyUpload upload ) throws InvalidInputException, BranchAdditionException,
        SolrException
    {
        Branch newBranch = null;
        if ( adminUser == null ) {
            LOG.error( "admin user parameter is null!" );
            throw new InvalidInputException( "admin user parameter is null!" );
        }
        if ( branch == null ) {
            LOG.error( "branch parameter is null!" );
            throw new InvalidInputException( "branch parameter is null!" );
        }

        LOG.info( "createBranch called to create branch :  " + branch.getBranchName() );
        String country, countryCode;
        if ( branch.getBranchCountry() != null && branch.getBranchCountryCode() != null ) {
            country = branch.getBranchCountry();
            countryCode = branch.getBranchCountryCode();
        } else {
            OrganizationUnitSettings companySettings = organizationManagementService.getCompanySettings( adminUser );
            country = companySettings.getContact_details().getCountry();
            countryCode = companySettings.getContact_details().getCountryCode();
        }
        //Default region
        if ( branch.getSourceRegionId() == null || branch.getSourceRegionId().isEmpty() ) {
            try {
                Region region = organizationManagementService.getDefaultRegionForCompany( adminUser.getCompany() );
                branch.setRegionId( region.getRegionId() );
            } catch ( NoRecordsFetchedException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        } else {
            //Resolve region Id
            if ( upload.getRegionSourceMapping().containsKey( branch.getSourceRegionId() ) ) {
                branch.setRegionId( upload.getRegionSourceMapping().get( branch.getSourceRegionId() ) );
            }
        }

        newBranch = organizationManagementService.addNewBranch( adminUser, branch.getRegionId(), CommonConstants.NO,
            branch.getBranchName(), branch.getBranchAddress1(), branch.getBranchAddress2(), country, countryCode,
            branch.getBranchState(), branch.getBranchCity(), branch.getBranchZipcode() );

        LOG.info( "createBranch finished for branch : " + branch.getBranchName() );
        return newBranch;
    }


    @Transactional
    void uploadRegions( HierarchyUpload upload, User user, Company company )
    {
        LOG.debug( "Uploading new regions." );
        List<RegionUploadVO> regionsToBeUploaded = upload.getRegions();
        if ( regionsToBeUploaded != null && !regionsToBeUploaded.isEmpty() ) {
            Region region = null;
            for ( RegionUploadVO regionUpload : regionsToBeUploaded ) {

                // create the region. add the field to history for all fields as its new region and map source id to the id mapping list
                try {
                    //If the region wasn't added, modified nor deleted, skip the next step
                    if ( !( regionUpload.isRegionAdded() || regionUpload.isRegionModified() ) ) {
                        continue;
                    }
                    if ( regionUpload.isRegionAdded() ) {
                        region = createRegion( user, regionUpload );
                        regionUpload.setRegionId( region.getRegionId() );

                    } else if ( regionUpload.isRegionModified() ) {
                        //process modified records
                        region = modifyRegion( user, regionUpload );
                    }
                    // map the history records
                    //mapRegionModificationHistory( regionUpload, region );
                    // map the id mapping
                    if ( regionUpload.getSourceRegionId() != null && !regionUpload.getSourceRegionId().isEmpty() ) {
                        upload.getRegionSourceMapping().put( regionUpload.getSourceRegionId(), region.getRegionId() );
                    }
                    
                    //Store the updated regionUploads in upload
                    upload.setRegions( regionsToBeUploaded );
                } catch ( InvalidInputException | SolrException | NoRecordsFetchedException | UserAssignmentException e ) {
                    // TODO: Add error records
                    e.printStackTrace();
                }
            }
        }
    }


    Region modifyRegion( User adminUser, RegionUploadVO region ) throws InvalidInputException, SolrException,
        NoRecordsFetchedException, UserAssignmentException
    {
        Region newRegion = null;
        if ( adminUser == null ) {
            LOG.error( "admin user parameter is null!" );
            throw new InvalidInputException( "admin user parameter is null!" );
        }
        if ( region == null ) {
            LOG.error( "region parameter is null!" );
            throw new InvalidInputException( "region parameter is null!" );
        }

        LOG.info( "ModifyRegion called for region : " + region.getRegionName() );
        LOG.debug( "Updating region with RegionId : " + region.getRegionId() );

        //Update region
        Map<String, Object> map = organizationManagementService.updateRegion( adminUser, region.getRegionId(),
            region.getRegionName(), region.getRegionAddress1(), region.getRegionAddress2(), region.getRegionCountry(),
            region.getRegionCountryCode(), region.getRegionState(), region.getRegionCity(), region.getRegionZipcode(), 0, null,
            false, false );
        newRegion = (Region) map.get( CommonConstants.REGION_OBJECT );

        if ( newRegion == null ) {
            LOG.error( "No region found with regionId :" + region.getRegionId() );
            throw new InvalidInputException( "No region found with regionId :" + region.getRegionId() );
        }

        return newRegion;
    }


    RegionUploadVO mapRegionModificationHistory( RegionUploadVO regionUpload, Region region )
    {
        LOG.debug( "mapping region history" );
        Timestamp currentTimestamp = new Timestamp( System.currentTimeMillis() );
        // map region id history
        if ( regionUpload.isRegionAdded() || regionUpload.isRegionIdModified() ) {
            List<LongUploadHistory> regionIdHistoryList = regionUpload.getRegionIdHistory();
            if ( regionIdHistoryList == null ) {
                regionIdHistoryList = new ArrayList<LongUploadHistory>();
            }
            LongUploadHistory regionIdHistory = new LongUploadHistory();
            regionIdHistory.setValue( region.getRegionId() );
            regionIdHistory.setTime( currentTimestamp );
            regionIdHistoryList.add( regionIdHistory );
            regionUpload.setRegionIdHistory( regionIdHistoryList );
            regionUpload.setRegionIdModified( false );
        }

        // map source region id history
        if ( regionUpload.isRegionAdded() || regionUpload.isSourceRegionIdModified() ) {
            List<StringUploadHistory> sourceIdHistoryList = regionUpload.getSourceRegionIdHistory();
            if ( sourceIdHistoryList == null ) {
                sourceIdHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory sourceIdHistory = new StringUploadHistory();
            sourceIdHistory.setValue( regionUpload.getSourceRegionId() );
            sourceIdHistory.setTime( currentTimestamp );
            sourceIdHistoryList.add( sourceIdHistory );
            regionUpload.setSourceRegionIdHistory( sourceIdHistoryList );
            regionUpload.setSourceRegionIdModified( false );
        }

        // map region name history
        if ( regionUpload.isRegionAdded() || regionUpload.isRegionNameModified() ) {
            List<StringUploadHistory> regionNameHistoryList = regionUpload.getRegionNameHistory();
            if ( regionNameHistoryList == null ) {
                regionNameHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory regionNameHistory = new StringUploadHistory();
            regionNameHistory.setValue( regionUpload.getRegionName() );
            regionNameHistory.setTime( currentTimestamp );
            regionNameHistoryList.add( regionNameHistory );
            regionUpload.setRegionNameHistory( regionNameHistoryList );
            regionUpload.setRegionNameModified( false );
        }

        // map region address 1 history
        if ( regionUpload.isRegionAdded() || regionUpload.isRegionAddress1Modified() ) {
            List<StringUploadHistory> regionAddress1HistoryList = regionUpload.getRegionAddress1History();
            if ( regionAddress1HistoryList == null ) {
                regionAddress1HistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory regionAddress1History = new StringUploadHistory();
            regionAddress1History.setValue( regionUpload.getRegionAddress1() );
            regionAddress1History.setTime( currentTimestamp );
            regionAddress1HistoryList.add( regionAddress1History );
            regionUpload.setRegionAddress1History( regionAddress1HistoryList );
            regionUpload.setRegionAddress1Modified( false );
        }

        // map region address 2 history
        if ( regionUpload.isRegionAdded() || regionUpload.isRegionAddress2Modified() ) {
            List<StringUploadHistory> regionAddress2HistoryList = regionUpload.getRegionAddress2History();
            if ( regionAddress2HistoryList == null ) {
                regionAddress2HistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory regionAddress2History = new StringUploadHistory();
            regionAddress2History.setValue( regionUpload.getRegionAddress2() );
            regionAddress2History.setTime( currentTimestamp );
            regionAddress2HistoryList.add( regionAddress2History );
            regionUpload.setRegionAddress2History( regionAddress2HistoryList );
            regionUpload.setRegionAddress2Modified( false );
        }

        // map city history
        if ( regionUpload.isRegionAdded() || regionUpload.isRegionCityModified() ) {
            List<StringUploadHistory> regionCityHistoryList = regionUpload.getRegionCityHistory();
            if ( regionCityHistoryList == null ) {
                regionCityHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory regionCityHistory = new StringUploadHistory();
            regionCityHistory.setValue( regionUpload.getRegionCity() );
            regionCityHistory.setTime( currentTimestamp );
            regionCityHistoryList.add( regionCityHistory );
            regionUpload.setRegionCityHistory( regionCityHistoryList );
            regionUpload.setRegionCityModified( false );
        }

        // map state history
        if ( regionUpload.isRegionAdded() || regionUpload.isRegionStateModified() ) {
            List<StringUploadHistory> regionStateHistoryList = regionUpload.getRegionStateHistory();
            if ( regionStateHistoryList == null ) {
                regionStateHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory regionStateHistory = new StringUploadHistory();
            regionStateHistory.setValue( regionUpload.getRegionState() );
            regionStateHistory.setTime( currentTimestamp );
            regionStateHistoryList.add( regionStateHistory );
            regionUpload.setRegionStateHistory( regionStateHistoryList );
            regionUpload.setRegionStateModified( false );
        }

        // map zip history
        if ( regionUpload.isRegionAdded() || regionUpload.isRegionZipcodeModified() ) {
            List<StringUploadHistory> regionZipCodeHistoryList = regionUpload.getRegionZipcodeHistory();
            if ( regionZipCodeHistoryList == null ) {
                regionZipCodeHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory regionZipCodeHistory = new StringUploadHistory();
            regionZipCodeHistory.setValue( regionUpload.getRegionZipcode() );
            regionZipCodeHistory.setTime( currentTimestamp );
            regionZipCodeHistoryList.add( regionZipCodeHistory );
            regionUpload.setRegionZipcodeHistory( regionZipCodeHistoryList );
            regionUpload.setRegionZipcodeModified( false );
        }

        //map country history
        if ( regionUpload.isRegionAdded() || regionUpload.isRegionCountryModified() ) {
            List<StringUploadHistory> regionCountryHistoryList = regionUpload.getRegionCountryHistory();
            if ( regionCountryHistoryList == null ) {
                regionCountryHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory regionCountryHistory = new StringUploadHistory();
            regionCountryHistory.setTime( currentTimestamp );
            regionCountryHistory.setValue( regionUpload.getRegionCountry() );
            regionCountryHistoryList.add( regionCountryHistory );
            regionUpload.setRegionCountryHistory( regionCountryHistoryList );
            regionUpload.setRegionCountryModified( false );
        }

        regionUpload.setRegionAdded( false );
        regionUpload.setRegionModified( false );
        return regionUpload;
    }


    /**
     * Creates a region
     * 
     * @param adminUser
     * @param region
     * @throws InvalidInputException
     * @throws RegionAdditionException
     * @throws SolrException
     */
    @Transactional
    Region createRegion( User adminUser, RegionUploadVO region ) throws InvalidInputException, SolrException
    {
        Region newRegion = null;
        if ( adminUser == null ) {
            LOG.error( "admin user parameter is null!" );
            throw new InvalidInputException( "admin user parameter is null!" );
        }
        if ( region == null ) {
            LOG.error( "region parameter is null!" );
            throw new InvalidInputException( "region parameter is null!" );
        }
        LOG.info( "createRegion called to add region : " + region.getRegionName() );

        LOG.debug( "Adding region : " + region.getRegionName() );
        newRegion = organizationManagementService.addNewRegion( adminUser, region.getRegionName(), CommonConstants.NO,
            region.getRegionAddress1(), region.getRegionAddress2(), region.getRegionCountry(), region.getRegionCountryCode(),
            region.getRegionState(), region.getRegionCity(), region.getRegionZipcode() );
        organizationManagementService.addNewBranch( adminUser, newRegion.getRegionId(), CommonConstants.YES,
            CommonConstants.DEFAULT_BRANCH_NAME, null, null, null, null, null, null, null );
        return newRegion;
    }


    UserUploadVO mapUserModificationHistory( UserUploadVO userUpload, User user )
    {
        LOG.info( "Mapping user history" );
        Timestamp currentTimestamp = new Timestamp( System.currentTimeMillis() );
        //map user first name history
        if ( userUpload.isUserAdded() || userUpload.isFirstNameModified() ) {
            List<StringUploadHistory> firstNameHistoryList = userUpload.getFirstNameHistory();
            if ( firstNameHistoryList == null ) {
                firstNameHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory firstNameHistory = new StringUploadHistory();
            firstNameHistory.setTime( currentTimestamp );
            firstNameHistory.setValue( userUpload.getFirstName() );
            firstNameHistoryList.add( firstNameHistory );
            userUpload.setFirstNameHistory( firstNameHistoryList );
            userUpload.setFirstNameModified( false );
        }

        //map user last name history
        if ( userUpload.isUserAdded() || userUpload.isLastNameModified() ) {
            List<StringUploadHistory> lastNameHistoryList = userUpload.getLastNameHistory();
            if ( lastNameHistoryList == null ) {
                lastNameHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory lastNameHistory = new StringUploadHistory();
            lastNameHistory.setTime( currentTimestamp );
            lastNameHistory.setValue( userUpload.getLastName() );
            lastNameHistoryList.add( lastNameHistory );
            userUpload.setLastNameHistory( lastNameHistoryList );
            userUpload.setLastNameModified( false );
        }

        //map user title history
        if ( userUpload.isUserAdded() || userUpload.isTitleModified() ) {
            List<StringUploadHistory> titleHistoryList = userUpload.getTitleHistory();
            if ( titleHistoryList == null ) {
                titleHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory titleHistory = new StringUploadHistory();
            titleHistory.setTime( currentTimestamp );
            titleHistory.setValue( userUpload.getTitle() );
            titleHistoryList.add( titleHistory );
            userUpload.setTitleHistory( titleHistoryList );
            userUpload.setTitleModified( false );
        }

        //map branch id history
        if ( userUpload.isUserAdded() || userUpload.isBranchIdModified() ) {
            List<LongUploadHistory> branchIdHistoryList = userUpload.getBranchIdHistory();
            if ( branchIdHistoryList == null ) {
                branchIdHistoryList = new ArrayList<LongUploadHistory>();
            }
            LongUploadHistory branchIdHistory = new LongUploadHistory();
            branchIdHistory.setTime( currentTimestamp );
            branchIdHistory.setValue( userUpload.getBranchId() );
            branchIdHistoryList.add( branchIdHistory );
            userUpload.setBranchIdHistory( branchIdHistoryList );
            userUpload.setBranchIdModified( false );
        }

        //map source branch id history
        if ( userUpload.isUserAdded() || userUpload.isSourceBranchIdModified() ) {
            List<StringUploadHistory> sourceBranchIdHistoryList = userUpload.getSourceBranchIdHistory();
            if ( sourceBranchIdHistoryList == null ) {
                sourceBranchIdHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory sourceBranchIdHistory = new StringUploadHistory();
            sourceBranchIdHistory.setTime( currentTimestamp );
            sourceBranchIdHistory.setValue( userUpload.getSourceBranchId() );
            sourceBranchIdHistoryList.add( sourceBranchIdHistory );
            userUpload.setSourceBranchIdHistory( sourceBranchIdHistoryList );
            userUpload.setSourceBranchIdModified( false );
        }

        //map region id history
        if ( userUpload.isUserAdded() || userUpload.isRegionIdModified() ) {
            List<LongUploadHistory> regionIdHistoryList = userUpload.getRegionIdHistory();
            if ( regionIdHistoryList == null ) {
                regionIdHistoryList = new ArrayList<LongUploadHistory>();
            }
            LongUploadHistory regionIdHistory = new LongUploadHistory();
            regionIdHistory.setTime( currentTimestamp );
            regionIdHistory.setValue( userUpload.getRegionId() );
            regionIdHistoryList.add( regionIdHistory );
            userUpload.setRegionIdHistory( regionIdHistoryList );
            userUpload.setRegionIdModified( false );
        }

        //map source region id history
        if ( userUpload.isUserAdded() || userUpload.isSourceRegionIdModified() ) {
            List<StringUploadHistory> sourceRegionIdHistoryList = userUpload.getSourceRegionIdHistory();
            if ( sourceRegionIdHistoryList == null ) {
                sourceRegionIdHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory sourceRegionIdHistory = new StringUploadHistory();
            sourceRegionIdHistory.setTime( currentTimestamp );
            sourceRegionIdHistory.setValue( userUpload.getSourceRegionId() );
            sourceRegionIdHistoryList.add( sourceRegionIdHistory );
            userUpload.setSourceRegionIdHistory( sourceRegionIdHistoryList );
            userUpload.setSourceRegionIdModified( false );
        }

        //map is agent history
        if ( userUpload.isUserAdded() || userUpload.isAgentModified() ) {
            List<BooleanUploadHistory> isAgentHistoryList = userUpload.getIsAgentHistory();
            if ( isAgentHistoryList == null ) {
                isAgentHistoryList = new ArrayList<BooleanUploadHistory>();
            }
            BooleanUploadHistory isAgentHistory = new BooleanUploadHistory();
            isAgentHistory.setTime( currentTimestamp );
            isAgentHistory.setValue( userUpload.isAgent() );
            isAgentHistoryList.add( isAgentHistory );
            userUpload.setIsAgentHistory( isAgentHistoryList );
            userUpload.setAgentModified( false );
        }

        //map email ID history
        if ( userUpload.isUserAdded() || userUpload.isEmailIdModified() ) {
            List<StringUploadHistory> emailIdHistoryList = userUpload.getEmailIdHistory();
            if ( emailIdHistoryList == null ) {
                emailIdHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory emailIdHistory = new StringUploadHistory();
            emailIdHistory.setTime( currentTimestamp );
            emailIdHistory.setValue( userUpload.getEmailId() );
            emailIdHistoryList.add( emailIdHistory );
            userUpload.setEmailIdHistory( emailIdHistoryList );
            userUpload.setEmailIdModified( false );
        }

        //map belongs to company history
        if ( userUpload.isUserAdded() || userUpload.isBelongsToCompanyModified() ) {
            List<BooleanUploadHistory> belongsToCompanyHistoryList = userUpload.getBelongsToCompanyHistory();
            if ( belongsToCompanyHistoryList == null ) {
                belongsToCompanyHistoryList = new ArrayList<BooleanUploadHistory>();
            }
            BooleanUploadHistory belongsToCompanyHistory = new BooleanUploadHistory();
            belongsToCompanyHistory.setTime( currentTimestamp );
            belongsToCompanyHistory.setValue( userUpload.isBelongsToCompany() );
            belongsToCompanyHistoryList.add( belongsToCompanyHistory );
            userUpload.setBelongsToCompanyHistory( belongsToCompanyHistoryList );
            userUpload.setBelongsToCompanyModified( false );
        }

        //map assign to company history
        if ( userUpload.isUserAdded() || userUpload.isAssignToCompany() ) {
            List<BooleanUploadHistory> assignedToCompanyHistoryList = userUpload.getAssignToCompanyHistory();
            if ( assignedToCompanyHistoryList == null ) {
                assignedToCompanyHistoryList = new ArrayList<BooleanUploadHistory>();
            }
            BooleanUploadHistory assignedToCompanyHistory = new BooleanUploadHistory();
            assignedToCompanyHistory.setTime( currentTimestamp );
            assignedToCompanyHistory.setValue( userUpload.isAssignToCompany() );
            assignedToCompanyHistoryList.add( assignedToCompanyHistory );
            userUpload.setAssignToCompanyHistory( assignedToCompanyHistoryList );
            userUpload.setAssignToCompanyModified( false );
        }

        //map assigned branch name history
        if ( userUpload.isUserAdded() || userUpload.isAssignedBranchNameModified() ) {
            List<StringUploadHistory> branchNameHistoryList = userUpload.getAssignedBranchNameHistory();
            if ( branchNameHistoryList == null ) {
                branchNameHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory branchNameHistory = new StringUploadHistory();
            branchNameHistory.setTime( currentTimestamp );
            branchNameHistory.setValue( userUpload.getAssignedBranchName() );
            branchNameHistoryList.add( branchNameHistory );
            userUpload.setAssignedBranchNameHistory( branchNameHistoryList );
            userUpload.setAssignedBranchNameModified( false );
        }

        //map assigned branches history
        if ( userUpload.isUserAdded() || userUpload.isAssignedBranchesModified() ) {
            List<StringListUploadHistory> assignedBranchesHistoryList = userUpload.getAssignedBranchesHistory();
            if ( assignedBranchesHistoryList == null ) {
                assignedBranchesHistoryList = new ArrayList<StringListUploadHistory>();
            }
            StringListUploadHistory assignedBranchesHistory = new StringListUploadHistory();
            assignedBranchesHistory.setTime( currentTimestamp );
            assignedBranchesHistory.setValue( userUpload.getAssignedBranches() );
            assignedBranchesHistoryList.add( assignedBranchesHistory );
            userUpload.setAssignedBranchesHistory( assignedBranchesHistoryList );
            userUpload.setAssignedBranchesModified( false );
        }

        //map assigned region name history
        if ( userUpload.isUserAdded() || userUpload.isAssignedRegionNameModified() ) {
            List<StringUploadHistory> regionNameHistoryList = userUpload.getAssignedRegionNameHistory();
            if ( regionNameHistoryList == null ) {
                regionNameHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory regionNameHistory = new StringUploadHistory();
            regionNameHistory.setTime( currentTimestamp );
            regionNameHistory.setValue( userUpload.getAssignedRegionName() );
            regionNameHistoryList.add( regionNameHistory );
            userUpload.setAssignedRegionNameHistory( regionNameHistoryList );
            userUpload.setAssignedRegionNameModified( false );
        }

        //map assigned regions history
        if ( userUpload.isUserAdded() || userUpload.isAssignedRegionsModified() ) {
            List<StringListUploadHistory> assignedRegionsHistoryList = userUpload.getAssignedRegionsHistory();
            if ( assignedRegionsHistoryList == null ) {
                assignedRegionsHistoryList = new ArrayList<StringListUploadHistory>();
            }
            StringListUploadHistory assignedRegionsHistory = new StringListUploadHistory();
            assignedRegionsHistory.setTime( currentTimestamp );
            assignedRegionsHistory.setValue( userUpload.getAssignedRegions() );
            assignedRegionsHistoryList.add( assignedRegionsHistory );
            userUpload.setAssignedRegionsHistory( assignedRegionsHistoryList );
            userUpload.setAssignedRegionsModified( false );
        }

        //map is branch admin history
        if ( userUpload.isUserAdded() || userUpload.isBranchAdminModified() ) {
            List<BooleanUploadHistory> isBranchAdminHistoryList = userUpload.getIsBranchAdminHistory();
            if ( isBranchAdminHistoryList == null ) {
                isBranchAdminHistoryList = new ArrayList<BooleanUploadHistory>();
            }
            BooleanUploadHistory isBranchAdminHistory = new BooleanUploadHistory();
            isBranchAdminHistory.setTime( currentTimestamp );
            isBranchAdminHistory.setValue( userUpload.isBranchAdmin() );
            isBranchAdminHistoryList.add( isBranchAdminHistory );
            userUpload.setIsBranchAdminHistory( isBranchAdminHistoryList );
            userUpload.setBranchAdminModified( false );
        }

        //map assigned branches admin history
        if ( userUpload.isUserAdded() || userUpload.isAssignedBrachesAdminModified() ) {
            List<StringListUploadHistory> assignedBranchesAdminHistoryList = userUpload.getAssignedBrachesAdminHistory();
            if ( assignedBranchesAdminHistoryList == null ) {
                assignedBranchesAdminHistoryList = new ArrayList<StringListUploadHistory>();
            }
            StringListUploadHistory assignedBranchesAdminHistory = new StringListUploadHistory();
            assignedBranchesAdminHistory.setTime( currentTimestamp );
            assignedBranchesAdminHistory.setValue( userUpload.getAssignedBranchesAdmin() );
            assignedBranchesAdminHistoryList.add( assignedBranchesAdminHistory );
            userUpload.setAssignedBrachesAdminHistory( assignedBranchesAdminHistoryList );
            userUpload.setAssignedBrachesAdminModified( false );
        }

        //map is region admin history
        if ( userUpload.isUserAdded() || userUpload.isRegionAdminModified() ) {
            List<BooleanUploadHistory> isRegionAdminHistoryList = userUpload.getIsRegionAdminHistory();
            if ( isRegionAdminHistoryList == null ) {
                isRegionAdminHistoryList = new ArrayList<BooleanUploadHistory>();
            }
            BooleanUploadHistory isRegionAdminHistory = new BooleanUploadHistory();
            isRegionAdminHistory.setTime( currentTimestamp );
            isRegionAdminHistory.setValue( userUpload.isRegionAdmin() );
            isRegionAdminHistoryList.add( isRegionAdminHistory );
            userUpload.setIsRegionAdminHistory( isRegionAdminHistoryList );
            userUpload.setRegionAdminModified( false );
        }

        //map assigned regions admin history
        if ( userUpload.isUserAdded() || userUpload.isAssignedRegionsAdminModified() ) {
            List<StringListUploadHistory> assignedRegionsAdminHistoryList = userUpload.getAssignedRegionsAdminHistory();
            if ( assignedRegionsAdminHistoryList == null ) {
                assignedRegionsAdminHistoryList = new ArrayList<StringListUploadHistory>();
            }
            StringListUploadHistory assignedRegionsAdminHistory = new StringListUploadHistory();
            assignedRegionsAdminHistory.setTime( currentTimestamp );
            assignedRegionsAdminHistory.setValue( userUpload.getAssignedRegionsAdmin() );
            assignedRegionsAdminHistoryList.add( assignedRegionsAdminHistory );
            userUpload.setAssignedRegionsAdminHistory( assignedRegionsAdminHistoryList );
            userUpload.setAssignedRegionsAdminModified( false );
        }

        //map phone number history
        if ( userUpload.isUserAdded() || userUpload.isPhoneNumberModified() ) {
            List<StringUploadHistory> phoneNumberHistoryList = userUpload.getPhoneNumberHistory();
            if ( phoneNumberHistoryList == null ) {
                phoneNumberHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory phoneNumberHistory = new StringUploadHistory();
            phoneNumberHistory.setTime( currentTimestamp );
            phoneNumberHistory.setValue( userUpload.getPhoneNumber() );
            phoneNumberHistoryList.add( phoneNumberHistory );
            userUpload.setPhoneNumberHistory( phoneNumberHistoryList );
            userUpload.setPhoneNumberModified( false );
        }

        //map website url history
        if ( userUpload.isUserAdded() || userUpload.isWebsiteUrlModified() ) {
            List<StringUploadHistory> websiteUrlHistoryList = userUpload.getWebsiteUrlHistory();
            if ( websiteUrlHistoryList == null ) {
                websiteUrlHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory websiteUrlHistory = new StringUploadHistory();
            websiteUrlHistory.setTime( currentTimestamp );
            websiteUrlHistory.setValue( userUpload.getWebsiteUrl() );
            websiteUrlHistoryList.add( websiteUrlHistory );
            userUpload.setWebsiteUrlHistory( websiteUrlHistoryList );
            userUpload.setWebsiteUrlModified( false );
        }

        //map license history
        if ( userUpload.isUserAdded() || userUpload.isLicenseModified() ) {
            List<StringUploadHistory> licenseHistoryList = userUpload.getLicenseHistory();
            if ( licenseHistoryList == null ) {
                licenseHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory licenseHistory = new StringUploadHistory();
            licenseHistory.setTime( currentTimestamp );
            licenseHistory.setValue( userUpload.getLicense() );
            licenseHistoryList.add( licenseHistory );
            userUpload.setLicenseHistory( licenseHistoryList );
            userUpload.setLicenseModified( false );
        }

        //map legal disclaimer history
        if ( userUpload.isUserAdded() || userUpload.isLegalDisclaimerModified() ) {
            List<StringUploadHistory> legalDisclaimerHistoryList = userUpload.getLegalDisclaimerHistory();
            if ( legalDisclaimerHistoryList == null ) {
                legalDisclaimerHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory legalDisclaimerHistory = new StringUploadHistory();
            legalDisclaimerHistory.setTime( currentTimestamp );
            legalDisclaimerHistory.setValue( userUpload.getLegalDisclaimer() );
            legalDisclaimerHistoryList.add( legalDisclaimerHistory );
            userUpload.setLegalDisclaimerHistory( legalDisclaimerHistoryList );
            userUpload.setLegalDisclaimerModified( false );
        }

        //map about me history
        if ( userUpload.isUserAdded() || userUpload.isAboutMeDescriptionModified() ) {
            List<StringUploadHistory> aboutMeHistoryList = userUpload.getAboutMeDescriptionHistory();
            if ( aboutMeHistoryList == null ) {
                aboutMeHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory aboutMeHistory = new StringUploadHistory();
            aboutMeHistory.setTime( currentTimestamp );
            aboutMeHistory.setValue( userUpload.getAboutMeDescription() );
            aboutMeHistoryList.add( aboutMeHistory );
            userUpload.setAboutMeDescriptionHistory( aboutMeHistoryList );
            userUpload.setAboutMeDescriptionModified( false );
        }

        //map user profile photo
        if ( userUpload.isUserAdded() || userUpload.isUserPhotoUrlModified() ) {
            List<StringUploadHistory> photoHistoryList = userUpload.getUserPhotoUrlHistory();
            if ( photoHistoryList == null ) {
                photoHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory photoHistory = new StringUploadHistory();
            photoHistory.setTime( currentTimestamp );
            photoHistory.setValue( userUpload.getUserPhotoUrl() );
            photoHistoryList.add( photoHistory );
            userUpload.setLegalDisclaimerHistory( photoHistoryList );
            userUpload.setUserPhotoUrlModified( false );
        }

        return userUpload;

    }


    BranchUploadVO mapBranchModificationHistory( BranchUploadVO branchUpload, Branch branch )
    {
        LOG.info( "Mapping branch history" );
        Timestamp currentTimestamp = new Timestamp( System.currentTimeMillis() );
        //map branch id history
        if ( branchUpload.isBranchIdModified() || branchUpload.isBranchAdded() ) {
            List<LongUploadHistory> branchIdHistoryList = branchUpload.getBranchIdHistory();
            if ( branchIdHistoryList == null ) {
                branchIdHistoryList = new ArrayList<LongUploadHistory>();
            }
            LongUploadHistory branchIdHistory = new LongUploadHistory();
            branchIdHistory.setTime( currentTimestamp );
            branchIdHistory.setValue( branch.getBranchId() );
            branchIdHistoryList.add( branchIdHistory );
            branchUpload.setBranchIdHistory( branchIdHistoryList );
            branchUpload.setBranchIdModified( false );
        }

        //map source branch id history
        if ( branchUpload.isSourceBranchIdModified() || branchUpload.isBranchAdded() ) {
            List<StringUploadHistory> branchSourceIdHistoryList = branchUpload.getSourceBranchIdHistory();
            if ( branchSourceIdHistoryList == null ) {
                branchSourceIdHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory branchSourceIdHistory = new StringUploadHistory();
            branchSourceIdHistory.setTime( currentTimestamp );
            branchSourceIdHistory.setValue( branchUpload.getSourceBranchId() );
            branchSourceIdHistoryList.add( branchSourceIdHistory );
            branchUpload.setSourceBranchIdHistory( branchSourceIdHistoryList );
            branchUpload.setSourceBranchIdModified( false );
        }

        //map region id history
        if ( branchUpload.isRegionIdModified() || branchUpload.isBranchAdded() ) {
            List<LongUploadHistory> regionIdHistoryList = branchUpload.getRegionIdHistory();
            if ( regionIdHistoryList == null ) {
                regionIdHistoryList = new ArrayList<LongUploadHistory>();
            }
            LongUploadHistory regionIdHistory = new LongUploadHistory();
            regionIdHistory.setTime( currentTimestamp );
            regionIdHistory.setValue( branchUpload.getRegionId() );
            regionIdHistoryList.add( regionIdHistory );
            branchUpload.setRegionIdHistory( regionIdHistoryList );
            branchUpload.setRegionIdModified( false );
        }

        //map region source id history
        if ( branchUpload.isSourceRegionIdModified() || branchUpload.isBranchAdded() ) {
            List<StringUploadHistory> regionSourceIdHistoryList = branchUpload.getSourceRegionIdHistory();
            if ( regionSourceIdHistoryList == null ) {
                regionSourceIdHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory regionSourceIdHistory = new StringUploadHistory();
            regionSourceIdHistory.setTime( currentTimestamp );
            regionSourceIdHistory.setValue( branchUpload.getSourceRegionId() );
            regionSourceIdHistoryList.add( regionSourceIdHistory );
            branchUpload.setSourceRegionIdHistory( regionSourceIdHistoryList );
            branchUpload.setSourceRegionIdModified( false );
        }

        //map branch name history
        if ( branchUpload.isBranchNameModified() || branchUpload.isBranchAdded() ) {
            List<StringUploadHistory> branchNameHistoryList = branchUpload.getBranchNameHistory();
            if ( branchNameHistoryList == null ) {
                branchNameHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory branchNameHistory = new StringUploadHistory();
            branchNameHistory.setTime( currentTimestamp );
            branchNameHistory.setValue( branchUpload.getBranchName() );
            branchNameHistoryList.add( branchNameHistory );
            branchUpload.setBranchNameHistory( branchNameHistoryList );
            branchUpload.setBranchNameModified( false );
        }

        //map branch address 1 history
        if ( branchUpload.isBranchAdded() || branchUpload.isBranchAddress1Modified() ) {
            List<StringUploadHistory> address1HistoryList = branchUpload.getBranchAddress1History();
            if ( address1HistoryList == null ) {
                address1HistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory address1History = new StringUploadHistory();
            address1History.setTime( currentTimestamp );
            address1History.setValue( branchUpload.getBranchAddress1() );
            address1HistoryList.add( address1History );
            branchUpload.setBranchAddress1History( address1HistoryList );
            branchUpload.setBranchAddress1Modified( false );
        }

        //map branch address 2 history
        if ( branchUpload.isBranchAdded() || branchUpload.isBranchAddress2Modified() ) {
            List<StringUploadHistory> address2HistoryList = branchUpload.getBranchAddress2History();
            if ( address2HistoryList == null ) {
                address2HistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory address2History = new StringUploadHistory();
            address2History.setTime( currentTimestamp );
            address2History.setValue( branchUpload.getBranchAddress2() );
            address2HistoryList.add( address2History );
            branchUpload.setBranchAddress2History( address2HistoryList );
            branchUpload.setBranchAddress2Modified( false );
        }

        //map branch country history
        if ( branchUpload.isBranchAdded() || branchUpload.isBranchCountryModified() ) {
            List<StringUploadHistory> countryHistoryList = branchUpload.getBranchCountryHistory();
            if ( countryHistoryList == null ) {
                countryHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory countryHistory = new StringUploadHistory();
            countryHistory.setTime( currentTimestamp );
            countryHistory.setValue( branchUpload.getBranchCountry() );
            countryHistoryList.add( countryHistory );
            branchUpload.setBranchCountryHistory( countryHistoryList );
            branchUpload.setBranchCountryModified( false );
        }

        //map branch country code history
        if ( branchUpload.isBranchAdded() || branchUpload.isBranchCountryCodeModified() ) {
            List<StringUploadHistory> countryCodeHistoryList = branchUpload.getBranchCountryCodeHistory();
            if ( countryCodeHistoryList == null ) {
                countryCodeHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory countryCodeHistory = new StringUploadHistory();
            countryCodeHistory.setTime( currentTimestamp );
            countryCodeHistory.setValue( branchUpload.getBranchCountryCode() );
            countryCodeHistoryList.add( countryCodeHistory );
            branchUpload.setBranchCountryCodeHistory( countryCodeHistoryList );
            branchUpload.setBranchCountryCodeModified( false );
        }

        //map branch state history
        if ( branchUpload.isBranchAdded() || branchUpload.isBranchStateModified() ) {
            List<StringUploadHistory> stateHistoryList = branchUpload.getBranchStateHistory();
            if ( stateHistoryList == null ) {
                stateHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory stateHistory = new StringUploadHistory();
            stateHistory.setTime( currentTimestamp );
            stateHistory.setValue( branchUpload.getBranchState() );
            stateHistoryList.add( stateHistory );
            branchUpload.setBranchStateHistory( stateHistoryList );
            branchUpload.setBranchStateModified( false );
        }

        //map branch city history
        if ( branchUpload.isBranchAdded() || branchUpload.isBranchCityModified() ) {
            List<StringUploadHistory> cityHistoryList = branchUpload.getBranchCityHistory();
            if ( cityHistoryList == null ) {
                cityHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory cityHistory = new StringUploadHistory();
            cityHistory.setTime( currentTimestamp );
            cityHistory.setValue( branchUpload.getBranchCity() );
            cityHistoryList.add( cityHistory );
            branchUpload.setBranchCityHistory( cityHistoryList );
            branchUpload.setBranchCityModified( false );
        }

        //map branch zipcode history
        if ( branchUpload.isBranchAdded() || branchUpload.isBranchZipcodeModified() ) {
            List<StringUploadHistory> zipcodeHistoryList = branchUpload.getBranchZipcodeHistory();
            if ( zipcodeHistoryList == null ) {
                zipcodeHistoryList = new ArrayList<StringUploadHistory>();
            }
            StringUploadHistory zipcodeHistory = new StringUploadHistory();
            zipcodeHistory.setTime( currentTimestamp );
            zipcodeHistory.setValue( branchUpload.getBranchZipcode() );
            zipcodeHistoryList.add( zipcodeHistory );
            branchUpload.setBranchZipcodeHistory( zipcodeHistoryList );
            branchUpload.setBranchZipcodeModified( false );
        }
        branchUpload.setBranchAdded( false );
        branchUpload.setBranchModified( false );
        return branchUpload;
    }


    Company getCompany( User user ) throws InvalidInputException
    {
        Company company = user.getCompany();
        if ( company == null ) {
            LOG.error( "Company property not found in admin user object!" );
            throw new InvalidInputException( "Company property not found in admin user object!" );

        }
        return company;
    }


    LicenseDetail getLicenseDetail( Company company ) throws InvalidInputException
    {
        LicenseDetail companyLicenseDetail = null;
        if ( company.getLicenseDetails() != null && !company.getLicenseDetails().isEmpty() ) {
            companyLicenseDetail = company.getLicenseDetails().get( CommonConstants.INITIAL_INDEX );
        } else {
            LOG.error( "License Detail property not found in admin user's company object!" );
            throw new InvalidInputException( "License Detail property not found in admin user's company object!" );
        }
        return companyLicenseDetail;
    }

    /**
     * Method to assign/unassign branches to user
     * @param user
     * @param adminUser
     * @param assigneeUser
     * @param currentUserMap
     * @param upload
     * @return
     * @throws UserAssignmentException
     * @throws InvalidInputException
     * @throws NoRecordsFetchedException
     * @throws SolrException
     * @throws UserAdditionException
     */
    User assignBranchesToUser( UserUploadVO user, User adminUser, User assigneeUser, Map<String, UserUploadVO> currentUserMap,
        HierarchyUpload upload, boolean isAdmin ) throws UserAssignmentException, InvalidInputException,
        NoRecordsFetchedException, SolrException, UserAdditionException
    {
        LOG.info( "Method assignBranchesToUser() for user : " + user.getEmailId() + " isAdmin : " + isAdmin
            + " started." );
        if ( user.isSourceBranchIdModified() || user.isAssignedBranchesModified() ) {
            //Compare the current user assignments and the new assignments to find the changes
            /*
             * Cases to handle:
             * 1. All new assignments
             * 2. Delete all assignments
             * 3. add some, delete some assignments
             */
            List<String> addedAssignments = new ArrayList<String>();
            List<String> deletedAssignments = new ArrayList<String>();

            if ( currentUserMap.containsKey( user.getSourceUserId() ) ) {
                //Existing user
                List<String> oldAssignments;
                List<String> newAssignments;
                if ( isAdmin ) {
                    oldAssignments = currentUserMap.get( user.getSourceBranchId() ).getAssignedBranchesAdmin();
                    newAssignments = user.getAssignedBranchesAdmin();
                } else {
                    oldAssignments = currentUserMap.get( user.getSourceUserId() ).getAssignedBranches();
                    newAssignments = user.getAssignedBranches();
                }

                if ( oldAssignments == null || oldAssignments.isEmpty() ) {
                    //All assignments are new
                    addedAssignments.addAll( newAssignments );
                } else if ( newAssignments == null || newAssignments.isEmpty() ) {
                    //Delete all assignments
                    deletedAssignments.addAll( oldAssignments );
                } else {
                    //find added and deleted assignments
                    List<String> tempOldAssignments = new ArrayList<String>();
                    tempOldAssignments.addAll( oldAssignments );
                    List<String> tempNewAssignments = new ArrayList<String>();
                    tempNewAssignments.addAll( newAssignments );

                    tempOldAssignments.removeAll( newAssignments );
                    deletedAssignments.addAll( tempOldAssignments );

                    tempNewAssignments.removeAll( oldAssignments );
                    addedAssignments.addAll( tempNewAssignments );
                }
            } else {
                //All assignments are new
                if ( isAdmin ) {
                    if ( user.getAssignedBranchesAdmin() != null ) {
                        addedAssignments.addAll( user.getAssignedBranchesAdmin() );
                    }
                } else {
                    if ( user.getAssignedBranches() != null ) {
                        addedAssignments.addAll( user.getAssignedBranches() );
                    }
                }
            }
            //Add branch assignments
            for ( String sourceBranchId : addedAssignments ) {
                if ( upload.getBranchSourceMapping().containsKey( sourceBranchId ) ) {
                    long branchId = upload.getBranchSourceMapping().get( sourceBranchId );
                    Branch branch = userManagementService.getBranchById( branchId );
                    if ( branch == null ) {
                        throw new UserAssignmentException( "unable to find branch with branchId : " + branchId
                            + " and sourceId : " + sourceBranchId );
                    }
                    long regionId = branch.getRegion().getRegionId();
                    organizationManagementService.assignBranchToUser( adminUser, branchId, regionId, assigneeUser, isAdmin );
                } else {
                    throw new UserAssignmentException( "unable to resolve sourceBranchId : " + sourceBranchId );
                }
            }
            //Remove branch assignments
            for ( String sourceBranchId : deletedAssignments ) {
                if ( upload.getBranchSourceMapping().containsKey( sourceBranchId ) ) {
                    long branchId = upload.getBranchSourceMapping().get( sourceBranchId );
                    Branch branch = userManagementService.getBranchById( branchId );
                    if ( branch == null ) {
                        throw new UserAdditionException( "unable to find branch with branchId : " + branchId
                            + " and sourceId : " + sourceBranchId );
                    }
                    long regionId = branch.getRegion().getRegionId();
                    try {
                        int profilesMaster;
                        if ( isAdmin ) {
                            profilesMaster = CommonConstants.PROFILES_MASTER_BRANCH_ADMIN_PROFILE_ID;
                        } else {
                            profilesMaster = CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID;
                        }
                        UserProfile profile = userProfileDao.findUserProfile( assigneeUser.getUserId(), branchId, regionId,
                            profilesMaster );
                        userManagementService.removeUserProfile( assigneeUser, adminUser, profile.getUserProfileId() );
                    } catch ( NoRecordsFetchedException e ) {
                        throw new UserAssignmentException( "Unable to fetch userProfile for user. Reason: ", e );
                    }
                } else {
                    throw new UserAssignmentException( "unable to resolve sourceBranchId : " + sourceBranchId );
                }
            }
        }
        LOG.info( "Method assignBranchesToUser() for user : " + user.getEmailId() + " isAdmin : " + isAdmin
            + " finished." );
        return assigneeUser;
    }
    

    /**
     * Method to assign/unassign regions to user
     * @param user
     * @param adminUser
     * @param assigneeUser
     * @param currentUserMap
     * @param upload
     * @return
     * @throws UserAssignmentException
     * @throws InvalidInputException
     * @throws NoRecordsFetchedException
     * @throws SolrException
     * @throws UserAdditionException
     */
    User assignRegionsToUser( UserUploadVO user, User adminUser, User assigneeUser, Map<String, UserUploadVO> currentUserMap,
        HierarchyUpload upload, boolean isAdmin ) throws UserAssignmentException, InvalidInputException,
        NoRecordsFetchedException, SolrException, UserAdditionException
    {
        LOG.info( "Method assignRegionsToUser started for user : " + user.getEmailId() );
        if ( user.isSourceRegionIdModified() || user.isAssignedRegionsModified() ) {
            //Compare the current user assignments and the new assignments to find the changes
            /*
             * Cases to handle:
             * 1. All new assignments
             * 2. Delete all assignments
             * 3. add some, delete some assignments
             */
            List<String> addedAssignments = new ArrayList<String>();
            List<String> deletedAssignments = new ArrayList<String>();

            if ( currentUserMap.containsKey( user.getSourceUserId() ) ) {
                //Existing user
                List<String> oldAssignments;
                List<String> newAssignments;
                if ( isAdmin ) {
                    oldAssignments = currentUserMap.get( user.getSourceRegionId() ).getAssignedRegionsAdmin();
                    newAssignments = user.getAssignedRegionsAdmin();
                } else {
                    oldAssignments = currentUserMap.get( user.getSourceUserId() ).getAssignedRegions();
                    newAssignments = user.getAssignedRegions();
                }

                if ( oldAssignments == null || oldAssignments.isEmpty() ) {
                    //All assignments are new
                    addedAssignments.addAll( newAssignments );
                } else if ( newAssignments == null || newAssignments.isEmpty() ) {
                    //Delete all assignments
                    deletedAssignments.addAll( oldAssignments );
                } else {
                    //find added and deleted assignments
                    List<String> tempOldAssignments = new ArrayList<String>();
                    tempOldAssignments.addAll( oldAssignments );
                    List<String> tempNewAssignments = new ArrayList<String>();
                    tempNewAssignments.addAll( newAssignments );

                    tempOldAssignments.removeAll( newAssignments );
                    deletedAssignments.addAll( tempOldAssignments );

                    tempNewAssignments.removeAll( oldAssignments );
                    addedAssignments.addAll( tempNewAssignments );
                }
            } else {
                //All assignments are new
                if ( isAdmin ) {
                    if ( user.getAssignedRegionsAdmin() != null ) {
                        addedAssignments.addAll( user.getAssignedRegionsAdmin() );
                    }
                } else {
                    if ( user.getAssignedRegions() != null ) {
                        addedAssignments.addAll( user.getAssignedRegions() );
                    }
                }
            }
            //Add region assignments
            for ( String sourceRegionId : addedAssignments ) {
                if ( upload.getRegionSourceMapping().containsKey( sourceRegionId ) ) {
                    long regionId = upload.getRegionSourceMapping().get( sourceRegionId );
                    organizationManagementService.assignRegionToUser( adminUser, regionId, assigneeUser, isAdmin );
                } else {
                    throw new UserAssignmentException( "unable to resolve sourceRegionId : " + sourceRegionId );
                }
            }
            //Remove region assignments
            for ( String sourceRegionId : deletedAssignments ) {
                if ( upload.getRegionSourceMapping().containsKey( sourceRegionId ) ) {
                    long regionId = upload.getRegionSourceMapping().get( sourceRegionId );
                    Branch branch = organizationManagementService.getDefaultBranchForRegion( regionId );
                    if ( branch == null ) {
                        throw new UserAdditionException( "unable to find default branch for regionId : " + regionId
                            + " and sourceId : " + sourceRegionId );
                    }
                    try {
                        int profilesMaster;
                        if ( isAdmin ) {
                            profilesMaster = CommonConstants.PROFILES_MASTER_REGION_ADMIN_PROFILE_ID;
                        } else {
                            profilesMaster = CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID;
                        }
                        UserProfile profile = userProfileDao.findUserProfile( assigneeUser.getUserId(), branch.getBranchId(),
                            regionId, profilesMaster );
                        userManagementService.removeUserProfile( assigneeUser, adminUser, profile.getUserProfileId() );
                    } catch ( NoRecordsFetchedException e ) {
                        throw new UserAssignmentException( "Unable to fetch userProfile for user. Reason: ", e );
                    }
                } else {
                    throw new UserAssignmentException( "unable to resolve sourceRegionId : " + sourceRegionId );
                }
            }
        }
        LOG.info( "Method assignRegionsToUser finished for user : " + user.getEmailId() );
        return assigneeUser;
    }
    

    /**
     * Method to assign/unassign user to regions and branches
     * @param user
     * @param adminUser
     * @param currentUserMap
     * @param upload
     * @return
     * @throws UserAdditionException
     * @throws InvalidInputException
     * @throws SolrException
     * @throws NoRecordsFetchedException
     * @throws UserAssignmentException
     */
    User assignUser( UserUploadVO user, User adminUser, Map<String, UserUploadVO> currentUserMap, HierarchyUpload upload )
        throws UserAdditionException, InvalidInputException, SolrException, NoRecordsFetchedException, UserAssignmentException
    {
        LOG.info( "Method assignUser() started for user : " + user.getEmailId() );
        if ( !( checkIfEmailIdExistsWithCompany( user.getEmailId(), adminUser.getCompany() ) ) ) {
            throw new UserAdditionException( "User : " + user.getEmailId() + " belongs to a different company" );
        }
        User assigneeUser = userManagementService.getUserByEmailAddress( extractEmailId( user.getEmailId() ) );
        try {
            if ( ( user.getAssignedBranches() == null || user.getAssignedBranches().isEmpty() )
                && ( user.getAssignedRegions() == null || user.getAssignedRegions().isEmpty() )
                && ( user.getAssignedBranchesAdmin() == null || user.getAssignedBranchesAdmin().isEmpty() )
                && ( user.getAssignedRegionsAdmin() == null || user.getAssignedRegionsAdmin().isEmpty() ) ) {
                //TODO: Assign user to company
                Region region = organizationManagementService.getDefaultRegionForCompany( adminUser.getCompany() );
                userManagementService.assignUserToRegion( adminUser, assigneeUser.getUserId(), region.getRegionId() );
            } else {
                //Agent assignments
                assigneeUser = assignBranchesToUser( user, adminUser, assigneeUser, currentUserMap, upload, false );
                assigneeUser = assignRegionsToUser( user, adminUser, assigneeUser, currentUserMap, upload, false );
                //Admin assignments
                assigneeUser = assignBranchesToUser( user, adminUser, assigneeUser, currentUserMap, upload, true );
                assigneeUser = assignRegionsToUser( user, adminUser, assigneeUser, currentUserMap, upload, true );
            }
        } catch ( InvalidInputException e ) {
            //If it's because the user already exists, don't throw the exception
            if ( e.getMessage() == DisplayMessageConstants.USER_ASSIGNMENT_ALREADY_EXISTS ) {
                LOG.debug( "User assignment already exists" );
            } else {
                throw e;
            }
        }
        LOG.info( "Method assignUser() finished for user : " + user.getEmailId() );
        return assigneeUser;
    }
    
    
    


    boolean checkIfEmailIdExists( String emailId, Company company ) throws InvalidInputException
    {
        boolean status = false;
        emailId = extractEmailId( emailId );
        if ( emailId == null || emailId.isEmpty() ) {
            throw new InvalidInputException( "EmailId is empty" );
        }
        try {
            userManagementService.getUserByEmailAddress( emailId );
            status = true;
        } catch ( NoRecordsFetchedException e ) {
            status = false;
        }
        return status;
    }


    boolean checkIfEmailIdExistsWithCompany( String emailId, Company company ) throws InvalidInputException
    {
        boolean status = false;
        emailId = extractEmailId( emailId );
        if ( emailId == null || emailId.isEmpty() ) {
            throw new InvalidInputException( "EmailId is empty" );
        }
        try {
            User user = userManagementService.getUserByEmailAddress( emailId );
            if ( user.getCompany().getCompanyId() == company.getCompanyId() ) {
                status = true;
            }
        } catch ( NoRecordsFetchedException e ) {
            status = false;
        }
        return status;
    }


    public static String[] removeElements( String[] input, String deleteMe )
    {
        List<String> result = new LinkedList<String>();

        for ( String item : input )
            if ( !deleteMe.equals( item ) )
                result.add( item );

        String[] modifiedArray = result.toArray( new String[result.size()] );
        return modifiedArray;
    }


    String extractEmailId( String emailId )
    {
        if ( emailId.contains( "\"" ) ) {
            emailId = emailId.replace( "\"", "" );
        }
        String firstName = "";
        String lastName = "";
        String toRemove = null;
        if ( emailId.indexOf( "@" ) != -1 && emailId.indexOf( "." ) != -1 ) {
            if ( emailId.contains( " " ) ) {
                String[] userArray = emailId.split( " " );
                String[] userInformation = removeElements( userArray, "" );
                List<String> tempList = new LinkedList<String>();
                for ( String str : userInformation ) {
                    tempList.add( str );
                }
                String tempString = "";
                for ( int i = 0; i < tempList.size(); i++ ) {

                    LOG.debug( "removing extra spaces " );
                    if ( tempList.get( i ).equalsIgnoreCase( "<" ) ) {
                        if ( i + 1 < tempList.size() ) {
                            if ( !tempList.get( i + 1 ).contains( "<" ) ) {
                                tempString = tempList.get( i ).concat( tempList.get( i + 1 ) );

                                toRemove = tempList.get( i + 1 );
                                if ( i + 2 < tempList.size() ) {

                                    if ( tempList.get( i + 2 ).equalsIgnoreCase( ">" ) ) {
                                        tempString = tempString.concat( tempList.get( i + 2 ) );


                                    }
                                }
                            }
                        }
                    } else if ( tempList.get( i ).equalsIgnoreCase( ">" ) ) {
                        if ( !tempList.get( i - 1 ).contains( ">" ) ) {
                            if ( tempString.isEmpty() ) {
                                tempString = tempList.get( i - 1 ).concat( tempList.get( i ) );
                                toRemove = tempList.get( i - 1 );
                            }

                        }
                    }

                }
                if ( !tempString.isEmpty() ) {
                    tempList.add( tempString );
                }
                Iterator<String> it = tempList.iterator();
                while ( it.hasNext() ) {
                    String iteratedValue = it.next();
                    if ( iteratedValue.equalsIgnoreCase( "<" ) || iteratedValue.equalsIgnoreCase( ">" ) ) {
                        it.remove();
                    }
                    if ( toRemove != null ) {
                        if ( iteratedValue.equalsIgnoreCase( toRemove ) ) {
                            it.remove();
                        }
                    }
                }
                userInformation = tempList.toArray( new String[tempList.size()] );
                if ( userInformation.length >= 3 ) {
                    LOG.debug( "This contains middle name as well" );
                    for ( int i = 0; i < userInformation.length - 1; i++ ) {
                        firstName = firstName + userInformation[i] + " ";
                    }
                    firstName = firstName.trim();
                    lastName = userInformation[userInformation.length - 1];
                    if ( lastName.contains( "<" ) ) {
                        emailId = lastName.substring( lastName.indexOf( "<" ) + 1, lastName.length() - 1 );
                        lastName = lastName.substring( 0, lastName.indexOf( "<" ) );
                        if ( lastName.equalsIgnoreCase( "" ) ) {
                            lastName = userInformation[userInformation.length - 2];
                            if ( firstName.contains( lastName ) ) {
                                firstName = firstName.substring( 0, firstName.indexOf( lastName ) );
                            }
                        }
                    }

                } else if ( userInformation.length == 2 ) {
                    firstName = userInformation[0];
                    lastName = userInformation[1];
                    if ( lastName.contains( "<" ) ) {
                        emailId = lastName.substring( lastName.indexOf( "<" ) + 1, lastName.length() - 1 );
                        lastName = lastName.substring( 0, lastName.indexOf( "<" ) );
                    }
                }
            } else {
                LOG.debug( "Contains no space hence wont have a last name" );
                lastName = null;
                if ( emailId.contains( "<" ) ) {
                    firstName = emailId.substring( 0, emailId.indexOf( "<" ) );
                    if ( firstName.equalsIgnoreCase( "" ) ) {
                        firstName = emailId.substring( emailId.indexOf( "<" ) + 1, emailId.indexOf( "@" ) );
                    }
                    emailId = emailId.substring( emailId.indexOf( "<" ) + 1, emailId.indexOf( ">" ) );

                } else {
                    LOG.debug( "This doesnt contain a first name and last name" );
                    firstName = emailId.substring( 0, emailId.indexOf( "@" ) );
                }

            }
        }
        return emailId;
    }



    /**
     * Method to upload users from hierarchy upload object
     * @param upload
     * @param adminUser
     */
    @Transactional
    void uploadUsers( HierarchyUpload upload, User adminUser )
    {
        LOG.debug( "Uploading users to database" );
        Map<String, UserUploadVO> currentUserMap = new HashMap<String, UserUploadVO>();
        try {
                HierarchyUpload currentUpload = hierarchyUploadDao.getHierarchyUploadByCompany( adminUser.getCompany()
                    .getCompanyId() );
                if ( currentUpload != null && currentUpload.getUsers() != null ) {
                    for ( UserUploadVO user : currentUpload.getUsers() ) {
                        currentUserMap.put( user.getSourceUserId(), user );
                    }
                }
            List<UserUploadVO> usersToUpload = upload.getUsers();
            if ( usersToUpload == null || usersToUpload.isEmpty() ) {
                return;
            }
            for ( UserUploadVO userToBeUploaded : usersToUpload ) {
                User user = null;
                if ( !userToBeUploaded.isUserAdded() && !userToBeUploaded.isUserModified() ) {
                    continue;
                }
                if ( checkIfEmailIdExists( userToBeUploaded.getEmailId(), adminUser.getCompany() ) ) {
                    user = assignUser( userToBeUploaded, adminUser, currentUserMap, upload );
                } else {
                    // add user
                    user = addUser( userToBeUploaded, adminUser, currentUserMap, upload );
                }
                userToBeUploaded.setUserId( user.getUserId() );
                updateUserSettingsInMongo( user, userToBeUploaded );
                //map the history records
                //mapUserModificationHistory( userToBeUploaded, user );
                //map the id mapping
                if ( userToBeUploaded.getSourceRegionId() != null && !userToBeUploaded.getSourceRegionId().isEmpty() ) {
                    upload.getRegionSourceMapping().put( userToBeUploaded.getSourceRegionId(), userToBeUploaded.getRegionId() );
                }
                if ( userToBeUploaded.getSourceBranchId() != null && !userToBeUploaded.getSourceBranchId().isEmpty() ) {
                    upload.getBranchSourceMapping().put( userToBeUploaded.getSourceBranchId(), userToBeUploaded.getBranchId() );
                }
                if ( userToBeUploaded.getSourceUserId() != null && !userToBeUploaded.getSourceUserId().isEmpty() ) {
                    upload.getUserSourceMapping().put( userToBeUploaded.getSourceUserId(), userToBeUploaded.getUserId() );
                }
                
                //Store the updated userUploads in upload
                upload.setUsers( usersToUpload );
            }
        } catch ( Exception e ) {
            // TODO: Add error records
            e.printStackTrace();
        }
        LOG.debug( "Finished uploading users to the database" );
    }


    /**
     * Method to add user to MySQL
     * @param user
     * @param adminUser
     * @param currentUserMap
     * @param upload
     * @return
     * @throws InvalidInputException
     * @throws NoRecordsFetchedException
     * @throws SolrException
     * @throws UserAssignmentException
     * @throws UserAdditionException
     */
    User addUser( UserUploadVO user, User adminUser, Map<String, UserUploadVO> currentUserMap, HierarchyUpload upload ) throws InvalidInputException, NoRecordsFetchedException, SolrException,
        UserAssignmentException, UserAdditionException
    {
        LOG.info( "Method addUser() started for user : " + user.getEmailId() ); 
        User uploadedUser = null;
        List<User> userList = new ArrayList<User>();
        if ( checkIfEmailIdExists( user.getEmailId(), adminUser.getCompany() ) ) {
            throw new UserAdditionException( "The user already exists" );
        }
        //Mask email address if needed
        String emailId = user.getEmailId();
        if ( user.getEmailId() == null || user.getEmailId().isEmpty() ) {
            throw new InvalidInputException( "User email ID cannot be null" );
        }
        if ( CommonConstants.YES_STRING.equals( maskEmail ) ) {
            emailId = utils.maskEmailAddress( emailId );
            if ( emailId != null ) {
                user.setEmailId( user.getFirstName()
                    + ( user.getLastName() != null ? " " + user.getLastName() : "" ) + " <"
                    + emailId + ">" );
            }
        } else {
            user.setEmailId( user.getFirstName()
                + ( user.getLastName() != null ? " " + user.getLastName() : "" ) + " <"
                + emailId + ">" );
        }
        
        //Add user and call assignUser method
        //Add user
        Map<String, List<User>>resultMap = organizationManagementService.getUsersFromEmailIdsAndInvite( new String[] { user.getEmailId() }, adminUser, true );
        if ( resultMap != null ) {
            userList = (List<User>) resultMap.get( CommonConstants.VALID_USERS_LIST );
            if ( userList != null && !userList.isEmpty() ) {
                uploadedUser = userList.get( 0 );
            } else {
                throw new UserAdditionException( "Unable to add user with emailID : " + user.getEmailId() );
            }
        }
        LOG.debug( "Added user with email : " + user.getEmailId() );
        //Assign user
        assignUser( user, adminUser, currentUserMap, upload );
        
        LOG.info( "Method addUser() finished for user : " + user.getEmailId() ); 
        return uploadedUser;
    }


    /**
     * Method to update agentSettings of user in mongo
     * @param user
     * @param userUploadVO
     * @throws InvalidInputException
     */
    private void updateUserSettingsInMongo( User user, UserUploadVO userUploadVO ) throws InvalidInputException
    {
        LOG.debug( "Inside method updateUserSettingsInMongo " );
        AgentSettings agentSettings = userManagementService.getAgentSettingsForUserProfiles( user.getUserId() );
        if ( agentSettings == null ) {
            throw new InvalidInputException( "No company settings found for user " + user.getUsername() + " "
                + user.getUserId() );
        } else {
            ContactDetailsSettings contactDetailsSettings = agentSettings.getContact_details();
            if ( contactDetailsSettings == null ) {
                contactDetailsSettings = new ContactDetailsSettings();
            }
            ContactNumberSettings contactNumberSettings = contactDetailsSettings.getContact_numbers();
            if ( contactNumberSettings == null ) {
                contactNumberSettings = new ContactNumberSettings();
            }
            contactNumberSettings.setWork( userUploadVO.getPhoneNumber() );
            contactDetailsSettings.setContact_numbers( contactNumberSettings );
            contactDetailsSettings.setAbout_me( userUploadVO.getAboutMeDescription() );
            contactDetailsSettings.setTitle( userUploadVO.getTitle() );
            WebAddressSettings webAddressSettings = contactDetailsSettings.getWeb_addresses();
            if ( webAddressSettings == null ) {
                webAddressSettings = new WebAddressSettings();
            }
            webAddressSettings.setWork( userUploadVO.getWebsiteUrl() );
            contactDetailsSettings.setWeb_addresses( webAddressSettings );
            agentSettings.setContact_details( contactDetailsSettings );

            if ( userUploadVO.getLicense() != null && !userUploadVO.getLicense().isEmpty() ) {
                Licenses licenses = agentSettings.getLicenses();
                if ( licenses == null ) {
                    licenses = new Licenses();
                }
                List<String> authorizedIn = licenses.getAuthorized_in();
                if ( authorizedIn == null ) {
                    authorizedIn = new ArrayList<String>();
                }
                licenses.setAuthorized_in( getAllStateLicenses( userUploadVO.getLicense(), authorizedIn ) );
                agentSettings.setLicenses( licenses );
                if ( licenses != null && licenses.getAuthorized_in() != null && !licenses.getAuthorized_in().isEmpty() ) {
                    organizationUnitSettingsDao.updateParticularKeyAgentSettings(
                        MongoOrganizationUnitSettingDaoImpl.KEY_LICENCES, licenses, agentSettings );
                }
            }
            agentSettings.setDisclaimer( userUploadVO.getLegalDisclaimer() );

            profileManagementService.updateAgentContactDetails( MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION,
                agentSettings, contactDetailsSettings );

            if ( userUploadVO.getLegalDisclaimer() != null ) {
                organizationUnitSettingsDao.updateParticularKeyOrganizationUnitSettings(
                    MongoOrganizationUnitSettingDaoImpl.KEY_DISCLAIMER, userUploadVO.getLegalDisclaimer(), agentSettings,
                    MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION );
            }

            if ( userUploadVO.getUserPhotoUrl() != null ) {

                updateProfileImageForAgent( userUploadVO.getUserPhotoUrl(), agentSettings );
                /*
                 * profileManagementService.updateProfileImage(
                 * MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION, agentSettings,
                 * userUploadVO.getUserPhotoUrl() );
                 */
            }
        }
    }


    private List<String> getAllStateLicenses( String licenses, List<String> authorizedIn )
    {
        String toRemove = "Licensed State(s):";
        if ( licenses.indexOf( toRemove ) != -1 ) {
            licenses = licenses.substring( licenses.indexOf( "Licensed State(s):" ) + toRemove.length(), licenses.length() );
        }
        licenses = licenses.trim();
        authorizedIn.add( licenses );
        return authorizedIn;
    }


    private void updateProfileImageForAgent( String userPhotoUrl, AgentSettings agentSettings ) throws InvalidInputException
    {
        LOG.debug( "Uploading for agent " + agentSettings.getIden() + " with photo: " + userPhotoUrl );
        // TODO: Check if the image is local or online. In case it is local, then we should
        // upload that to S3 and then link the same
        /*
         * String profileImageUrl = null; if
         * (userPhotoUrl.trim().matches(CommonConstants.URL_REGEX)) {
         * LOG.debug("Profile photo is publicaly available"); profileImageUrl = userPhotoUrl; } else
         * { LOG.debug("User photo is locally available. Uploading the image to cloud"); File
         * imageFile = new File(userPhotoUrl); String imageName =
         * userPhotoUrl.substring(userPhotoUrl.lastIndexOf(CommonConstants.FILE_SEPARATOR)); String
         * profileImageName = fileUploadService.fileUploadHandler(imageFile, imageName);
         * profileImageUrl = amazonEndpoint + CommonConstants.FILE_SEPARATOR + amazonImageBucket +
         * CommonConstants.FILE_SEPARATOR + profileImageName; }
         */
        profileManagementService.updateProfileImage( MongoOrganizationUnitSettingDaoImpl.AGENT_SETTINGS_COLLECTION,
            agentSettings, userPhotoUrl );
    }

}
