package org.gnome;
import java.util.List;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.exceptions.DBusException;
public interface ScreenSaver extends DBusInterface
{
   public static class ActiveChanged extends DBusSignal
   {
      public final boolean new_value;
      public ActiveChanged(String path, boolean new_value) throws DBusException
      {
         super(path, new_value);
         this.new_value = new_value;
      }
   }

  public void Lock();
  public void Cycle();
  public void SimulateUserActivity();
  public UInt32 Inhibit(String application_name, String reason);
  public void UnInhibit(UInt32 cookie);
  public List<String> GetInhibitors();
  public UInt32 Throttle(String application_name, String reason);
  public void UnThrottle(UInt32 cookie);
  public boolean GetActive();
  public UInt32 GetActiveTime();
  public void SetActive(boolean value);
  public void ShowMessage(String summary, String body, String icon);

}