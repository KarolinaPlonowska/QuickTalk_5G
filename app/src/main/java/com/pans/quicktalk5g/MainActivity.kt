package com.pans.quicktalk5g
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pans.quicktalk5g.databinding.ActivityMainBinding
import java.net.InetAddress
import android.provider.Settings

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    private lateinit var darkSwitch: SwitchMaterial
    private lateinit var nsdManager: NsdManager
    private lateinit var wifiManager: WifiManager
    private var multicastLock: WifiManager.MulticastLock? = null
    private var receiver: AudioReceiver? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var sender: AudioSender? = null
    private var rxCount = 0
    private lateinit var prefs: SharedPreferences
    private var listenPort = 6000
    private lateinit var deviceAdapter: DeviceAdapter

    private val reqPerm = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load preferences and apply saved dark mode BEFORE inflating layout so initial theme is correct
        prefs = getSharedPreferences("qt_prefs", Context.MODE_PRIVATE)
        val darkPref = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(if (darkPref) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        darkSwitch = bind.root.findViewById(R.id.darkSwitch)
        // setup device adapter (used by dialog when opened)
        deviceAdapter = DeviceAdapter(mutableListOf()) { dev ->
            // on click -> fill ip/port and dismiss dialog (dialog will auto-dismiss if implemented)
            bind.peerIp.setText(dev.host)
            bind.peerPort.setText(dev.port.toString())
            Toast.makeText(this, "Wybrano: ${dev.name}", Toast.LENGTH_SHORT).show()
        }

        // Initialize RecyclerView on main screen to show discovered devices automatically
        val rv = bind.root.findViewById<RecyclerView>(R.id.devicesList)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = deviceAdapter

        // configure devices button (shows dialog with list)
        val devicesBtn = bind.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.devicesBtn)
        devicesBtn.setOnClickListener {
            // inflate dialog layout and attach a dialog-scoped adapter that will dismiss the dialog on click
            val dlgView = layoutInflater.inflate(R.layout.dialog_devices, null)
            val dlgRv = dlgView.findViewById<RecyclerView>(R.id.dialogDevicesRecycler)
            dlgRv.layoutManager = LinearLayoutManager(this)
            // create a dialog-local adapter that uses the same backing list but closes the dialog when an item is chosen
            val dlgBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.devices_dialog_title))
                .setView(dlgView)
                .setNegativeButton(android.R.string.cancel, null)
            val dlg = dlgBuilder.create()
            val dlgAdapter = DeviceAdapter(deviceAdapter.items) { dev ->
                bind.peerIp.setText(dev.host)
                bind.peerPort.setText(dev.port.toString())
                Toast.makeText(this, "Wybrano: ${dev.name}", Toast.LENGTH_SHORT).show()
                try { dlg.dismiss() } catch (_: Throwable) {}
            }
            dlgRv.adapter = dlgAdapter
            dlg.show()
        }
        // header view is referenced dynamically when needed via findViewById
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // initialize switch state and listener; call recreate() after changing mode so UI updates immediately
        darkSwitch.isChecked = darkPref
        darkSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            // stop audio threads first to avoid resource conflicts during activity recreation
            try { sender?.stop(); sender = null } catch (_: Throwable) {}
            try { receiver?.stop(); receiver = null } catch (_: Throwable) {}
            AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            // recreate after a short delay so threads and resources have time to release
            bind.root.postDelayed({ try { recreate() } catch (_: Throwable) {} }, 250)
        }
        bind.peerIp.setText(prefs.getString("ip", ""))
        bind.peerPort.setText((prefs.getInt("port", 6000)).toString())
        listenPort = bind.peerPort.text.toString().toIntOrNull() ?: 6000

        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.mode = AudioManager.MODE_IN_COMMUNICATION

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val speaker = devices.firstOrNull { it.type == android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
            if (speaker != null) {
                am.setCommunicationDevice(speaker)
            } else {
                @Suppress("DEPRECATION")
                am.isSpeakerphoneOn = true
            }
        } else {
            @Suppress("DEPRECATION")
            am.isSpeakerphoneOn = true
        }

        val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        am.setStreamVolume(AudioManager.STREAM_MUSIC, maxOf(1, maxVol / 2), 0)

        startReceiver(listenPort)
        // NSD now started/stopped in onResume/onPause to better follow lifecycle
        reqPerm.launch(Manifest.permission.RECORD_AUDIO)

        bind.peerPort.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                applyPortChange(); true
            } else false
        }
        bind.peerPort.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) applyPortChange() }

        bind.btnHoldToTalk.setOnTouchListener { v, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> { v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(100).start(); startTalking() }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { v.animate().scaleX(1f).scaleY(1f).setDuration(100).start(); stopTalking() }
            }
            true
        }

        // initial visibility
        updateDevicesVisibility()
    }

    private val hideRx = Runnable { bind.rxBadge.visibility = View.INVISIBLE }

    @SuppressLint("SetTextI18n")
    private fun startReceiver(port: Int) {
        receiver?.stop()
        rxCount = 0
        bind.rxCounter.text = "RX: 0"
        receiver = AudioReceiver(port) { _ ->
            runOnUiThread {
                rxCount++
                bind.rxCounter.text = "RX: $rxCount"
                bind.rxBadge.apply { visibility = View.VISIBLE; removeCallbacks(hideRx); postDelayed(hideRx, 300) }
            }
        }
        receiver!!.start()
        bind.statusText.text = getString(R.string.status_listening)
    }

    private fun registerNsdService(port: Int) {
        try { registrationListener?.let { nsdManager.unregisterService(it) } } catch (_: Throwable) {}
        val serviceInfo = NsdServiceInfo()
        // generate a unique service name so devices are distinguishable on the network
        val androidId = try { Settings.Secure.getString(AppContextHolder.context.contentResolver, Settings.Secure.ANDROID_ID) ?: "" } catch (_:Throwable){""}
        val shortId = if (androidId.length >= 4) androidId.takeLast(4) else androidId
        val model = Build.MODEL.replace(Regex("[^A-Za-z0-9]"), "") // sanitize
        val uniqueName = "QuickTalk-${model}-$shortId"
        serviceInfo.serviceName = uniqueName
        serviceInfo.serviceType = "_quicktalk._udp." // custom type
        serviceInfo.port = port

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                localServiceName = serviceInfo.serviceName
                Log.i(TAG, "service registered: $localServiceName")
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.w(TAG, "registration failed: $errorCode")
            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                Log.i(TAG, "service unregistered: ${serviceInfo.serviceName}")
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.w(TAG, "unregistration failed: $errorCode")
            }
        }
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            Log.w(TAG, "resolve failed: " + (serviceInfo?.serviceName ?: "?") + " code=" + errorCode)
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            val host = serviceInfo.host?.hostAddress
            val port = serviceInfo.port
            Log.i(TAG, "resolved: name=${serviceInfo.serviceName} host=$host port=$port")
            if (host != null) runOnUiThread {
                val rawName = serviceInfo.serviceName ?: "?"
                val displayName = if (rawName.startsWith("QuickTalk-")) rawName.removePrefix("QuickTalk-") else rawName
                // add to device list and update devices button visibility/count
                deviceAdapter.addOrUpdate(DiscoveredDevice(displayName, host, port))
                val btn = bind.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.devicesBtn)
                btn.visibility = View.VISIBLE
                btn.text = getString(R.string.devices_button_label_count, deviceAdapter.itemCount)
                updateDevicesVisibility()
                updateDevicesLayout()
            }
        }
    }
    private var localServiceName: String? = null
    private val TAG = "MainActivity"

    private fun discoverNsdServices() {
        try { discoveryListener?.let { nsdManager.stopServiceDiscovery(it) } } catch (_: Throwable) {}
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) { Log.i(TAG, "NSD discovery started: $regType") }
            override fun onDiscoveryStopped(serviceType: String) { Log.i(TAG, "NSD discovery stopped: $serviceType") }
            override fun onServiceFound(service: NsdServiceInfo) {
                Log.i(TAG, "service found: ${service.serviceName} / ${service.serviceType}")
                if (service.serviceType != "_quicktalk._udp.") return
                if (service.serviceName == localServiceName) return
                try { nsdManager.resolveService(service, resolveListener) } catch (e:Throwable){ Log.w(TAG, "resolve call failed", e) }
            }
            override fun onServiceLost(service: NsdServiceInfo) {
                Log.i(TAG, "service lost: ${service.serviceName}")
                runOnUiThread {
                    val name = service.serviceName
                    if (name != null) {
                        deviceAdapter.removeByName(name)
                        val btn = bind.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.devicesBtn)
                        if (deviceAdapter.isEmpty()) {
                            btn.visibility = View.GONE
                        } else {
                            btn.text = getString(R.string.devices_button_label_count, deviceAdapter.itemCount)
                        }
                    }
                    updateDevicesVisibility()
                    updateDevicesLayout()
                }
            }
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) { Log.w(TAG, "start discovery failed: $errorCode") ; try { nsdManager.stopServiceDiscovery(this) } catch(_:Throwable){} }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) { Log.w(TAG, "stop discovery failed: $errorCode") ; try { nsdManager.stopServiceDiscovery(this) } catch(_:Throwable){} }
        }
        nsdManager.discoverServices("_quicktalk._udp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    //  Switches orientation of the devices list: horizontal when more than 1 device, vertical when 0/1
    private fun updateDevicesLayout() {
        runOnUiThread {
            val rv = findViewById<RecyclerView>(R.id.devicesList)
            val lm = if (deviceAdapter.itemCount > 1) LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                     else LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            rv.layoutManager = lm
        }
    }

    private fun stopNsd() {
        try { discoveryListener?.let { nsdManager.stopServiceDiscovery(it) } } catch (_: Throwable) {}
        try { registrationListener?.let { nsdManager.unregisterService(it) } } catch (_: Throwable) {}
        discoveryListener = null; registrationListener = null
    }

    // Updates the visibility of the discovered devices list and the empty view
    private fun updateDevicesVisibility() {
        runOnUiThread {
            val empty = deviceAdapter.isEmpty()
            val rv = findViewById<RecyclerView>(R.id.devicesList)
            val emptyTv = findViewById<View>(R.id.devicesEmpty)
            rv.visibility = if (empty) View.GONE else View.VISIBLE
            emptyTv.visibility = if (empty) View.VISIBLE else View.GONE
        }
    }

    private fun applyPortChange() {
        val newPort = bind.peerPort.text.toString().toIntOrNull() ?: 6000
        if (newPort != listenPort) {
            listenPort = newPort
            prefs.edit().putInt("port", listenPort).apply()
            startReceiver(listenPort)
            try { registerNsdService(listenPort) } catch(_:Throwable){}
        }
    }

    private fun startTalking() {
        applyPortChange()
        val ipStr = bind.peerIp.text.toString().trim()
        val port = listenPort
        if (ipStr.isBlank()) return
        prefs.edit().putString("ip", ipStr).apply()
        val addr = InetAddress.getByName(ipStr)

        val isEmulator = (Build.FINGERPRINT.contains("generic")
                || Build.FINGERPRINT.lowercase().contains("emulator")
                || Build.MODEL.contains("Android SDK built for"))

        sender?.stop()
        sender = AudioSender(addr, port, testTone = isEmulator) { level ->
            bind.vuMeter.setProgress(level, true)
        }
        sender!!.start()
        bind.statusText.text = getString(R.string.status_tx, ipStr, port)
    }

    private fun stopTalking() {
        sender?.stop()
        bind.vuMeter.setProgress(0, true)
        bind.statusText.text = getString(R.string.status_listening)
    }

    override fun onDestroy() {
        super.onDestroy()
        sender?.stop()
        receiver?.stop()
        stopNsd()
    }

    override fun onResume() {
        super.onResume()
        // acquire multicast lock to ensure discovery works reliably on some devices
        try {
            multicastLock = wifiManager.createMulticastLock("quicktalk_multicast")
            multicastLock?.setReferenceCounted(true)
            multicastLock?.acquire()
        } catch (e:Throwable) { Log.w(TAG, "multicast lock failed", e) }
        try { registerNsdService(listenPort) } catch (e:Throwable){ Log.w(TAG, "nsd register", e) }
        try { discoverNsdServices() } catch (e:Throwable){ Log.w(TAG, "nsd discover", e) }
    }

    override fun onPause() {
        super.onPause()
        stopNsd()
        try { multicastLock?.release() } catch (_:Throwable) {}
        multicastLock = null
    }
}
