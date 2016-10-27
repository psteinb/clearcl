package clearcl.view.jfx;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.toIntExact;

import java.nio.ByteBuffer;

import clearcl.ClearCLHostImage;
import clearcl.ClearCLImage;
import coremem.buffers.ContiguousBuffer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * JavaFX Panel for displaying the contents of a ClearCLImage.
 *
 * @author royer
 */
public class ClearCLImagePanel extends StackPane
{

  private Canvas mCanvas;
  private GraphicsContext mGraphicsContext2D;
  private ClearCLImage mClearCLImage;
  private ClearCLHostImage mClearCLHostImage;
  private byte[] mArray;

  /**
   * Creates a panel for a given ClearCL image. 
   * @param pClearCLImage
   */
  public ClearCLImagePanel(ClearCLImage pClearCLImage)
  {
    super();
    mClearCLImage = pClearCLImage;
    mClearCLHostImage = ClearCLHostImage.allocateSameAs(pClearCLImage);
    mCanvas = new Canvas(pClearCLImage.getWidth(),
                         pClearCLImage.getHeight());
    mCanvas.setCache(true);
    mCanvas.setCacheHint(CacheHint.SPEED);
    mGraphicsContext2D = mCanvas.getGraphicsContext2D();

    getChildren().add(mCanvas);
    StackPane.setAlignment(mCanvas, Pos.CENTER);

    mGraphicsContext2D = mCanvas.getGraphicsContext2D();

    pClearCLImage.addListener((q, s) -> {
      q.waitToFinish();

      updateImage();

    });

    updateImage();
  }

  /**
   * Updates the display of this ImageView. This is called automatically through
   * an internal listener when the image contents (may) have changed.
   */
  public void updateImage()
  {
    mClearCLImage.copyTo(mClearCLHostImage, true);

    long lSizeInBytes = mClearCLHostImage.getSizeInBytes();
    long lWidth = mClearCLHostImage.getWidth();
    long lHeight = mClearCLHostImage.getHeight();

    if (mArray == null || mArray.length != mClearCLHostImage.getVolume() * 3)
      mArray = new byte[toIntExact(mClearCLHostImage.getVolume() * 3)];

    ContiguousBuffer lBuffer = ContiguousBuffer.wrap(mClearCLHostImage.getContiguousMemory());

    float lMin = Float.POSITIVE_INFINITY;
    float lMax = Float.NEGATIVE_INFINITY;

    lBuffer.rewind();
    while (lBuffer.hasRemaining(mClearCLHostImage.getPixelSizeInBytes()))
    {
      float lValue = lBuffer.readFloat();
      lMin = min(lMin, lValue);
      lMax = max(lMax, lValue);
    }

    lBuffer.rewind();
    int i = 0;
    while (lBuffer.hasRemaining(mClearCLHostImage.getPixelSizeInBytes()))
    {
      float lValue = (lBuffer.readFloat() - lMin) / (lMax - lMin);
      // System.out.println(lValue);
      mArray[i++] = (byte) (lValue * 255);
      mArray[i++] = (byte) (lValue * 255);
      mArray[i++] = (byte) (lValue * 255);
    }

    // for (i = 0; i < mArray.length; i++)
    // mArray[i] = (byte) (Math.random() * 255);

    Platform.runLater(() -> {
      PixelFormat<ByteBuffer> lPixelFormat = PixelFormat.getByteRgbInstance();
      PixelWriter pixelWriter = mGraphicsContext2D.getPixelWriter();

      pixelWriter.setPixels(0,
                            0,
                            (int) lWidth,
                            (int) lHeight,
                            lPixelFormat,
                            mArray,
                            0,
                            (int) (lWidth * 3));

      mGraphicsContext2D.beginPath();
      mGraphicsContext2D.rect(0, 0, lWidth, lHeight);
      mGraphicsContext2D.setStroke(Color.RED);
      mGraphicsContext2D.stroke();
    });
  }

}
