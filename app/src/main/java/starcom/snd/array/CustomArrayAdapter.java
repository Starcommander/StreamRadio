package starcom.snd.array;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.ArrayList;

import starcom.snd.WebRadioChannel;
import starcom.snd.listener.CallbackListener;
import starcom.snd.dialog.SettingsDialog;
import starcom.snd.R;

public class CustomArrayAdapter extends ArrayAdapter<WebRadioChannel> implements CallbackListener
{
    public static final String SEP_LINE = "###/*-! SEP /*!-###";
    private final Activity context;

    public CustomArrayAdapter(Activity context) {
        super(context, -1);
        this.context = context;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        WebRadioChannel curChannel = getItem(pos);
        if (curChannel.getUrl().equals(SEP_LINE))
        {
          View rowView = inflater.inflate(R.layout.fragment_header, parent, false);
          TextView sepTxt = (TextView) rowView.findViewById(R.id.txtHeader);
          sepTxt.setText(Integer.parseInt(curChannel.getName()));
          return rowView;
        }
        View rowView = convertView;
        if (rowView == null || rowView.findViewById(R.id.icon) == null)
        {
          rowView = inflater.inflate(R.layout.fragment_channels_entry, parent, false);
        }
        TextView channelTxt = (TextView) rowView.findViewById(R.id.channelTxt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        CheckBox checkbox = (CheckBox) rowView.findViewById(R.id.checkbox);
        channelTxt.setOnLongClickListener(new CustomTouchListener(pos, context, this));
        imageView.setImageResource(curChannel.getGenreIcon());
        channelTxt.setText(curChannel.getName());
        checkbox.setOnCheckedChangeListener(null);
        checkbox.setChecked(curChannel.isSelected());
        checkbox.setOnCheckedChangeListener(createCheckboxListener(pos));
        return rowView;
    }

    private OnCheckedChangeListener createCheckboxListener(final int pos)
    {
      OnCheckedChangeListener l = new OnCheckedChangeListener()
      {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean bSel)
        {
          boolean bDef = ChannelList.getInstance().listViewIsDefault(pos);
          int aPos = ChannelList.getInstance().listViewGetArrayPos(pos);
          if (bDef) { ChannelList.getInstance().getDefaultChannelList().get(aPos).setSelected(bSel); }
          else { ChannelList.getInstance().getCustomChannelList().get(aPos).setSelected(bSel); }
        }
      };
      return l;
    }

    public static class CustomTouchListener implements View.OnLongClickListener
    {
      boolean isDefault;
      int index;
      Activity activity;
      CallbackListener callback;

      public CustomTouchListener(int listViewIdx, Activity activity, CallbackListener callback)
      {
        this.isDefault = ChannelList.getInstance().listViewIsDefault(listViewIdx);
        this.callback = callback;
        this.activity = activity;
        this.index = ChannelList.getInstance().listViewGetArrayPos(listViewIdx);
      }

      @Override
      public boolean onLongClick(View v)
      {
        int selectedIdx = index;
        if (isDefault) { selectedIdx = -1; }
        ChannelList.getInstance().setSelectedChannel(selectedIdx, isDefault);
        if (isDefault)
        {
          SettingsDialog.showSettings(v, activity.getFragmentManager(), "fragment_settings", SettingsDialog.class, callback, SettingsDialog.SettingsType.DefaultChannel);
        }
        else
        {
          SettingsDialog.showSettings(v, activity.getFragmentManager(), "fragment_settings", SettingsDialog.class, callback, SettingsDialog.SettingsType.CustomChannel);
        }
        return true;
      }
    }

    @Override
    public void onCallback()
    {
      clear();
      ArrayList<WebRadioChannel> allChannels = ChannelList.getInstance().listViewCreateChannelList();
      addAll(allChannels);
    }
}

