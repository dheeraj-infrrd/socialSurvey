package com.realtech.socialsurvey.core.services.mail;

import com.realtech.socialsurvey.core.entities.SurveyDetails;
import com.realtech.socialsurvey.core.exception.InvalidInputException;

/**
 * Services for sending mails via application
 */
public interface EmailServices {
	/**
	 * Queues the registration invite mail
	 * 
	 * @param url
	 * @param recipientMailId
	 * @param firstname
	 * @param lastName
	 * @throws InvalidInputException
	 */
	public void queueRegistrationInviteMail(String url, String recipientMailId, String firstName, String lastName) throws InvalidInputException;

	public void sendRegistrationInviteMail(String url, String recipientMailId, String firstname, String lastName) throws InvalidInputException,
			UndeliveredEmailException;

	/**
	 * Queues the reset password email
	 * 
	 * @param url
	 * @param recipientMailId
	 * @param name
	 * @throws InvalidInputException
	 */
	public void queueResetPasswordEmail(String url, String recipientMailId, String name, String loginName) throws InvalidInputException;

	public void sendResetPasswordEmail(String url, String recipientMailId, String name, String loginName) throws InvalidInputException,
			UndeliveredEmailException;

	// JIRA SS-42 by RM05 : BOC
	/**
	 * Sends a link to new user to complete registration.
	 * 
	 * @param url
	 * @param recipientMailId
	 * @throws InvalidInputException
	 * @throws UndeliveredEmailException
	 */
	public void queueRegistrationCompletionEmail(String url, String recipientMailId, String name, String profileName, String loginName)
			throws InvalidInputException;

	public void sendRegistrationCompletionEmail(String url, String recipientMailId, String name, String profileName, String loginName)
			throws InvalidInputException, UndeliveredEmailException;

	/**
	 * Queues subscription charge unsuccessful email
	 * 
	 * @param recipientMailId
	 * @param name
	 * @param retryDays
	 * @throws InvalidInputException
	 */
	public void queueSubscriptionChargeUnsuccessfulEmail(String recipientMailId, String name, String retryDays) throws InvalidInputException;

	public void sendSubscriptionChargeUnsuccessfulEmail(String recipientMailId, String name, String retryDays) throws InvalidInputException,
			UndeliveredEmailException;

	// JIRA SS-42 by RM05 : EOC

	/**
	 * Queues the verification mail
	 * 
	 * @param url
	 * @param recipientMailId
	 * @param recipientName
	 * @throws InvalidInputException
	 */
	public void queueVerificationMail(String url, String recipientMailId, String recipientName, String profileName, String loginName)
			throws InvalidInputException;

	public void sendVerificationMail(String url, String recipientMailId, String recipientName, String profileName, String loginName)
			throws InvalidInputException, UndeliveredEmailException;

	/**
	 * Queues the email verification mail
	 * 
	 * @param url
	 * @param recipientMailId
	 * @param recipientName
	 * @throws InvalidInputException
	 */
	public void queueEmailVerificationMail(String url, String recipientMailId, String recipientName) throws InvalidInputException;

	public void sendEmailVerificationMail(String url, String recipientMailId, String recipientName) throws InvalidInputException,
			UndeliveredEmailException;

	/**
	 * Queues the retry charge email
	 * 
	 * @param recipientMailId
	 * @param displayName
	 * @param retries
	 * @throws InvalidInputException
	 */
	public void queueRetryChargeEmail(String recipientMailId, String displayName, String loginName) throws InvalidInputException;

	public void sendRetryChargeEmail(String recipientMailId, String displayName, String loginName) throws InvalidInputException,
			UndeliveredEmailException;

	/**
	 * Queues the retry exhausted mail
	 * 
	 * @param recipientMailId
	 * @param displayName
	 * @throws InvalidInputException
	 */
	public void queueRetryExhaustedEmail(String recipientMailId, String displayName, String loginName) throws InvalidInputException;

	public void sendRetryExhaustedEmail(String recipientMailId, String displayName, String loginName) throws InvalidInputException,
			UndeliveredEmailException;

	/**
	 * Queues the account disabled mail
	 * 
	 * @param recipientMailId
	 * @param displayName
	 * @throws InvalidInputException
	 */
	public void queueAccountDisabledMail(String recipientMailId, String displayName, String loginName) throws InvalidInputException;

	public void sendAccountDisabledMail(String recipientMailId, String displayName, String loginName) throws InvalidInputException,
			UndeliveredEmailException;

	/**
	 * Queues the account upgrade mail
	 * 
	 * @param recipientMailId
	 * @param displayName
	 * @throws InvalidInputException
	 */
	public void queueAccountUpgradeMail(String recipientMailId, String displayName, String loginName) throws InvalidInputException;

	public void sendAccountUpgradeMail(String recipientMailId, String displayName, String loginName) throws InvalidInputException,
			UndeliveredEmailException;

	/**
	 * Queues the survey complete mail
	 * 
	 * @param recipientMailId
	 * @param displayName
	 * @param agentName
	 * @throws InvalidInputException
	 */
	public void queueSurveyCompletionMail(String recipientMailId, String displayName, String agentName) throws InvalidInputException;

	public void sendSurveyCompletionMail(String recipientMailId, String displayName, String agentName) throws InvalidInputException,
			UndeliveredEmailException;

	/**
	 * Queues the survey reminder mail
	 * 
	 * @param recipientMailId
	 * @param displayName
	 * @param agentName
	 * @throws InvalidInputException
	 */
	public void queueSurveyReminderMail(String recipientMailId, String displayName, String agentName, String link, String agentPhone,
			String agentTitle, String companyName) throws InvalidInputException;

	public void sendDefaultSurveyReminderMail(String recipientMailId, String displayName, String agentName, String link, String agentPhone,
			String agentTitle, String companyName) throws InvalidInputException, UndeliveredEmailException;

	/**
	 * Queues the survey complete admin mail
	 * 
	 * @param recipientMailId
	 * @param displayName
	 * @param agentName
	 * @throws InvalidInputException
	 */
	public void queueSurveyCompletionMailToAdmins(String recipientMailId, String customerName, String agentName, String mood, SurveyDetails survey)
			throws InvalidInputException;

	public void sendSurveyCompletionMailToAdmins(String recipientMailId, String customerName, String agentName, String mood, SurveyDetails survey)
			throws InvalidInputException, UndeliveredEmailException;

	/**
	 * Queues the social post reminder mail
	 * 
	 * @param recipientMailId
	 * @param displayName
	 * @param agentName
	 * @throws InvalidInputException
	 */
	public void queueSocialPostReminderMail(String recipientMailId, String displayName, String agentName, String links) throws InvalidInputException;

	public void sendSocialPostReminderMail(String recipientMailId, String displayName, String agentName, String links) throws InvalidInputException,
			UndeliveredEmailException;

	/**
	 * Sends the message from the contact us page as a mail to the respective admin or agent
	 * 
	 * @param recipientEmailId
	 * @param displayName
	 * @param senderEmailId
	 * @param message
	 * @throws InvalidInputException
	 * @throws UndeliveredEmailException
	 */
	public void sendContactUsMail(String recipientEmailId, String displayName, String senderName, String senderEmailId, String message)
			throws InvalidInputException, UndeliveredEmailException;

	/**
	 * Sends survey invitation mail
	 * 
	 * @param recipientMailId
	 * @param subject
	 * @param mailBody
	 * @param firstName
	 * @param lastName
	 * @throws InvalidInputException
	 * @throws UndeliveredEmailException
	 */
	public void sendSurveyInvitationMail(String recipientMailId, String subject, String mailBody, String emailId, String name)
			throws InvalidInputException, UndeliveredEmailException;

	/**
	 * Sends account blocking mail when retries fail
	 * 
	 * @param recipientMailId
	 * @param displayName
	 * @throws UndeliveredEmailException
	 * @throws InvalidInputException
	 */
	public void sendAccountBlockingMail(String recipientMailId, String displayName, String loginName) throws InvalidInputException,
			UndeliveredEmailException;

	/**
	 * Send mail to customer when his account is reactivated
	 * 
	 * @param recipientMailId
	 * @param displayName
	 * @throws InvalidInputException
	 * @throws UndeliveredEmailException
	 */
	public void sendAccountReactivationMail(String recipientMailId, String displayName, String loginName) throws InvalidInputException,
			UndeliveredEmailException;

	/**
	 * Sends a subscription revision mail to the user
	 * 
	 * @param recipientMailId
	 * @param name
	 * @param oldAmount
	 * @param revisedAmount
	 * @param numOfUsers
	 * @throws InvalidInputException
	 * @throws UndeliveredEmailException
	 */
	public void sendSubscriptionRevisionMail(String recipientMailId, String name, String oldAmount, String revisedAmount, String numOfUsers)
			throws InvalidInputException, UndeliveredEmailException;

	/**
	 * Sends manual registration link
	 * 
	 * @param recipientId
	 * @param firstName
	 * @param lastName
	 * @param link
	 * @throws InvalidInputException
	 * @throws UndeliveredEmailException
	 */
	public void sendManualRegistrationLink(String recipientId, String firstName, String lastName, String link) throws InvalidInputException,
			UndeliveredEmailException;

	public void sendSurveyReminderMail(String recipientMailId, String subject, String mailBody) throws InvalidInputException,
			UndeliveredEmailException;

	public void sendDefaultSurveyInvitationMail(String recipientMailId, String displayName, String agentName, String link, String agentEmailId)
			throws InvalidInputException, UndeliveredEmailException;

	public void sendDefaultSurveyInvitationMailByCustomer(String recipientMailId, String displayName, String agentName, String link,
			String agentEmailId) throws InvalidInputException, UndeliveredEmailException;

	public void sendSurveyInvitationMailByCustomer(String recipientMailId, String subject, String mailBody, String emailId, String name)
			throws InvalidInputException, UndeliveredEmailException;

	public void sendFatalExceptionEmail(String recipientMailId, String stackTrace) throws InvalidInputException, UndeliveredEmailException;

	public void sendEmailSendingFailureMail(String recipientMailId, String destinationMailId, String displayName, String stackTrace)
			throws InvalidInputException, UndeliveredEmailException;
}