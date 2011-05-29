package de.doubleslash.snomassistant;

import java.awt.EventQueue;

public class SnomAssistant {


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Controller c = new Controller();
					AssistantGui window = new AssistantGui(c);
					window.getFrame().setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
