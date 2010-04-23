package net.tevp.Postcode;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

import org.json.*;

import android.app.Activity;
import android.os.Bundle;
import android.location.*;
import android.content.Context;
import android.widget.TextView;

public class Postcode extends Activity implements LocationListener {	
	TextView tv;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria c = new Criteria();
        String provider = lm.getBestProvider(c, false);
        lm.requestLocationUpdates(provider, 1000, 20, this);
                
        Location l = lm.getLastKnownLocation(provider);
        
        setContentView(R.layout.main);
        tv = (TextView) findViewById(R.id.Postcode);
        if (l!= null)
        	tv.setText(get(l.getLatitude(),l.getLongitude()));
        else
        	tv.setText("Postcode is unknown");
    }

    static Geohash e = new Geohash();
    
	static String get(double lat, double lon)
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
			JSONObject js = new JSONObject(new String(out));
			return js.getString("postcode");
		}
		catch (JSONException e2) {
			return "jsonexception "+e2.getMessage()+"\n"+new String(out);
		}
	}

	public static void main(String[] args) throws Exception {
		String[] coords = args[0].split(",");
		assert coords.length == 2;
		System.out.println(Postcode.get(Double.parseDouble(coords[0]),Double.parseDouble(coords[1])));
	}

	public void onLocationChanged(Location l) {
		tv.setText(get(l.getLatitude(),l.getLongitude()));		
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}

