package com.realtech.socialsurvey.web.controller;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.realtech.socialsurvey.core.commons.CommonConstants;
import com.realtech.socialsurvey.core.entities.User;
import com.realtech.socialsurvey.core.enums.DisplayMessageType;
import com.realtech.socialsurvey.core.exception.InvalidInputException;
import com.realtech.socialsurvey.core.exception.NonFatalException;
import com.realtech.socialsurvey.core.services.authentication.AuthenticationService;
import com.realtech.socialsurvey.core.services.authentication.CaptchaValidation;
import com.realtech.socialsurvey.core.utils.DisplayMessageConstants;
import com.realtech.socialsurvey.core.utils.MessageUtils;
import com.realtech.socialsurvey.web.common.JspResolver;

/**
 * Controller for testing jsp pages styles directly This is meant for UI testing
 */
@Controller
public class TestController {

	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	private MessageUtils messageUtils;
	
	@Autowired
	private CaptchaValidation captchaValidation;

	private static final Logger LOG = LoggerFactory.getLogger(TestController.class);

	@RequestMapping(value = "/testpage")
	public String testpage(HttpServletRequest request) {
		LOG.info("Method testpage called");
		return "registration";
	}
	
	@RequestMapping(value = "/jumptodashboard")
	public String jumpToDashboard(Model model, HttpServletRequest req, HttpServletResponse response) {
		LOG.info("Jumping to Dashboard with ");
		// Login with the hardcoded user id
		User user = null;
		HttpSession session = req.getSession(true);
		try {
			try {
				user = authenticationService.getUserWithLoginName("nishit@raremile.com");
				session.setAttribute(CommonConstants.USER_IN_SESSION, user);
			}
			catch (InvalidInputException e) {
				LOG.error("Invalid Input exception in fetching User. Reason " + e.getMessage(), e);
				throw new InvalidInputException(e.getMessage(), DisplayMessageConstants.USER_NOT_PRESENT, e);
			}
		}
		catch (NonFatalException e) {
			LOG.error("NonFatalException while logging in. Reason : " + e.getMessage(), e);
			model.addAttribute("message", messageUtils.getDisplayMessage(e.getErrorCode(), DisplayMessageType.ERROR_MESSAGE));
			return JspResolver.MESSAGE_HEADER;
		}
		return JspResolver.DASHBOARD;
	}

	@RequestMapping("/test")
	public String test() {

		return "test";
	}

	@RequestMapping("/Invitation")
	public String start() {

		return "invitation";
	}

	@RequestMapping("/Register_User")
	public String registerStepTwo() {

		return "registerUser";
	}

	@RequestMapping("/Company_Info")
	public String registerStepThree() {

		return "companyInfo";
	}

	@RequestMapping("/form")
	public String form() {

		return "form";
	}

	@RequestMapping("/validat")
	public String validate(@RequestParam("recaptcha_challenge_field") String challangeField,
			@RequestParam("recaptcha_response_field") String responseField, ServletRequest servletRequest) {

		String remoteAddress = servletRequest.getRemoteAddr();
		
		try {
			captchaValidation.isCaptchaValid(remoteAddress, challangeField, responseField);
		}
		catch (InvalidInputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "test";
	}
}
