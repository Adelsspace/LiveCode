package ru.hh.blokshnote.utility.colors;

public class UserColorUtil {
  private static final String[] COLOR_POOL = {
      "#e6194b", // Red
      "#3cb44b", // Green
      "#ffe119", // Yellow
      "#4363d8", // Blue
      "#f58231", // Orange
      "#911eb4", // Purple
      "#46f0f0", // Cyan
      "#f032e6", // Magenta
      "#bcf60c", // Lime
      "#fabebe", // Pink
      "#008080", // Teal
      "#e6beff", // Lavender
      "#fffac8", // Beige
      "#800000", // Maroon
      "#aaffc3", // Mint
      "#808000", // Olive
      "#ffd8b1", // Apricot
      "#000075", // Navy
      "#00aaff", // Sky Blue
      "#ff66b2"  // Bright Pink
  };

  public static String generateUserColor(String username) {
    int hashCode = Math.abs(username.hashCode());
    return COLOR_POOL[hashCode % COLOR_POOL.length];
  }
}
