package starcom.snd;

public class WebRadioChannel
{
  private String radioName;
  private String radioUrl;
  private boolean selected = true;
  
  public WebRadioChannel(String radioName, String radioUrl)
  {
    this.radioName = radioName;
    this.radioUrl = radioUrl;
  }
  
  public String getUrl()
  {
    return radioUrl;
  }
  
  /** @return Whether this channel is selected as Favourite. **/
  public boolean isSelected()
  {
    return selected;
  }

  /** @param selected Whether this channel is selected as Favourite. **/
  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }
  
  @Override
  public String toString()
  {
    return radioName;
  }
}
