package starcom.snd;

import starcom.debug.LoggingSystem;
import starcom.snd.WebStreamPlayer.State;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.widget.Toast;

public class WebRadio extends Activity implements OnClickListener, StateListener
{
  private int NOTIFICATION = R.string.app_name;
  final static String TXT_PLAY = "Play";
  final static String TXT_STOP = "Stop";
  final static String TXT_LABEL = "WebStreamPlayer";
  final static String TXT_NOTIFICATION = "StreamPlayer";
  static String lastChannel;
  static NotificationManager mNM;
  TextView label;
  Button playButton;
  Button setButton;
  Spinner choice;
  WebStreamPlayer streamPlayer;
  int progress = 100;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    label = new TextView(this);
    label.setText(TXT_LABEL);
    label.setTextSize(20);
    label.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
    playButton = new Button(this);
    playButton.setText(TXT_PLAY);
    playButton.setOnClickListener(this);
    setButton = new Button(this);
    setButton.setBackground(getResources().getDrawable( R.drawable.ic_settings ));
    setButton.setOnClickListener(this);
    setButton.setText(" ");

    choice = new Spinner(this);
    ArrayAdapter<WebRadioChannel> arrayAdapter = new ArrayAdapter<WebRadioChannel>(this,android.R.layout.simple_spinner_dropdown_item);
    String channelsTxt = ChannelsDialog.readChannels(this);
    if (channelsTxt==null)
    {
      ChannelsDialog.doWriteDefault(this);
      channelsTxt = ChannelsDialog.readChannels(this);
    }
    ChannelsDialog.getChannelsFromString(channelsTxt, arrayAdapter, false);
    choice.setAdapter(arrayAdapter);
    
    ImageView space = new ImageView(this);
    space.setImageResource(R.drawable.ic_headphones);
    space.setImageLevel(96);
    
    LinearLayout rl = new LinearLayout(this);
    rl.setOrientation(LinearLayout.HORIZONTAL);
    rl.addView(setButton);
    rl.setGravity(android.view.Gravity.RIGHT);

    LinearLayout ll = new LinearLayout(this);
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.setGravity(android.view.Gravity.CENTER);
    ll.addView(rl);
    ll.addView(space);
    ll.addView(label);
    ll.addView(choice);
    ll.addView(playButton);
    setContentView(ll);
    
    streamPlayer = WebStreamPlayer.getInstance();
    streamPlayer.setStateListener(this);
    if (streamPlayer.getState()!=State.Stopped)
    {
      playButton.setText(TXT_STOP);
      label.setText(getChannelName());
    }
    
    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
    telephonyManager.listen(new CallStateListener(streamPlayer), PhoneStateListener.LISTEN_CALL_STATE);
  }

  private String getChannelName()
  {
    String newName = lastChannel.substring(3);
    if (lastChannel.startsWith("[e]")) { return "[electro]" + newName; }
    if (lastChannel.startsWith("[r]")) { return "[rock]" + newName; }
    if (lastChannel.startsWith("[c]")) { return "[classic]" + newName; }
    if (lastChannel.startsWith("[o]")) { return "[oldies]" + newName; }
    if (lastChannel.startsWith("[u]")) { return "[unknown]" + newName; }
    return lastChannel;
  }

  @Override
  public void onClick(View v)
  {
    if (v==setButton)
    {
      SettingsDialog.showSettings(v, getFragmentManager(), "fragment_settings", SettingsDialog.class);
      return;
    }
    if (playButton.getText().equals(TXT_PLAY))
    {
      try
      {
        if (streamPlayer.getState() != WebStreamPlayer.State.Stopped)
        {
          throw new IllegalStateException("Player is busy on state: " + streamPlayer.getState());
        }
        WebRadioChannel curChannel = (WebRadioChannel) choice.getSelectedItem();
        streamPlayer.play(curChannel.getUrl());
        lastChannel = curChannel.toString();
        label.setText(getChannelName());
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
      playButton.setText(TXT_STOP);
      showNotification();
    }
    else if (state == State.Stopped)
    {
      playButton.setText(TXT_PLAY);
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