package starcom.snd;

import starcom.debug.LoggingSystem;
import starcom.snd.WebStreamPlayer.State;
import starcom.snd.array.ChannelList;
import starcom.snd.array.SimpleArrayAdapter;
import starcom.snd.dialog.SettingsDialog;
import starcom.snd.listener.CallStateListener;
import starcom.snd.listener.CallbackListener;
import starcom.snd.listener.StateListener;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class WebRadio extends Activity implements OnClickListener, StateListener, CallbackListener
{
  private int NOTIFICATION = R.string.app_name;
  final static String TXT_LABEL = "WebStreamPlayer";
  final static String TXT_NOTIFICATION = "StreamPlayer";
  static WebRadioChannel lastPlayChannel;
  static WebRadioChannel lastSelectedChannel;
  static NotificationManager mNM;
  TextView label;
  Button playButton;
  boolean bPlayButton = false;
  Button setButton;
  Spinner choice;
  WebStreamPlayer streamPlayer;
  int progress = 100;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    LinearLayout rl = (LinearLayout)findViewById(R.id.mainLayout);
    rl.setBackgroundColor(Color.LTGRAY);
    ChannelList.init(this);
    
    playButton = (Button) findViewById(R.id.mainPlay);
    playButton.setOnClickListener(this);
    label = (TextView) findViewById(R.id.mainText);
    label.setText(TXT_LABEL);
    setButton = (Button) findViewById(R.id.mainSettings);
    setButton.setOnClickListener(this);
    choice = (Spinner) findViewById(R.id.mainSpinner);
    
    SimpleArrayAdapter arrayAdapter = new SimpleArrayAdapter(this.getApplicationContext());
    choice.setAdapter(arrayAdapter);
    choice.setOnItemSelectedListener(createSpinnerListener());
    
    streamPlayer = WebStreamPlayer.getInstance();
    streamPlayer.setStateListener(this);
    stateChanged(streamPlayer.getState());
    
    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
    telephonyManager.listen(new CallStateListener(streamPlayer), PhoneStateListener.LISTEN_CALL_STATE);
  }
  
  private OnItemSelectedListener createSpinnerListener()
  {
    OnItemSelectedListener l = new OnItemSelectedListener()
    {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
      {
        lastSelectedChannel = (WebRadioChannel) choice.getSelectedItem();
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent)
      {
      }
    };
    return l;
  }

  @Override
  public void onResume()
  {
    super.onResume();
    updateSpinner();
  }
  
  public void onCallback()
  {
    updateSpinner();
  }
  
  private void updateSpinner()
  {
    WebRadioChannel selChannel = lastSelectedChannel;
    SimpleArrayAdapter adapter = (SimpleArrayAdapter) choice.getAdapter();
    adapter.clear();
    ArrayList<WebRadioChannel> selectedChannels = ChannelList.getInstance().createSelectedChannelList();
    adapter.addAll(selectedChannels);
    int idx = selectedChannels.indexOf(selChannel);
    if (idx >= 0) { choice.setSelection(idx); }
  }

  @Override
  public void onClick(View v)
  {
    if (v==setButton)
    {
      SettingsDialog.showSettings(v, getFragmentManager(), "fragment_settings", SettingsDialog.class, this, SettingsDialog.SettingsType.Main);
      return;
    }
    if (bPlayButton)
    {
      try
      {
        if (streamPlayer.getState() != WebStreamPlayer.State.Stopped)
        {
          throw new IllegalStateException("Player is busy on state: " + streamPlayer.getState());
        }
        WebRadioChannel curChannel = (WebRadioChannel) choice.getSelectedItem();
        streamPlayer.play(curChannel.getUrl());
        lastPlayChannel = curChannel;
        label.setText(lastPlayChannel.getName());
      }
      catch (Exception e)
      {
        Toast.makeText(getApplicationContext(), "Player is busy", Toast.LENGTH_SHORT).show();
        LoggingSystem.warn(WebRadio.class, "Cant play because player is busy: "+e);
      }
    }
    else
    {
      boolean result = streamPlayer.stop();
      if (result==false)
      {
        Toast.makeText(getApplicationContext(), "Player is busy", Toast.LENGTH_SHORT).show();
      }
    }
  }

  @Override
  public void stateChanged(State state)
  {
    if (state == State.Playing)
    {
      playButton.setText(R.string.stop);
      bPlayButton = false;
      showNotification();
    }
    else if (state == State.Stopped)
    {
      playButton.setText(R.string.play);
      bPlayButton = true;
      label.setText(TXT_LABEL);
      hideNotification();
    }
    else if (state == State.Preparing) {}
    else if (state == State.Pause) {}
    else
    {
      LoggingSystem.severe(WebRadio.class, "Error, unknown State: "+state);
    }
  }

  private void showNotification()
  {
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, WebRadio.class), PendingIntent.FLAG_UPDATE_CURRENT);
    if (mNM==null)
    {
      mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    Notification notification = new Notification.Builder(this)
        .setSmallIcon(R.drawable.ic_headphones)  // the status icon
        .setTicker(TXT_NOTIFICATION)  // the status text
        .setWhen(System.currentTimeMillis())  // the time stamp
        .setContentTitle(TXT_NOTIFICATION)  // the label of the entry
        .setContentText(TXT_NOTIFICATION)  // the contents of the entry
        .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
        .setOngoing(true)
        .build();
    mNM.notify(NOTIFICATION, notification);
  }

  void hideNotification()
  {
    if (mNM==null) { return; }
    mNM.cancel(NOTIFICATION);
    mNM = null;
  }

  @Override
  public void stateLoading(int percent)
  {
    if (percent!=0 && percent<=progress) { return; }
    progress = percent;
    Toast.makeText(getApplicationContext(), "Loading: " + percent + "%", Toast.LENGTH_SHORT).show();
  }
}
