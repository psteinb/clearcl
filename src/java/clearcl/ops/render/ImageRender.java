package clearcl.ops.render;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.vecmath.Matrix4f;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.ClearCLQueue;
import clearcl.enums.HostAccessType;
import clearcl.enums.KernelAccessType;
import clearcl.enums.MemAllocMode;
import clearcl.ocllib.OCLlib;
import clearcl.ops.OpsBase;
import clearcl.ops.render.enums.Algorithm;
import clearcl.ops.render.enums.Parameter;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;

/**
 * Fractional Brownian Noise generation.
 *
 * @author royer
 */
public class ImageRender extends OpsBase
{

  private ClearCLKernel mRenderKernel;

  private final ConcurrentHashMap<Parameter, Float> mFloatParameters =
                                                                     new ConcurrentHashMap<Parameter, Float>();
  private final ConcurrentHashMap<Parameter, Integer> mIntegerParameters =
                                                                         new ConcurrentHashMap<Parameter, Integer>();
  private final ConcurrentHashMap<Parameter, Matrix4f> mMatrixParameters =
                                                                         new ConcurrentHashMap<Parameter, Matrix4f>();
  private final ConcurrentHashMap<Parameter, ClearCLBuffer> mMatrixBufferParameters =
                                                                                    new ConcurrentHashMap<Parameter, ClearCLBuffer>();

  /**
   * Instanciates a volume renderer given a queue
   * 
   * @param pClearCLQueue
   *          queue
   * @param pVolumeRenderAlgorithm
   *          type of volume rendering algorithm
   * @throws IOException
   *           thrown if kernels cannot be read
   */
  public ImageRender(ClearCLQueue pClearCLQueue,
                     Algorithm pVolumeRenderAlgorithm)
  {
    super(pClearCLQueue);

    setDefaultParameters();

    try
    {
      String lKernelPath = pVolumeRenderAlgorithm.getKernelPath();
      String lKernelName = pVolumeRenderAlgorithm.getKernelName();

      ClearCLProgram lNoiseProgram =
                                   getContext().createProgram(OCLlib.class,
                                                              lKernelPath);
      // lNoiseProgram.addBuildOptionAllMathOpt();
      lNoiseProgram.buildAndLog();
      System.out.println(lNoiseProgram.getSourceCode());

      mRenderKernel = lNoiseProgram.createKernel(lKernelName);
    }
    catch (Throwable e)
    {
      throw new RuntimeException(e);
    }
  }

  private void setDefaultParameters()
  {
    setFloatParameter(Parameter.Min, 0);
    setFloatParameter(Parameter.Max, 1);
    setFloatParameter(Parameter.Gamma, 1);
    setFloatParameter(Parameter.Alpha, 0.2f);
    setIntegerParameter(Parameter.MaxSteps, 64);

    Matrix4f lIdentityMatrix = new Matrix4f();
    lIdentityMatrix.setIdentity();

    setMatrixParameter(Parameter.ProjectionMatrix, lIdentityMatrix);
    setMatrixParameter(Parameter.ModelViewMatrix, lIdentityMatrix);
  }

  private void setArguments(ClearCLKernel pNoiseKernel,
                            ClearCLBuffer pBuffer)
  {
    pNoiseKernel.setArgument("output", pBuffer);

  }

  public void setFloatParameter(Parameter pParameter, float pValue)
  {
    mFloatParameters.put(pParameter, pValue);
  }

  public void setIntegerParameter(Parameter pParameter, int pValue)
  {
    mIntegerParameters.put(pParameter, pValue);
  }

  public void setMatrixParameter(Parameter pParameter,
                                 Matrix4f pMatrix)
  {
    mMatrixParameters.put(pParameter, pMatrix);
  }

  private ClearCLBuffer getMatrixBuffer(Parameter pParameter)
  {
    ClearCLBuffer lBuffer = mMatrixBufferParameters.get(pParameter);
    if (lBuffer == null)
    {
      Matrix4f lMatrix = mMatrixParameters.get(pParameter);
      if (lMatrix == null)
        return null;

      lBuffer = matrixToClearCLBuffer(lMatrix);
      mMatrixBufferParameters.put(pParameter, lBuffer);
    }

    return lBuffer;
  }

  private ClearCLBuffer matrixToClearCLBuffer(Matrix4f pMatrix)
  {
    ClearCLContext lContext = getContext();

    ClearCLBuffer lClearCLBuffer =
                                 lContext.createBuffer(MemAllocMode.Best,
                                                       HostAccessType.WriteOnly,
                                                       KernelAccessType.ReadOnly,
                                                       1,
                                                       NativeTypeEnum.Float,
                                                       16);
    float[] lMatrixToArray = matrixToArray(pMatrix);

    OffHeapMemory lBuffer = OffHeapMemory.allocateFloats(16);
    lBuffer.copyFrom(lMatrixToArray);

    lClearCLBuffer.readFrom(lBuffer, true);

    return lClearCLBuffer;
  }

  private float[] matrixToArray(Matrix4f pMatrix)
  {
    float[] lArray = new float[16];
    int k = 0;
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        lArray[k++] = pMatrix.getElement(i, j);

    return lArray;
  }

  public void render(ClearCLImage p3DImage,
                     ClearCLBuffer pRGBABuffer,
                     boolean waitToFinish)
  {
    mRenderKernel.setArgument("image", p3DImage);
    mRenderKernel.setArgument("rgbabuffer", pRGBABuffer);

    for (Parameter lParameter : Parameter.values())
    {
      String lKernelArgumentName = lParameter.getKernelArgumentName();
      Float lFloat = mFloatParameters.get(lParameter);
      if (lFloat != null)
        mRenderKernel.setOptionalArgument(lKernelArgumentName,
                                          lFloat);

      Integer lInteger = mIntegerParameters.get(lParameter);
      if (lInteger != null)
        mRenderKernel.setOptionalArgument(lKernelArgumentName,
                                          lInteger);
      ClearCLBuffer lMatrixBuffer = getMatrixBuffer(lParameter);
      if (lMatrixBuffer != null)
        mRenderKernel.setOptionalArgument(lKernelArgumentName,
                                          lMatrixBuffer);
    }

    mRenderKernel.setGlobalSizes(pRGBABuffer);
    mRenderKernel.run(waitToFinish);

  }

}
