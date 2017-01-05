package com.realtech.socialsurvey.api.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.realtech.socialsurvey.api.exceptions.SSApiException;
import com.realtech.socialsurvey.api.models.v2.SurveyGetV2VO;
import com.realtech.socialsurvey.api.transformers.SurveysAndReviewsV2VOTransformer;
import com.realtech.socialsurvey.api.utils.RestUtils;
import com.realtech.socialsurvey.core.commons.CommonConstants;
import com.realtech.socialsurvey.core.entities.User;
import com.realtech.socialsurvey.core.exception.AuthorizationException;
import com.realtech.socialsurvey.core.exception.InvalidInputException;
import com.realtech.socialsurvey.core.exception.NoRecordsFetchedException;
import com.realtech.socialsurvey.core.services.admin.AdminAuthenticationService;
import com.realtech.socialsurvey.core.services.organizationmanagement.UserManagementService;
import com.realtech.socialsurvey.core.services.surveybuilder.SurveyHandler;
import com.realtech.socialsurvey.core.vo.SurveysAndReviewsVO;
import com.wordnik.swagger.annotations.ApiOperation;


//@RestController
//@RequestMapping ( "/api/v1.2")
public class SurveyApiV2Controller
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SurveyApiV2Controller.class );

    @Autowired
    private SurveysAndReviewsV2VOTransformer surveysAndReviewsV2VOTransformer;

    @Autowired
    private SurveyHandler surveyHandler;

    @Autowired
    private AdminAuthenticationService adminAuthenticationService;

    @Autowired
    private RestUtils restUtils;

    @Autowired
    private UserManagementService userManagementService;


    @RequestMapping ( value = "/survey", method = RequestMethod.GET)
    @ApiOperation ( value = "Get Survey Transactions")
    public ResponseEntity<?> getSurveyTransactions( HttpServletRequest request ) throws SSApiException
    {
        LOGGER.info( "SurveyApiController.getSurveyTransactions started" );

        //authorize request
        String authorizationHeader = request.getHeader( CommonConstants.SURVEY_API_REQUEST_PARAMETER_AUTHORIZATION );
        long companyId = 0;
        try {
            companyId = adminAuthenticationService.validateAuthHeader( authorizationHeader );
        } catch ( AuthorizationException e1 ) {
            return restUtils.getRestResponseEntity( HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", null, null, request , companyId );
        }

        //parse request parameters from request
        String countStr = request.getParameter( "count" );
        String startStr = request.getParameter( "start" );
        String status = request.getParameter( "status" );
        String startSurveyIDStr = request.getParameter("startSurveyID");
        String startReviewDateStr = request.getParameter( "startReviewDate" );
        String startTransactionDateStr = request.getParameter( "startTransactionDate" );
        String state = request.getParameter( "state" );
        String userEmailAddress = request.getParameter( "user" );
        String includeManagedTeamStr = request.getParameter( "includeManagedTeam" );
        
        Set<String> inputRequestParameters = request.getParameterMap().keySet();
        List<String> fixReqParameters =  Arrays.asList("count" , "start" , "status" , "startSurveyID" , "startReviewDate" ,  "startTransactionDate" , "state" , "user" , "includeManagedTeam");
        for(String currParameter : inputRequestParameters){
        	if( ! fixReqParameters.contains(currParameter)){
        		return restUtils.getRestResponseEntity( HttpStatus.BAD_REQUEST, "Unsupported filter parameter : " + currParameter , null, null,
                        request , companyId  );
        	}
        }

        int count = CommonConstants.SURVEY_API_DEFAUAT_BATCH_SIZE;
        int start = 0;
        if ( countStr != null ) {
            try {
                count = Integer.parseInt( countStr );
                // default count is 1000
                if ( count > CommonConstants.SURVEY_API_DEFAUAT_BATCH_SIZE ) {
                    count = CommonConstants.SURVEY_API_DEFAUAT_BATCH_SIZE;
                }
            } catch ( NumberFormatException e ) {
                return restUtils.getRestResponseEntity( HttpStatus.BAD_REQUEST, "Passed parameter count is invalid", null, null,
                    request , companyId  );
            }
        }

        if ( startStr != null ) {
            try {
                start = Integer.parseInt( startStr );
            } catch ( NumberFormatException e ) {
                return restUtils.getRestResponseEntity( HttpStatus.BAD_REQUEST, "Passed parameter start is invalid", null, null,
                    request , companyId  );
            }
        }

        if ( status == null ) {
            status = "all";
        } else if ( !status.equalsIgnoreCase( "complete" ) && !status.equalsIgnoreCase( "incomplete" ) ) {
            return restUtils.getRestResponseEntity( HttpStatus.BAD_REQUEST, "Passed parameter status is invalid", null, null,
                request , companyId  );
        }

        List<String> validState = new ArrayList<String>();
        validState.add( CommonConstants.SURVEY_MOOD_GREAT );
        validState.add( CommonConstants.SURVEY_MOOD_OK );
        validState.add( CommonConstants.SURVEY_MOOD_UNPLEASANT );

        if ( state != null && !validState.contains( state ) ) {
            return restUtils.getRestResponseEntity( HttpStatus.BAD_REQUEST, "Passed parameter state is invalid", null, null,
                request , companyId  );
        }
        
        
        Long startSurveyID = null;
		if (!StringUtils.isEmpty(startSurveyIDStr)) {
			try {
				startSurveyID = Long.parseLong(startSurveyIDStr);
			} catch (NumberFormatException e) {
				return restUtils.getRestResponseEntity(HttpStatus.BAD_REQUEST, "Passed parameter startReviewDate is invalid", null,null, request , companyId );
			}
		}
        
        Date startReviewDate = null;
        if ( startReviewDateStr != null && !startReviewDateStr.isEmpty() ) {
            try {
                startReviewDate = new SimpleDateFormat( CommonConstants.DATE_FORMAT ).parse( startReviewDateStr );
            } catch ( ParseException e ) {
                return restUtils.getRestResponseEntity( HttpStatus.BAD_REQUEST, "Passed parameter startReviewDate is invalid",
                    null, null, request , companyId  );
            }
        }

        //startTransactionDate
        Date startTransactionDate = null;
        if ( startTransactionDateStr != null && !startTransactionDateStr.isEmpty() ) {
            try {
                startTransactionDate = new SimpleDateFormat( CommonConstants.DATE_FORMAT ).parse( startTransactionDateStr );
            } catch ( ParseException e ) {
                return restUtils.getRestResponseEntity( HttpStatus.BAD_REQUEST, "Passed parameter startReviewDate is invalid",
                    null, null, request , companyId  );
            }
        }
        
        
        //includeManagedTeam
        Boolean includeManagedTeam = null;
        if( ! StringUtils.isEmpty(includeManagedTeamStr)){
        	try{
                includeManagedTeam = Boolean.parseBoolean(includeManagedTeamStr);
        	}catch(NumberFormatException e){
        		return restUtils.getRestResponseEntity( HttpStatus.BAD_REQUEST, "Passed parameter includeManagedTeam is invalid",
                        null, null, request , companyId  );
        	}
        	
        	if(StringUtils.isEmpty( userEmailAddress )){
        		return restUtils.getRestResponseEntity( HttpStatus.BAD_REQUEST, "Passed parameter userEmailAddress can not be empty if includeManagedTeam is used ",
                        null, null, request , companyId  );
        	}
        }

        //get user
        List<Long> userIds = null;
        if ( !StringUtils.isEmpty( userEmailAddress ) ) {
            try {
                User user = userManagementService.getUserByEmail( userEmailAddress );
                userIds = new ArrayList<Long>();
                userIds.add( user.getUserId() );
                
                //if includeManagedTeam is present than get all the users under the given user 
                if(includeManagedTeam != null && includeManagedTeam == true){
                	Set<Long> agentIds = userManagementService.getUserIdsUnderAdmin(user);
                	userIds.addAll(agentIds);
                }
                
            } catch ( InvalidInputException | NoRecordsFetchedException e ) {
                return restUtils.getRestResponseEntity( HttpStatus.BAD_REQUEST, "Passed parameter user "  + userEmailAddress + " is invalid", null, null,
                    request  , companyId );
            }
        }

        //get data from database
        SurveysAndReviewsVO surveysAndReviewsVO = surveyHandler.getSurveysByFilterCriteria( status, state, startSurveyID , startReviewDate,
            startTransactionDate, userIds, start, count, companyId );

        //create vo object
        List<SurveyGetV2VO> surveyVOs = surveysAndReviewsV2VOTransformer
            .transformDomainObjectToApiResponse( surveysAndReviewsVO );
        LOGGER.info( "SurveyApiController.getSurveyTransaction completed successfully" );

        return restUtils.getRestResponseEntity( HttpStatus.OK, "Request Successfully processed", "surveys", surveyVOs,
            request  , companyId  );
    }
}
