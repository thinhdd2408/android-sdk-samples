package com.stringee.apptoappcallsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stringee.apptoappcallsample.utils.Utils;
import com.stringee.call.StringeeCall;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luannguyen on 10/26/2017.
 */

public class IncomingCallActivity extends AppCompatActivity implements View.OnClickListener {

    private FrameLayout mLocalViewContainer;
    private FrameLayout mRemoteViewContainer;
    private TextView tvFrom;
    private TextView tvState;
    private ImageButton btnAnswer;
    private ImageButton btnEnd;
    private ImageButton btnMute;
    private ImageButton btnSpeaker;
    private ImageButton btnVideo;
    private ImageButton btnSwitch;
    private View vControl;

    private StringeeCall mStringeeCall;
    private boolean isMute = false;
    private boolean isSpeaker = false;
    private boolean isVideo = false;

    public static final int REQUEST_PERMISSION_CALL = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        mStringeeCall = getIntent().getParcelableExtra("stringeecall");

        MainActivity.callNum++;

        mLocalViewContainer = (FrameLayout) findViewById(R.id.v_local);
        mRemoteViewContainer = (FrameLayout) findViewById(R.id.v_remote);

        tvFrom = (TextView) findViewById(R.id.tv_from);
        tvFrom.setText(mStringeeCall.getFrom());

        tvState = (TextView) findViewById(R.id.tv_state);

        btnAnswer = (ImageButton) findViewById(R.id.btn_answer);
        btnAnswer.setOnClickListener(this);

        btnEnd = (ImageButton) findViewById(R.id.btn_end);
        btnEnd.setOnClickListener(this);

        btnMute = (ImageButton) findViewById(R.id.btn_mute);
        btnMute.setOnClickListener(this);
        btnSpeaker = (ImageButton) findViewById(R.id.btn_speaker);
        btnSpeaker.setOnClickListener(this);
        btnVideo = (ImageButton) findViewById(R.id.btn_video);
        btnVideo.setOnClickListener(this);
        btnSwitch = (ImageButton) findViewById(R.id.btn_switch);
        btnSwitch.setOnClickListener(this);

        isSpeaker = mStringeeCall.isVideoCall();
        if (isSpeaker) {
            btnSpeaker.setImageResource(R.drawable.ic_speaker_on);
        } else {
            btnSpeaker.setImageResource(R.drawable.ic_speaker_off);
        }

        vControl = findViewById(R.id.v_control);
        isVideo = mStringeeCall.isVideoCall();
        if (isVideo) {
            btnVideo.setVisibility(View.VISIBLE);
            btnVideo.setImageResource(R.drawable.ic_video);
        } else {
            btnVideo.setVisibility(View.INVISIBLE);
            btnVideo.setImageResource(R.drawable.ic_video_off);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> lstPermissions = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                lstPermissions.add(Manifest.permission.RECORD_AUDIO);
            }

            if (mStringeeCall.isVideoCall()) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    lstPermissions.add(Manifest.permission.CAMERA);
                }
            }

            if (lstPermissions.size() > 0) {
                String[] permissions = new String[lstPermissions.size()];
                for (int i = 0; i < lstPermissions.size(); i++) {
                    permissions[i] = lstPermissions.get(i);
                }
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CALL);
                return;
            }
        }

        initAnswer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        boolean isGranted = false;
        if (grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                } else {
                    isGranted = true;
                }
            }
        }
        switch (requestCode) {
            case REQUEST_PERMISSION_CALL:
                if (!isGranted) {
                    finish();
                } else {
                    initAnswer();
                }
                break;
        }
    }

    private void initAnswer() {
        mStringeeCall.setStateListener(new StringeeCall.StringeeCallStateListener() {
            @Override
            public void onStateChange(final StringeeCall call, final StringeeCall.CallState state, String description) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (state == StringeeCall.CallState.STARTING) {
                            tvState.setText("Starting");
                        } else if (state == StringeeCall.CallState.STARTED) {
                            tvState.setText("Started");
                        } else if (state == StringeeCall.CallState.END) {
                            tvState.setText("Ended");
                            if (mStringeeCall != null) {
                                if (MainActivity.callNum > 1) {
                                    mStringeeCall.stopCall();
                                } else {
                                    mStringeeCall.hangup();
                                }
                            }
                            finish();
                        }
                    }
                });
            }

            @Override
            public void onError(StringeeCall stringeeCall, int code, String description) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.reportMessage(IncomingCallActivity.this, "Fails to make call.");
                    }
                });
            }

            @Override
            public void onDTMFComplete(String callId, int requestId, int result) {

            }
        });

        mStringeeCall.setMediaListener(new StringeeCall.StringeeCallMediaListener() {
            @Override
            public void onLocalStream(final StringeeCall stringeeCall) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stringeeCall.isVideoCall()) {
                            mLocalViewContainer.addView(stringeeCall.getLocalView());
                            stringeeCall.renderLocalView(true);
                        }
                    }
                });
            }

            @Override
            public void onRemoteStream(final StringeeCall stringeeCall) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stringeeCall.isVideoCall()) {
                            mRemoteViewContainer.addView(stringeeCall.getRemoteView());
                            stringeeCall.renderRemoteView(false);
                        }
                    }
                });
            }

            @Override
            public void onCallInfo(StringeeCall stringeeCall, JSONObject jsonObject) {

            }
        });

        mStringeeCall.initAnswer(this, MainActivity.client);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_mute:
                isMute = !isMute;
                if (isMute) {
                    btnMute.setImageResource(R.drawable.ic_mute);
                } else {
                    btnMute.setImageResource(R.drawable.ic_mic);
                }
                if (mStringeeCall != null) {
                    mStringeeCall.mute(isMute);
                }
                break;
            case R.id.btn_speaker:
                isSpeaker = !isSpeaker;
                if (isSpeaker) {
                    btnSpeaker.setImageResource(R.drawable.ic_speaker_on);
                } else {
                    btnSpeaker.setImageResource(R.drawable.ic_speaker_off);
                }
                if (mStringeeCall != null) {
                    mStringeeCall.setSpeakerphoneOn(isSpeaker);
                }
                break;
            case R.id.btn_answer:
                vControl.setVisibility(View.VISIBLE);
                if (mStringeeCall != null) {
                    btnAnswer.setVisibility(View.GONE);
                    if (!mStringeeCall.isVideoCall()) {
                        btnVideo.setVisibility(View.VISIBLE);
                    }
                    mStringeeCall.answer();
                }
                break;
            case R.id.btn_end:
                if (mStringeeCall != null) {
                    if (MainActivity.callNum > 1) {
                        mStringeeCall.stopCall();
                    } else {
                        mStringeeCall.hangup();
                    }
                }
                finish();
                MainActivity.callNum--;
                break;
            case R.id.btn_video:
                isVideo = !isVideo;
                if (isVideo) {
                    btnVideo.setImageResource(R.drawable.ic_video);
                } else {
                    btnVideo.setImageResource(R.drawable.ic_video_off);
                }
                if (mStringeeCall != null) {
                    mStringeeCall.enableVideo(isVideo);
                }
                break;
            case R.id.btn_switch:
                if (mStringeeCall != null) {
                    mStringeeCall.switchCamera(null);
                }
                break;
        }
    }
}
