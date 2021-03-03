import com.google.common.base.Strings;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.*;

public class MybatisGenerator {
    private String jdbcUrl;
    private String username;
    private String password;
    private String datasource = "unknown";
    private String schemaName;
    private String packageName;

    private String tableName;

    private void init() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileReader(getFileInClasspath("application.properties")));
        Set<String> strings = properties.stringPropertyNames();

        String projectTemplate = null;
        for (String key : strings) {
            String k = extractKey(key);
            switch (k) {
                case "url":
                case "jdbcUrl": {
                    String value = properties.getProperty(key);
                    if (value.contains("jdbc:mysql:")) {
                        projectTemplate = key;
                        this.jdbcUrl = handleJdbcUrl(value);
                    }
                }
                break;

                case "username":
                    this.username = properties.getProperty(key);
                    break;

                case "password":
                    this.password = properties.getProperty(key);
                    break;

                case "tableName":
                    this.tableName = properties.getProperty(key);
                    break;
            }
        }


        int end = this.jdbcUrl.indexOf('?');
        int start = this.jdbcUrl.lastIndexOf('/', end);
        this.schemaName = jdbcUrl.substring(start + 1, end);

        if (projectTemplate != null) {
            end = projectTemplate.lastIndexOf('.');
            String datasourceStr = "datasource.";
            start = projectTemplate.indexOf(datasourceStr) + datasourceStr.length();
            this.datasource = projectTemplate.substring(start, end);
        }

        this.packageName = this.datasource + "." + this.schemaName.replaceAll("_", "");

        if (Strings.isNullOrEmpty(this.tableName.trim())) {
            this.tableName = "%";
        }
    }

    private String handleJdbcUrl(String rawUrl) {
        return rawUrl.replaceAll("&amp;", "&");
    }


    private String extractKey(String propertyKey) {
        int start = propertyKey.lastIndexOf('.');

        if (start < 0) {
            return propertyKey;
        } else {
            return propertyKey.substring(start + 1);
        }
    }

    private File getFileInClasspath(String fileName) throws Exception {
        URL resource = this.getClass().getResource(fileName);
        return new File(resource.toURI());
    }

    public void generate() throws Exception {
        System.out.println(this.toString());
        System.out.println("start generating.....");

        List<String> warnings = new ArrayList<>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(getFileInClasspath("generatorConfig.xml"));
        Context context = config.getContext("simple");

        JDBCConnectionConfiguration jdbcConnection = context.getJdbcConnectionConfiguration();
        jdbcConnection.setConnectionURL(this.jdbcUrl);
        jdbcConnection.setUserId(this.username);
        jdbcConnection.setPassword(this.password);

        context.getJavaModelGeneratorConfiguration().setTargetPackage("entity." + this.packageName);
        context.getSqlMapGeneratorConfiguration().setTargetPackage(this.packageName);

        context.getTableConfigurations().get(0).setTableName(this.tableName);

        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        if (!warnings.isEmpty()) {
            System.out.println(warnings);
        }
    }

    public void run() throws Exception {
        init();
        generate();
    }

    @Override
    public String toString() {
        return "MybatisGenerator{" +
                "jdbcUrl='" + jdbcUrl + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", datasource='" + datasource + '\'' +
                ", schemaName='" + schemaName + '\'' +
                ", packageName='" + packageName + '\'' +
                '}';
    }

    public static void main(String[] args) throws Exception {
        MybatisGenerator generator = new MybatisGenerator();
        generator.run();
    }
}
