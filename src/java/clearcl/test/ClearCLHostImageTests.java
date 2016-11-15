package clearcl.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.ClearCLHostImageBuffer;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import coremem.enums.NativeTypeEnum;

public class ClearCLHostImageTests
{

  @Test
  public void test()
  {
    ClearCLBackendJOCL lClearCLJOCLBackend = new ClearCLBackendJOCL();

    try (ClearCL lClearCL = new ClearCL(lClearCLJOCLBackend))
    {

      ClearCLDevice lBestGPUDevice = lClearCL.getBestGPUDevice();

      ClearCLHostImageBuffer lHostImage = new ClearCLHostImageBuffer(lBestGPUDevice.createContext(),
                                                         NativeTypeEnum.UnsignedShort,
                                                         10,
                                                         10,
                                                         10);

      assertEquals(10 * 10 * 10 * 2, lHostImage.getSizeInBytes());
      
      lHostImage.close();
    }
  }

}
