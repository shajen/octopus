package pl.shajen.octopus.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.util.HashSet;
import java.util.Set;

import pl.shajen.octopus.R;
import pl.shajen.octopus.helper.NetworkTools;

public class ScanTask extends AsyncTask<Void, Integer, Set<String>> {
    private final int PORT = 80;
    private final int TIMEOUT_MS = 200;
    private final int MAX_HOST = 255;

    private final ScanTaskResponse m_response;
    private final Activity m_activity;
    private final NetworkTools m_networkTools;
    private final ProgressDialog m_progressDialog;

    public interface ScanTaskResponse {
        void processFinish(Set<String> devices);
    }

    public ScanTask(ScanTaskResponse response, Activity activity, NetworkTools networkTools) {
        m_response = response;
        m_networkTools = networkTools;
        m_activity = activity;
        m_progressDialog = new ProgressDialog(m_activity);
        m_progressDialog.setCancelable(false);
        m_progressDialog.setMessage(m_activity.getString(R.string.SEARCHING_DEVICES));
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

    private Set<String> getActiveDevicesInternet() {
        Set<String> devices = new HashSet<>();
        if (m_networkTools.isInternet()) {

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
                    if (m_networkTools.isReachable(deviceIp, PORT, TIMEOUT_MS)) {
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