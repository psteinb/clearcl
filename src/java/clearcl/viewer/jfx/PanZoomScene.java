package clearcl.viewer.jfx;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

/**
 * This subclass of Scene allows for panning and zooming.
 *
 * @author royer
 */
public class PanZoomScene extends Scene
{
  private Node mPanZoomNode;
  private volatile double mPressedX, mPressedY;
  private Affine mAffine = new Affine();
  private Scale mScale;
  private Translate mTranslate;
  private Point2D mSceneCenterInRoot;
  private boolean mPivotInitialized;

  private volatile float mMouseX, mMouseY;

  /**
   * Constructs a PanZoomScene from a stage, root node, a node to pan and zoom,
   * window dimensions and a fill color.
   * 
   * @param pStage
   *          stage
   * 
   * @param pRoot
   *          root node
   * @param pNodeToPanZoom
   *          node to pan and zoom
   * @param pWidth
   *          window width
   * @param pHeight
   *          window height
   * @param pFill
   *          fill color
   */

  public PanZoomScene(Stage pStage,
                      Parent pRoot,
                      Node pNodeToPanZoom,
                      double pWidth,
                      double pHeight,
                      Paint pFill)
  {
    super(pRoot, pWidth, pHeight, pFill);
    mPanZoomNode = pNodeToPanZoom;

    mScale = new Scale();
    mTranslate = new Translate();
    mAffine = new Affine();
    mPanZoomNode.getTransforms().add(mAffine);

    pStage.setFullScreenExitHint("Double click gain to exit fullscreen mode");

    setOnMouseMoved((e) -> {
      mMouseX = (float) e.getX();
      mMouseY = (float) e.getY();
    });

    setOnMousePressed((event) -> {

      if (event.getButton() == MouseButton.PRIMARY)
      {

        if (event.getClickCount() == 2)
        {
          double lSceneWidthBefore = getWidth();
          double lSceneHeightBefore = getHeight();
          pStage.setFullScreen(!pStage.isFullScreen());
          double lSceneWidthAfter = getWidth();
          double lSceneHeightAfter = getHeight();

          scaleScene(Math.sqrt(lSceneHeightAfter
                               / lSceneHeightBefore));
        }
        else
        {

          double lMouseX = event.getX();
          double lMouseY = event.getY();
          double lSceneWidth = getWidth();
          double lSceneHeight = getHeight();

          if (lMouseX >= 10 && lMouseX <= lSceneWidth - 11
              && lMouseY >= 10
              && lMouseY <= lSceneHeight - 11)
          {
            Point2D lRootNodePoint =
                                   mPanZoomNode.sceneToLocal(event.getX(),
                                                             event.getY());
            mPressedX = lRootNodePoint.getX();
            mPressedY = lRootNodePoint.getY();
            event.consume();
          }
        }
      }
    });

    setOnMouseDragged((event) -> {
      if (event.getButton() == MouseButton.PRIMARY)
      {

        double lMouseX = event.getX();
        double lMouseY = event.getY();
        double lSceneWidth = getWidth();
        double lSceneHeight = getHeight();

        if (lMouseX >= 10 && lMouseX <= lSceneWidth - 11
            && lMouseY >= 10
            && lMouseY <= lSceneHeight - 11)
        {
          Point2D lRootNodePoint = mPanZoomNode.sceneToLocal(lMouseX,
                                                             lMouseY);

          double lDeltaX = lRootNodePoint.getX() - mPressedX;
          double lDeltaY = lRootNodePoint.getY() - mPressedY;

          mTranslate.setX(lDeltaX);
          mTranslate.setY(lDeltaY);

          mAffine.append(mTranslate);

          event.consume();
        }
      }
    });

    setOnScroll((event) -> {

      double lDelta = event.getDeltaY() * 0.001;
      double lDeltaFactor = Math.exp(lDelta);

      scaleScene(lDeltaFactor);

      if (!mPivotInitialized)
      {
        resetZoomPivot();
        mPivotInitialized = true;
      }

      event.consume();
    });

    widthProperty().addListener((obs, o, n) -> {
      // resetZoomPivot();
      scaleScene(Math.sqrt(n.doubleValue() / o.doubleValue()));
    });

    heightProperty().addListener((obs, o, n) -> {
      // resetZoomPivot();
      scaleScene(Math.sqrt(n.doubleValue() / o.doubleValue()));
    });

  }

  private void scaleScene(double lDeltaFactor)
  {
    if (lDeltaFactor < 0.5)
      lDeltaFactor = 0.5;
    else if (lDeltaFactor > 2)
      lDeltaFactor = 2;

    resetZoomPivot();

    mScale.setX(lDeltaFactor);
    mScale.setY(lDeltaFactor);

    mAffine.append(mScale);
  }

  public void resetZoomPivot()
  {
    double lSceneWidth = getWidth();
    double lSceneHeight = getHeight();

    Point2D lRootNodePoint = mPanZoomNode.sceneToLocal(lSceneWidth
                                                       / 2,
                                                       lSceneHeight
                                                            / 2);

    mScale.setPivotX(lRootNodePoint.getX());
    mScale.setPivotY(lRootNodePoint.getY());/**/
  }

  public float getMouseX()
  {
    return mMouseX;
  }

  public float getMouseY()
  {
    return mMouseY;
  }

}
