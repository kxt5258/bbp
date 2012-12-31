<%@page import="species.participation.Checklist"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.participation.Comment"%>
<%@page import="species.groups.UserGroup"%>
<%@page import="species.participation.ActivityFeedService"%>
<div class="activityFeedContext" >
	<div class="feedParentContext thumbnail">
		<g:if test="${feedInstance.rootHolderType ==  Observation.class.getCanonicalName()}" >
			<%
				def tmpUserGroup = (feedHomeObject && feedHomeObject.class.getCanonicalName() == UserGroup.class.getCanonicalName()) ? feedHomeObject : null
			%>
			<obv:showSnippet model="['observationInstance':feedParentInstance, userGroup:tmpUserGroup, userGroupWebaddress:tmpUserGroup?.webaddress]"></obv:showSnippet>
		</g:if>
		<g:elseif test="${feedInstance.rootHolderType ==  UserGroup.class.getCanonicalName()}" >
			<uGroup:showUserGroupSignature model="['userGroup':feedParentInstance, 'showDetails':true]"></uGroup:showUserGroupSignature>
		</g:elseif>
		<g:elseif test="${feedInstance.rootHolderType ==  Checklist.class.getCanonicalName()}" >
			<clist:showSnippet model="['checklistInstance':feedParentInstance, userGroup:tmpUserGroup]"></clist:showSnippet>
		</g:elseif>
		
		<g:else>
			${feedInstance.rootHolderType}
		</g:else>
	</div>
	<%
		def isCommentThread = (feedInstance.subRootHolderType == Comment.class.getCanonicalName() && feedInstance.rootHolderType != Observation.class.getCanonicalName()) 
	%>
	<g:if test="${isCommentThread}">
		<div class="feedSubParentContext ${feedInstance.fetchMainCommentFeed().subRootHolderType + feedInstance.fetchMainCommentFeed().subRootHolderId}">
		Comment thread
<%--			<comment:showCommentWithReply model="['feedInstance' : feedInstance.fetchMainCommentFeed(), 'feedPermission':feedPermission, 'showAlways':true]" />	--%>
		</div>
	</g:if>
	<feed:showAllActivityFeeds model="['rootHolder':feedParentInstance, 'isCommentThread':isCommentThread, 'subRootHolderType':feedInstance.subRootHolderType, 'subRootHolderId':feedInstance.subRootHolderId, 'feedType':ActivityFeedService.SPECIFIC, 'refreshType':ActivityFeedService.MANUAL, 'feedPermission':feedPermission]" />
</div>