// Variables for processing
var startIndex = 0;
var numOfRows = 3;
var minScore = 0;
var attrName = null;
var attrVal = null;
var webAddressRegEx = /[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)/;
var timer = 0;
var profileId;
var delay = (function() {
	return function(callback, ms) {
		clearTimeout(timer);
		timer = setTimeout(callback, ms);
	};
})();

// Toggle text editor
$(document).on('focus', '.prof-edditable', function() {
	var lockId = $(this).attr("id") + "-lock";
	if ($('#' + lockId).attr('data-control') == 'user' || ($('#' + lockId).attr('data-state') == 'unlocked' && !$(this).is('[readonly]'))) {
		$(this).addClass('prof-name-edit');
		$('#prof-all-lock').val('modified');
	}
});

$(document).on('blur', '.prof-edditable', function() {
	var lockId = $(this).attr("id") + "-lock";
	if ($('#' + lockId).attr('data-control') == 'user' || ($('#' + lockId).attr('data-state') == 'unlocked' && !$(this).is('[readonly]'))) {
		$(this).removeClass('prof-name-edit');
	}
});

$(document).on('focus', '.prof-edditable-sin', function() {
	var lockId = $(this).attr("id") + "-lock";
	if ($('#' + lockId).attr('data-control') == 'user' || ($('#' + lockId).attr('data-state') == 'unlocked' && !$(this).is('[readonly]'))) {
		$(this).addClass('prof-name-edit');
		$('#prof-all-lock').val('modified');
	}
});

$(document).on('blur', '.prof-edditable-sin', function() {
	var lockId = $(this).attr("id") + "-lock";
	if ($('#' + lockId).attr('data-control') == 'user' || ($('#' + lockId).attr('data-state') == 'unlocked' && !$(this).is('[readonly]'))) {
		$(this).removeClass('prof-name-edit');
	}
});

// On hover for lock icons
$(document).on('mouseover', '#prof-logo-container', function(e) {
	$(this).find('.prof-img-lock:first').show();
});
$(document).on('mouseout', '#prof-logo-container', function(e) {
	$(this).find('.prof-img-lock:first').hide();
});

$(document).on('mouseover', '#prof-name-container', function(e) {
	$(this).find('.lp-edit-locks:first').show();
});
$(document).on('mouseout', '#prof-name-container', function(e) {
	$(this).find('.lp-edit-locks:first').hide();
});

$(document).on('mouseover', '.lp-con-row', function(e) {
	$(this).find('.lp-edit-locks:first').show();
});
$(document).on('mouseout', '.lp-con-row', function(e) {
	$(this).find('.lp-edit-locks:first').hide();
});


// Lock Settings
$(document).on('click', '.lp-edit-locks', function(e) {
	e.stopImmediatePropagation();
	var lockId = $(this).attr("id");

	if ($(this).attr('data-control') == 'user') {
		if($(this).hasClass('lp-edit-locks-locked')) {
			$(this).removeClass('lp-edit-locks-locked');
			$(this).attr('data-state', 'unlocked');
			updateLockSettings(lockId, false);
			
		} else {
			$(this).addClass('lp-edit-locks-locked');
			$(this).attr('data-state', 'locked');
			updateLockSettings(lockId, true);
		}
	} else {
		$('#overlay-toast').html("Settings locked by Admin");
		showToast();
	}
});

$(document).on('click', '.prof-img-lock-item', function(e) {
	e.stopImmediatePropagation();
	var lockId = $(this).attr("id");

	if ($(this).attr('data-control') == 'user') {
		if($(this).hasClass('prof-img-lock-locked')) {
			$(this).removeClass('prof-img-lock-locked');
			$(this).attr('data-state', 'unlocked');
			updateLockSettings(lockId, false);

		} else {
			$(this).addClass('prof-img-lock-locked');
			$(this).attr('data-state', 'locked');
			updateLockSettings(lockId, true);
		}
	} else {
		$('#overlay-toast').html("Settings locked by Admin");
		showToast();
	}
});

function updateLockSettings(id, state) {
	if (id == undefined || id == "") {
		return;
	}
	
	delay(function() {
		var payload = {
			"id" : id,
			"state" : state
		};
		callAjaxPostWithPayloadData("./updatelocksettings.do", callBackUpdateLock, payload);
	}, 0);
}

function callBackUpdateLock () {
	showMainContent('./showprofilepage.do');
}


// Update AboutMe details
function callBackShowAboutMe(data) {
	$('#intro-about-me').html(data);
	adjustImage();
}

$(document).on('click', '#intro-body-text', function() {
	if ($('#aboutme-lock').attr('data-state') == 'unlocked') {
		$(this).hide();
		var textContent = $(this).text().trim();
		if ($('#aboutme-status').val() == 'new') {
			textContent = "";
		}
		$('#intro-body-text-edit').val(textContent);
		$('#intro-body-text-edit').show();
		$('#intro-body-text-edit').focus();
	}
});

$(document).on('blur', '#intro-body-text-edit', function() {
	if ($('#aboutme-lock').attr('data-state') == 'unlocked') {
		
		var aboutMe = $('#intro-body-text-edit').val().trim();
		if (aboutMe == undefined || aboutMe == "") {
			$('#overlay-toast').html("Please add a few words about you");
			showToast();
			return;
		}
		delay(function() {
			var payload = {
				"aboutMe" : aboutMe
			};
			callAjaxPostWithPayloadData("./addorupdateaboutme.do", callBackOnEditAdboutMeDetails, payload);
		}, 0);
	}
});

function callBackOnEditAdboutMeDetails(data) {
	$('#prof-message-header').html(data);
	if ($('#prof-message-header #display-msg-div').hasClass('success-message')) {
		$('#intro-body-text-edit').hide();
		var textContent = $('#intro-body-text-edit').val().trim();
		$('#intro-body-text').text(textContent);
		$('#intro-body-text').show();
	}
	else {
		$('#intro-body-text-edit').hide();
		$('#intro-body-text').show();
	}

	if ($('#aboutme-status').val() == 'new') {
		callAjaxGET("./fetchaboutme.do", function(data) {
			$('#intro-about-me').html(data);
		}, true);
	}
	
	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();
}


// Update Contact details
function callBackShowContactDetails(data) {
	$('#contant-info-container').html(data);
	adjustImage();
}

// Phone numbers in contact details
$(document).on('blur', '#contant-info-container input[data-phone-number]', function() {
	if ($('#prof-all-lock').val() != 'modified' || !$(this).val() || $(this).is('[readonly]')) {
		return;
	}
	if (!phoneRegex.test(this.value)) {
		$('#overlay-toast').html("Please add a valid phone number");
		showToast();
		return;
	}

	delay(function() {
		var phoneNumbers = [];
		$('#contant-info-container input[data-phone-number]').each(function() {
			if (this.value != "" && phoneRegex.test(this.value) && !$(this).is('[readonly]')) {
				var phoneNumber = {};
				phoneNumber.key = $(this).attr("data-phone-number");
				phoneNumber.value = this.value;
				phoneNumbers.push(phoneNumber);
			}
		});
		phoneNumbers = JSON.stringify(phoneNumbers);
		var payload = {
			"phoneNumbers" : phoneNumbers
		};
		callAjaxPostWithPayloadData("./updatephonenumbers.do", callBackOnUpdatePhoneNumbers, payload);
	}, 0);
});

function callBackOnUpdatePhoneNumbers(data) {
	$('#prof-all-lock').val('locked');
	$('#prof-message-header').html(data);
	callAjaxGET("./fetchcontactdetails.do", callBackShowContactDetails);

	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();
}

// Function to update web addresses in contact details
$(document).on('blur', '#contant-info-container input[data-web-address]', function() {
	if ($('#prof-all-lock').val() != 'modified' || !$(this).val() || $(this).is('[readonly]')) {
		return;
	}
	if (!isValidUrl($(this).val().trim())) {
		$('#overlay-toast').html("Please add a valid web address");
		showToast();
		return;
	}

	delay(function() {
		var webAddresses = [];
		var i = 0;
		var webAddressValid = true;
		$('#contant-info-container input[data-web-address]').each(function() {
			var link = $.trim(this.value);
			if (link != "") {
				if (isValidUrl(link) && !$(this).is('[readonly]')) {
					var webAddress = {};
					webAddress.key = $(this).attr("data-web-address");
					webAddress.value = link;
					webAddresses[i++] = webAddress;
				} else {
					return;
					$(this).focus();
					webAddressValid = false;
				}
			}
		});
		if (!webAddressValid) {
			alert("Invalid web address");
			return false;
		}
		webAddresses = JSON.stringify(webAddresses);
		var payload = {
			"webAddresses" : webAddresses
		};
		callAjaxPostWithPayloadData("./updatewebaddresses.do", callBackOnUpdateWebAddresses, payload);
	}, 0);
});

function callBackOnUpdateWebAddresses(data) {
	$('#prof-all-lock').val('locked');
	$('#prof-message-header').html(data);
	callAjaxGET("./fetchcontactdetails.do", callBackShowContactDetails);

	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();
}

// Update Address detail
function callBackShowAddressDetails(data) {
	$('#prof-address-container').html(data);
	adjustImage();
}

$(document).on('click', '#prof-address-container', function() {
	callAjaxGET("./fetchaddressdetailsedit.do", callBackEditAddressDetails);
});

function callBackEditAddressDetails(data) {
	var header = "Edit Address Detail";
	createPopupConfirm(header, data);
	
	$('#overlay-continue').click(function() {
		var profName = $('#prof-name').val();
		var profAddress1 = $('#prof-address1').val();
		var profAddress2 = $('#prof-address2').val();
		var country = $('#prof-country').val();
		var zipCode = $('#prof-zipcode').val();
		if (!profName || !profAddress1 || !country || !zipCode) {
			return;
		}
		
		delay(function() {
			var payload = {
				"profName" : profName,
				"address1" : profAddress1,
				"address2" : profAddress2,
				"country" : country,
				"zipCode" : zipCode
			};
			callAjaxPostWithPayloadData("./updateprofileaddress.do", callBackUpdateAddressDetails, payload);
		}, 0);

		$('#overlay-continue').unbind('click');
	});

	$('.overlay-disable-wrapper').addClass('pu_arrow_rt');
	$('body').css('overflow','hidden');
	$('body').scrollTop('0');
}

function callBackUpdateAddressDetails(data) {
	$('#prof-message-header').html(data);
	callAjaxGET("./fetchbasicdetails.do", callBackShowBasicDetails);
	callAjaxGET("./fetchaddressdetails.do", callBackShowAddressDetails);

	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();

	overlayRevert();
}

$('#overlay-cancel').click(function(){
	$('#overlay-continue').unbind('click');
	overlayRevert();
});

function createPopupConfirm(header, body) {
	$('#overlay-header').html(header);
	$('#overlay-text').html(body);
	$('#overlay-continue').html("Ok");
	$('#overlay-cancel').html("Cancel");

	$('#overlay-main').show();
}
function overlayRevert() {
	$('#overlay-main').hide();
	if ($('#overlay-continue').attr("disabled") == "disabled") {
		$('#overlay-continue').removeAttr("disabled");
	}
	$("#overlay-header").html('');
	$("#overlay-text").html('');
	$('#overlay-continue').html('');
	$('#overlay-cancel').html('');
	
	$('#overlay-continue').unbind('click');

	$('body').css('overflow','auto');
	$('.overlay-disable-wrapper').removeClass('pu_arrow_rt');
}


// Update Basic detail
function callBackShowBasicDetails(response) {
	$('#prof-basic-container').html(response);
	adjustImage();
	fetchAvgRating(attrName, attrVal);
	fetchReviewCount(attrName, attrVal, minScore);
}

$(document).on('blur', '#prof-basic-container input', function() {
	if ($('#prof-all-lock').val() != 'modified' || !$(this).val()) {
		return;
	}

	delay(function() {
		var profName = $('#prof-name').val().trim();
		var profTitle = $('#prof-title').val().trim();
		var payload = {
			"profName" : profName,
			"profTitle" : profTitle
		};
		callAjaxPostWithPayloadData("./updatebasicprofile.do", callBackUpdateBasicDetails, payload);
	}, 0);
});

function callBackUpdateBasicDetails(data) {
	$('#prof-all-lock').val('locked');
	$('#prof-message-header').html(data);
	callAjaxGET("./fetchbasicdetails.do", callBackShowBasicDetails);
	callAjaxGET("./fetchaddressdetails.do", callBackShowAddressDetails);

	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();
}


// Function to update profile logo image
function callBackShowProfileLogo(data) {
	$('#prof-logo-container').html(data);
	var logoImageUrl = $('#prof-logo-edit').css("background-image");
	if (logoImageUrl == undefined || logoImageUrl == "none") {
		return;
	}
	if ($('#header-user-info').find('.user-info-logo').length <= 0) {
		var userInfoDivider = $('<div>').attr({
			"class" : "float-left user-info-seperator"
		});
		var userInfoLogo = $('<div>').attr({
			"class" : "float-left user-info-logo"
		}).css({
			"background" : logoImageUrl + " no-repeat center",
			"background-size" : "100% auto"
		});
		$('#header-user-info').append(userInfoDivider).append(userInfoLogo);
	} else {
		$('.user-info-logo').css("background-image", logoImageUrl);
	}
	adjustImage();
	hideOverlay();
}

$(document).on('change', '#prof-logo', function() {
	showOverlay();
	
	var formData = new FormData();
	formData.append("logo", $(this).prop("files")[0]);
	formData.append("logoFileName", $(this).prop("files")[0].name);
	
	delay(function() {
		callAjaxPOSTWithTextData("./updatelogo.do", function(data) {
			$('#prof-message-header').html(data);
			callAjaxGET("./fetchprofilelogo.do", callBackShowProfileLogo);
			
			$('#overlay-toast').html($('#display-msg-div').text().trim());
			showToast();
		}, false, formData);
	}, 1000);
});

var imageMaxWidth = 470;
var ratio;

// Function to crop and upload profile image
$(document).on('change', '#prof-image', function() {
	initiateJcrop(this);
});

function initiateJcrop(input) {
	if (input.files && input.files[0]) {
		createPopupCanvas();

		var reader = new FileReader();
		reader.onload = function(e) {
			$('#target').attr('src', e.target.result);
			if (typeof ratio === 'undefined') {
				ratio = $('#target').width() / imageMaxWidth;
			}
			$('#target').removeClass('hide');
			$('#target').width(imageMaxWidth);
			
			$('#target').Jcrop({
				aspectRatio : 1,
				setSelect: [ 200, 100, 200, 200 ],
				onSelect: updatePreview,
				onChange: updatePreview
			});
		};
		reader.readAsDataURL(input.files[0]);

		$('#overlay-continue').click(function() {
			showOverlay();
			var dataurl = canvas.toDataURL("image/png");
			overlayRevert();

			var formData = new FormData();
			formData.append("imageBase64", dataurl);
			formData.append("imageFileName", $('#prof-image').prop("files")[0].name);
			
			delay(function() {
				callAjaxPOSTWithTextData("./updateprofileimage.do", callBackOnProfileImageUpload, false, formData);
			}, 1000);
		});
	}
}

function createPopupCanvas() {
	var canvas = '<img src="" id="target" class="hide" style="position:absoulte;"/>'
		+ '<canvas id="canvas" width="200" height="200" style="overflow:hidden; position:absoulte; display:none;"></canvas>';
	$('#overlay-header').html("Edit image");
	$('#overlay-text').html(canvas).css('position', 'relative');
	$('#overlay-continue').html("Upload");
	$('#overlay-cancel').html("Cancel");

	$('#overlay-main').show();
}

function updatePreview(c) {
	if (parseInt(c.w) > 0) {
		var imageObj = $("#target")[0];
		var canvas = $("#canvas")[0];
		var context = canvas.getContext("2d");
		context.drawImage(imageObj, (c.x)*ratio, (c.y)*ratio, (c.w)*ratio, (c.h)*ratio, 0, 0, canvas.width, canvas.height);
	}
}

function callBackOnProfileImageUpload(data) {
	$('#prof-message-header').html(data);
	
	callAjaxGET("./fetchprofileimage.do", function(data) {
		$('#prof-img-container').html(data);
		var profileImageUrl = $('#prof-image-edit').css("background-image");
		if (profileImageUrl == undefined || profileImageUrl == "none") {
			return;
		}
		adjustImage();
		hideOverlay();
	});
	
	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();
	loadDisplayPicture();
}

// Function to show social media links
function showProfileSocialLinks() {
	$('#social-token-text').hide();
	callAjaxGET("./fetchprofilesociallinks.do", callBackShowProfileSocialLinks);
}

function callBackShowProfileSocialLinks(data) {
	$('#prof-edit-social-link').html(data);
	adjustImage();
}

// Agent details
$(document).on('focus', '.prof-edditable-sin-agent', function() {
	$(this).addClass('prof-name-edit');
});

$(document).on('input', '.prof-edditable-sin-agent', function() {
	$(this).attr('data-status', 'edited');
});

$(document).on('blur', '.prof-edditable-sin-agent', function() {
	$(this).removeClass('prof-name-edit');
});

$(document).on('mouseover', '.lp-dummy-row', function() {
	$(this).children().last().removeClass('hide');
});
$(document).on('mouseout', '.lp-dummy-row', function() {
	$(this).children().last().addClass('hide');
});

// remove agent details
$(document).on('click', '.lp-ach-item-img', function(e) {
	e.stopPropagation();
	$(this).prev().attr('data-status', 'removed');

	var type = $(this).data('type');
	$(this).parent().hide();

	if (type == 'association') {
		updateAssociations();
	}
	else if (type == 'achievement') {
		updateAchievements();
	}
	else if (type == 'license') {
		updateLicenseAuthorizations();
	}
});

// Function to update association/membership list
function addAnAssociation() {
	if ($('#association-container > div').length <= 0) {
		$('#association-container').empty();
	}

	var newDiv = $('<div>').attr({
		"class" : "lp-dummy-row clearfix"
	});
	$('#association-container').append(newDiv);
	
	var newAssociation = $('<input>').attr({
		"class" : "lp-assoc-row lp-row clearfix prof-edditable-sin-agent",
		"placeholder" : "New Associaion",
		"data-status" : "new"
	});
	var newAssociationButton = $('<div>').attr({
		"class" : "lp-ach-item-img hide",
		"data-type" : "association"
	});

	$('#association-container').children().last().append(newAssociation);
	$('#association-container').children().last().append(newAssociationButton);
	newAssociation.focus();
	newAssociationButton.addClass('float-right');
}

$(document).on('blur', '#association-container input', function(e) {
	e.stopPropagation();
	delay(function() {
		updateAssociations();
	}, 0);
});

function updateAssociations() {
	var associationList = [];
	var statusEdited = false;
	
	$('#association-container').children().each(function() {
		var status = $(this).children().first().attr('data-status');
		var value = $(this).children().first().val();
		console.log(status);
		console.log(value);
		if (value != "" && status != 'removed') {
			var association = {};
			association.name = value;
			associationList.push(association);

			statusEdited = true;
		} else if (status == 'removed') {
			statusEdited = true;
		}
	});
	if (!statusEdited) {
		return;
	}

	associationList = JSON.stringify(associationList);
	var payload = {
		"associationList" : associationList
	};
	callAjaxPostWithPayloadData("./updateassociations.do", callBackUpdateAssociations, payload);
}

function callBackUpdateAssociations(data) {
	$('#prof-message-header').html(data);
	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();
	
	if (! $('#association-container').find('input').length) { 
		$('#association-container').append('<span>No association added yet</span>');
	}
}

// Function to update achievement list
function addAnAchievement() {
	if ($('#achievement-container > div').length <= 0) {
		$('#achievement-container').empty();
	}
	
	var newDiv = $('<div>').attr({
		"class" : "lp-dummy-row clearfix"
	});
	$('#achievement-container').append(newDiv);

	var newAchievement = $('<input>').attr({
		"class" : "lp-ach-row lp-row clearfix prof-edditable-sin-agent",
		"placeholder" : "New Achievement",
		"data-status" : "new"
	});
	var newAchievementButton = $('<div>').attr({
		"class" : "lp-ach-item-img hide",
		"data-type" : "achievement"
	});

	$('#achievement-container').children().last().append(newAchievement);
	$('#achievement-container').children().last().append(newAchievementButton);
	newAchievement.focus();
	newAchievementButton.addClass('float-right');
}

$(document).on('blur', '#achievement-container input', function(e) {
	e.stopPropagation();
	delay(function() {
		updateAchievements();
	}, 0);
});

function updateAchievements() {
	var achievementList = [];
	var statusEdited = false;

	$('#achievement-container').children().each(function() {
		var status = $(this).children().first().attr('data-status');
		var value = $(this).children().first().val();
		if (value != "" && status != 'removed') {
			var achievement = {};
			achievement.achievement = value;
			achievementList.push(achievement);

			statusEdited = true;
		} else if (status == 'removed') {
			statusEdited = true;
		}
	});
	if (!statusEdited) {
		return;
	}

	achievementList = JSON.stringify(achievementList);
	var payload = {
		"achievementList" : achievementList
	};
	callAjaxPostWithPayloadData("./updateachievements.do", callBackUpdateAchievements, payload);
}

function callBackUpdateAchievements(data) {
	$('#prof-message-header').html(data);
	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();

	if (! $('#achievement-container').find('input').length) { 
		$('#achievement-container').append('<span>No achievement added yet</span>');
	}
}

// Function to update License authorizations
function addAuthorisedIn() {
	if ($('#authorised-in-container > div').length <= 0) {
		$('#authorised-in-container').empty();
	}
	
	var newDiv = $('<div>').attr({
		"class" : "lp-dummy-row clearfix"
	});
	$('#authorised-in-container').append(newDiv);

	var newAuthorisation = $('<input>').attr({
		"class" : "lp-auth-row lp-row clearfix prof-edditable-sin-agent",
		"placeholder" : "Authorized in",
		"data-status" : "new"
	});
	var newAuthorizationButton = $('<div>').attr({
		"class" : "lp-ach-item-img hide",
		"data-type" : "license"
	});

	$('#authorised-in-container').children().last().append(newAuthorisation);
	$('#authorised-in-container').children().last().append(newAuthorizationButton);
	newAuthorisation.focus();
	newAuthorizationButton.addClass('float-right');
}

$(document).on('blur', '#authorised-in-container input', function(e) {
	e.stopPropagation();
	delay(function() {
		updateLicenseAuthorizations();
	}, 0);
});

function updateLicenseAuthorizations() {
	var licenceList = [];
	var statusEdited = false;

	$('#authorised-in-container').children().each(function() {
		var status = $(this).children().first().attr('data-status');
		var value = $(this).children().first().val();
		if (value != "" && status != 'removed') {
			var licence = value;
			licenceList.push(licence);
			
			statusEdited = true;
		} else if (status == 'removed') {
			statusEdited = true;
		}
	});
	if (!statusEdited) {
		return;
	}

	licenceList = JSON.stringify(licenceList);
	var payload = {
		"licenceList" : licenceList
	};
	callAjaxPostWithPayloadData("./updatelicenses.do", callBackUpdateLicenseAuthorizations, payload);
}

function callBackUpdateLicenseAuthorizations(data) {
	$('#prof-message-header').html(data);
	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();

	if (! $('#authorised-in-container').find('input').length) { 
		$('#authorised-in-container').append('<span>No license added yet</span>');
	}
}


// Update Social links
/*$('body').on('click','#prof-edit-social-link .icn-fb',function(){
	$('#social-token-text').show();
	var link = $(this).attr("data-link");
	$('#social-token-text').attr({
		"placeholder" : "Add facebook link",
		"value" : link,
		"onblur" : "updateFacebookLink(this.value);$('#social-token-text').hide();"
	});
});

$('body').on('click','#prof-edit-social-link .icn-twit',function(){
	$('#social-token-text').show();
	var link = $(this).attr("data-link");
	$('#social-token-text').attr({
		"placeholder" : "Add Twitter link",
		"value" : link,
		"onblur" : "updateTwitterLink(this.value);$('#social-token-text').hide();"
	});
});

$('body').on('click','#prof-edit-social-link .icn-lin',function(){
	$('#social-token-text').show();
	var link = $(this).attr("data-link");
	$('#social-token-text').attr({
		"placeholder" : "Add LinkedIn link",
		"value" : link,
		"onblur" : "updateLinkedInLink(this.value);$('#social-token-text').hide();"
	});
});

$('body').on('click','#prof-edit-social-link .icn-yelp',function(){
	$('#social-token-text').show();
	var link = $(this).attr("data-link");
	$('#social-token-text').attr({
		"placeholder" : "Add Yelp link",
		"value" : link,
		"onblur" : "updateYelpLink(this.value);$('#social-token-text').hide();"
	});
});

function updateFacebookLink(link) {
	var payload = {
		"fblink" : link	
	};
	if(isValidUrl(link)){
        callAjaxPostWithPayloadData("./updatefacebooklink.do", callBackUpdateFacebookLink, payload);
	}else{
		$('#overlay-toast').html("Enter a valid url");
		showToast();
	}
}

function callBackUpdateFacebookLink(data) {
	$('#prof-message-header').html(data);
	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();

	showProfileSocialLinks();
}

function updateTwitterLink(link) {
	var payload = {
		"twitterlink" : link	
	};
	if(isValidUrl(link)){
        	callAjaxPostWithPayloadData("./updatetwitterlink.do", callBackUpdateTwitterLink, payload);
	}else{
		$('#overlay-toast').html("Enter a valid url");
		showToast();
	}
}

function callBackUpdateTwitterLink(data) {
	$('#prof-message-header').html(data);
	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();

	showProfileSocialLinks();
}

function updateLinkedInLink(link) {
	var payload = {
		"linkedinlink" : link	
	};
	if(isValidUrl(link)){
		callAjaxPostWithPayloadData("./updatelinkedinlink.do", callBackUpdateLinkedInLink, payload);
	}else{
		$('#overlay-toast').html("Enter a valid url");
		showToast();
	}
}

function callBackUpdateLinkedInLink(data) {
	$('#prof-message-header').html(data);
	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();

	showProfileSocialLinks();
}

function updateYelpLink(link) {
	var payload = {
		"yelplink" : link	
	};
	if(isValidUrl(link)){
		callAjaxPostWithPayloadData("./updateyelplink.do", callBackUpdateYelpLink, payload);
	}else{
		$('#overlay-toast').html("Enter a valid url");
		showToast();
	}
}

function callBackUpdateYelpLink(data) {
	$('#prof-message-header').html(data);
	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();

	showProfileSocialLinks();
}*/

function isValidUrl(url){
	var myVariable = url;
	if(/^(http|https|ftp):\/\/[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/i.test(myVariable)) {
		return true;
	} else {
		return false;
	}  
}

// Adjust image
function adjustImage() {
	var windW = $(window).width();
	if (windW < 768) {
		var imgW = $('#prof-image').width();
		$('#prof-image').height(imgW * 0.7);
		var h2 = $('.prog-img-container').height() - 11;
		$('.prof-name-container').height(h2);
		var rowW = $('.lp-con-row').width() - 55 - 10 - 5;
		$('.lp-con-row-item').width(rowW + 'px');
		$('.footer-main-wrapper').hide();
	}
	else {
		$('.prof-name-container,#prof-image').height(200);
		var rowW = $('.lp-con-row').width() - 50 - 50; // left image-50; right-locks-50
		$('.lp-con-row-item').width(rowW + 'px');
		// $('.lp-con-row-item').width('auto');
		$('.footer-main-wrapper').show();
	}
}

// Function to show map on the screen
/*function initializeGoogleMap() {
    var mapCanvas = document.getElementById('map-canvas');
    var geocoder = new google.maps.Geocoder();
	var address = "Raremile technologies,HSR layout,bangalore, 560102";
	var latitude = 45;
	var longitude = -73;
	geocoder.geocode({
		'address' : address
	}, function(results, status) {
		if (status == google.maps.GeocoderStatus.OK) {
			latitude = results[0].geometry.location.lat();
			longitude = results[0].geometry.location.lng();
			var mapOptions = {
				      center: new google.maps.LatLng(latitude, longitude),
				      zoom: 15,
				      mapTypeId: google.maps.MapTypeId.ROADMAP
				    };
			
			map = new google.maps.Map(mapCanvas, mapOptions);
	        map.setCenter(results[0].geometry.location);
	        marker = new google.maps.Marker({
	            position: results[0].geometry.location,
	            map: map,
	            title: "RM"
	        });
		}
	});
}*/


// Data population for Admin
function paintForProfile() {
	profileId = $('#profile-id').val();
	var companyId = $('#prof-company-id').val();
	var regionId = $('#prof-region-id').val();
	var branchId = $('#prof-branch-id').val();
	var agentId = $('#prof-agent-id').val();
	
	if (companyId != undefined) {
		attrName = "companyId";
		attrVal = companyId;
		
		fetchHierarchy("companyProfileName", $("#company-profile-name").val());
	}
	else if (regionId != undefined) {
		attrName = "regionId";
		attrVal = regionId;

		fetchHierarchy(attrName, attrVal);
	}
	else if (branchId != undefined) {
		attrName = "branchId";
		attrVal = branchId;
		
		fetchHierarchy(attrName, attrVal);
	}
	else if (agentId != undefined) {
		attrName = "agentId";
		attrVal = agentId;
	}
	
	// Common call for all cases
	fetchAvgRating(attrName, attrVal);
	fetchReviewCount(attrName, attrVal, minScore);
	fetchReviews(attrName, attrVal, minScore, startIndex, numOfRows);
}

// Hierarchy data population
function fetchHierarchy(attrName, attrValue) {
	var url = "./getadminhierarchy.do?" + attrName + "=" + attrValue;
	callAjaxGET(url, paintHierarchy, true);
}

//Hierarchy data population
function fetchCompanyHierarchy(attrName, attrValue) {
	var url = "./getcompanyhierarchy.do?" + attrName + "=" + attrValue;
	callAjaxGET(url, paintHierarchy, true);
}

function paintHierarchy(data) {
	$("#prof-hierarchy-container").html(data);
	$("#prof-hierarchy-container").show();
	
	// Click on region
	$(document).on('click', '.comp-region', function(){
		if($(this).attr("data-openstatus") == "closed") {
			fetchRegionHierarchyOnClick($(this).attr('data-regionid'));
			$(this).attr("data-openstatus", "open");
		} else {
			$('#comp-region-branches-' + $(this).attr('data-regionid')).slideUp(200);
			$(this).attr("data-openstatus", "closed");
		}
	});
	
	// Click on branch
	bindClickBranchForIndividuals("comp-region-branch");
	bindClickBranchForIndividuals("comp-branch");
	
	// Individuals
	paintProfImage("comp-individual-prof-image");
}

// region hierarchy on click
function fetchRegionHierarchyOnClick(regionId) {
	var url = "./getregionhierarchy.do?regionId=" + regionId;
	callAjaxGET(url, function(data) {
		$("#comp-region-branches-" + regionId).html(data).slideDown(200);
		bindClickBranchForIndividuals("comp-region-branch");
		bindClickForIndividuals("comp-region-individual");
	}, true);
}

function bindClickBranchForIndividuals(bindingClass) {
	$("." + bindingClass).click(function(e){
		e.stopPropagation();
		if($(this).attr("data-openstatus") == "closed") {
			fetchBranchHierarchyOnClick($(this).attr('data-branchid'));
			$(this).attr("data-openstatus","open");
		} else {
			$('#comp-branch-individuals-' + $(this).attr('data-branchid')).slideUp(200);
			$(this).attr("data-openstatus","closed");
		}
	});
}

// Branch hierarchy on click
function fetchBranchHierarchyOnClick(branchId) {
	var url = "./getbranchhierarchy.do?branchId=" + branchId;
	callAjaxGET(url, function(data) {
		$("#comp-branch-individuals-" + branchId).html(data).slideDown(200);
		paintProfImage("comp-individual-prof-image");
		bindClickForIndividuals("comp-individual");
	}, true);
}

function paintProfImage(imgDivClass) {
	$("." + imgDivClass).each(function(){
		var imageUrl = $(this).attr('data-imageurl');
		if(imageUrl != "" && imageUrl != undefined) {
			$(this).css("background", "url(" + imageUrl + ") no-repeat center");
			$(this).css("background-size", "100%");
		}		
	});
}

// Fetch and paint Reviews
$(window).scroll(function() {
	var newIndex = startIndex + numOfRows;
	var totalReviews = $("#prof-company-review-count").html();
	if(totalReviews != undefined) {
		totalReviews = totalReviews.substr(0, totalReviews.indexOf(' '));

		if ((window.innerHeight + window.pageYOffset) >= (document.body.offsetHeight) && newIndex <= totalReviews) {
			fetchReviews(attrName, attrVal, minScore, newIndex, numOfRows);
			startIndex = newIndex;
		}
	}	
});

function fetchReviews(attrName, attrVal, minScore, startIndex, numOfRows) {
	var url = "./fetchreviews.do?" + attrName + "=" + attrVal + "&minScore="
			+ minScore + "&startIndex=" + startIndex + "&numOfRows=" + numOfRows;
	callAjaxGET(url, function(data) {
		$("#prof-review-item").append(data);
		$(".review-ratings").each(function() {
			changeRatingPattern($(this).attr("data-rating"), $(this));
		});
		
		$('.icn-plus-open').click(function(){
	        $(this).hide();
	        $(this).parent().find('.ppl-share-social,.icn-remove').show();
	    });
	    
	    $('.icn-remove').click(function(){
	        $(this).hide();
	        $(this).parent().find('.ppl-share-social').hide();
	        $(this).parent().find('.icn-plus-open').show();
	    });
	}, true);
}

// fetch review count
function fetchReviewCount(attrName, attrVal, minScore) {
	var url = "./fetchreviewcount.do?" + attrName + "=" + attrVal
			+ "&minScore=" + minScore;
	callAjaxGET(url, paintReviewCount, true);
}

function paintReviewCount(reviewCount) {
	if (reviewCount != undefined) {
		if (reviewCount == 0) {
			reviewCount = 'No Reviews';
			
			// hiding reviews-container if 0 reviews
			$("#reviews-container").hide();
		} else if (reviewCount == 1) {
			reviewCount = reviewCount + ' Review';
		} else {
			reviewCount = reviewCount + ' Reviews';
		}
		
		$("#prof-company-review-count").html(reviewCount);
		$("#prof-company-review-count").click(function(){
			$(window).scrollTop($('#reviews-container').offset().top);
		});
	}
}

// fetch avg rating
function fetchAvgRating(attrName, attrVal) {
	var url = "./fetchaveragerating.do?" + attrName + "=" + attrVal;
	callAjaxGET(url, paintAvgRating, true);
}

function paintAvgRating(avgRating) {
	if (avgRating != undefined) {
		changeRatingPattern(avgRating, $("#rating-avg-comp"));
	}
}

// Edit EmailIds
$(document).on('blur', '#contant-info-container input[data-email]', function() {
	if (!$(this).val() || !emailRegex.test(this.value) || ($(this).val() == $('#' + $(this).attr("id") + '-old').val())) {
		return;
	}
	
	delay(function() {
		var mailIds = [];
		$('#contant-info-container input[data-email]').each(function() {
			if (this.value != "") {
				var mailId = {};
				mailId.key = $(this).attr("data-email");
				mailId.value = this.value;
				mailIds.push(mailId);
			}
		});
		mailIds = JSON.stringify(mailIds);
		var payload = {
			"mailIds" : mailIds
		};
		callAjaxPostWithPayloadData("./updateemailids.do", callBackOnUpdateMailIds, payload);
	}, 0);
});

function callBackOnUpdateMailIds(data) {
	$('#prof-message-header').html(data);
	callAjaxGET("./fetchcontactdetails.do", callBackShowContactDetails);

	$('#overlay-toast').html($('#display-msg-div').text().trim());
	showToast();
}

function bindClickForIndividuals(elementClass) {
	$("."+elementClass).click(function(e){
		e.stopPropagation();
	});
}