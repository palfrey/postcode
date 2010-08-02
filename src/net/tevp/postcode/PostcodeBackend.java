package net.tevp.postcode;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

import org.json.*;
import android.location.*;
import java.util.Vector;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;

public class PostcodeBackend implements LocationListener  {	
	public static final String TAG = "PostcodeBackend";
    static Geohash e = new Geohash();
    
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
			throw new PostcodeException("IOException during url grab", e1);
		}

		return new String(out, 0, sofar);
	}

	private static String getUKPostcodes(double lat, double lon) throws PostcodeException
	{
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

	private static String getWhatIsMyPostcode(double lat, double lon) throws PostcodeException
	{
		String s = e.encode(lat, lon);
		String data = grabURL("http://whatismypostcode.appspot.com/"+s);
		return data;
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
		//return getUKPostcodes(lat,lon);
		return getWhatIsMyPostcode(lat,lon);
	}

	static Vector<PostcodeListener> pls = null;
	static String lastPostcode = null;

    public void getPostcode(Context context, final PostcodeListener callback)
	{
		Log.d(TAG, "Acquiring postcode from location");
		if (pls == null)
		{
			LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			Criteria c = new Criteria();
			String provider = lm.getBestProvider(c, false);
			lm.requestLocationUpdates(provider, 1000, 200, this);
			
			pls = new Vector<PostcodeListener>();
			pls.add(callback);
			
			final Location l = lm.getLastKnownLocation(provider);
			if (l!= null)
			{
				new Thread() { public void run() {
					updatedLocation(l);
				}};
			}
		}
		else
		{
			pls.add(callback);
			if (lastPostcode != null)
			{
				new Thread() { public void run() {
					callback.postcodeChange(lastPostcode);
				}};
			}
		}
	}

	public static void updatedLocation(Location l)
	{
		Log.d(TAG, "Got an updated location");
		try
		{
			lastPostcode = PostcodeBackend.get(l.getLatitude(),l.getLongitude());
			Log.d(TAG, "Postcode is "+lastPostcode);
			Log.d(TAG, "Have "+Integer.toString(pls.size())+" postcode listeners");
			for (final PostcodeListener pl: pls)
			{
				pl.postcodeChange(lastPostcode);
			}
		}
		catch(PostcodeException pe)
		{
			Log.e(TAG, "Parse error during new postcode", pe);
		}
	}

	public void onLocationChanged(Location l){
		updatedLocation(l);
	}

	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
}

