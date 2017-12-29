package starcom.snd;

import starcom.debug.LoggingSystem;
import starcom.snd.array.ChannelList;
import starcom.snd.array.SimpleArrayAdapter;
import starcom.snd.listener.CallbackListener;
import starcom.snd.listener.DialogFragmentWithListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Button;

public class SettingsDialog extends DialogFragmentWithListener implements OnClickListener
{
  public enum SettingsType{Main, CustomChannel, DefaultChannel, EditChannel};
  private static SettingsType settingsType = SettingsType.Main;
  private EditText channelUrl, channelName;
  private Spinner channelIcon;

  // Empty constructor required for DialogFragment
  public SettingsDialog() {}

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View view;
    if (settingsType == SettingsType.EditChannel)
    {
      getDialog().setTitle(R.string.channels);
      view = inflater.inflate(R.layout.fragment_edit, container);
      Button saveButton = (Button) view.findViewById(R.id.saveButton);
      saveButton.setOnClickListener(this);
      this.channelName = (EditText) view.findViewById(R.id.channelName);
      this.channelUrl = (EditText) view.findViewById(R.id.channelUrl);
      this.channelIcon = (Spinner) view.findViewById(R.id.channelIconSpinner);
      SimpleArrayAdapter channelAdapter = new SimpleArrayAdapter(inflater.getContext());
      channelIcon.setAdapter(channelAdapter);
      channelAdapter.add(new WebRadioChannel("[e] Electro",""));
      channelAdapter.add(new WebRadioChannel("[r] Rock",""));
      channelAdapter.add(new WebRadioChannel("[o] Oldies",""));
      channelAdapter.add(new WebRadioChannel("[c] Classic",""));
      channelAdapter.add(new WebRadioChannel("[j] Jazz",""));
      channelAdapter.add(new WebRadioChannel("[u] Undef",""));
      
      WebRadioChannel selChannel = ChannelList.getInstance().getSelectedChannel();
      if (selChannel != null)
      {
        channelName.setText(selChannel.getName());
        channelUrl.setText(selChannel.getUrl());
      }
    }
    else
    {
      getDialog().setTitle(R.string.settings);
      view = inflater.inflate(R.layout.fragment_settings, container);
      Button addButton = (Button) view.findViewById(R.id.addChannel);
      if (settingsType==SettingsType.Main) { ((ViewManager)view).removeView(addButton); }
      Button editButton = (Button) view.findViewById(R.id.editChannel);
      if (settingsType!=SettingsType.CustomChannel) { ((ViewManager)view).removeView(editButton); }
      Button editCsButton = (Button) view.findViewById(R.id.editChannels);
      if (settingsType!=SettingsType.Main) { ((ViewManager)view).removeView(editCsButton); }
      Button rmButton = (Button) view.findViewById(R.id.rmChannel);
      if (settingsType!=SettingsType.CustomChannel) { ((ViewManager)view).removeView(rmButton); }
      Button aboutButton = (Button) view.findViewById(R.id.about);
      if (settingsType!=SettingsType.Main) { ((ViewManager)view).removeView(aboutButton); }
      else { aboutButton.setText(R.string.about); }
      addButton.setOnClickListener(this);
      editButton.setOnClickListener(this);
      editCsButton.setOnClickListener(this);
      rmButton.setOnClickListener(this);
      aboutButton.setOnClickListener(this);
      view.forceLayout();
    }
    return view;
  }

  @Override
  public void onClick(View v)
  {
    if (v.getId()==R.id.editChannels)
    {
      LoggingSystem.info(SettingsDialog.class, "Setting selected: addChannel");
      CallbackListener delegateCallback = setCallbackListener(null);
      dismiss();
      showSettings(v, getFragmentManager(), "fragment_channels", ChannelsDialog.class, delegateCallback, null);
    }
    else if (v.getId()==R.id.editChannel)
    {
      LoggingSystem.info(SettingsDialog.class, "Setting selected: editChannel");
      CallbackListener delegateCallback = setCallbackListener(null);
      dismiss();
      showSettings(v, getFragmentManager(), "fragment_edit", SettingsDialog.class, delegateCallback, SettingsType.EditChannel);
    }
    else if (v.getId()==R.id.rmChannel)
    {
      LoggingSystem.info(SettingsDialog.class, "Setting selected: rmChannel");
      dismiss();
      ChannelList.getInstance().getCustomChannelList().remove(ChannelList.getInstance().getSelectedChannel());
    }
    else if (v.getId()==R.id.addChannel)
    {
      LoggingSystem.info(SettingsDialog.class, "Setting selected: addChannel");
      CallbackListener delegateCallback = setCallbackListener(null);
      dismiss();
      ChannelList.getInstance().setSelectedChannel(-1, false);
      showSettings(v, getFragmentManager(), "fragment_edit", SettingsDialog.class, delegateCallback, SettingsType.EditChannel);
    }
    else if (v.getId()==R.id.about)
    {
      LoggingSystem.info(SettingsDialog.class, "starcom Not supported yet: About");
      dismiss();
      showSettings(v, getFragmentManager(), "fragment_text", TextDialog.class, null, null);
    }
    else if (v.getId()==R.id.saveButton)
    {
      String newName = channelIcon.getSelectedItem().toString().substring(0, 4);
      newName = newName + channelName.getText().toString();
      WebRadioChannel newChannel = new WebRadioChannel(newName, channelUrl.getText().toString());
      WebRadioChannel selChannel = ChannelList.getInstance().getSelectedChannel();
      if (selChannel == null)
      {
        ChannelList.getInstance().getCustomChannelList().add(newChannel);
      }
      else
      {
        selChannel.setData(newChannel);
      }
      dismiss();
    }
  }

  /** Show this setting where buttons responses to var settingsType.
    * @param selectedChanel The selected channel for this menu, or -1
    * @param settingsTypeObj The Type for settings, null on other parent class. **/
  public static void showSettings(View v, FragmentManager fm, String dialogKey, Class<?> c, CallbackListener callback, SettingsType settingsTypeObj)
  { // close existing dialog fragments
    if (settingsTypeObj != null) { settingsType = settingsTypeObj; }
    Fragment frag = fm.findFragmentByTag(dialogKey);
    if (frag != null)
    {
      fm.beginTransaction().remove(frag).commit();
    }
    DialogFragmentWithListener editNameDialog;
    try
    {
      editNameDialog = (DialogFragmentWithListener) c.newInstance();
      editNameDialog.show(fm, dialogKey);
      editNameDialog.setCallbackListener(callback);
      //TODO: DialogFragmentWithCallback
    }
    catch (java.lang.InstantiationException | IllegalAccessException e)
    {
      LoggingSystem.severe(SettingsDialog.class, e, "Error creating class: "+c);
    }
  }

}
