/*-
 * #%L
 * Scenery-backed 3D visualization package for ImageJ.
 * %%
 * Copyright (C) 2016 - 2018 SciView developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.iview.commands.demo;

import graphics.scenery.Node;
import graphics.scenery.volumes.Volume;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.mesh.Mesh;
import net.imagej.ops.OpService;
import net.imagej.ops.geom.geom3d.mesh.BitTypeVertexInterpolator;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.joml.Vector3f;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import sc.iview.SciView;
import sc.iview.process.MeshConverter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static sc.iview.commands.MenuWeights.DEMO;
import static sc.iview.commands.MenuWeights.DEMO_VOLUME_RENDER;

/**
 * A demo of volume rendering.
 *
 * @author Kyle Harrington
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, label = "Volume Render/Isosurface Demo", menuRoot = "SciView", //
        menu = { @Menu(label = "Demo", weight = DEMO), //
                 @Menu(label = "Volume Render/Isosurface", weight = DEMO_VOLUME_RENDER) })
public class VolumeRenderDemo implements Command {

    @Parameter
    private DatasetIOService datasetIO;

    @Parameter
    private LogService log;

    @Parameter
    private OpService ops;

    @Parameter
    private SciView sciView;

    @Parameter(label = "Show isosurface")
    private boolean iso = true;

    @Override
    public void run() {
        final Dataset cube;
        try {
            File cubeFile = ResourceLoader.createFile( getClass(), "/cored_cube_var2_8bit.tif" );

            cube = datasetIO.open( cubeFile.getAbsolutePath() );
        }
        catch (IOException exc) {
            log.error( exc );
            return;
        }

        Volume v = (Volume) sciView.addVolume( cube, new float[] { 1, 1, 1 } );
        v.setPixelToWorldRatio(0.05f);
        v.setName( "Volume Render Demo" );
        v.setDirty(true);
        v.setNeedsUpdate(true);

        if (iso) {
            int isoLevel = 1;

            @SuppressWarnings("unchecked")
            Img<UnsignedByteType> cubeImg = ( Img<UnsignedByteType> ) cube.getImgPlus().getImg();

            Img<BitType> bitImg = ( Img<BitType> ) ops.threshold().apply( cubeImg, new UnsignedByteType( isoLevel ) );

            Mesh m = ops.geom().marchingCubes( bitImg, isoLevel, new BitTypeVertexInterpolator() );

            graphics.scenery.Mesh isoSurfaceMesh = MeshConverter.toScenery(m,false);
            v.addChild(isoSurfaceMesh);

            isoSurfaceMesh.setName( "Volume Render Demo Isosurface" );
        }

        sciView.setActiveNode(v);
        sciView.centerOnNode( sciView.getActiveNode() );
    }

    public static void main(String... args) throws Exception {
        SciView sv = SciView.create();

        CommandService command = sv.getScijavaContext().getService(CommandService.class);

        HashMap<String, Object> argmap = new HashMap<String, Object>();
        argmap.put("iso",true);

        command.run(VolumeRenderDemo.class, true, argmap);
    }
}
