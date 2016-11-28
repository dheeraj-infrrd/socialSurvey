package com.realtech.socialsurvey.api.transformers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.realtech.socialsurvey.api.models.ReviewVO;
import com.realtech.socialsurvey.api.models.ServiceProviderInfo;
import com.realtech.socialsurvey.api.models.SurveyGetVO;
import com.realtech.socialsurvey.api.models.SurveyResponseVO;
import com.realtech.socialsurvey.api.models.TransactionInfoGetVO;
import com.realtech.socialsurvey.core.entities.SurveyDetails;
import com.realtech.socialsurvey.core.entities.SurveyPreInitiation;
import com.realtech.socialsurvey.core.entities.SurveyResponse;
import com.realtech.socialsurvey.core.services.organizationmanagement.UserManagementService;

@Component
public class SurveyTransformer implements Transformer<SurveyGetVO, SurveyDetails, SurveyGetVO>{

	@Autowired
	UserManagementService userManagementService; 
	
	@Override
	public SurveyDetails transformApiRequestToDomainObject(SurveyGetVO a,
			Object... objects) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SurveyGetVO transformDomainObjectToApiResponse(SurveyDetails d,
			Object... objects) {
		
		SurveyPreInitiation surveyPreInitiation;
		TransactionInfoGetVO transactionInfo = new TransactionInfoGetVO();
		ServiceProviderInfo serviceProviderInfo = new ServiceProviderInfo();
		ReviewVO review = new ReviewVO();
		SurveyGetVO survey = new SurveyGetVO();
		List<SurveyResponseVO> surveyResponses = new ArrayList<SurveyResponseVO>();
		
		if( d != null ){
			transactionInfo.setCustomerEmail(d.getCustomerEmail());
			transactionInfo.setCustomerFirstName(d.getCustomerFirstName());
			transactionInfo.setCustomerLastName(d.getCustomerLastName());
			
			transactionInfo.setTransactionCity(d.getCity());
			transactionInfo.setTransactionState(d.getState());
			if(d.getSurveyTransactionDate() != null)
				transactionInfo.setTransactionDate(String.valueOf(d.getSurveyTransactionDate()));
			transactionInfo.setTransactionRef(d.getSourceId());
			if( objects[0] != null && objects[0] instanceof SurveyPreInitiation )
			    transactionInfo.setSurveySentDate( String.valueOf( ((SurveyPreInitiation)objects[0]).getLastReminderTime()) );
										
			serviceProviderInfo.setServiceProviderEmail(d.getAgentEmailId());
			serviceProviderInfo.setServiceProviderName(d.getAgentName());
	
			survey.setReviewId(d.get_id());
			review.setSummary(d.getSummary());
			review.setDescription(d.getReview());
			review.setRating(String.valueOf(d.getScore()));
			review.setReviewDate(d.getModifiedOn().toString());
			review.setSource(d.getSource());
			review.setIsReportedAbusive(d.isAbusive());
			
			if(d.getSurveyResponse() != null){
			    for (SurveyResponse response : d.getSurveyResponse()){
			        SurveyResponseVO responseVO = new SurveyResponseVO();
			        responseVO.setSurveyResponse( response );
			        surveyResponses.add( responseVO );
			    }
			}
	         SurveyResponseVO responseVO = new SurveyResponseVO();
	         SurveyResponse gatewayQuestion = new SurveyResponse();
	         gatewayQuestion.setQuestionType( "Gateway-Question" );
	         gatewayQuestion.setQuestion( "How would you rate your overall experience?" );
	         gatewayQuestion.setAnswer( d.getMood() );
	         responseVO.setSurveyResponse( gatewayQuestion );
	         surveyResponses.add( responseVO );
			review.setSurveyResponses( surveyResponses );
			review.setReview( d.getReview() );
			//review.setIsCRMVerified(surveyPreInitiation);
			boolean isCRMVerified = false;
			if(d.getSource()!= null)
				if(d.getSource().equals("encompass") || d.getSource().equals("DOTLOOP"))
					isCRMVerified = true;
			review.setIsCRMVerified(isCRMVerified);

		}
		
		if (objects[0] != null && objects[0] instanceof SurveyPreInitiation) {
			surveyPreInitiation = (SurveyPreInitiation) objects[0];
			survey.setSurveyId(Long.valueOf(surveyPreInitiation.getSurveyPreIntitiationId()));
			if(StringUtils.isBlank( transactionInfo.getCustomerEmail()) )
				transactionInfo.setCustomerEmail(surveyPreInitiation.getCustomerEmailId());
			if(StringUtils.isBlank( transactionInfo.getCustomerFirstName()) )
				transactionInfo.setCustomerFirstName(surveyPreInitiation.getCustomerFirstName());
			if(StringUtils.isBlank( transactionInfo.getCustomerLastName()) )
				transactionInfo.setCustomerLastName(surveyPreInitiation.getCustomerLastName());

			if(StringUtils.isBlank( serviceProviderInfo.getServiceProviderEmail()) )
				serviceProviderInfo.setServiceProviderEmail(surveyPreInitiation.getAgentEmailId());
			if(StringUtils.isBlank( serviceProviderInfo.getServiceProviderName()) )
				serviceProviderInfo.setServiceProviderName(surveyPreInitiation.getAgentName());

			if(StringUtils.isBlank( transactionInfo.getTransactionCity()) )
				transactionInfo.setTransactionCity(surveyPreInitiation.getCity());
			if(StringUtils.isBlank( transactionInfo.getTransactionState()) )
				transactionInfo.setTransactionState(surveyPreInitiation.getState());
			if(StringUtils.isBlank( transactionInfo.getTransactionDate()) )
				transactionInfo.setTransactionDate(String.valueOf(surveyPreInitiation.getEngagementClosedTime()));
			if(StringUtils.isBlank( transactionInfo.getTransactionRef()) )
				transactionInfo.setTransactionRef(surveyPreInitiation.getSurveySourceId());
					
		}
				
		survey.setReview(review);
		survey.setTransactionInfo(transactionInfo);
		survey.setServiceProviderInfo(serviceProviderInfo);
		
		return survey;
	}

}
