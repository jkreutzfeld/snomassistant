package de.doubleslash.snomassistant.watcher;

public interface LockScreenObserver {

	public void kill();
	
	public void send(String data);
}
