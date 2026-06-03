package com.example.web_monitor.configuration;

import java.util.Properties;

import javax.naming.directory.InitialDirContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;



@Configuration
@Slf4j
public class LdapConfig {


    @Value("${LDAP_PROVIDER_URL}")
    private String providerUrl;

    @Value("${LDAP_INITIAL_CONTEXT_FACTORY}")
    private String contextFactory;


    public boolean loginByLDAP(String username, String password) {
        try {
            Properties props = new Properties();
            props.put("java.naming.factory.initial", contextFactory);
            props.put("java.naming.provider.url", providerUrl);
            props.put("java.naming.security.authentication", "simple");
            props.put("java.naming.security.principal", username);
            props.put("java.naming.security.credentials", password);
            InitialDirContext context = new InitialDirContext(props);
            log.info("Login LDAP Successful: " + context.getNameInNamespace());
            return true;
        } catch (Exception var4) {
            log.error("loginByLDAP: Error connect to LDAP with Input " + username + ": " + password, var4);
            return false;
        }
    }
}
