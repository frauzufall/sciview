package sc.iview.controls.behaviours;

import sc.iview.SciView;
import graphics.scenery.Node;
import org.joml.Vector3f;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;

/**
 * Control behavior for moving a Node
 *
 * @author Kyle Harrington
 *
 */
public class NodeTranslateControl implements DragBehaviour, ScrollBehaviour {

    protected SciView sciView;
    private boolean firstEntered = true;
    private int lastX;
    private int lastY;

    public float getDragSpeed() {
        return dragXySpeed;
    }

    public void setDragSpeed( float dragSpeed ) {
        this.dragXySpeed = dragSpeed;
        this.scrollSpeed = 10 * dragSpeed;
    }

    protected float dragXySpeed;
    protected float scrollSpeed;

    public NodeTranslateControl( SciView sciView, float dragSpeed ) {
        this.sciView = sciView;
        setDragSpeed(dragSpeed);
    }

    /**
     * This function is called upon mouse down and initializes the camera control
     * with the current window size.
     *
     * x position in window
     * y position in window
     */
    @Override public void init( int x, int y ) {
        if (sciView.getActiveNode() == null) return;
        if (firstEntered) {
            lastX = x;
            lastY = y;
            firstEntered = false;
        }
    }

    @Override public void drag( int x, int y ) {
        final Node targetedNode = sciView.getActiveNode();
        if (targetedNode == null || !targetedNode.getLock().tryLock()) return;

        sciView.getCamera().getRight().mul(( x - lastX ) * dragXySpeed, dragPosUpdater);
        targetedNode.getPosition().add(dragPosUpdater);
        sciView.getCamera().getUp().mul(   ( lastY - y ) * dragXySpeed, dragPosUpdater);
        targetedNode.getPosition().add(dragPosUpdater);
        targetedNode.setNeedsUpdate(true);
        //nothing works...
        //we need to have "targetedNode.getMaximumBoundingBox().getBoundingSphere().getOrigin()" updated
        //so that shift+mouse rotates around the fresh coord...
        //
        //targetedNode.updateWorld(false,false);
        //targetedNode.setBoundingBox( targetedNode.generateBoundingBox() );

        targetedNode.getLock().unlock();

        lastX = x;
        lastY = y;
    }
    private final Vector3f dragPosUpdater = new Vector3f();

    @Override public void end( int x, int y ) {
        firstEntered = true;
    }

    @Override
    public void scroll(double wheelRotation, boolean isHorizontal, int x, int y) {
        final Node targetedNode = sciView.getActiveNode();
        if (targetedNode == null || !targetedNode.getLock().tryLock()) return;

        sciView.getCamera().getForward().mul(-1f * (float)wheelRotation * scrollSpeed, scrollPosUpdater);
        targetedNode.getPosition().add(scrollPosUpdater);
        targetedNode.setNeedsUpdate(true); //to be consistent with how it is done in drag() above

        targetedNode.getLock().unlock();
    }
    private final Vector3f scrollPosUpdater = new Vector3f();
}
