package com.pans.quicktalk5g
import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.util.Log
import androidx.core.app.ActivityCompat
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sin

class AudioSender(
    private val dstAddr: InetAddress,
    private val dstPort: Int,
    private val testTone: Boolean = false,
    private val onLevel: (Int) -> Unit = {}
) {
    companion object { private const val TAG = "AudioSender"; private const val SR = 16000; private const val MS = 20 }
    private val running = AtomicBoolean(false)
    private var thread: Thread? = null

    @Volatile private var socket: DatagramSocket? = null
    @Volatile private var rec: AudioRecord? = null

    fun start() {
        if (running.getAndSet(true)) return
        thread = Thread {
            val frameBytes = SR / 1000 * MS * 2
            socket = DatagramSocket()
            var seq = 0
            try {
                if (!testTone) {
                    val minBuf = AudioRecord.getMinBufferSize(SR, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                    if (ActivityCompat.checkSelfPermission(
                            /* context = */ AppContextHolder.context,
                            /* permission = */ Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return@Thread
                    }
                    rec = AudioRecord.Builder()
                        .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                        .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(SR).setChannelMask(AudioFormat.CHANNEL_IN_MONO).build())
                        .setBufferSizeInBytes(kotlin.math.max(minBuf, frameBytes * 4))
                        .build()
                    rec?.startRecording()
                }
                val raw = ByteArray(frameBytes)
                var phase = 0.0
                val inc = 2.0 * Math.PI * 440.0 / SR
                while (running.get()) {
                    val read = if (testTone) {
                        var i=0
                        while (i<frameBytes) {
                            val s = (sin(phase) * 0.2 * Short.MAX_VALUE).toInt().toShort()
                            raw[i] = (s.toInt() and 0xFF).toByte()
                            raw[i+1] = ((s.toInt() ushr 8) and 0xFF).toByte()
                            phase += inc
                            i += 2
                        }
                        frameBytes
                    } else rec?.read(raw,0,raw.size) ?: 0
                    if (read>0) {
                        var m=0; var i=0
                        while (i<read) {
                            val v = ((raw[i+1].toInt() shl 8) or (raw[i].toInt() and 0xFF))
                            val av = if (v and 0x8000 != 0) (v - 0x10000) * -1 else v
                            if (av>m) m=av
                            i+=2
                        }
                        onLevel(m)
                        val header = java.nio.ByteBuffer.allocate(4).putInt(seq++)
                        val data = ByteArray(4+read)
                        System.arraycopy(header.array(),0,data,0,4)
                        System.arraycopy(raw,0,data,4,read)
                        try { socket?.send(DatagramPacket(data,data.size,dstAddr,dstPort)) } catch(e:Throwable) { Log.e(TAG,"send",e) }
                    } else Thread.sleep(2)
                }
            } catch (e:Throwable) { Log.e(TAG,"send loop",e) }
            finally {
                try { rec?.stop(); rec?.release() } catch (_: Throwable) {}
                try { socket?.close() } catch (_: Throwable) {}
                rec = null; socket = null
            }
        }
        thread!!.start()
    }
    fun stop() {
        running.set(false)
        try { socket?.close() } catch (_: Throwable) {}
        try { thread?.interrupt() } catch (_: Throwable) {}
        try { thread?.join(1000) } catch (_: Throwable) {}
        try { rec?.stop() } catch (_: Throwable) {}
        try { rec?.release() } catch (_: Throwable) {}
        socket = null; rec = null
    }
}
