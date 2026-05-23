package dev.threadly.core.runtime.executors;

import java.util.Map;
import java.util.regex.*;

/** Renders {{variable}} placeholders from session variables. */
public final class TemplateEngine {

  private static final Pattern VAR = Pattern.compile("\\{\\{([^}]+)\\}\\}");

  private TemplateEngine() {}

  public static String render(String template, Map<String, Object> vars) {
    if (template == null) return "";
    Matcher m = VAR.matcher(template);
    StringBuilder sb = new StringBuilder();
    while (m.find()) {
      String key = m.group(1).trim();
      Object val = vars.getOrDefault(key, "");
      m.appendReplacement(sb, Matcher.quoteReplacement(val != null ? val.toString() : ""));
    }
    m.appendTail(sb);
    return sb.toString();
  }
}
