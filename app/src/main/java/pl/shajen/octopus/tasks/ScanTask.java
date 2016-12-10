package pl.shajen.octopus.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.shajen.octopus.R;
import pl.shajen.octopus.helper.NetworkTools;

import static pl.shajen.octopus.constants.NetworkConstant.MAX_HOST;
import static pl.shajen.octopus.constants.NetworkConstant.PORT;
import static pl.shajen.octopus.constants.NetworkConstant.TIMEOUT_CONNECT_MS;

public class ScanTask extends AsyncTask<Void, Integer, Set<String>> {
    private final ScanTaskResponse m_response;
    private final NetworkTools m_networkTools;
    private final ProgressDialog m_progressDialog;

    public interface ScanTaskResponse {
        void processFinish(Set<String> devices);
    }

    public ScanTask(Context context, ScanTaskResponse response, NetworkTools networkTools) {
        m_response = response;
        m_networkTools = networkTools;
        m_progressDialog = new ProgressDialog(context);
        m_progressDialog.setCancelable(false);
        m_progressDialog.setMessage(context.getString(R.string.SEARCHING_DEVICES));
        m_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        m_progressDialog.setProgress(0);
        m_progressDialog.setMax(MAX_HOST);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        m_progressDialog.show();
    }

    @Override
    protected Set<String> doInBackground(Void... voids) {
        return getActiveDevices();
    }

    @Override
    protected void onPostExecute(Set<String> result) {
        super.onPostExecute(result);
        m_progressDialog.dismiss();
        m_response.processFinish(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        m_progressDialog.setProgress(values[0]);
    }

    private Set<String> getActiveDevices() {
        Set<String> devices = new HashSet<>();
        devices.addAll(getActiveDevicesWifi());
        devices.addAll(getActiveDevicesInternet());
        return devices;
    }

    private List<String> getDevicesInternet() {
        return Arrays.asList("90.156.55.205");
    }

    private Set<String> getActiveDevicesInternet() {
        Set<String> devices = new HashSet<>();
        if (m_networkTools.isInternet()) {
            for (final String deviceIp : getDevicesInternet()) {
                if (m_networkTools.isReachable(deviceIp, PORT, TIMEOUT_CONNECT_MS)) {
                    devices.add(deviceIp);
                }
            }
        }
        return devices;
    }

    private Set<String> getActiveDevicesWifi() {
        Set<String> devices = new HashSet<>();
        if (m_networkTools.isWifi()) {
            final String ip = m_networkTools.getWifiIp();
            for (int i = 0; i <= MAX_HOST; i++) {
                try {
                    final String deviceIp = ip.substring(0, ip.lastIndexOf('.') + 1) + i;
                    if (m_networkTools.isReachable(deviceIp, PORT, TIMEOUT_CONNECT_MS)) {
                        devices.add(deviceIp);
                    }
                } catch (Exception ex) {
                }
                publishProgress(i);
            }
        }
        return devices;
    }
}