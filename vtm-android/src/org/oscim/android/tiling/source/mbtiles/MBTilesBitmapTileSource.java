/*
 * Copyright 2019 Andrea Antonello
 * Copyright 2019 devemux86
 * Copyright 2019 Kostas Tzounopoulos
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
package org.oscim.android.tiling.source.mbtiles;

/**
 * A tile source for MBTiles raster databases.
 */
public class MBTilesBitmapTileSource extends MBTilesTileSource {

    /**
     * Create a tile source for MBTiles raster databases.
     *
     * @param path the path to the MBTiles database.
     */
    public MBTilesBitmapTileSource(String path) {
        this(path, null, null);
    }

    /**
     * Create a tile source for MBTiles raster databases.
     *
     * @param path             the path to the MBTiles database.
     * @param alpha            an optional alpha value [0-255] to make the tiles transparent.
     * @param transparentColor an optional color that will be made transparent in the bitmap.
     */
    public MBTilesBitmapTileSource(String path, Integer alpha, Integer transparentColor) {
        super(new MBTilesBitmapTileDataSource(path, alpha, transparentColor));
    }
}
