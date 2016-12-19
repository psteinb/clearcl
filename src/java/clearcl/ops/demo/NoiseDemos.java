package clearcl.ops.demo;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.ClearCLQueue;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.KernelAccessType;
import clearcl.enums.MemAllocMode;
import clearcl.ops.Noise;
import clearcl.test.ClearCLBasicTests;
import clearcl.viewer.ClearCLImageViewer;
import coremem.enums.NativeTypeEnum;

public class NoiseDemos
{

  @Test
  public void test() throws InterruptedException, IOException
  {

    ClearCLBackendInterface lClearCLBackendInterface =
                                                     ClearCLBackends.getBestBackend();

    try (ClearCL lClearCL = new ClearCL(lClearCLBackendInterface))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDeviceForImages();

      System.out.println(lFastestGPUDevice);

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      Noise lNoise = new Noise(lContext.getDefaultQueue());

      ClearCLBuffer lBuffer = lContext.createBuffer(MemAllocMode.Best,
                                                    HostAccessType.ReadOnly,
                                                    KernelAccessType.WriteOnly,
                                                    1,
                                                    NativeTypeEnum.Float,
                                                    128,
                                                    128);

      ClearCLImageViewer lViewImage =
                                    ClearCLImageViewer.view(lBuffer);

      for (int i = 0; i < 1000000; i++)
      {
        lNoise.setSeed(i);
        lNoise.perlin2D(lBuffer, true);
      }

      lViewImage.waitWhileShowing();
    }

  }

}
