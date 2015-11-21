package net.tevp.postcode;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import org.json.*;
import android.location.*;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;
import android.os.Looper;

public class PostcodeBackend implements LocationListener  {
	public static final String TAG = "PostcodeBackend";
	Location last = null;
	static Timer timer = new Timer(true);
	TimerTask ttLocator = null;

	private static String grabURL(String url) throws PostcodeException
	{
		URL what;
		try {
			what = new URL(url);
		} catch (MalformedURLException e1) {
			throw new PostcodeException("Malformed for"+url, e1);
		}
		InputStream data;
		int max = 1000, sofar=0;

		byte[] out = new byte[max];
		try {
			data = what.openStream();

			while (sofar<max)
			{
				int ret = data.read(out,sofar,max-sofar);
				if (ret == -1)
					break;
				sofar += ret;
			}
		} catch (IOException e1) {
			throw new PostcodeException("IOException during url grab of " + url, e1);
		}
		return new String(out, 0, sofar);
	}

	/* eliminate a few locations that definitely aren't in the UK */
	private static boolean definitelyNotInUK(double lat, double lon)
	{
		/* These are overestimates for the UK bounding box, based on clicking
		 * on locations in Google Maps. Anything not in this box definitely
		 * isn't in the UK, so don't check UK-only services
		 */
		final double bottom = -11.074219;
		final double left = 49.310799;
		final double top = 2.680664;
		final double right = 60.866312;

		if (lat > right || lat < left || lon > top || lon < bottom)
			return true;
		else
			return false;
	}

	private static String getUKPostcodes(double lat, double lon) throws PostcodeException
	{
		if (definitelyNotInUK(lat, lon))
			throw new NonUKLocation();
		String data = grabURL(String.format("http://www.uk-postcodes.com/latlng/%.8f,%.8f.json",lat,lon));
		try
		{
			JSONObject js = new JSONObject(data);
			return js.getString("postcode");
		}
		catch (JSONException e) {
			throw new PostcodeException("json issue", e);
		}
	}

	private static String getPostcodesIO(double lat, double lon) throws PostcodeException
	{
		if (definitelyNotInUK(lat, lon))
			throw new NonUKLocation();
		String data = grabURL(String.format("http://api.postcodes.io/postcodes?lon=%.8f&lat=%.8f&limit=1", lon,lat));
		try
		{
			JSONObject js = new JSONObject(data);
			return js.getJSONArray("result").getJSONObject(0).getString("postcode");
		}
		catch (JSONException e) {
			System.out.println(e);
			throw new PostcodeException("json issue", e);
		}
	}

	private static String getGeonames(double lat, double lon) throws PostcodeException
	{
		String data = grabURL(String.format("http://ws.geonames.org/findNearbyPostalCodesJSON?lat=%.8f&lng=%.8f&maxRows=1",lat,lon));
		try
		{
			JSONObject js = new JSONObject(data);
			return js.getJSONArray("postalCodes").getJSONObject(0).getString("postalCode");
		}
		catch (JSONException e) {
			throw new PostcodeException("json issue", e);
		}
	}

	static String get(double lat, double lon) throws PostcodeException
	{
		PostcodeException old = null;
		int i=0;
		while(true)
		{
			try
			{
				switch (i)
				{
					case 0:
						return getPostcodesIO(lat,lon);
					case 1:
						return getGeonames(lat,lon);
					case 2:
						return getUKPostcodes(lat,lon);
					default:
						break;
				}
				break;
			}
			catch (NonUKLocation ue)
			{
				i++;
			}
			catch (PostcodeException pe)
			{
				old = pe;
				i++;
			}
		}
		if (old == null)
			throw new PostcodeException("Failed to get postcode");
		else
			throw old;
	}

	HashSet<PostcodeListener> pls = null;
	private final ReentrantLock plsLock = new ReentrantLock();

	String lastPostcode = null;
	LocationManager lm;

    public void getPostcode(Context context, final PostcodeListener callback)
	{
    	getPostcode(context, callback, false);
	}

    public void getPostcode(Context context, final PostcodeListener callback, boolean mustBeNew)
	{
		final PostcodeBackend self = this;
		ttLocator = new TimerTask() {
			public void run()
			{
				lm.removeUpdates(self);
				HashSet<PostcodeListener> myPls = getListeners();
				if (myPls == null)
					return;
				for (PostcodeListener pl: myPls)
					pl.locationFindFail();
			}
		};

		timer.schedule(ttLocator, 1000 * 60); // 1 minute before we give up

		Log.d(TAG, "Acquiring postcode from location");
		lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		Location l = null;
		for(final String provider: lm.getProviders(false))
		{
			if (l == null && !mustBeNew)
			{
				l = lm.getLastKnownLocation(provider);
				if (l!=null &&
					(((System.currentTimeMillis()-l.getTime())/1000.0) > 60 || // > 1 minute old
					!l.hasAccuracy() || l.getAccuracy() > 200)) // want only items accurate to <200m
				{
					Log.d(TAG, "Got old/crap data from "+provider);
					l = null;
				}
				else
					Log.d(TAG, "Got data from "+provider);
			}
			if (l == null)
			{
				final LocationManager internal_lm = lm;
				new Thread() { public void run() {
					Looper.prepare();
					internal_lm.requestLocationUpdates(provider, 0, 0, self);
					Looper.loop();
				}}.start();
			}
		}

		plsLock.lock();
		try
		{
			if (pls == null)
				pls = new HashSet<PostcodeListener>();
			pls.add(callback);
		}
		finally
		{
			plsLock.unlock();
		}

		if (l!= null)
		{
			final Location l2 = l;
			Thread t = new Thread() { public void run() {
				updatedLocation(l2);
			}};
			t.start();
		}
	}

	public HashSet<PostcodeListener> getListeners()
	{
		HashSet<PostcodeListener> myPls = null;
		plsLock.lock();
		try
		{
			myPls = pls;
			pls = null;
		}
		finally
		{
			plsLock.unlock();
		}
		return myPls;
	}

	public void updatedLocation(Location l)
	{
		HashSet<PostcodeListener> myPls = getListeners();
		if (myPls == null)
			return;

		lm.removeUpdates(this);
		if (ttLocator != null)
		{
			ttLocator.cancel();
			ttLocator = null;
		}
		Log.d(TAG, "Got an updated location");
		for (PostcodeListener pl: myPls)
			pl.updatedLocation(l);

		try
		{
			if (last == null || lastPostcode == null || l.getLatitude()!=last.getLatitude() || l.getLongitude() != last.getLongitude())
				lastPostcode = PostcodeBackend.get(l.getLatitude(),l.getLongitude());
		}
		catch(PostcodeException pe)
		{
			Log.e(TAG, "Parse error during new postcode", pe);
			for (PostcodeListener pl: myPls)
				pl.postcodeLookupFail();
			return;
		}

		last = l;
		Log.d(TAG, "Postcode is "+lastPostcode);
		Log.d(TAG, "Have "+Integer.toString(myPls.size())+" postcode listeners");
		for (PostcodeListener pl: myPls)
		{
			pl.postcodeChange(lastPostcode);
		}
	}

	public void onLocationChanged(Location l){
		if (!l.hasAccuracy() || l.getAccuracy() > 200) // want only items accurate to <200m
			return;
		if (((System.currentTimeMillis()-l.getTime())/1000.0) > 60) // > 1 minute old
			return;
		updatedLocation(l);
	}

	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}

	public static void main (String args[]) throws Exception
	{
		System.out.println(new PostcodeBackend().get(Double.valueOf(args[0]), Double.valueOf(args[1])));
	}
}
