package io.snapback.sdk.wear;

import android.hardware.Sensor;
import android.util.SparseArray;

public class SensorStringTypes {
    public SparseArray<String> stringTypes;

    public SensorStringTypes() {
        stringTypes = new SparseArray<String>();

        stringTypes.append(0, "Debug Sensor");
        stringTypes.append(Sensor.TYPE_ACCELEROMETER, "Accelerometer");
        stringTypes.append(Sensor.TYPE_AMBIENT_TEMPERATURE, "Ambient temperatur");
        stringTypes.append(Sensor.TYPE_GAME_ROTATION_VECTOR, "Game Rotation Vector");
        stringTypes.append(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, "Geomagnetic Rotation Vector");
        stringTypes.append(Sensor.TYPE_GRAVITY, "Gravity");
        stringTypes.append(Sensor.TYPE_GYROSCOPE, "Gyroscope");
        stringTypes.append(Sensor.TYPE_GYROSCOPE_UNCALIBRATED, "Gyroscope (Uncalibrated)");
        stringTypes.append(Sensor.TYPE_HEART_RATE, "Heart Rate");
        stringTypes.append(Sensor.TYPE_LIGHT, "Light");
        stringTypes.append(Sensor.TYPE_LINEAR_ACCELERATION, "Linear Acceleration");
        stringTypes.append(Sensor.TYPE_MAGNETIC_FIELD, "Magnetic Field");
        stringTypes.append(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, "Magnetic Field (Uncalibrated)");
        stringTypes.append(Sensor.TYPE_PRESSURE, "Pressure");
        stringTypes.append(Sensor.TYPE_PROXIMITY, "Proximity");
        stringTypes.append(Sensor.TYPE_RELATIVE_HUMIDITY, "Relative Humidity");
        stringTypes.append(Sensor.TYPE_ROTATION_VECTOR, "Rotation Vector");
        stringTypes.append(Sensor.TYPE_SIGNIFICANT_MOTION, "Significant Motion");
        stringTypes.append(Sensor.TYPE_STEP_COUNTER, "Step Counter");
        stringTypes.append(Sensor.TYPE_STEP_DETECTOR, "Step Detector");
        
        stringTypes.append(65562, "Samsung Heart Rate");
        stringTypes.append(65536, "Wrist Tilt");
        stringTypes.append(65538, "Wellness Passive");
        stringTypes.append(65539, "User Profile");
        stringTypes.append(65537, "Detailed Step Counter");
    }

    public String getStringType(int sensorType) {
        String stringType = stringTypes.get(sensorType);

        if (stringType == null) {
        	stringType = "Unknown";
        }

        return stringType;
    }
}
