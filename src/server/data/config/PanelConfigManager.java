package server.data.config;

import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public class PanelConfigManager {

	private static final String CONFIG_FILE = "panel_config.json";

	public enum ConfigValues {
		SM_FOLDER("StarMade Folder Path", String.class, "./StarMade/"),
		THEME("Theme", String.class, "LIGHT");

		public final String displayName;
		public final Class<?> type;
		public final Object defaultValue;

		ConfigValues(String displayName, Class<?> type, Object defaultValue) {
			this.displayName = displayName;
			this.type = type;
			this.defaultValue = defaultValue;
		}

		public void setValue(Object value) {
			if(value.getClass() != type) throw new IllegalArgumentException("Value must be of type " + type.getSimpleName());
			JSONObject config = load();
			config.put(name(), value);
			save(config);
		}

		public Object getValue() {
			JSONObject config = load();
			return config.get(name());
		}
	}

	private static JSONObject load() {
		File configFile = new File(CONFIG_FILE);
		if(!configFile.exists()) initialize(configFile);
		try {
			FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8);
			StringBuilder builder = new StringBuilder();
			int character;
			while((character = reader.read()) != -1) builder.append((char) character);
			return new JSONObject(builder.toString());
		} catch(IOException exception) {
			throw new RuntimeException(exception); //Todo: Error box
		}
	}

	private static void save(JSONObject config) {
		File configFile = new File(CONFIG_FILE);
		try {
			FileWriter writer = new FileWriter(configFile, StandardCharsets.UTF_8);
			writer.write(config.toString());
			writer.flush();
		} catch(IOException exception) {
			throw new RuntimeException(exception); //Todo: Error box
		}
	}

	private static void initialize(File configFile) {
		try {
			configFile.createNewFile();
			JSONObject config = new JSONObject();
			for(ConfigValues value : ConfigValues.values()) config.put(value.name(), value.defaultValue);
			save(config);
		} catch(IOException exception) {
			throw new RuntimeException(exception); //Todo: Error box
		}
	}
}
