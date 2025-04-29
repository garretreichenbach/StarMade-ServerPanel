package server.ui;

import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class DatabaseTable extends JTable {

	private static final int SORT_NAME = 0;
	private static final int SORT_ID = 1;
	private static final int SORT_TYPE = 2;
	private static final int SORT_SECTOR = 4;
	private static final int SORT_SYSTEM = 8;

	private static final int SORT_ASCENDING = 16;
	private static final int SORT_DESCENDING = 32;

	public static int sortMode = SORT_NAME | SORT_DESCENDING;
	private final File worldFolder;
	private final HashSet<EntityData> entities = new HashSet<>();
	private boolean rootsOnly = false;

	public DatabaseTable(File worldFolder) {
		this.worldFolder = worldFolder;
	}

	public void refresh() {

	}

	public void searchByName(String name) {
		entities.clear();
		if(worldFolder.isDirectory() && worldFolder.exists()) {
			File[] files = worldFolder.listFiles();
			if(files != null) {
				for(File file : files) {
					if(!file.isDirectory()) {
						String fileName = file.getName();
						if(fileName.endsWith(".ent")) {
							if(fileName.toLowerCase().contains(name.toLowerCase())) {
								if(rootsOnly) {
									if(!fileName.matches(".*rl\\d+\\.ent")) entities.add(new EntityData(file));
								} else {
									entities.add(new EntityData(file));
								}
							}
						}
					}
				}
			}
		}
	}

	public void searchByID(String id) {
		//Todo: Read the entity data somehow
	}

	public void searchByType(String type) {
		entities.clear();
		if(worldFolder.isDirectory() && worldFolder.exists()) {
			File[] files = worldFolder.listFiles();
			if(files != null) {
				for(File file : files) {
					if(!file.isDirectory()) {
						String fileName = file.getName();
						if(fileName.endsWith(".ent")) {
							if(fileName.toLowerCase().contains(type.toLowerCase())) {
								if(rootsOnly) {
									if(!fileName.matches(".*rl\\d+\\.ent")) entities.add(new EntityData(file));
								} else {
									entities.add(new EntityData(file));
								}
							}
						}
					}
				}
			}
		}
	}

	public void searchBySector(String sector) {
		//Todo: Read the entity data somehow
	}

	public void searchBySystem(String system) {
		//Todo: Read the entity data somehow
	}

	public static class EntityData implements Comparable<EntityData> {

		private final String name;
		private final long id;
		private final String type;
		private final int[] sector;
		private final int[] system;

		public EntityData(File file) {
			name = parseName(file.getName());
			id = Long.parseLong(name.substring(0, name.indexOf('.')));
			type = parseType(file.getName());
			sector = new int[3]; //Todo
			system = new int[3]; //Todo
		}

		public static String parseName(String fileName) {
			//ENTITY_SHIP_[System Fleet 1] Defending #33 - B150-18 Defenderrl00.ent
			String name = fileName.replace(".ent", ""); //Remove the .ent
			name = name.replaceAll("ENTITY_" + parseType(fileName), ""); //Remove ENTITY_
			//If there is a [...], remove it
			if(name.contains("[")) name = name.substring(0, name.indexOf('['));
			return name.trim();
		}

		public static String parseType(String fileName) {
			String type = fileName.replace(".ent", "").replaceAll("ENTITY_", ""); //Remove the .ent and ENTITY_
			type = type.substring(0, type.indexOf('_')); //Remove everything after the second _ (now first cus we removed ENTITY_)
			type = type.replaceAll("_", " "); //Replace _ with space
			return type.trim();
		}

		@Override
		public String toString() {
			JSONObject obj = new JSONObject();
			obj.put("name", name);
			obj.put("id", id);
			obj.put("type", type);
			obj.put("sector", sector);
			obj.put("system", system);
			return obj.toString();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(obj == null || getClass() != obj.getClass()) return false;
			EntityData that = (EntityData) obj;
			return id == that.id && name.equals(that.name) && type.equals(that.type) && Arrays.equals(sector, that.sector) && Arrays.equals(system, that.system);
		}

		@Override
		public int hashCode() {
			return name.hashCode() + (int) id + type.hashCode() + Arrays.hashCode(sector) + Arrays.hashCode(system);
		}

		@Override
		public int compareTo(EntityData o) {
			switch(sortMode) {
				case SORT_NAME | SORT_ASCENDING -> {
					return name.compareTo(o.name);
				}
				case SORT_NAME | SORT_DESCENDING -> {
					return o.name.compareTo(name);
				}
				case SORT_ID | SORT_ASCENDING -> {
					return Long.compare(id, o.id);
				}
				case SORT_ID | SORT_DESCENDING -> {
					return Long.compare(o.id, id);
				}
				case SORT_TYPE | SORT_ASCENDING -> {
					return type.compareTo(o.type);
				}
				case SORT_TYPE | SORT_DESCENDING -> {
					return o.type.compareTo(type);
				}
				case SORT_SECTOR | SORT_ASCENDING -> {
					return Long.compare(getSectorId(sector), getSectorId(o.sector));
				}
				case SORT_SECTOR | SORT_DESCENDING -> {
					return Long.compare(getSectorId(o.sector), getSectorId(sector));
				}
				case SORT_SYSTEM | SORT_ASCENDING -> {
					return Long.compare(getSystemId(system), getSystemId(o.system));
				}
				case SORT_SYSTEM | SORT_DESCENDING -> {
					return Long.compare(getSystemId(o.system), getSystemId(system));
				}
			}
		}

		private long getSectorId(int[] sector) {
			if(sector.length != 3) return -1;
			return (long) sector[0] << 32 | (long) sector[1] << 16 | sector[2];
		}

		private long getSystemId(int[] system) {
			if(system.length != 3) return -1;
			return (long) system[0] << 32 | (long) system[1] << 16 | system[2];
		}
	}
}
