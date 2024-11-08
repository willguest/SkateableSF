package com.example.skateable_sf.WT901BLE.activity;

/*
 * Activity for scanning and displaying available Bluetooth LE devices.
 */

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.example.skateable_sf.WT901BLE.R;

import com.example.skateable_sf.WT901BLE.databinding.ActDeviceBinding;
import com.example.skateable_sf.WT901BLE.databinding.ListitemDeviceBinding;

public class DeviceScanActivity extends AppCompatActivity {

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final String TAG = "DeviceScanActivity";

    private ListView mListView;
    private View mViewScan;
    private View mViewOkay;

    public final static int PERMISSION_REQUEST_FINE_LOCATION = 2;
    private SharedPreferences mSharedPrefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActDeviceBinding deviceBinding = ActDeviceBinding.inflate(getLayoutInflater());
        View view = deviceBinding.getRoot();
        setContentView(view);

        //Toolbar myToolbar = deviceBinding.myToolbar;
        //setSupportActionBar(myToolbar);

        setTitle(R.string.pick_use);
        ListView mListView = deviceBinding.listView;

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox cb = ((ViewHolder) view.getTag()).use;
                cb.toggle();
                mLeDeviceListAdapter.mUse.set(i, cb.isChecked());
            }
        });

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mListView.setAdapter(mLeDeviceListAdapter);

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes acc Bluetooth adapter.  For API level 18 and above, get acc reference to
        // BluetoothAdapter through BluetoothManager.
        //final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mSharedPrefs = getSharedPreferences("General", Activity.MODE_PRIVATE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.act_scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.okay) {
            final Intent intent = new Intent(this, DeviceControlActivity.class);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return true;
        } else if (itemId == R.id.refresh) {
            if (mScanning) {
                scanLeDevice(false);
            } else {
                mLeDeviceListAdapter.clear();
                mLeDeviceListAdapter.notifyDataSetChanged();
                scanLeDevice(true);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // Android M Permission check
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
        }

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display acc dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        scanLeDevice(false);

        Set<BluetoothDevice> usingSensors = new HashSet<>();
        for (int i = 0; i < mLeDeviceListAdapter.mLeDevices.size(); i++) {
            if (Boolean.TRUE.equals(mLeDeviceListAdapter.mUse.get(i))) {
                usingSensors.add(mLeDeviceListAdapter.mLeDevices.get(i));
            }
        }
        setUsingSensors(usingSensors);
        mLeDeviceListAdapter.clear();
        mLeDeviceListAdapter.notifyDataSetChanged();
    }

    private synchronized void scanLeDevice(final boolean enable) {
        if (enable) {
            if (!mScanning) {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
                        }
                    }
                }, SCAN_PERIOD);

                // Start scanning
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Starting scan");
                    mScanning = true;
                    mBluetoothAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
                }
            }
        } else {
            mScanning = false;
            Log.d(TAG, "Stopping scan");
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
        }
    }

    private boolean isUsingSensor(BluetoothDevice device) {
        return mSharedPrefs.getStringSet("Using-devices", new HashSet<String>()).contains(device.getAddress());
    }

    private void setUsingSensors(Set<BluetoothDevice> devices) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        Set<String> deviceAddresses = new HashSet<>();
        Set<String> deviceNames = new HashSet<>();

        for (BluetoothDevice device : devices)
            deviceAddresses.add(device.getAddress());

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            for (BluetoothDevice device : devices)
                deviceNames.add(device.getName() + " - " + device.getAddress());
        }

        editor.putStringSet("Using-devices", deviceAddresses);
        editor.putStringSet("Using-devices-names", deviceNames);
        editor.apply();
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private final ArrayList<BluetoothDevice> mLeDevices;
        private final ArrayList<Integer> mRSSIs;
        private final ArrayList<ScanRecord> mRecords;
        private final ArrayList<Boolean> mUse;
        private final LayoutInflater mInflater;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mRSSIs = new ArrayList<>();
            mRecords = new ArrayList<>();
            mInflater = DeviceScanActivity.this.getLayoutInflater();
            mUse = new ArrayList<>();
        }

        public void addDevice(BluetoothDevice device, int rssi, ScanRecord record) {
            String address = device.getAddress();
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                mRSSIs.add(rssi);
                mRecords.add(record);
                mUse.add(isUsingSensor(device));
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
            mRSSIs.clear();
            mRecords.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;


            // General ListView optimization code.
            if (view == null) {
                ListitemDeviceBinding listItemBinding = ListitemDeviceBinding.inflate(getLayoutInflater());
                View listItemView = listItemBinding.getRoot();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    view = mInflater.inflate(listItemView.getSourceLayoutResId(), null);
                }
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = listItemBinding.deviceAddress;
                viewHolder.deviceName = listItemBinding.deviceName;
                viewHolder.use = listItemBinding.use;
                assert view != null;
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = mRecords.get(i).getDeviceName();

            final String displayName;
            if (deviceName != null && !deviceName.trim().isEmpty()) {
                displayName = deviceName.trim() + "  RSSI:" + mRSSIs.get(i).toString();
            }
            else {
                displayName = getString(R.string.unknown_device) + "  RSSI:" + mRSSIs.get(i).toString();
            }

            viewHolder.deviceName.setText(displayName);
            viewHolder.deviceAddress.setText(device.getAddress());
            viewHolder.use.setChecked(mUse.get(i));

            return view;
        }
    }

    private void addDeviceToAdapter(BluetoothDevice device, int rssi, ScanRecord record) {
        mLeDeviceListAdapter.addDevice(device, rssi, record);
        mLeDeviceListAdapter.notifyDataSetChanged();
    }

    // Device scan callback.
    private final LeScanCallback mLeScanCallback = new LeScanCallback() {
        @Override
        public void onScanResult(final ScanResult result) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ScanRecord record = result.getScanRecord();
                    BluetoothDevice device = result.getDevice();
                    int Rssi = result.getRssi();

                    if (record == null || device == null) {
                        return;
                    }
                    Log.d("--", "scan result found " + record.getDeviceName());

                    addDeviceToAdapter(device, Rssi, record);
                }
            });
        }
    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        CheckBox use;
    }
}