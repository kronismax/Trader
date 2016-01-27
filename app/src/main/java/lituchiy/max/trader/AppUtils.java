package lituchiy.max.trader;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

public class AppUtils {
    Context context;

    public AppUtils(Context context) {
        this.context = context;
    }

    public int getNavigationBarHeight(int orientation) {
        Resources resources = context.getResources();
        int id = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
        if (id > 0) {
            return resources.getDimensionPixelSize(id);
        }
        return 0;
    }
}
