<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="user"
	value="${sessionScope.SPRING_SECURITY_CONTEXT.authentication.principal}" />

<body>
	<div class="hm-header-main-wrapper">
		<div class="container">
			<div class="hm-header-row hm-header-row-main clearfix">
				<div class="float-left hm-header-row-left text-center">
					<spring:message code="label.reviewsmonitor.key" />
				</div>
			</div>
		</div>
	</div>
	<div class="vendasta-container">
		<iframe id="vendasta-iframe" height="600px" width="102%">
			Vendasta Integration </iframe>
		<div id="alternate" class="access-cont hide">You do not have
			access to this resource.</div>
	</div>
	<script>
		var url = "";
		$(document).ready(function() {
			loadVendastaIframe();
		});
		//load Reviews monitor Iframe with Vendasta product URL
		function loadVendastaIframe() {
			url = fetchVendastaUrl();
			if (url == "" || url == undefined) {
				$('#vendasta-iframe').hide();
				$('#alternate').show();
			} else {
				$("#vendasta-iframe").attr("src", url);
			}
		}

		function fetchVendastaUrl() {
			var payload = {};
			callAjaxGetWithPayloadData("/fetchvendastaurl.do", function(data) {
				map = JSON.parse(data);
				if (map.status == "success") {
					if (map.ssoToken != undefined || mao.ssoToken != "") {
						url = map.url + "?sso_token=" + map.ssoToken;
					}
				}
			}, payload, false);
			return url;
		}
	</script>