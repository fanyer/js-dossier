package com.github.jsdossier.soy;

import com.google.common.base.Joiner;
import org.owasp.html.HtmlChangeListener;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import java.util.logging.Logger;
import java.util.regex.Pattern;

final class HtmlSanitizer {
  private static final Pattern HTML_TITLE = Pattern.compile(
      "[\\p{L}\\p{N}\\s\\-_',:\\[\\]!\\./\\\\\\(\\)&]*");
  private static final Pattern NUMBER = Pattern.compile("[0-9]+");
  private static final Pattern NUMBER_OR_PERCENT = Pattern.compile("[0-9]+%?");
  private static final Pattern ALIGN = Pattern.compile("(?i)center|left|right|justify|char");
  private static final Pattern VALIGN = Pattern.compile("(?i)baseline|bottom|middle|top");
  private static final Pattern HTML_DIR = Pattern.compile("(?i)ltr|rtl|auto");

  private static final PolicyFactory HTML_POLICY = new HtmlPolicyBuilder()
      .allowElements(
          "a",
          "h1", "h2", "h3", "h4", "h5", "h6",
          "p", "div", "span", "blockquote",
          "b", "i", "strong", "em", "tt", "code", "ins", "del", "sup", "sub", "kbd", "samp", "q",
          "var", "cite", "strike", "center",
          "hr", "br", "wbr",
          "ul", "ol", "li", "dd", "dt", "dl",
          "table", "caption", "tbody", "thead", "tfoot", "td", "th", "tr", "colgroup", "col")
      .allowStandardUrlProtocols()
      .allowAttributes("title").matching(HTML_TITLE).globally()
      .allowAttributes("dir").matching(HTML_DIR).globally()
      .allowAttributes("lang").matching(Pattern.compile("[a-zA-Z]{2,20}")).globally()
      .allowAttributes("href").onElements("a")
      .allowAttributes("border", "cellpadding", "cellspacing").matching(NUMBER).onElements("table")
      .allowAttributes("colspan").matching(NUMBER).onElements("td", "th")
      .allowAttributes("nowrap").onElements("td", "th")
      .allowAttributes("height", "width").matching(NUMBER_OR_PERCENT).onElements(
          "table", "td", "th", "tr")
      .allowAttributes("align").matching(ALIGN).onElements(
          "thead", "tbody", "tfoot", "td", "th", "tr", "colgroup", "col")
      .allowAttributes("valign").matching(VALIGN).onElements(
          "thead", "tbody", "tfoot", "td", "th", "tr", "colgroup", "col")
      .allowAttributes("charoff").matching(NUMBER_OR_PERCENT).onElements(
          "td", "th", "tr", "colgroup", "col", "thead", "tbody", "tfoot")
      .allowAttributes("colspan", "rowspan").matching(NUMBER).onElements("td", "th")
      .allowAttributes("span", "width").matching(NUMBER_OR_PERCENT).onElements("colgroup", "col")
      .toFactory()
      .and(Sanitizers.BLOCKS)
      .and(Sanitizers.FORMATTING)
      .and(Sanitizers.IMAGES);

  private static final Logger log = Logger.getLogger(HtmlSanitizer.class.getName());

  private HtmlSanitizer() {}

  static String sanitize(final String html) {
    return HTML_POLICY.sanitize(html, new HtmlChangeListener<Void>() {
      @Override
      public void discardedTag(Void context, String elementName) {
        log.warning("Discarded tag \"" + elementName + "\" in text:\n" + html);
      }

      @Override
      public void discardedAttributes(
          Void context, String tagName, String... attributeNames) {
        log.warning("In tag \"" + tagName + "\", removed attributes: [\""
            + Joiner.on("\", \"").join(attributeNames) + "\"], from text:\n" + html);
      }
    }, null);
  }
}
