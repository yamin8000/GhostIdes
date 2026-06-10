package ir.hanzodev1375.ghostide.codeeditors.langs.antlr4base;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.antlr.v4.runtime.Lexer;

public class LineState {

  public static final int NORMAL = 0;

  public static final int INCOMPLETE = 1;
  public int state;
  public int lineNumber = 0;
  public int col;
  public boolean hasBraces;
  public int lexerMode;
  public List<String> identifiers;

  public LineState() {
    this(NORMAL, false, Lexer.DEFAULT_MODE);
  }

  public LineState(int state, boolean hasBraces, int lexerMode) {
    this.state = state;
    this.hasBraces = hasBraces;
    this.lexerMode = lexerMode;
    this.identifiers = null;
  }

  public void addIdentifier(CharSequence idt) {
    if (identifiers == null) {
      identifiers = new ArrayList<>();
    }
    identifiers.add(idt.toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LineState)) {
      return false;
    }
    LineState that = (LineState) o;
    return state == that.state && hasBraces == that.hasBraces && lexerMode == that.lexerMode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, hasBraces, lexerMode);
  }

  @Override
  public String toString() {

    return "LineState{"
        + "state="
        + state
        + ", hasBraces="
        + hasBraces
        + ", lexerMode="
        + lexerMode
        + ", identifiers="
        + identifiers
        + '}';
  }
}
