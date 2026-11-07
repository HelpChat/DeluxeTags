package me.clip.deluxetags;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import me.clip.deluxetags.utils.Permissions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
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
    try (InputStream resource = textResource;
         BufferedReader reader = new BufferedReader(
        new InputStreamReader(resource, StandardCharsets.UTF_8)
    )) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.trim().equals("api-version: \"1.13\"")) {
          hasApiVersion = true;
        }
      }
    }
    assertTrue(hasApiVersion);
  }

  @Test
  public void declaresEveryCommandPermission() throws Exception {
    InputStream descriptorResource = getClass().getClassLoader().getResourceAsStream("plugin.yml");
    assertNotNull(descriptorResource);
    Set<String> declared;
    try (InputStream resource = descriptorResource;
         InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
      // PluginDescriptionFile#getPermissions needs a running server on older Bukkit APIs.
      // A non-dot separator stops dotted permission nodes being split into nested sections.
      YamlConfiguration descriptor = new YamlConfiguration();
      descriptor.options().pathSeparator('/');
      descriptor.load(reader);
      ConfigurationSection permissions = descriptor.getConfigurationSection("permissions");
      assertNotNull(permissions);
      declared = permissions.getKeys(false);
    }

    for (Field field : Permissions.class.getFields()) {
      String node = (String) field.get(null);
      assertTrue(node + " is missing from plugin.yml", declared.contains(node));
    }
  }
}
