package starcom.snd.array;

import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.io.IOException;
import android.app.Activity;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;
import starcom.snd.WebRadioChannel;
import starcom.snd.util.Resources;
import starcom.snd.WebRadio;
import starcom.debug.LoggingSystem;
import starcom.snd.R;

public class ChannelList
{
  public static final String CHANNELS_FILENAME = "channel_list.properties";
  public static final String CHANNELS_FILENAME_DEFAULT = "channel_list_default.properties";
  private static final String NAME_KEY = "name=";
  private static final String URI_KEY = "url=";
  private static final String NAME_KEY_COM = "#name=";
  private static final String URI_KEY_COM = "#url=";

  public static ChannelList instance;
  private ArrayList<WebRadioChannel> channels_custom;
  private ArrayList<WebRadioChannel> channels_default;
  private int selectedChannel;
  private boolean selectedDefault;
  

  public static ChannelList getInstance()
  {
    return instance;
  }

  public static void init(Activity activity)
  {
    if (instance == null)
    {
      instance = new ChannelList();
      String channelsTxt = readChannels(activity, CHANNELS_FILENAME);
      instance.channels_custom = getChannelsFromString(channelsTxt);
      channelsTxt = readChannels(activity, CHANNELS_FILENAME_DEFAULT);
      instance.channels_default = getChannelsFromString(channelsTxt);
      instance.checkVersion(activity);
    }
  }

  public ArrayList<WebRadioChannel> getCustomChannelList() { return channels_custom; }
  public ArrayList<WebRadioChannel> getDefaultChannelList() { return channels_default; }
  /** Creates a new list with all visible channels from custom_list and default_list **/
  public ArrayList<WebRadioChannel> createSelectedChannelList()
  {
    ArrayList<WebRadioChannel> list = new ArrayList<WebRadioChannel>();
    for (WebRadioChannel curChannel : channels_custom)
    {
      if (curChannel.isSelected()) { list.add(curChannel); }
    }
    for (WebRadioChannel curChannel : channels_default)
    {
      if (curChannel.isSelected()) { list.add(curChannel); }
    }
    return list;
  }
  
  public ArrayList<WebRadioChannel> listViewCreateChannelList()
  {
    ArrayList<WebRadioChannel> list = new ArrayList<WebRadioChannel>();
    list.add(new WebRadioChannel("" + R.string.custom_channels,CustomArrayAdapter.SEP_LINE));
    list.addAll(channels_custom);
    list.add(new WebRadioChannel("" + R.string.predefined_channels,CustomArrayAdapter.SEP_LINE));
    list.addAll(channels_default);
    return list;
  }
  
  public boolean listViewIsDefault(int pos)
  {
    if (pos <= channels_custom.size()) { return false; }
    return true;
  }
  
  public int listViewGetArrayPos(int pos)
  {
    if (pos == 0) { return -1; }
    if (pos == (channels_custom.size()+1)) { return -1; }
    if (pos <= channels_custom.size()) { return pos - 1; }
    return pos - (channels_custom.size() + 1 + 1);
  }

  public WebRadioChannel getSelectedChannel()
  {
    if (selectedChannel < 0) { return null; }
    if (selectedDefault) { return channels_default.get(selectedChannel); }
    return channels_custom.get(selectedChannel);
  }

  public void setSelectedChannel(int selectedChannel, boolean selectedDefault)
  {
    this.selectedChannel = selectedChannel;
    this.selectedDefault = selectedDefault;
  }

  void checkVersion(Activity activity)
  {
    String curAppVersion = "";
    try
    {
      curAppVersion = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
    }
    catch (Exception e)
    {
      LoggingSystem.severe(ChannelList.class, e, "Error while getting app version!!!!");
    }
    SharedPreferences pref = activity.getPreferences(Context.MODE_PRIVATE);
    String storedAppVersion = pref.getString("starcom.snd.versiondate", "");
    LoggingSystem.info(ChannelList.class, "Version-check: stored=" + storedAppVersion + " cur=" + curAppVersion);
    boolean isNewVersion = !storedAppVersion.equals(curAppVersion);
    ArrayList<WebRadioChannel> channels_raw;
    if (isNewVersion || channels_default.size()==0)
    {
      channels_raw = addMissingDefaults(activity);
      if (isNewVersion)
      {
        manipulateCustomAndDefault(channels_raw);
        writeChannels(activity, CHANNELS_FILENAME);
        SharedPreferences.Editor sharedEditor= pref.edit();
        sharedEditor.putString("starcom.snd.versiondate", curAppVersion);
        sharedEditor.commit();
      }
      channels_default = channels_raw;
      writeChannels(activity, CHANNELS_FILENAME_DEFAULT);
    }
  }

  private void manipulateCustomAndDefault(ArrayList<WebRadioChannel> channels_raw)
  {
    for (WebRadioChannel curChannel : channels_raw)
    { // Set visible as stored, and ensure defaults are not in custom list.
      if (channels_custom.contains(curChannel))
      {
        int index = channels_custom.indexOf(curChannel);
        WebRadioChannel deprecatedChannel = channels_custom.get(index);
        channels_custom.remove(curChannel);
        curChannel.setSelected(deprecatedChannel.isSelected());
      } 
      int index = channels_default.indexOf(curChannel);
      if (index < 0) { curChannel.setSelected(channels_default.get(index).isSelected()); }
    }
  }

  private ArrayList<WebRadioChannel> addMissingDefaults(Activity activity)
  {
    ArrayList<WebRadioChannel> channels_raw = getChannelsFromString(readDefaultChannels(activity));
    for (WebRadioChannel curChannel : channels_raw)
    { // Add missing default channels.
      if (!channels_default.contains(curChannel)) { channels_default.add(curChannel); }
    }
    return channels_raw;
  }

  /** Reads all channels to String.
   *  @param activity The activity, that calls this method.
   *  @return The channels text content, or null, if no channels list exists. **/
  private static String readChannels(Activity activity, String channelFile)
  {
    StringBuilder sb = new StringBuilder();


    FileInputStream is = null;
    try
    {
      is = activity.openFileInput(channelFile);
      InputStreamReader sr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(sr);
      while (br.ready())
      {
        String s = br.readLine();
        sb.append(s).append("\n");
      }
    }
    catch (IOException e)
    {
      LoggingSystem.severe(ChannelList.class, e, "Reading channels");
      return "";
    }
    finally
    {
      if (is != null)
      {
        try { is.close(); } catch (Exception e) {}
      }
    }
    return sb.toString();
  }

  public void writeChannels(Activity activity, String channels_filename)
  {
    String toWrite;
    if (channels_filename.equals(CHANNELS_FILENAME)) { toWrite = getStringFromChannels(channels_custom); }
    else { toWrite = getStringFromChannels(channels_default); }
    doWriteChannels(activity, toWrite, channels_filename);
  }

  /** Read final readonly raw file. **/
  private static String readDefaultChannels(Context context)
  {
    return Resources.readTextRaw(context, R.raw.channel_list);
  }
  
  /** Reads all channels and puts it into arrayAdapter.
   *  @param txt The channels text data.
   **/
  private static ArrayList<WebRadioChannel> getChannelsFromString(String txt)
  {
    ArrayList<WebRadioChannel> arrayAdapter = new ArrayList<WebRadioChannel>();
    String lastName = null;
    StringReader sr = null;
    try
    {
      sr = new StringReader(txt);
      BufferedReader br = new BufferedReader(sr);
      while (br.ready())
      {
        String s = br.readLine();
        if (s==null) { break; }
        boolean hasName = s.startsWith(NAME_KEY);
        boolean hasNameCom = s.startsWith(NAME_KEY_COM);
        boolean hasUri = s.startsWith(URI_KEY);
        boolean hasUriCom = s.startsWith(URI_KEY_COM);
        if (hasName || hasNameCom)
        {
          if (hasNameCom) { lastName = s.substring(NAME_KEY_COM.length()); }
          else { lastName = s.substring(NAME_KEY.length()); }
        }
        else if ((hasUri || hasUriCom) && lastName!=null)
        {
          LoggingSystem.info(WebRadio.class, "Appending channel: ", lastName);
          String newUri = s.substring(URI_KEY.length());
          if (hasUriCom) { newUri = s.substring(URI_KEY_COM.length()); }
          WebRadioChannel curChannel = new WebRadioChannel(lastName, newUri);
          if (hasUriCom) { curChannel.setSelected(false); }
          arrayAdapter.add(curChannel);
          lastName = null;
        }
        else
        {
          LoggingSystem.info(ChannelList.class, "Unknown Line: " + s);
        }
      }
    }
    catch (IOException e)
    {
      LoggingSystem.severe(ChannelList.class, e, "Get channels from String");
    }
    finally
    {
      if (sr != null)
      {
        try { sr.close(); } catch (Exception e) {}
      }
    }
    return arrayAdapter;
  }
  
  private static String getStringFromChannels(ArrayList<WebRadioChannel> adapter)
  {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<adapter.size(); i++)
    {
      WebRadioChannel curChannel = (WebRadioChannel) adapter.get(i);
      String raute = "#";
      if (curChannel.isSelected()) { raute = ""; }
      sb.append(raute).append("name=").append(curChannel.getFullName()).append("\n");
      sb.append(raute).append("url=").append(curChannel.getUrl()).append("\n");
    }
    return sb.toString();
  }
  
  private static void doWriteChannels(Activity activity, String toWrite, String channels_filename)
  {
    LoggingSystem.info(ChannelList.class, "Write custom channels");
    FileOutputStream os = null;
    try
    {
      os = activity.openFileOutput(channels_filename,Context.MODE_PRIVATE);
      os.write(toWrite.getBytes());
      os.flush();
    }
    catch (IOException e)
    {
      LoggingSystem.severe(ChannelList.class, e, "Writing channels");
      Toast.makeText(activity.getApplicationContext(), R.string.error_write_channels, Toast.LENGTH_LONG).show();
    }
    finally
    {
      if (os != null)
      {
        try { os.close(); } catch (Exception e) {}
      }
    }
  }

  public int countSelectedCustom()
  {
    int c = 0;
    for (WebRadioChannel curChannel : channels_custom)
    {
      if (curChannel.isSelected()) { c++; }
    }
    return c;
  }
}
