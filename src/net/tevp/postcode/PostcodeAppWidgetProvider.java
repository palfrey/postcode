package net.tevp.postcode;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 *
 */
public class PostcodeAppWidgetProvider extends AppWidgetProvider {
    private final static String TAG = PostcodeAppWidgetProvider.class.getName();

    private final static long FIVE_MINS_MS = 5 * 60 * 1000;
    private final static int FIVE_HUNDRED_M = 500;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("PostcodeAppWidgetProvider","onUpdate");
        context.startService(new Intent(context, UpdateService.class));
    }

    public static class UpdateService extends Service implements LocationListener {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setCostAllowed(false);
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String providerName = locationManager.getBestProvider(criteria, true);
            Location lastLocation = locationManager.getLastKnownLocation(providerName);
            onLocationChanged(lastLocation);
            Log.d("PostcodeAppWidgetProvider","onStartCommand:"+providerName);
            locationManager.requestLocationUpdates(providerName, FIVE_MINS_MS, FIVE_HUNDRED_M, UpdateService.this);
            return START_STICKY;
        }



        @Override
        public void onDestroy() {
            Log.d("PostcodeAppWidgetProvider","onDestroy");
            super.onDestroy();
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(this);
        }

        @Override
        public IBinder onBind(final Intent intent) {
            return null;
        }


        @Override
        public void onLocationChanged(final Location location) {
            Log.d("PostcodeAppWidgetProvider","onLocationChanged:"+location);

            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    RemoteViews views = buildRemoteViews(location);
                    AppWidgetManager manager = AppWidgetManager.getInstance(UpdateService.this);
                    ComponentName thisWidget = new ComponentName(UpdateService.this, PostcodeAppWidgetProvider.class);
                    manager.updateAppWidget(thisWidget, views);
                }
            };
            Thread tmpThread = new Thread(runner,"Widget Update");
            tmpThread.setDaemon(true);
            tmpThread.start();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("PostcodeAppWidgetProvider","onStatusChanged:");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("PostcodeAppWidgetProvider","onProviderEnabled:"+provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("PostcodeAppWidgetProvider","onProviderDisabled:"+provider);
        }

        private RemoteViews buildRemoteViews(Location location) {
            final RemoteViews views = new RemoteViews(getPackageName(), R.layout.appwidget);
            String postCode;
            try {
                postCode = PostcodeBackend.get(location.getLatitude(),location.getLongitude());
                views.setTextViewText(R.id.postcode, postCode);
                views.setTextViewText(R.id.accuracy, Float.toString(location.getAccuracy()));
            } catch (PostcodeException e) {
                Log.e(TAG, "Exception getting postcode for " + location, e);
            }
            return views;
        }
    }
}
