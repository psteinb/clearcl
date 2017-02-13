package clearcl.viewer;

import static java.lang.Math.toIntExact;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLHostImageBuffer;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.enums.HostAccessType;
import clearcl.enums.KernelAccessType;
import clearcl.enums.MemAllocMode;
import clearcl.exceptions.ClearCLUnsupportedException;
import clearcl.interfaces.ClearCLImageInterface;
import clearcl.ocllib.OCLlib;
import clearcl.ops.MinMax;
import clearcl.util.ElapsedTime;
import clearcl.util.Region2;
import coremem.ContiguousMemoryInterface;
import coremem.enums.NativeTypeEnum;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * JavaFX Panel for displaying the contents of a ClearCLImage.
 *
 * @author royer
 */
public class ClearCLImagePanel extends StackPane
{
  private static final float cSmoothingFactor = 0.2f;

  private Canvas mCanvas;
  private GraphicsContext mGraphicsContext2D;
  private ClearCLImageInterface mClearCLImage;
  private ClearCLBuffer mRenderRGBBuffer;
  private ClearCLHostImageBuffer mClearCLHostImage;
  private byte[] mPixelArray;
  private ClearCLProgram mProgram;
  private ClearCLKernel mRenderKernel;
  private MinMax mMinMax;

  private ReentrantLock mLock = new ReentrantLock();

  private final BooleanProperty mAuto =
                                      new SimpleBooleanProperty(true);
  private final FloatProperty mMin = new SimpleFloatProperty(0);
  private final FloatProperty mMax = new SimpleFloatProperty(1);
  private final FloatProperty mGamma = new SimpleFloatProperty(1);
  private final IntegerProperty mZ = new SimpleIntegerProperty(0);
  private final IntegerProperty mNumberOfSteps =
                                               new SimpleIntegerProperty(128);

  private final ObjectProperty<RenderMode> mRenderMode =
                                                       new SimpleObjectProperty<>(RenderMode.Slice);

  private Float mTrueMin = 0f, mTrueMax = 1f;

  /**
   * Creates a panel for a given ClearCL image.
   * 
   * @param pClearCLImage image
   */
  public ClearCLImagePanel(ClearCLImageInterface pClearCLImage)
  {
    super();
    mClearCLImage = pClearCLImage;

    ClearCLContext lContext = pClearCLImage.getContext();

    if (pClearCLImage.getDimension() == 1)
    {
      throw new ClearCLUnsupportedException("1D image visualizationnot supported");
    }

    mRenderRGBBuffer =
                     lContext.createBuffer(MemAllocMode.AllocateHostPointer,
                                           HostAccessType.ReadOnly,
                                           KernelAccessType.WriteOnly,
                                           4,
                                           NativeTypeEnum.Byte,
                                           Region2.region(mClearCLImage.getDimensions()));

    try
    {
      mProgram = lContext.createProgram(OCLlib.class,
                                        "render/render.cl");

      mProgram.addBuildOptionAllMathOpt();
      mProgram.buildAndLog();

      if (pClearCLImage.getDimension() == 1)
      {
        throw new ClearCLUnsupportedException("1D image visualizationnot supported");
      }

      mMinMax = new MinMax(lContext.getDefaultQueue());

    }
    catch (IOException e)
    {
      e.printStackTrace();
      throw new RuntimeException("Cannot build program", e);
    }

    mClearCLHostImage =
                      ClearCLHostImageBuffer.allocateSameAs(mRenderRGBBuffer);

    backgroundProperty().set(new Background(new BackgroundFill(Color.BLACK,
                                                               CornerRadii.EMPTY,
                                                               Insets.EMPTY)));/**/

    mCanvas = new Canvas(pClearCLImage.getWidth(),
                         pClearCLImage.getHeight());
    // mCanvas.setCache(false);
    // mCanvas.setCacheHint(CacheHint.SPEED);
    mGraphicsContext2D = mCanvas.getGraphicsContext2D();

    getChildren().add(mCanvas);
    StackPane.setAlignment(mCanvas, Pos.CENTER);

    mGraphicsContext2D = mCanvas.getGraphicsContext2D();

    pClearCLImage.addListener((q, s) -> {

      ElapsedTime.measure("q.waitToFinish();",
                          () -> q.waitToFinish());

      updateImage();
    });

    mMin.addListener((e) -> {
      updateImage();
    });
    mMax.addListener((e) -> {
      updateImage();
    });
    mAuto.addListener((e) -> {
      updateImage();
    });
    mGamma.addListener((e) -> {
      updateImage();
    });
    mZ.addListener((e) -> {
      updateImage();
    });
    mRenderMode.addListener((e) -> {
      updateImage();
    });

    mNumberOfSteps.set((int) Math.min(mNumberOfSteps.get(),
                                      pClearCLImage.getDepth()));

    updateImage();
  }
  
  

  /**
   * Updates the display of this ImageView. This is called automatically through
   * an internal listener when the image contents (may) have changed.
   */
  public void updateImage()
  {

    boolean lTryLock = false;
    try
    {
      lTryLock = mLock.tryLock(1, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e)
    {
    }

    if (lTryLock)
    {
      try
      {
        // System.out.println("Update View");
        float lMin = 0;
        float lMax = 1;

        if (mAuto.get() || mTrueMin == null)
        {
          float[] lMinMax = mMinMax.minmax(mClearCLImage, 32);
          mTrueMin = (1-cSmoothingFactor)*lMinMax[0]+ cSmoothingFactor*mTrueMin;
          mTrueMax = (1-cSmoothingFactor)*lMinMax[1]+ cSmoothingFactor*mTrueMax;

          lMin = mTrueMin;
          lMax = mTrueMax;
        }
        else
        {
          lMin = mTrueMin + (mTrueMax - mTrueMin) * mMin.get();
          lMax = mTrueMin + (mTrueMax - mTrueMin) * mMax.get();
        }

        if (mClearCLImage.getDimension() == 2)
        {
          if (mClearCLImage instanceof ClearCLImage)
            mRenderKernel = mProgram.getKernel("image_render_2df");
          else if (mClearCLImage instanceof ClearCLBuffer)
            mRenderKernel = mProgram.getKernel("buffer_render_2df");

        }
        else if (mClearCLImage.getDimension() == 3)
        {
          switch (getRenderModeProperty().get())
          {
          case AvgProjection:
            if (mClearCLImage instanceof ClearCLImage)
              mRenderKernel =
                            mProgram.getKernel("image_render_avgproj_3df");
            else if (mClearCLImage instanceof ClearCLBuffer)
              mRenderKernel =
                            mProgram.getKernel("buffer_render_avgproj_3df");
            break;
          case ColorProjection:
            if (mClearCLImage instanceof ClearCLImage)
              mRenderKernel =
                            mProgram.getKernel("image_render_colorproj_3df");
            else if (mClearCLImage instanceof ClearCLBuffer)
              mRenderKernel =
                            mProgram.getKernel("buffer_render_colorproj_3df");
            break;
          case MaxProjection:
            if (mClearCLImage instanceof ClearCLImage)
              mRenderKernel =
                            mProgram.getKernel("image_render_maxproj_3df");
            else if (mClearCLImage instanceof ClearCLBuffer)
              mRenderKernel =
                            mProgram.getKernel("buffer_render_maxproj_3df");
            break;
          default:
          case Slice:
            if (mClearCLImage instanceof ClearCLImage)
              mRenderKernel =
                            mProgram.getKernel("image_render_slice_3df");
            else if (mClearCLImage instanceof ClearCLBuffer)
              mRenderKernel =
                            mProgram.getKernel("buffer_render_slice_3df");
            break;
          }

        }

        mRenderKernel.setGlobalSizes(Region2.region(mClearCLImage.getDimensions()));

        mRenderKernel.setArgument("image", mClearCLImage);
        mRenderKernel.setArgument("rgbbuffer", mRenderRGBBuffer);

        mRenderKernel.setArgument("vmin", lMin);
        mRenderKernel.setArgument("vmax", lMax);
        mRenderKernel.setArgument("gamma", mGamma.get());
        mRenderKernel.setOptionalArgument("z", mZ.get());

        final int lZStep = (int) (1.0 * mClearCLImage.getDepth()
                                  / mNumberOfSteps.get());
        mRenderKernel.setOptionalArgument("zstep", lZStep);

        mRenderKernel.run(true);

        ElapsedTime.measure("mRenderRGBBuffer.copyTo(mClearCLHostImage, true);",
                            () -> mRenderRGBBuffer.copyTo(mClearCLHostImage,
                                                          true));

        long lWidth = mClearCLHostImage.getWidth();
        long lHeight = mClearCLHostImage.getHeight();

        ContiguousMemoryInterface lContiguousMemory =
                                                    mClearCLHostImage.getContiguousMemory();

        if (mPixelArray == null
            || mPixelArray.length != lContiguousMemory.getSizeInBytes())
        {
          mPixelArray =
                      new byte[toIntExact(lContiguousMemory.getSizeInBytes())];
        }

        ElapsedTime.measure("lContiguousMemory.copyTo(mPixelArray)",
                            () -> lContiguousMemory.copyTo(mPixelArray));

        Platform.runLater(() -> {

          PixelFormat<ByteBuffer> lPixelFormat =
                                               PixelFormat.getByteBgraInstance();
          PixelWriter pixelWriter =
                                  mGraphicsContext2D.getPixelWriter();

          pixelWriter.setPixels(0,
                                0,
                                (int) lWidth,
                                (int) lHeight,
                                lPixelFormat,
                                mPixelArray,
                                0,
                                (int) (lWidth * 4));

          mGraphicsContext2D.beginPath();
          mGraphicsContext2D.rect(0, 0, lWidth, lHeight);
          mGraphicsContext2D.setStroke(Color.RED);
          mGraphicsContext2D.stroke();/**/
          mGraphicsContext2D.closePath();
        });
      }
      finally
      {
        mLock.unlock();
      }
    }
  }

  /**
   * Returns auto property
   * @return auto property
   */
  public BooleanProperty getAutoProperty()
  {
    return mAuto;
  }

  /**
   * Returns min property
   * @return min property
   */
  public FloatProperty getMinProperty()
  {
    return mMin;
  }

  /**
   * Returns max property
   * @return max property
   */
  public FloatProperty getMaxProperty()
  {
    return mMax;
  }

  /**
   * Returns gamma property
   * @return gamma property
   */
  public FloatProperty getGammaProperty()
  {
    return mGamma;
  }

  /**
   * Returns z property
   * @return z property
   */
  public IntegerProperty getZProperty()
  {
    return mZ;
  }

  /**
   * Returns render mode property
   * @return render mode property
   */
  public ObjectProperty<RenderMode> getRenderModeProperty()
  {
    return mRenderMode;
  }
  

}
