package starcom.snd.listener;

import android.app.DialogFragment;
import android.content.DialogInterface;

public class DialogFragmentWithListener extends DialogFragment
{
  private CallbackListener l;
  
  /** Set the Listener for callback.
   *  @return The last listener. **/
  public CallbackListener setCallbackListener(CallbackListener l)
  {
    CallbackListener last = this.l;
    this.l = l;
starcom.debug.LoggingSystem.info(this.getClass(), "paul CallbackListener from Null=" + (last==null) + " to Null=" + (l==null));
    return last;
  }
  
  @Override
  public void onDismiss(DialogInterface di)
  {
    super.onDismiss(di);
    starcom.debug.LoggingSystem.info(this.getClass(), "paul CallbackListener onDismiss() Null:" + (l==null));
    if (l != null) { l.onCallback(); }
  }
}
