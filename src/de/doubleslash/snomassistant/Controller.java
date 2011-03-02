package de.doubleslash.snomassistant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

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
   private boolean linkWithLock = false;

   private LockScreenObserver observer;

   private boolean loginOnStartup = false;

   private boolean logoutOnShutdown = false;

   public Controller() {
      System.out.println("OS: "+System.getProperty("os.name"));
      this.propertyHandler = new PropertyHandler();
      loadValues();
      
      if (System.getProperty("os.name").equals("Windows 7")) {
         String configured = propertyHandler.get("windowsConfigured");
         if (configured.equals("") || !Boolean.parseBoolean(configured)){
            initWindows();
            propertyHandler.set("windowsConfigured", "true");
            propertyHandler.save();
         }
      }
      
      
      addShutdownHook();
      if (loginOnStartup) {
         setIdentityStatus(true);
      }
      watchLockScreen();
   }

   private void initWindows() {
      System.out.println("Initializing Windows Settings");
      
      URI uri = null;

      try {
         uri = Utils.getJarURI();
      } catch (URISyntaxException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      try {
         final URI inf = Utils.getFile(uri, "update_logging.inf");
         
         new Thread(new Runnable() {
            @Override
            public void run() {
               Runtime run = Runtime.getRuntime();
               String path = inf.getPath().substring(1).replace('/', '\\');
               String tempFolderPath = path.substring(0, path.lastIndexOf('\\'));
               String[] cmds = new String[] {"secedit", "/configure", "/db", tempFolderPath+"\\secedit.sdb", "/cfg", path};
               try {
                  System.out.println("Running command: "+StringUtils.join(cmds, " "));
                  Process exec = run.exec(cmds);
                  BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                        exec.getInputStream()));
                  String s;
                  while ((s = stdInput.readLine()) != null) {
                     System.out.println(s);
                  }
                  
                  
               } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
         }).start();
         
      } catch (ZipException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   private void addShutdownHook() {
      Runtime run = Runtime.getRuntime();
      run.addShutdownHook(new Thread(new ShutdownHook(this)));
   }

   public void watchLockScreen() {
      if (linkWithLock) {
         if (this.observer == null) {
            String osName = System.getProperty("os.name");
            System.out.println("Starting Process for " + osName);
            if (osName.equals("Linux")) {
               this.observer = new LockScreenObserverLinux(this);
            } else {
               this.observer = new LockScreenObserverWindows(this);
            }
         }
         System.out.println("Observing..");
      } else {
         if (this.observer != null) {
            this.observer.kill();
            this.observer = null;
         }
      }

   }

   public void setIdentityStatus(boolean enabled) {
      if (editIdentity1) {
         String url = generateUrl("1", enabled);
         callUrl(url, "1", enabled);
      }

      if (editIdentity2) {
         String url = generateUrl("2", enabled);
         callUrl(url, "2", enabled);
      }

   }

   private String generateUrl(String identity, boolean enabled) {
      return "http://snom-" + phone + "/line_login.htm?l=" + identity
            + "&Settings=Save&user_active" + identity + "="
            + (enabled ? "on" : "off");
   }

   private void callUrl(String url, String identity, boolean enabled) {
      DefaultHttpClient httpclient = new DefaultHttpClient();
      httpclient.getCredentialsProvider().setCredentials(
            new AuthScope("snom-" + phone, 80),
            new UsernamePasswordCredentials(username, password));
      HttpPost httpost = new HttpPost(url);
      List<NameValuePair> nvps = new ArrayList<NameValuePair>();
      nvps.add(new BasicNameValuePair("Settings", "Save"));
      nvps.add(new BasicNameValuePair("user_active" + identity, (enabled ? "on"
            : "off")));

      try {
         httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
      } catch (UnsupportedEncodingException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      System.out.println(httpost.getURI());

      try {
         HttpResponse execute = httpclient.execute(httpost);
         
         if (execute.getStatusLine().getStatusCode() == 401) {
            showError("Die Zugangsdaten sind inkorrekt.");
         }
      } catch (ClientProtocolException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IOException e) {
         if (e instanceof UnknownHostException) {
            showError("Das Telefon konnte nicht gefunden werden.");
         } else {
            e.printStackTrace();
         }
      }
   }

   private void showError(String string) {
      System.out.println(string);
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

   public void loadValues() {
      System.out.println("Loading values..");
      phone = propertyHandler.get("phone");
      username = propertyHandler.get("username");
      password = propertyHandler.get("password");
      editIdentity1 = Boolean
            .parseBoolean(propertyHandler.get("editIdentity1"));
      editIdentity2 = Boolean
            .parseBoolean(propertyHandler.get("editIdentity2"));
      linkWithLock = Boolean.parseBoolean(propertyHandler
            .get("linkedWithLockScreen"));
      loginOnStartup = Boolean.parseBoolean(propertyHandler
            .get("loginOnStartup"));
      logoutOnShutdown = Boolean.parseBoolean(propertyHandler
            .get("logoutOnShutdown"));
   }

   public void setPropertyHandler(PropertyHandler propertyHandler) {
      this.propertyHandler = propertyHandler;
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
      propertyHandler.set("loginOnStartup", Boolean.toString(loginOnStartup));
      propertyHandler.set("logoutOnShutdown",
            Boolean.toString(logoutOnShutdown));

      propertyHandler.save();
   }

   public void setLinkWithLock(boolean linkWithLock) {
      this.linkWithLock = linkWithLock;
   }

   public boolean isLinkWithLock() {
      return linkWithLock;
   }

   public boolean isLoginOnStartup() {
      return loginOnStartup;
   }

   public boolean isLogoutOnShutdown() {
      return logoutOnShutdown;
   }

   public void setLoginOnStartup(boolean selected) {
      this.loginOnStartup = selected;

   }

   public void setLogoutOnShutdown(boolean selected) {
      this.logoutOnShutdown = selected;

   }
   
   public LockScreenObserver getObserver() {
      return observer;
   }

   public void setObserver(LockScreenObserver observer) {
      this.observer = observer;
   }

}
