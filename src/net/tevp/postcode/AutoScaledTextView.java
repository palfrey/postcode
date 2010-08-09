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

		String text = getText().toString();
		Log.d(TAG, String.format("text is '%s'", text));
		Rect bounds = new Rect();	
		TextPaint tp = getPaint();
		tp.getTextBounds(text, 0, text.length(), bounds);
		int height = -bounds.top;
		int width = bounds.right;
		Log.d(TAG, String.format("Bounds bottom-top is %d, %d", bounds.bottom, bounds.top));
		Log.d(TAG, String.format("Bounds right-left is %d, %d", bounds.right, bounds.left));
		double fudge = 0.95;
		double scale = (getWidth()/(width*1.0)) * fudge;
		Log.d(TAG, String.format("Scale is %f", scale));
		Log.d(TAG, String.format("Font size: old %f, new %f", getTextSize(), getTextSize()*scale));
		setTextSize((int)(getTextSize()*scale));
		
		tp.getTextBounds(text, 0, text.length(), bounds);
		Log.d(TAG, String.format("Bounds bottom-top is %d, %d", bounds.bottom, bounds.top));
		Log.d(TAG, String.format("Bounds right-left is %d, %d", bounds.right, bounds.left));
		int newHeight = (int)(height*scale);
		if (getHeight()<newHeight)
		{
			Log.d(TAG, String.format("Setting height to %d, old height is %d", newHeight, getHeight()));
			//setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, newHeight));
			getLayoutParams().height = newHeight;
			/*setMinimumHeight(newHeight);
			setHeight(newHeight);*/
			requestLayout();
			Log.d(TAG, String.format("Revised height: %d", getHeight()));
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
