package ir.hanzodev1375.ghostide.codeeditors.langs.json;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import com.caverock.androidsvg.SVG;
import io.github.rosemoe.sora.lang.analysis.IncrementalAnalyzeManager.LineTokenizeResult;
import io.github.rosemoe.sora.lang.styling.Span;
import io.github.rosemoe.sora.lang.styling.line.LineSideIcon;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import ir.hanzodev1375.ghostide.codeeditors.langs.antlr4base.CodeAnalyzer;
import ir.hanzodev1375.ghostide.codeeditors.langs.antlr4base.IncrementalToken;
import ir.hanzodev1375.ghostide.codeeditors.langs.antlr4base.LineState;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonAnalyzer extends CodeAnalyzer {

  private Context context;
  private String jsonFilePath;
  public JsonAnalyzer() {
    super(JSONLexer.class);
  }

  public void init(Context context, String jsonFilePath) {
    this.context = context.getApplicationContext();
    this.jsonFilePath = jsonFilePath;
  }

  @Override
  protected int[][] getMultilineTokenStartEndTypes() {
    return new int[][] {new int[] {-1}, new int[] {-1}};
  }

  @Override
  protected int[] getCodeBlockTokens() {
    return new int[] {JSONLexer.LBRACE, JSONLexer.RBRACE};
  }

  @Override
  protected boolean isIdentifierToken(int tokenType) {
    return false;
  }

  private boolean isImagePath(String path) {
    String lower = path.toLowerCase();
    return lower.endsWith(".png")
        || lower.endsWith(".jpg")
        || lower.endsWith(".jpeg")
        || lower.endsWith(".webp")
        || lower.endsWith(".gif")
        || lower.endsWith(".svg");
  }

  private void loadSvgToLine(String value, int currentLine) {
    if (value.startsWith("./")) {
      value = value.substring(2);
    }
    File file = new File(value);
    if (!file.isAbsolute()) {
      File parent = new File(jsonFilePath).getParentFile();
      if (parent != null) {
        file = new File(parent, value);
      }
    }
    try {
      file = file.getCanonicalFile();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    if (!file.exists()) return;
    try (FileInputStream fis = new FileInputStream(file)) {
      SVG svg = SVG.getFromInputStream(fis);
      svg.setDocumentWidth(48);
      svg.setDocumentHeight(48);
      PictureDrawable drawable = new PictureDrawable(svg.renderToPicture());
      if (getManagedStyles() != null) {
        getManagedStyles().eraseLineStyle(currentLine, LineSideIcon.class);
        getManagedStyles().addLineStyle(new LineSideIcon(currentLine, drawable));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void loadImageToLine(String value, int currentLine, int col) {
    if (value.toLowerCase().endsWith(".svg")) {
      loadSvgToLine(value, currentLine);
      return;
    }
    if (!isImagePath(value)) {
      if (getManagedStyles() != null) {
        getManagedStyles().eraseLineStyle(currentLine, LineSideIcon.class);
      }
      return;
    }
    if (value.startsWith("./")) {
      value = value.substring(2);
    }
    File file = new File(value);
    if (!file.isAbsolute()) {
      File parent = new File(jsonFilePath).getParentFile();
      if (parent != null) {
        file = new File(parent, value);
      }
    }
    try {
      file = file.getCanonicalFile();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    if (!file.exists()) {
      if (getManagedStyles() != null) {
        getManagedStyles().eraseLineStyle(currentLine, LineSideIcon.class);
      }
      return;
    }
    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
    if (bitmap == null) {
      if (getManagedStyles() != null) {
        getManagedStyles().eraseLineStyle(currentLine, LineSideIcon.class);
      }
      return;
    }
    Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
    if (getManagedStyles() != null) {
      getManagedStyles().eraseLineStyle(currentLine, LineSideIcon.class);
      getManagedStyles().addLineStyle(new LineSideIcon(currentLine, drawable));
    }
  }

  @Override
  protected List<Span> generateSpans(LineTokenizeResult<LineState, IncrementalToken> tokens) {
    List<Span> spans = new ArrayList<>();
    int pretoken = -1;

    for (int i = 0; i < tokens.tokens.size(); ++i) {
      final var token = tokens.tokens.get(i);
      int type = token.getType();
      int offset = token.getStartIndex();
      final String text = token.getText();

      switch (type) {
        case JSONLexer.COLON:
        case JSONLexer.COMMA:
          spans.add(Span.obtain(offset, EditorColorScheme.OPERATOR));
          break;
        case JSONLexer.LBRACE:
        case JSONLexer.LBRACKET:
        case JSONLexer.RBRACE:
        case JSONLexer.RBRACKET:
          spans.add(Span.obtain(offset, EditorColorScheme.ATTRIBUTE_NAME));
          break;
        case JSONLexer.STRING:
          int currentLine = tokens.state.lineNumber;
          int col = tokens.state.col;
          if (pretoken == JSONLexer.COLON) {
            spans.add(Span.obtain(offset, EditorColorScheme.LITERAL));
            String value = text.substring(1, text.length() - 1);
            loadImageToLine(value, currentLine, col);
          } else {
            spans.add(Span.obtain(offset, EditorColorScheme.ATTRIBUTE_VALUE));
          }
          break;
        case JSONLexer.TRUE:
        case JSONLexer.FALSE:
        case JSONLexer.NULL:
          spans.add(Span.obtain(offset, EditorColorScheme.KEYWORD));
          break;
      }

      if (type != JSONLexer.WS) {
        pretoken = type;
      }
    }
    return spans;
  }

  @Override
  protected void handleIncompleteToken(IncrementalToken token) {}
}
