package com.sendclipper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class VersionTest {
  @Test
  void versionMatchesThePomAndTheChangelog() throws Exception {
    String pom = new String(Files.readAllBytes(Path.of("pom.xml")), StandardCharsets.UTF_8);
    assertTrue(
        pom.contains("<artifactId>sendclipper-java</artifactId>\n  <version>" + Version.VERSION + "</version>"),
        "pom.xml's version is not " + Version.VERSION);
    String changelog = new String(Files.readAllBytes(Path.of("CHANGELOG.md")), StandardCharsets.UTF_8);
    assertTrue(changelog.contains("## " + Version.VERSION + " "), "CHANGELOG.md has no entry for " + Version.VERSION);
  }
}
