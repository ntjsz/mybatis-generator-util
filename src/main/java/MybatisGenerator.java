import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class MybatisGenerator {
    private String jdbcUrl;
    private String username;
    private String password;
    private String datasource = "unknown";
    private String schemaName;
    private String packageName;

    private void init() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileReader(getFileInClasspath("application.properties")));
        Set<String> strings = properties.stringPropertyNames();

        String projectTemplate = null;
        for (String key : strings) {
            String k = extractKey(key);
            if ("url".equals(k) || "jdbcUrl".equals(k)) {
                String value = properties.getProperty(key);
                if (value.contains("jdbc:mysql:")) {
                    projectTemplate = key;
                    this.jdbcUrl = value;
                }
            }
            if ("username".equals(k)) this.username = properties.getProperty(key);
            if ("password".equals(k)) this.password = properties.getProperty(key);
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

        context.getJavaModelGeneratorConfiguration().setTargetPackage(this.packageName);

        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        System.out.println(warnings);
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
