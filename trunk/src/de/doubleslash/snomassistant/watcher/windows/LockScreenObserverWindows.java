package de.doubleslash.snomassistant.watcher.windows;

import com.sun.jna.platform.win32.Advapi32Util.EventLogIterator;
import com.sun.jna.platform.win32.Advapi32Util.EventLogRecord;

import de.doubleslash.snomassistant.Controller;
import de.doubleslash.snomassistant.watcher.LockScreenObserver;

public class LockScreenObserverWindows implements LockScreenObserver {

	private Controller controller;

	int currentRecordNumber = -1;

	public LockScreenObserverWindows(Controller c) {
		this.controller = c;

		EventLogIterator iter = new EventLogIterator("Security");
		EventLogRecord record;

		if (currentRecordNumber == -1) {
			do {
				record = iter.next();
			} while (iter.hasNext());
			this.currentRecordNumber = record.getRecordNumber();
		}

		new Thread(new EventReader(controller, this)).start();

	}

}

class EventReader implements Runnable {

	private Controller controller;
	private LockScreenObserverWindows observer;

	EventReader(Controller c,
			LockScreenObserverWindows lockScreenObserverWindows) {
		this.controller = c;
		this.observer = lockScreenObserverWindows;
	}

	@Override
	public void run() {
		EventLogIterator iter;

		while (true) {
			if (controller.isLinkWithLock()) {
				// Immer wieder ab der gespeicherten ID lesen
				iter = new EventLogIterator("Security");
				EventLogRecord record;
				do {
					record = iter.next();
				} while (iter.hasNext()
						&& record.getRecordNumber() != observer.currentRecordNumber);

				// Bei neuen Einträgen verarbeiten..
				while (iter.hasNext()) {
					record = iter.next();
					System.out.println(record.getEventId());
					if (record.getEventId() == 4800) {
						controller.setIdentityStatus(false);
					} else if (record.getEventId() == 4801) {
						controller.setIdentityStatus(true);
					}
				}
				// ..und neueste ID wieder speichern
				System.out.println("Newest Event ID: " + record.getEventId());
				observer.currentRecordNumber = record.getRecordNumber();

				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}