package clearcl.benchmark.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.javacl.ClearCLBackendJavaCL;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import clearcl.benchmark.Benchmark;

public class BenchmarkDemo
{

  @Test
  public void demo() throws Exception
  {
    testWithBackend(new ClearCLBackendJOCL());
    testWithBackend(new ClearCLBackendJavaCL());
  }

  private void testWithBackend(ClearCLBackendInterface lClearCLBackendInterface)
  {
    try (ClearCL lClearCL = new ClearCL(lClearCLBackendInterface))
    {
      ArrayList<ClearCLDevice> lAllDevices = lClearCL.getAllDevices();

      Collections.shuffle(lAllDevices, new Random(System.nanoTime()));

      ClearCLDevice lFastestDevice = Benchmark.getFastestDevice(lAllDevices);

      System.out.println("Fastest device: "+lFastestDevice);
    }
  }

}
