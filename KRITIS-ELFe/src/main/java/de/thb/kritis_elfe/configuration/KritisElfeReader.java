package de.thb.kritis_elfe.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Class for defining all important data of the Webapplication KRITIS-ELFe
 */
@Component("kritisElfeReader")
@ConfigurationProperties("kritiselfe")
public class KritisElfeReader {
    private String helpPath;
    private String url;

    public String getHelpPath() {
        return helpPath;
    }

    public void setHelpPath(String helpPath) {
        this.helpPath = helpPath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
