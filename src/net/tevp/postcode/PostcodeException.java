package net.tevp.postcode;

public class PostcodeException extends Exception
{
	PostcodeException(String msg) {super(msg);}
	PostcodeException(String msg, Throwable t) {super(msg);initCause(t);}
}
