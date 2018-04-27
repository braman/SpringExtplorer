package kz.ramanqul.upwork.mark_joachim.SpringExtplorer.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import kz.ramanqul.upwork.mark_joachim.SpringExtplorer.conf.Constants;
import kz.ramanqul.upwork.mark_joachim.SpringExtplorer.dto.FileInfoDTO;
import kz.ramanqul.upwork.mark_joachim.SpringExtplorer.exception.FileDeleteException;
import kz.ramanqul.upwork.mark_joachim.SpringExtplorer.exception.FileStoreException;
import kz.ramanqul.upwork.mark_joachim.SpringExtplorer.exception.FileUnzipException;

@Service
public class StorageService {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final FileComparator FILE_COMPARATOR = new FileComparator();
	
	@Autowired
	private Constants constants;
	
	
	/**Unzips file to relative folder path
	 * @param file
	 * @param relativeDestDir
	 * @throws FileStoreException
	 */
	public void unzip(MultipartFile file, String relativeDestDir) throws FileStoreException {
	    if (!isUnderRootFolder(relativeDestDir)) {
	        throw new FileStoreException("Upload to directory " + relativeDestDir + " is not allowed");
	    }

	    Path distLocation = Paths.get(constants.getRootUploadDir() + relativeDestDir);
	    
	    File dir = distLocation.toFile();
        // create output directory if it doesn't exist
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            ZipInputStream zis = new ZipInputStream(file.getInputStream());
            ZipEntry ze = zis.getNextEntry();
            
            while(ze != null) {
                String fileName = ze.getName();
                File newFile = new File(dir.getAbsolutePath() + File.separator + fileName);
                log.info("Unzipping to {} ...", newFile.getAbsolutePath());
                //create directories for sub directories in zip
                
                if (ze.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    newFile.getParentFile().mkdirs();
                    
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
        } catch (IOException e) {
            throw new FileUnzipException(e);
        }
    }
	
	private boolean isUnderRootFolder(String relativePath) {
	    boolean needSlash = !relativePath.startsWith("/");
	    
	    Path outputDirectory = Paths.get(constants.getRootUploadDir());
	    Path inputPath =  Paths.get(constants.getRootUploadDir() + (needSlash ? "/" : "") + relativePath);
	    
	    return inputPath.toAbsolutePath().startsWith(outputDirectory.toAbsolutePath());
	}
	
	/** Saves uploaded file unders root folder
	 * @param file
	 * @param relativePath
	 * @throws FileStoreException
	 */
	public void store(MultipartFile file, String relativePath) throws FileStoreException{
	    Path rootLocation = Paths.get(constants.getRootUploadDir() + relativePath);
	    
	    if (!isUnderRootFolder(relativePath)) {
	        throw new FileStoreException("Upload to directory " + relativePath + " is not allowed");
	    }
	    
	    try {
            Files.copy(file.getInputStream(), rootLocation.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
        	throw new FileStoreException(e);
        }
	}

    /**Returns Resource reference object to file
     * @param filename
     * @return
     * @throws FileStoreException
     */
    public Resource loadFile(String filename) throws FileStoreException {
        if (!isUnderRootFolder(filename)) {
            throw new FileStoreException("File access denined for " + filename);
        }
        
        Path rootLocation = Paths.get(constants.getRootUploadDir());
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
            	throw new RuntimeException("FAIL!");
            }
        } catch (MalformedURLException e) {
        	throw new RuntimeException("FAIL!");
        }
    }

    /**Returns list of files in FileInfoDTO
     * @param relativePath
     * @return List<FileInfoDTO> - list of files 
     * @throws FileStoreException
     */
    public List<FileInfoDTO> loadFiles(String relativePath) throws FileStoreException {
        if (StringUtils.isEmptyOrWhitespace(relativePath)) {
            return Collections.emptyList();
        }

        if (!isUnderRootFolder(relativePath)) {
            throw new FileStoreException("File access denined for " + relativePath);
        }
        
        List<FileInfoDTO> resultList = null;
        
        File[] files = new File(constants.getRootUploadDir() + relativePath).listFiles();
        //If this pathname does not denote a directory, then listFiles() returns null. 

        
        if (files != null && files.length > 0) {
            Arrays.sort(files, FILE_COMPARATOR);

            resultList = new ArrayList<>();
            
            for (File file : files) {
                FileInfoDTO dto = new FileInfoDTO();
                dto.setName(file.getName());
                dto.setIsFile(file.isFile());
                dto.setFileSize(file.length());
                LocalDateTime modifiedDate =
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
                
                dto.setLastModified(modifiedDate);

                resultList.add(dto);
            }
            
        }
        
        if (resultList == null || resultList.isEmpty()) {
            return Collections.emptyList();
        }
        

        return resultList;
    }
    
    
    /** Deletes file unders root folder
     * @param relativePath
     * @throws FileStoreException
     */
    public void deleteFile(String relativePath) throws FileStoreException {
        if (!isUnderRootFolder(relativePath)) {
            throw new FileStoreException("File access denined for " + relativePath);
        } 
        
        try {
            FileUtils.forceDelete(new File(constants.getRootUploadDir() + relativePath));
        } catch (IOException e) {
            log.error("Failed to delete a file " + relativePath, e);
            throw new FileDeleteException(relativePath);
        }
    }

    /**
     * Creates root folder if doesn't exist
     */
    public void init() {
        new File(constants.getRootUploadDir()).mkdirs();
    }
    
    /**
     * Compares files and priorities folders as first items
     *
     */
    private class FileComparator implements Comparator<File> {

        @Override
        public int compare(File o1, File o2) {
            if (o1 != null && o1.isDirectory() ) {
                return -1;
            }
            
            if (o2 != null && o2.isDirectory()) {
                return 1;
            }
            
            return 0;
        }
    }
}