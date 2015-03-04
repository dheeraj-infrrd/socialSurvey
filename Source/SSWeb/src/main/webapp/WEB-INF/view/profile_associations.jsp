<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:if test="${not empty profile && not empty profile.associations}">
	<c:set value="${profile.associations}" var="associations"></c:set>
</c:if>
<c:choose>
	<c:when test="${not empty associations}">
		<c:forEach items="${associations}" var="association">
			<input class="lp-assoc-row lp-row clearfix prof-edditable-sin-agent" value="${association.name}">
			<div class="float-left lp-ach-item-img" data-type="association"></div>
		</c:forEach>
	</c:when>
	<c:otherwise>
		<div><spring:message code="label.membership.empty.key"></spring:message></div>
	</c:otherwise>
</c:choose>