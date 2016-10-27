package clearcl.view.jfx;

import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Scale;

/**
 * This subclass of Scene allows for panning and zooming.
 *
 * @author royer
 */
public class PanZoomScene extends Scene
{
  private Parent mRoot;
  private volatile double mPressedX, mPressedY;
  private volatile double mFactor = 1;

  /**
   * T
   * 
   * @param pRoot
   * @param pWidth
   * @param pHeight
   * @param pFill
   */
  public PanZoomScene(Parent pRoot,
                      double pWidth,
                      double pHeight,
                      Paint pFill)
  {
    super(pRoot, pWidth, pHeight, pFill);
    mRoot = pRoot;

    setOnMousePressed((event) -> {

      if (event.getButton() == MouseButton.PRIMARY)
      {
        Point2D lRootNodePoint = mRoot.sceneToLocal(event.getX(),
                                                    event.getY());
        mPressedX = lRootNodePoint.getX();
        mPressedY = lRootNodePoint.getY();
        event.consume();
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
          Point2D lRootNodePoint = mRoot.sceneToLocal(lMouseX,
                                                      lMouseY);

          double lDeltaX = mRoot.getTranslateX() + mRoot.getScaleX()
                           * (lRootNodePoint.getX() - mPressedX);
          double lDeltaY = mRoot.getTranslateY() + mRoot.getScaleY()
                           * (lRootNodePoint.getY() - mPressedY);

          mRoot.setTranslateX(lDeltaX);
          mRoot.setTranslateY(lDeltaY);

          event.consume();
        }
      }
    });

    setOnScroll((event) -> {

      double lMouseX = event.getX();
      double lMouseY = event.getY();

      double lDelta = event.getDeltaY() * 0.001;
      // System.out.println("lDelta=" + lDelta);
      mFactor = Math.exp(lDelta);
      // System.out.println("mFactor=" + mFactor);
      if (mFactor < 0.5)
        mFactor = 0.5;
      else if (mFactor > 2)
        mFactor = 2;

      // mRoot.setScaleX(mFactor * mRoot.getScaleX());
      // mRoot.setScaleY(mFactor * mRoot.getScaleY());/**/

      Scale scale = new Scale();
      scale.setX(mFactor);
      scale.setY(mFactor);

      Point2D lSceneCenterInRoot = mRoot.sceneToLocal(lMouseX,
                                                      lMouseY);
      scale.setPivotX(lSceneCenterInRoot.getX());
      scale.setPivotY(lSceneCenterInRoot.getY());
      mRoot.getTransforms().add(scale);

      event.consume();
    });
  }
}
