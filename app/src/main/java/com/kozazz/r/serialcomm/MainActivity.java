package com.kozazz.r.serialcomm;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "dubu";
    private TextView mDumpTextView;
    private ScrollView mScrollView;

    private Sensor mAccelerometer;
    private Sensor mMagneticField;

    private SensorManager mSensorManager;
    private PowerManager mPowerManager;
    private WindowManager mWindowManager;
    private Display mDisplay;
    private PowerManager.WakeLock mWakeLock;

    // Gravity rotational data
    private float gravity[];
    // Magnetic rotational data
    private float magnetic[]; //for magnetic rotational data
    private float accels[] = new float[3];
    private float mags[] = new float[3];
    private float[] values = new float[3];

    // azimuth, pitch and roll
    private float azimuth;
    private float pitch;
    private float roll;

    private static UsbSerialPort sPort = null;
    private SerialInputOutputManager mSerialIoManager;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };

    @Override
    protected void onResume() {
        super.onResume();

//        onDeviceStateChange();

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_FASTEST);

        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

// Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }

// Read some data! Most have just one port (port 0).
        UsbSerialPort port = driver.getPorts().get(0);
        sPort = port;


        if (sPort == null) {

        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

             connection = usbManager.openDevice(sPort.getDriver().getDevice());
            if (connection == null) {
//                mTitleTextView.setText("Opening device failed");
                return;
            }

            try {
                sPort.open(connection);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);


            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
//                mTitleTextView.setText("Error opening device: " + e.getMessage());
                try {
                    sPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sPort = null;
                return;
            }
//            mTitleTextView.setText("Serial device: " + sPort.getClass().getSimpleName());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDumpTextView= (TextView) findViewById(R.id.rstitle);
        mScrollView = (ScrollView) findViewById(R.id.demoScroller);
        mDumpTextView.setText("hi dubu \n asdfaf \n asdfaf\naaa \n asdfsfd");



        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get an instance of the PowerManager
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Get an instance of the WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();

        // Create a bright wake lock
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
                .getName());


        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

// Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }

// Read some data! Most have just one port (port 0).
        UsbSerialPort port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            sPort = port;

//            byte buffer[] = new byte[16];
//            int numBytesRead = port.read(buffer, 1000);
//            byte buffer[] = new byte[] {(byte) '1'};
//            int numBytesWrite = port.write(buffer, 200);
//            Log.d(TAG, "Read " + numBytesWrite + " bytes.");



        } catch (IOException e) {
            // Deal with error.
        } finally {
            //                port.close();
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER || event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){

            // pass
        }else{
            return;
        }
        /*
         * record the accelerometer data, the event's timestamp as well as
         * the current time. The latter is needed so we can calculate the
         * "present" time during rendering. In this application, we need to
         * take into account how the screen is rotated with respect to the
         * sensors (which always return data in a coordinate space aligned
         * to with the screen in its native orientation).
         */

        // Rotation matrix based on current readings from accelerometer and magnetometer.


        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                mags = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accels = event.values.clone();
                break;
        }

        if (mags != null && accels != null) {
            gravity = new float[9];
            magnetic = new float[9];
            SensorManager.getRotationMatrix(gravity, null, accels, mags);
            float[] outGravity = new float[9];
            /// 기본
//            SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_Y,SensorManager.AXIS_X, outGravity);

//           SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X,SensorManager.AXIS_Y, outGravity);
          SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X,SensorManager.AXIS_Z, outGravity);
            SensorManager.getOrientation(outGravity, values);

            azimuth = values[0] * 57.2957795f;
            pitch =values[1] * 57.2957795f;
            roll = values[2] * 57.2957795f;
            mags = null;
            accels = null;
        }

        if(sPort != null){

//                byte buffer[] = new byte[] {(byte) pitch};
//                int numBytesWrite = sPort.write(buffer, 200);
//                Log.d(TAG, "Write" + numBytesWrite + " bytes.");

//            new SendDataTask().execute(new Float(pitch));
            new SendDataTask().execute(String.valueOf(values[1]));
        }else{
            new MockDataTask().execute(String.valueOf(values[1]));

        }


        Log.e("pitch aa", String.format("%s %s %s ", azimuth , pitch , roll));
//            Log.e("pitch", String.format("%s %s %s ", values[0], values[1], values[2]));
//        Log.e("pitch", String.format("%s",  new Float(values[1])));


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }
    private void updateReceivedData(byte[] data) {
        final String message = "Readd " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";
//        LocalDateTime now = LocalDateTime.now();
//        mDumpTextView.setText(String.valueOf(now.getSecond()));
//        mRsTitle.setText("resv");
        mDumpTextView.append(message);
        mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
    }

    private class SendDataTask extends AsyncTask<String, Integer, Integer> {
        protected Integer doInBackground(String... data) {

            String str = data[0]+"\n";
//                    Log.e("pitch", String.format("%s", str ));
//            byte buffer[] = new byte[]{str.getBytes()};
            int numBytesWrite = 0;
            try {
//                numBytesWrite = sPort.write("-1.23456789".getBytes(), 200);
//                numBytesWrite = sPort.write("-1.2345".getBytes(), 200);
                numBytesWrite = sPort.write(str.getBytes(), 20);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            Log.d(TAG, "Write" + numBytesWrite + " bytes.");

            return numBytesWrite;
        }

        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
//            showDialog("Downloaded " + result + " bytes");
        }
    }


    private class MockDataTask extends AsyncTask<String, Integer, Integer> {
        protected Integer doInBackground(String... data) {

            String str = data[0];
//            Log.e("mock pitch", String.format("%s", str ));
//            byte buffer[] = new byte[]{f.byteValue()};
            int numBytesWrite = 0;
//            Log.d(TAG, "Write" + numBytesWrite + " bytes.");
            return numBytesWrite;
        }

        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
//            showDialog("Downloaded " + result + " bytes");
        }
    }
}
