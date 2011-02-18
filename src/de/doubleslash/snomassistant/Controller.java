package de.doubleslash.snomassistant;

import java.io.IOException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import de.doubleslash.snomassistant.watcher.LockScreenObserver;
import de.doubleslash.snomassistant.watcher.linux.LockScreenObserverLinux;
import de.doubleslash.snomassistant.watcher.windows.LockScreenObserverWindows;

public class Controller {

	private PropertyHandler propertyHandler;

	private String phone = "";
	private String username = "";
	private String password = "";

	private boolean editIdentity1 = false;
	private boolean editIdentity2 = false;
	private volatile boolean linkWithLock = false;

	private LockScreenObserver observer;

	public Controller() {
		this.propertyHandler = new PropertyHandler();
		loadValues();
		watchLockScreen();
	}

	public void watchLockScreen() {
		if (linkWithLock) {
			if (this.observer == null) {
				String osName = System.getProperty("os.name");
				System.out.println("Starting Process for "+osName);
				if (osName.equals("Linux")) {
					this.observer = new LockScreenObserverLinux(this);
				} else {
					this.observer = new LockScreenObserverWindows(this);
				}
			} 
			System.out.println("Observing..");
		}

	}

	public void setIdentityStatus(boolean enabled) {

		if (editIdentity1) {
			String url = generateUrl("1", enabled);
			callUrl(url);
		}

		if (editIdentity2) {
			String url = generateUrl("2", enabled);
			callUrl(url);
		}

	}

	private String generateUrl(String identity, boolean enabled) {
		return "http://snom-" + phone + "/line_login.htm?l=" + identity
				+ "&Settings=Save&user_active" + identity + "="
				+ (enabled ? "on" : "off");

	}

	private void callUrl(String url) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getCredentialsProvider().setCredentials(
				new AuthScope("snom-" + phone, 80),
				new UsernamePasswordCredentials(username, password));
		HttpGet httpget = new HttpGet(url);
		System.out.println(httpget.getURI());

		try {
			httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isEditIdentity1() {
		return editIdentity1;
	}

	public void setEditIdentity1(boolean editIdentity1) {
		this.editIdentity1 = editIdentity1;
	}

	public boolean isEditIdentity2() {
		return editIdentity2;
	}

	public void setEditIdentity2(boolean editIdentity2) {
		this.editIdentity2 = editIdentity2;
	}

	public PropertyHandler getPropertyHandler() {
		return propertyHandler;
	}

	public void setPropertyHandler(PropertyHandler propertyHandler) {
		this.propertyHandler = propertyHandler;
	}

	public void loadValues() {
		System.out.println("Loading values..");
		phone = propertyHandler.get("phone");
		username = propertyHandler.get("username");
		password = propertyHandler.get("password");
		editIdentity1 = Boolean.parseBoolean(propertyHandler
				.get("editIdentity1"));
		editIdentity2 = Boolean.parseBoolean(propertyHandler
				.get("editIdentity2"));
		linkWithLock = Boolean.parseBoolean(propertyHandler
				.get("linkedWithLockScreen"));
	}

	public void saveValues() {
		System.out.println("Saving values..");
		propertyHandler.set("phone", phone);
		propertyHandler.set("username", username);
		propertyHandler.set("password", password);
		propertyHandler.set("editIdentity1", Boolean.toString(editIdentity1));
		propertyHandler.set("editIdentity2", Boolean.toString(editIdentity2));
		propertyHandler.set("linkedWithLockScreen",
				Boolean.toString(linkWithLock));
		propertyHandler.save();
	}

	public void setLinkWithLock(boolean linkWithLock) {
		this.linkWithLock = linkWithLock;
	}

	public boolean isLinkWithLock() {
		return linkWithLock;
	}
}
