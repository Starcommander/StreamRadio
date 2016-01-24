package starcom.snd;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import starcom.debug.LoggingSystem;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ChannelsDialog extends DialogFragment implements OnClickListener
{
  private static final String CHANNELS_FILENAME = "channel_list.properties";
  private static final String NAME_KEY = "name=";
  private static final String URI_KEY = "url=";
  private static final String NAME_KEY_COM = "#name=";
  private static final String URI_KEY_COM = "#url=";
  ListView listView;
  
  // Empty constructor required for DialogFragment
  public ChannelsDialog() {}
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View view = inflater.inflate(R.layout.fragment_channels, container);
    Button okButton = (Button) view.findViewById(R.id.okChannelsButton);
    listView = (ListView) view.findViewById(R.id.channelsList);
    okButton.setOnClickListener(this);
    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    
    ArrayAdapter<WebRadioChannel> adapter = new ArrayAdapter<WebRadioChannel>(ChannelsDialog.this.getActivity(),android.R.layout.simple_list_item_multiple_choice);
    String channelsTxt = readChannels(getActivity());
    getChannelsFromString(channelsTxt, adapter, true);
    listView.setAdapter(adapter);
    for (int i=0; i<listView.getCount(); i++)
    {
      WebRadioChannel curChannel = (WebRadioChannel) listView.getItemAtPosition(i);
      listView.setItemChecked(i, curChannel.isSelected());
    }
    return view;
  }

  @Override
  public void onClick(View v)
  {
    if (v.getId()==R.id.okChannelsButton)
    {
      LoggingSystem.info(ChannelsDialog.class, "Selected: selectChannelsOk");
      dismiss();
      ArrayAdapter<?> adapter = (ArrayAdapter<?>) listView.getAdapter();
      for (int i=0; i<listView.getCount(); i++)
      {
        WebRadioChannel curChannel = (WebRadioChannel) listView.getItemAtPosition(i);
        boolean check = listView.isItemChecked(i);
        curChannel.setSelected(check);
      }
      String toWrite = getStringFromChannels(adapter);
      doWriteChannels(getActivity(),toWrite);
    }
  }
  
  /** Reads all channels.
   *  @param activity The activity, that calls this method.
   *  @return The channels text content, or null, if no channels list exists. **/
  public static String readChannels(Activity activity)
  {
    StringBuilder sb = new StringBuilder();
    try(FileInputStream is = activity.openFileInput(CHANNELS_FILENAME);
        InputStreamReader sr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(sr))
    {
      while (br.ready())
      {
        String s = br.readLine();
        sb.append(s).append("\n");
      }
      return sb.toString();
    }
    catch (IOException e)
    {
      LoggingSystem.severe(WebRadio.class, e, "Reading channels");
      return null;
    }
  }

  public static void doWriteDefault(Activity activity)
  {
    LoggingSystem.info(WebRadio.class, "Write default channels");
    try (FileOutputStream os = activity.openFileOutput(CHANNELS_FILENAME,Context.MODE_PRIVATE);
        InputStream is = activity.getResources().openRawResource(R.raw.channel_list))
    {
      while (true)
      {
        int b = is.read();
        if (b<0) { break; }
        os.write(b);
      }
      is.close();
      os.flush();
      os.close();
    }
    catch (IOException e)
    {
      LoggingSystem.severe(WebRadio.class, e, "Writing default channels");
    }
  }
  
  /** Reads all channels and puts it into arrayAdapter.
   *  @param txt The channels text data.
   *  @param arrayAdapter The adapter, where to put channels
   *  @param addCommented Whether the unselected channels should be added. **/
  public static void getChannelsFromString(String txt, ArrayAdapter<WebRadioChannel> arrayAdapter, boolean addCommented)
  {
    BufferedReader br = new BufferedReader(new StringReader(txt));
    String lastName = null;
    try
    {
      while (br.ready())
      {
        String s = br.readLine();
        if (s==null) { break; }
        boolean hasName = s.startsWith(NAME_KEY);
        boolean hasNameCom = addCommented && s.startsWith(NAME_KEY_COM);
        boolean hasUri = s.startsWith(URI_KEY);
        boolean hasUriCom = addCommented && s.startsWith(URI_KEY_COM);
        if (hasName || hasNameCom)
        {
          lastName = s.substring(NAME_KEY.length());
          if (hasNameCom) { lastName = s.substring(NAME_KEY_COM.length()); }
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
      }
    }
    catch (IOException e)
    {
      LoggingSystem.severe(WebRadio.class, e, "Get channels");
    }
  }
  
  public static String getStringFromChannels(ArrayAdapter<?> adapter)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("# This is a list of available channels\n");
    sb.append("# Only uncomment channels are visible\n");
    for (int i=0; i<adapter.getCount(); i++)
    {
      WebRadioChannel curChannel = (WebRadioChannel) adapter.getItem(i);
      String raute = "#";
      if (curChannel.isSelected()) { raute = ""; }
      sb.append(raute).append("name=").append(curChannel.toString()).append("\n");
      sb.append(raute).append("url=").append(curChannel.getUrl()).append("\n");
    }
    return sb.toString();
  }
  
  public static void doWriteChannels(Activity activity, String toWrite)
  {
    LoggingSystem.info(WebRadio.class, "Write custom channels");
    try (FileOutputStream os = activity.openFileOutput(CHANNELS_FILENAME,Context.MODE_PRIVATE))
    {
      os.write(toWrite.getBytes());
      os.flush();
      os.close();
    }
    catch (IOException e)
    {
      LoggingSystem.severe(WebRadio.class, e, "Writing channels");
      Toast.makeText(activity.getApplicationContext(), "Error writing channels!", Toast.LENGTH_LONG).show();
    }
  }

}
