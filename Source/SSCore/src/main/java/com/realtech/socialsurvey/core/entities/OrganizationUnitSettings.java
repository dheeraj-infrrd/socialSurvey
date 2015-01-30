package com.realtech.socialsurvey.core.entities;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Holds the company settings
 */
@Document
public class OrganizationUnitSettings {

	@Id
	private String id;
	private long iden;
	private float profile_completion;
	private String profileName;
	private String logo;
	private boolean isLocationEnabled;
	private boolean isAccountDisabled;
	private ContactDetailsSettings contact_details;
	private CRMInfo crm_info;
	private MailContentSettings mail_content;
	private Licenses licenses;
	private List<Association> associations;
	private List<Achievement> achievements;
	private SurveySettings survey_settings;
	private String createdBy;
	private String modifiedBy;
	private long createdOn;
	private long modifiedOn;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getIden() {
		return iden;
	}

	public void setIden(long iden) {
		this.iden = iden;
	}

	public float getProfile_completion() {
		return profile_completion;
	}

	public void setProfile_completion(float profile_completion) {
		this.profile_completion = profile_completion;
	}
	
	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public boolean getIsLocationEnabled() {
		return isLocationEnabled;
	}

	public void setLocationEnabled(boolean isLocationEnabled) {
		this.isLocationEnabled = isLocationEnabled;
	}

	public boolean getIsAccountDisabled() {
		return isAccountDisabled;
	}

	public void setAccountDisabled(boolean isAccountDisabled) {
		this.isAccountDisabled = isAccountDisabled;
	}

	public ContactDetailsSettings getContact_details() {
		return contact_details;
	}

	public void setContact_details(ContactDetailsSettings contact_details) {
		this.contact_details = contact_details;
	}

	public CRMInfo getCrm_info() {
		return crm_info;
	}

	public void setCrm_info(CRMInfo crm_info) {
		this.crm_info = crm_info;
	}

	public MailContentSettings getMail_content() {
		return mail_content;
	}

	public void setMail_content(MailContentSettings mail_content) {
		this.mail_content = mail_content;
	}

	public Licenses getLicenses() {
		return licenses;
	}

	public void setLicenses(Licenses licenses) {
		this.licenses = licenses;
	}

	public List<Association> getAssociations() {
		return associations;
	}

	public void setAssociations(List<Association> associations) {
		this.associations = associations;
	}

	public List<Achievement> getAchievements() {
		return achievements;
	}

	public void setAchievements(List<Achievement> achievements) {
		this.achievements = achievements;
	}

	public SurveySettings getSurvey_settings() {
		return survey_settings;
	}

	public void setSurvey_settings(SurveySettings survey_settings) {
		this.survey_settings = survey_settings;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}

	public long getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(long modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	@Override
	public String toString() {
		return "OrganizationUnitSettings [id=" + id + ", iden=" + iden + ", profile_completion=" + profile_completion + ", profileName="
				+ profileName + ", logo=" + logo + ", isLocationEnabled=" + isLocationEnabled + ", isAccountDisabled=" + isAccountDisabled
				+ ", contact_details=" + contact_details + ", crm_info=" + crm_info + ", mail_content=" + mail_content + ", licenses=" + licenses
				+ ", associations=" + associations + ", achievements=" + achievements + ", survey_settings=" + survey_settings + ", createdBy="
				+ createdBy + ", modifiedBy=" + modifiedBy + ", createdOn=" + createdOn + ", modifiedOn=" + modifiedOn + "]";
	}

}
