package clearcl.test;

import java.io.File;

import clearcl.*;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.enums.*;
import clearcl.io.TiffWriter;
import coremem.enums.NativeTypeEnum;

import org.junit.Test;

/**
 * Test for File input/output
 *
 * @author haesleinhuepf
 */
public class ClearCLIOTests
{
  @Test
  public void testTiffWriter() throws Throwable
  {
    ClearCLBackendInterface lClearCLBackend =
                                            ClearCLBackends.getBestBackend();

    ClearCL lClearCL = new ClearCL(lClearCLBackend);

    ClearCLDevice lBestGPUDevice = lClearCL.getBestGPUDevice();

    System.out.println(lBestGPUDevice.getInfoString());

    ClearCLContext lContext = lBestGPUDevice.createContext();

    ClearCLProgram lProgram =
                            lContext.createProgram(ClearCLBasicTests.class,
                                                   "test.cl");

    lProgram.addDefine("CONSTANT", "1");

    BuildStatus lBuildStatus = lProgram.buildAndLog();

    ClearCLImage lImageSrc =
                           lContext.createImage(HostAccessType.WriteOnly,
                                                KernelAccessType.ReadWrite,
                                                ImageChannelOrder.Intensity,
                                                ImageChannelDataType.Float,
                                                100,
                                                100,
                                                100);

    ClearCLKernel lKernel = lProgram.createKernel("fillimagexor");

    lKernel.setArgument("image", lImageSrc);
    lKernel.setArgument("u", 1f);
    lKernel.setGlobalSizes(100, 100, 100);
    lKernel.run();

    ClearCLImage lImageDst =
                           lContext.createImage(HostAccessType.ReadOnly,
                                                KernelAccessType.WriteOnly,
                                                ImageChannelOrder.Intensity,
                                                ImageChannelDataType.Float,
                                                100,
                                                100,
                                                100);

    lImageSrc.copyTo(lImageDst, new long[]
    { 0, 0, 0 }, new long[]
    { 0, 0, 0 }, new long[]
    { 100, 100, 100 }, true);

    TiffWriter lTiffWriter = new TiffWriter(NativeTypeEnum.Byte,
                                            1f,
                                            0f);
    File lFile8 = new File("out/temp/test8.tif");
    File lFile16 = new File("out/temp/test16.tif");
    File lFile32 = new File("out/temp/test32.tif");
    if (lFile8.exists())
    {
      lFile8.delete();
    }
    if (lFile16.exists())
    {
      lFile16.delete();
    }
    if (lFile32.exists())
    {
      lFile32.delete();
    }

    lTiffWriter.setBytesPerPixel(8);
    lTiffWriter.write(lImageDst, lFile8);
    lTiffWriter.setBytesPerPixel(16);
    lTiffWriter.write(lImageDst, lFile16);
    lTiffWriter.setBytesPerPixel(32);
    lTiffWriter.write(lImageDst, lFile32);

  }
}
