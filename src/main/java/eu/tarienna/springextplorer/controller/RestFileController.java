package eu.tarienna.springextplorer.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import eu.tarienna.springextplorer.conf.Constants;
import eu.tarienna.springextplorer.dto.FileInfoDTO;
import eu.tarienna.springextplorer.exception.FileDeleteException;
import eu.tarienna.springextplorer.exception.FileStoreException;
import eu.tarienna.springextplorer.exception.FileUnzipException;
import eu.tarienna.springextplorer.service.StorageService;

@RestController
@RequestMapping("api")
public class RestFileController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    StorageService storageService;

    @PostMapping("uploadfile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadFileMulti(@RequestParam MultipartFile[] files, String uploadDir) {
        for (MultipartFile file: files) {
            try {
                storageService.store(file, uploadDir);
                //unzip file
                if (StringUtils.equalsAnyIgnoreCase(file.getContentType(), 
                        Constants.MIME_APPLICATION_ZIP, 
                        Constants.MIME_APPLICATION_X_ZIP_COMPRESSED)) {
                    storageService.unzip(file, uploadDir);
                }
            } catch (FileUnzipException e) {
                log.error("Failed to unzip", e);
                throw e;
            } catch (FileStoreException e) {
                log.error("Failed to upload", e);
                throw e;
            }

            log.info("You successfully uploaded {}", file.getOriginalFilename());
        }
    }

    @RequestMapping(value = "files/**", method = RequestMethod.GET)
    public List<FileInfoDTO> getFiles(HttpServletRequest request) {
        String relativePath = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        final String prefix = "/api/files";

        relativePath = relativePath.substring(prefix.length());

        return storageService.loadFiles(relativePath);
    }

    @RequestMapping(value = "file/**", method = RequestMethod.GET)
    public ResponseEntity<Resource> getFile(HttpServletRequest request) {
        String relativePath = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        final String prefix = "/api/file/";

        relativePath = relativePath.substring(prefix.length());
        
        Resource file = storageService.loadFile(relativePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @RequestMapping(value = "delete/", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFiles(@RequestBody String [] files) {
        for (String file:files) {
            try {
                storageService.deleteFile(file);
            } catch (FileDeleteException e) {
                log.error("Failed to delete file " + file, e);
                throw e;
            }
        }
    }
}
