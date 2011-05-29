package de.doubleslash.snomassistant;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public class AssistantGui implements ActionListener, DocumentListener
{

   private JDialog        frmSnomAssistant;
   private Controller     controller;
   private JTextField     txtPhone;
   private JTextField     txtUser;
   private JPasswordField txtPassword;

   public Controller getController()
   {
      return controller;
   }

   public void setController(Controller controller)
   {
      this.controller = controller;
   }

   public JDialog getFrame()
   {
      return frmSnomAssistant;
   }

   public void setFrame(JDialog frame)
   {
      this.frmSnomAssistant = frame;
   }

   /**
    * Create the application.
    * 
    * @param c
    */
   public AssistantGui(Controller c)
   {
      this.controller = c;
      initialize();
   }

   /**
    * Initialize the contents of the frame.
    */
   private void initialize()
   {

      frmSnomAssistant = new JDialog();
      frmSnomAssistant.setTitle("Snom Assistent");
      frmSnomAssistant.setBounds(100, 100, 377, 399);
      frmSnomAssistant.getContentPane().setLayout(null);
      frmSnomAssistant.setResizable(false);

      JButton btnActivate = new JButton("Aktivieren");
      btnActivate.setBounds(217, 134, 140, 25);
      btnActivate.addActionListener(this);
      btnActivate.setActionCommand("activate");
      frmSnomAssistant.getContentPane().add(btnActivate);

      JButton btnDeactivate = new JButton("Deaktivieren");
      btnDeactivate.setBounds(217, 171, 140, 25);
      btnDeactivate.setActionCommand("deactivate");
      btnDeactivate.addActionListener(this);
      frmSnomAssistant.getContentPane().add(btnDeactivate);

      JCheckBox chckbxLinkWithLockscreen = new JCheckBox("Mit Bildschirmsperrung verbinden");
      chckbxLinkWithLockscreen.setBounds(12, 253, 318, 23);
      chckbxLinkWithLockscreen.setSelected(controller.isLinkWithLock());
      chckbxLinkWithLockscreen.setActionCommand("linkToLockscreen");
      chckbxLinkWithLockscreen.addActionListener(this);
      frmSnomAssistant.getContentPane().add(chckbxLinkWithLockscreen);

      JLabel lblPhone = new JLabel("Telefonnummer:");
      lblPhone.setBounds(12, 18, 126, 15);
      frmSnomAssistant.getContentPane().add(lblPhone);

      JLabel lblUser = new JLabel("Benutzername:");
      lblUser.setBounds(12, 45, 117, 15);
      frmSnomAssistant.getContentPane().add(lblUser);

      JLabel lblPassword = new JLabel("Passwort:");
      lblPassword.setBounds(12, 72, 111, 15);
      frmSnomAssistant.getContentPane().add(lblPassword);

      txtPhone = new JTextField();
      txtPhone.setText(controller.getPhone());
      txtPhone.setBounds(156, 12, 201, 27);
      txtPhone.getDocument().addDocumentListener(this);
      txtPhone.getDocument().putProperty("identifier", "phone");
      frmSnomAssistant.getContentPane().add(txtPhone);
      txtPhone.setColumns(10);

      txtUser = new JTextField();
      txtUser.setText(controller.getUsername());
      txtUser.setBounds(156, 39, 201, 27);
      txtUser.getDocument().addDocumentListener(this);
      txtUser.getDocument().putProperty("identifier", "username");
      frmSnomAssistant.getContentPane().add(txtUser);
      txtUser.setColumns(10);

      txtPassword = new JPasswordField();
      txtPassword.setText(controller.getPassword());
      txtPassword.getDocument().addDocumentListener(this);
      txtPassword.getDocument().putProperty("identifier", "password");
      txtPassword.setBounds(156, 66, 201, 27);
      frmSnomAssistant.getContentPane().add(txtPassword);

      JCheckBox chckbxIdentity1 = new JCheckBox("Identität 1");
      chckbxIdentity1.setBounds(12, 134, 129, 23);
      chckbxIdentity1.setSelected(controller.isEditIdentity1());
      chckbxIdentity1.setActionCommand("identity1Edit");
      chckbxIdentity1.addActionListener(this);
      frmSnomAssistant.getContentPane().add(chckbxIdentity1);

      JCheckBox chckbxIdentity2 = new JCheckBox("Identität 2");
      chckbxIdentity2.setBounds(12, 171, 129, 23);
      chckbxIdentity2.setSelected(controller.isEditIdentity2());
      chckbxIdentity2.setActionCommand("identity2Edit");
      chckbxIdentity2.addActionListener(this);
      frmSnomAssistant.getContentPane().add(chckbxIdentity2);

      JSeparator separator = new JSeparator();
      separator.setForeground(Color.LIGHT_GRAY);
      separator.setBounds(12, 243, 345, 2);
      frmSnomAssistant.getContentPane().add(separator);
      JCheckBox chckbxLoginOnStartup = new JCheckBox("Beim Start Identitäten aktivieren");
      chckbxLoginOnStartup.setBounds(12, 307, 318, 23);
      chckbxLoginOnStartup.setSelected(controller.isLoginOnStartup());
      chckbxLoginOnStartup.setActionCommand("loginOnStartup");
      chckbxLoginOnStartup.addActionListener(this);
      frmSnomAssistant.getContentPane().add(chckbxLoginOnStartup);

      JCheckBox chckbxLogoutOnShutdown = new JCheckBox("Beim Herunterfahren Identitäten deaktivieren");
      chckbxLogoutOnShutdown.setEnabled(!System.getProperty("os.name").equals("Linux"));
      chckbxLogoutOnShutdown.setBounds(12, 334, 357, 23);
      chckbxLogoutOnShutdown.setSelected(controller.isLogoutOnShutdown());
      chckbxLogoutOnShutdown.setActionCommand("logoutOnShutdown");
      chckbxLogoutOnShutdown.addActionListener(this);
      frmSnomAssistant.getContentPane().add(chckbxLogoutOnShutdown);
      
      JCheckBox chckbxLinkScreensaver = new JCheckBox("Mit Bildschirmschoner verbinden");
      chckbxLinkScreensaver.setBounds(12, 280, 274, 23);
      chckbxLinkScreensaver.setEnabled(!System.getProperty("os.name").equals("Linux"));
      chckbxLinkScreensaver.setSelected(controller.isLinkWithScreensaver());
      chckbxLinkScreensaver.setActionCommand("linkWithScreensaver");
      chckbxLinkScreensaver.addActionListener(this);
      frmSnomAssistant.getContentPane().add(chckbxLinkScreensaver);

      SystemTray tray = SystemTray.getSystemTray();

      PopupMenu popup = new PopupMenu();
      MenuItem item = new MenuItem("Beenden");
      item.addActionListener(this);
      item.setActionCommand("quit");
      popup.add(item);

      TrayIcon trayIcon = new TrayIcon(createImage("image/snom_logo.gif", "tray icon"),
            "Snom Assistent", popup);
      trayIcon.setImageAutoSize(true);
      trayIcon.addActionListener(this);
      trayIcon.setActionCommand("trayIconClicked");

      frmSnomAssistant.setIconImage(createImage("image/snom_logo.gif", "tray icon"));

      try {
         tray.add(trayIcon);
      }
      catch (AWTException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   protected static Image createImage(String path, String description)
   {
      URL imageURL = AssistantGui.class.getResource(path);

      if (imageURL == null) {
         System.err.println("Resource not found: " + path);
         return null;
      }
      else {
         return (new ImageIcon(imageURL, description)).getImage();
      }
   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
      if ("activate".equals(e.getActionCommand())) {
         controller.setIdentityStatus(true);
      }
      else if ("deactivate".equals(e.getActionCommand())) {
         controller.setIdentityStatus(false);
      }
      else if ("identity1Edit".equals(e.getActionCommand())) {
         JCheckBox box = (JCheckBox) e.getSource();
         controller.setEditIdentity1(box.isSelected());
         controller.saveValues();
      }
      else if ("identity2Edit".equals(e.getActionCommand())) {
         JCheckBox box = (JCheckBox) e.getSource();
         controller.setEditIdentity2(box.isSelected());
         controller.saveValues();
      }
      else if ("linkToLockscreen".equals(e.getActionCommand())) {
         JCheckBox box = (JCheckBox) e.getSource();
         controller.setLinkWithLock(box.isSelected());
         controller.watchLockScreen();
         controller.saveValues();
      }
      else if ("trayIconClicked".equals(e.getActionCommand())) {
         frmSnomAssistant.setVisible(true);
      }
      else if ("quit".equals(e.getActionCommand())) {
         controller.handleExit();
         System.exit(0);
      }
      else if ("loginOnStartup".equals(e.getActionCommand())) {
         JCheckBox box = (JCheckBox) e.getSource();
         controller.setLoginOnStartup(box.isSelected());
         controller.saveValues();
      }
      else if ("logoutOnShutdown".equals(e.getActionCommand())) {
         JCheckBox box = (JCheckBox) e.getSource();
         controller.setLogoutOnShutdown(box.isSelected());
         controller.watchShutdown();
         controller.saveValues();
      } 
      else if ("linkWithScreensaver".equals(e.getActionCommand())) {
         JCheckBox box = (JCheckBox) e.getSource();
         controller.setLinkWithScreensaver(box.isSelected());
         controller.saveValues();
      }
   }

   @Override
   public void insertUpdate(DocumentEvent e)
   {
      fieldChanged(e);

   }

   private void fieldChanged(DocumentEvent e)
   {
      String id = (String) e.getDocument().getProperty("identifier");
      String value = null;
      try {
         value = e.getDocument().getText(0, e.getDocument().getLength());
      }
      catch (BadLocationException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      if (id.equals("phone")) {
         controller.setPhone(value);
      }
      else if (id.equals("username")) {
         controller.setUsername(value);
      }
      else if (id.equals("password")) {
         controller.setPassword(value);
      }
      controller.saveValues();

   }

   @Override
   public void removeUpdate(DocumentEvent e)
   {
      fieldChanged(e);

   }

   @Override
   public void changedUpdate(DocumentEvent e)
   {
      fieldChanged(e);

   }
}
