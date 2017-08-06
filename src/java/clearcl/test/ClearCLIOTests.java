package clearcl.test;

import clearcl.*;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.enums.*;
import clearcl.io.TiffWriter;
import coremem.enums.NativeTypeEnum;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Test for File input/output
 *
 * @author haesleinhuepf
 */
public class ClearCLIOTests {
    @Test
    public void testTiffWriter() throws Throwable {
        ClearCLBackendInterface lClearCLBackend =
                ClearCLBackends.getBestBackend();

        ClearCL lClearCL = new ClearCL(lClearCLBackend);

        ClearCLDevice lBestGPUDevice = lClearCL.getBestGPUDevice();

        System.out.println(lBestGPUDevice.getInfoString());

        ClearCLContext lContext = lBestGPUDevice.createContext();

        ClearCLProgram lProgram =
                lContext.createProgram(ClearCLBasicTests.class,
                        "test.cl");

        lProgram.addDefine("CONSTANT", "1");

        BuildStatus lBuildStatus = lProgram.buildAndLog();

        ClearCLImage lImageSrc =
                lContext.createImage(HostAccessType.WriteOnly,
                        KernelAccessType.ReadWrite,
                        ImageChannelOrder.Intensity,
                        ImageChannelDataType.Float,
                        100,
                        100,
                        100);

        ClearCLKernel lKernel = lProgram.createKernel("fillimagexor");

        lKernel.setArgument("image", lImageSrc);
        lKernel.setArgument("u", 1f);
        lKernel.setGlobalSizes(100, 100, 100);
        lKernel.run();

        ClearCLImage lImageDst =
                lContext.createImage(HostAccessType.ReadOnly,
                        KernelAccessType.WriteOnly,
                        ImageChannelOrder.Intensity,
                        ImageChannelDataType.Float,
                        100,
                        100,
                        100);

        lImageSrc.copyTo(lImageDst, new long[]
                { 0, 0, 0 }, new long[]
                { 0, 0, 0 }, new long[]
                { 100, 100, 100 }, true);


        TiffWriter lTiffWriter = new TiffWriter(NativeTypeEnum.Byte, 1f, 0f);
        File lFile = new File("out/temp/test.tif");
        if (lFile.exists()) {
            lFile.delete();
        }

        lTiffWriter.write(lImageDst, lFile);
    }
}
