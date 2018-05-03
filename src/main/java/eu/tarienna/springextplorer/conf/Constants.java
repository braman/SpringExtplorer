package eu.tarienna.springextplorer.conf;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.util.StringUtils;

@ConfigurationProperties(prefix = "eu.tarienna.springextplorer")
@Configuration("constants")
public class Constants implements ConstantsVals {

    private String rootUploadDir;
    private Integer filesNumPerPage;
    
    @PostConstruct
    private void postInit() {
        if (StringUtils.isEmptyOrWhitespace(rootUploadDir)) {
            rootUploadDir = ROOT_UPLOAD_DIR_DEFAULT;
        }
        
        if (filesNumPerPage == null || filesNumPerPage < 1) {
            filesNumPerPage = FILES_NUM_PER_PAGE_DEFAULT;
        }
    }
    
    
    public String getRootUploadDir() {
        return rootUploadDir;
    }

    public void setRootUploadDir(String rootUploadDir) {
        this.rootUploadDir = rootUploadDir;
    }

    public Integer getFilesNumPerPage() {
        return filesNumPerPage;
    }

    public void setFilesNumPerPage(Integer filesNumPerPage) {
        this.filesNumPerPage = filesNumPerPage;
    }
    
    
    
}
