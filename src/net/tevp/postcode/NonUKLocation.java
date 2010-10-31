package net.tevp.postcode;

public class NonUKLocation extends PostcodeException
{
	public NonUKLocation()
	{
		super("non-UK location");
	}
}
