package clearcl.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.javacl.ClearCLBackendJavaCL;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import clearcl.selector.BadDeviceSelector;
import clearcl.selector.DeviceTypeSelector;
import clearcl.selector.FastestDeviceSelector;
import clearcl.selector.GlobalMemorySelector;

/**
 * Test 'best device' functionality
 *
 * @author royer
 */
public class ClearCLBestDeviceTests
{

  /**
   * Test with JOCL backend
   * 
   * @throws Exception
   *           NA
   */
  @Test
  public void testBackendJOCL() throws Exception
  {
    ClearCLBackendJOCL lClearCLJOCLBackend = new ClearCLBackendJOCL();

    testWithBackend(lClearCLJOCLBackend);

  }

  /**
   * Test with JavaCL backend
   * 
   * @throws Exception
   *           NA
   */
  @Test
  public void testBackenJavaCL() throws Exception
  {
    ClearCLBackendJavaCL lClearCLBackendJavaCL =
                                               new ClearCLBackendJavaCL();

    testWithBackend(lClearCLBackendJavaCL);

  }

  private void testWithBackend(ClearCLBackendInterface pClearCLBackendInterface) throws Exception
  {
    try (ClearCL lClearCL = new ClearCL(pClearCLBackendInterface))
    {

      {
        ClearCLDevice lClearClDevice =
                                     lClearCL.getBestDevice(DeviceTypeSelector.GPU,
                                                            BadDeviceSelector.NotIntegratedIntel,
                                                            GlobalMemorySelector.MAX);

        // System.out.println(lClearClDevice);
        assertTrue(lClearClDevice != null);
      }

      {
        ClearCLDevice lClearClDevice =
                                     lClearCL.getBestDevice(DeviceTypeSelector.GPU,
                                                            BadDeviceSelector.NotIntegratedIntel,
                                                            FastestDeviceSelector.FastestForImages);

        // System.out.println(lClearClDevice);
        assertTrue(lClearClDevice != null);
      }

      {
        ClearCLDevice lClearClDevice =
                                     lClearCL.getBestDevice(DeviceTypeSelector.GPU,
                                                            FastestDeviceSelector.FastestForImages);

        // System.out.println(lClearClDevice);
        assertTrue(lClearClDevice != null);
      }

      {
        ClearCLDevice lClearClDevice =
                                     lClearCL.getBestDevice(DeviceTypeSelector.GPU,
                                                            FastestDeviceSelector.FastestForBuffers);

        // System.out.println(lClearClDevice);
        assertTrue(lClearClDevice != null);
      }
    }
  }

}
