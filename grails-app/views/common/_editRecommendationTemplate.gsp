<%@ page import="species.participation.RecommendationVote.ConfidenceType"%>
<%@ page import="species.Language"%>

<g:hasErrors bean="${recommendationVoteInstance}">
	<div class="alert alert-error">
		<g:renderErrors bean="${observationInstance}" as="list" />
	</div>
</g:hasErrors>
	<%
		def species_sn_name = ""
		def species_cn_name = ""
		def species_call_comment = ""
		def species_canonical_name = "" 
		//showing vote added by creator of the observation
		if(params.action == 'edit' || params.action == 'update'){
			def tmp_reco_vote = observationInstance?.fetchOwnerRecoVote()
			def tmp_cn_reco	= tmp_reco_vote?.commonNameReco
			
			species_call_comment =  tmp_reco_vote?.comment
			species_cn_name = (tmp_cn_reco)? tmp_cn_reco.name : ""
			
			if(tmp_reco_vote && tmp_reco_vote.recommendation.isScientificName){
				species_sn_name = tmp_reco_vote.recommendation.name
				species_canonical_name = tmp_reco_vote.recommendation.taxonConcept?.canonicalForm
			}
		}
		if(params.action == 'save'){
			species_sn_name = params?.recoName
			species_cn_name =  params?.commonName
			species_call_comment = params?.recoComment
			species_canonical_name = params?.canName
		}
	%>

        <div class="row control-group commonNameDiv">
            <label for="recommendationVote" class="control-label"> <g:message
                code="observation.recommendationVote.label" default="Common name" />
            </label>
            <div class="controls">
                <div class="nameContainer textbox" style="position:relative;">

                    <input type="text" name="commonName" class="commonName"
                        value="${species_cn_name}" placeholder='Suggest a common name'
                        class="input-block-level ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')} ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}" />
                    <input type="hidden" class="mappedRecoNameForcanName" />

                    <div style="width:90px;">
                        <s:chooseLanguage />
                    </div>

                    </div>
            </div>
        </div>



            <div class="row control-group sciNameDiv" style="margin-top:5px;">
                <label for="recommendationVote" class="control-label"> <g:message
                    code="observation.recommendationVote.label" default="Scientific name" />
                </label>
                <div class="controls">
                    <div class="textbox nameContainer">

                        <g:set var="species_sn_lang" value="${species_sn_lang}" />
                        <input type="text" name="recoName" class="recoName" value="${species_sn_name}"
                            placeholder='Suggest a scientific name'
                            class="input-block-level ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')} ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}" />
                        <input type="hidden" name="canName" class="canName" value="${species_canonical_name }"/>
                        

                    </div>
                </div>
            </div>


            <div class="row control-group recoCommentDiv"  style="margin-top:5px;">
                <label for="recommendationVote" class="control-label"> <g:message
                    code="observation.recommendationVote.label" default="Comment" />
                </label>
                <div class="controls">
                    <div class="nameContainer textbox">
                        <input type="text" name="recoComment" id="recoComment" value="${species_call_comment}"
                        class="input-block-level ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')} ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}"
                        placeholder="Write comment on species call"/>
                    </div>
                </div>
            </div>



<r:script>
	$(document).ready(function() {
		//$('#recoComment').val('');
		$('#reco-action').click(function() {
			$('#reco-options').show();
			$('#reco-action').hide();
		});
                $(".commonName").click(function(){
                    $("#commonNameSuggestions").remove();
                    $(this).next().next().after("<div id='commonNameSuggestions' style='display: block;'></div>");
                    $(this).autofillNames({
                        'appendTo' : '#commonNameSuggestions',
                        'nameFilter':'commonNames',
                        focus: function( event, ui ) {
                            $(this).val( ui.item.label.replace(/<.*?>/g,"") );
                            $("#commonNameSuggestions li a").css('border', 0);
                            return false;
                        },select: function( event, ui ) {
                            $(this).val( ui.item.label.replace(/<.*?>/g,"") );
                            $(this).closest(".commonNameDiv").next().find(".canName").val( ui.item.value );
                            $(this).closest(".commonNameDiv").next().find(".recoName").val( ui.item.value );
                            if(ui.item.languageName !== null){
                                $(this).closest(".commonNameDiv").find(".languageComboBox").val(ui.item.languageName).attr("selected",true);
                                $(this).closest(".commonNameDiv").find(".languageComboBox").data('combobox').refresh();
                            }
                            return false;
                        },open: function(event, ui) {
                            $("#commonNameSuggestions ul").removeAttr('style').css({'display': 'block','width':'300px'}); 
                        }

                    });

                });
                
                $(".recoName").click(function(){
                    $("#nameSuggestions").remove();
                    $(this).after("<div id='nameSuggestions' style='display: block;'></div>");
                    $(this).autofillNames({
                        'appendTo' : '#nameSuggestions',
                        'nameFilter':'scientificNames',
                        focus: function( event, ui ) {
                            $(this).closest(".sciNameDiv").find(".canName").val("");
                            $(this).val( ui.item.label.replace(/<.*?>/g,"") );
                            $("#nameSuggestions li a").css('border', 0);
                            return false;
                        },
                        select: function( event, ui ) {
                            $(this).val( ui.item.label.replace(/<.*?>/g,"") );
                            $(this).closest(".sciNameDiv").find(".canName").val( ui.item.value );
                            $(this).closest(".sciNameDiv").find(".mappedRecoNameForcanName").val(ui.item.label.replace(/<.*?>/g,""));
                            return false;
                        },open: function(event, ui) {
                            $("#nameSuggestions ul").removeAttr('style').css({'display': 'block','width':'300px'}); 
                        }
                    });
                    
                    $(".recoName").keypress(function() {
                        if ($(this).closest(".sciNameDiv").prev().find(".mappedRecoNameForcanName").val() !== $(this).val()) {
                            $(this).closest(".sciNameDiv").find(".canName").val('');
                        }
                    });
                });
	});

	function cancelRecoComment() {
            $('#recoComment').val('');
            $('#reco-options').hide();
            $('#reco-action').show();
	}
</r:script>
