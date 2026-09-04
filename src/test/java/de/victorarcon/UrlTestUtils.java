package de.victorarcon;

public class UrlTestUtils {

    private static final String JDBC_URL_TEMPLATE = "jdbc:oracle:thin:@%s:%d:%s";

    private static final String HTTP_URL_TEMPLATE = "http://%s:%s";

    public static String resolveJdbcUrl(String dbHostAddr, Integer dbHostPort, String sid) {
        return JDBC_URL_TEMPLATE.formatted(dbHostAddr, dbHostPort, sid);
    }

    public static String resolveHttpUrl(String vaultHostAddr, Integer vaultHostPort) {
        return HTTP_URL_TEMPLATE.formatted(vaultHostAddr, vaultHostPort);
    }
}
