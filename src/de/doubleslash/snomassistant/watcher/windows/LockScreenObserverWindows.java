package de.doubleslash.snomassistant.watcher.windows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipException;

import org.apache.commons.lang.ArrayUtils;

import de.doubleslash.snomassistant.Controller;
import de.doubleslash.snomassistant.Utils;
import de.doubleslash.snomassistant.watcher.LockScreenObserver;

public class LockScreenObserverWindows implements LockScreenObserver, Runnable {

   private Controller controller;
   
   
   public static int PORT = 9685;

   private Thread thread;

   private URI exe;

   private CommunicationServer communicationServer;

   public LockScreenObserverWindows(Controller c) {
      this.controller = c;

      URI uri = null;

      try {
         uri = Utils.getJarURI();
      } catch (URISyntaxException e) {
         e.printStackTrace();
      }
      try {
         this.exe = Utils.getFile(uri, "SnomAssistantNativeHelper.exe");
      } catch (ZipException e) {
         e.printStackTrace();
      } catch (IOException e) {
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
            (controller.getPhone().length() > 0 ? controller.getPhone() : "-"),
            (controller.getUsername().length() > 0 ? controller.getUsername()
                  : "-"),
            (controller.getPassword().length() > 0 ? controller.getPassword()
                  : "-"), Boolean.toString(controller.isEditIdentity1()),
            Boolean.toString(controller.isEditIdentity2()),
            Integer.toString(PORT),
            Boolean.toString(controller.isLogoutOnShutdown()),
            Boolean.toString(controller.isLinkWithLock()) };
      System.out.println(ArrayUtils.toString(cmds));
      try {
         exec = run.exec(cmds);
      } catch (IOException e) {
         e.printStackTrace();
      }

      this.communicationServer = new CommunicationServer(controller);

      new Thread(communicationServer).start();
      try {
         exec.waitFor();
      } catch (InterruptedException e) {
         System.out.println("EventReader interrupted.");
         exec.destroy();
      }
   }

   @Override
   public void send(String data) {
        new CommunicationClient().send(data);
   }

}

class CommunicationServer implements Runnable {
   int port = LockScreenObserverWindows.PORT;
   ServerSocket serverSocket;
   Controller controller;

   public CommunicationServer(Controller controller2) {
      this.controller = controller2;
   }

   @Override
   public void run() {
      try {
         serverSocket = new ServerSocket(port, 0, InetAddress.getByName(null));
         System.out.println("Server waiting for client on port "
               + serverSocket.getLocalPort());

         // server infinite loop
         while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New connection accepted "
                  + socket.getInetAddress() + ":" + socket.getPort());

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                  socket.getInputStream()));
            String s;
            while ((s = stdInput.readLine()) != null) {
               System.out.println("Received: "+s);
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

            // connection closed by client
            try {
               socket.close();
               System.out.println("Connection closed by client");
            } catch (IOException e) {
               System.out.println(e);
            }
         }
      }

      catch (IOException e) {
         System.out.println(e);
      }

   }
}

class CommunicationClient {

   public void send(String data) {
      try {
         Socket server = new Socket("localhost", LockScreenObserverWindows.PORT+1);
         InputStream input = server.getInputStream();
         OutputStream output = server.getOutputStream();
         BufferedWriter w = new BufferedWriter(new OutputStreamWriter(output));
         w.write(data);
         w.close();
         output.close();
         input.close();
         server.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
      
   }
   
}
