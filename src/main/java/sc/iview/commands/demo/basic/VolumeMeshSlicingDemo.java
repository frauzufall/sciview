/*-
 * #%L
 * Scenery-backed 3D visualization package for ImageJ.
 * %%
 * Copyright (C) 2016 - 2021 SciView developers.
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
package sc.iview.commands.demo.basic;

import graphics.scenery.Mesh;
import graphics.scenery.PointLight;
import graphics.scenery.volumes.Colormap;
import graphics.scenery.volumes.SlicingPlane;
import graphics.scenery.volumes.TransferFunction;
import graphics.scenery.volumes.Volume;
import ij.IJ;
import io.scif.formats.TIFFFormat;
import io.scif.img.ImgOpener;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.mesh.io.stl.STLMeshIO;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.scijava.InstantiableException;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.io.IOService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;
import sc.iview.SciView;
import sc.iview.io.OBJMeshIO;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static sc.iview.commands.MenuWeights.DEMO;
import static sc.iview.commands.MenuWeights.DEMO_BASIC;
import static sc.iview.commands.MenuWeights.DEMO_BASIC_LINES;

/**
 * A demo of lines.
 *
 * @author Kyle Harrington
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, label = "Lines Demo", menuRoot = "SciView", //
        menu = { @Menu(label = "Demo", weight = DEMO), //
                 @Menu(label = "Basic", weight = DEMO_BASIC), //
                 @Menu(label = "Lines", weight = DEMO_BASIC_LINES) })
public class VolumeMeshSlicingDemo implements Command {

    @Parameter
    float scalex;

    @Parameter
    float scaley;

    @Parameter
    float scalez;

    @Parameter
    File raw;

    @Parameter(style = FileWidget.DIRECTORY_STYLE)
    File meshes;

    @Parameter
    IOService ioService;

    @Parameter
    DatasetIOService datasetIOService;

    @Parameter
    private SciView sciView;

    @Parameter
    private PluginService pluginService;

    @Parameter
    private UIService uiService;

    @Parameter
    private DatasetService datasetService;

    @Override
    public void run() {
        System.out.println("Starting volume mesh slicing demo");
        Volume volume = null;
        System.out.println("Adding volume from " + raw.getAbsolutePath() + "..");
        Img image = ImageJFunctions.wrap(IJ.openImage(raw.getAbsolutePath()));
//        Img image = null;
//        try {
//            image = datasetIOService.open(raw.getAbsolutePath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        uiService.show(image);
//        volume = Volume.fromPath(raw.toPath(), sciView.getHub());
//        sciView.addNode(volume, true);
        volume = sciView.addVolume((RandomAccessibleInterval) image);
//        volume.setPixelToWorldRatio(0.03f);
        volume.setColormap(Colormap.get("plasma"));
        volume.setScale(new Vector3f(scalex, scaley, scalez));
//        volume.setPosition(new int[]{2, 2, 2});
        volume.getConverterSetups().get(0).setDisplayRange(0, 1700);
//        volume.setTransferFunction(TransferFunction.ramp(0.02f, 0.98f));
        System.out.println("Added volume.");

        SlicingPlane slicingPlane = new SlicingPlane();
        sciView.addChild(slicingPlane);
        sciView.getFloor().setVisible(false);
        slicingPlane.addTargetVolume(volume);
        volume.setSlicingMode(Volume.SlicingMode.Both);
        System.out.println("Added slicing plane.");

        File[] files = meshes.listFiles();
        for (File file : files) {
            try {
                System.out.println("Adding mesh " + file.getAbsolutePath() + "..");
                OBJMeshIO ioPlugin = (OBJMeshIO) pluginService.getPlugin(OBJMeshIO.class).createInstance();
                Mesh mesh = ioPlugin.open(file.getAbsolutePath());
                mesh.getMaterial().setAmbient(new Vector3f(1.0f, 1.0f, 1.0f));
                mesh.getMaterial().setDiffuse(new Vector3f(0.6f, 1.0f, 0.8f));
                mesh.getMaterial().setSpecular(new Vector3f(1.0f, 1.0f, 1.0f));
                mesh.getMaterial().setRoughness(4.0f);
                mesh.getMaterial().setWireframe(true);
//                mesh.setScale(new Vector3f(0.1f, 0.1f, 0.1f));
//                Vector3f max = mesh.getBoundingBox().getMax();
//                Vector3f min = mesh.getBoundingBox().getMin();
//                float width = max.x - min.x;
//                float height = max.y - min.y;
//                float depth = max.z - min.z;
//                mesh.setPosition(new Vector3f(-width*0.05f, -height*0.05f, -depth*0.05f));
                sciView.addNode(mesh);
                volume.addChild(mesh);
                mesh.setNeedsUpdate(true);
                mesh.setNeedsUpdateWorld(true);
            } catch (InstantiableException e) {
                e.printStackTrace();
            }
        }

        for (PointLight light : sciView.getLights()) {
            light.setPosition(new int[]{(int) (Math.random()*10-5), (int) (Math.random()*10-5), (int) (Math.random()*100-50)});
            light.setLightRadius(100);
            light.setIntensity(2f);
        }

        System.out.println("Added all meshes. Animating slicing plane..");

        sciView.animate(10, new Thread(() -> {
            float y = (float) (Math.sin(((System.currentTimeMillis() % 20000) / 20000f) * Math.PI * 2) * 2);
//            System.out.println(y);
            slicingPlane.setRotation(new Quaternionf().rotateXYZ(y,0.0f,0.0f));
            slicingPlane.setNeedsUpdate(true);
            slicingPlane.setNeedsUpdateWorld(true);
            slicingPlane.updateWorld(true, false);
        }));
    }

    public static void main(String... args) throws Exception {
        SciView sv = SciView.create();

        CommandService command = sv.getScijavaContext().getService(CommandService.class);

        HashMap<String, Object> argmap = new HashMap<>();
        argmap.put("raw", "/home/random/Development/hips/data/Keller_CellTrackingChallenge/t033-scale0.5-1.tif");
        argmap.put("meshes", "/home/random/Development/hips/data/res/s1_meshes");
        argmap.put("scalex", 1.0);
        argmap.put("scaley", 1.0);
        argmap.put("scalez", 1.0);

        command.run(VolumeMeshSlicingDemo.class, true, argmap);
    }
}
