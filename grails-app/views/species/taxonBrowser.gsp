<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta name="layout" content="main" />

<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<title>Taxonomy Browser</title>

<r:require modules="species_show"/>

</head>
<body>
        <div class="span12">
        <s:showSubmenuTemplate model="['entityName':'Taxonomy Browser']"/>

            <div class="taxonomyBrowser sidebar_section" style="position: relative;" data-name="classification" data-speciesid="${speciesInstance?.id}">
                <h5>Classifications</h5>	

                <div id="taxaHierarchy">

                    <%
                    def classifications = [];
                    Classification.list().each {
                    classifications.add([it.id, it, null]);
                    }
                    classifications = classifications.sort {return it[1].name};
                    %>

                    <g:render template="/common/taxonBrowserTemplate" model="['classifications':classifications, 'expandAll':false]"/>
                </div>
            </div>
            <g:render template="/species/inviteForContribution"/>

        </div>
        <g:javascript>
        var taxonRanks = [];
            <g:each in="${TaxonomyRank.list()}" var="t">
            taxonRanks.push({value:"${t.ordinal()}", text:"${t.value()}"});
            </g:each>

            </g:javascript>	

        <r:script>
        $(document).ready(function() {
            var taxonBrowser = $('.taxonomyBrowser').taxonhierarchy({
                expandAll:false
            });	
        });
        </r:script>
    </body>
</html>
