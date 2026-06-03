package ir.hanzodev1375.ghostide.codeeditors.langs.html;

import io.github.rosemoe.sora.lang.completion.CompletionItemKind;
import ir.hanzodev1375.ghostide.codeeditors.lspcustomhot.CustomCompletionItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HtmlHelper {

  private static final Set<String> SELF_CLOSING_TAGS =
      new HashSet<>(
          Arrays.asList(
              "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param",
              "source", "track", "wbr"));

  private static final List<TagEntry> TAGS = new ArrayList<>();
  private static final List<AttrEntry> ATTRS = new ArrayList<>();
  private static final List<JsKeywordEntry> JS_KEYWORDS = new ArrayList<>();

  static {
    initTags();
    initAttributes();
    initJsKeywords();
  }

  private static void initTags() {
    addTag("html", "HTML root element", CompletionItemKind.Class);
    addTag("head", "Document metadata", CompletionItemKind.Class);
    addTag("body", "Document body", CompletionItemKind.Class);
    addTag("title", "Document title", CompletionItemKind.Class);
    addTag("base", "Base URL", true, CompletionItemKind.Property);
    addTag("link", "External resource link", true, CompletionItemKind.Property);
    addTag("meta", "Metadata", true, CompletionItemKind.Property);
    addTag("style", "CSS styles", CompletionItemKind.Class);
    addTag("script", "JavaScript code", CompletionItemKind.Class);
    addTag("noscript", "Alternative content", CompletionItemKind.Class);

    addTag("header", "Header section", CompletionItemKind.Class);
    addTag("footer", "Footer section", CompletionItemKind.Class);
    addTag("nav", "Navigation", CompletionItemKind.Class);
    addTag("main", "Main content", CompletionItemKind.Class);
    addTag("article", "Article", CompletionItemKind.Class);
    addTag("section", "Section", CompletionItemKind.Class);
    addTag("aside", "Sidebar", CompletionItemKind.Class);
    addTag("details", "Details widget", CompletionItemKind.Class);
    addTag("summary", "Summary for details", CompletionItemKind.Class);
    addTag("dialog", "Dialog box", CompletionItemKind.Class);

    addTag("h1", "Heading level 1", CompletionItemKind.Text);
    addTag("h2", "Heading level 2", CompletionItemKind.Text);
    addTag("h3", "Heading level 3", CompletionItemKind.Text);
    addTag("h4", "Heading level 4", CompletionItemKind.Text);
    addTag("h5", "Heading level 5", CompletionItemKind.Text);
    addTag("h6", "Heading level 6", CompletionItemKind.Text);
    addTag("p", "Paragraph", CompletionItemKind.Text);
    addTag("br", "Line break", true, CompletionItemKind.Snippet);
    addTag("hr", "Horizontal rule", true, CompletionItemKind.Snippet);
    addTag("div", "Generic block container", CompletionItemKind.Class);
    addTag("span", "Generic inline container", CompletionItemKind.Text);

    addTag("a", "Anchor link", CompletionItemKind.Text);
    addTag("strong", "Strong importance", CompletionItemKind.Text);
    addTag("em", "Emphasis", CompletionItemKind.Text);
    addTag("b", "Bold text", CompletionItemKind.Text);
    addTag("i", "Italic text", CompletionItemKind.Text);
    addTag("u", "Underlined text", CompletionItemKind.Text);
    addTag("small", "Small text", CompletionItemKind.Text);
    addTag("mark", "Marked text", CompletionItemKind.Text);
    addTag("del", "Deleted text", CompletionItemKind.Text);
    addTag("ins", "Inserted text", CompletionItemKind.Text);
    addTag("sub", "Subscript", CompletionItemKind.Text);
    addTag("sup", "Superscript", CompletionItemKind.Text);
    addTag("code", "Code snippet", CompletionItemKind.Text);
    addTag("pre", "Preformatted text", CompletionItemKind.Text);
    addTag("q", "Inline quotation", CompletionItemKind.Text);
    addTag("blockquote", "Block quotation", CompletionItemKind.Text);
    addTag("cite", "Citation", CompletionItemKind.Text);
    addTag("abbr", "Abbreviation", CompletionItemKind.Text);
    addTag("address", "Contact address", CompletionItemKind.Text);
    addTag("time", "Date/time", CompletionItemKind.Text);

    addTag("ul", "Unordered list", CompletionItemKind.Text);
    addTag("ol", "Ordered list", CompletionItemKind.Text);
    addTag("li", "List item", CompletionItemKind.Text);
    addTag("dl", "Description list", CompletionItemKind.Text);
    addTag("dt", "Description term", CompletionItemKind.Text);
    addTag("dd", "Description details", CompletionItemKind.Text);

    addTag("table", "Table", CompletionItemKind.Class);
    addTag("caption", "Table caption", CompletionItemKind.Text);
    addTag("thead", "Table header", CompletionItemKind.Class);
    addTag("tbody", "Table body", CompletionItemKind.Class);
    addTag("tfoot", "Table footer", CompletionItemKind.Class);
    addTag("tr", "Table row", CompletionItemKind.Text);
    addTag("th", "Table header cell", CompletionItemKind.Text);
    addTag("td", "Table data cell", CompletionItemKind.Text);
    addTag("col", "Table column", true, CompletionItemKind.Property);
    addTag("colgroup", "Column group", CompletionItemKind.Class);

    addTag("form", "Form", CompletionItemKind.Class);
    addTag("input", "Input field", true, CompletionItemKind.Property);
    addTag("button", "Button", CompletionItemKind.Text);
    addTag("select", "Dropdown", CompletionItemKind.Text);
    addTag("option", "Option item", CompletionItemKind.Text);
    addTag("optgroup", "Option group", CompletionItemKind.Text);
    addTag("textarea", "Text area", CompletionItemKind.Text);
    addTag("label", "Label", CompletionItemKind.Text);
    addTag("fieldset", "Fieldset", CompletionItemKind.Class);
    addTag("legend", "Legend", CompletionItemKind.Text);
    addTag("datalist", "Data list", CompletionItemKind.Text);
    addTag("output", "Output", CompletionItemKind.Text);
    addTag("progress", "Progress bar", CompletionItemKind.Text);
    addTag("meter", "Meter", CompletionItemKind.Text);

    addTag("img", "Image", true, CompletionItemKind.Value);
    addTag("picture", "Picture container", CompletionItemKind.Class);
    addTag("source", "Media source", true, CompletionItemKind.Property);
    addTag("audio", "Audio player", CompletionItemKind.Class);
    addTag("video", "Video player", CompletionItemKind.Class);
    addTag("track", "Text track", true, CompletionItemKind.Property);
    addTag("canvas", "Canvas drawing", CompletionItemKind.Class);
    addTag("svg", "SVG graphics", CompletionItemKind.Class);
    addTag("iframe", "Inline frame", CompletionItemKind.Class);
    addTag("embed", "External content", true, CompletionItemKind.Property);
    addTag("object", "Object", CompletionItemKind.Class);
    addTag("param", "Object parameter", true, CompletionItemKind.Property);

    addTag("figure", "Figure", CompletionItemKind.Class);
    addTag("figcaption", "Figure caption", CompletionItemKind.Text);
    addTag("ruby", "Ruby annotation", CompletionItemKind.Text);
    addTag("rt", "Ruby text", CompletionItemKind.Text);
    addTag("rp", "Ruby parentheses", CompletionItemKind.Text);
    addTag("bdi", "Bi-directional isolation", CompletionItemKind.Text);
    addTag("bdo", "Bi-directional override", CompletionItemKind.Text);
    addTag("wbr", "Word break opportunity", true, CompletionItemKind.Snippet);
  }

  private static void initAttributes() {
    addAttr("id", "Unique identifier", CompletionItemKind.Property);
    addAttr("class", "CSS class name", CompletionItemKind.Property);
    addAttr("style", "Inline CSS", CompletionItemKind.Property);
    addAttr("title", "Tooltip text", CompletionItemKind.Property);
    addAttr("lang", "Language code", CompletionItemKind.Property);
    addAttr("dir", "Text direction (ltr/rtl)", CompletionItemKind.Property);
    addAttr("tabindex", "Tab order", CompletionItemKind.Property);
    addAttr("accesskey", "Keyboard shortcut", CompletionItemKind.Property);
    addAttr("hidden", "Hidden element", CompletionItemKind.Property);
    addAttr("contenteditable", "Editable content", CompletionItemKind.Property);
    addAttr("draggable", "Draggable", CompletionItemKind.Property);
    addAttr("spellcheck", "Spell checking", CompletionItemKind.Property);
    addAttr("translate", "Translation", CompletionItemKind.Property);

    addAttr("href", "URL link", CompletionItemKind.Property);
    addAttr("target", "Link target", CompletionItemKind.Property);
    addAttr("rel", "Relationship", CompletionItemKind.Property);
    addAttr("download", "Download file", CompletionItemKind.Property);
    addAttr("hreflang", "Language of linked resource", CompletionItemKind.Property);
    addAttr("type", "MIME type", CompletionItemKind.Property);
    addAttr("media", "Media query", CompletionItemKind.Property);

    addAttr("src", "Source URL", CompletionItemKind.Property);
    addAttr("alt", "Alternative text", CompletionItemKind.Property);
    addAttr("width", "Width", CompletionItemKind.Property);
    addAttr("height", "Height", CompletionItemKind.Property);
    addAttr("loading", "Loading behavior", CompletionItemKind.Property);

    addAttr("name", "Name attribute", CompletionItemKind.Property);
    addAttr("value", "Value", CompletionItemKind.Property);
    addAttr("placeholder", "Placeholder text", CompletionItemKind.Property);
    addAttr("required", "Required field", CompletionItemKind.Property);
    addAttr("disabled", "Disabled", CompletionItemKind.Property);
    addAttr("readonly", "Read only", CompletionItemKind.Property);
    addAttr("checked", "Checked", CompletionItemKind.Property);
    addAttr("selected", "Selected", CompletionItemKind.Property);
    addAttr("multiple", "Multiple selection", CompletionItemKind.Property);
    addAttr("pattern", "Regex pattern", CompletionItemKind.Property);
    addAttr("min", "Minimum value", CompletionItemKind.Property);
    addAttr("max", "Maximum value", CompletionItemKind.Property);
    addAttr("step", "Step value", CompletionItemKind.Property);
    addAttr("autocomplete", "Autocomplete", CompletionItemKind.Property);
    addAttr("autofocus", "Auto focus", CompletionItemKind.Property);

    addAttr("action", "Form action", CompletionItemKind.Property);
    addAttr("method", "HTTP method", CompletionItemKind.Property);
    addAttr("enctype", "Encoding type", CompletionItemKind.Property);
    addAttr("novalidate", "No validation", CompletionItemKind.Property);

    addAttr("cols", "Columns", CompletionItemKind.Property);
    addAttr("rows", "Rows", CompletionItemKind.Property);
    addAttr("wrap", "Wrap", CompletionItemKind.Property);
    addAttr("maxlength", "Max length", CompletionItemKind.Property);
    addAttr("minlength", "Min length", CompletionItemKind.Property);
    addAttr("size", "Size", CompletionItemKind.Property);

    addAttr("scope", "Header scope", CompletionItemKind.Property);
    addAttr("colspan", "Column span", CompletionItemKind.Property);
    addAttr("rowspan", "Row span", CompletionItemKind.Property);

    addAttr("controls", "Media controls", CompletionItemKind.Property);
    addAttr("autoplay", "Auto play", CompletionItemKind.Property);
    addAttr("loop", "Loop", CompletionItemKind.Property);
    addAttr("muted", "Muted", CompletionItemKind.Property);
    addAttr("poster", "Poster image", CompletionItemKind.Property);

    addAttr("async", "Async script", CompletionItemKind.Property);
    addAttr("defer", "Defer script", CompletionItemKind.Property);
    addAttr("charset", "Charset", CompletionItemKind.Property);
    addAttr("integrity", "Integrity", CompletionItemKind.Property);
    addAttr("crossorigin", "CORS", CompletionItemKind.Property);
  }

  private static void initJsKeywords() {
    addJs("var");
    addJs("let");
    addJs("const");
    addJs("function");
    addJs("return");
    addJs("if");
    addJs("else");
    addJs("for");
    addJs("while");
    addJs("switch");
    addJs("case");
    addJs("break");
    addJs("continue");
    addJs("try");
    addJs("catch");
    addJs("finally");
    addJs("throw");
    addJs("class");
    addJs("new");
    addJs("this");
    addJs("import");
    addJs("export");
    addJs("default");
    addJs("async");
    addJs("await");
    addJs("typeof");
    addJs("instanceof");
    addJs("in");
    addJs("of");
    addJs("yield");
    addJs("true");
    addJs("false");
    addJs("null");
    addJs("undefined");
  }

  private static void addJs(String keyword) {
    JS_KEYWORDS.add(new JsKeywordEntry(keyword, "JavaScript keyword"));
  }

  public static List<CustomCompletionItem> getJsKeywordItems(String prefix) {
    List<CustomCompletionItem> list = new ArrayList<>();
    for (var k : JS_KEYWORDS) {
      if (prefix == null || prefix.isEmpty() || k.name.startsWith(prefix)) {
        var item = new CustomCompletionItem(k.name, k.desc, k.name, k.name.length(), prefix);
        item.kind(CompletionItemKind.Keyword);
        list.add(item);
      }
    }
    return list;
  }

  private static void addTag(String name, String desc, CompletionItemKind kind) {
    addTag(name, desc, false, kind);
  }

  private static void addTag(
      String name, String desc, boolean selfClosing, CompletionItemKind kind) {
    TAGS.add(new TagEntry(name, desc, selfClosing, kind));
  }

  private static void addAttr(String name, String desc, CompletionItemKind kind) {
    ATTRS.add(new AttrEntry(name, desc, kind));
  }

  public static List<HtmlTagCompletionItem> getAllTagItems() {
    List<HtmlTagCompletionItem> items = new ArrayList<>();
    for (TagEntry entry : TAGS) {
      HtmlTagCompletionItem item =
          new HtmlTagCompletionItem(entry.name, entry.desc, entry.selfClosing);
      item.kind(entry.kind);
      items.add(item);
    }
    return items;
  }

  public static List<HtmlTagCompletionItem> getTagItemsByPrefix(String prefix) {
    List<HtmlTagCompletionItem> items = new ArrayList<>();
    for (TagEntry entry : TAGS) {
      if (entry.name.contains(prefix)) {
        HtmlTagCompletionItem item =
            new HtmlTagCompletionItem(entry.name, entry.desc, entry.selfClosing, prefix);
        item.kind(entry.kind);
        items.add(item);
      }
    }
    return items;
  }

  public static List<CustomCompletionItem> getNormalTag(String prefix) {
    List<CustomCompletionItem> list = new ArrayList<>();
    for (var tag : TAGS) {
      if (prefix == null
          || prefix.isEmpty()
          || tag.name.startsWith(prefix)
          || tag.name.contains(prefix)) {
        var item =
            new CustomCompletionItem(tag.name, tag.desc, tag.name, tag.name.length(), prefix);
        item.kind(tag.kind);
        list.add(item);
      }
    }
    return list;
  }

  public static List<HtmlAttributeCompletionItem> getAllAttributeItems() {
    List<HtmlAttributeCompletionItem> items = new ArrayList<>();
    for (AttrEntry entry : ATTRS) {
      HtmlAttributeCompletionItem item =
          new HtmlAttributeCompletionItem(entry.name, entry.desc, "");
      item.kind(entry.kind);
      items.add(item);
    }
    return items;
  }

  public static List<HtmlAttributeCompletionItem> getAttributeItemsByPrefix(String prefix) {
    List<HtmlAttributeCompletionItem> items = new ArrayList<>();
    for (AttrEntry entry : ATTRS) {
      if (entry.name.contains(prefix)) {
        HtmlAttributeCompletionItem item =
            new HtmlAttributeCompletionItem(entry.name, entry.desc, prefix);
        item.kind(entry.kind);
        items.add(item);
      }
    }
    return items;
  }

  private static class TagEntry {
    String name;
    String desc;
    boolean selfClosing;
    CompletionItemKind kind;

    TagEntry(String n, String d, boolean sc, CompletionItemKind k) {
      name = n;
      desc = d;
      selfClosing = sc;
      kind = k;
    }
  }

  private static class AttrEntry {
    String name;
    String desc;
    CompletionItemKind kind;

    AttrEntry(String n, String d, CompletionItemKind k) {
      name = n;
      desc = d;
      kind = k;
    }
  }

  private static class JsKeywordEntry {
    String name;
    String desc;

    JsKeywordEntry(String n, String d) {
      name = n;
      desc = d;
    }
  }
}
