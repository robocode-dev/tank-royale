package net.robocode2.util;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@UtilityClass
public class Version {

  private String version;

  public String getVersion() {
    if (version == null) {
      try (InputStream inputStream =
              Version.class.getClassLoader().getResourceAsStream("version.txt");
          BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
        version = reader.readLine().trim();
      } catch (IOException e) {
        throw new IllegalStateException("Cannot read version");
      }
    }
    return version;
  }
}
