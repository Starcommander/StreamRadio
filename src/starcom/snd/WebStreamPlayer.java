package starcom.snd;

import java.io.IOException;

import starcom.debug.LoggingSystem;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;

public class WebStreamPlayer implements OnBufferingUpdateListener, OnCompletionListener, OnErrorListener, OnInfoListener, OnPreparedListener
{
  public enum State { Preparing,Playing,Pause,Stopped };
  private static WebStreamPlayer instance;
  private MediaPlayer mediaPlayer;
  private State curState = State.Stopped;
  StateListener stateListener;
  
//  public WebStreamPlayer()
//  {
//    ServiceBinderService s = new ServiceBinderService();
//    s.setCommunicationObject(this);
//    s.startS
//  }
  private WebStreamPlayer() {}

  public static WebStreamPlayer getInstance()
  {
    if (instance==null)
    {
      instance = new WebStreamPlayer();
    }
    return instance;
  }
  
  public synchronized void play(String url) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException
  {
    if (curState==State.Playing)
    {
      throw new IllegalStateException("MediaPlayer is playing, cant start now.");
    }
    else if (curState==State.Preparing)
    {
      throw new IllegalStateException("MediaPlayer busy, cant start now.");
    }
    else if (curState==State.Pause)
    {
      throw new IllegalStateException("MediaPlayer paused, cant stop now.");
    }
    else if (curState==State.Stopped)
    {
      LoggingSystem.info(WebStreamPlayer.class, "Start preparing of MediaPlayer.");
      getMediaPlayer().setDataSource(url);
      setState(State.Preparing);
      stateListener.stateLoading(0);
      getMediaPlayer().prepareAsync();
    }
    else
    {
      throw new IllegalStateException("Unknown State: "+curState);
    }
  }
  
  private MediaPlayer getMediaPlayer()
  {
    if (mediaPlayer==null)
    {
      mediaPlayer = new MediaPlayer();
      mediaPlayer.setOnBufferingUpdateListener(this);
      mediaPlayer.setOnCompletionListener(this);
      mediaPlayer.setOnErrorListener(this);
      mediaPlayer.setOnInfoListener(this);
      mediaPlayer.setOnPreparedListener(this);
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }
    return mediaPlayer;
  }
  
  public void setStateListener(StateListener stateListener)
  {
    this.stateListener = stateListener;
  }
  
  private void setState(State state)
  {
    curState = state;
    LoggingSystem.info(WebStreamPlayer.class, "Set to new State: "+state);
    if (stateListener!=null) { stateListener.stateChanged(state); }
  }
  
  public synchronized boolean pause(boolean doPause)
  {
    if (doPause && curState==State.Playing)
    {
      getMediaPlayer().pause();
      setState(State.Pause);
      return true;
    }
    else if (!doPause && curState==State.Pause)
    {
      getMediaPlayer().start();
      setState(State.Playing);
      return true;
    }
    return false;
  }
  
  public synchronized boolean stop()
  {
    if (curState==State.Stopped) { return true; }
    LoggingSystem.info(WebStreamPlayer.class, "Stop MediaPlayer.");
    setState(State.Stopped);
    if (mediaPlayer==null) { return true; }
    if (curState==State.Preparing) { return false; }
    mediaPlayer.pause();
    mediaPlayer.stop();
    mediaPlayer.reset();
    return true;
  }
  
  public State getState() { return curState; }

  @Override
  public void onPrepared(MediaPlayer mp)
  {
    LoggingSystem.info(WebStreamPlayer.class, "Start playing of MediaPlayer.");
    setState(State.Playing);
    stateListener.stateLoading(100);
    mediaPlayer.start();
  }

  @Override
  public boolean onInfo(MediaPlayer mp, int what, int extra)
  {
    return false;
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra)
  {
    if (curState==State.Playing)
    {
      stop();
    }
    else if (curState==State.Preparing)
    {
      stop();
    }
    else if (curState==State.Pause)
    {
      stop();
    }
    else if (curState==State.Stopped)
    {
      // Nothing to do!
    }
    else
    {
      throw new IllegalStateException("Unknown State: "+curState);
    }
    return false;
  }


  @Override
  public void onCompletion(MediaPlayer mp)
  {
    stop();
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent)
  {
    if (stateListener!=null) { stateListener.stateLoading(50); }
  }

}
