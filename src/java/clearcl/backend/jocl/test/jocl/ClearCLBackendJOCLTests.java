package clearcl.backend.jocl.test.jocl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clearcl.backend.jocl.ClearCLBackendJOCL;

public class ClearCLBackendJOCLTests
{

  @Test
  public void test()
  {
    ClearCLBackendJOCL lClearCLBackendJOCL = new ClearCLBackendJOCL();

    //System.out.println(lClearCLBackendJOCL.getNumberOfPlatforms());
    assertTrue(lClearCLBackendJOCL.getNumberOfPlatforms() > 0);

    
  }

}
