package pl.shajen.octopus.adapters;

import android.content.Context;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pl.shajen.octopus.R;
import pl.shajen.octopus.helper.NetworkTools;
import pl.shajen.octopus.tasks.DeviceRequestTask;

public class DevicePinsAdapter extends ArrayAdapter<Pair<Integer, Boolean>> {
    private final Context m_context;
    private final DeviceRequestTask.DeviceRequestResponse m_requestResponse;
    private final List<Pair<Integer, Boolean>> m_list;
    private final String m_ip;

    public DevicePinsAdapter(Context context, DeviceRequestTask.DeviceRequestResponse requestResponse, List<Pair<Integer, Boolean>> list, String ip) {
        super(context, -1, list);
        this.m_requestResponse = requestResponse;
        this.m_context = context;
        this.m_list = list;
        this.m_ip = ip;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) m_context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.device_pin_row, parent, false);

        final Integer pin = m_list.get(position).first;
        TextView textView = (TextView) rowView.findViewById(R.id.pinName);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        textView.setText(m_context.getString(R.string.PIN_LABEL, pin));

        if (m_list.get(position).second) {
            imageView.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            imageView.setImageResource(android.R.drawable.btn_star_big_off);
        }

        final Button onButton = (Button) rowView.findViewById(R.id.onButton);
        final Button offButton = (Button) rowView.findViewById(R.id.offButton);

        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ON", String.format("%d", pin));
                new DeviceRequestTask(m_context, m_requestResponse, new NetworkTools(m_context), m_ip, false).execute(String.format("GPIO/SET?PIN=%d&MODE=ON", pin));
            }
        });

//        onButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                Log.e("ON LONG", String.format("%d", pin));
//                new DeviceRequestTask(m_context, m_requestResponse, new NetworkTools(m_context), m_ip).execute(String.format("GPIO?PIN=%d&MODE=SWITCH&COUNT=%d&SLEEP=20", pin, SWITCH_COUNT));
//                return true;
//            }
//        });

        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("OFF", String.format("%d", pin));
                new DeviceRequestTask(m_context, m_requestResponse, new NetworkTools(m_context), m_ip, false).execute(String.format("GPIO/SET?PIN=%d&MODE=OFF", pin));
            }
        });

//        offButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                Log.e("OFF LONG", String.format("%d", pin));
//                new DeviceRequestTask(m_context, m_requestResponse, new NetworkTools(m_context), m_ip).execute(String.format("GPIO?PIN=%d&MODE=SWITCH&COUNT=%d&SLEEP=100", pin, SWITCH_COUNT));
//                return true;
//            }
//        });

//        imageView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                Log.e("IMAGE LONG", String.format("%d", pin));
//                List<String> list = new ArrayList<>(Collections.nCopies(SWITCH_COUNT, String.format("GPIO?PIN=%d&MODE=SWITCH", pin)));
//                new DeviceRequestTask(m_context, m_requestResponse, new NetworkTools(m_context), m_ip).execute(list.toArray(new String[list.size()]));
//                return true;
//            }
//        });
        return rowView;
    }
}