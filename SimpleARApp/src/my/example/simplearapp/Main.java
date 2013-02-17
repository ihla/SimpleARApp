
package my.example.simplearapp;

import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Main extends Activity {
	public final static String TAG = "SARAPP";
    private LocationManager locationManager;
	private SensorManager sensorManager;
	private int orientationSensor;
	private int accelerometerSensor;
	private boolean inPreview;
	private SurfaceView cameraPreview;
	private SurfaceHolder previewHolder;
	private TextView headingValue;
	private TextView pitchValue;
	private TextView rollValue;
	private TextView xAxisValue;
	private TextView yAxisValue;
	private TextView zAxisValue;
	private TextView altitudeValue;
	private TextView latitudeValue;
	private TextView longitudeValue;
	private Button button;
	private Camera camera;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, locationListener);
        
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientationSensor = Sensor.TYPE_ORIENTATION;
        accelerometerSensor = Sensor.TYPE_ACCELEROMETER;
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(orientationSensor), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(accelerometerSensor), SensorManager.SENSOR_DELAY_NORMAL);
        
        /* #camera stuff
         * 	We will create an XML layout with the SurfaceView and then use that SurfaceView to display the cameraPreview.
         * 	We will also need a SurfaceHolder (previewHolder), which controls the behavior of our SurfaceView (for example, its size). 
         * 	It will also be notified when changes occur, such as when the preview starts
         * 	We need to register a SurfaceHolder.Callback (surfaceCallback) so that we are notified when our SurfaceView is ready or changes.
         * 	Then we tell the SurfaceView, via the SurfaceHolder, that it has the SURFACE_TYPE_PUSH_BUFFERS type (using setType()).
         * 	This indicates that something in the system will be updating the SurfaceView and providing the bitmap data to display.
         * 	Camera objects renders picture on SurfaceView by calling setPreviewDisplay().
         * 	However, the SurfaceView might not be ready immediately after being changed into SURFACE_TYPE_PUSH_BUFFERS mode.
         * 	Therefore, we should wait until the SurfaceHolder.Callback has its surfaceCreated() method called to create (Camera.Open())
         * 	and registering SurfaceHolder (camera.setPreviewDisplay(previewHolder)).
         */
        inPreview = false;
        cameraPreview = (SurfaceView)findViewById(R.id.cameraPreview);
        previewHolder = cameraPreview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
     
        
        headingValue = (TextView) findViewById(R.id.headingValue);
        pitchValue = (TextView) findViewById(R.id.pitchValue);
        rollValue = (TextView) findViewById(R.id.rollValue);
        
        xAxisValue = (TextView) findViewById(R.id.xAxisValue);
        yAxisValue = (TextView) findViewById(R.id.yAxisValue);
        zAxisValue = (TextView) findViewById(R.id.zAxisValue);
        
        altitudeValue = (TextView) findViewById(R.id.altitudeValue);
        longitudeValue = (TextView) findViewById(R.id.longitudeValue);
        latitudeValue = (TextView) findViewById(R.id.latitudeValue);
        
        button = (Button) findViewById(R.id.helpButton);
        button.setOnClickListener(new OnClickListener() {
	        	public void onClick(View v) {
	        	showHelp();
	        	}
        	});
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.help:
				showHelp();
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, locationListener);
		sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(orientationSensor), SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(accelerometerSensor), SensorManager.SENSOR_DELAY_NORMAL);
		//camera=Camera.open();???
	}
	
	@Override
	public void onPause() {
		if (inPreview) {
			camera.stopPreview();
		}
		locationManager.removeUpdates(locationListener);
		sensorManager.unregisterListener(sensorEventListener);
		if (camera != null) {//TODO camera referenced above w/o check!
			camera.release();
			camera=null;
		}
		inPreview=false;
		super.onPause();//TODO Always call the superclass method first???
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		camera.release();
		camera=null;
	}
	
    protected void showHelp() {
    	final Dialog dialog = new Dialog(this);
    	dialog.setContentView(R.layout.help);
    	dialog.setTitle("Help");
    	dialog.setCancelable(true);
    	//there are a lot of settings, for dialog, check them all out!
    	//set up text
    	TextView text = (TextView) dialog.findViewById(R.id.TextView01);
    	text.setText(R.string.help);
    	//set up button
    	Button button = (Button) dialog.findViewById(R.id.Button01);
    	button.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		dialog.cancel();
	    	}
    	});
    	//now that the dialog is set up, it's time to show it
    	dialog.show();
	}

	private final LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			double altitude = location.getAltitude();
			
			Log.d(TAG, "Latitude: " + String.valueOf(latitude));
			Log.d(TAG, "Longitude: " + String.valueOf(longitude));
			Log.d(TAG, "Altitude: " + String.valueOf(altitude));
			
			latitudeValue.setText(String.valueOf(latitude));
			longitudeValue.setText(String.valueOf(longitude));
			altitudeValue.setText(String.valueOf(altitude));
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	};

	private final SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				float headingAngle = event.values[0]; //azimuth
				float pitchAngle = event.values[1];
				float rollAngle = event.values[2];
				
				Log.d(TAG, "Heading: " + String.valueOf(headingAngle));
				Log.d(TAG, "Pitch: " + String.valueOf(pitchAngle));
				Log.d(TAG, "Roll: " + String.valueOf(rollAngle));
				
				headingValue.setText(String.valueOf(headingAngle));
				pitchValue.setText(String.valueOf(pitchAngle));
				rollValue.setText(String.valueOf(rollAngle));
				
				if (pitchAngle < 7 && pitchAngle > -7 && rollAngle < 7 && rollAngle > -7) {
					launchFlatBack();
				}
			}
			else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				float xAxis = event.values[0];
				float yAxis = event.values[1];
				float zAxis = event.values[2];
				
				Log.d(TAG, "X Axis: " + String.valueOf(xAxis));
				Log.d(TAG, "Y Axis: " + String.valueOf(yAxis));
				Log.d(TAG, "Z Axis: " + String.valueOf(zAxis));
				
				xAxisValue.setText(String.valueOf(xAxis));
				yAxisValue.setText(String.valueOf(yAxis));
				zAxisValue.setText(String.valueOf(zAxis));
			}
		}
		
	};

	private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		/* #camera stuff
		 * creates camera objects and registers SurfaceHolder
		 */
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			if (camera == null) {
					camera = Camera.open();
				}
				try {
					camera.setPreviewDisplay(previewHolder);
				}
				catch (Throwable t) {
					Log.e(TAG, "Exception in setPreviewDisplay()", t);
				}
		}

		/* #camera stuff
		 * The surfaceChanged() method is called if any changes are made by Android to the SurfaceView (after an orientation change, for example).
		 * we need to pass the configuration data to the Camera so it knows how big a preview it should be drawing
		 * As the picture size depends on hardware devices, we need to calculate preview size in runtime (getBestPreviewSize())
		 * Finally we start preview by calling camera.startPreview() 
		 */
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Camera.Parameters parameters = camera.getParameters();
			Camera.Size size = getBestPreviewSize(width, height, parameters);
			if (size != null) {
				parameters.setPreviewSize(size.width, size.height);
				camera.setParameters(parameters);
				camera.startPreview();
				inPreview = true;
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (camera != null) {
				camera.stopPreview();
				camera.setPreviewCallback(null);
				camera.release();
				camera = null;
			}
		}

		/* #camera stuff
		 * The getBestPreviewSize() method gets a list of available preview sizes and chooses the best one.
		 */
		private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
			Camera.Size result = null;
			for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
				if (size.width <= width && size.height <= height) {
					if (result == null) {
						result = size;
					}
					else {
						int resultArea = result.width * result.height;
						int newArea = size.width * size.height;
						if (newArea > resultArea) {
							result = size;
						}
					}
				}
			}
		return(result);
		}
	};

	public void launchFlatBack() {
		Intent flatBackIntent = new Intent(this, FlatBack.class);
		startActivity(flatBackIntent);
		}
}
