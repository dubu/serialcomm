package com.kozazz.r.serialcomm;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "dubu";

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

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_GAME);



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

            for (int i = 0; i < 10; i++) {

                byte buffer[] = new byte[] {(byte) '1'};
                int numBytesWrite = port.write(buffer, 200);
                Log.d(TAG, "Write" + numBytesWrite + " bytes.");


                buffer = new byte[] {(byte) '0'};
                numBytesWrite = port.write(buffer, 200);
                Thread.sleep(2000L);

            }


        } catch (IOException e) {
            // Deal with error.
        } catch (InterruptedException e) {
            e.printStackTrace();
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
//                SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X,SensorManager.AXIS_Z, outGravity);
            SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_Y,SensorManager.AXIS_X, outGravity);
            SensorManager.getOrientation(outGravity, values);

            azimuth = values[0] * 57.2957795f;
            pitch =values[1] * 57.2957795f;
            roll = values[2] * 57.2957795f;
            mags = null;
            accels = null;
        }

        try {
            if(sPort != null){

                byte buffer[] = new byte[] {(byte) pitch};
                int numBytesWrite = sPort.write(buffer, 200);
                Log.d(TAG, "Write" + numBytesWrite + " bytes.");
            }
        } catch (IOException x) {

        }


//        Log.e("pitch", String.format("%s %s %s ", azimuth , pitch , roll));
//            Log.e("pitch", String.format("%s %s %s ", values[0], values[1], values[2]));





    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
