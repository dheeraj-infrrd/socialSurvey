<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:if test="${not empty companyList}">
	<c:forEach items="${companyList}" var="companyItem">
		<div id="tr-comp-${companyItem.iden}" clicked="false"
			class="v-tbl-row v-tbl-row-sel comp-row cursor-pointer"
			data-iden="${companyItem.iden}">
			<div class="v-tbl-line">
				<div class="v-line-comp"></div>
			</div>
			<div class="v-tbl-name">
				<c:if test="${not empty companyItem.contact_details && not empty companyItem.contact_details.name }">
					${companyItem.contact_details.name}
				</c:if>
			</div>
			<div class="v-tbl-add"></div>
			<div class="v-tbl-role"></div>
			<div class="v-tbl-btns">
				<div class="clearfix v-tbl-icn-wraper">
					<div class="float-left v-tbl-icn v-icn-close comp-del-icn vis-hidden"
						data-iden="${companyItem.iden}"></div>
					<div class="float-right v-tbl-icn v-icn-edit comp-edit-icn"
						clicked="false" data-iden="${companyItem.iden}"></div>
				</div>
			</div>
			<div class="v-tbl-spacer"></div>
		</div>
		<div data-iden="${companyItem.iden}" class="hide comp-hr-cont"></div>
	</c:forEach>
</c:if>
<script>
$('.comp-login-icn').on('click',function(e){
	e.stopImmediatePropagation();
	var payload = {
		"colName" : "companyId",
		"colValue" : $(this).attr('data-iden')	
	};
	callAjaxGETWithTextData("/loginadminas.do", function(data) {
		window.location = window.location.origin + '/userlogin.do';
	}, true, payload);
});
</script>