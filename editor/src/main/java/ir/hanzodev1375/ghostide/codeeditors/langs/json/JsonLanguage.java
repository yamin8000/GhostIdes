package ir.hanzodev1375.ghostide.codeeditors.langs.json;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.lang.QuickQuoteHandler;
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager;
import io.github.rosemoe.sora.lang.completion.CompletionHelper;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete;
import io.github.rosemoe.sora.lang.format.AsyncFormatter;
import io.github.rosemoe.sora.lang.format.Formatter;
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler;
import io.github.rosemoe.sora.lang.styling.Styles;
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandleResult;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.text.TextRange;
import io.github.rosemoe.sora.text.TextUtils;
import io.github.rosemoe.sora.widget.SymbolPairMatch;
import ir.hanzodev1375.ghostide.codeeditors.langs.antlr4base.CharParser;
import java.io.IOException;
import java.io.StringReader;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandleResult;
import io.github.rosemoe.sora.lang.styling.Styles;

public class JsonLanguage implements Language {

  private final JsonAnalyzer analyzer;
  private final IdentifierAutoComplete autoComplete;
  private String path;
  private Context ctx;
  
  
  public JsonLanguage(Context ctx, String path) {
    this.path = path;
    this.ctx = ctx;
    String[] htmlKeywords = {"true", "false", "null"};
    autoComplete = new IdentifierAutoComplete(htmlKeywords);
    analyzer = new JsonAnalyzer();
    analyzer.init(ctx, path);
  }

  private final Formatter formatter =
      new AsyncFormatter() {
        @Nullable
        @Override
        public TextRange formatAsync(@NonNull Content text, @NonNull TextRange cursorRange) {
          String formatted;
          try {

            formatted = formatJson(text.toString());
          } catch (Exception e) {
            formatted = text.toString();
          }

          if (!text.toString().equals(formatted)) {
            int oldCursor = cursorRange.getStartIndex();
            text.delete(0, text.length());
            text.insert(0, 0, formatted);
            int newCursor = Math.min(oldCursor, formatted.length());
            CharPosition pos = text.getIndexer().getCharPosition(newCursor);
            return new TextRange(pos, pos);
          }

          return cursorRange;
        }

        @Nullable
        @Override
        public TextRange formatRegionAsync(
            @NonNull Content text,
            @NonNull TextRange rangeToFormat,
            @NonNull TextRange cursorRange) {
          return null;
        }
      };

  @NonNull
  @Override
  public AnalyzeManager getAnalyzeManager() {
    return analyzer;
  }

  @Nullable
  @Override
  public QuickQuoteHandler getQuickQuoteHandler() {
    return null;
  }

  @Override
  public void destroy() {}

  @Override
  public int getInterruptionLevel() {
    return INTERRUPTION_LEVEL_STRONG;
  }

  @Override
  public void requireAutoComplete(
      @NonNull ContentReference content,
      @NonNull CharPosition position,
      @NonNull CompletionPublisher publisher,
      @NonNull Bundle es) {
    String prefix = CompletionHelper.computePrefix(content, position, CharParser::parserJava);
    autoComplete.requireAutoComplete(content, position, prefix, publisher, null);
  }

  @Override
  public int getIndentAdvance(@NonNull ContentReference text, int line, int column) {

    try {
      var lexer = new JSONLexer(CharStreams.fromReader(new StringReader(text.getLine(line))));
      Token token;
      int advance = 0;
      while (((token = lexer.nextToken()) != null && token.getType() != token.EOF)) {
        switch (token.getType()) {
          case JSONLexer.LBRACE:
            advance++;
            break;
          case JSONLexer.RBRACE:
            advance--;
            break;
        }
      }
      advance = Math.max(0, advance);
      return advance * 2;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public boolean useTab() {
    return false;
  }

  @NonNull
  @Override
  public Formatter getFormatter() {
    return formatter;
  }

  @Override
  public SymbolPairMatch getSymbolPairs() {
    return new SymbolPairMatch.DefaultSymbolPairs();
  }

  private class CssOpenBraceHandler implements NewlineHandler {

    @Override
    public boolean matchesRequirement(
        @NonNull Content text, @NonNull CharPosition position, @Nullable Styles style) {

      int line = position.line;
      if (line < 0 || line >= text.getLineCount()) {
        return false;
      }

      String before = text.getLine(line).subSequence(0, position.column).toString();

      int len = before.length();

      for (int i = len - 1; i >= 0; i--) {
        char c = before.charAt(i);

        if (c == '{') {
          return true;
        }

        if (!Character.isWhitespace(c)) {
          break;
        }
      }

      return false;
    }

    @NonNull
    @Override
    public NewlineHandleResult handleNewline(
        @NonNull Content text,
        @NonNull CharPosition position,
        @Nullable Styles style,
        int tabSize) {

      int line = position.line;

      String before = text.getLine(line).subSequence(0, position.column).toString();
      int indent = TextUtils.countLeadingSpaceCount(before, tabSize);
      String indentStr = TextUtils.createIndent(indent + tabSize, tabSize, false);
      return new NewlineHandleResult(new StringBuilder("\n").append(indentStr), 0);
    }
  }

  private class CssCloseBraceHandler implements NewlineHandler {

    @Override
    public boolean matchesRequirement(
        @NonNull Content text, @NonNull CharPosition position, @Nullable Styles style) {

      int line = position.line;
      if (line < 0 || line >= text.getLineCount()) {
        return false;
      }

      String before = text.getLine(line).subSequence(0, position.column).toString();

      return before.trim().endsWith("}");
    }

    @NonNull
    @Override
    public NewlineHandleResult handleNewline(
        @NonNull Content text,
        @NonNull CharPosition position,
        @Nullable Styles style,
        int tabSize) {

      int line = position.line;

      String before = text.getLine(line).subSequence(0, position.column).toString();

      int indent = TextUtils.countLeadingSpaceCount(before, tabSize);

      int newIndent = Math.max(0, indent - tabSize);

      String indentStr = TextUtils.createIndent(newIndent, tabSize, false);

      return new NewlineHandleResult(new StringBuilder("\n").append(indentStr), 0);
    }
  }

  @Override
  public NewlineHandler[] getNewlineHandlers() {
    return new NewlineHandler[] {new CssOpenBraceHandler(), new CssCloseBraceHandler()};
  }

  private String formatJson(String code) {
    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      JsonParser jp = new JsonParser();
      JsonElement je = jp.parse(code);
      return gson.toJson(je);
    } catch (Exception err) {
      return code;
    }
  }
}
