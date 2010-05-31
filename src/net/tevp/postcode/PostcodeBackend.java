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
    static Geohash e = new Geohash();
    
	static String get(double lat, double lon) throws PostcodeException
	{
		String s = e.encode(lat, lon);
		URL what;
		try {			
			what = new URL(String.format("http://www.uk-postcodes.com/latlng/%.8f,%.8f.json",lat,lon));
		} catch (MalformedURLException e1) {
			return "malformed for"+s;
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
			return "ioexception "+e1.getMessage();
		}
		try 
		{
			JSONObject js = new JSONObject(new String(out, 0, sofar));
			return js.getString("postcode");
		}
		catch (JSONException e2) {
			PostcodeException pe = new PostcodeException("json issue");
			pe.initCause(e2);
			throw pe;
		}
	}

	static Vector<PostcodeListener> pls = null;
	static String lastPostcode = null;

    public void getPostcode(Context context, PostcodeListener callback)
	{
		if (pls == null)
		{
			LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			Criteria c = new Criteria();
			String provider = lm.getBestProvider(c, false);
			lm.requestLocationUpdates(provider, 1000, 20, this);
			
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
				callback.postcodeChange(lastPostcode);
		}
	}

	public static void updatedLocation(Location l)
	{
		try
		{
			lastPostcode = PostcodeBackend.get(l.getLatitude(),l.getLongitude());
			for (PostcodeListener pl: pls)
				pl.postcodeChange(lastPostcode);
		}
		catch(PostcodeException pe)
		{
			Log.e("Postcode", "Parse error during new postcode", pe);
		}
	}

	public void onLocationChanged(Location l){
		updatedLocation(l);
	}

	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
}

