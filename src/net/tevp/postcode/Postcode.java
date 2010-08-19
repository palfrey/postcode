package net.tevp.postcode;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.widget.TextView;
import android.location.Location;
import android.widget.Button;
import android.view.View;

public class Postcode extends Activity implements PostcodeListener {	
	TextView tv;
	PostcodeBackend pb;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tv = (TextView) findViewById(R.id.Postcode);
        tv.setText("Finding location..");

		pb = (PostcodeBackend) getLastNonConfigurationInstance();
		if (pb == null)
			pb = new PostcodeBackend();
		newPostcode(false);

		final Postcode self = this;
		((Button) findViewById(R.id.btnUpdate)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				self.newPostcode(true);
        		self.setText("Finding location..");
			}
		});
    }

	private void newPostcode(boolean mustBeNew)
	{
		pb.getPostcode(this,this,mustBeNew);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		newPostcode(false);
	}

	public static void main(String[] args) throws Exception {
		String[] coords = args[0].split(",");
		assert coords.length == 2;
		System.out.println(PostcodeBackend.get(Double.parseDouble(coords[0]),Double.parseDouble(coords[1])));
	}

	private void setText(final String text)
	{
		final TextView myTV = tv;		
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				myTV.setText(text);
			}
		});
	}

	public void postcodeChange(String postcode)
	{
		setText(postcode);
	}

	public void updatedLocation(Location l)
	{
		setText("Found location, looking for postcode...");
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		return pb;
	}
}
