<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<div class="hm-header-main-wrapper hm-hdr-bord-bot soc-mon-hdr">
	<div class="container">
		<div class="hm-header-row clearfix">
			<div class="float-left hm-header-row-left hr-dsh-adj-lft soc-mon-hdr-txt">
				<spring:message code="label.social.monitor.key" /> - <spring:message code="label.edit.monitors.key" />
			</div>
			<div class="float-right hm-header-right text-center soc-mon-btn" onclick="javascript:showMainContent('./showsocialmonitorstreampage.do')">
					<spring:message code="label.view.stream.key" />
			</div>
			<div class="float-right hm-header-right text-center soc-mon-btn" onclick="">
					<spring:message code="label.social.monitor.reports.key" />
			</div>
		</div>
	</div>
</div>

<div class="hm-header-main-wrapper hm-hdr-bord-bot soc-mon-sub-hdr">
	<div class="container">
		<div class="hm-header-row clearfix">
			<div class="v-um-hdr-left v-um-hdr-search float-left soc-mon-search-bar">
				<input id="search-monitors-key" class="v-um-inp soc-mon-inp" placeholder="<spring:message code="label.social.monitor.search.key" />">
				<span id="soc-mon-search-icn" class="um-search-icn"></span>
				<div id="soc-mon-search-clr" class="um-clear-input-icn hide" title="clear"></div>
			</div>
			<div class="hm-header-left text-center float-left">
				<div id="mon-type-dropdown" class="float-left stream-bulk-actions macro-dropdown-options">
					<div class="mon-type-select">Monitor Type(s) <img src="${initParam.resourcesPath}/resources/images/chevron-down.png" id="mon-type-chevron-down" class="float-right bulk-actions-dropdown-img"><img id="mon-type-chevron-up" src="${initParam.resourcesPath}/resources/images/chevron-up.png" class="hide float-right bulk-actions-dropdown-img"></div>
					<div id="mon-type-options" class="hide float-left bulk-actions-options mon-type-select-options">
						<div id="mon-type-keyword-mon" class="bulk-option mon-type-option">
							<img id="keyword-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="hide float-left mon-type-checkbox">
							<img id="keyword-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="float-left mon-type-checkbox">
							Keyword Monitor
						</div>
						<div id="mon-type-google-alerts" class="bulk-option mon-type-option">
							<img id="google-alerts-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="hide float-left mon-type-checkbox">
							<img id="google-alerts-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="float-left mon-type-checkbox">
							Google Alerts
						</div>
					</div>
				</div>
			</div>
			<div class="float-right hm-header-right text-center soc-mon-btn soc-mon-add-mon" onclick="">
				<spring:message code="label.add.monitors.key" />
			</div>
		</div>
	</div>
</div>

<div id="soc-mon-edit-mon" class="dash-wrapper-main" style="margin-bottom: 20px;">
	<div class="dash-container container zero-padding">
			<div class="container zero-padding" style="width:100%">
				<div class="dash-stats-wrapper clearfix"  >
					<div id="mon-type-bulk-actions" class="float-left edit-mon-bulk-actions">
						<div class="bulk-actions-select">Bulk Actions <img src="${initParam.resourcesPath}/resources/images/chevron-down.png" id="chevron-down" class="float-right bulk-actions-dropdown-img"><img id="chevron-up" src="${initParam.resourcesPath}/resources/images/chevron-up.png" class="hide float-right bulk-actions-dropdown-img"></div>
						<div id="edit-monitor-bulk-options" class="hide float-left bulk-actions-options">
								
						</div>
					</div>
				</div>
			</div>
	</div>
	
	<div class="dash-container container mon-type-container zero-padding">
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 clearfix mon-type-hdr"  >
			<img id="edit-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="float-left mon-type-checkbox">
			<img id="edit-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="hide float-left mon-type-checkbox">
			<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8 soc-mon-txt-bold">Keyphrase</div>
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-3 mon-type-hdr-txt">Monitor Type</div>
		</div>
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 clearfix mon-type-gray-container"  >
			<img id="edit-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="float-left mon-type-checkbox">
			<img id="edit-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="hide float-left mon-type-checkbox">
			<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8 mon-type-keyphrase">Lowest Rate</div>
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-3 mon-type-tbl-txt">Keyword Monitor</div>
		</div>
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 clearfix mon-type-white-container"  >
			<img id="edit-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="float-left mon-type-checkbox">
			<img id="edit-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="hide float-left mon-type-checkbox">
			<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8 mon-type-keyphrase">New American Financial</div>
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-3 mon-type-tbl-txt">Google Alert</div>
		</div>
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 clearfix mon-type-gray-container"  >
			<img id="edit-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="float-left mon-type-checkbox">
			<img id="edit-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="hide float-left mon-type-checkbox">
			<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8 mon-type-keyphrase">Lowest Rate</div>
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-3 mon-type-tbl-txt">Keyword Monitor</div>
		</div>
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 clearfix mon-type-white-container"  >
			<img id="edit-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="float-left mon-type-checkbox">
			<img id="edit-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="hide float-left mon-type-checkbox">
			<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8 mon-type-keyphrase">New American Financial</div>
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-3 mon-type-tbl-txt">Google Alert</div>
		</div>
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 clearfix mon-type-gray-container"  >
			<img id="edit-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="float-left mon-type-checkbox">
			<img id="edit-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="hide float-left mon-type-checkbox">
			<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8 mon-type-keyphrase">Lowest Rate</div>
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-3 mon-type-tbl-txt">Keyword Monitor</div>
		</div>
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 clearfix mon-type-white-container"  >
			<img id="edit-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="float-left mon-type-checkbox">
			<img id="edit-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="hide float-left mon-type-checkbox">
			<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8 mon-type-keyphrase">New American Financial</div>
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-3 mon-type-tbl-txt">Google Alert</div>
		</div>
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 clearfix mon-type-gray-container"  >
			<img id="edit-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="float-left mon-type-checkbox">
			<img id="edit-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="hide float-left mon-type-checkbox">
			<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8 mon-type-keyphrase">Lowest Rate</div>
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-3 mon-type-tbl-txt">Keyword Monitor</div>
		</div>
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 clearfix mon-type-white-container"  >
			<img id="edit-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="float-left mon-type-checkbox">
			<img id="edit-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="hide float-left mon-type-checkbox">
			<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8 mon-type-keyphrase">New American Financial</div>
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-3 mon-type-tbl-txt">Google Alert</div>
		</div>
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 clearfix mon-type-gray-container"  >
			<img id="edit-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="float-left mon-type-checkbox">
			<img id="edit-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="hide float-left mon-type-checkbox">
			<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8 mon-type-keyphrase">Lowest Rate</div>
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-3 mon-type-tbl-txt">Keyword Monitor</div>
		</div>
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 clearfix mon-type-white-container"  >
			<img id="edit-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="float-left mon-type-checkbox">
			<img id="edit-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="hide float-left mon-type-checkbox">
			<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8 mon-type-keyphrase">New American Financial</div>
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-3 mon-type-tbl-txt">Google Alert</div>
		</div>
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 clearfix mon-type-gray-container"  >
			<img id="edit-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="float-left mon-type-checkbox">
			<img id="edit-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="hide float-left mon-type-checkbox">
			<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8 mon-type-keyphrase">Lowest Rate</div>
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-3 mon-type-tbl-txt">Keyword Monitor</div>
		</div>
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 clearfix mon-type-white-container"  >
			<img id="edit-mon-unchecked" src="${initParam.resourcesPath}/resources/images/check-no.png"  class="float-left mon-type-checkbox">
			<img id="edit-mon-checked" src="${initParam.resourcesPath}/resources/images/check-yes.png"  class="hide float-left mon-type-checkbox">
			<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8 mon-type-keyphrase">New American Financial</div>
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-3 mon-type-tbl-txt">Google Alert</div>
		</div>
	</div>
</div>