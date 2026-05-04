package com.pans.quicktalk5g
import android.annotation.SuppressLint
import android.media.*
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class AudioReceiver(private val listenPort:Int, private val onRx:(Int)->Unit = {}) {
    companion object { private const val TAG="AudioReceiver" }
    private val running = AtomicBoolean(false)
    private var rxThread: Thread? = null
    private var plThread: Thread? = null
    private val queue = ConcurrentLinkedQueue<ByteArray>()

    // resources that need to be closed from stop()
    @Volatile private var socket: DatagramSocket? = null
    @Volatile private var track: AudioTrack? = null

    @SuppressLint("Range")
    fun start() {
        if (running.getAndSet(true)) return
        rxThread = Thread {
            try {
                socket = DatagramSocket(listenPort)
                val buf = ByteArray(4096)
                while (running.get()) {
                    try {
                        val pkt = DatagramPacket(buf, buf.size)
                        socket?.receive(pkt)
                        val len = pkt.length - 4
                        if (len > 0) {
                            val pcm = ByteArray(len)
                            System.arraycopy(pkt.data, 4, pcm, 0, len)
                            queue.offer(pcm)
                            try { onRx(len) } catch(_:Throwable){}
                        }
                    } catch (e: Throwable) {
                        if (!running.get()) break
                        Log.e(TAG, "rx loop", e)
                    }
                }
            } catch (e: Throwable) {
                Log.e(TAG, "rx", e)
            } finally {
                try { socket?.close() } catch (_: Throwable) {}
                socket = null
            }
        }
        rxThread!!.start()

        plThread = Thread {
            val SR = 16000
            try {
                val minBuf = AudioTrack.getMinBufferSize(SR, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
                track = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                    .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(SR).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(minBuf * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setSessionId(AudioManager.AUDIO_SESSION_ID_GENERATE)
                    .build()
                try {
                    try {
                        val am = AppContextHolder.context.getSystemService(AudioManager::class.java)
                        am?.mode = AudioManager.MODE_IN_COMMUNICATION
                        am?.setSpeakerphoneOn(true)
                    } catch(_:Throwable){}
                    track?.play()
                    // wait until some buffered frames or until stopped; handle interruption gracefully
                    while (running.get() && queue.size < 3) {
                        try { Thread.sleep(5) }
                        catch (ie: InterruptedException) { Thread.currentThread().interrupt(); break }
                    }
                    while (running.get()) {
                        val frame = queue.poll()
                        if (frame != null) {
                            try { track?.write(frame, 0, frame.size) } catch (e: Throwable) { Log.e(TAG, "write", e) }
                        } else {
                            try { Thread.sleep(2) }
                            catch (ie: InterruptedException) { Thread.currentThread().interrupt(); break }
                        }
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "playback", e)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "play thread init", e)
            } finally {
                try { track?.stop() } catch (_: Throwable) {}
                try { track?.release() } catch (_: Throwable) {}
                track = null
            }
        }
        plThread!!.start()
    }

    fun stop() {
        running.set(false)
        try { socket?.close() } catch (_: Throwable) {}
        try { rxThread?.interrupt() } catch (_: Throwable) {}
        try { plThread?.interrupt() } catch (_: Throwable) {}
        try { rxThread?.join(1000) } catch (_: Throwable) {}
        try { plThread?.join(1000) } catch (_: Throwable) {}
        try { track?.stop() } catch (_: Throwable) {}
        try { track?.release() } catch (_: Throwable) {}
        socket = null; track = null; queue.clear()
    }
}
