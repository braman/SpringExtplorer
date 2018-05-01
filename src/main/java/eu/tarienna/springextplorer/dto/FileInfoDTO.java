package eu.tarienna.springextplorer.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import eu.tarienna.springextplorer.conf.Constants;

public class FileInfoDTO {

    private String name;
    private Long fileSize;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.FULL_DATE_FORMAT)
    private LocalDateTime lastModified;
    private Boolean isFile;
    
    
    public FileInfoDTO() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsFile() {
        return isFile;
    }

    public void setIsFile(Boolean isFile) {
        this.isFile = isFile;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

}
