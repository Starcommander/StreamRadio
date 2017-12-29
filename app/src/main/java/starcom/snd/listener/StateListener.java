package starcom.snd.listener;

import starcom.snd.WebStreamPlayer.State;

public interface StateListener
{
  public void stateChanged(State state);
  public void stateLoading(int percent);
}
