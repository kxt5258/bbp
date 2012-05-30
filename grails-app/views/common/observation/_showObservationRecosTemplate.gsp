<%@page import="species.utils.ImageType"%>

<g:if test="${result.size() > 0 }">
	<g:each in="${result}" var="r">
		<li class="reco_block">
			<div>
				<div class="users">
					<div class="iAgree btn btn-primary btn-small">
						<g:remoteLink action="addAgreeRecommendationVote" method="GET"
							controller="observation"
							params="['obvId':r.obvId, 'recoId':r.recoId, 'currentVotes':r.noOfVotes]"
							onSuccess="preLoadRecos(3,false);return false"
							onFailure="handleError(XMLHttpRequest,textStatus,errorThrown,this.success,showRecoUpdateStatus);return false;">Agree</g:remoteLink>
					</div>
					<g:each in="${r.authors}" var="author">
						<g:link controller="SUser" action="show" id="${author?.id}">
							<img class="very_small_profile_pic"
								src="${author?.icon(ImageType.VERY_SMALL)}"
								title="${author.name}" />
						</g:link>
					</g:each>
				</div>

				<g:if test="${r.observationImage}">
					<g:link controller="observation" action="show" id="${r.obvId}">
						<img style="width: 75px; height: 75px;"
							src="${r.observationImage}">
					</g:link>
				</g:if>

				<span class="voteCount"><span id="votes_${r.recoId}">
						${r.noOfVotes} </span> <g:if test="${r.noOfVotes <= 1}"> user thinks</g:if>
					<g:else> users think</g:else> it is:</span><span class="highlight">
					<g:if test="${r.canonicalForm}">
						<g:link controller="species" action="show" id="${r.speciesId}">
							<i> ${r.canonicalForm} </i>
						</g:link>
					</g:if> <g:else>
						<i> ${r.name} </i>
					</g:else> </span>
				<obv:showRecoComment
					model="['recoComments':r.recoComments, 'recoId': r.recoId]" />

			</div> <g:javascript>
                        $(document).ready(function(){
                                $('#voteCountLink_${r.recoId}').click(function() {
                                        $('#voteDetails_${r.recoId}').show();
                                });

                                $('#voteDetails_${r.recoId}').mouseout(function(){
                                        $('#voteDetails_${r.recoId}').hide();
                                });
                        });
                       
                        </g:javascript></li>
	</g:each>
</g:if>
<g:else>
	<g:message code="recommendations.zero.message" />
</g:else>



