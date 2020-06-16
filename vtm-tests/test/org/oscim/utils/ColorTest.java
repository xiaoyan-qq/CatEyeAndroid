/*
 * Copyright 2019 Gustl22
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.oscim.utils;

import org.junit.Test;
import org.oscim.backend.canvas.Color;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.oscim.backend.canvas.Color.b;
import static org.oscim.backend.canvas.Color.g;
import static org.oscim.backend.canvas.Color.r;

public class ColorTest {

    // See: https://en.wikipedia.org/wiki/ANSI_escape_code
    public static final String ANSI_RESET = "\033[0m";
    public static final String ANSI_PREFIX_BACKGROUND_24Bit = "\033[48;2;";
    public static final String ANSI_PREFIX_FOREGROUND_24Bit = "\033[38;2;";

    @Test
    public void testColorHSV() {
        List<Integer> colors = initHSVTest();
        for (int color : colors) {
            // Try to display them in terminal (Intellij not supports 24 bit colors)
            System.out.println(ANSI_PREFIX_BACKGROUND_24Bit + Color.r(color) + ";" + Color.g(color) + ";" + Color.b(color) + "m "
                    + Color.toString(color) + "\t" + (new Color.HSV(ColorUtil.rgbToHsv(r(color), g(color), b(color)))).toString()
                    + ANSI_RESET);
        }
    }

    public List<Integer> initHSVTest() {
        List<Integer> colors = new ArrayList<>();
        int color;

        // Hue
        color = Color.get(255, 0, 0);
        colors.add(color);
        colors.add(ColorUtil.modHsv(color, 0.33f, 1f, 1f, false));

        color = Color.get(0, 0, 255);
        colors.add(color);
        colors.add(ColorUtil.modHsv(color, 0.33f, 1f, 1f, false));

        // Saturation
        color = Color.get(255, 200, 200);
        colors.add(color);
        colors.add(ColorUtil.modHsv(color, 0f, 1.5f, 1f, false));

        color = Color.get(255, 0, 0);
        colors.add(color);
        colors.add(ColorUtil.modHsv(color, 0f, 0.5f, 1f, false));

        // Lightness (value)
        color = Color.get(255, 255, 255);
        colors.add(color);
        colors.add(ColorUtil.modHsv(color, 0f, 1f, 0.8f, false));

        color = Color.get(0, 0, 0);
        colors.add(color);
        colors.add(ColorUtil.modHsv(color, 0f, 1f, 1.5f, false));

        return colors;
    }

    private void displayHSVColorsInBrowser(List<Integer> colors) {
        try {
            File tempFile;
            tempFile = File.createTempFile("test-color-", ".html");
            tempFile.deleteOnExit();
            StringBuilder builder = new StringBuilder("<html>");

            for (int color : colors) {
                builder.append(String.format("<div><pre style=\"background:rgb(%s,%s,%s);margin:0;\">", Color.r(color), Color.g(color), Color.b(color)));
                builder.append(Color.toString(color));
                builder.append("\t");
                builder.append((new Color.HSV(ColorUtil.rgbToHsv(r(color), g(color), b(color)))).toString());
                builder.append("</pre></div>");
            }
            builder.append("</html>");

            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(builder.toString());
            writer.close();

            Desktop.getDesktop().browse(tempFile.toURI());

            Thread.sleep(2000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ColorTest test = new ColorTest();
        List<Integer> colors = test.initHSVTest();
        test.displayHSVColorsInBrowser(colors);
    }
}
