package me.clip.deluxetags.storage;

import static org.junit.Assert.*;
import java.io.DataInputStream;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import org.junit.Test;

public class ShadedStorageTest {
    @Test public void loadsBundledDriverAndPoolWithoutServerDependencies() throws Exception {
        File jar = new File(System.getProperty("deluxetags.shadedJar"));
        try (URLClassLoader loader = new URLClassLoader(new URL[]{jar.toURI().toURL()}, ClassLoader.getSystemClassLoader().getParent())) {
            Class<?> type = loader.loadClass("me.clip.deluxetags.libs.mysql.cj.jdbc.MysqlDataSource");
            Object source = type.getConstructor().newInstance();
            type.getMethod("setSslMode", String.class).invoke(source, "VERIFY_IDENTITY");
            type.getMethod("setCharacterEncoding", String.class).invoke(source, "UTF-8");
            type.getMethod("setConnectTimeout", int.class).invoke(source, 500);
            assertTrue(source instanceof javax.sql.DataSource);
            Object driver = loader.loadClass("me.clip.deluxetags.libs.mysql.cj.jdbc.Driver").getConstructor().newInstance();
            assertTrue(((java.sql.Driver) driver).acceptsURL("jdbc:mysql://localhost/test"));
            Class<?> configType = loader.loadClass("me.clip.deluxetags.libs.hikari.HikariConfig");
            Object config = configType.getConstructor().newInstance();
            configType.getMethod("setDataSource", javax.sql.DataSource.class).invoke(config, source);
            configType.getMethod("setMinimumIdle", int.class).invoke(config, 0);
            configType.getMethod("setInitializationFailTimeout", long.class).invoke(config, -1L);
            Object pool = loader.loadClass("me.clip.deluxetags.libs.hikari.HikariDataSource").getConstructor(configType).newInstance(config);
            ((AutoCloseable) pool).close();
        }
        try (JarFile archive = new JarFile(jar)) {
            assertNull(archive.getEntry("com/mysql/cj/jdbc/Driver.class"));
            assertNull(archive.getEntry("com/zaxxer/hikari/HikariConfig.class"));
            java.util.Enumeration<java.util.jar.JarEntry> entries = archive.entries();
            while (entries.hasMoreElements()) {
                java.util.jar.JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class") || entry.getName().startsWith("META-INF/")
                        || entry.getName().endsWith("module-info.class")) continue;
                try (DataInputStream bytes = new DataInputStream(archive.getInputStream(entry))) {
                    assertEquals(0xCAFEBABE, bytes.readInt());
                    bytes.readUnsignedShort();
                    assertTrue("Not Java 8 compatible: " + entry.getName(), bytes.readUnsignedShort() <= 52);
                }
            }
        }
    }
}
