package starcom.snd.array;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import starcom.snd.WebRadio;
import starcom.snd.WebRadioChannel;
import starcom.snd.R;

public class SimpleArrayAdapter extends BaseAdapter
{
    private ArrayList<WebRadioChannel> channels = new ArrayList<WebRadioChannel>();
    private final Context context;
    private SharedPreferences mPreferences;

    public SimpleArrayAdapter(Context context) {
        this.context = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    @Override
    public View getView(int pos, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        WebRadioChannel curChannel = (WebRadioChannel) getItem(pos);

        View rowView = convertView;
        if (rowView == null)
        {
          rowView = inflater.inflate(R.layout.fragment_channels_entrysimple, parent, false);
        }
        TextView channelTxt = (TextView) rowView.findViewById(R.id.channelTxtSimple);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.iconSimple);
        imageView.setImageResource(curChannel.getGenreIcon());
        channelTxt.setText(curChannel.getName());
        if (mPreferences.getBoolean("is_dark", false))
        {
            channelTxt.setTextColor(context.getResources().getColor(R.color.colorTextNight));
        }else {
            channelTxt.setTextColor(context.getResources().getColor(R.color.colorTextDay));
        }
        return rowView;
    }

    @Override
    public int getCount()
    {
      return channels.size();
    }

    @Override
    public Object getItem(int position)
    {
      return channels.get(position);
    }

    @Override
    public long getItemId(int position)
    {
      return position;
    }

    public void add(WebRadioChannel curChannel) { channels.add(curChannel); notifyDataSetChanged(); }
    public void addAll(List<WebRadioChannel> curChannels) { channels.addAll(curChannels); notifyDataSetChanged(); }
    public void clear() { channels.clear(); notifyDataSetChanged(); }
}

