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

import graphics.scenery.*;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import net.imagej.mesh.Mesh;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.display.ScaledARGBConverter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import org.joml.Vector3f;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.io.IOService;
import org.scijava.log.LogService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import sc.iview.SciView;

import java.io.File;
import java.io.IOException;

import static sc.iview.Utils.convertToARGB;
import static sc.iview.commands.MenuWeights.DEMO;
import static sc.iview.commands.MenuWeights.DEMO_MESH;

/**
 * An example of rigging/staging a scene for creating a visualization
 *
 * @author Kyle Harrington
 */
@Plugin(type = Command.class, label = "Scene Rigging Demo", menuRoot = "SciView", //
        menu = { @Menu(label = "Demo", weight = DEMO), //
                 @Menu(label = "Scene Rigging", weight = DEMO_MESH) })
public class SceneRiggingDemo implements Command {

    @Parameter
    private IOService io;

    @Parameter
    private LogService log;

    @Parameter
    private SciView sciView;

    @Parameter
    private UIService uiService;

    @Parameter
    private CommandService commandService;
    private Camera screenshotCam;

    @Override
    public void run() {
        final Mesh m;
        try {
            File meshFile = ResourceLoader.createFile( getClass(), "/WieseRobert_simplified_Cip1.stl" );
            m = (Mesh) io.open( meshFile.getAbsolutePath() );
        }
        catch (IOException exc) {
            log.error( exc );
            return;
        }

        Node msh = sciView.addMesh( m );
        msh.setName( "Mesh Demo" );

        //msh.fitInto( 15.0f, true );

        Material mat = new Material();
        mat.setAmbient( new Vector3f( 1.0f, 0.0f, 0.0f ) );
        mat.setDiffuse( new Vector3f( 0.8f, 0.5f, 0.4f ) );
        mat.setSpecular( new Vector3f( 1.0f, 1.0f, 1.0f ) );
        //mat.setDoubleSided( true );

        msh.setMaterial( mat );

        msh.setNeedsUpdate( true );
        msh.setDirty( true );

        sciView.centerOnNode( sciView.getActiveNode() );

        for( PointLight light : sciView.getLights() ) {
            Icosphere s = new Icosphere(1f, 1);
            s.getMaterial().setDiffuse(light.getEmissionColor());
            s.getMaterial().setAmbient(light.getEmissionColor());
            s.getMaterial().setSpecular(light.getEmissionColor());
            light.addChild(s);
        }

        screenshotCam = new DetachedHeadCamera();
        screenshotCam.setPosition( new Vector3f( 0.0f, 5.0f, -5.0f ) );
        screenshotCam.perspectiveCamera( 50.0f, sciView.getWindowWidth(), sciView.getWindowHeight(), 0.1f, 1000.0f );
        screenshotCam.setActive( true );
        sciView.addNode(screenshotCam);

        Box b = new Box(new Vector3f(1f, 1f, 1f));
        b.getMaterial().setDiffuse(new Vector3f(1f, 0, 0));
        b.getMaterial().setAmbient(new Vector3f(1f, 0, 0));
        b.getMaterial().setSpecular(new Vector3f(1f, 0, 0));
        screenshotCam.addChild(b);

        sciView.setActiveNode(b);
        sciView.centerOnNode(b);

        makeDialog();
    }

    private void makeDialog() {
        GenericDialog dialog = new NonBlockingGenericDialog("Run scene");
        dialog.showDialog();
        while( dialog.isVisible() ) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if( dialog.wasCanceled() )
            return;

        Camera prevCamera = sciView.getCamera();
        sciView.setCamera(screenshotCam);
        screenshotCam.setNeedsUpdate(true);
        screenshotCam.setDirty(true);
        Img<UnsignedByteType> screenshot = sciView.getScreenshot();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        RandomAccessibleInterval<ARGBType> colorScreenshot = convertToARGB(screenshot);
        ImageJFunctions.show(colorScreenshot);

        sciView.setCamera( prevCamera );

    }


}
