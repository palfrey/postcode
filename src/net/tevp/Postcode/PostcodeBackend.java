package net.tevp.Postcode;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

import org.json.*;

public class PostcodeBackend {	
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
}

