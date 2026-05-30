package ir.hanzodev1375.ghostide.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import com.google.android.material.R;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

public class ShapeUtil {

  public static MaterialShapeDrawable shape(float topRadius, Context context) {

    ShapeAppearanceModel model =
        ShapeAppearanceModel.builder()
            .setTopLeftCornerSize(topRadius)
            .setTopRightCornerSize(topRadius)
            .setBottomLeftCornerSize(0)
            .setBottomRightCornerSize(0)
            .build();

    MaterialShapeDrawable drawable = new MaterialShapeDrawable(model);

    drawable.setFillColor(
        ColorStateList.valueOf(MaterialColors.getColor(context, R.attr.colorSurface, 0)));

    return drawable;
  }
}