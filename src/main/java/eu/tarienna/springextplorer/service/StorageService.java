package eu.tarienna.springextplorer.service;

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

import eu.tarienna.springextplorer.conf.Constants;
import eu.tarienna.springextplorer.conf.FileComparator;
import eu.tarienna.springextplorer.dto.FileInfoDTO;
import eu.tarienna.springextplorer.exception.FileDeleteException;
import eu.tarienna.springextplorer.exception.FileNotFoundException;
import eu.tarienna.springextplorer.exception.FileStoreException;
import eu.tarienna.springextplorer.exception.FileUnzipException;

/**
 * @author Ramanqul
 *
 */
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
     * @param relativeDirPath
     * @throws FileStoreException
     */
    public void store(MultipartFile file, String relativeDirPath) throws FileStoreException{
        Path locationDir = Paths.get(constants.getRootUploadDir() + relativeDirPath);
        
        if (!isUnderRootFolder(relativeDirPath)) {
            throw new FileStoreException("Upload to directory " + relativeDirPath + " is not allowed");
        }
        
        try {
            String newFileName = getNewFileName(file.getOriginalFilename(), locationDir);
            Files.copy(file.getInputStream(), locationDir.resolve(newFileName));
        } catch (Exception e) {
            throw new FileStoreException(e);
        }
    }
    
    
    /**
     * Returns a new file name with _num suffix if file name already exists in a directory 
     * @param fileName - uploaded file name
     * @param locationDir - file directory
     * @return
     */
    private String getNewFileName(final String fileName, Path locationDir) {
        int num = 1;

        File file = locationDir.resolve(fileName).toFile();

        while(file.exists()) {
            int dotIndex = fileName.lastIndexOf(".");
            String newFileName = null;
            
            if (dotIndex > 0) {
                String name = fileName.substring(0, dotIndex);
                String extension = "";
                if (dotIndex < fileName.length()) {
                    extension = fileName.substring(dotIndex + 1);
                }
                newFileName = name + "_" + num + "." + extension;
            } else {
                newFileName = fileName + "_" + num;
            }

            file = locationDir.resolve(newFileName).toFile();
            num++;
        }

        return file.getName();
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
                throw new FileNotFoundException(filename);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException(filename);
        }
    }

    /**Returns list of files in FileInfoDTO
     * @param relativePath
     * @return List<FileInfoDTO> - list of files 
     * @throws FileStoreException
     */
    public List<FileInfoDTO> loadFiles(String relativePath, int page) throws FileStoreException {
        if (StringUtils.isEmptyOrWhitespace(relativePath)) {
            return Collections.emptyList();
        }

        if (!isUnderRootFolder(relativePath)) {
            throw new FileStoreException("File access denined for " + relativePath);
        }
        
        
        File[] allFiles = new File(constants.getRootUploadDir() + relativePath).listFiles();
        Arrays.sort(allFiles, FILE_COMPARATOR);

        List<FileInfoDTO> resultList = new ArrayList<>();;
        
        int perPage = constants.getFilesNumPerPage();
        int begin = (page - 1) * perPage;

        for (int count = begin; count < begin + perPage; count++) {
            if (count >= allFiles.length) {
                break;
            }
            
            File file = allFiles[count];
            
            FileInfoDTO dto = new FileInfoDTO();
            dto.setName(file.getName());
            dto.setIsFile(file.isFile());
            dto.setFileSize(file.length());
            LocalDateTime modifiedDate =
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
            
            dto.setLastModified(modifiedDate);

            resultList.add(dto);
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
    
}