package starcom.snd;

import starcom.debug.LoggingSystem;
import starcom.snd.WebStreamPlayer.State;
import starcom.snd.array.ChannelList;
import starcom.snd.array.SimpleArrayAdapter;
import starcom.snd.dialog.ChannelsDialog;
import starcom.snd.dialog.SettingsDialog;
import starcom.snd.dialog.TextDialog;
import starcom.snd.listener.CallStateListener;
import starcom.snd.listener.CallbackListener;
import starcom.snd.listener.StateListener;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.app.TimePickerDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimerTask;
import java.util.Timer;

public class WebRadio extends AppCompatActivity implements OnClickListener, StateListener, CallbackListener
{
  public static final String NOTIFICATION_CHANNEL_ID_LOCATION = "starcom_snd_channel_location";
  private int NOTIFICATION = R.string.app_name;
  static WebRadioChannel lastPlayChannel;
  static WebRadioChannel lastSelectedChannel;
  static boolean skip_replay = false; // Stop by button or init.
  static Timer timer = new Timer();
  static MyTimerTask timerTask;
  TextView label;
  Button playButton;
  boolean bPlayButton = false;
  Spinner choice;
  WebStreamPlayer streamPlayer;
  int progress = 100;

  private SharedPreferences mPreferences;
  private Menu optionsmenu;
  ProgressBar progressBar;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    skip_replay = true;
    mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    if (savedInstanceState == null)
    {
      WebStreamPlayer.getInstance().do_replay = mPreferences.getBoolean("is_replay", false);
      if (mPreferences.getBoolean("is_dark", false))
      {
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
      } else {
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
      }
      recreate();
    }
    setContentView(R.layout.activity_main);
    ChannelList.init(this);

    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setIcon(R.mipmap.logo);
    
    playButton = (Button) findViewById(R.id.mainPlay);
    playButton.setOnClickListener(this);
    label = (TextView) findViewById(R.id.mainText);
    choice = (Spinner) findViewById(R.id.mainSpinner);
    progressBar = (ProgressBar) findViewById(R.id.progressBar);

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

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    streamPlayer.releaseMP();
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
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
        skip_replay = false; // Start by button
      }
      catch (Exception e)
      {
        Toast.makeText(getApplicationContext(), R.string.busy, Toast.LENGTH_SHORT).show();
        LoggingSystem.warn(WebRadio.class, "Cant play because player is busy: "+e);
      }
    }
    else
    {
      skip_replay = true; // Stop by button
      boolean result = streamPlayer.stop();
      if (result==false)
      {
        Toast.makeText(getApplicationContext(), R.string.busy, Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void doClickLater()
  {
    for (int i=0; i<20; i++)
    {
      try { Thread.sleep(500); }
      catch (Exception e) { e.printStackTrace(); }
      if (streamPlayer.getState() == WebStreamPlayer.State.Stopped) { break; }
    }
    this.runOnUiThread( new Runnable(){ public void run() {onClick(null);} } );
  }

  @Override
  public void stateChanged(State state)
  {
    if (state == State.Playing)
    {
      playButton.setText(R.string.stop);
      bPlayButton = false;
      progressBar.setIndeterminate(false);
      progressBar.setVisibility(View.GONE);
      label.setText(String.format(getString(R.string.playing), lastPlayChannel.getName()));
      showNotification();
    }
    else if (state == State.Stopped)
    {
      playButton.setText(R.string.play);
      bPlayButton = true;
      progressBar.setIndeterminate(false);
      progressBar.setVisibility(View.GONE);
      label.setText(""); // empty
      hideNotification();
      if (skip_replay)
      {
        skip_replay = false;
      }
      else if (WebStreamPlayer.getInstance().do_replay)
      {
        new Thread(){ public void run() {doClickLater();} }.start();
      }
    }
    else if (state == State.Preparing) {
      progressBar.setIndeterminate(true);
      progressBar.setVisibility(View.VISIBLE);
      label.setText(""); // empty
    }
    else if (state == State.Pause) {}
    else
    {
      LoggingSystem.severe(WebRadio.class, "Error, unknown State: "+state);
    }
  }

  private void showNotification()
  {
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, WebRadio.class), PendingIntent.FLAG_UPDATE_CURRENT);
    NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    if (Build.VERSION.SDK_INT >= 26) {
      NotificationManager mngr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
      if (mngr.getNotificationChannel(NOTIFICATION_CHANNEL_ID_LOCATION) == null) {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID_LOCATION,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(getString(R.string.app_name));
        channel.enableLights(false);
        channel.enableVibration(false);
        mngr.createNotificationChannel(channel);
      }
    }

    Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.logo);
    NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
    bigStyle.bigText(lastPlayChannel.getName());

    Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_LOCATION)
            .setSmallIcon(R.mipmap.logo)  // the status icon
            .setLargeIcon(largeIcon)
            .setStyle(bigStyle)
            .setTicker(lastPlayChannel.getName())  // the status text
            .setWhen(Calendar.getInstance().getTimeInMillis())  // the time stamp
            .setContentTitle(getString(R.string.app_name))  // the label of the entry
            .setContentText(lastPlayChannel.getName())  // the contents of the entry
            .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
            .setOngoing(true)                 // remove/only cancel by stop button
            .build();
    mNM.notify(NOTIFICATION, notification);
  }

  void hideNotification()
  {
    NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    mNM.cancel(NOTIFICATION);
  }

  @Override
  public void stateLoading(int percent)
  {
    if (percent!=0 && percent<=progress) { return; }
    progress = percent;
    Toast.makeText(
            getApplicationContext(),
            String.format(getString(R.string.loading), String.valueOf(percent)+"%"),
            Toast.LENGTH_SHORT
    ).show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.options, menu);
    optionsmenu = menu;
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId()) {
      case R.id.action_sleep:
        showTimeSelector();
        return true;
      case R.id.action_setting:

        SettingsDialog.showSettings(null, getFragmentManager(), "fragment_channels", ChannelsDialog.class, this, null);
        return true;

      case R.id.action_dark:

        if (item.isChecked())
        {
          item.setChecked(false);
          mPreferences.edit().putBoolean("is_dark", false).apply();
          getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
          recreate();
        } else {
          item.setChecked(true);
          mPreferences.edit().putBoolean("is_dark", true).apply();
          getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
          recreate();
        }
        return true;

      case R.id.action_replay:

        item.setChecked(!item.isChecked());
        WebStreamPlayer.getInstance().do_replay = item.isChecked();
        mPreferences.edit().putBoolean("is_replay", WebStreamPlayer.getInstance().do_replay).apply();
        return true;

      case R.id.action_power:

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
          intent.setData(Uri.parse("package:" + getPackageName()));
          startActivity(intent);
        }
        return true;

      case R.id.action_about:

        SettingsDialog.showSettings(null, getFragmentManager(), "fragment_text", TextDialog.class, null, null);
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    optionsmenu.findItem(R.id.action_dark).setChecked(mPreferences.getBoolean("is_dark", false));
    optionsmenu.findItem(R.id.action_replay).setChecked(mPreferences.getBoolean("is_replay", false));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
      optionsmenu.findItem(R.id.action_power).setVisible(true);
    }
    return super.onPrepareOptionsMenu(menu);
  }

  public void showTimeSelector() {
    TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
      @Override
      public void onTimeSet(android.widget.TimePicker view, int hour, int minute) {
        if (view.isShown()) {
          long minuteMult = 1000 * 60;
          long delayMS = (minute * minuteMult) + hour * 60 * minuteMult;
          timer.purge(); // Cleans up canceled tasks.
          if (timerTask!=null) { timerTask.cancel(); }
          timerTask = new MyTimerTask(delayMS);
          timer.schedule (timerTask, delayMS);
        }
      }
    };
    TimePickerDialog timePickerDialog = new TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, myTimeListener, 0, 10, true);
    timePickerDialog.setTitle("Choose delay sleep time:");
    timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    if (timerTask==null || timerTask.getRemainingMS() < 0)
    {
      Toast.makeText(getApplicationContext(), "SleepTimer: off", Toast.LENGTH_SHORT).show();
    }
    else
    {
      Toast.makeText(getApplicationContext(), "SleepTimer: " + timerTask.getRemainingMin() + "min", Toast.LENGTH_SHORT).show();
    }
    timePickerDialog.show();
  }

  static class MyTimerTask extends TimerTask
  {
    long delay;
    long initTime;

    public MyTimerTask(long delay)
    {
      this.delay = delay;
      initTime = System.currentTimeMillis();
    }

    public long getRemainingMin()
    {
      return getRemainingMS / 60000;
    }

    public long getRemainingMS()
    {
      long now = System.currentTimeMillis();
      long diff = now - initTime;
      return delay - diff;
    }

    @Override public void run() { WebStreamPlayer.getInstance().stop(); }
  }

}
