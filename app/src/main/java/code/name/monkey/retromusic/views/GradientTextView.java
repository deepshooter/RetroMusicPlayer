package code.name.monkey.retromusic.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.util.AttributeSet;

import code.name.monkey.retromusic.R;

/**
 * Created by hemanths on 21/08/17.
 */

public class GradientTextView extends AppCompatTextView {
    // ===========================================================
    // Constants
    // ===========================================================

    public static final int LG_HORIZONTAL = 101;
    public static final int LG_VERTICAL = 102;

    // ===========================================================
    // Fields
    // ===========================================================

    private Typeface mTypeFace = null;
    private boolean mApplyLinearGradient = false;
    private int mGradientOrientation = LG_HORIZONTAL;
    private int mStartColor = R.color.md_green_A200;
    private int mEndColor = R.color.md_blue_A700;

    // ===========================================================
    // Constructors
    // ===========================================================

    public GradientTextView(Context context) {
        super(context);
    }

    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Apply Defined Customization from XML
        applyAttributes(attrs);
    }

    public GradientTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Apply Defined Customization from XML
        applyAttributes(attrs);
    }

    // ===========================================================
    // Overriden Methods
    // ===========================================================

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mApplyLinearGradient)
            applyLinearGradient();
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void applyAttributes(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.GradientTextView);

        for (int i = 0; i < array.getIndexCount(); i++) {
            int type = array.getIndex(i);

            if (type == R.styleable.GradientTextView_gFontPath) {
                setFont(array.getString(R.styleable.GradientTextView_gFontPath));
            }

            if (type == R.styleable.GradientTextView_text_html) {
                applyHtmlText(array.getString(R.styleable.GradientTextView_text_html));
            }

            if (type == R.styleable.GradientTextView_linear_gradient) {
                // Set the flag
                mApplyLinearGradient = true;
                String orientation = array.getString(R.styleable.GradientTextView_linear_gradient);

                if (orientation.toLowerCase().equalsIgnoreCase("vertical")) {
                    mGradientOrientation = LG_VERTICAL;
                } else if (orientation.toLowerCase().equalsIgnoreCase("horizontal")) {
                    mGradientOrientation = LG_HORIZONTAL;
                }
            }

            if (type == R.styleable.GradientTextView_start_color) {
                // Set the flag
                mApplyLinearGradient = true;
                mStartColor = array.getColor(R.styleable.GradientTextView_start_color, Color.BLACK);
            }

            if (type == R.styleable.GradientTextView_end_color) {
                // Set the flag
                mApplyLinearGradient = true;
                mEndColor = array.getColor(R.styleable.GradientTextView_end_color, Color.BLACK);
            }
        }

        array.recycle();
    }

    private void applyLinearGradient() {
        Shader shader = null;

        if (mGradientOrientation == LG_HORIZONTAL) {
            shader = new LinearGradient(0, getHeight(), getWidth(), 0, mStartColor, mEndColor, Shader.TileMode.CLAMP);
        } else {
            shader = new LinearGradient(0, 0, 0, getHeight(), mStartColor, mEndColor, Shader.TileMode.CLAMP);
        }

        getPaint().setShader(shader);
    }

    private void applyHtmlText(String htmltext) {
        setHtmlText(htmltext);
    }

    /**
     * Sets the font for textview by loading the font from assets
     *
     * @param fontname name of the font with path ex. fonts/fontfilename.ttf
     */
    public void setFont(String fontname) {
        if (!isInEditMode()) {
            if (fontname.trim().length() == 0)
                return;

            mTypeFace = Typeface.createFromAsset(getContext().getAssets(), fontname);
            setTypeface(mTypeFace);
        }
    }

    /**
     * Simple method to set linear gradient to textview with orientation as Horizontal or Vertical
     */
    public void setLinearGradient(int mStartColor, int mEndColor, int mGradientOrientation) {
        this.mApplyLinearGradient = true;
        this.mStartColor = mStartColor;
        this.mEndColor = mEndColor;
        this.mGradientOrientation = mGradientOrientation;

        // Request for refresh
        requestLayout();
    }

    /**
     * Handy Method to set HTML text to textview
     */
    public void setHtmlText(String htmltext) {
        setText(Html.fromHtml(htmltext));
    }
}