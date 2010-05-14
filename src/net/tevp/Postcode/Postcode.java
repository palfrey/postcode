package net.tevp.Postcode;

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
        	tv.setText(PostcodeBackend.get(l.getLatitude(),l.getLongitude()));
        else
        	tv.setText("Postcode is unknown");
    }


	public static void main(String[] args) throws Exception {
		String[] coords = args[0].split(",");
		assert coords.length == 2;
		System.out.println(PostcodeBackend.get(Double.parseDouble(coords[0]),Double.parseDouble(coords[1])));
	}

	public void onLocationChanged(Location l) {
		tv.setText(PostcodeBackend.get(l.getLatitude(),l.getLongitude()));		
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

