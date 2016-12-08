package pl.shajen.octopus.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.net.InetSocketAddress;
import java.net.Socket;

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
            new Socket().connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
