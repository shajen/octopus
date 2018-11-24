package pl.shajen.octopus.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.LinkedList;
import java.util.List;

import pl.shajen.octopus.R;
import pl.shajen.octopus.constants.NetworkConstant;
import pl.shajen.octopus.helper.NetworkTools;

public class DeviceRequestTask extends AsyncTask<String, Void, List<String>> {
    private final DeviceRequestTask.DeviceRequestResponse m_response;
    private final NetworkTools m_networkTools;
    private final String m_deviceIp;
    private final ProgressDialog m_progressDialog;
    private final boolean m_useHttp;

    public interface DeviceRequestResponse {
        void processFinish(List<String> responses);
    }

    public DeviceRequestTask(Context context, DeviceRequestResponse response, NetworkTools networkTools, String deviceIp, boolean useHttp) {
        m_response = response;
        m_networkTools = networkTools;
        m_deviceIp = deviceIp;
        m_progressDialog = ProgressDialog.show(context, context.getString(R.string.TASK), context.getString(R.string.PLEASE_WAIT));
        m_useHttp = useHttp;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        m_progressDialog.show();
    }

    @Override
    protected List<String> doInBackground(String... strings) {
        List<String> response = new LinkedList<>();
        if (m_networkTools.isIpInsideNetwork(m_deviceIp)) {
            for (final String url : strings) {
                if (m_useHttp) {
                    response.add(m_networkTools.getStringResponse(m_deviceIp, url, NetworkConstant.PORT));
                } else {
                    response.add(m_networkTools.getRawSocketResponse(m_deviceIp, url));
                }
            }
        }
        return response;
    }

    @Override
    protected void onPostExecute(List<String> result) {
        super.onPostExecute(result);
        m_progressDialog.dismiss();
        m_response.processFinish(result);
    }
}
