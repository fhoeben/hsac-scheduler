package nl.hsac.scheduler.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class to access properties.
 */
public class PropertyHelper {
	private static final Properties PROPERTIES = new Properties();

	private PropertyHelper() {}

	static {
		load("scheduler.properties");
	}

    /**
     * @param key property's key
     * @return value for property
     * @throws IllegalArgumentException if no value is configured for this key.
     * @throws NumberFormatException if no int value is configured for this key.
     */
    public static int getIntProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }

    /**
     * @param key property's key
     * @return value for property
     * @throws IllegalArgumentException if no value is configured for this key.
     * @throws NumberFormatException if no int value is configured for this key.
     */
    public static long getLongProperty(String key) {
        return Long.parseLong(getProperty(key));
    }

    /**
     * @param key property's key
     * @return value for property
     * @throws IllegalArgumentException if no value is configured for this key.
     */
	public static String getProperty(String key) {
		if (!PROPERTIES.containsKey(key)) {
			throw new IllegalArgumentException("Unknown key: " + key);
		} else {
			return PROPERTIES.getProperty(key);
		}
	}

	public static void load(String fileName) {
		InputStream stream = getInputStream(fileName);
		try {
			if (stream == null) {
				throw new IllegalArgumentException("Unable to load: " + fileName);
			} else {
				PROPERTIES.load(stream);
				trimValues();
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Unable to load: " + fileName, e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					throw new IllegalStateException("Unable to close properties file", e);
				}
			}
		}
	}

	private static InputStream getInputStream(String fileName) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
	}

	private static void trimValues() {
		for (String key : PROPERTIES.stringPropertyNames()) {
			String value = PROPERTIES.getProperty(key);
			String trimmedValue = value.trim();
			storeValue(key, trimmedValue);
		}
	}

	private static void storeValue(String key, String trimmedValue) {
		PROPERTIES.put(key, trimmedValue);
	}
}
