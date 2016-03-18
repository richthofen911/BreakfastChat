package io.ap1.backendlesschattest;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.Message;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.backendless.messaging.PublishStatusEnum;

import java.net.PortUnreachableException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter;
    //channel name with be the same as new bluetooth adapter name
    String btNameOrigin = "unknown";
    private BroadcastReceiver mReceiver;
    public AdapterUserInList adapterUserInList;
    //public static String myUserObjectId = "F4CEADE3-459F-8E00-FF6D-CF7D1B6D7C00";
    public static String myUserObjectId = "3AB6934F-DD15-3667-FF1B-532196D08400";
    RecyclerView recyclerView;
    TextView tvMe;
    TextView tvStatus;
    LinearLayoutManager linearLayoutManager;
    private String myChannel;
    public static SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.CANADA);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Backendless.initApp(this, getString(R.string.BACKENDLESS_APP_ID),
                getString(R.string.BACKENDLESS_SECRET_KEY), getString(R.string.BACKENDLESS_APP_VERSION));
        Backendless.setUrl("http://159.203.15.85/api"); //Digital Ocean host

        tvMe = (TextView) findViewById(R.id.tv_me);
        tvMe.setText(myUserObjectId);
        tvStatus = (TextView) findViewById(R.id.tv_status);

        adapterUserInList = new AdapterUserInList(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_user_list);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterUserInList);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null && !bluetoothAdapter.isEnabled())
            bluetoothAdapter.enable();
        ensureDiscoverable();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String detectedDeviceName = device.getName();
                    if(detectedDeviceName != null)
                        Log.e("device detected", detectedDeviceName);
                    if(detectedDeviceName != null && detectedDeviceName.startsWith("proximity/")){
                        Log.e("proximity user found", device.getName() + "\n" + device.getAddress());
                        retrieveDetectedUserObject(getTargetUserObjectId(detectedDeviceName));
                    }

                    // When discovery is finished, change the Activity title
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    tvStatus.setText("scan finished");
                    Log.e("device discovery", "finished");
                }
            }
        };
        registerMyReceiver(this);
        changeBTNameForThisApp(myUserObjectId);


        if(!checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        }else {
            if (!bluetoothAdapter.isDiscovering()){
                bluetoothAdapter.startDiscovery();
                tvStatus.setText("scanning");
            }

        }

    }

    private void registerMyReceiver(Context context) {
        context.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        context.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    private void changeBTNameForThisApp(String myUserObjectId){
        btNameOrigin = bluetoothAdapter.getName();
        myChannel = "proximity/chat/" + myUserObjectId;
        bluetoothAdapter.setName(myChannel);
    }

    private void publishToTargetChannel(String targetChannel, String msgType, String msgContent){
        PublishOptions publishOptions = new PublishOptions();
        publishOptions.putHeader("type", msgType);
        publishOptions.setPublisherId(myUserObjectId);
        Backendless.Messaging.publish(targetChannel, msgContent, publishOptions, new AsyncCallback<MessageStatus>() {
            @Override
            public void handleResponse(MessageStatus messageStatus) {
                PublishStatusEnum msgStatus = messageStatus.getStatus();
                if (msgStatus == PublishStatusEnum.FAILED)
                    Toast.makeText(MainActivity.this, "Failed to send the message: " + messageStatus.getMessageId(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                Toast.makeText(MainActivity.this, "Send message, Error: " + backendlessFault.getDetail(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subscribeToMyChannel(String myUserObjectId){
        Backendless.Messaging.subscribe(myChannel, new AsyncCallback<List<Message>>() {
            @Override
            public void handleResponse(List<Message> messages) {
                for (Message message : messages) {
                    String publisherId = message.getPublisherId();
                    /*
                    Map<String, String> header = message.getHeaders();
                    String msgType = header.get("type");
                    String content = (String) message.getData();
                    String msgTimeStamp = timeFormat.format(message.getTimestamp());
                    */
                    for(int i = 0; i < DataStore.userList.size(); i++){
                        if(publisherId.equals(DataStore.userList.get(i).getUserObjectId())){
                            DataStore.userList.get(i).addToMessageList(message);
                            adapterUserInList.notifyItemChanged(i);
                        }
                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                Toast.makeText(MainActivity.this, backendlessFault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterMessage(String msgType, String publisherId){
        if(msgType.equals(Constants.CHAT_MESSAGE_TYPE_REQUEST)){
            if(!DataStore.blockList.contains(publisherId)){ // if publishId is in the blockList, ignore the message
                // pop up a dialog, asking if you want the detected user to chat with you

            }
        }
    }

    private void retrieveDetectedUserObject(final String detectedUserObjectId){
        Backendless.Persistence.of( BackendlessUser.class ).findById(detectedUserObjectId, new DefaultCallback<BackendlessUser>(this) {
            @Override
            public void handleResponse(BackendlessUser response) {
                Log.e("detect user info", response.getProperty("name") + "\n" + response.getProperty("pictureUrl"));
                if(!DataStore.userList.contains(response)){
                    //DataStore.userList.add(DataStore.userList.size(), response);
                    DataStore.userList.add(new MyBackendlessUser(response));
                    adapterUserInList.notifyItemInserted(DataStore.userList.size());
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                super.handleFault(fault);
                Log.e("Handle fault", fault.toString());
            }
        });
    }



    private String getTargetUserObjectId(String targetChannel){
        String[] components = targetChannel.split("/");
        return components[2];
    }

    public boolean checkPermission(Context context, String permissionName){
        return (ContextCompat.checkSelfPermission(context, permissionName) == PackageManager.PERMISSION_GRANTED);
    }

    public void requestPermission(Activity activity, String permissionName){
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionName)){
            Log.e("request reason", "need the permission");
        }else {
            Log.e("request permission", permissionName);
            ActivityCompat.requestPermissions(activity, new String[]{permissionName}, 1);
        }
    }

    /*
    public void addElement(int pos, BackendlessUser backendlessUser){
        DataStore.userList.add(pos, backendlessUser);
        adapterUserInList.notifyItemInserted(pos);
        if (pos != DataStore.userList.size() - 1) {
            adapterUserInList.notifyItemRangeChanged(pos, DataStore.userList.size() - pos);
        }
    }
    */

    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(discoverableIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!bluetoothAdapter.isDiscovering()){
                        bluetoothAdapter.startDiscovery();
                        tvStatus.setText("scanning");
                    }

                } else {
                    Toast.makeText(this, "no permission to run this app", Toast.LENGTH_SHORT).show();
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onDestroy(){
        if(bluetoothAdapter != null){
            bluetoothAdapter.setName(btNameOrigin);
            bluetoothAdapter.cancelDiscovery();
        }

        this.unregisterReceiver(mReceiver);

        super.onDestroy();
    }
}
