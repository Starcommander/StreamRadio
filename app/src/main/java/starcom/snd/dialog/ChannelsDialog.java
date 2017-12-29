package starcom.snd.dialog;

import starcom.debug.LoggingSystem;
import starcom.snd.R;
import starcom.snd.WebRadioChannel;
import starcom.snd.array.ChannelList;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import starcom.snd.array.CustomArrayAdapter;
import starcom.snd.listener.DialogFragmentWithListener;

public class ChannelsDialog extends DialogFragmentWithListener implements OnClickListener
{
  ListView listView;
  
  // Empty constructor required for DialogFragment
  public ChannelsDialog() {}

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View view = inflater.inflate(R.layout.fragment_channels, container);
    Button okButton = (Button) view.findViewById(R.id.okChannelsButton);
    okButton.setOnClickListener(this);
    getDialog().setTitle(R.string.settings);
    
    ArrayList<WebRadioChannel> channelList = ChannelList.getInstance().listViewCreateChannelList();
    listView = (ListView) view.findViewById(R.id.channelsList);
    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    CustomArrayAdapter adapter = new CustomArrayAdapter(getActivity());
    for (WebRadioChannel curChannel : channelList)
    {
      adapter.add(curChannel);
    }
    listView.setAdapter(adapter);
    return view;
  }

  @Override
  public void onClick(View v)
  {
    if (v.getId()==R.id.okChannelsButton)
    {
      LoggingSystem.info(ChannelsDialog.class, "Selected: selectChannelsOk");
      dismiss();
    }
  }

  @Override
  public void onDismiss(DialogInterface di)
  {
    super.onDismiss(di);
    ChannelList.getInstance().writeChannels(this.getActivity(), ChannelList.CHANNELS_FILENAME);
    ChannelList.getInstance().writeChannels(this.getActivity(), ChannelList.CHANNELS_FILENAME_DEFAULT);
  }
}
