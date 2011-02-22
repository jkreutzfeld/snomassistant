package de.doubleslash.snomassistant.watcher.linux;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.exceptions.DBusException;

import de.doubleslash.snomassistant.Controller;
import de.doubleslash.snomassistant.watcher.LockScreenObserver;


public class LockScreenObserverLinux implements Runnable, LockScreenObserver {
	
	Controller controller;
	Handler handler;

	public LockScreenObserverLinux(Controller controller) {
		this.controller = controller;
		new Thread( this ).start();
		
	}

	@Override
	public void run() {
		try {
			DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
			handler = new Handler(controller);
			connection.addSigHandler(org.gnome.ScreenSaver.ActiveChanged.class, handler);
			
		} catch (DBusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void kill() {
		try {
			DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
			connection.removeSigHandler(org.gnome.ScreenSaver.ActiveChanged.class, handler);
		} catch (DBusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
class Handler implements DBusSigHandler<org.gnome.ScreenSaver.ActiveChanged> {
	
	private Controller controller;

	public Handler(Controller c) {
		this.controller = c;
	}
	
	public void handle(org.gnome.ScreenSaver.ActiveChanged signal)
	{
		if (controller.isLinkWithLock()) {
			controller.setIdentityStatus(!signal.new_value);
		}
	}
} 