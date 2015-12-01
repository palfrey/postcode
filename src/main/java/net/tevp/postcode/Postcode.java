package net.tevp.postcode;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.widget.TextView;
import android.location.Location;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;
import android.text.ClipboardManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Postcode extends Activity implements PostcodeListener {	
	TextView tv;
	PostcodeBackend pb;
	String lastPostcode = null;
	PostcodeState ps = null;
	boolean acquiring = false;
	public static Context ctx = null;

	private class PostcodeState
	{
		public PostcodeBackend pb;
		public String lastPostcode;
		public String text;
		public Date timestamp;
		public boolean acquiring;
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		ctx = this;

		setContentView(R.layout.main);
        tv = (TextView) findViewById(R.id.Postcode);

		ps = (PostcodeState) getLastNonConfigurationInstance();
		final Postcode self = this;
		((Button) findViewById(R.id.btnUpdate)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				self.newPostcode(true);
			}
		});
   		((Button) findViewById(R.id.btnCopyPostcode)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (self.lastPostcode == null)
					self.toastText("Don't yet have a postcode!");
				else
				{
					ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
					clipboard.setText(self.lastPostcode);
					self.toastText("Copied postcode to clipboard");
				}
			}
		});
	}

	private void toastText(String text)
	{
		Context context = getApplicationContext();
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	private void newPostcode(boolean mustBeNew)
	{
		if (lastPostcode!= null)
			setText(lastPostcode+"\n(updating...)");
		else
			setText("Finding location..");
		acquiring = true;
		pb.getPostcode(this,this,mustBeNew);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (ps == null || ps.pb == null)
			pb = new PostcodeBackend();
		else
			pb = ps.pb;
		
		if (ps == null
				|| ps.acquiring
				|| (new Date().getTime()-ps.timestamp.getTime()) > 300 * 1000 // more than 5 minutes old
				)
			newPostcode(false);
		else
		{
			lastPostcode = ps.lastPostcode;
			setText(ps.text);
		}
	}

	public static void main(String[] args) throws Exception {
		String[] coords = args[0].split(",");
		assert coords.length == 2;
		System.out.println(PostcodeBackend.get(Double.parseDouble(coords[0]),Double.parseDouble(coords[1])));
	}

	private void setText(final CharSequence text)
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
		lastPostcode = postcode;
		setText(String.format("%s\n(@ %s)", postcode, new SimpleDateFormat("HH:mm:ss").format(new Date())));
		acquiring = false;
	}

	public void updatedLocation(Location l)
	{
		if (lastPostcode!= null)
			setText(lastPostcode+"\nFound new location, looking for postcode...");
		else
			setText("Found location, looking for postcode...");
	}

	public void locationFindFail()
	{
		acquiring = false;
		setText("Can't find location. Enable GPS, or wait until you're outside");
	}

	public void postcodeLookupFail()
	{
		acquiring = false;
		setText("Failed to lookup postcode. Please check you've got a working internet connection");
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		PostcodeState ps = new PostcodeState();
		ps.pb = pb;
		ps.text = tv.getText().toString();
		ps.timestamp = new Date();
		ps.acquiring = acquiring;
		ps.lastPostcode = lastPostcode;
		return ps;
	}
}
