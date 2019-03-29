package print.and.post.camel.bridge.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.LoggerFactory;


import print.and.post.camel.bridge.MosipBridgeFactory;

/**
 * The Class PropertyFileUtil.
 *
 *
 */
public class PropertyFileUtil {

	/** The reg proc logger. */
	//private static Logger regProcLogger = RegProcessorLogger.getLogger(PropertyFileUtil.class);
	private static final org.slf4j.Logger regProcLogger = LoggerFactory.getLogger(MosipBridgeFactory.class);

	/**
	 * Instantiates a new property file util.
	 */
	private PropertyFileUtil() {

	}

	/**
	 * Gets the property.
	 *
	 * @param clazz the clazz
	 * @param fileName the file name
	 * @param key the key
	 * @return the property
	 */
	public static String getProperty(Class<?> clazz, String fileName, String key) {

		Properties prop = new Properties();
		String value = null;

		try (InputStream input = clazz.getClassLoader().getResourceAsStream(fileName)) {

			if (input == null) {
				throw new FileNotFoundException("File Not available " + fileName);
			}
			prop.load(input);
			value = prop.getProperty(key);
		} catch (IOException ex) {

			regProcLogger.error("Failed to read properties : "+ex.getCause().toString());

		}
		return value;
	}
}
