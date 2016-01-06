/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.BluetoothChat;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static com.example.android.BluetoothChat.R.drawable.button_shape_pink;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;
    public String SAVED_ENTITY;

    //
    int Case = 0;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private Button mS1Button;
    private Button mS2Button;
    private Button mENTButton;

    private Button mM1Button;
    private Button mM2Button;
    private Button mM3Button;
    private Button mM4Button;
    private Button mM5Button;
    private Button mM6Button;
    private Button mM7Button;
    private Button mM8Button;
    private Button mM9Button;
    private Button mM10Button;
    private Button mM11Button;
    private Button mCLRButton;

    //ボタンの背景部のTextView
    private TextView m1Background;
    private TextView m2Background;
    private TextView m3Background;
    private TextView m5Background;
    private TextView m6Background;
    private TextView m7Background;
    private TextView m8Background;
    private TextView s1Background;
    private TextView s2Background;
    private TextView clrBackground;
    private TextView entBackground;

    //ボタンを押した瞬間のボタンを取得
    private TextView pushedButton = null;
    //ボタンを押した後に受信した文字数を保存
    private int pushedNumber = 0;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    //振動フィードバック
    private Vibrator vibrator;
    //clr状態の保持
    private boolean clr_state = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(1000);

        if (D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        setContentView(R.layout.main);

        //create時のデフォルトカラーはグレイ
        m1Background = (TextView) findViewById(R.id.button_m1_background);
        m2Background = (TextView) findViewById(R.id.button_m2_background);
        m3Background = (TextView) findViewById(R.id.button_m3_background);
        m5Background = (TextView) findViewById(R.id.button_m5_background);
        m6Background = (TextView) findViewById(R.id.button_m6_background);
        m7Background = (TextView) findViewById(R.id.button_m7_background);
        m8Background = (TextView) findViewById(R.id.button_m8_background);
        s1Background = (TextView) findViewById(R.id.button_s1_background);
        s2Background = (TextView) findViewById(R.id.button_s2_background);
        entBackground = (TextView) findViewById(R.id.button_ent_background);
        clrBackground = (TextView) findViewById(R.id.button_clr_background);
        default_color();


        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {


        if (D) Log.e(TAG, "++ ON SaveInstanceState ++");


        super.onSaveInstanceState(outState);

        byte[] buf = null;
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(mChatService);
            buf = byteOut.toByteArray();

        } catch (Exception e) {
            // 何もしない
        }

        // byte配列で格納
        outState.putByteArray(SAVED_ENTITY, buf);


    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("Lifecycle", "+++onRestoreInstanceState()+++");

        // 保存値がある場合、エンティティを復帰
        if (savedInstanceState != null) {
            byte[] buf = savedInstanceState.getByteArray(SAVED_ENTITY);
            if (buf != null && buf.length > 0) {
                try {

                    ByteArrayInputStream byteInput = new ByteArrayInputStream(buf);
                    ObjectInputStream objectInput = new ObjectInputStream(byteInput);
                    mChatService = (BluetoothChatService) objectInput.readObject();
                    mChatService.start();

                } catch (Exception e) {
                    // 何もしない
                }
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.e(TAG, "++ ON START ++");


        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+ ON RESUME +");
        //Case = 0;

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        mS1Button = (Button) findViewById(R.id.button_s1);
        mS1Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = ".";
                sendMessage(message);
                vibrator.vibrate(100);

            }
        });
        mS2Button = (Button) findViewById(R.id.button_s2);
        mS2Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = ",";
                sendMessage(message);
                vibrator.vibrate(100);
            }
        });
        /*
        mS3Button = (Button) findViewById(R.id.button_s3);
        mS3Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "3";
                sendMessage(message);
                vibrator.vibrate(100);
            }
        });
        */
        mENTButton = (Button) findViewById(R.id.button_ent);
        mENTButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "e";
                sendMessage(message);
                vibrator.vibrate(100);
                pushedButton=null;
                clr_state = false;

            }
        });
        mM1Button = (Button) findViewById(R.id.button_m1);
        mM1Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "1";
                sendMessage(message);
                vibrator.vibrate(100);
                pushedNumber = 0;
                pushedButton = m1Background;
                //m1Background.setBackgroundResource(R.drawable.button_shape_bule);

            }
        });
        mM2Button = (Button) findViewById(R.id.button_m2);
        mM2Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "2";
                sendMessage(message);
                vibrator.vibrate(100);
                pushedNumber = 0;
                pushedButton = m2Background;
                //m2Background.setBackgroundResource(R.drawable.button_shape_bule);
            }
        });
        mM3Button = (Button) findViewById(R.id.button_m3);
        mM3Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "3";
                sendMessage(message);
                vibrator.vibrate(100);
                pushedNumber = 0;
                pushedButton = m3Background;
            }
        });
        /*mM4Button = (Button) findViewById(R.id.button_m4);
        mM4Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "4";
                sendMessage(message);
                vibrator.vibrate(100);
            }
        });*/
        mM5Button = (Button) findViewById(R.id.button_m5);
        mM5Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "5";
                sendMessage(message);
                vibrator.vibrate(100);
                pushedNumber = 0;
                pushedButton = m5Background;
            }
        });
        mM6Button = (Button) findViewById(R.id.button_m6);
        mM6Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "6";
                sendMessage(message);
                vibrator.vibrate(100);
                pushedNumber = 0;
                pushedButton = m6Background;
            }
        });
        mM7Button = (Button) findViewById(R.id.button_m7);
        mM7Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "7";
                sendMessage(message);
                vibrator.vibrate(100);
                pushedNumber = 0;
                pushedButton = m7Background;
            }
        });
        mM8Button = (Button) findViewById(R.id.button_m8);
        mM8Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "8";
                sendMessage(message);
                vibrator.vibrate(100);
                pushedNumber = 0;
                pushedButton = m8Background;
            }
        });
        /*mM9Button = (Button) findViewById(R.id.button_m9);
        mM9Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "9";
                sendMessage(message);
                vibrator.vibrate(100);
            }
        });
        mM10Button = (Button) findViewById(R.id.button_m10);
        mM10Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "o";
                sendMessage(message);
                vibrator.vibrate(100);
            }
        });
        mM11Button = (Button) findViewById(R.id.button_m11);
        mM11Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "p";
                sendMessage(message);
                vibrator.vibrate(100);
            }
        });*/
        mCLRButton = (Button) findViewById(R.id.button_clr);
        mCLRButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = "c";
                sendMessage(message);
                vibrator.vibrate(100);
                possible_color();

                clr_state = true;
            }
        });


        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }


    private void button_vibration(int msec) {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(msec);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        if (mChatService != null) mChatService.stop();

        if (D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if (D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        sendMessage(message);
                    }
                    if (D) Log.i(TAG, "END onEditorAction");
                    return true;
                }
            };

    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }

    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            //接続が確認されたらボタンを有効にする
                            button_state_all_true();
                            //ボタンは教示可能色pinkに切り替わる
                            possible_color();

                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:

                            //接続するまではボタンは無効
                            button_state_all_false();

                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);

                    //TODO 押したボタンに応じてボタンの色を変更する
                    if (clr_state == false) {
                        pushedNumber++;
                        if (pushedButton != null && pushedNumber == 10) {
                            pushedButton.setBackgroundResource(R.drawable.button_shape_bule);
                            pushedButton = null;
                        }
                    }

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(500);
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }

    //全ボタンのデフォルトカラーをセット
    private void default_color() {

        m1Background.setBackgroundResource(R.drawable.button_shape_gray);
        m2Background.setBackgroundResource(R.drawable.button_shape_gray);
        m3Background.setBackgroundResource(R.drawable.button_shape_gray);
        m5Background.setBackgroundResource(R.drawable.button_shape_gray);
        m6Background.setBackgroundResource(R.drawable.button_shape_gray);
        m7Background.setBackgroundResource(R.drawable.button_shape_gray);
        m8Background.setBackgroundResource(R.drawable.button_shape_gray);
        s1Background.setBackgroundResource(R.drawable.button_shape_gray);
        s2Background.setBackgroundResource(R.drawable.button_shape_gray);
        entBackground.setBackgroundResource(R.drawable.button_shape_gray);
        clrBackground.setBackgroundResource(R.drawable.button_shape_gray);

    }

    private void possible_color() {

        m1Background.setBackgroundResource(R.drawable.button_shape_pink);
        m2Background.setBackgroundResource(R.drawable.button_shape_pink);
        m3Background.setBackgroundResource(R.drawable.button_shape_pink);
        m5Background.setBackgroundResource(R.drawable.button_shape_pink);
        m6Background.setBackgroundResource(R.drawable.button_shape_pink);
        m7Background.setBackgroundResource(R.drawable.button_shape_pink);
        m8Background.setBackgroundResource(R.drawable.button_shape_pink);
        entBackground.setBackgroundResource(R.drawable.button_shape_green);
        clrBackground.setBackgroundResource(R.drawable.button_shape_green);
        s1Background.setBackgroundResource(R.drawable.button_shape_green);
        s2Background.setBackgroundResource(R.drawable.button_shape_green);

    }

    private void button_state_all_false() {
        mM1Button.setEnabled(false);
        mM2Button.setEnabled(false);
        mM3Button.setEnabled(false);
        mM5Button.setEnabled(false);
        mM6Button.setEnabled(false);
        mM7Button.setEnabled(false);
        mM8Button.setEnabled(false);
        mS1Button.setEnabled(false);
        mS2Button.setEnabled(false);
        mENTButton.setEnabled(false);
        mCLRButton.setEnabled(false);

    }

    private void button_state_all_true() {
        mM1Button.setEnabled(true);
        mM2Button.setEnabled(true);
        mM3Button.setEnabled(true);
        mM5Button.setEnabled(true);
        mM6Button.setEnabled(true);
        mM7Button.setEnabled(true);
        mM8Button.setEnabled(true);
        mS1Button.setEnabled(true);
        mS2Button.setEnabled(true);
        mENTButton.setEnabled(true);
        mCLRButton.setEnabled(true);

    }

}
