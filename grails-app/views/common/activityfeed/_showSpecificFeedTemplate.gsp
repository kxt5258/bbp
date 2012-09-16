<%@page import="species.utils.ImageType"%>
<div class="yj-message-container">
	<div class="yj-avatar">
		<g:link controller="SUser" action="show"
			id="${feedInstance.author.id}">
			<img class="small_profile_pic"
				src="${feedInstance.author.icon(ImageType.SMALL)}"
				title="${feedInstance.author.name}" />
		</g:link>
	</div>
	<b> ${feedInstance.author.name} :<span class="yj-context"> ${feedInstance.activityType}</span></b>
	<feed:showActivity model="['feedInstance' : feedInstance, 'feedPermission':feedPermission]" />
	<time class="timeago" datetime="${feedInstance.lastUpdated.getTime()}"></time>
</div>
