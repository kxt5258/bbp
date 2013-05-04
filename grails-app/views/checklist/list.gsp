<%@page import="species.utils.Utils"%>
<html>
<head>
<link rel="canonical" href="${Utils.getIBPServerDomain() + createLink(controller:'checklist', action:'list')}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'checklistList.label', default: 'Checklist')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<r:require modules="checklist"/>
</head>
<body>
<div class="span12">
			<clist:showSubmenuTemplate model="['entityName':entityName]" />

			<div class="gallerytoolbar">
				<clist:filterTemplate />		
			</div>
			
			<div style="clear: both;"></div>
			
			<g:if test="${!isSearch && instanceTotal > 0}">
				<div id="map_view_bttn" class="btn-group" style="clear:both;">
					<a class="btn btn-success dropdown-toggle" data-toggle="dropdown"
						href="#"
						onclick="$(this).parent().css('background-color', '#9acc57'); showChecklistMapView(); return false;">
						Map view <span class="caret"></span> </a>
				</div>
			</g:if>
			
			<div id="observations_list_map" class="observation"
				style="clear: both; display: none;">
				<clist:showChecklistLocation
					model="['checklistInstanceList':checklistMapInstanceList, 'userGroup':userGroup]">
				</clist:showChecklistLocation>
			</div>
			
			<clist:showList />
		
</div>
</body>
</html>
