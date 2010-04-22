package net.tevp.Postcode;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

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
			what = new URL("http://whatismypostcode.appspot.com/"+s);
		} catch (MalformedURLException e1) {
			return "malformed for"+s;
		}
		InputStream data;
		try {
			data = what.openStream();

			byte[] out = new byte[10];
			data.read(out,0,10);
			return new String(out);//+s+" - "+Double.toString(lat)+", "+Double.toString(lon);
		} catch (IOException e1) {
			return "ioexception "+e1.getMessage();
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

