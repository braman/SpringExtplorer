package eu.tarienna.springextplorer.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "kz.ramanqul.upwork.markjoachim")
@Configuration("constants")
public class Constants {

    public static final String MIME_APPLICATION_X_ZIP_COMPRESSED = "application/x-zip-compressed";
    public static final String MIME_APPLICATION_ZIP = "application/zip";
    public static final String FULL_DATE_FORMAT = "dd-MM-yyyy hh:mm:ss";
    
    private String rootUploadDir;

    public String getRootUploadDir() {
        return rootUploadDir;
    }

    public void setRootUploadDir(String rootUploadDir) {
        this.rootUploadDir = rootUploadDir;
    }
    
    
    
}
