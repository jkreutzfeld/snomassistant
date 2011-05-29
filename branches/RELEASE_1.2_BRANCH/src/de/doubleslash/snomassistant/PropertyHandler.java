package de.doubleslash.snomassistant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyHandler {

	Properties p = new Properties();
	String url = getSettingsDirectory() + File.separator + "settings.properties";

	public PropertyHandler() {
		load();
	}

	public static File getSettingsDirectory() {
		String userHome = System.getProperty("user.home");
		if (userHome == null) {
			throw new IllegalStateException("user.home==null");
		}
		File home = new File(userHome);
		File settingsDirectory = new File(home, ".snomassistant");
		if (!settingsDirectory.exists()) {
			if (!settingsDirectory.mkdir()) {
				throw new IllegalStateException(settingsDirectory.toString());
			}
		}
		return settingsDirectory;
	}

	private void load() {
		
		File newFile = new File(url);
		try {
			newFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		BufferedInputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(url));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		try {
			p.load(stream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String get(String key) {
		String value = p.getProperty(key);
		return (value==null?"":value);
	}
	
	public void set(String key, String value) {
		p.setProperty(key, value);
	}
	
	public void save() {
		try {
			p.store(new FileOutputStream(url), null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
