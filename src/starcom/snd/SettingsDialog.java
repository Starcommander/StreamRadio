package starcom.snd;

import starcom.debug.LoggingSystem;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class SettingsDialog extends DialogFragment implements OnClickListener
{
  // Empty constructor required for DialogFragment
  public SettingsDialog() {}

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View view = inflater.inflate(R.layout.fragment_settings, container);
    Button addButton = (Button) view.findViewById(R.id.addChannel);
    Button editButton = (Button) view.findViewById(R.id.editChannels);
    Button restoreButton = (Button) view.findViewById(R.id.restoreChannels);
    Button aboutButton = (Button) view.findViewById(R.id.about);
    addButton.setOnClickListener(this);
    editButton.setOnClickListener(this);
    restoreButton.setOnClickListener(this);
    aboutButton.setOnClickListener(this);
    return view;
  }

  @Override
  public void onClick(View v)
  {
    if (v.getId()==R.id.addChannel)
    {
      LoggingSystem.info(SettingsDialog.class, "Setting selected: addChannel");
      dismiss();
      showSettings(v, getFragmentManager(), "fragment_channels", ChannelsDialog.class);
    }
    else if (v.getId()==R.id.editChannels)
    {
      LoggingSystem.info(SettingsDialog.class, "starcom Not supported yet: editChannels");
      dismiss();
      showSettings(v, getFragmentManager(), "fragment_text", TextChannelsDialog.class);
    }
    else if (v.getId()==R.id.restoreChannels)
    {
      LoggingSystem.info(SettingsDialog.class, "Setting selected: restoreChannels");
      dismiss();
      ChannelsDialog.doWriteDefault(getActivity());
      Toast.makeText(getActivity().getApplicationContext(), "Channel list restored", Toast.LENGTH_LONG).show();
    }
    else if (v.getId()==R.id.about)
    {
      LoggingSystem.info(SettingsDialog.class, "starcom Not supported yet: About");
      dismiss();
      showSettings(v, getFragmentManager(), "fragment_text", TextDialog.class);
    }
  }

  public static void showSettings(View v, FragmentManager fm, String dialogKey, Class<?> c)
  { // close existing dialog fragments
    FragmentManager manager = fm;
    Fragment frag = manager.findFragmentByTag(dialogKey);
    if (frag != null)
    {
      manager.beginTransaction().remove(frag).commit();
    }
    DialogFragment editNameDialog;
    try
    {
      editNameDialog = (DialogFragment) c.newInstance();
      editNameDialog.show(manager, dialogKey);
    }
    catch (java.lang.InstantiationException | IllegalAccessException e)
    {
      LoggingSystem.severe(SettingsDialog.class, e, "Error creating class: "+c);
    }
  }
}
