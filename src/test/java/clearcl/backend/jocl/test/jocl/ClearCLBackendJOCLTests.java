package clearcl.backend.jocl.test.jocl;

import static org.junit.Assert.assertTrue;
import ome.xml.model.ImageRef;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.ClearCLContext;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageType;

import org.junit.Test;

/**
 *
 *
 * @author royer
 */
public class ClearCLBackendJOCLTests
{

  /**
   * Basic test
   */
  @Test
  public void testCreation()
  {
    ClearCLBackendJOCL lClearCLBackendJOCL = new ClearCLBackendJOCL();

    assertTrue(lClearCLBackendJOCL.getNumberOfPlatforms() > 0);
  }

  /**
   * decipher image type
   */
  @Test
  public void testImageType()
  {

    ClearCL lClearCL = new ClearCL(new ClearCLBackendJOCL());
    for (ClearCLDevice device : lClearCL.getAllDevices())
    {
      final ClearCLContext lContext = device.createContext();
      final boolean isSupported = ClearCLBackendJOCL.isImageFormatSupported(
          lContext.getPeerPointer(),
          ImageType.IMAGE2D,
          ImageChannelOrder.R,
          ImageChannelDataType.UnsignedInt32);

      assertTrue(isSupported);
      lContext.close();
    }
  }

}
