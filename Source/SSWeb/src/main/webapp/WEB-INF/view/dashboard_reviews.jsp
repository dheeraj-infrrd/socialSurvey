<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<c:choose>
	<c:when test="${entityType == 'companyId'}">
		<c:set value="1" var="profilemasterid"></c:set>
	</c:when>
	<c:when test="${entityType == 'regionId'}">
		<c:set value="2" var="profilemasterid"></c:set>
	</c:when>
	<c:when test="${entityType == 'branchId'}">
		<c:set value="3" var="profilemasterid"></c:set>
	</c:when>
	<c:when test="${entityType == 'agentId'}">
		<c:set value="4" var="profilemasterid"></c:set>
	</c:when>
</c:choose>
<c:if test="${not empty reviews}">
	<c:forEach var="feedback" varStatus="loop" items="${reviews}">
		<c:set value="#.#" var="scoreformat"></c:set>
		<c:set
			value="${ feedback.customerFirstName } ${ feedback.customerLastName }"
			var="customerName"></c:set>
		<c:set value="${fn:split(customerName, ' ')}" var="nameArray"></c:set>
		<c:choose>
			<c:when test="${ not empty nameArray[1] }">
				<c:set
					value="${ nameArray[0] } ${ nameArray[1].substring( 0, 1 ).toUpperCase() }."
					var="customerDisplayName"></c:set>
			</c:when>
			<c:otherwise>
				<c:set value="${ nameArray[0] }" var="customerDisplayName"></c:set>
			</c:otherwise>
		</c:choose>
		<div data-firstname="${feedback.customerFirstName}" data-lastname="${feedback.customerLastName}"
			data-agentid="${feedback.agentId}" data-agentname="${feedback.agentName}" data-customeremail="${feedback.customerEmail}"
			data-review="${fn:escapeXml(feedback.review)}" data-score="${feedback.score}" survey-mongo-id="${feedback._id}" class="ppl-review-item dsh-review-cont hide">
			
			<%-- <div class="ppl-header-wrapper clearfix">
				<div class="float-left ppl-header-left">
					<div class="ppl-head-1">
						<c:choose>
							<c:when test="${fn:toLowerCase(feedback.customerLastName) eq 'null'}">
								${feedback.customerFirstName}
							</c:when>
							<c:otherwise>
								${feedback.customerFirstName} ${feedback.customerLastName}
							</c:otherwise>
						</c:choose>
					</div>
					<div class="ppl-head-2" data-modified="false" data-modifiedon="<fmt:formatDate type="date" pattern="yyyy-MM-dd-H-mm-ss"
						value="${feedback.modifiedOn}" />">
					</div>
				</div>
				<div class="float-right ppl-header-right">
					<div class="st-rating-wrapper maring-0 clearfix review-ratings float-right" data-modified="false" data-rating="${feedback.score}" data-source="${feedback.source }">
					</div>
					<c:if test="${feedback.source != 'Zillow'}">
						<div class="report-resend-icn-container clearfix float-right">
							<!-- <div class="report-abuse-txt report-txt">Report</div> -->
							
							<div class="restart-survey-mail-txt report-txt">Retake</div>
						</div>
					</c:if>
				</div>
			</div> --%>
			<div class="ppl-header-wrapper clearfix">
			<div class="float-left ppl-header-right">
					<div class="st-rating-wrapper maring-0 clearfix review-ratings float-right" data-modified="false" data-rating="${feedback.score}" data-source="${feedback.source }">
					</div>
					<%-- <c:if test="${feedback.source != 'Zillow'}">
						<div class="report-resend-icn-container clearfix float-right">	
							<div class="restart-survey-mail-txt report-txt">Retake</div>
						</div>
					</c:if>  --%>
				</div>
				<div class="float-left ppl-header-left">
					<div class="ppl-head-1">
					<span class="float-left"> &#8212; Reviewed by </span>
						<c:choose>
							<c:when test="${fn:toLowerCase(feedback.customerLastName) eq 'null'}">
								<span class="float-left" style="margin-left:5px;font-weight:600 !important;"> ${feedback.customerFirstName} </span >
							</c:when>
							<c:otherwise>
								<span class="float-left" style="margin-left:5px;font-weight:600 !important;"> ${feedback.customerFirstName} ${feedback.customerLastName}</span>
							</c:otherwise>
						</c:choose>
						<c:if test="${profilemasterid !=4}">
					<span class="float-left " style="margin-left:5px;">for<a style="color:#236CAF;font-weight: 600 !important;" href="${feedback.completeProfileUrl}" target="_blank"> ${feedback.agentName}</a></span>
					</c:if>
					<span class="float-left" style="margin: 0 5px;">&#8212;</span>
					<div class="ppl-head-2 float-left" data-modified="false" data-modifiedon="<fmt:formatDate type="date" pattern="yyyy-MM-dd-H-mm-ss"
						value="${feedback.modifiedOn}" />"> 
					</div>
					</div>
					
					<%--  <c:choose>
					 <c:when test="${feedback.source=='agent' || feedback.source=='admin'}">
					  <span class="float-left" style="font-family: opensanslight;"> - sourced by SocialSurvey</span>
					 </c:when>
					 <c:when test="${feedback.source=='Zillow'}">
					  <span class="float-left" style="font-family: opensanslight;"> - sourced by <span style="color:#009FE0;">Zillow.com</span></span>
					 </c:when>
					 <c:otherwise>
					 <span class="float-left" style="font-family: opensanslight;"> - sourced by ${feedback.source} </span>
					 </c:otherwise>
					 </c:choose> --%>
				</div>
				<c:if test="${feedback.source =='encompass'}">
				<div class='verified-badge  verify-image float-right' title='Click here to know more'></div>
				</c:if>
				<c:if test="${feedback.source =='DOTLOOP'}">
				<div class='verified-badge  verify-image float-right' title='Click here to know more'></div>
				</c:if>
				<c:if test="${feedback.source =='Zillow'}">
				<div class='zillow-badge  verify-image float-right'></div>
				</c:if>
			</div>
			<c:if test="${ not empty feedback.summary }">
				<div class="ppl-content">${feedback.summary}</div>
			</c:if>
			<%-- <div class="ppl-content">${feedback.review}</div> --%>
			<div class="ppl-share-wrapper clearfix share-plus-height" style="visibility:hidden;">
				<%-- <div class="float-left blue-text ppl-share-shr-txt"><spring:message code="label.share.key" /></div> --%>
				<!-- <div class="float-left icn-share icn-plus-open" style="display: block;"></div> -->
				<div class="float-left clearfix ppl-share-social hide" style="display: block;">
				
					<span id ="fb_${loop.index}"class="float-left ppl-share-icns icn-fb" title="Facebook" onclick ="getDashboardImageandCaption(${loop.index})" data-link="https://www.facebook.com/dialog/feed?${feedback.faceBookShareUrl}&link=${fn:replace(feedback.completeProfileUrl, 'localhost', '127.0.0.1')}&description=<fmt:formatNumber type="number" pattern="${ scoreformat }" value="${feedback.score}" maxFractionDigits="1" minFractionDigits="1" />-star response from ${ customerDisplayName } for ${feedback.agentName} at SocialSurvey - ${fn:escapeXml(feedback.review)} .&redirect_uri=https://www.facebook.com"></span>
					
					<input type="hidden" id="twttxt_${loop.index}" class ="twitterText_loop" value ="<fmt:formatNumber type="number" pattern="${ scoreformat }" value="${feedback.score}" maxFractionDigits="1" minFractionDigits="1" />-star response from ${ customerDisplayName } for ${feedback.agentName} at SocialSurvey - ${fn:escapeXml(feedback.review)}"/>
					<span class="float-left ppl-share-icns icn-twit" id ="twitt_${loop.index}" onclick="twitterDashboardFn(${loop.index},this);" data-link="https://twitter.com/intent/tweet?text=<fmt:formatNumber type="number" pattern="${ scoreformat }" value="${feedback.score}" maxFractionDigits="1" minFractionDigits="1" />-star response from ${ customerDisplayName } for ${feedback.agentName} at SocialSurvey - ${fn:escapeXml(feedback.review)}&url=${feedback.completeProfileUrl}"></span>
					 <span
						class="float-left ppl-share-icns icn-lin" title="LinkedIn"
						data-link="https://www.linkedin.com/shareArticle?mini=true&url=${feedback.completeProfileUrl} &title=&summary=<fmt:formatNumber type="number" pattern="${ scoreformat }" value="${feedback.score}" maxFractionDigits="1" minFractionDigits="1" />-star response from ${ customerDisplayName } for ${feedback.agentName} at SocialSurvey - ${fn:escapeXml(feedback.review)}+ &source="></span>
                       <span class="float-left" title="Google+">
                       <button 
                           class="g-interactivepost float-left ppl-share-icns icn-gplus"
                           data-contenturl="${feedback.completeProfileUrl}"
                           data-clientid="${feedback.googleApi}"
                           data-cookiepolicy="single_host_origin"
                           data-prefilltext="<fmt:formatNumber type="number" pattern="${ scoreformat }" value="${feedback.score}" maxFractionDigits="1" minFractionDigits="1" />-star response from ${ customerDisplayName } for ${feedback.agentName} at SocialSurvey - ${fn:escapeXml(feedback.review)}"
                           data-calltoactionlabel="USE"
                           data-calltoactionurl="${feedback.completeProfileUrl}">
                          <span class="icon">&nbsp;</span>
                          <span class="label">share</span>
                      </button>
                       </span>
                       
				</div>
				<!-- <div class="float-left icn-share icn-remove icn-rem-size hide" style="display: none;"></div> -->
				<div class="float-right">
					<div class="clearfix">
						<div class="icn-flag float-left report-abuse-txt cursor-pointer "
							title="Report"></div>
						<c:if test="${feedback.source != 'Zillow'}">
							<!-- <span class="report-resend-icn-container clearfix float-right"> -->
								<div class="restart-survey-mail-txt report-txt retake-icn float-left" title="Retake"></div>
							
						</c:if>
					</div>
				</div>
			</div>
			<div class="ppl-content">${feedback.review}</div>
		</div>
	</c:forEach>
</c:if>
<script type="text/javascript" src="//apis.google.com/js/client:plusone.js" async="async"></script>
<script type="text/javascript" src="//apis.google.com/js/plusone.js" async="async"></script>
