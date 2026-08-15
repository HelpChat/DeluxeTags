package me.clip.deluxetags;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.Test;

public class PluginDescriptorTest {

  @Test
  public void keepsApiVersionWhileRemainingReadableByBukkit188()
      throws IOException, InvalidDescriptionException {
    InputStream descriptorResource = getClass().getClassLoader().getResourceAsStream("plugin.yml");
    assertNotNull(descriptorResource);
    try (InputStream resource = descriptorResource) {
      PluginDescriptionFile description = new PluginDescriptionFile(resource);
      assertEquals("DeluxeTags", description.getName());
    }

    InputStream textResource = getClass().getClassLoader().getResourceAsStream("plugin.yml");
    assertNotNull(textResource);
    boolean hasApiVersion = false;
    boolean hasFoliaSupport = false;
    try (InputStream resource = textResource;
         BufferedReader reader = new BufferedReader(
        new InputStreamReader(resource, StandardCharsets.UTF_8)
    )) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.trim().equals("api-version: \"1.13\"")) {
          hasApiVersion = true;
        }
        if (line.trim().equals("folia-supported: true")) {
          hasFoliaSupport = true;
        }
      }
    }
    assertTrue(hasApiVersion);
    assertTrue(hasFoliaSupport);
  }
}
