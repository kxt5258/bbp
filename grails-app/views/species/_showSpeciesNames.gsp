<%@ page import="species.Synonyms"%>
<%@ page import="species.CommonNames"%>
<%@page import="species.participation.ActivityFeedService"%>
<%def nameRecords = fields.get(grailsApplication.config.speciesPortal.fields.NOMENCLATURE_AND_CLASSIFICATION)?.get(grailsApplication.config.speciesPortal.fields.TAXON_RECORD_NAME).collect{if(it.value && !it.key.equals('hasContent') &&  !it.key.equals('isContributor') && it.value.containsKey('speciesFieldInstance')){ return it.value.speciesFieldInstance[0]}} %>
<g:if test="${nameRecords}">
<div class="sidebar_section" style="clear:both;">
    <a class="speciesFieldHeader"  data-toggle="collapse" href="#taxonRecordName">
        <h5>Taxon Record Name</h5>
    </a>

    <div id="taxonRecordName" class="speciesField collapse in">
        <table>
            <tr class="prop">
                <td><span class="grid_3 name">${grailsApplication.config.speciesPortal.fields.SCIENTIFIC_NAME }</span></td><td> ${speciesInstance.taxonConcept.italicisedForm}</td>
            </tr>
            <g:each in="${nameRecords}">
            <g:if test="${it}">
            <tr class="prop">

                <g:if test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.REFERENCES)}">
                <td><span class="grid_3 name">${it?.field?.subCategory} </span></td> <td class="linktext">${it?.description}</td>
                </g:if> 
                <g:elseif test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.GENERIC_SPECIFIC_NAME)}">

                </g:elseif> 
                <g:elseif test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.SCIENTIFIC_NAME)}">

                </g:elseif> 
                <g:elseif test="${it?.field?.subCategory?.equalsIgnoreCase('year')}">
                <td><span class="grid_3 name">${it?.field?.subCategory} </span></td> <td> ${it?.description}</td>
                </g:elseif> 
                <g:else>
                <td><span class="grid_3 name">${it?.field?.subCategory} </span></td> <td> ${it?.description}</td>
                </g:else> 
            </tr>
            </g:if>
            </g:each>
        </table>
    </div>

    <comment:showCommentPopup model="['commentHolder':[objectType:ActivityFeedService.SPECIES_TAXON_RECORD_NAME, id:speciesInstance.id], 'rootHolder':speciesInstance]" />
</div>
<br/>
</g:if>

<!-- Synonyms -->
<%def synonyms = Synonyms.findAllByTaxonConcept(speciesInstance.taxonConcept) %>
<g:if test="${synonyms }">
<div class="sidebar_section">
    <a class="speciesFieldHeader"  data-toggle="collapse" href="#synonyms"> 
        <h5>Synonyms</h5>
    </a> 
    <ul id="synonyms" class="speciesField collapse in" style="list-style:none;overflow:hidden;margin-left:0px;">
            <g:each in="${synonyms}" var="synonym">
            <li>
            <div class="span3">
                <a href="#" class="synRel  ${isSpeciesContributor?'selector':''}" data-type="select" data-name="relationship" data-original-title="Edit Synonym Relationship">
                    ${synonym?.relationship?.value()}</a> 
            </div>
            <div class="span8">
                <a href="#" class="sci_name ${isSpeciesContributor?'editField':''}" data-type="text" data-pk="${speciesInstance.id}" data-params="{sid:${synonym.id}}" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="synonym" data-original-title="Edit synonym name" title="Click to edit">  ${(synonym?.italicisedForm)?synonym.italicisedForm:'<i>'+(synonym?.name)+'</i>'} </a>
            </div>    
            </li>
            </g:each>
            <g:if test="${isSpeciesContributor}">
            <li>
            <div class="span3">
                <a href="#" class="synRel add_selector ${isSpeciesContributor?'selector':''}" data-type="select" data-name="relationship" data-original-title="Edit Synonym Relationship"></a>
            </div>
            <div class="span8">
                <a href="#" class="addField"  data-pk="${speciesInstance.id}" data-type="text"  data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="synonym" data-original-title="Add Synonym" data-placeholder="Add Synonym"></a>
            </div>
            </li>
            </g:if>

    </ul>
    <comment:showCommentPopup model="['commentHolder':[objectType:ActivityFeedService.SPECIES_SYNONYMS, id:speciesInstance.id], 'rootHolder':speciesInstance]" />
</div>
<br/>
</g:if>

<!-- Common Names -->
<%
Map names = new LinkedHashMap();
CommonNames.findAllByTaxonConcept(speciesInstance.taxonConcept).each() {
String languageName = it?.language?.name ?: "Others";

if(it?.language?.isDirty) {
languageName = "Others";	
}
if(!names.containsKey(languageName)) {
names.put(languageName, new ArrayList());
}
names.get(languageName).add(it)
};

names = names.sort();
names.each { key, list ->
list.sort();						
}

%>
<g:if test="${names}">
<div class="sidebar_section">
    <a class="speciesFieldHeader" data-toggle="collapse" href="#commonNames"><h5> Common Names</h5></a> 
    <div id="commonNames" class="speciesField collapse in">

        <table>
            <g:each in="${names}">
            <tr><td class="prop">
                    <span class="grid_3 name">${it.key} </span></td> 
                <td><g:each in="${it.value}"  status="i" var ="n">
                    <g:if test="${n.language?.isDirty}">${n.language.name+ " : "} </g:if>${n.name}<g:if test="${i < it.value.size()-1}">,</g:if>
                    </g:each></td>
                </tr>
                </g:each>
            </table>
        </div>
        <comment:showCommentPopup model="['commentHolder':[objectType:ActivityFeedService.SPECIES_COMMON_NAMES, id:speciesInstance.id], 'rootHolder':speciesInstance]" />
    </div>
    <br/>
    </g:if>
    <!-- Common Names End-->

