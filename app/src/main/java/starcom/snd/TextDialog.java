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

public class TextDialog extends DialogFragment implements OnClickListener
{
  // Empty constructor required for DialogFragment
  public TextDialog() {}
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View view = inflater.inflate(R.layout.fragment_text, container);
    Button okButton = (Button) view.findViewById(R.id.okChannelsTextButton);
    EditText listText = (EditText) view.findViewById(R.id.channelsText);
    listText.setFocusable(false);
    String aboutTxt = createAboutText();
    listText.setText(aboutTxt);
    okButton.setOnClickListener(this);
    return view;
  }

  private String createAboutText()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("This software is written by\nPaul Kashofer from Austria.\n\n");
    sb.append("The project is released under the\nGPLv3 General Public License!\n");
    sb.append("github.com/Starcommander/StreamRadio\n\n\n");
    sb.append("For more channels you can browse:\nwww.listenlive.eu\ndir.xiph.org/");
    return sb.toString();
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
