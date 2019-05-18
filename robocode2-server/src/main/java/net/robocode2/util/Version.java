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
      InputStream inputStream = Version.class.getClassLoader().getResourceAsStream("version.txt");
      if (inputStream != null) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
          version = reader.readLine().trim();
          inputStream.close();
        } catch (IOException e) {
          throw new IllegalStateException("Cannot read version");
        }
      }
    }
    return version;
  }
}
