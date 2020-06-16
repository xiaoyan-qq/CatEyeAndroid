/*
 * Copyright 2018 Gustl22
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
package org.oscim.model;

import org.oscim.backend.AssetAdapter;

import java.io.InputStream;

/**
 * Enumeration of all internal VTM models.
 * <p>
 * Generate own models:
 * If using Blender for new models, start fresh project delete camera and light source, keep blender render engine.
 * Export as .obj/fbx
 * Use Fbx converter [https://github.com/libgdx/fbx-conv] and Java GUI [https://github.com/ASneakyFox/libgdx-fbxconv-gui]
 * to convert to g3d.
 * .obj is supported, too, but has troubles with textures and materials.
 * More: [https://github.com/libgdx/libgdx/wiki/Importing-Blender-models-in-LibGDX]
 */
public enum VtmModels {

    CAR("models/vehicles/car.g3db"),
    MEMORIAL("models/historic/memorial.g3db"),
    STREETLAMP("models/highway/streetlamp.g3db"),
    TREE("models/natural/tree.g3dj"),
    TREE_ASH("models/natural/tree_ash.g3db"),
    TREE_FIR("models/natural/tree_fir.g3db"),
    TREE_OAK("models/natural/tree_oak.g3db");

    private final String mPath;

    VtmModels(String path) {
        mPath = path;
    }

    /**
     * Get relative path to models. Assets path isn't included!
     */
    public String getPath() {
        return mPath;
    }

    public InputStream getModelAsStream() {
        return AssetAdapter.readFileAsStream(mPath);
    }
}
