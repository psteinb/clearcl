package clearcl.benchmark.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import clearcl.benchmark.Benchmark;

public class BenchmarkTests
{

  @Test
  public void test() throws Exception
  {
    // ClearCLBackendInterface lClearCLBackendInterface = new
    // ClearCLBackendJavaCL();
    ClearCLBackendInterface lClearCLBackendInterface = new ClearCLBackendJOCL();
    try (ClearCL lClearCL = new ClearCL(lClearCLBackendInterface))
    {
      ArrayList<ClearCLDevice> lAllDevices = lClearCL.getAllDevices();

      Collections.shuffle(lAllDevices, new Random(System.nanoTime()));

      ClearCLDevice lFastestDevice = Benchmark.getFastestDevice(lAllDevices);

      System.out.println(lFastestDevice);
    }
  }

}
