package il.co.shmulman.www.wifisense_v5

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION = 120
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ConnectWifi.setOnClickListener() {
            ConnectionStatus.text = "The CONNECT WIFI button pressed"
            startScanning()
        }

        DisconnectWifi.setOnClickListener() {
            ConnectionStatus.text = "The DISCONNECT WIFI button pressed"
            stopScanning()
        }
    }

    //Permission
    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION
            )
            return false
        } else {
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION -> {
                startScanning()
            }
        }
    }

    private fun startScanning() {
        ConnectionStatus.text = "WiFi status:"
        if (!checkIfConnected()) {
            ConnectionStatus.text = "NO Wifi or Mobile connection"
        } else {
            wifiScanFun()
        }
    }

    private fun stopScanning() {
        ConnectionStatus.text = "WiFi scan stopped"
    }

    private fun checkIfConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        val isWiFi: Boolean = activeNetwork?.type == ConnectivityManager.TYPE_WIFI

        if (isConnected) {
            if (isWiFi) {
                DataOutput.append("CONNECTED WIFI\n")
                return true
            } else {
                DataOutput.append("CONNECTED MOBILE\n")
                return true
            }
        } else {
            DataOutput.append("NOT CONNECTED\n")
            return false
        }
    }

    private fun wifiScanFun() {

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {

                val results = wifiManager.scanResults

                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    DataOutput.append("BroadcastReceiver in onReceive got results\n")
                    printResults(results)
                } else {
                    DataOutput.append("CAN NOT READ WIFI DATA\n")
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)

        val success = wifiManager.startScan()
        if (!success) {
            DataOutput.append("CAN NOT SCAN WIFI DATA\n")
        } else {
            if (checkPermissions()) {
               registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            }
            DataOutput.append("Started WiFi scan ...\n")
        }
    }

    private fun rssiLevel(rssiData : Int) : String {
        return when (rssiData) {
            in -50..0 -> "Excellent signal"
            in -70..-50 -> "Good signal"
            in -80..-70 -> "Fair signal"
            in -100..-80 -> "Weak signal"
            else -> "NO SIGNAL"
        }
    }

    private fun printResults(resultsData: MutableList<ScanResult>){
        DataOutput.append("Number of wifi channels: ")
        DataOutput.append(resultsData.size.toString() + "\n")
        for (i in resultsData)
            DataOutput.append(i.SSID + " " + i.level + " dBm " + rssiLevel(i.level) + "\n")
    }
}