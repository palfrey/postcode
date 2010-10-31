package net.tevp.postcode;

import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;
import android.util.AttributeSet;
import android.content.Context;
import android.text.TextPaint;
import android.util.Log;
import android.graphics.Rect;
import android.widget.LinearLayout;
import android.text.Layout;

public class AutoScaledTextView extends TextView implements TextWatcher
{
	public static final String TAG = "AutoScaledTextView";

	public AutoScaledTextView(Context ctx, AttributeSet as)
	{
		super(ctx,as);
		addTextChangedListener(this);
	}

	private void calculateTextSize(boolean textSet)
	{
		if (getWidth() == 0)
			return;
		Log.d(TAG, "calculating font size");

		// FIXME: the String version ignores markup
		// CharSequence text = getText();
		
		String text = getText().toString();
		Log.d(TAG, String.format("text is '%s'", text));
		
		Rect bounds = new Rect();	
		TextPaint tp = getPaint();
		int attempts = 0;
		while (true)
		{
			int totalHeight = 0;
			int maxWidth = 0;
			/* getTextBounds doesn't seem to cope with newlines properly */
			for(String s: text.split("\n"))
			{
				tp.getTextBounds(s, 0, s.length(), bounds);
				int height = bounds.bottom-bounds.top;
				int width = bounds.right-bounds.left;
				if (width > maxWidth)
					maxWidth = width;
				if (totalHeight != 0)
					totalHeight += tp.getFontSpacing();
				totalHeight += height;
			}
			Log.d(TAG, String.format("Text w,h = %d, %d", maxWidth, totalHeight));
			Log.d(TAG, String.format("View w,h = %d, %d", getWidth(), getHeight()));
			double fudge = 0.95;
			double scale = (getWidth()/(maxWidth*1.0)) * fudge;
			double heightScale = (getHeight()/(totalHeight*1.0)) * fudge;
			if (getHeight() < totalHeight)
			{
				scale = heightScale;
				Log.d(TAG, "using heightScale");
			}
			Log.d(TAG, String.format("Scale is %f, %f", scale, heightScale));
			Log.d(TAG, String.format("Font size: old %f, new %f", getTextSize(), getTextSize()*scale));
			double fontdiff = Math.abs(getTextSize()-(getTextSize()*scale));
			if (fontdiff<.5)
				break;
			setTextSize(Math.round(getTextSize()*scale));
			requestLayout();
			attempts +=1;
			if (attempts > 5)
				break;
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		Log.d(TAG, String.format("Size changed: %d, %d, %d, %d", w, h, oldw, oldh));
		calculateTextSize(false);
	}
	
	public void afterTextChanged(Editable s)
	{
		calculateTextSize(true);
	}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

}
