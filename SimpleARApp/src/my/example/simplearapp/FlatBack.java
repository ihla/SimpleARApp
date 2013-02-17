package my.example.simplearapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;



/* 
 * 	This Activity is called whenever the phone is held parallel to the ground and displays your current location on a map.
 */
public class FlatBack extends MapActivity {

	private SensorManager sensorManager;
	private int orientationSensor;
	private MapView mapView;
	private MyLocationOverlay myLocationOverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// main.xml contains a MapView
		setContentView(R.layout.map);
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		orientationSensor = Sensor.TYPE_ORIENTATION;
		sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(orientationSensor), SensorManager.SENSOR_DELAY_NORMAL);
		
		// extract MapView from layout
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		// create an overlay that shows our current location
		myLocationOverlay = new FixLocation(this, mapView);
		// add this overlay to the MapView and refresh it
		mapView.getOverlays().add(myLocationOverlay);
		mapView.postInvalidate();
		// call convenience method that zooms map on our location
		zoomToMyLocation();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false; //not used in app
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_toggle, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.map:
				if (mapView.isSatellite() == true) {
					mapView.setSatellite(false);
					mapView.setStreetView(true);
				}
				return true;
			case R.id.sat:
				if (mapView.isSatellite()==false) {
					mapView.setSatellite(true);
					mapView.setStreetView(false);
				}
				return true;
			case R.id.both:
				mapView.setSatellite(true);
				mapView.setStreetView(true);
			default:
				return super.onOptionsItemSelected(item);
			}
	}
	
	/* Logs orientation data and checks to see if the phone is no longer parallel to the ground 
	 * and then calls the custom method that will take us back to the camera preview 
	 */
	private final SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent sensorEvent) {
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				//TODO duplicate code - see in Main.SensorEventListener()
				float headingAngle = sensorEvent.values[0];
				float pitchAngle = sensorEvent.values[1];
				float rollAngle = sensorEvent.values[2];
				Log.d(Main.TAG, "Heading: " + String.valueOf(headingAngle));
				Log.d(Main.TAG, "Pitch: " + String.valueOf(pitchAngle));
				Log.d(Main.TAG, "Roll: " + String.valueOf(rollAngle));
				
				if (pitchAngle > 7 || pitchAngle < -7 || rollAngle > 7 || rollAngle < -7) {//TODO inverse logic as in Main.SensorEventListener()
					launchCameraView();
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
	};

	/* 
	 * 	The launchCameraView() method finishes the current activity so that we can get to the camera preview main activity
	 */
	protected void launchCameraView() {
		finish();
		
	}

	/* 
	 * 	This method applies a zoom level of 10 to the current location on the map
	 */
	private void zoomToMyLocation() {
		GeoPoint myLocationGeoPoint = myLocationOverlay.getMyLocation();
		if(myLocationGeoPoint != null) {
			mapView.getController().animateTo(myLocationGeoPoint);
			mapView.getController().setZoom(10);
		}
	}

}
