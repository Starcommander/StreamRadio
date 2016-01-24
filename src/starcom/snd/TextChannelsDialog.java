package starcom.snd;

import starcom.debug.LoggingSystem;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TextChannelsDialog extends DialogFragment implements OnClickListener
{
  private EditText listText;
  // Empty constructor required for DialogFragment
  public TextChannelsDialog() {}
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View view = inflater.inflate(R.layout.fragment_text, container);
    Button okButton = (Button) view.findViewById(R.id.okChannelsTextButton);
    listText = (EditText) view.findViewById(R.id.channelsText);
    String channelsTxt = ChannelsDialog.readChannels(getActivity());
    listText.setText(channelsTxt);
    okButton.setOnClickListener(this);
    return view;
  }

  @Override
  public void onClick(View v)
  {
    if (v.getId()==R.id.okChannelsTextButton)
    {
      LoggingSystem.info(TextChannelsDialog.class, "Selected: editChannelsOk");
      dismiss();
      String channelsTxt = listText.getText().toString();
      ChannelsDialog.doWriteChannels(getActivity(), channelsTxt);
      Toast.makeText(getActivity().getApplicationContext(), "Channel list written", Toast.LENGTH_SHORT).show();
    }
  }
}
