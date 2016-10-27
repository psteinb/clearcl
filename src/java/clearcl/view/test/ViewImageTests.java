package clearcl.view.test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;
import clearcl.test.ClearCLBasicTests;
import clearcl.util.Region3;
import clearcl.view.ClearCLImageView;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ViewImageTests
{

  @Test
  public void testViewImage2D() throws InterruptedException,
                               IOException
  {

    ClearCLBackendInterface lClearCLBackendInterface = new ClearCLBackendJOCL();
    try (ClearCL lClearCL = new ClearCL(lClearCLBackendInterface))
    {
      ClearCLDevice lFastestGPUDevice = lClearCL.getFastestGPUDevice();

      System.out.println(lFastestGPUDevice);

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      ClearCLProgram lProgram = lContext.createProgram(ClearCLBasicTests.class,
                                                       "test.cl");
      lProgram.addDefine("CONSTANT", "1");
      lProgram.build();

      ClearCLImage lImage = lContext.createImage(ImageType.IMAGE2D,
                                                 ImageChannelOrder.Intensity,
                                                 ImageChannelDataType.Float,
                                                 100,
                                                 100);

      ClearCLKernel lKernel = lProgram.createKernel("fillimagexor");
      lKernel.setArgument("image", lImage);
      lKernel.setGlobalSizesFor(lImage);
      lKernel.run(true);

      ClearCLImageView lViewImage = ClearCLImageView.view(lImage);

      for (int i = 10000; i > 1; i--)
      {
        if (i % 1000 == 0)
          System.out.println("i=" + i);
        lKernel.setArgument("u", 100.0f / i);
        lKernel.setArgument("dx", i);

        lKernel.run(true);
        Thread.sleep(10);
      }

      lViewImage.waitWhileShowing();
    }

  }

  // Stage stage = null;
  //
  // @Test
  // public void testJavaFXWindow() throws InterruptedException
  // {
  // new JFXPanel();
  // final CountDownLatch latch = new CountDownLatch(1);
  // Platform.runLater(() -> {
  // stage = new Stage();
  // stage.setTitle("My New Stage Title");
  // stage.setScene(new Scene(new Button("TEST"), 450, 450));
  // stage.show();
  // });
  // latch.await();
  //
  // while (stage.isShowing())
  // {
  // Thread.sleep(100);
  // }
  //
  // }

}
