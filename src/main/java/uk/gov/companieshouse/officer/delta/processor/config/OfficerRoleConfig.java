package uk.gov.companieshouse.officer.delta.processor.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "officer-role-ordinal")
public class OfficerRoleConfig {
    
    private Map<String, Integer> nonResigned;
    private Map<String, Integer> resigned;

    public OfficerRoleConfig(Map<String, Integer> nonResigned, Map<String, Integer> resigned) {
        this.nonResigned = nonResigned;
        this.resigned = resigned;
    }

    public Map<String, Integer> getNonResigned() {
        return nonResigned;
    }

    public void setNonResigned(Map<String, Integer> nonResigned) {
        this.nonResigned = nonResigned;
    }

    public Map<String, Integer> getResigned() {
        return resigned;
    }

    public void setResigned(Map<String, Integer> resigned) {
        this.resigned = resigned;
    }

}
