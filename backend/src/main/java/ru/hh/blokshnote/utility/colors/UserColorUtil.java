package ru.hh.blokshnote.utility.colors;

public class UserColorUtil {
  private static final String[] COLOR_POOL = {
      "#0bd5c2",
      "#c277f9",
      "#09ae9f",
      "#ff7a92",
      "#ffa520",
      "#FF6348",
      "#e74024",
      "#00a329"
  };

  public static String generateUserColor(String username) {
    int hashCode = Math.abs(username.hashCode());
    return COLOR_POOL[hashCode % COLOR_POOL.length];
  }
}
