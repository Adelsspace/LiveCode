package ru.hh.blokshnote.utility.colors;

import java.util.Set;

public class UserColorUtil {
  private static final String[] COLOR_POOL = {
      "0bd5c2",
      "c277f9",
      "09ae9f",
      "ff7a92",
      "ffa520",
      "FF6348",
      "e74024",
      "00a329"
  };

  public static String generateUserColor(String username, Set<String> usedColors) {
    int hashCode = Math.abs(username.hashCode());
    int initialIdx = hashCode % COLOR_POOL.length;

    for (int i = 0; i < COLOR_POOL.length; i++) {
      int idx = (initialIdx + i) % COLOR_POOL.length;
      String color = COLOR_POOL[idx];
      if (!usedColors.contains(color)) {
        return color;
      }
    }

    return COLOR_POOL[initialIdx];
  }
}
