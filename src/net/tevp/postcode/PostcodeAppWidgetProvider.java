package net.tevp.postcode;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.location.Location;
import android.widget.RemoteViews;

import java.text.DateFormat;

/**
 * TODO: Add in the pending intents to handle, click to start the main app, click on accuracy to refresh
 * TODO: Handle size changes to show extras  - and persist post code  / location
 */
public class PostcodeAppWidgetProvider extends AppWidgetProvider implements PostcodeListener {

    private final PostcodeBackend pb = new PostcodeBackend();
    private Location lastLocation = null;
    private Context context;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        pb.getPostcode(context, this, true);
        this.context = context;
    }

    @Override
    public void postcodeChange(final String postcode) {
        RemoteViews views = buildRemoteViews(context,postcode);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, PostcodeAppWidgetProvider.class);
        manager.updateAppWidget(thisWidget, views);
    }

    @Override
    public void updatedLocation(final Location l) {
        lastLocation = l;
    }

    @Override
    public void locationFindFail() {
    }

    @Override
    public void postcodeLookupFail() {
    }

    private RemoteViews buildRemoteViews(final Context context,final String postCode) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
        views.setTextViewText(R.id.postcode, postCode);
        views.setTextViewText(R.id.accuracy, Integer.toString((int) lastLocation.getAccuracy()));
        views.setTextViewText(R.id.location, String.format("%2.2f / %2.2f", lastLocation.getLatitude(),lastLocation.getLongitude()));
        views.setTextViewText(R.id.time, DateFormat.getTimeInstance().format(lastLocation.getTime()));
        return views;
    }
}
