package pl.shajen.octopus.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.test.espresso.core.deps.guava.base.Charsets;
import android.support.test.espresso.core.deps.guava.io.CharStreams;
import android.text.format.Formatter;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import static pl.shajen.octopus.constants.NetworkConstant.PORT;
import static pl.shajen.octopus.constants.NetworkConstant.PUBLIC_IP_SERVER;
import static pl.shajen.octopus.constants.NetworkConstant.TIMEOUT_COMMAND_MS;
import static pl.shajen.octopus.constants.NetworkConstant.TIMEOUT_CONNECT_MS;
import static pl.shajen.octopus.constants.NetworkConstant.TIMEOUT_PING_MS;

public class NetworkTools {
    private final Context m_context;

    public NetworkTools(Context context) {
        m_context = context;
    }

    public boolean isInternet() {
        ConnectivityManager cm = (ConnectivityManager) m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return (ni != null);
    }

    public boolean isWifi() {
        ConnectivityManager cm = (ConnectivityManager) m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return (ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public String getWifiIp() {
        WifiManager wifiMgr = (WifiManager) m_context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return Formatter.formatIpAddress(ip);
    }

    public boolean isReachable(String addr, int openPort, int timeOutMillis) {
        try {
            if (isLocalIp(addr) && !InetAddress.getByName(addr).isReachable(TIMEOUT_PING_MS)) {
                return false;
            }
            new Socket().connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getResponse(String ip, String url) {
        try {
            Socket socket = new Socket();
            socket.setSoTimeout(TIMEOUT_COMMAND_MS);
            socket.connect(new InetSocketAddress(ip, PORT), TIMEOUT_CONNECT_MS);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(String.format("GET /%s HTTP", url));
            out.flush();
            InputStream in = new BufferedInputStream(socket.getInputStream());
            final String result = CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
            in.close();
            out.close();
            socket.close();
            return result;
        } catch (Exception ex) {
            Log.e("Ex", ex.toString());
            return "";
        }
    }

    public JSONObject getJsonResponse(String stringUrl) {
        try {
            final URL url = new URL(stringUrl);
            final String response = CharStreams.toString(new InputStreamReader(url.openStream(), Charsets.UTF_8));
            return new JSONObject(response);
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean isLocalIp(String ip) {
        return ip.startsWith("192.") || ip.startsWith("10.");
    }

    public boolean isIpInsideNetwork(String ip) {
        return (isLocalIp(ip) && isWifi()) || (!isLocalIp(ip) && isInternet());
    }

    public String getPublicIP() {
        try {
            URL url = new URL(PUBLIC_IP_SERVER);
            URLConnection con = url.openConnection();
            final String result = CharStreams.toString(new InputStreamReader(con.getInputStream(), Charsets.UTF_8));
            return result.trim();
        } catch (Exception ex) {
            Log.e("Ex", ex.toString());
            return "";
        }
    }

}
