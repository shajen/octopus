package pl.shajen.octopus.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pl.shajen.octopus.R;
import pl.shajen.octopus.constants.NetworkConstant;
import pl.shajen.octopus.helper.NetworkTools;
import pl.shajen.octopus.models.Device;

import static pl.shajen.octopus.constants.NetworkConstant.IP_LIST_URL;
import static pl.shajen.octopus.constants.NetworkConstant.MAX_HOST;
import static pl.shajen.octopus.constants.NetworkConstant.PORT;
import static pl.shajen.octopus.constants.NetworkConstant.TIMEOUT_CONNECT_MS;

public class ScanTask extends AsyncTask<Void, Integer, Set<Device>> {
    private final Context m_context;
    private final ScanTaskResponse m_response;
    private final NetworkTools m_networkTools;
    private final ProgressDialog m_progressDialog;

    public interface ScanTaskResponse {
        void processFinish(Set<Device> devices);
    }

    public ScanTask(Context context, ScanTaskResponse response, NetworkTools networkTools) {
        m_context = context;
        m_response = response;
        m_networkTools = networkTools;
        m_progressDialog = new ProgressDialog(context);
        m_progressDialog.setCancelable(false);
        m_progressDialog.setMessage(context.getString(R.string.SEARCHING_DEVICES));
        m_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        m_progressDialog.setProgress(0);
        m_progressDialog.setMax(0);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        m_progressDialog.show();
    }

    @Override
    protected Set<Device> doInBackground(Void... voids) {
        return getControllerDevices();
    }

    @Override
    protected void onPostExecute(Set<Device> result) {
        super.onPostExecute(result);
        m_progressDialog.dismiss();
        m_response.processFinish(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        m_progressDialog.setProgress(values[0]);
    }

    private Set<Device> getControllerDevices() {
        List<String> list = getActiveDevices();
        Set<Device> controllerDevices = new HashSet<>();
        m_progressDialog.setMax(list.size());
        m_progressDialog.setProgress(0);
        int i = 0;
        for (final String deviceIp : list) {
            m_progressDialog.setMessage(m_context.getString(R.string.SCANNING_IP, deviceIp));
            final JSONObject json = m_networkTools.getJsonResponse(deviceIp, "/", NetworkConstant.PORT);
            if (json != null) {
                try {
                    controllerDevices.add(new Device(deviceIp, json.getString("device_type")));
                } catch (JSONException e) {
                    controllerDevices.add(new Device(deviceIp, "micropython"));
                }
            } else {
                controllerDevices.add(new Device(deviceIp, "unknown"));
            }
            publishProgress(++i);
        }
        return controllerDevices;
    }

    private List<String> getActiveDevices() {
        List<String> devices = new LinkedList<>();
        final List<String> devicesInternet = getDevicesInternet();
        final List<String> devicesWifi = getDevicesWifi();
        final int maxHostInternet = devicesInternet.size();
        final int maxHostWifi = devicesWifi.size();
        m_progressDialog.setMax(maxHostInternet + maxHostWifi);
        devices.addAll(getActiveDevices(0, devicesInternet));
        devices.addAll(getActiveDevices(maxHostInternet, devicesWifi));
        return devices;
    }

    private List<String> getActiveDevices(final int progressStart, List<String> list) {
        List<String> devices = new LinkedList<>();
        int i = 1;
        for (final String deviceIp : list) {
            if (m_networkTools.isReachable(deviceIp, PORT, TIMEOUT_CONNECT_MS)) {
                devices.add(deviceIp);
            }
            publishProgress(progressStart + i++);
        }
        return devices;
    }

    private List<String> getDevicesInternet() {
        final JSONObject json = m_networkTools.getJsonResponse(IP_LIST_URL);
        List<String> list = new ArrayList<>();
        if (json != null) {
            try {
                final JSONArray array = json.getJSONArray("ip_list");
                for (int i = 0; i < array.length(); ++i) {
                    list.add(array.getString(i));
                }
            } catch (Exception ex) {
            }
        }
        return list;
    }

    private List<String> getDevicesWifi() {
        List<String> devices = new ArrayList<>();
        if (m_networkTools.isWifi()) {
            final String ip = m_networkTools.getWifiIp();
            for (int i = 0; i <= MAX_HOST; i++) {
                final String deviceIp = ip.substring(0, ip.lastIndexOf('.') + 1) + i;
                devices.add(deviceIp);
            }
        }
        return devices;
    }
}
