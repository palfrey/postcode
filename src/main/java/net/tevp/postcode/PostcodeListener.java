package net.tevp.postcode;

import android.location.Location;

public interface PostcodeListener
{
	public void postcodeChange(String postcode);
	public void updatedLocation(Location l);

	public void locationFindFail();
	public void postcodeLookupFail();
}
