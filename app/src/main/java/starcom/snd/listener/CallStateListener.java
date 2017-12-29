package starcom.snd.listener;

import starcom.snd.WebStreamPlayer;
import starcom.snd.WebStreamPlayer.State;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CallStateListener extends PhoneStateListener
{
  private WebStreamPlayer streamPlayer;
  
  public CallStateListener(WebStreamPlayer streamPlayer)
  {
    this.streamPlayer = streamPlayer;
  }
  
  @Override
  public void onCallStateChanged(int state, String incomingNumber)
  {
    if (state==TelephonyManager.CALL_STATE_OFFHOOK) { do_stop(); }
    else if (state==TelephonyManager.CALL_STATE_RINGING) { do_stop(); }
    else if (state==TelephonyManager.CALL_STATE_IDLE) { do_continue(); }
  }

  private void do_continue()
  {
    if (streamPlayer.getState()==State.Pause)
    {
      streamPlayer.pause(false);
    }
  }

  private void do_stop()
  {
    if (streamPlayer.getState()==State.Playing)
    {
      streamPlayer.pause(true);
    }
  }
}

