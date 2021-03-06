package io.ap1.backendlesschattest;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
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
import com.pubnub.api.Pubnub;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ActivityMain extends AppCompatActivity {

    private static final String TAG = "ActivityMain";

    public AppPubsubCallback appPubsubCallback;

    BluetoothAdapter bluetoothAdapter;
    //channel name with be the same as new bluetooth adapter name
    String btNameOrigin = "unknown";
    private BroadcastReceiver mReceiver;
    public AdapterUserInList adapterUserInList;

    public static String myUserObjectId;
    RecyclerView recyclerView;
    TextView tvMe;
    TextView tvStatus;
    LinearLayoutManager linearLayoutManager;
    private String myProximityDeviceName;
    public static SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.CANADA);

    public ServiceMessageCenter.BinderMsgCenter binderMsgCenter;

    public MyPubsubProviderClient myPubsubProviderClient;

    private ServiceConnection messageIOCenterConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "onCreate");

        Backendless.initApp(this, getString(R.string.BACKENDLESS_APP_ID),
                getString(R.string.BACKENDLESS_SECRET_KEY), getString(R.string.BACKENDLESS_APP_VERSION));
        Backendless.setUrl("http://159.203.15.85/api"); //Digital Ocean host

        myPubsubProviderClient = new MyPubsubProviderClient(new Pubnub("pub-c-af13868a-beb9-4719-82fc-8518ddfacea8", "sub-c-48ef81b4-f118-11e5-8f88-0619f8945a4f"));

        //Backendless.Messaging.
        if(Build.DEVICE.equals("hammerhead"))
            myUserObjectId = "F4CEADE3-459F-8E00-FF6D-CF7D1B6D7C00";
        else if(Build.DEVICE.equals("victara"))
            myUserObjectId = "3AB6934F-DD15-3667-FF1B-532196D08400";

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null && !bluetoothAdapter.isEnabled())
            bluetoothAdapter.enable();
        ensureDiscoverable();

        tvMe = (TextView) findViewById(R.id.tv_me);
        tvMe.setText(myUserObjectId);
        tvStatus = (TextView) findViewById(R.id.tv_status);


        adapterUserInList = new AdapterUserInList(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_user_list);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterUserInList);

        appPubsubCallback = new AppPubsubCallback(this, myUserObjectId, adapterUserInList, TAG);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String detectedDeviceName = device.getName();
                    if(detectedDeviceName != null && detectedDeviceName.startsWith("proximity/")) {
                        Log.e("proximity user found", device.getName() + "\n" + device.getAddress());
                        binderMsgCenter.getUserObjectByObjectId(getTargetUserObjectId(detectedDeviceName));
                    }

                    // When discovery is finished, change the Activity title
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    tvStatus.setText("scan finished");
                    //discoverDevices();
                    Log.e("device discovery", "finished");
                }
            }
        };

        messageIOCenterConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e("Service MessageCenter", "Connected");
                binderMsgCenter = (ServiceMessageCenter.BinderMsgCenter) service;

                binderMsgCenter.setMyAdapterUserInList(adapterUserInList);
                registerMyReceiver(ActivityMain.this);

                binderMsgCenter.setSubChannel("proximity_" + myUserObjectId);
                binderMsgCenter.setPubsubProviderClient(myPubsubProviderClient);
                binderMsgCenter.setPubsubCallback(appPubsubCallback);

                binderMsgCenter.subToChannel();

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e("Service MessageCenter", "Disconnected");
            }
        };

        bindServiceMsgIOCenter();
        changeBTNameForThisApp(myUserObjectId);

        discoverDevices();
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.e(TAG, "onStart");
        /*
        adapterUserInList = new AdapterUserInList(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_user_list);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterUserInList);
        */
    }

    private void discoverDevices(){
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
        myProximityDeviceName = "proximity/" + myUserObjectId;
        bluetoothAdapter.setName(myProximityDeviceName);
    }

    private String getTargetUserObjectId(String targetChannel){
        String[] components = targetChannel.split("/");
        return components[1];
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

    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {

            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(discoverableIntent);
        }
    }


    protected void bindServiceMsgIOCenter(){
        Log.e("trying to bind", "Service MessageIOCenter");
        Bundle bundle = new Bundle();
        bundle.putString("myUserObjectId", myUserObjectId);
        bindService(new Intent(ActivityMain.this, ServiceMessageCenter.class).putExtras(bundle), messageIOCenterConn, BIND_AUTO_CREATE);
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

    @Override
    public void onPause(){
        super.onPause();
        Log.e(TAG, "onPause");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.e(TAG, "onResume");
    }

    @Override
    public void onRestart(){
        super.onRestart();
        Log.e(TAG, "onRestart");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.e(TAG, "onStop");
    }

    public void onDestroy(){
        super.onDestroy();
        Log.e(TAG, "onDestroy");

        if(bluetoothAdapter != null){
            bluetoothAdapter.setName(btNameOrigin);
            bluetoothAdapter.cancelDiscovery();
            //bluetoothAdapter.disable();
        }

        DataStore.userList.clear();
        DataStore.duplicateCheck.clear();
        adapterUserInList.notifyItemRangeRemoved(0, DataStore.userList.size() - 1);

        this.unregisterReceiver(mReceiver);

        if(binderMsgCenter != null && binderMsgCenter.isBinderAlive())
            this.unbindService(messageIOCenterConn);

    }
}
