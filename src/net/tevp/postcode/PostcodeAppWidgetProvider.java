package net.tevp.postcode;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.RemoteViews;

import java.text.DateFormat;

/**
 *
 */
public class PostcodeAppWidgetProvider extends AppWidgetProvider implements PostcodeListener {

    private final PostcodeBackend pb = new PostcodeBackend();
    private Location lastLocation = null;
    private Context context;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        pb.getPostcode(context, this, true);
        this.context = context;
        lastLocation = null;
        postcodeChange(context.getString(R.string.locating));
    }

    @Override
    public void postcodeChange(final String postcode) {
        final ComponentName thisWidget = new ComponentName(context, PostcodeAppWidgetProvider.class);
        final AppWidgetManager manager = AppWidgetManager.getInstance(context);
        final RemoteViews views = buildRemoteViews(context,postcode);
        views.setOnClickPendingIntent(R.id.refresh, createUpdatePendingIntent(manager.getAppWidgetIds(thisWidget)));
        views.setOnClickPendingIntent(R.id.postcodewidget,PendingIntent.getActivity(context,0,new Intent(context,Postcode.class),0));
        manager.updateAppWidget(thisWidget, views);
    }

    private PendingIntent createUpdatePendingIntent(final int[] widgetIds)
    {
        final Intent update = new Intent(context,PostcodeAppWidgetProvider.class);
        update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        return PendingIntent.getBroadcast(context,0,update,0);
    }

    @Override
    public void updatedLocation(final Location l) {
        lastLocation = l;
        postcodeChange(context.getString(R.string.updating));
    }

    @Override
    public void locationFindFail() {
        postcodeChange(pb.lastPostcode);
        lastLocation = pb.last;
    }

    @Override
    public void postcodeLookupFail() {
        postcodeChange(context.getString(R.string.lookup_fail));
        lastLocation = null;
    }

    private RemoteViews buildRemoteViews(final Context context,final String postCode) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
        views.setTextViewText(R.id.postcode, postCode);
        if(lastLocation!=null)
        {
            views.setTextViewText(R.id.time, DateFormat.getTimeInstance(DateFormat.SHORT).format(lastLocation.getTime()));
        }
        return views;
    }
}
