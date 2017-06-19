/*
 * Bach - Java Shell Builder
 * Copyright (C) 2017 Christian Stein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.spi.ToolProvider;
import org.junit.jupiter.api.Test;

class BachTests {

  @Test
  void isInterface() {
    assertTrue(Bach.class.isInterface());
  }

  @Test
  void builder() {
    assertNotNull(new Bach.Builder().toString());
    assertNotNull(new Bach.Builder().build());
  }

  @Test
  void defaultConfiguration() {
    Bach.Configuration configuration = new Bach.Builder().build().configuration();
    assertTrue(System.getProperty("user.dir").endsWith(configuration.name()), configuration.name());
    assertEquals("1.0.0-SNAPSHOT", configuration.version());
  }

  @Test
  void configurationPropertiesAreImmutable() {
    Bach.Configuration configuration = new Bach.Builder().build().configuration();
    assertThrows(UnsupportedOperationException.class, () -> configuration.folders().clear());
  }

  @Test
  void customConfiguration() {
    Bach.Builder builder =
        new Bach.Builder()
            .name("kernel")
            .version("4.12-rc5")
            .handler(null)
            .level(Level.WARNING)
            .folder(Bach.Folder.AUXILIARY, Paths.get("aux"))
            .folder(Bach.Folder.DEPENDENCIES, Bach.Folder.Location.of(Paths.get("mods")))
            .tool(new CustomTool());
    Bach bach = builder.build();
    assertNotNull(bach.toString());
    Bach.Configuration custom = bach.configuration();
    assertEquals("kernel", custom.name());
    assertEquals("4.12-rc5", custom.version());
    assertEquals(Paths.get("aux"), custom.folders().get(Bach.Folder.AUXILIARY));
    assertEquals(Paths.get("mods"), custom.folders().get(Bach.Folder.DEPENDENCIES));
    for (Bach.Folder folder : Bach.Folder.values()) {
      assertEquals(bach.path(folder), custom.folders().get(folder));
    }
    assertEquals(CustomTool.class, custom.tools().get("custom").getClass());
  }

  @Test
  void call() {
    Bach bach = new Bach.Builder().build();
    assertEquals(0, bach.call("java", "--version"));
    assertThrows(Error.class, () -> bach.call("java", "--thisOptionDoesNotExist"));
    assertThrows(Error.class, () -> bach.call("executable, that does not exist", 1, 2, 3));
  }

  static class CustomTool implements ToolProvider {

    @Override
    public String name() {
      return "custom";
    }

    @Override
    public int run(PrintWriter out, PrintWriter err, String... args) {
      out.println("CustomTool with " + Arrays.toString(args));
      return 0;
    }
  }
}
