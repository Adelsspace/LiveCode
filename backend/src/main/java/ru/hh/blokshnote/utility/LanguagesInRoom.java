package ru.hh.blokshnote.utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum LanguagesInRoom {
  JAVA("java", """
        public class Main {
            public static void main(String[] args) {
                // Начните писать код
            }
        }
        """),
  PYTHON("python", "# Начните писать код\n"),
  JAVASCRIPT("javascript", """
        // Начните писать код
        function main() {

        }

        main();
        """),
  TYPESCRIPT("typescript", """
        // Начните писать код
        function main(): void {

        }

        main();
        """),
  PLAIN("plain", "Начните писать код\n");

  private final String alias;
  private final String template;

  LanguagesInRoom(String alias, String template) {
    this.alias = alias;
    this.template = template;
  }

  public String getAlias() {
    return alias;
  }

  public String getTemplate() {
    return template;
  }

  private static final Map<String, LanguagesInRoom> ALIAS_MAP = new HashMap<>();
  private static final Set<String> TEMPLATES_SET = new HashSet<>();

  static {
    for (LanguagesInRoom lang : values()) {
      ALIAS_MAP.put(lang.alias, lang);
      TEMPLATES_SET.add(lang.template);
    }
  }

  public static String getTemplateByAlias(String alias) {
    return ALIAS_MAP.getOrDefault(alias, PLAIN).getTemplate();
  }

  public static boolean isInTemplatesSet(String text) {
    return TEMPLATES_SET.contains(text);
  }
}
