package clearcl.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import clearcl.ClearCLHostImage;
import coremem.types.NativeTypeEnum;

public class ClearCLHostImageTests
{

  @Test
  public void test()
  {
    ClearCLHostImage lHostImage = new ClearCLHostImage(NativeTypeEnum.UnsignedShort,
                                                       10,
                                                       10,
                                                       10);

    assertEquals(10 * 10 * 10 * 2, lHostImage.getSizeInBytes());
  }

}
