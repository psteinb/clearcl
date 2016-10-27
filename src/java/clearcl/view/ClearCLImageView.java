package clearcl.view;

import java.util.concurrent.CountDownLatch;

import com.sun.javafx.application.PlatformImpl;

import clearcl.ClearCLImage;
import clearcl.view.jfx.ClearCLImagePanel;
import clearcl.view.jfx.PanZoomScene;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * utility to display ClearCL images and monitoring the change of their content.
 *
 * @author royer
 */
public class ClearCLImageView
{
  private Stage mStage = null;
  private ClearCLImagePanel mImagePanel;

  /**
   * Opens a window showing the image content. View can be panned and zoomed.
   * 
   * @param pImage
   * @return
   */
  public static ClearCLImageView view(ClearCLImage pImage)
  {
    ClearCLImageView lViewImage = new ClearCLImageView(pImage,
                                                       "Image",
                                                       (int) (pImage.getWidth() * 1.5),
                                                       (int) (pImage.getHeight() * 1.5));
    return lViewImage;
  }

  /**
   * Creates a view for a given image, window title, and window dimensions.
   * 
   * @param pClearCLImage
   *          image
   * @param pWindowTitle
   *          window title
   * @param pWindowWidth
   *          window width
   * @param pWindowHeight
   *          window height
   */
  public ClearCLImageView(ClearCLImage pClearCLImage,
                          String pWindowTitle,
                          int pWindowWidth,
                          int pWindowHeight)
  {
    super();

    PlatformImpl.startup(() -> {
    });
    final CountDownLatch lCountDownLatch = new CountDownLatch(1);
    Platform.runLater(() -> {
      mStage = new Stage();
      mStage.setTitle("My New Stage Title");

      mImagePanel = new ClearCLImagePanel(pClearCLImage);

      mImagePanel.updateImage();

      PanZoomScene lPanZoomScene = new PanZoomScene(mImagePanel,
                                                    pWindowWidth,
                                                    pWindowHeight,
                                                    Color.BLACK);

      mStage.setScene(lPanZoomScene);
      mStage.show();
      lCountDownLatch.countDown();
    });
    try
    {
      lCountDownLatch.await();
    }
    catch (InterruptedException e)
    {
    }

  }

  /**
   * Waits (blocking call) while window is showing.
   */
  public void waitWhileShowing()
  {
    while (mStage.isShowing())
    {
      try
      {
        Thread.sleep(100);
      }
      catch (InterruptedException e)
      {
      }
    }
  }

}
