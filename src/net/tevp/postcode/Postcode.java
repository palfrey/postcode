package net.tevp.postcode;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.widget.TextView;
import android.location.Location;

public class Postcode extends Activity implements PostcodeListener {	
	TextView tv;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tv = (TextView) findViewById(R.id.Postcode);
        tv.setText("Finding location..");
		new PostcodeBackend().getPostcode(this,this);
    }

	public static void main(String[] args) throws Exception {
		String[] coords = args[0].split(",");
		assert coords.length == 2;
		System.out.println(PostcodeBackend.get(Double.parseDouble(coords[0]),Double.parseDouble(coords[1])));
	}

	public void postcodeChange(String postcode)
	{
		tv.setText(postcode);
	}

	public void updatedLocation(Location l)
	{
		tv.setText("Found location, looking for postcode...");
	}
}
