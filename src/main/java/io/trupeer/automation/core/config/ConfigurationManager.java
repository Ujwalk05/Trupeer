package io.trupeer.automation.core.config;

import io.trupeer.automation.core.exceptions.FrameworkException;

import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads config for the chosen environment (-Denvironment=qa, default qa). Value
 * precedence: file &lt; ${ENV_VAR} interpolation &lt; -D system property.
 */
public final class ConfigurationManager {

    private static volatile ConfigurationManager instance;
    private static final Pattern ENV_PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    private final Properties props = new Properties();
    private final String environment;

    private ConfigurationManager() {
        this.environment = System.getProperty("environment", "qa");
        String resource = "/config/" + environment + ".properties";
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            if (in == null) {
                throw new FrameworkException("No configuration file on classpath: " + resource
                        + " (check your -Denvironment value)");
            }
            props.load(in);
        } catch (Exception e) {
            throw new FrameworkException("Failed to load configuration: " + resource, e);
        }
    }

    public static ConfigurationManager get() {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) instance = new ConfigurationManager();
            }
        }
        return instance;
    }

    
    public String get(String key) {
        String value = System.getProperty(key, props.getProperty(key));
        if (value == null) {
            throw new FrameworkException("Missing config key: '" + key
                    + "' for environment '" + environment + "'");
        }
        return interpolate(value);
    }

    public String get(String key, String defaultValue) {
        String value = System.getProperty(key, props.getProperty(key, defaultValue));
        return value == null ? null : interpolate(value);
    }

    private String interpolate(String value) {
        Matcher m = ENV_PLACEHOLDER.matcher(value);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String envVar = m.group(1);
            String resolved = System.getenv(envVar);
            if (resolved == null) {
                throw new FrameworkException("Environment variable '" + envVar
                        + "' referenced in config is not set. See README / .env.example.");
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(resolved));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public String environment()   { return environment; }
    public String baseUrl()       { return get("base.url"); }
    public String loginUrl()      { return get("login.url"); }
    public String browser()       { return get("browser", "chromium"); }
    public boolean headless()     { return Boolean.parseBoolean(get("headless", "false")); }
    public int defaultTimeoutMs() { return Integer.parseInt(get("timeout.default", "30000")); }
    public int aiTimeoutMs()      { return Integer.parseInt(get("timeout.ai", "60000")); }
    public boolean reuseBrowser() { return Boolean.parseBoolean(get("browser.reuse", "true")); }
    public String email()         { return get("trupeer.email"); }
    public String password()      { return get("trupeer.password"); }
}
