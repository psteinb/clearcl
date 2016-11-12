package clearcl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLPlatform;
import clearcl.ClearCLProgram;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.javacl.ClearCLBackendJavaCL;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import clearcl.enums.BuildStatus;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;
import clearcl.exceptions.OpenCLException;
import coremem.offheap.OffHeapMemory;
import coremem.types.NativeTypeEnum;

public class ClearCLBasicTests
{

  private static final int cFloatArrayLength = 1024 * 1024;

  @Test
  public void testBackendJOCL() throws Exception
  {
    ClearCLBackendJOCL lClearCLJOCLBackend = new ClearCLBackendJOCL();

    testWithBackend(lClearCLJOCLBackend);

  }

  @Test
  public void testBackenJavaCL() throws Exception
  {
    ClearCLBackendJavaCL lClearCLBackendJavaCL = new ClearCLBackendJavaCL();

    testWithBackend(lClearCLBackendJavaCL);

  }

  private void testWithBackend(ClearCLBackendInterface pClearCLBackendInterface) throws Exception
  {
    try (ClearCL lClearCL = new ClearCL(pClearCLBackendInterface))
    {

      int lNumberOfPlatforms = lClearCL.getNumberOfPlatforms();

      System.out.println("lNumberOfPlatforms=" + lNumberOfPlatforms);

      for (int p = 0; p < lNumberOfPlatforms; p++)
      {
        ClearCLPlatform lPlatform = lClearCL.getPlatform(p);

        System.out.println(lPlatform.getInfoString());

        for (int d = 0; d < lPlatform.getNumberOfDevices(); d++)
        {
          ClearCLDevice lClearClDevice = lPlatform.getDevice(d);

          System.out.println("\t" + d
                             + " -> \n"
                             + lClearClDevice.getInfoString());

          ClearCLContext lContext = lClearClDevice.createContext();

          ClearCLProgram lProgram = lContext.createProgram(this.getClass(),
                                                           "test.cl");
          lProgram.addDefine("CONSTANT", "1");

          System.out.println(lProgram.getSourceCode());

          BuildStatus lBuildStatus = lProgram.build();

          System.out.println(lProgram.getBuildLog());
          System.out.println(lBuildStatus);
          assertEquals(lBuildStatus, BuildStatus.Success);
          // assertTrue(lProgram.getBuildLog().isEmpty());

          testBuffers(lContext, lProgram);

          testImages(lContext, lProgram);

        }

      }
    }
  }

  private void testImages(ClearCLContext lContext,
                          ClearCLProgram pProgram)
  {

    ClearCLImage lImageSrc = lContext.createImage(HostAccessType.WriteOnly,
                                                  KernelAccessType.ReadWrite,
                                                  ImageChannelOrder.Intensity,
                                                  ImageChannelDataType.Float,
                                                  100,
                                                  100,
                                                  100);

    ClearCLKernel lKernel = pProgram.createKernel("fillimagexor");

    lKernel.setArgument("image",lImageSrc);
    lKernel.setArgument("u", 1f);
    lKernel.setGlobalSizes(100, 100, 100);
    lKernel.run();

    ClearCLImage lImageDst = lContext.createImage(HostAccessType.ReadOnly,
                                                  KernelAccessType.WriteOnly,
                                                  ImageChannelOrder.Intensity,
                                                  ImageChannelDataType.Float,
                                                  10,
                                                  10,
                                                  10);

    lImageSrc.copyTo(lImageDst, new long[]
    { 10, 20, 30 }, new long[]
    { 0, 0, 0 }, new long[]
    { 10, 10, 10 }, true);

    OffHeapMemory lBuffer = OffHeapMemory.allocateBytes(lImageDst.getSizeInBytes());
    lImageDst.writeTo(lBuffer, new long[]
    { 0, 0, 0 }, new long[]
    { 10, 10, 10 }, true);

    // for(int i=0; i<lBuffer.getSizeInBytes()/4; i++)
    // System.out.println(lBuffer.getFloatAligned(i));

    assertEquals((10 + 1) ^ (20 + 2 + 1) ^ (30 + 3 + 2),
                 lBuffer.getFloatAligned(1 + 2 * 10 + 3 * 10 * 10),
                 0.1);

    

  }

  private void testBuffers(ClearCLContext lCreateContext,
                           ClearCLProgram pProgram) throws IOException
  {

    try
    {
      @SuppressWarnings("unused")
      ClearCLBuffer lBufferTooBig = lCreateContext.createBuffer(HostAccessType.WriteOnly,
                                                                KernelAccessType.ReadOnly,
                                                                NativeTypeEnum.Float,
                                                                Long.MAX_VALUE);
      fail();
    }
    catch (OpenCLException e)
    {
      System.out.println("ERROR:" + e.getMessage());
      assertTrue(e.getErrorCode() == -61 || e.getErrorCode() == -6);
    }

    float[] lArrayA = new float[cFloatArrayLength];
    float[] lArrayB = new float[cFloatArrayLength];

    for (int j = 0; j < cFloatArrayLength; j++)
    {
      lArrayA[j] = j;
      lArrayB[j] = 1.5f * j;
    }

    ClearCLBuffer lBufferA = lCreateContext.createBuffer(HostAccessType.WriteOnly,
                                                         KernelAccessType.ReadOnly,
                                                         NativeTypeEnum.Float,
                                                         cFloatArrayLength);

    ClearCLBuffer lBufferB = lCreateContext.createBuffer(HostAccessType.WriteOnly,
                                                         KernelAccessType.ReadOnly,
                                                         NativeTypeEnum.Float,
                                                         cFloatArrayLength);

    ClearCLBuffer lBufferC = lCreateContext.createBuffer(HostAccessType.ReadOnly,
                                                         KernelAccessType.WriteOnly,
                                                         NativeTypeEnum.Float,
                                                         cFloatArrayLength);

    lBufferA.readFrom(FloatBuffer.wrap(lArrayA),
                      0L,
                      cFloatArrayLength,
                      true);
    lBufferB.readFrom(FloatBuffer.wrap(lArrayB),
                      0L,
                      cFloatArrayLength,
                      true);

    ClearCLKernel lKernel = pProgram.createKernel("buffersum");

    lKernel.setArguments(11f, lBufferA, lBufferB, lBufferC);

    lKernel.setGlobalSizes(cFloatArrayLength);
    lKernel.run();

    FloatBuffer lArrayC = ByteBuffer.allocateDirect(4 * cFloatArrayLength)
                                    .order(ByteOrder.nativeOrder())
                                    .asFloatBuffer();

    lBufferC.writeTo(lArrayC, 0, cFloatArrayLength, true);

    for (int j = 0; j < cFloatArrayLength; j++)
    {
      float lObservedValue = lArrayC.get(j);
      float lTrueValue = j + (1.5f * j) + 11;

      if (lObservedValue != lTrueValue)
      {
        System.out.format("NOT EQUAL: (c[%d] = %g) != %g \n",
                          j,
                          lObservedValue,
                          lTrueValue);
        assertTrue(false);
        break;
      }
      if (j % 100000 == 0)
        System.out.println(lObservedValue + " == " + lTrueValue);
    }
  }

}
