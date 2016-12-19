package clearcl.ops;

import java.io.IOException;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLHostImageBuffer;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.ClearCLQueue;
import clearcl.enums.BuildStatus;
import clearcl.enums.HostAccessType;
import clearcl.enums.KernelAccessType;
import clearcl.interfaces.ClearCLImageInterface;
import clearcl.ocllib.OCLlib;
import coremem.buffers.ContiguousBuffer;
import coremem.enums.NativeTypeEnum;

public class MinMax extends OpsBase
{

  private ClearCLBuffer mScratchBuffer;
  private ClearCLHostImageBuffer mScratchHostBuffer;
  private ClearCLKernel mMinKernelBufferF, mMinKernelImage1F,
      mMinKernelImage2F, mMinKernelImage3F;

  public MinMax(ClearCLQueue pClearCLQueue) throws IOException
  {
    super(pClearCLQueue);

    ClearCLProgram lMinMaxProgram =
                                      getClearCLContext().createProgram(OCLlib.class,
                                                                        "reduction/reductions.cl");
    lMinMaxProgram.addBuildOptionAllMathOpt();
    lMinMaxProgram.buildAndLog();

    mMinKernelBufferF =
                      lMinMaxProgram.createKernel("reduce_min_buffer_f");
    mMinKernelImage1F =
                      lMinMaxProgram.createKernel("reduce_min_image_1df");
    mMinKernelImage2F =
                      lMinMaxProgram.createKernel("reduce_min_image_2df");
    mMinKernelImage3F =
                      lMinMaxProgram.createKernel("reduce_min_image_3df");
  }

  public float[] minmax(ClearCLImageInterface pClearCLImageInterface,
                        int pReduction)
  {
    if (pClearCLImageInterface instanceof ClearCLBuffer)
      return minmax((ClearCLBuffer) pClearCLImageInterface,
                    pReduction);
    else if (pClearCLImageInterface instanceof ClearCLImage)
      return minmax((ClearCLImage) pClearCLImageInterface,
                    pReduction);
    return null;
  }

  public float[] minmax(ClearCLBuffer pBuffer, int lReduction)
  {
    if (mScratchBuffer == null
        || mScratchBuffer.getLength() != 2 * lReduction)
    {
      mScratchBuffer =
                     getClearCLContext().createBuffer(HostAccessType.ReadOnly,
                                                      KernelAccessType.WriteOnly,
                                                      NativeTypeEnum.Float,
                                                      2 * lReduction);
      mScratchHostBuffer =
                         ClearCLHostImageBuffer.allocateSameAs(mScratchBuffer);
    }

    mMinKernelBufferF.setArgument("buffer", pBuffer);
    mMinKernelBufferF.setArgument("result", mScratchBuffer);
    mMinKernelBufferF.setArgument("length",
                                  pBuffer.getLength()
                                            * pBuffer.getNumberOfChannels());
    mMinKernelBufferF.setGlobalSizes(Math.min(pBuffer.getLength()
                                              * pBuffer.getNumberOfChannels(),
                                              lReduction));

    mMinKernelBufferF.run();
    mScratchBuffer.copyTo(mScratchHostBuffer, true);

    ContiguousBuffer lContiguousBuffer =
                                       ContiguousBuffer.wrap(mScratchHostBuffer.getContiguousMemory());

    float lMin = Float.POSITIVE_INFINITY;
    float lMax = Float.NEGATIVE_INFINITY;
    lContiguousBuffer.rewind();
    while (lContiguousBuffer.hasRemainingFloat())
    {
      float lMinValue = lContiguousBuffer.readFloat();
      lMin = Math.min(lMin, lMinValue);
      float lMaxValue = lContiguousBuffer.readFloat();
      lMax = Math.max(lMax, lMaxValue);
    }

    return new float[]
    { lMin, lMax };
  }

  public float[] minmax(ClearCLImage pImage, int lReduction)
  {

    ClearCLKernel lKernel = null;
    long[] lGlobalSizes = null;
    long lVolume = 1;

    if (pImage.getDimension() == 1)
    {
      lKernel = mMinKernelImage1F;
      lGlobalSizes = new long[]
      { lReduction };
      lVolume = lReduction;
    }
    else if (pImage.getDimension() == 2)
    {
      lKernel = mMinKernelImage2F;
      lGlobalSizes = new long[]
      { lReduction, lReduction };
      lVolume = lReduction * lReduction;
    }
    else if (pImage.getDimension() == 3)
    {
      lKernel = mMinKernelImage3F;
      lGlobalSizes = new long[]
      { lReduction, lReduction, lReduction };
      lVolume = lReduction * lReduction * lReduction;
    }

    if (mScratchBuffer == null
        || mScratchBuffer.getLength() != 2 * lVolume)
    {
      mScratchBuffer =
                     getClearCLContext().createBuffer(HostAccessType.ReadWrite,
                                                      KernelAccessType.WriteOnly,
                                                      NativeTypeEnum.Float,
                                                      2 * lVolume);
      mScratchHostBuffer =
                         ClearCLHostImageBuffer.allocateSameAs(mScratchBuffer);
    }

    lKernel.setArgument("image", pImage);
    lKernel.setArgument("result", mScratchBuffer);

    lKernel.setGlobalSizes(lGlobalSizes);

    lKernel.run();
    mScratchBuffer.copyTo(mScratchHostBuffer, true);

    ContiguousBuffer lContiguousBuffer =
                                       ContiguousBuffer.wrap(mScratchHostBuffer.getContiguousMemory());

    float lMin = Float.POSITIVE_INFINITY;
    float lMax = Float.NEGATIVE_INFINITY;
    lContiguousBuffer.rewind();
    while (lContiguousBuffer.hasRemainingFloat())
    {
      float lMinValue = lContiguousBuffer.readFloat();
      lMin = Math.min(lMin, lMinValue);
      float lMaxValue = lContiguousBuffer.readFloat();
      lMax = Math.max(lMax, lMaxValue);
      // System.out.format("min=%f, max=%f \n",lMin,lMax);
    }

    return new float[]
    { lMin, lMax };
  }
}
