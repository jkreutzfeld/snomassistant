package de.doubleslash.snomassistant.watcher.windows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipException;

import org.apache.commons.lang.ArrayUtils;

import de.doubleslash.snomassistant.Controller;
import de.doubleslash.snomassistant.Utils;
import de.doubleslash.snomassistant.watcher.LockScreenObserver;

public class LockScreenObserverWindows implements LockScreenObserver, Runnable {

	private Controller controller;

	private Thread thread;

	private URI exe;

	public LockScreenObserverWindows(Controller c) {
		this.controller = c;

		URI uri = null;

		try {
			uri = Utils.getJarURI();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.exe = Utils.getFile(uri, "EventListener.exe");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.thread = new Thread(this);
		thread.start();

	}

	public void kill() {
		this.thread.interrupt();
	}

	@Override
	public void run() {
		URI dir = this.exe;
		System.out.println(dir);
		Runtime run = Runtime.getRuntime();
		Process exec = null;
		
		String[] cmds = new String[] {
				dir.getPath(),
				(controller.getPhone().length()>0?controller.getPhone():"-"),
				(controller.getUsername().length()>0?controller.getUsername():"-"),
				(controller.getPassword().length()>0?controller.getPassword():"-"),
				Boolean.toString(controller.isEditIdentity1()),
				Boolean.toString(controller.isEditIdentity2())
		};
		System.out.println(ArrayUtils.toString(cmds));
		try {
			exec = run.exec(cmds);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(new EventReader(controller, exec.getInputStream())).start();
		try {
			exec.waitFor();
		} catch (InterruptedException e) {
			System.out.println("EventReader interrupted.");
			exec.destroy();
		}
	}

}

class EventReader implements Runnable {

	private Controller controller;
	private InputStream input;

	EventReader(Controller c, InputStream in) {
		this.controller = c;
		this.input = in;
	}

	@Override
	public void run() {
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				input));

		String s;
		// read the output from the command
		try {
			while ((s = stdInput.readLine()) != null) {
				switch (Integer.parseInt(s)) {

				case 4800:
				   controller.setScreenLocked(true);
				   controller.setIdentityStatus(false);
				   break;
				case 4802:
				   if (!controller.isScreenLocked()) {
				      controller.setIdentityStatus(false);
				   }
				   break;
				case 4647:
					controller.setIdentityStatus(false);
					break;
					
				case 4801:
				   controller.setScreenLocked(false);
				   controller.setIdentityStatus(true);
				   break;
				case 4803:
				   if (!controller.isScreenLocked()) {
				      controller.setIdentityStatus(true);
				   }
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}