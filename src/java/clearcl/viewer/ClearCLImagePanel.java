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
import clearcl.enums.BuildStatus;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;
import clearcl.enums.MemAllocMode;
import clearcl.exceptions.ClearCLUnsupportedException;
import clearcl.ocllib.OCLlib;
import clearcl.ops.Reductions;
import clearcl.util.Region2;
import clearcl.util.ElapsedTime;
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
  private static final float cSmoothingFactor = 0.99f;

  private Canvas mCanvas;
  private GraphicsContext mGraphicsContext2D;
  private ClearCLImage mClearCLImage;
  private ClearCLBuffer mRenderRGBBuffer;
  private ClearCLHostImageBuffer mClearCLHostImage;
  private byte[] mPixelArray;
  private ClearCLProgram mProgram;
  private ClearCLKernel mRenderKernel;
  private Reductions mReductions;

  private ReentrantLock mLock = new ReentrantLock();

  private final BooleanProperty mAuto = new SimpleBooleanProperty(true);
  private final FloatProperty mMin = new SimpleFloatProperty(0);
  private final FloatProperty mMax = new SimpleFloatProperty(1);
  private final FloatProperty mGamma = new SimpleFloatProperty(1);
  private final IntegerProperty mZ = new SimpleIntegerProperty(0);
  private final IntegerProperty mNumberOfSteps = new SimpleIntegerProperty(128);

  private final ObjectProperty<RenderMode> mRenderMode = new SimpleObjectProperty<>(RenderMode.ColorProjection);


  private Float mTrueMin = 0f, mTrueMax = 1f;

  /**
   * Creates a panel for a given ClearCL image.
   * 
   * @param pClearCLImage
   */
  public ClearCLImagePanel(ClearCLImage pClearCLImage)
  {
    super();
    mClearCLImage = pClearCLImage;

    ClearCLContext lContext = pClearCLImage.getContext();

    if (pClearCLImage.getImageType() == ImageType.IMAGE1D)
    {
      throw new ClearCLUnsupportedException("1D image visualizationnot supported");
    }

    mRenderRGBBuffer = lContext.createBuffer(MemAllocMode.AllocateHostPointer,
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

      BuildStatus lBuildStatus = mProgram.buildAndLog();

      if (pClearCLImage.getImageType() == ImageType.IMAGE1D)
      {
        throw new ClearCLUnsupportedException("1D image visualizationnot supported");
      }

      mReductions = new Reductions(lContext.getDefaultQueue());

    }
    catch (IOException e)
    {
      e.printStackTrace();
      throw new RuntimeException("Cannot build program", e);
    }

    mClearCLHostImage = ClearCLHostImageBuffer.allocateSameAs(mRenderRGBBuffer);

    backgroundProperty().set(new Background(new BackgroundFill(Color.BLACK,
                                                               CornerRadii.EMPTY,
                                                               Insets.EMPTY)));/**/

    mCanvas = new Canvas(pClearCLImage.getWidth(),
                         pClearCLImage.getHeight());
    mCanvas.setCache(false);
    // mCanvas.setCacheHint(CacheHint.SPEED);
    mGraphicsContext2D = mCanvas.getGraphicsContext2D();

    getChildren().add(mCanvas);
    StackPane.setAlignment(mCanvas, Pos.CENTER);

    mGraphicsContext2D = mCanvas.getGraphicsContext2D();

    pClearCLImage.addListener((q, s) -> {

      ElapsedTime.measure("q.waitToFinish();", () -> q.waitToFinish());

      updateImage();
    });

    mMin.addListener((e) -> {
      updateImage();
    });
    mMax.addListener((e) -> {
      updateImage();
    });
    mGamma.addListener((e) -> {
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

    boolean lTryLock = false;
    try
    {
      lTryLock = true;
      mLock.tryLock(1, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e)
    {
    }

    if (lTryLock)
    {
      try
      {
        float lMin = 0;
        float lMax = 1;

        if (mAuto.get() || mTrueMin == null)
        {
          float[] lMinMax = mReductions.minmax(mClearCLImage, 32);
          mTrueMin = lMinMax[0];
          mTrueMax = lMinMax[1];

          // System.out.format("min=%f, max=%f \n", mTrueMin, mTrueMax);

          lMin = mTrueMin;
          lMax = mTrueMax;
        }
        else
        {
          lMin = mTrueMin + (mTrueMax - mTrueMin) * mMin.get();
          lMax = mTrueMin + (mTrueMax - mTrueMin) * mMax.get();
        }

        if (mClearCLImage.getImageType() == ImageType.IMAGE2D)
        {
          mRenderKernel = mProgram.getKernel("image_render_2df");
        }
        else if (mClearCLImage.getImageType() == ImageType.IMAGE3D)
        {
          switch (getRenderModeProperty().get())
          {
          case AvgProjection:
            mRenderKernel = mProgram.getKernel("image_render_avgproj_3df");
            break;
          case ColorProjection:
            mRenderKernel = mProgram.getKernel("image_render_colorproj_3df");
            break;
          case MaxProjection:
            mRenderKernel = mProgram.getKernel("image_render_maxproj_3df");
            break;
          default:
          case Slice:
            mRenderKernel = mProgram.getKernel("image_render_slice_3df");
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
        mRenderKernel.setOptionalArgument("zstep",
                                          (int) (mClearCLImage.getDepth() / mNumberOfSteps.get()));
        mRenderKernel.run(true);

        ElapsedTime.measure("mRenderRGBBuffer.copyTo(mClearCLHostImage, true);",
                        () -> mRenderRGBBuffer.copyTo(mClearCLHostImage,
                                                      true));

        long lWidth = mClearCLHostImage.getWidth();
        long lHeight = mClearCLHostImage.getHeight();

        ContiguousMemoryInterface lContiguousMemory = mClearCLHostImage.getContiguousMemory();

        if (mPixelArray == null || mPixelArray.length != lContiguousMemory.getSizeInBytes())
        {
          mPixelArray = new byte[toIntExact(lContiguousMemory.getSizeInBytes())];
        }

        ElapsedTime.measure("lContiguousMemory.copyTo(mPixelArray)",
                        () -> lContiguousMemory.copyTo(mPixelArray));


        
        Platform.runLater(() -> {

          PixelFormat<ByteBuffer> lPixelFormat = PixelFormat.getByteBgraInstance();
          PixelWriter pixelWriter = mGraphicsContext2D.getPixelWriter();

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

  public BooleanProperty getAutoProperty()
  {
    return mAuto;
  }

  public FloatProperty getMinProperty()
  {
    return mMin;
  }

  public FloatProperty getMaxProperty()
  {
    return mMax;
  }

  public FloatProperty getGammaProperty()
  {
    return mGamma;
  }

  public IntegerProperty getZProperty()
  {
    return mZ;
  }

  public ObjectProperty<RenderMode> getRenderModeProperty()
  {
    return mRenderMode;
  }

}
