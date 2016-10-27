package clearcl.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import clearcl.ClearCLHostImage;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import coremem.types.NativeTypeEnum;

public class ClearCLHostImageTests
{

  @Test
  public void test()
  {
    ClearCLBackendJOCL lClearCLJOCLBackend = new ClearCLBackendJOCL();

    ClearCLHostImage lHostImage = new ClearCLHostImage(lClearCLJOCLBackend,
                                                       NativeTypeEnum.UnsignedShort,
                                                       10,
                                                       10,
                                                       10);

    assertEquals(10 * 10 * 10 * 2, lHostImage.getSizeInBytes());
  }

}
