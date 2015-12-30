package com.realtech.socialsurvey.core.starter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.realtech.socialsurvey.core.commons.CommonConstants;
import com.realtech.socialsurvey.core.dao.OrganizationUnitSettingsDao;
import com.realtech.socialsurvey.core.entities.AgentSettings;
import com.realtech.socialsurvey.core.entities.BillingReportData;
import com.realtech.socialsurvey.core.entities.ContactDetailsSettings;
import com.realtech.socialsurvey.core.entities.FileUpload;
import com.realtech.socialsurvey.core.entities.OrganizationUnitSettings;
import com.realtech.socialsurvey.core.exception.InvalidInputException;
import com.realtech.socialsurvey.core.exception.NoRecordsFetchedException;
import com.realtech.socialsurvey.core.services.mail.EmailServices;
import com.realtech.socialsurvey.core.services.mail.UndeliveredEmailException;
import com.realtech.socialsurvey.core.services.organizationmanagement.DashboardService;
import com.realtech.socialsurvey.core.services.organizationmanagement.OrganizationManagementService;
import com.realtech.socialsurvey.core.services.upload.CsvUploadService;

@Component
public class PrepareBillingReport implements Runnable
{
    public static final Logger LOG = LoggerFactory.getLogger( PrepareBillingReport.class );

    @Value ( "${BATCH_SIZE}")
    private int batchSize;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private OrganizationUnitSettingsDao organizationUnitSettingsDao;

    @Autowired
    private OrganizationManagementService organizationManagementService;

    @Autowired
    private CsvUploadService csvUploadService;

    @Autowired
    private EmailServices emailServices;

    @Value ( "${FILE_DIRECTORY_LOCATION}")
    private String fileDirectoryLocation;

    @Value ( "${APPLICATION_ADMIN_EMAIL}")
    private String adminEmailId;

    @Value ( "${APPLICATION_ADMIN_NAME}")
    private String adminName;


    @Override
    public void run()
    {
        LOG.info( "Started method to prepare billing report" );
        //Check if a request for billing report is present in FILE_UPLOAD table
        while ( true ) {
            try {
                List<FileUpload> filesToBeUploaded = dashboardService.getBillingReportToBeSent();
                for ( FileUpload fileUpload : filesToBeUploaded ) {
                    try {
                        // update the status to be processing
                        fileUpload.setStatus( CommonConstants.STATUS_UNDER_PROCESSING );
                        csvUploadService.updateFileUploadRecord( fileUpload );
                        
                        // prepare and send the billing report to admin
                        prepareAndSendBillingReport();
                        
                        // update the status to be processed
                        fileUpload.setStatus( CommonConstants.STATUS_INACTIVE );
                        csvUploadService.updateFileUploadRecord( fileUpload );
                    } catch ( InvalidInputException e ) {
                        LOG.debug( "Error updating the status" );
                        continue;
                    }
                }
            } catch ( NoRecordsFetchedException e ) {
                LOG.debug( "No files to be uploaded. Sleep for a minute" );
                try {
                    Thread.sleep( 1000 * 60 );
                } catch ( InterruptedException e1 ) {
                    LOG.warn( "Thread interrupted" );
                    break;
                }
            }
        }
    }


    /**
     * Method to prepare and mail billing report to admin
     */
    void prepareAndSendBillingReport()
    {
        int startIndex = 0;

        XSSFWorkbook workbook = new XSSFWorkbook();

        // Create a blank sheet
        XSSFSheet sheet = workbook.createSheet();
        Integer counter = 1;

        // This data needs to be written (List<Object>)
        Map<String, List<Object>> data = new TreeMap<>();
        List<Object> billingReportToPopulate = new ArrayList<>();

        // Store regions settings in map
        Map<Long, OrganizationUnitSettings> regionsSettings = new HashMap<Long, OrganizationUnitSettings>();

        // Store branches settings in map
        Map<Long, OrganizationUnitSettings> branchesSettings = new HashMap<Long, OrganizationUnitSettings>();

        // Store companies settings in map
        Map<Long, OrganizationUnitSettings> companiesSettings = new HashMap<Long, OrganizationUnitSettings>();

        //Store records for each batch here
        List<BillingReportData> records = null;

        //Get records batch-wise and add to reportDataObject
        do {
            records = dashboardService.getBillingReportRecords( startIndex, batchSize );
            if ( records != null ) {

                //start populating the data
                for ( BillingReportData reportRow : records ) {
                    OrganizationUnitSettings companySettings = null;
                    if ( companiesSettings.containsKey( reportRow.getCompanyId() ) ) {
                        companySettings = companiesSettings.get( reportRow.getCompanyId() );
                    } else {
                        companySettings = organizationUnitSettingsDao.fetchOrganizationUnitSettingsById(
                            reportRow.getCompanyId(), CommonConstants.COMPANY_SETTINGS_COLLECTION );

                        //If companySettings is null, log the error and send a mail to the admin
                        if ( companySettings == null ) {
                            LOG.error( "Company Settings not found for company ID : " + reportRow.getCompanyId() );
                            //TODO: Send a mail to the admin?
                            continue;
                        }
                        companiesSettings.put( reportRow.getCompanyId(), companySettings );
                    }

                    //Company Name
                    billingReportToPopulate.add( reportRow.getCompany() );

                    //Region name
                    if ( reportRow.getRegion().equalsIgnoreCase( CommonConstants.DEFAULT_REGION_NAME ) ) {
                        billingReportToPopulate.add( "" );
                    } else {
                        billingReportToPopulate.add( reportRow.getRegion() );
                    }

                    //Branch name
                    if ( reportRow.getBranch().equalsIgnoreCase( CommonConstants.DEFAULT_BRANCH_NAME ) ) {
                        billingReportToPopulate.add( "" );
                    } else {
                        billingReportToPopulate.add( reportRow.getBranch() );
                    }

                    //First Name
                    billingReportToPopulate.add( reportRow.getFirstName() );

                    //Last Name
                    if ( reportRow.getLastName() == null || reportRow.getLastName().isEmpty()
                        || reportRow.getLastName().equalsIgnoreCase( "null" ) ) {
                        billingReportToPopulate.add( "" );
                    } else {
                        billingReportToPopulate.add( reportRow.getLastName() );
                    }

                    //Login ID
                    billingReportToPopulate.add( reportRow.getLoginName() );

                    boolean isAgent = isUserAnAgent( reportRow.getProfilesMasterIds() );
                    AgentSettings agentSettings = organizationUnitSettingsDao.fetchAgentSettingsById( reportRow.getUserId() );

                    if ( agentSettings == null ) {
                        LOG.error( "Agent profile null for user ID : " + reportRow.getUserId() );
                        continue;
                    }
                    
                    //Public profile page url
                    if ( isAgent ) {

                        if ( agentSettings.getCompleteProfileUrl() == null || agentSettings.getCompleteProfileUrl().isEmpty() ) {
                            LOG.error( "Agent profile url is empty for agentID : " + agentSettings.getIden() );
                            billingReportToPopulate.add( "NA" );
                        } else {
                            billingReportToPopulate.add( agentSettings.getCompleteProfileUrl() );
                        }
                    } else {
                        billingReportToPopulate.add( "" );
                    }
                    

                    //INDIVIDUAL PUBLIC PROFILE PAGE(is agent or not)
                    if ( isAgent ) {
                        billingReportToPopulate.add( CommonConstants.YES_STRING );
                    } else {
                        billingReportToPopulate.add( CommonConstants.NO_STRING );
                    }

                    //Address
                    try {
                        billingReportToPopulate.add( getAddressForUser( agentSettings, companySettings, reportRow,
                            reportRow.getCompanyId(), regionsSettings, branchesSettings ) );
                    } catch ( InvalidInputException e ) {
                        LOG.error( "An error occured while fetching the address for the user. Reason : ", e );
                    }

                    data.put( ( ++counter ).toString(), billingReportToPopulate );
                    billingReportToPopulate = new ArrayList<>();
                }
            }
            startIndex += batchSize;
        } while ( records != null );

        // Setting up headers
        billingReportToPopulate.add( CommonConstants.HEADER_COMPANY );
        billingReportToPopulate.add( CommonConstants.HEADER_REGION );
        billingReportToPopulate.add( CommonConstants.HEADER_BRANCH );
        billingReportToPopulate.add( CommonConstants.HEADER_FIRST_NAME );
        billingReportToPopulate.add( CommonConstants.HEADER_LAST_NAME );
        billingReportToPopulate.add( CommonConstants.HEADER_LOGIN_ID );
        billingReportToPopulate.add( CommonConstants.HEADER_PUBLIC_PROFILE_URL );
        billingReportToPopulate.add( CommonConstants.HEADER_IS_AGENT );
        billingReportToPopulate.add( CommonConstants.HEADER_ADDRESS );


        data.put( "1", billingReportToPopulate );

        // Iterate over data and write to sheet
        DecimalFormat decimalFormat = new DecimalFormat( "#0" );
        decimalFormat.setRoundingMode( RoundingMode.DOWN );

        int rownum = 0;
        for ( int i = 1; i <= data.size(); i ++ ) {
            String key = String.valueOf( i );
            Row row = sheet.createRow( rownum++ );
            List<Object> objArr = data.get( key );
            int cellnum = 0;
            for ( Object obj : objArr ) {
                Cell cell = row.createCell( cellnum++ );
                if ( obj instanceof String )
                    cell.setCellValue( (String) obj );
                else if ( obj instanceof Integer )
                    cell.setCellValue( (Integer) obj );
                else if ( obj instanceof Double )
                    cell.setCellValue( decimalFormat.format( obj ) );
                else if ( obj instanceof Long )
                    cell.setCellValue( (Long) obj );
                else if ( obj instanceof Boolean )
                    cell.setCellValue( (Boolean) obj );
            }
        }
        //Create file and write report into it
        boolean excelCreated = false;
        String fileName = "Billing_Report-" + ( new Timestamp( new Date().getTime() ) );
        FileOutputStream fileOutput = null;
        InputStream inputStream = null;
        File file = null;
        String filePath = null;
        try {
            file = new File( fileDirectoryLocation + File.separator + fileName + ".xls" );
            fileOutput = new FileOutputStream( file );
            file.createNewFile();
            workbook.write( fileOutput );
            filePath = file.getPath();
            excelCreated = true;
        } catch ( FileNotFoundException fe ) {
            LOG.error( "Exception caught " + fe.getMessage() );
            excelCreated = false;
        } catch ( IOException e ) {
            LOG.error( "Exception caught " + e.getMessage() );
            excelCreated = false;
        } finally {
            try {
                fileOutput.close();
                if ( inputStream != null ) {
                    inputStream.close();
                }
            } catch ( IOException e ) {
                LOG.error( "Exception caught " + e.getMessage() );
                excelCreated = false;
            }
        }

        //Mail the report to the admin
        try {
            if ( excelCreated ) {
                Map<String, String> attachmentsDetails = new HashMap<String, String>();
                attachmentsDetails.put( fileName + ".xls", filePath );
                emailServices.sendBillingReportMail( adminName, "", adminEmailId, attachmentsDetails );
            }
        } catch ( InvalidInputException | UndeliveredEmailException e ) {
            LOG.error( "Exception caught in sendCorruptDataFromCrmNotificationMail() while sending mail to company admin" );
        }
    }

    /**
     * Method to check if user is agent or not
     * @param userId
     * @throws InvalidInputException 
     */
    boolean isUserAnAgent( List<Long> profilesMasters )
    {
        for ( Long profilesMaster : profilesMasters ) {
            if ( profilesMaster == CommonConstants.PROFILES_MASTER_AGENT_PROFILE_ID ) {
                return true;
            }
        }
        return false;
    }


    /**
     * Method to get the address for a user
     * @param agentSettings
     * @param companySettings
     * @param reportRow
     * @param companyId
     * @param regionsSettings
     * @param branchesSettings
     * @return
     * @throws InvalidInputException
     */
    String getAddressForUser( AgentSettings agentSettings, OrganizationUnitSettings companySettings,
        BillingReportData reportRow, long companyId, Map<Long, OrganizationUnitSettings> regionsSettings,
        Map<Long, OrganizationUnitSettings> branchesSettings ) throws InvalidInputException
    {
        LOG.info( "Method getStateForUser started" );
        if ( companyId <= 0l ) {
            throw new InvalidInputException( "Invalid companyID for ID : " + companyId );
        }
        if ( companySettings == null ) {
            throw new InvalidInputException( "Company Settings empty for companyID : " + companyId );
        }
        if ( agentSettings == null ) {
            throw new InvalidInputException( "Agent settings null!" );
        }
        if ( reportRow == null ) {
            throw new InvalidInputException( "Billing Report row is null" );
        }
        if ( regionsSettings == null ) {
            throw new InvalidInputException( "Regions Settings is null" );
        }
        if ( branchesSettings == null ) {
            throw new InvalidInputException( "Branches Settings is null" );
        }
        if ( companySettings.getContact_details() == null || companySettings.getContact_details().getState() == null
            || companySettings.getContact_details().getState().isEmpty() ) {
            throw new InvalidInputException( "Unable to get contact details for company ID : " + companyId );
        }
        String companyAddress = getAddressFromContactDetails( companySettings.getContact_details() );
        if ( agentSettings.getContact_details() == null || agentSettings.getContact_details().getState() == null
            || agentSettings.getContact_details().getState().isEmpty() ) {
            if ( reportRow.getBranch().equals( CommonConstants.DEFAULT_BRANCH_NAME ) ) {
                //Get address from region/company
                return getRegionAddress( reportRow, companyAddress, regionsSettings );
            } else {
                //Check if branchSettings already exists in the map
                if ( branchesSettings.containsKey( reportRow.getBranchId() ) ) {
                    OrganizationUnitSettings branchSettings = branchesSettings.get( reportRow.getBranchId() );
                    if ( branchSettings.getContact_details() == null || branchSettings.getContact_details().getState() == null
                        || branchSettings.getContact_details().getState().isEmpty() ) {
                        //get state from region/company
                        return getRegionAddress( reportRow, companyAddress, regionsSettings );
                    } else {
                        return getAddressFromContactDetails( branchSettings.getContact_details() );
                    }
                } else {
                    //The branchSettings doesn't yet exist in the map
                    try {
                        OrganizationUnitSettings branchSettings = organizationManagementService
                            .getBranchSettingsDefault( reportRow.getBranchId() );
                        //Add to map
                        branchesSettings.put( reportRow.getBranchId(), branchSettings );
                        if ( branchSettings.getContact_details() == null
                            || branchSettings.getContact_details().getState() == null
                            || branchSettings.getContact_details().getState().isEmpty() ) {
                            //get state from region/company
                            return getRegionAddress( reportRow, companyAddress, regionsSettings );
                        } else {
                            return getAddressFromContactDetails( branchSettings.getContact_details() );
                        }
                    } catch ( NoRecordsFetchedException e ) {
                        throw new InvalidInputException( "branch settings don't exist for the branch ID : "
                            + reportRow.getBranchId() );
                    }
                }
            }
        } else {
            return getAddressFromContactDetails( agentSettings.getContact_details() );
        }
    }


    /**
     * Get the address of the region
     * @param reportRow
     * @param companyAddress
     * @param regionsSettings
     * @return
     * @throws InvalidInputException
     */
    String getRegionAddress( BillingReportData reportRow, String companyAddress,
        Map<Long, OrganizationUnitSettings> regionsSettings ) throws InvalidInputException
    {
        LOG.info( "Method getRegionState started" );
        if ( reportRow == null ) {
            throw new InvalidInputException( "Billing Report row is null" );
        }
        if ( companyAddress == null || companyAddress.isEmpty() ) {
            throw new InvalidInputException( "Company State cannot be empty!" );
        }
        if ( regionsSettings == null ) {
            throw new InvalidInputException( "Regions Settings is null" );
        }
        if ( reportRow.getRegion().equals( CommonConstants.DEFAULT_REGION_NAME ) ) {
            return companyAddress;
        } else {
            //Check if regionSettings already exists for the current region in the map
            if ( regionsSettings.containsKey( reportRow.getRegionId() ) ) {
                OrganizationUnitSettings regionSettings = regionsSettings.get( reportRow.getRegionId() );
                if ( regionSettings.getContact_details() == null || regionSettings.getContact_details().getState() == null
                    || regionSettings.getContact_details().getState().isEmpty() ) {
                    return companyAddress;
                }
                return getAddressFromContactDetails( regionSettings.getContact_details() );
            } else {
                //The regionSettings doesn't  yet exist in the map
                OrganizationUnitSettings regionSettings = organizationManagementService.getRegionSettings( reportRow
                    .getRegionId() );
                //Add to map
                regionsSettings.put( reportRow.getRegionId(), regionSettings );
                if ( regionSettings.getContact_details() == null || regionSettings.getContact_details().getState() == null
                    || regionSettings.getContact_details().getState().isEmpty() ) {
                    return companyAddress;
                }
                return getAddressFromContactDetails( regionSettings.getContact_details() );
            }
        }
    }


    String getAddressFromContactDetails( ContactDetailsSettings contactDetails ) throws InvalidInputException
    {
        if ( contactDetails == null ) {
            throw new InvalidInputException( "Contact details is null" );
        }
        String fullAddress = "";

        //Get Address
        if ( contactDetails.getAddress() != null && !( contactDetails.getAddress().isEmpty() ) ) {
            fullAddress += contactDetails.getAddress();
        }

        //Get City
        if ( contactDetails.getCity() != null && !( contactDetails.getCity().isEmpty() ) ) {
            fullAddress += ", " + contactDetails.getCity();
        }

        //Get State
        if ( contactDetails.getState() != null && !( contactDetails.getState().isEmpty() ) ) {
            fullAddress += ", " + contactDetails.getState();
        }

        //Get Zip-code
        if ( contactDetails.getZipcode() != null && !( contactDetails.getZipcode().isEmpty() ) ) {
            fullAddress += ", " + contactDetails.getZipcode();
        }

        //Get Country-code 
        if ( contactDetails.getCountryCode() != null && !( contactDetails.getCountryCode().isEmpty() ) ) {
            fullAddress += ", " + contactDetails.getCountryCode();
        }

        return fullAddress;
    }
}
