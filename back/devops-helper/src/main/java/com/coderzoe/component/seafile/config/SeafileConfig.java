package com.coderzoe.component.seafile.config;

import com.coderzoe.common.enums.ArchiveFileType;
import com.coderzoe.component.seafile.model.ArchiveNameMatch;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;
import java.util.Set;

import static com.coderzoe.common.Constants.BASE_DIR_START_CHAR;

/**
 * @author yinhuasheng
 * @date 2024/8/20 10:38
 */
@ConfigurationProperties(prefix = "seafile")
@Configuration
@Setter
public class SeafileConfig {
    @Getter
    private String baseUrl = "http://10.254.7.209";
    @Getter
    private String userName;
    private String password;
    @Getter
    private String repositoryId = "46850dcf-bc23-4652-a251-fcced38a97a8";

    private String basePath;
    /**
     * 归档目录
     */
    @Getter
    private String archiveBasePath = "final-dh";

    /**
     * 归档的内容
     */
    @Getter
    private Set<ArchiveFileType> archiveTypes;

    /**
     * 不参与归档的目录名
     */
    @Getter
    private Set<ArchiveNameMatch> archiveIgnorePaths;

    /**
     * 参与归档的SQL
     */
    @Getter
    private Set<ArchiveNameMatch> archiveSqlNames;

    private String loginTokenUrl = "/accounts/login/";
    private String loginUrl = "/accounts/login/";

    private String baseDirectoryUrl = "/api/v2.1/repos/" + repositoryId + "/dir/";

    private String baseFileUrl = "/api/v2.1/repos/" + repositoryId + "/file/";

    @Getter
    private String suffix = "&with_thumbnail=true";

    private String baseDownloadFileUrl = "/lib/" + repositoryId + "/file";

    private String baseUploadUrl = "/api2/repos/" + repositoryId + "/upload-link/?p=";
    @Getter
    private String baseUploadSuffix = "&from=web";

    private String deleteDirUrl = "/api/v2.1/repos/batch-delete-item/";


    public String getPassword() {
        return new String(Base64.getDecoder().decode(this.password));
    }

    public String getLoginTokenUrl() {
        return baseUrl + loginTokenUrl;
    }

    public String getLoginUrl() {
        return baseUrl + loginUrl;
    }

    public String getBaseDirectoryUrl() {
        return baseUrl + baseDirectoryUrl;
    }

    public String getBaseFileUrl() {
        return baseUrl + baseFileUrl;
    }

    public String getBaseDownloadFileUrl() {
        return baseUrl + baseDownloadFileUrl;
    }

    public String getBaseUploadUrl() {
        return baseUrl + baseUploadUrl;
    }

    public String getDeleteDirUrl() {
        return baseUrl + deleteDirUrl;
    }

    public boolean isArchive(ArchiveFileType type) {
        return archiveTypes != null && archiveTypes.contains(type);
    }

    public String getBasePath() {
        if (this.basePath.startsWith(BASE_DIR_START_CHAR)) {
            return basePath;
        } else {
            return BASE_DIR_START_CHAR + basePath;
        }
    }
}
