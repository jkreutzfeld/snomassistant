package de.doubleslash.snomassistant;

public class ShutdownHook implements Runnable
{

   private Controller controller;

   public ShutdownHook(Controller controller)
   {
      this.controller = controller;
   }

   @Override
   public void run()
   {
     if (controller.isLogoutOnShutdown()) {
        controller.setIdentityStatus(false);
        System.out.println("Logging out because of shutdown.");
     }
   }

}
