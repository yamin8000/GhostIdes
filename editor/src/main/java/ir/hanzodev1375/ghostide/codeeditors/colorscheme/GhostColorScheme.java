package ir.hanzodev1375.ghostide.codeeditors.colorscheme;

import android.graphics.Color;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class GhostColorScheme extends EditorColorScheme {
  private GhostColorScheme() {}

  private static int endColor = END_COLOR_ID;
  public static final int COLORNEXTDOT = ++endColor; // .
  public static final int COLORNEXTBRAK = ++endColor; // (
  public static final int COLORNEXTCHAR = ++endColor; //User.hsi == UserHasColor
  public static final int COLORUPPERCASE = ++endColor; //HELLO
  public static final int COLORNEXTLESS = ++endColor; //<

  @Override
  public void applyDefault() {
    super.applyDefault();
    // TODO: Implement this method
    setColor(COLORNEXTDOT,Color.parseColor("#ff3208"));
    setColor(COLORNEXTBRAK,Color.parseColor("#ff10ba"));
    setColor(COLORNEXTCHAR,Color.parseColor("#6ba108"));
    setColor(COLORUPPERCASE,Color.parseColor("#ff2c11"));
    setColor(COLORNEXTLESS,Color.parseColor("#ffc190"));
  }

  @Override
  public boolean isDark() {
    // TODO: Implement this method
    return true;
  }
}
