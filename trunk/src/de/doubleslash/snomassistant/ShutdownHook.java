package de.doubleslash.snomassistant;

public class ShutdownHook implements Runnable {

   private Controller controller;

   public ShutdownHook(Controller controller) {
      this.controller = controller;
   }

   @Override
   public void run() {
      if (System.getProperty("os.name").equals("Windows 7")) {
         controller.getObserver().kill();
      }
      if (controller.isLogoutOnShutdown()) {
         controller.setIdentityStatus(false);
         System.out.println("Logging out because of shutdown.");
      }
   }

}
