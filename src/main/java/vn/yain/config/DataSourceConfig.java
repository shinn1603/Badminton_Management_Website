package vn.yain.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Value("${MYSQL_URL:}")
    private String mysqlUrl;

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Value("${MYSQL_PRIVATE_URL:}")
    private String mysqlPrivateUrl;

    @Value("${MYSQL_PUBLIC_URL:}")
    private String mysqlPublicUrl;

    @Value("${DATABASE_PUBLIC_URL:}")
    private String databasePublicUrl;

    @Value("${MYSQLHOST:}")
    private String mysqlHost;

    @Value("${MYSQLPORT:}")
    private String mysqlPort;

    @Value("${MYSQLUSER:}")
    private String mysqlUser;

    @Value("${MYSQLPASSWORD:}")
    private String mysqlPassword;

    @Value("${MYSQLDATABASE:}")
    private String mysqlDatabase;

    private String clean(String val) {
        if (val == null) return null;
        String trimmed = val.trim();
        // Remove surrounding double quotes or single quotes if present
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) ||
            (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            if (trimmed.length() >= 2) {
                trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            }
        }
        return trimmed;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        String finalUrl = clean(datasourceUrl);
        String finalUsername = clean(datasourceUsername);
        String finalPassword = clean(datasourcePassword);

        // Check if any Railway standard MySQL URL is available
        String candidateUrl = null;
        if (clean(mysqlUrl) != null && !clean(mysqlUrl).isBlank()) {
            candidateUrl = clean(mysqlUrl);
        } else if (clean(databaseUrl) != null && !clean(databaseUrl).isBlank() && clean(databaseUrl).startsWith("mysql://")) {
            candidateUrl = clean(databaseUrl);
        } else if (clean(mysqlPrivateUrl) != null && !clean(mysqlPrivateUrl).isBlank()) {
            candidateUrl = clean(mysqlPrivateUrl);
        } else if (clean(databasePublicUrl) != null && !clean(databasePublicUrl).isBlank() && clean(databasePublicUrl).startsWith("mysql://")) {
            candidateUrl = clean(databasePublicUrl);
        } else if (clean(mysqlPublicUrl) != null && !clean(mysqlPublicUrl).isBlank()) {
            candidateUrl = clean(mysqlPublicUrl);
        }

        // If datasourceUrl was passed directly as a raw "mysql://" format
        if (finalUrl != null && finalUrl.startsWith("mysql://")) {
            candidateUrl = finalUrl;
            finalUrl = null;
        }

        if ((finalUrl == null || finalUrl.isBlank() || finalUrl.contains("localhost")) && candidateUrl != null && !candidateUrl.isBlank()) {
            try {
                log.info("Detected Railway MySQL URL format: parsing connection parameters...");
                URI uri = new URI(candidateUrl.replace("mysql://", "http://"));
                String parsedHost = uri.getHost();
                int parsedPort = uri.getPort() == -1 ? 3306 : uri.getPort();
                String path = uri.getPath();
                String dbName = (path != null && path.length() > 1) ? path.substring(1) : "railway";
                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    if (userInfo.length > 0 && !userInfo[0].isBlank()) finalUsername = userInfo[0];
                    if (userInfo.length > 1 && !userInfo[1].isBlank()) finalPassword = userInfo[1];
                }
                finalUrl = String.format("jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useSSL=false", parsedHost, parsedPort, dbName);
            } catch (Exception e) {
                log.warn("Could not parse candidate MySQL URL, fallback to default datasource properties: {}", e.getMessage());
            }
        } else if ((finalUrl == null || finalUrl.isBlank() || finalUrl.contains("localhost")) && clean(mysqlHost) != null && !clean(mysqlHost).isBlank()) {
            log.info("Detected Railway MYSQLHOST: configuring MySQL DataSource directly...");
            String host = clean(mysqlHost);
            String port = clean(mysqlPort);
            if (port == null || port.isBlank()) port = "3306";
            String db = clean(mysqlDatabase);
            if (db == null || db.isBlank()) db = "railway";
            finalUrl = String.format("jdbc:mysql://%s:%s/%s?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useSSL=false", host, port, db);
            if (clean(mysqlUser) != null) finalUsername = clean(mysqlUser);
            if (clean(mysqlPassword) != null) finalPassword = clean(mysqlPassword);
        }

        log.info("Connecting to Database: URL=[{}], User=[{}]", finalUrl, finalUsername);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(finalUrl);
        config.setUsername(finalUsername);
        config.setPassword(finalPassword);

        if (finalUrl != null && finalUrl.startsWith("jdbc:mysql:")) {
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else if (finalUrl != null && finalUrl.startsWith("jdbc:sqlserver:")) {
            config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } else if (finalUrl != null && finalUrl.startsWith("jdbc:h2:")) {
            config.setDriverClassName("org.h2.Driver");
        }

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}
