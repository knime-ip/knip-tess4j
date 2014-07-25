/**
 * Copyright @ 2013 Quan Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.sourceforge.tess4j.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.sourceforge.vietocr.ImageHelper;

public class Utils {

    /**
     * Gets user-friendly name of the public static final constant defined in a class or an
     * interface for display purpose.
     *
     * @param value the constant value
     * @param c type of class or interface
     * @return name
     */
    public static String getConstantName(final Object value, final Class c) {
        for (final Field f : c.getDeclaredFields()) {
            final int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod)) {
                try {
                    if (f.get(null).equals(value)) {
                        return f.getName();
                    }
                } catch (final IllegalAccessException e) {
                    return String.valueOf(value);
                }
            }
        }
        return String.valueOf(value);
    }
    
    /**
     * Converts <code>BufferedImage</code> to <code>ByteBuffer</code>.
     * 
     * @param bi Input image
     * @return pixel data
     */
    public static ByteBuffer convertImageData(BufferedImage bi) {
        DataBuffer buff = bi.getRaster().getDataBuffer();
        // ClassCastException thrown if buff not instanceof DataBufferByte because raster data is not necessarily bytes.
        // Convert the original buffered image to grayscale.
        if (!(buff instanceof DataBufferByte)) {
            bi = ImageHelper.convertImageToGrayscale(bi);
            buff = bi.getRaster().getDataBuffer();
        }
        final byte[] pixelData = ((DataBufferByte) buff).getData();
        //        return ByteBuffer.wrap(pixelData);
        final ByteBuffer buf = ByteBuffer.allocateDirect(pixelData.length);
        buf.order(ByteOrder.nativeOrder());
        buf.put(pixelData);
        buf.flip();
        return buf;
    }
}
