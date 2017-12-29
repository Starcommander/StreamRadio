package starcom.snd.dialog;

import starcom.debug.LoggingSystem;
import starcom.snd.R;
import starcom.snd.listener.DialogFragmentWithListener;
import starcom.snd.util.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class TextDialog extends DialogFragmentWithListener implements OnClickListener
{
  static String aboutTxt;
  // Empty constructor required for DialogFragment
  public TextDialog() {}
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View view = inflater.inflate(R.layout.fragment_text, container);
    Button okButton = (Button) view.findViewById(R.id.okChannelsTextButton);
    EditText listText = (EditText) view.findViewById(R.id.channelsText);
    listText.setFocusable(false);
    if (aboutTxt==null)
    {
      aboutTxt = Resources.readTextRaw(this.getActivity(), R.raw.about);
    }
    listText.setText(aboutTxt);
    okButton.setOnClickListener(this);
    getDialog().setTitle(R.string.about);
    return view;
  }

  @Override
  public void onClick(View v)
  {
    if (v.getId()==R.id.okChannelsTextButton)
    {
      LoggingSystem.info(TextDialog.class, "Selected: aboutOk");
      dismiss();
    }
  }
}
