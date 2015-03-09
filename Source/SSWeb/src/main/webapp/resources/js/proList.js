// pagination variables
var rowSize = 10;
var startIndex = 0;

function adjustTextContainerWidthOnResize() {
	var parentWidth = $('.ctnt-list-item').width();
	var imgWidth = $('.ctnt-list-item .ctnt-list-item-img').width();
	// var buttonWidth = $('.ctnt-list-item .ctnt-list-item-btn-wrap').width();
	// var textContainerWidth = parentWidth - (imgWidth + buttonWidth) - 20;
	var textContainerWidth = parentWidth - imgWidth - 20;
	$('.ctnt-list-item .ctnt-list-item-txt-wrap').width(textContainerWidth);
}

// Function to validate the first name pattern
function validateProFirstNamePattern(elementId) {
	if ($('#' + elementId).val() != "") {
		if (firstNamePatternRegex.test($('#' + elementId).val()) == true) {
			return true;
		} else {
			$('#overlay-toast').html('Please enter a first name pattern.');
			showToast();
			return false;
		}
	} else {
		$('#overlay-toast').html('Please enter a first name pattern.');
		showToast();
		return false;
	}
}

// Function to validate the last name pattern
function validateProLastNamePattern(elementId) {
	if ($('#' + elementId).val() != "") {
		if (lastNamePatternRegEx.test($('#' + elementId).val()) == true) {
			return true;
		} else {
			$('#overlay-toast').html('Please enter a valid last name pattern.');
			showToast();
			return false;
		}
	} else {
		return false;
	}
}

// Function to validate registration form
function validateFindProForm(id) {
	$("#serverSideerror").hide();
	if (!validateProFirstNamePattern('find-pro-first-name') && !validateProLastNamePattern('find-pro-last-name')) {
		$('#find-pro-first-name').focus();
		return false;
	}
	return true;
}

$('#find-pro-submit').click(function(e) {
	e.preventDefault();
	if(validateFindProForm('find-pro-form')){
		console.log("Submitting Find a Profile form");
		$('#find-pro-form').submit();
	}
	showOverlay();
});

$(window).scroll(function() {
	var newIndex = startIndex + rowSize;
	if ((window.innerHeight + window.pageYOffset) >= (document.body.offsetHeight) && newIndex < $('#srch-num').html()) {
		fetchUsers(newIndex);
		startIndex = newIndex;
	}
});

/**
 * Method to fetch users list based on the criteria i.e if profile level is specified,
 *  bring all users of that level else search based on first/last name
 * @param newIndex
 */
function fetchUsers(newIndex) {
	var profileLevel = $("#fp-profile-level-fetch-info").data("profile-level");
	var iden = $("#fp-profile-level-fetch-info").data("iden");
	
	if(profileLevel != undefined && profileLevel != ""){
		fetchUsersByProfileLevel(iden, profileLevel, startIndex);
	}
	else {
		var formData = new FormData();
		formData.append("find-pro-first-name", $('#fp-first-name-pattern').val());
		formData.append("find-pro-last-name", $('#fp-last-name-pattern').val());
		formData.append("find-pro-start-index", newIndex);
		formData.append("find-pro-row-size", rowSize);
		callAjaxPOSTWithTextData("./findaproscroll.do", infiniteScrollCallback, true, formData);
	}
	
}

function infiniteScrollCallback(response) {
	var users =  $.parseJSON(response);
	if (users != undefined) {
		paintProList(users);
	}
}

function paintProList(usersList) {
	if(usersList != undefined) {
		var usersSize = usersList.length;
		var usersHtml = "";
		if(usersSize > 0){
			$.each(usersList,function(i,user){
				var evenOddClass = (i % 2 == 0) ? '' : 'ctnt-list-item-even';
				usersHtml = usersHtml + '<div class="ctnt-list-item clearfix ' + evenOddClass + '">';
				usersHtml = usersHtml + '<div class="float-left ctnt-list-item-img" style="background: url(' + user.profileImageUrl + ') no-repeat center; background-size: cover;"></div>';
				usersHtml = usersHtml + '<div class="float-left ctnt-list-item-txt-wrap">';
				usersHtml = usersHtml + '	<div class="ctnt-item-name user-display-name" data-profilename="' + user.profileName + '">' + user.displayName + '</div>';
				if(user.title != undefined){
					usersHtml = usersHtml + '<div class="ctnt-item-desig">' + user.title + '</div>';
				}
				if(user.aboutMe != undefined){
					usersHtml = usersHtml + '<div class="ctnt-item-comment">' + user.aboutMe + '</div>';
				}
				usersHtml = usersHtml + '</div>';
				usersHtml = usersHtml + '<div class="float-left ctnt-list-item-btn-wrap">'
					+ '<div class="ctnt-review-btn" user="' + user.userId + '">Review</div>'
				+ '</div>'
			+ '</div>';
			});
			
			$('#ctnt-list-wrapper').append(usersHtml);
			$('#fp-users-size').val(usersSize);
			
			/*$(".user-display-name").click(function(e){
				// e.stopPropagation();
				var agentProfileName = $(this).attr("data-profilename");
				var url = window.location.origin + "/individualprofile/" + agentProfileName + ".do";
				window.open(url, "_blank");
			});*/
		}
	}
}

function fetchUsersByProfileLevel(iden,profileLevel,startIndex){
	if(iden == undefined){
		console.log("iden is undefined for fetchUsersByProfileLevel");
		return;
	}
	var url = window.location.origin +"/rest/profile/individuals/"+iden+"?profileLevel="+profileLevel+"&startIndex="+startIndex;
	callAjaxGET(url, fetchUsersByProfileLevelCallback, false);
}

function fetchUsersByProfileLevelCallback(data) {
	var response = $.parseJSON(data);
	if(response != undefined) {
		var usersList = $.parseJSON(response.entity);
		paintProList(usersList);
	}
}

$(document).on('click', '.ctnt-review-btn', function(){
	initSurveyReview($(this).attr('user'));
});
