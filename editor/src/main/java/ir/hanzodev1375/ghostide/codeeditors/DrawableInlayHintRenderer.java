package ir.hanzodev1375.ghostide.codeeditors;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import io.github.rosemoe.sora.graphics.InlayHintRenderParams;
import io.github.rosemoe.sora.graphics.Paint;
import io.github.rosemoe.sora.graphics.inlayHint.InlayHintRenderer;
import io.github.rosemoe.sora.lang.styling.inlayHint.InlayHint;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class DrawableInlayHintRenderer extends InlayHintRenderer {

  @NonNull
  @Override
  public String getTypeName() {
    return DrawableInlayHint.TYPE_NAME;
  }

  @Override
  public float onMeasure(
      @NonNull InlayHint inlayHint, @NonNull Paint paint, @NonNull InlayHintRenderParams params) {
    Drawable drawable = ((DrawableInlayHint) inlayHint).getDrawable();
    float targetHeight = params.getTextHeight() * 0.75f; // مثل color hint
    float targetWidth =
        drawable.getIntrinsicWidth() * (targetHeight / drawable.getIntrinsicHeight());
    return paint.getSpaceWidth() + targetWidth;
  }

  @Override
  public void onRender(
      @NonNull InlayHint inlayHint,
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @NonNull InlayHintRenderParams params,
      @NonNull EditorColorScheme colorScheme,
      float measuredWidth) {
    Drawable drawable = ((DrawableInlayHint) inlayHint).getDrawable();
    float targetHeight = params.getTextHeight() * 0.75f;
    float targetWidth =
        drawable.getIntrinsicWidth() * (targetHeight / drawable.getIntrinsicHeight());

    // مرکز ناحیه رو حساب می‌کنیم مثل ColorInlayHintRenderer
    float centerX = measuredWidth / 2f;
    float centerY = (params.getTextTop() + params.getTextBottom()) / 2f;

    float left = centerX - targetWidth / 2f;
    float top = centerY - targetHeight / 2f;

    drawable.setBounds(
        (int) left, (int) top, (int) (left + targetWidth), (int) (top + targetHeight));
    drawable.draw(canvas);
  }
}
