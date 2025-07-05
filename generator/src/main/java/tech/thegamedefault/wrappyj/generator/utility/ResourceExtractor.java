package tech.thegamedefault.wrappyj.generator.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourceExtractor {

  public static Path extractResource(String resourcePath, String targetDir) throws IOException {
    try (InputStream in = ResourceExtractor.class.getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new FileNotFoundException("Resource not found: " + resourcePath);
      }

      Path targetPath = Paths.get(targetDir, new File(resourcePath).getName());
      Files.createDirectories(targetPath.getParent());
      Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
      return targetPath;
    }
  }

  public static Path extractZip(String resourcePath, String targetDir) throws IOException {
    Path zipFile = extractResource(resourcePath, targetDir);
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toFile()))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        Path filePath = Paths.get(targetDir, entry.getName());
        if (entry.isDirectory()) {
          Files.createDirectories(filePath);
        } else {
          Files.createDirectories(filePath.getParent());
          Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        zis.closeEntry();
      }
    }
    return Paths.get(targetDir);
  }
}
