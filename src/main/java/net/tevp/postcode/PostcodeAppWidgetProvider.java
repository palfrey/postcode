package net.tevp.postcode;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.opengl.Visibility;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.DateFormat;

import static android.os.Build.VERSION.SDK_INT;

public class PostcodeAppWidgetProvider extends AppWidgetProvider implements PostcodeListener {

    private static final String ACTION_UPDATE_AND_OPEN = "ACTION_UPDATE_AND_OPEN";
    private static final String POSTCODE_PREFERENCE_NAME = "Postcode";
    private static final String LAST_POSTCODE_PREFERENCE_KEY = "lastPostcode";

    private final PostcodeBackend pb = new PostcodeBackend();
    private long lastLocationUpdateTime;
    private Context context;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        pb.getPostcode(context, this, true);
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(intent.getAction() == null)   {
            if(SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (manager != null) {
                    String lastPostcode = context.getSharedPreferences(POSTCODE_PREFERENCE_NAME, Activity.MODE_PRIVATE).getString(LAST_POSTCODE_PREFERENCE_KEY,"");
                    Toast.makeText(context, "Copied '"+lastPostcode+"' to clipboard ", Toast.LENGTH_SHORT).show();
                    manager.setPrimaryClip(ClipData.newPlainText("Postcode", lastPostcode));
                }
            }
        } else if (intent.getAction().equals(ACTION_UPDATE_AND_OPEN)) {
            this.context = context;
            pb.getPostcode(context, this, true);
            context.startActivity(new Intent(context,Postcode.class));
        }
    }

    @Override
    public void postcodeChange(final String postcode) {
        final ComponentName thisWidget = new ComponentName(context, PostcodeAppWidgetProvider.class);
        final AppWidgetManager manager = AppWidgetManager.getInstance(context);
        final RemoteViews views = buildRemoteViews(context,postcode);
        views.setOnClickPendingIntent(R.id.postcodewidget,createUpdatePendingIntent(manager.getAppWidgetIds(thisWidget), true));
        views.setOnClickPendingIntent(R.id.refresh, createUpdatePendingIntent(manager.getAppWidgetIds(thisWidget), false));

        if(SDK_INT > 11) {
            views.setOnClickPendingIntent(R.id.copy, createCopyPendingIntent(manager.getAppWidgetIds(thisWidget)));
            views.setViewVisibility(R.id.copy, View.VISIBLE);
        }
        views.setViewVisibility(R.id.refresh, View.VISIBLE);
        manager.updateAppWidget(thisWidget, views);
        SharedPreferences pref = context.getSharedPreferences(POSTCODE_PREFERENCE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(LAST_POSTCODE_PREFERENCE_KEY, postcode);
        editor.commit();
    }

    private PendingIntent createUpdatePendingIntent(final int[] widgetIds, boolean showPostcodeApp) {
        final Intent update = new Intent(context, PostcodeAppWidgetProvider.class);
        if(showPostcodeApp) {
            update.setAction(ACTION_UPDATE_AND_OPEN);
        }
        else{
            update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        }

        update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        return PendingIntent.getBroadcast(context,0,update,0);
    }

    private PendingIntent createCopyPendingIntent(final int[] widgetIds) {
        final Intent update = new Intent(context, PostcodeAppWidgetProvider.class);
        update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        return PendingIntent.getBroadcast(context,0,update,0);
    }

    @Override
    public void updatedLocation(final Location location) {
        lastLocationUpdateTime = location.getTime();
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
        views.setTextViewText(R.id.time, DateFormat.getTimeInstance(DateFormat.SHORT).format(lastLocationUpdateTime));
        return views;
    }
}
