package com.realtech.socialsurvey.core.services.socialmonitor.feed.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.realtech.socialsurvey.core.commons.ActionHistoryComparator;
import com.realtech.socialsurvey.core.dao.MongoSocialFeedDao;
import com.realtech.socialsurvey.core.dao.impl.MongoSocialFeedDaoImpl;
import com.realtech.socialsurvey.core.entities.ActionHistory;
import com.realtech.socialsurvey.core.entities.OrganizationUnitSettings;
import com.realtech.socialsurvey.core.entities.SocialFeedsActionUpdate;
import com.realtech.socialsurvey.core.entities.SocialMonitorFeedData;
import com.realtech.socialsurvey.core.entities.SocialMonitorMacro;
import com.realtech.socialsurvey.core.entities.SocialMonitorResponseData;
import com.realtech.socialsurvey.core.entities.SocialResponseObject;
import com.realtech.socialsurvey.core.enums.ActionHistoryType;
import com.realtech.socialsurvey.core.enums.SocialFeedStatus;
import com.realtech.socialsurvey.core.enums.TextActionType;
import com.realtech.socialsurvey.core.exception.InvalidInputException;
import com.realtech.socialsurvey.core.services.mail.EmailServices;
import com.realtech.socialsurvey.core.services.mail.UndeliveredEmailException;
import com.realtech.socialsurvey.core.services.socialmonitor.feed.SocialFeedService;


/**
 * @author manish
 *
 */
@DependsOn ( "generic")
@Component
public class SocialFeedServiceImpl implements SocialFeedService
{
    private static final Logger LOG = LoggerFactory.getLogger( SocialFeedServiceImpl.class );
    @Autowired
    MongoSocialFeedDao mongoSocialFeedDao;
    
    private EmailServices emailServices;

	@Autowired
	public void setEmailServices(EmailServices emailServices) {
		this.emailServices = emailServices;
	}

	@Override
    public SocialResponseObject<?> saveFeed( SocialResponseObject<?> socialFeed ) throws InvalidInputException
    {
        LOG.info( "Inside save feed method {}" , socialFeed);
        if(socialFeed == null){
            throw new InvalidInputException( "Feed cannt be null or empy" );
        }
        mongoSocialFeedDao.insertSocialFeed( socialFeed, MongoSocialFeedDaoImpl.SOCIAL_FEED_COLLECTION );
        LOG.info( "End of save feed method" );
        return socialFeed;
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public SocialMonitorResponseData getAllSocialPosts(int startIndex, int limit, String status, boolean flag,
			List<String> feedtype, Long companyId, List<Long> regionIds, List<Long> branchIds, List<Long> agentIds)
			throws InvalidInputException {
		LOG.debug("Fetching social posts");

		SocialMonitorResponseData socialMonitorResponseData = new SocialMonitorResponseData();
		List<SocialMonitorFeedData> socialMonitorStreamDataList = new ArrayList<>();
		List<SocialResponseObject> socialResponseObjects;

		socialResponseObjects = mongoSocialFeedDao.getAllSocialFeeds(startIndex, limit, flag, status,
				feedtype, companyId, regionIds, branchIds, agentIds);
		if (socialResponseObjects != null && !socialResponseObjects.isEmpty()) {
			for (SocialResponseObject socialResponseObject : socialResponseObjects) {
				SocialMonitorFeedData socialMonitorFeedData = new SocialMonitorFeedData();
				socialMonitorFeedData.setType(socialResponseObject.getType());
				socialMonitorFeedData.setStatus(socialResponseObject.getStatus());
				socialMonitorFeedData.setText(socialResponseObject.getText());
				socialMonitorFeedData.setPicture(socialResponseObject.getPicture());
				socialMonitorFeedData.setOwnerName(socialResponseObject.getOwnerName());
				socialMonitorFeedData.setOwnerProfileImage(socialResponseObject.getOwnerProfileImage());
				socialMonitorFeedData.setCompanyId(socialResponseObject.getCompanyId());
				socialMonitorFeedData.setRegionId(socialResponseObject.getRegionId());
				socialMonitorFeedData.setBranchId(socialResponseObject.getBranchId());
				socialMonitorFeedData.setAgentId(socialResponseObject.getAgentId());
				socialMonitorFeedData.setPostId(socialResponseObject.getPostId());
				socialMonitorFeedData.setFlagged(socialResponseObject.isFlagged());
				Collections.sort( socialResponseObject.getActionHistory(), new ActionHistoryComparator() );	
				socialMonitorFeedData.setActionHistory(socialResponseObject.getActionHistory());
				socialMonitorFeedData.setUpdatedOn(socialResponseObject.getUpdatedTime());
				socialMonitorFeedData.setFoundKeywords(socialResponseObject.getFoundKeywords());
				socialMonitorFeedData.setDuplicateCount(socialResponseObject.getDuplicateCount());
				socialMonitorStreamDataList.add(socialMonitorFeedData);
			}
			socialMonitorResponseData.setCount(mongoSocialFeedDao.getAllSocialFeedsCount(flag, status, feedtype, companyId, regionIds, branchIds, agentIds));
			if (flag) {
				socialMonitorResponseData.setStatus("FLAGGED");
			} else if (status != null && !flag) {
				socialMonitorResponseData.setStatus(status.toUpperCase());
			} else if(status == null && !flag){
				socialMonitorResponseData.setStatus("ALL");
			}
			socialMonitorResponseData.setSocialMonitorFeedData(socialMonitorStreamDataList);
		} else {
			LOG.warn("List is empty");
		}
		LOG.debug("End of getSocialPostsForStream{}");
		return socialMonitorResponseData;
	}
	
	@Override
	public void updateActionForFeeds(SocialFeedsActionUpdate socialFeedsActionUpdate, Long companyId)
			throws InvalidInputException {
		LOG.debug("Updating social Feeds for social monitor");
		if (socialFeedsActionUpdate == null) {
			LOG.error("No action passed");
			throw new InvalidInputException("No action passed");
		}
		int updateFlag = 0;
		// Check if a macro is applied
		if (!socialFeedsActionUpdate.getMacroId().isEmpty()) {
			OrganizationUnitSettings organizationUnitSettings = mongoSocialFeedDao.FetchMacros(companyId);
			for (SocialMonitorMacro macro : organizationUnitSettings.getSocialMonitorMacros()) {
				if (macro.getMacroId().equalsIgnoreCase(socialFeedsActionUpdate.getMacroId())) {
					macro.setCount(macro.getCount() + socialFeedsActionUpdate.getPostIds().size());
					mongoSocialFeedDao.updateMacroCount(organizationUnitSettings.getSocialMonitorMacros(), companyId);
					break;
				}
			}
		}
		List<ActionHistory> actionHistories = new ArrayList<>();
		for (String postId : socialFeedsActionUpdate.getPostIds()) {
			updateFlag = 0;
			SocialResponseObject socialResponseObject = mongoSocialFeedDao.getSocialFeed(postId,
					MongoSocialFeedDaoImpl.SOCIAL_FEED_COLLECTION);
			if (socialResponseObject.isFlagged() != socialFeedsActionUpdate.isFlagged()) {
				ActionHistory actionHistory = new ActionHistory();
				if (socialFeedsActionUpdate.isFlagged() && (socialResponseObject.getStatus().equals(SocialFeedStatus.NEW))) {
					updateFlag = 1;
					actionHistory.setActionType(ActionHistoryType.FLAGGED);
					actionHistory.setText("Post was FLAGGED manually by " + socialFeedsActionUpdate.getUserName());
					actionHistory.setOwnerName(socialFeedsActionUpdate.getUserName());
					actionHistory.setCreatedDate(new Date().getTime());
					actionHistories.add(actionHistory);
				} else if(!socialFeedsActionUpdate.isFlagged() && (socialResponseObject.getStatus().equals(SocialFeedStatus.NEW))){
					updateFlag = 1;
					actionHistory.setActionType(ActionHistoryType.UNFLAGGED);
					actionHistory.setText("Post was UNFLAGGED by " + socialFeedsActionUpdate.getUserName());
					actionHistory.setOwnerName(socialFeedsActionUpdate.getUserName());
					actionHistory.setCreatedDate(new Date().getTime());
					actionHistories.add(actionHistory);
				}
			} if (!socialFeedsActionUpdate.getStatus().toString().equalsIgnoreCase(socialResponseObject.getStatus().toString())
					&& socialFeedsActionUpdate.getStatus() != null) {
				updateFlag = 2;
				ActionHistory actionHistory = new ActionHistory();
				if(socialFeedsActionUpdate.getStatus().toString().equalsIgnoreCase(SocialFeedStatus.ESCALATED.toString())) {
					actionHistory.setActionType(ActionHistoryType.ESCALATE);
				} else {
					actionHistory.setActionType(ActionHistoryType.RESOLVED);
				}
				actionHistory.setText("Post was " + socialFeedsActionUpdate.getStatus() + " by "
						+ socialFeedsActionUpdate.getUserName());
				actionHistory.setOwnerName(socialFeedsActionUpdate.getUserName());
				actionHistory.setCreatedDate(new Date().getTime());
				actionHistories.add(actionHistory);
			}
			if ((socialFeedsActionUpdate.getTextActionType().toString()
					.equalsIgnoreCase(TextActionType.PRIVATE_NOTE.toString()))
					&& (socialFeedsActionUpdate.getText() != null) && !(socialFeedsActionUpdate.getText().isEmpty())) {
				ActionHistory actionHistory = new ActionHistory();
				actionHistory.setActionType(ActionHistoryType.PRIVATE_MESSAGE);
				actionHistory.setText(socialFeedsActionUpdate.getText());
				actionHistory.setOwnerName(socialFeedsActionUpdate.getUserName());
				actionHistory.setCreatedDate(new Date().getTime());
				actionHistories.add(actionHistory);
			} if ((socialFeedsActionUpdate.getTextActionType().toString()
					.equalsIgnoreCase(TextActionType.SEND_EMAIL.toString()))
					&& (socialFeedsActionUpdate.getText() != null) && !(socialFeedsActionUpdate.getText().isEmpty())) {
				ActionHistory actionHistory = new ActionHistory();
				actionHistory.setActionType(ActionHistoryType.EMAIL);
				actionHistory.setText(socialFeedsActionUpdate.getText());
				actionHistory.setOwnerName(socialFeedsActionUpdate.getUserName());
				actionHistory.setCreatedDate(new Date().getTime());
				actionHistories.add(actionHistory);
				// send mail to the user
				try {
					emailServices.sendSocialMonitorActionMail(socialResponseObject.getOwnerEmail(),
							socialResponseObject.getOwnerName(), socialFeedsActionUpdate.getText());
				} catch (UndeliveredEmailException e) {
					LOG.error("Email could not be delivered", e);
				}
			}
		}

		mongoSocialFeedDao.updateSocialFeed(socialFeedsActionUpdate, actionHistories, updateFlag, 
				MongoSocialFeedDaoImpl.SOCIAL_FEED_COLLECTION);
		LOG.debug("End of saveSocialPostsForStream{}");

	}

    @Override
    public long updateDuplicateCount(int hash, long companyId) throws InvalidInputException {
        LOG.info("Executing updateDuplicateCount method with hash = {}, companyId = {}", hash, companyId);
        if( hash  == 0 || companyId <= 0){
            throw new InvalidInputException( "companyId cannot be <= 0 or hash cannot be 0" );
        }
        return mongoSocialFeedDao.updateDuplicateCount(hash, companyId);
    }

	@Override
	public List<SocialMonitorMacro> getMacros( long companyId ) throws InvalidInputException {
		LOG.debug( "Fetching all Macros for company with Id {} " , companyId);
        if(companyId <= 0){
        	LOG.error("Invalid companyId");
            throw new InvalidInputException( "Invalid companyId" );
        }
        List<SocialMonitorMacro> macros = new ArrayList<>();
        OrganizationUnitSettings organizationUnitSettings = mongoSocialFeedDao.FetchMacros(companyId);
        if(organizationUnitSettings != null && organizationUnitSettings.getSocialMonitorMacros() != null && !organizationUnitSettings.getSocialMonitorMacros().isEmpty()) {
        	macros = organizationUnitSettings.getSocialMonitorMacros();
        } else {
        	LOG.warn("The List is empty");
        }
        return macros;
        
		
	}

	@Override
	public void updateMacrosForFeeds(SocialMonitorMacro socialMonitorMacro, long companyId)
			throws InvalidInputException {
		LOG.debug("Updating macros for social monitor for company with id {}", companyId);
		SocialMonitorMacro macro = new SocialMonitorMacro();
		if (socialMonitorMacro == null || companyId <= 0) {
			LOG.error("Invalid parameters passed");
			throw new InvalidInputException("Invalid parameters passed");
		}
		if (socialMonitorMacro.getMacroId() == null || socialMonitorMacro.getMacroId().isEmpty()) {
			socialMonitorMacro.setCount(0);
			socialMonitorMacro.setMacroId(UUID.randomUUID().toString());
			socialMonitorMacro.setCreatedOn(new Date().getTime());
			socialMonitorMacro.setModifiedOn(new Date().getTime());
			mongoSocialFeedDao.updateMacros(socialMonitorMacro, companyId);

		} else {
			macro = getMacroById(socialMonitorMacro.getMacroId(), companyId);
			OrganizationUnitSettings organizationUnitSettings = mongoSocialFeedDao.FetchMacros(companyId);
			organizationUnitSettings.getSocialMonitorMacros().remove(macro);
			socialMonitorMacro.setModifiedOn(new Date().getTime());
			organizationUnitSettings.getSocialMonitorMacros().add(socialMonitorMacro);
			mongoSocialFeedDao.updateMacroCount(organizationUnitSettings.getSocialMonitorMacros(), companyId);

		}

	}

	@Override
	public SocialMonitorMacro getMacroById(String macroId, Long companyId) throws InvalidInputException {
		LOG.debug("Fetching Macro with Id {} and companyId {}", macroId, companyId);
		if (macroId == null || macroId.isEmpty() || companyId <= 0 || companyId == null) {
			LOG.error("Invalid input parameters");
			throw new InvalidInputException("Invalid input parameters");
		}
		SocialMonitorMacro socialMonitorMacro = null;
		OrganizationUnitSettings organizationUnitSettings = mongoSocialFeedDao.FetchMacros(companyId);
		if (organizationUnitSettings != null && organizationUnitSettings.getSocialMonitorMacros() != null
				&& !organizationUnitSettings.getSocialMonitorMacros().isEmpty()) {
			for (SocialMonitorMacro macro : organizationUnitSettings.getSocialMonitorMacros()) {
				if (macro.getMacroId().equalsIgnoreCase(macroId)) {
					socialMonitorMacro = macro;
				}
			}
		} else {
			LOG.warn("The List is empty");
		}
		return socialMonitorMacro;

	}


} 

