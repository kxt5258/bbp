package species.participation

import org.apache.commons.logging.LogFactory;

import species.ScientificName
import species.TaxonomyDefinition;
import species.Synonyms;
import species.NamesMetadata;
import species.TaxonomyRegistry
import species.Classification;
import species.Species;
import species.ScientificName.TaxonomyRank
import species.Synonyms;
import species.CommonNames;

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.XML
import groovy.sql.Sql
import groovy.util.XmlParser
import grails.converters.JSON;

class NamelistService {
   
    private static final String COL_SITE = 'http://www.catalogueoflife.org'
	private static final String COL_URI = '/annual-checklist/2014/webservice'
    
    private static final String GBIF_SITE = 'http://api.gbif.org'
	private static final String GBIF_URI = '/v1/species'
	
	private static final int BATCH_SIZE = 100
	private static final log = LogFactory.getLog(this);

	private static final String ACCEPTED_NAME = "accepted name"
	private static final String SYNONYM = "synonym"
	private static final String PROV_ACCEPTED_NAME = "provisionally accepted name"
	private static final String COMMON_NAME = "common name"
	private static final String AMBI_SYN_NAME = "ambiguous synonym"
	private static final String MIS_APP_NAME = "misapplied name"

	def dataSource
	
    List searchCOL(String input, String searchBy){
        //http://www.catalogueoflife.org/col/webservice?name=Tara+spinosa

        def http = new HTTPBuilder()
        http.request( COL_SITE, GET, TEXT ) { req ->
            uri.path = COL_URI
            if(searchBy == 'name') {
                uri.query = [ name:input, response:'full', format:'xml']
            } else if(searchBy == 'id') {
                uri.query = [ id:input, response:'full', format:'xml']
            }
            //headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
            headers.Accept = 'text/xml'

            response.success = { resp, reader ->
                assert resp.statusLine.statusCode == 200
                println "Got response: ${resp.statusLine}"
                println "Content-Type: ${resp.headers.'Content-Type'}"
                def xmlText =  reader.text
                //println xmlText
                return responseAsMap(xmlText, searchBy);
            }
            response.'404' = { println 'Not found' }
        }
    }


    List searchGBIF(String input, String searchBy){
        //http://api.gbif.org/v1/species/match?verbose=true&name=Mangifera

        def http = new HTTPBuilder()
        println "========GBIF SITE===== " + GBIF_SITE
        http.request( GBIF_SITE, GET, TEXT ) { req ->
            if(searchBy == 'name') {
                uri.path = GBIF_URI + '/match';
            } else {
                uri.path = GBIF_URI + '/' + input;
            }
            if(searchBy == 'name') {
                uri.query = [ name:input]
            }
            /*else if(searchBy == 'id') {
                uri.query = [ id:input, format:'xml']
            }*/
            //headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
            headers.Accept = '*/*'

            response.success = { resp, reader ->
                assert resp.statusLine.statusCode == 200
                println "Got response: ${resp.statusLine}"
                println "Content-Type: ${resp.headers.'Content-Type'}"
                def xmlText =  reader.text
                return responseFromGBIFAsMap(xmlText, searchBy);
            }
            response.'404' = { println 'Not found' }
        }
    }


	List responseAsMap(String xmlText, String searchBy){
		def results = new XmlParser().parseText(xmlText)
		return responseAsMap(results, searchBy)
	}
	
	
	List responseAsMap(results, String searchBy){
		List finalResult = []
        //println results.'@total_number_of_results'
        //println results.'@number_of_results_returned'
        //println results.'@error_message'
        //println results.'@version'

        int i = 0
        results.result.each { r ->
            Map temp = new HashMap()
            Map id_details = new HashMap()
            //println " Starting result ======= ${++i}"
            //println r.id.text().getClass()
            temp['externalId'] = r.id.text()
            //println r.name.text()
            temp['name'] = r.name.text()
            //println r.rank.text()
            temp['rank'] = r.rank.text().toLowerCase()
            temp[r.rank.text().toLowerCase()] = r.name.text()
            //println r.name_status.text()
            id_details[r.name.text()] = r.id.text();
            temp['nameStatus'] = r.name_status.text().tokenize(' ')[0]
            //println r.author.text()
            temp['authorString'] = r.author.text()
            //println r.source_database.text()
            temp['sourceDatabase'] = r.source_database.text()
            //println r.source_database_url.text()
            //println r.url.text()
            //println "==GETTING GROUP==" 
			
            temp['group'] = r.classification.taxon[0].name.text()
            //println r.classification.taxon[0].name.text()

            if(searchBy == 'id') {
                //println "============= references  "
                r.references.reference.each { ref ->
                    //println ref.author.text()
                    //println ref.source.text()
                }

                println "============= higher taxon  "
                r.classification.taxon.each { t ->
                    //println t.rank.text() + " == " + t.name.text()
                    temp[t.rank.text().toLowerCase()] = t.name.text()
                    id_details[t.name.text()] = t.id.text()
                }

                println "============= child taxon  "
                r.child_taxa.taxon.each { t ->
                   // println t.name.text()
                   // println t.author.text()
                }

                println "============= synonyms  "
                r.synonyms.synonym.each { s ->
                    //println s.rank.text() + " == " + s.name.text()
                    //println "============= references  "
                    s.references.reference.each { ref ->
                        //println ref.author.text()
                        //println ref.source.text()
                    }
                }

            }
            temp['id_details'] = id_details
            finalResult.add(temp);
			
            println "============End result========"
        }
        return finalResult
    }

    List responseFromGBIFAsMap(String xmlText , String searchBy) {
        def result = JSON.parse(xmlText)
        def finalResult = []
        Map temp = new HashMap()
        temp['externalId'] = result['usageKey'];
        temp['name'] = result['scientificName'];
        temp['rank'] = result['rank'].toLowerCase();
        temp['nameStatus'] = '';
        temp['sourceDatabase'] = '';
        temp['group'] = result['kingdom'];
        if(searchBy == 'id') {
            temp['externalId'] = result['key'];
            temp['kingdom'] = result['kingdom']; 
            temp['phylum'] = result['phylum']; 
            temp['order'] = result['order']; 
            temp['family'] = result['family']; 
            temp['class'] = result['class']; 
            temp['genus'] = result['kingdom']; 
            temp['species'] = result['species']; 
            temp['nameStatus'] = result['taxonomicStatus'].toLowerCase();
            temp['sourceDatabase'] = result['accordingTo'];
            temp['authorString'] = result['authorship'];
        }
        finalResult.add(temp);
        println "===========PARSED RESULT ======== " + finalResult
        return finalResult;
    }

    def getNamesFromTaxon(params){
        log.debug params
        def sql = new Sql(dataSource)
        def sqlStr, rs
        def classSystem = params.classificationId.toLong()
        def parentId = params.parentId
        def limit = params.limit ? params.limit.toInteger() : 1000
        def offset = params.offset ? params.limit.toLong() : 0
        if(!parentId) {
            sqlStr = "select t.id as taxonid, t.rank as rank, t.name as name, s.path as path, ${classSystem} as classificationid, position as position \
                from taxonomy_registry s, \
                taxonomy_definition t \
                where \
                s.taxon_definition_id = t.id and "+
                (classSystem?"s.classification_id = :classSystem and ":"")+
                "t.rank = 0";
            rs = sql.rows(sqlStr, [classSystem:classSystem])
        } else {
            sqlStr = "select t.id as taxonid, t.rank as rank, t.name as name,  s.path as path , ${classSystem} as classificationid, position as position \
                from taxonomy_registry s, \
                taxonomy_definition t \
                where \
                s.taxon_definition_id = t.id and "+
                (classSystem?"s.classification_id = :classSystem and ":"")+
                "s.path like '"+parentId+"%' " +
                "order by t.rank, t.name asc limit :limit offset :offset";
            rs = sql.rows(sqlStr, [classSystem:classSystem, limit:limit, offset:offset])
        }

        println "total result size === " + rs.size()

        def dirtyList = []
        def workingList = []
        def cleanList = []

        rs.each {
            if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.DIRTY.value())){
                dirtyList << it
            }else if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.WORKING.value())){
                workingList << it
            }else{
                cleanList << it
            }
        }		
        return [dirtyList:dirtyList, workingList:workingList, cleanList:cleanList]	
    }

    def getNameDetails(params){
        log.debug params
        def taxonDef = TaxonomyDefinition.read(params.taxonId.toLong())
        def taxonReg = TaxonomyRegistry.findByClassificationAndTaxonDefinition(Classification.read(params.classificationId.toLong()), taxonDef);
        def result = taxonDef.fetchGeneralInfo()
        result['taxonId'] = params.taxonId;
	
        if(taxonReg) {
            result['taxonRegId'] = taxonReg.id?.toString()
            taxonReg.path.tokenize('_').each { taxonDefinitionId ->
                def td = TaxonomyDefinition.get(Long.parseLong(taxonDefinitionId));
                result.put(TaxonomyRank.getTRFromInt(td.rank).value().toLowerCase(), td.name);
            }
        }
        result['synonymsList'] = getSynonymsOfTaxon(taxonDef);
        result['commonNamesList'] = getCommonNamesOfTaxon(taxonDef);
        def counts = getObvCKLCountsOfTaxon(taxonDef);
        result['countObv'] = counts['countObv'];
        result['countCKL'] = counts['countCKL'];
        result['countSp'] = getSpeciesCountOfTaxon(taxonDef);
        println "=========COUNTS============= " + counts
        return result
    }
	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// COL Migration related /////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	
	def populateInfoFromCol(File sourceDir){
		if(!sourceDir.exists()){
			log.debug "Source dir does not exist. ${sourceDir} Aborting now..." 
			return
		}
		
		addNameToIBPHirarchyFromCol(new File(sourceDir, TaxonomyDefinition.class.simpleName), TaxonomyDefinition.class)
		//addNameToIBPHirarchyFromCol(new File(sourceDir, Synonyms.class.simpleName), Synonyms.class)
	}
	
	
	private void addNameToIBPHirarchyFromCol(File domainSourceDir, domainClass){
		if(!domainSourceDir.exists()){
			log.debug "Source dir does not exist. ${domainSourceDir} Aborting now..."
			return
		}

		long offset = 0
		int i = 0
		while(true && (offset == 0)){
			List tds = domainClass.list(max: BATCH_SIZE, offset: offset, sort: "rank", order: "desc")
			tds.each {
				log.debug  it.rank +  "    " + it.id + "   " +  it.canonicalForm
			}
			if(tds.isEmpty()){
				break
			}
			offset += BATCH_SIZE
			tds.each {
				log.debug  "===== starting " + it.canonicalForm + "   index >>>>>> " + (++i)
				processColData(new File(domainSourceDir, "" + it.id + ".xml"), it)
			}
		}
	}
	
	private void processColData(File f, ScientificName sciName){
		if(!f.exists()){
			log.debug "File not found for sciName ${sciName} skipping now..."
			return
		}
		def results = new XmlParser().parse(f)
		
		String errMsg = results.'@error_message'
		int resCount = Integer.parseInt((results.'@total_number_of_results').toString()) 
		if(errMsg != ""){
			log.debug "Error in col response " + errMsg
			return
		}
		
		if(resCount != 1 ){
			log.debug "Multiple result found [${resCount}]. so skipping this ${sciName} for manual curation"
			return
		}
		
		//Every thing is fine so now populating CoL info
		def m = responseAsMap(results, "id")
		
		log.debug "================   Response map   =================="
		log.debug m
		log.debug "================   Response map   =================="
	}
	
	

    def getSynonymsOfTaxon(TaxonomyDefinition taxonConcept) {
        def res = Synonyms.findAllByTaxonConcept(taxonConcept);
        def result = []
        res.each {
            def temp = [:]
            temp['id'] = it.id.toString();
            temp['name'] = it.name;
            temp['source'] = it.viaDatasource;
            String contri = '';
            it.contributors.each {
                contri += it.name + ", "
            }
            if(contri != '') {
                contri = contri.substring(0,contri.lastIndexOf(','));
            }
            temp['contributors'] = contri; 
            println "======TEMP ==== " +temp
            result.add(temp);
        }
        return result
    }

    def getCommonNamesOfTaxon(TaxonomyDefinition taxonConcept) {
        def res = CommonNames.findAllByTaxonConcept(taxonConcept);
        def result = []
        res.each {
            def temp = [:]
            println "======TEMP ==== " +temp
            temp['id'] = it.id.toString();
            temp['name'] = it.name;
            temp['source'] = it.viaDatasource;
            String contri = '';
            it.contributors.each {
                contri += it.name + ", "
            }
            if(contri != '') {
                contri = contri.substring(0,contri.lastIndexOf(','));
            }
            temp['contributors'] = contri; 
            println "======TEMP ==== " +temp
            result.add(temp);
        }
        return result
    }

    def getObvCKLCountsOfTaxon(TaxonomyDefinition taxonConcept) {
        def sql = new Sql(dataSource)
        def sqlStr;
        def countObv = 0, countCKL = 0;
        sqlStr = "select count(distinct o.id) from recommendation rec, recommendation_vote rv, observation o where rec.id = rv.recommendation_id and rv.observation_id = o.id and rec.taxon_concept_id ="+ taxonConcept.id.toString() +" and o.is_checklist = false and o.id = o.source_id";

        countObv =  sql.rows(sqlStr)[0].count
        println "=======COUNT OBV======== " + countObv

        sqlStr = "select count(distinct o.id) from recommendation rec, recommendation_vote rv, observation o where rec.id = rv.recommendation_id and rv.observation_id = o.id and rec.taxon_concept_id ="+ taxonConcept.id.toString() +" and o.is_checklist = true and o.id != o.source_id";

        countCKL =  sql.rows(sqlStr)[0].count
        println "=======COUNT CKL======== " + countCKL
        println "=====COUNTS=== " +countObv +"==== " + countCKL
        return ['countObv': countObv, 'countCKL': countCKL];
    }

    def getSpeciesCountOfTaxon(TaxonomyDefinition taxonConcept) {
        def taxonId = taxonConcept.id.toString();
        def sql = new Sql(dataSource)
        String sqlStr = """select * 
        from taxonomy_registry
        where 
        path like '%!_"""+taxonId+"' escape '!'";
        
        def res1 = sql.rows(sqlStr)
        sqlStr = """select * 
        from taxonomy_registry
        where 
        path like '%!_"""+taxonId+"!_%"+"' escape '!'";
        
        def res2 = sql.rows(sqlStr);

        sqlStr = """select * 
        from taxonomy_registry
        where 
        path like '"""+taxonId+"!_%"+"' escape '!'";
        
        def res3 = sql.rows(sqlStr);

        def taxonConcepts = res1.collect {TaxonomyDefinition.read(it.taxon_definition_id.toLong())};
        taxonConcepts.add(res2.collect {TaxonomyDefinition.read(it.taxon_definition_id.toLong())});
        taxonConcepts.add(res3.collect {TaxonomyDefinition.read(it.taxon_definition_id.toLong())});
        def speciesCount = Species.findAllByTaxonConceptInList(taxonConcepts).size();
        return speciesCount;
    }
}