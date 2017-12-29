package starcom.debug;

import java.util.logging.Logger;
import java.util.logging.Level;

public class LoggingSystem
{
  public static Level L_ALL = Level.ALL;
  public static Level L_OFF = Level.OFF;
  public static Level L_SEVERE = Level.SEVERE;
  public static Level L_WARNING  = Level.WARNING;
  public static Level L_INFO = Level.INFO;
  public static Level L_CONFIG = Level.CONFIG;
  public static Level L_FINE = Level.FINE;
  public static Level L_FINER = Level.FINER;
  public static Level L_FINEST = Level.FINEST;

  public static void setLogLevel(Object aclass, Level level) { setLogLevel(aclass.getClass(),level); }
  public static void setLogLevel(Class<? extends Object> aclass, Level level) { setLogLevel(getName(aclass),level); }
  public static void setLogLevel(String aclass, Level level) { Logger.getLogger(aclass).setLevel(level); }
  public static void setGlobalLogLevel(Level level) { Logger.getLogger("").setLevel(level); }

  public static void fine(Object aclass, String... msg) { fine(aclass.getClass(),msg); }
  public static void info(Object aclass, String... msg) { info(aclass.getClass(),msg); }
  public static void warn(Object aclass, String... msg) { warn(aclass.getClass(),msg); }
  public static void severe(Object aclass, String... msg) { severe(aclass.getClass(),msg); }
  public static void severe(Object aclass, Exception e) { severe(aclass.getClass(),e); }

  public static void fine(Class<? extends Object> aclass, String... msg) { log(Level.FINE,aclass,join(msg)); }
  public static void info(Class<? extends Object> aclass, String... msg) { log(Level.INFO,aclass,join(msg)); }
  public static void warn(Class<? extends Object> aclass, String... msg) { log(Level.WARNING,aclass,join(msg)); }
  public static void severe(Class<? extends Object> aclass, String... msg) { log(Level.SEVERE,aclass,join(msg)); }
  public static void severe(Class<? extends Object> aclass, Exception e) { e.printStackTrace(); severe(aclass, aclass.toString()); }
  public static void severe(Class<? extends Object> aclass, Exception e, String... msg) { e.printStackTrace(); severe(aclass, aclass.toString()); }

  public static void logToFile(Object aclass, String msg) { logToFile(aclass.getClass(),msg); }

  private static void log(Level level, Class<? extends Object> aclass, String msg)
  {
    String name = getName(aclass);
    Logger.getLogger(name).logp(level,name,"",msg);
  }

  private static String join(String[] msg)
  {
    if (msg.length==1) { return msg[0]; }
    StringBuilder sb = new StringBuilder();
    for (String curMsg : msg)
    {
      sb.append(curMsg);
    }
    return sb.toString();
  }
  
  private static String getName(Class<? extends Object> aclass)
  {
    String name = aclass.getCanonicalName();
    name.getClass(); // NullPointer bei anonymer oder lokaler Klasse
    return name;
  }

}