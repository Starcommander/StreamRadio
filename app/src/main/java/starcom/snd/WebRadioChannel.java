package starcom.snd;

public class WebRadioChannel implements Comparable<WebRadioChannel>
{
  enum Genre {Electro, Rock, Oldies, Undefined};
  private String radioName;
  private String radioUrl;
  private boolean selected = true;
  
  public WebRadioChannel(String radioName, String radioUrl)
  {
    this.radioName = radioName;
    this.radioUrl = radioUrl;
  }

  public int getGenreIcon()
  {
    if (radioName.startsWith("[e] ")) { return R.drawable.genre_electro; }
    if (radioName.startsWith("[r] ")) { return R.drawable.genre_rock; }
    if (radioName.startsWith("[o] ")) { return R.drawable.genre_oldies; }
    if (radioName.startsWith("[c] ")) { return R.drawable.genre_classic; }
    if (radioName.startsWith("[j] ")) { return R.drawable.genre_jazz; }
    if (radioName.startsWith("[u] ")) { return R.drawable.genre_undefined; }
    return R.drawable.genre_undefined;
  }

  public String getName()
  {
    if (getGenreIcon()==R.drawable.genre_undefined && !radioName.startsWith("[u] ")) { return radioName; }
    return radioName.substring(4);
  }
  
  public String getFullName()
  {
    return radioName;
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
  public int compareTo(WebRadioChannel other)
  {
    if (other instanceof WebRadioChannel)
    {
      WebRadioChannel otherW = (WebRadioChannel)other;
      int cmp = radioName.compareTo(otherW.radioName);
      if (cmp!=0) { return cmp; }
      return radioUrl.compareTo(otherW.radioUrl);
    }
    return 1;
  }
  
  public String toString() { return radioName + "\n" + radioUrl; }

  public void setData(WebRadioChannel newChannel)
  {
    radioName = newChannel.radioName;
    radioUrl = newChannel.radioUrl;
    selected = newChannel.selected;
  }
}
