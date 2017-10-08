package code.name.monkey.retromusic.helper;

/**
 * Created by hemanths on 14/08/17.
 */

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;

import java.lang.reflect.Field;

public class BottomNavigationViewHelper {
    public static void disableShiftMode(Context context, BottomNavigationView view) {
        int color = ATHUtil.resolveColor(context, android.R.attr.textColorSecondary);
        setItemIconColors(view, color, ThemeStore.accentColor(context));
        setItemTextColors(view, color, ThemeStore.accentColor(context));

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);

        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);


                 /*TextView smallText = (TextView) item.findViewById(R.id.smallLabel);
                smallText.setVisibility(View.GONE);
                TextView largeLabel = (TextView) item.findViewById(R.id.largeLabel);
                largeLabel.setVisibility(View.GONE);

                AppCompatImageView icon = (AppCompatImageView) item.getChildAt(0);

                FrameLayout.LayoutParams params = (BottomNavigationView.LayoutParams) icon.getLayoutParams();
                params.gravity = Gravity.CENTER;*/

                //noinspection RestrictedApi
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            ignored.printStackTrace();
        }
    }

    private static void setItemIconColors(@NonNull BottomNavigationView view, @ColorInt int normalColor, @ColorInt int selectedColor) {
        ColorStateList iconSl = new ColorStateList(new int[][]{{-16842912}, {16842912}}, new int[]{normalColor, selectedColor});
        view.setItemIconTintList(iconSl);
    }

    private static void setItemTextColors(@NonNull BottomNavigationView view, @ColorInt int normalColor, @ColorInt int selectedColor) {
        ColorStateList textSl = new ColorStateList(new int[][]{{-16842912}, {16842912}}, new int[]{normalColor, selectedColor});
        view.setItemTextColor(textSl);
    }
}
