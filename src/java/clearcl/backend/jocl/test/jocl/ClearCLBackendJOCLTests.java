package clearcl.backend.jocl.test.jocl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clearcl.backend.jocl.ClearCLBackendJOCL;

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
  public void test()
  {
    ClearCLBackendJOCL lClearCLBackendJOCL = new ClearCLBackendJOCL();

    assertTrue(lClearCLBackendJOCL.getNumberOfPlatforms() > 0);    
  }

}
