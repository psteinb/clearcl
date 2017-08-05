package clearcl.hasetest;

import clearcl.*;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.enums.BuildStatus;
import clearcl.enums.HostAccessType;
import clearcl.enums.KernelAccessType;
import clearcl.test.ClearCLBasicTests;
import coremem.enums.NativeTypeEnum;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class HaseTest {
    @Test
    public void test() throws IOException {



        ClearCLBackendInterface lClearCLBackend =
                ClearCLBackends.getBestBackend();

        ClearCL lClearCL = new ClearCL(lClearCLBackend);


        ClearCLDevice lBestGPUDevice = lClearCL.getBestGPUDevice();

        System.out.println(lBestGPUDevice.getInfoString());

        ClearCLContext lContext = lBestGPUDevice.createContext();

        ClearCLProgram lProgram =
                lContext.createProgram(HaseTest.class,
                        "haseTest.cl");

        BuildStatus lBuildStatus = lProgram.buildAndLog();
        assertEquals(lBuildStatus, BuildStatus.Success);

        ClearCLKernel lKernel = lProgram.createKernel("testmethod");

        int lArrayLength = 10000000;

        ClearCLBuffer lBufferA =
                lContext.createBuffer(HostAccessType.WriteOnly,
                        KernelAccessType.ReadOnly,
                        NativeTypeEnum.Float,
                        lArrayLength);


        lKernel.setArgument("a", 4f);
        lKernel.setArgument("b", 3f);
        lKernel.setArgument("arr", lBufferA);
        lKernel.setGlobalSizes(lArrayLength);
        lKernel.run();




    }
}
