package starcom.snd.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import starcom.debug.LoggingSystem;

public class Resources
{

  /** Read final readonly raw file. **/
  public static String readTextRaw(Context context, int rawID)
  {
    StringBuilder sb = new StringBuilder();
    LoggingSystem.info(Resources.class, "Read raw file!");
    try (InputStream is = context.getResources().openRawResource(rawID);
         InputStreamReader isr = new InputStreamReader(is, "UTF-8");
         BufferedReader br = new BufferedReader(isr))
    {
      while (true)
      {
        String line = br.readLine();
        if (line==null) { break; }
        sb.append(line);
        sb.append("\n");
      }
    }
    catch (IOException e)
    {
      LoggingSystem.severe(Resources.class, e, "Reading raw text");
    }
    return sb.toString();
  }
}
