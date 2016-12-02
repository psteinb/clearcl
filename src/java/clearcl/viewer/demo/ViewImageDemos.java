package clearcl.viewer.demo;

import java.io.IOException;
import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.ocllib.OCLlib;
import clearcl.test.ClearCLBasicTests;
import clearcl.viewer.ClearCLImageViewer;

public class ViewImageDemos
{

  @Test
  public void demoViewImage2D() throws InterruptedException,
                               IOException
  {

    ClearCLBackendInterface lClearCLBackendInterface = ClearCLBackends.getBestBackend();
    try (ClearCL lClearCL = new ClearCL(lClearCLBackendInterface))
    {
      ClearCLDevice lFastestGPUDevice = lClearCL.getFastestGPUDevice();

      System.out.println(lFastestGPUDevice);

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      ClearCLProgram lProgram = lContext.createProgram(ClearCLBasicTests.class,
                                                       "test.cl");
      lProgram.addDefine("CONSTANT", "1");
      lProgram.build();

      ClearCLImage lImage = lContext.createImage(ImageChannelOrder.Intensity,
                                                 ImageChannelDataType.Float,
                                                 512,
                                                 512);

      ClearCLKernel lKernel = lProgram.createKernel("fillimagexor");
      lKernel.setArgument("image", lImage);
      lKernel.setGlobalSizes(lImage);
      lKernel.run(true);

      ClearCLImageViewer lViewImage = ClearCLImageViewer.view(lImage);

      for (int i = 1000; i > 1 && lViewImage.isShowing(); i--)
      {
        
        if (i % 1000 == 0)
          System.out.println("i=" + i);
        lKernel.setArgument("u", 100.0f / i);
        lKernel.setArgument("dx", i);

        lKernel.run(true);
        lImage.notifyListenersOfChange(lContext.getDefaultQueue());
        Thread.sleep(10);
      }

      lViewImage.waitWhileShowing();
    }

  }
  
  @Test
  public void demoViewImage3D() throws InterruptedException,
                               IOException
  {

    ClearCLBackendInterface lClearCLBackendInterface = ClearCLBackends.getBestBackend();
    try (ClearCL lClearCL = new ClearCL(lClearCLBackendInterface))
    {
      ClearCLDevice lFastestGPUDevice = lClearCL.getFastestGPUDevice();

      System.out.println(lFastestGPUDevice);

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      ClearCLProgram lProgram = lContext.createProgram(OCLlib.class,
                                                       "phantoms/phantoms.cl");
      lProgram.buildAndLog();
      
      int lSize = 213;

      ClearCLImage lImage = lContext.createImage(ImageChannelOrder.R,
                                                 ImageChannelDataType.UnsignedInt8,
                                                 lSize,
                                                 lSize,
                                                 lSize);

      ClearCLKernel lKernel = lProgram.createKernel("sphere");
      lKernel.setArgument("image", lImage);
      lKernel.setGlobalSizes(lImage);
      
      lKernel.setOptionalArgument("r", 0.25f);
      lKernel.setOptionalArgument("cx", lSize/2);
      lKernel.setOptionalArgument("cy", lSize/2);
      lKernel.setOptionalArgument("cz", lSize/2);
      
      lKernel.setOptionalArgument("a", 1);
      lKernel.setOptionalArgument("b", 1);
      lKernel.setOptionalArgument("c", 1);
      lKernel.setOptionalArgument("d", 1);
      
      lKernel.run(true);
      lImage.notifyListenersOfChange(lContext.getDefaultQueue());

      ClearCLImageViewer lViewImage = ClearCLImageViewer.view(lImage);
      
      for (int i = 0; i < 10000 && lViewImage.isShowing(); i++)
      {
        int x= ((64+(i))  %lSize);
        int y= ((64+(int)(i*1.2)) %lSize);
        int z= ((64+(int)(i*1.3))%lSize);
        
        //System.out.format("x=%d, y=%d, z=%d \n",x,y,z);
        
        if (i % 1000 == 0)
          System.out.println("i=" + i);
        lKernel.setOptionalArgument("r", 0.25f);
        lKernel.setOptionalArgument("cx", x);
        lKernel.setOptionalArgument("cy", y);
        lKernel.setOptionalArgument("cz", z);
        
        lKernel.setOptionalArgument("a", 1);
        lKernel.setOptionalArgument("b", 1);
        lKernel.setOptionalArgument("c", 1);
        lKernel.setOptionalArgument("d", 1);

        lKernel.run(true);
        lImage.notifyListenersOfChange(lContext.getDefaultQueue());
        Thread.sleep(10);
      }

      lViewImage.waitWhileShowing();
    }

  }


}
