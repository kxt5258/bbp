package content.fileManager

import grails.converters.JSON
import org.grails.taggable.Tag

import java.io.File;
import java.io.InputStream;
import java.util.List

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import grails.converters.JSON
import static org.codehaus.groovy.grails.commons.ConfigurationHolder.config as Config
import org.springframework.http.HttpStatus
import uk.co.desirableobjects.ajaxuploader.exception.FileUploadException
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest
import uk.co.desirableobjects.ajaxuploader.AjaxUploaderService

import speciespage.ObservationService
import species.utils.Utils

class UFileController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	
	def observationService
	AjaxUploaderService ajaxUploaderService
	UFileService uFileService = new UFileService()

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [UFileInstanceList: UFile.list(params), UFileInstanceTotal: UFile.count()]
    }

	def browser = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[UFileInstanceList: UFile.list(params), UFileInstanceTotal: UFile.count()]
	}
	
	def fm = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[UFileInstanceList: UFile.list(params), UFileInstanceTotal: UFile.count()]
	}
	
    def create = {
        def UFileInstance = new UFile()
        UFileInstance.properties = params
        return [UFileInstance: UFileInstance]
    }

    def save = {
		log.debug params
	
		def UFileInstance = new UFile(params)		

        if (UFileInstance.save(flush: true)) {

			def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
			UFileInstance.setTags(tags)
			
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'UFile.label', default: 'UFile'), UFileInstance.id])}"
            redirect(action: "show", id: UFileInstance.id)
        }
        else {
            render(view: "create", model: [UFileInstanceList: UFile.list(params), UFileInstanceTotal: UFile.count()])
        }
    }
	
	def save_browser = {
		log.debug params

	try {
		
		def uFiles = uFileService.updateUFiles(params)
		redirect(action: "browser", model: [UFileInstanceList: UFile.list(params), UFileInstanceTotal: UFile.count()])
		
		
		}
		catch (Exception e) {
			e.printStackTrace();
			redirect(action: "browser", model: [UFileInstanceList: UFile.list(params), UFileInstanceTotal: UFile.count()])
			
		}
	}
	


    def show = {
        def UFileInstance = UFile.get(params.id)
        if (!UFileInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'UFile.label', default: 'UFile'), params.id])}"
            redirect(action: "list")
        }
        else {
            [UFileInstance: UFileInstance]
        }
    }

    def edit = {
        def UFileInstance = UFile.get(params.id)
        if (!UFileInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'UFile.label', default: 'UFile'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [UFileInstance: UFileInstance]
        }
    }

    def update = {
        def UFileInstance = UFile.get(params.id)
        if (UFileInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (UFileInstance.version > version) {
                    
                    UFileInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'UFile.label', default: 'UFile')] as Object[], "Another user has updated this UFile while you were editing")
                    render(view: "edit", model: [UFileInstance: UFileInstance])
                    return
                }
            }
            UFileInstance.properties = params
            if (!UFileInstance.hasErrors() && UFileInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'UFile.label', default: 'UFile'), UFileInstance.id])}"
                redirect(action: "show", id: UFileInstance.id)
            }
            else {
                render(view: "edit", model: [UFileInstance: UFileInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'UFile.label', default: 'UFile'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def UFileInstance = UFile.get(params.id)
        if (UFileInstance) {
            try {
                UFileInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'UFile.label', default: 'UFile'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'UFile.label', default: 'UFile'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'UFile.label', default: 'UFile'), params.id])}"
            redirect(action: "list")
        }
    }
	
	
	def tags = {
		log.debug params;
		render Tag.findAllByNameIlike("${params.term}%")*.name as JSON
	}

	def upload = {
		log.debug params
		try {

			File uploaded = createFile(params.qqfile)
			InputStream inputStream = selectInputStream(request)

			ajaxUploaderService.upload(inputStream, uploaded)
			
			UFile uFileInstance = new UFile()
			uFileInstance.properties = ['name':uploaded.getName(), 'path':uploaded.getPath()]
			uFileInstance.size = UFileService.getFileSize(uploaded)			
			uFileInstance.downloads = 0
			uFileInstance.save(flush:true)

			return render(text: [success:true, filePath:uFileInstance.path, fileId:uFileInstance.id, fileSize:uFileInstance.size, fileName:uFileInstance.name] as JSON, contentType:'text/json')
		} catch (FileUploadException e) {

			log.error("Failed to upload file.", e)
			return render(text: [success:false] as JSON, contentType:'text/json')
		}
	}
	
	private InputStream selectInputStream(HttpServletRequest request) {
		if (request instanceof MultipartHttpServletRequest) {
			MultipartFile uploadedFile = ((MultipartHttpServletRequest) request).getFile('qqfile')
			return uploadedFile.inputStream
		}
		return request.inputStream
	}
	
	private File createFile(String fileName) {
		File uploaded
		
		if (grailsApplication.config.speciesPortal.content.fileUploadDir) {
			File fileDir = new File(grailsApplication.config.speciesPortal.content.fileUploadDir)
			if(!fileDir.exists())
				fileDir.mkdir()
			uploaded = observationService.getUniqueFile(fileDir, Utils.cleanFileName(fileName));
			
		} else {
			uploaded = File.createTempFile('grails', 'ajaxupload')
		}
		return uploaded
	}
	
	
	def download = {
	
		UFile ufile = UFile.get(params.id)
		if (!ufile) {
			def msg = messageSource.getMessage("fileupload.download.nofile", [params.id] as Object[], request.locale)
			log.debug msg
			flash.message = msg
			redirect controller: params.errorController, action: params.errorAction
			return
		}
		
		def file = new File(ufile.path)
		if (file.exists()) {
			log.debug "Serving file id=[${ufile.id}] for the ${ufile.downloads} to ${request.remoteAddr}"
			ufile.downloads++
			ufile.save()
			response.setContentType("application/octet-stream")
			response.setHeader("Content-disposition", "${params.contentDisposition}; filename=${file.name}")
			response.outputStream << file.readBytes()
			return
		} else {
			def msg = messageSource.getMessage("fileupload.download.filenotfound", [ufile.name] as Object[], request.locale)
			log.error msg
			flash.message = msg
			redirect controller: params.errorController, action: params.errorAction
			return
		}
	}


}