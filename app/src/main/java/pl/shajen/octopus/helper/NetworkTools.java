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
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import pl.shajen.octopus.constants.NetworkConstant;

import static pl.shajen.octopus.constants.NetworkConstant.PORT;
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
            final Socket socket = new Socket();
            socket.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            final OutputStream o = socket.getOutputStream();
            final InputStream i = socket.getInputStream();
            final boolean result = socket.isConnected();
            o.close();
            i.close();
            socket.close();
            return result;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getRawSocketResponse(String ip, String url) {
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
            return "";
        }
    }

    public JSONObject getJsonResponse(String ip, String resource, int port) {
        return getJsonResponse("http://" + ip + ":" + port + resource);
    }

    public JSONObject getJsonResponse(String stringUrl) {
        try {
            return new JSONObject(getStringResponse(stringUrl));
        } catch (Exception ex) {
            return null;
        }
    }

    public String getStringResponse(String ip, String resource, int port) {
        return getStringResponse("http://" + ip + ":" + port + resource);
    }

    public String getStringResponse(String stringUrl) {
        try {
            final URL url = new URL(stringUrl);
            final URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(NetworkConstant.TIMEOUT_CONNECT_MS);
            urlConnection.setReadTimeout(NetworkConstant.TIMEOUT_COMMAND_MS);
            return CharStreams.toString(new InputStreamReader(urlConnection.getInputStream(), Charsets.UTF_8));
        } catch (Exception ex) {
            Log.d("NetworkTools", ex.toString());
            return "";
        }
    }

    public boolean isLocalIp(String ip) {
        return ip.startsWith("192.") || ip.startsWith("10.");
    }

    public boolean isIpInsideNetwork(String ip) {
        return (isLocalIp(ip) && isWifi()) || (!isLocalIp(ip) && isInternet());
    }
}
