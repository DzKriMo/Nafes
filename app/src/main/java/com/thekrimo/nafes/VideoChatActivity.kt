    package com.thekrimo.nafes

    import android.os.Bundle
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import com.thekrimo.nafes.databinding.ActivityVideoChatBinding
    import com.google.firebase.firestore.FirebaseFirestore
    import org.webrtc.*

    class VideoChatActivity : AppCompatActivity() {
        private lateinit var binding: ActivityVideoChatBinding
        private lateinit var firebaseFirestore: FirebaseFirestore
        private lateinit var peerConnectionFactory: PeerConnectionFactory
        private lateinit var localVideoTrack: VideoTrack
        private lateinit var remoteVideoTrack: VideoTrack
        private lateinit var localVideoView: SurfaceViewRenderer
        private lateinit var remoteVideoView: SurfaceViewRenderer
        private lateinit var peerConnection: PeerConnection
        private lateinit var localMediaStream: MediaStream
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            binding = ActivityVideoChatBinding.inflate(layoutInflater)
            setContentView(binding.root)
            firebaseFirestore = FirebaseFirestore.getInstance()

            // Initialize PeerConnectionFactory
            val options = PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)
            peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()

            // Initialize local media stream
            localMediaStream = peerConnectionFactory.createLocalMediaStream("local_media_stream")

            // Initialize local and remote video views
            localVideoView = binding.localVideoView
            remoteVideoView = binding.remoteVideoView

            // Create local video source and track
            val videoCapturer = getVideoCapturer()
            val videoSource = peerConnectionFactory.createVideoSource(videoCapturer != null)
            val videoTrack = peerConnectionFactory.createVideoTrack("video_track", videoSource)
            localVideoTrack = videoTrack
            localMediaStream.addTrack(videoTrack)

            // Setup local video view
            localVideoTrack.addSink(localVideoView)

            // Setup peer connection
            val rtcConfig = PeerConnection.RTCConfiguration(listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()))
            peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : CustomPeerConnectionObserver("peer_connection") {})!!

            // Add local stream to peer connection
            peerConnection.addStream(localMediaStream)
        }

        private fun getVideoCapturer(): VideoCapturer? {
            return Camera2Enumerator(this).run {
                deviceNames.find {
                    isFrontFacing(it)
                }?.let {
                    createCapturer(it, null)
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            localVideoTrack.dispose()
            peerConnection.dispose()
            peerConnectionFactory.dispose()
        }
    }

    // Custom PeerConnectionObserver to simplify callback handling
    open class CustomPeerConnectionObserver(private val logTag: String) : PeerConnection.Observer {
        override fun onIceCandidate(iceCandidate: IceCandidate) {}
        override fun onDataChannel(dataChannel: DataChannel) {}
        override fun onIceConnectionReceivingChange(p0: Boolean) {}
        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
        override fun onAddStream(mediaStream: MediaStream) {}
        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
        override fun onRemoveStream(mediaStream: MediaStream) {}
        override fun onRenegotiationNeeded() {}
        override fun onAddTrack(rtpReceiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {}
    }