package my.example.simplearapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Projection;

public class FixLocation extends MyLocationOverlay {

	private boolean bugged = false;
	private Drawable drawable;
	private Paint accuracyPaint;
	private int width;
	private int height;
	private Point center;
	private Point left;

	public FixLocation(Context context, MapView mapView) {
		super(context, mapView);
	}

	/* 
	 * We check to see whether it is bugged or not.
	 * We try to let it run the normal course, but if we get an exception,
	 * we set bugged to true and then proceed to execute our own implementation of the work
	 * 
	 */
	@Override
	protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
		//TODO brutal code!
		if(!bugged) {
			try {
				super.drawMyLocation(canvas, mapView, lastFix, myLocation, when);
			} catch (Exception e) {
				// we found a buggy phone, draw the location icons ourselves
				bugged = true;
			}
		}
		if(bugged) {
			if(drawable == null) {
				accuracyPaint = new Paint();
				accuracyPaint.setAntiAlias(true);
				accuracyPaint.setStrokeWidth(2.0f);
				drawable = mapView.getContext().getResources().getDrawable(R.drawable.ic_maps_indicator_current_position);
				width = drawable.getIntrinsicWidth();
				height = drawable.getIntrinsicHeight();
				center = new Point();
				left = new Point();
			}
			Projection projection = mapView.getProjection();
			double latitude = lastFix.getLatitude();
			double longitude = lastFix.getLongitude();
			float accuracy = lastFix.getAccuracy();
			float[] result = new float[1];
			Location.distanceBetween(latitude, longitude, latitude, longitude + 1, result);
			float longitudeLineDistance = result[0];
			GeoPoint leftGeo = new GeoPoint((int)(latitude*1e6), (int)((longitude-accuracy/longitudeLineDistance)*1e6));
			projection.toPixels(leftGeo, left);
			projection.toPixels(myLocation, center);
			int radius = center.x - left.x;
			accuracyPaint.setColor(0xff6666ff);
			accuracyPaint.setStyle(Style.STROKE);
			canvas.drawCircle(center.x, center.y, radius, accuracyPaint);
			accuracyPaint.setColor(0x186666ff);
			accuracyPaint.setStyle(Style.FILL);
			canvas.drawCircle(center.x, center.y, radius, accuracyPaint);
			drawable.setBounds(center.x - width/2, center.y - height/2, center.x + width/2, center.y + height/2);
			drawable.draw(canvas);
		}
	}

}
