package clearcl.backend.javacl.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clearcl.backend.javacl.ClearCLBackendJavaCL;

public class ClearCLBackendJavaCLTests
{

  @Test
  public void test()
  {
    ClearCLBackendJavaCL lClearCLBackendJavaCL = new ClearCLBackendJavaCL();

    System.out.println(lClearCLBackendJavaCL.getNumberOfPlatforms());
    assertTrue(lClearCLBackendJavaCL.getNumberOfPlatforms() > 0);

  }

}
