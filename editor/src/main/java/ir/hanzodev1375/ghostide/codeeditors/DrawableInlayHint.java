package ir.hanzodev1375.ghostide.codeeditors;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import io.github.rosemoe.sora.lang.styling.inlayHint.InlayHint;
import io.github.rosemoe.sora.lang.styling.inlayHint.CharacterSide;

public class DrawableInlayHint extends InlayHint {

  public static final String TYPE_NAME = "drawable";

  private final Drawable drawable;

  public DrawableInlayHint(int line, int column, Drawable drawable) {
    super(line, column, TYPE_NAME, CharacterSide.RIGHT);

    this.drawable = drawable;
  }

  public Drawable getDrawable() {
    return drawable;
  }
}
