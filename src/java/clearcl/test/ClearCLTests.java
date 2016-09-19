package clearcl.test;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLPlatform;
import clearcl.ClearCLProgram;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.jocl.ClearCLJOCLBackend;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageChannelType;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;
import clearcl.enums.BuildStatus;
import clearcl.enums.DataType;

import static org.junit.Assert.*;

public class ClearCLTests
{

	private static final int cFloatArrayLength = 1024 * 1024;

	@Test
	public void testJOCL() throws Exception
	{
		ClearCLJOCLBackend lClearCLJOCLBackend = new ClearCLJOCLBackend();

		testWithBackend(lClearCLJOCLBackend);

	}

	private void testWithBackend(ClearCLBackendInterface pClearCLBackendInterface) throws Exception
	{
		try (ClearCL lClearCL = new ClearCL(pClearCLBackendInterface))
		{

			int lNumberOfPlatforms = lClearCL.getNumberOfPlatforms();

			System.out.println("lNumberOfPlatforms=" + lNumberOfPlatforms);

			for (int i = 0; i < lNumberOfPlatforms; i++)
			{
				ClearCLPlatform lPlatform = lClearCL.getPlatform(0);

				System.out.println(lPlatform.getInfoString());

				for (int d = 0; d < lPlatform.getNumberOfDevices(); d++)
				{
					ClearCLDevice lClearClDevice = lPlatform.getDevice(d);

					System.out.println("\t" + d
															+ " -> "
															+ lClearClDevice.getInfoString());

					ClearCLContext lContext = lClearClDevice.createContext();

					testBuffers(lContext);

					testImages(lContext);

				}

			}
		}
	}

	private void testImages(ClearCLContext lContext)
	{
		try
		{
			ClearCLImage lImageSrc = lContext.createImage(	HostAccessType.WriteOnly,
														KernelAccessType.ReadOnly,
														ImageType.IMAGE2D,
														ImageChannelOrder.Intensity,
														ImageChannelType.Float,
														10,
														10,
														1);
			
			ClearCLImage lImageDst = lContext.createImage(	HostAccessType.ReadOnly,
			                																KernelAccessType.WriteOnly,
			                																ImageType.IMAGE2D,
			                																ImageChannelOrder.Intensity,
			                																ImageChannelType.Float,
			                																10,
			                																10,
			                																1);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	private void testBuffers(ClearCLContext lCreateContext) throws IOException
	{
		float[] lArrayA = new float[cFloatArrayLength];
		float[] lArrayB = new float[cFloatArrayLength];
		float[] lArrayC = new float[cFloatArrayLength];

		for (int j = 0; j < cFloatArrayLength; j++)
		{
			lArrayA[j] = j;
			lArrayB[j] = j;
		}

		ClearCLBuffer lBufferA = lCreateContext.createBuffer(	HostAccessType.WriteOnly,
																													KernelAccessType.ReadOnly,
																													DataType.Float,
																													cFloatArrayLength);

		ClearCLBuffer lBufferB = lCreateContext.createBuffer(	HostAccessType.WriteOnly,
																													KernelAccessType.ReadOnly,
																													DataType.Float,
																													cFloatArrayLength);

		ClearCLBuffer lBufferC = lCreateContext.createBuffer(	HostAccessType.ReadOnly,
																													KernelAccessType.WriteOnly,
																													DataType.Float,
																													cFloatArrayLength);

		lBufferA.readFrom(FloatBuffer.wrap(lArrayA),
											0L,
											cFloatArrayLength,
											true);
		lBufferB.readFrom(FloatBuffer.wrap(lArrayB),
											0L,
											cFloatArrayLength,
											true);

		ClearCLProgram lProgram = lCreateContext.createProgram(	this.getClass(),
																														"test.cl");

		BuildStatus lBuildStatus = lProgram.build();

		System.out.println(lProgram.getBuildLog());
		System.out.println(lBuildStatus);

		assertEquals(lBuildStatus, BuildStatus.Success);
		assertTrue(lProgram.getBuildLog().isEmpty());

		ClearCLKernel lKernel = lProgram.createKernel("sampleKernel");

		lKernel.setArguments(lBufferA, lBufferB, lBufferC);

		lKernel.run(true, cFloatArrayLength);

		lBufferC.writeTo(	FloatBuffer.wrap(lArrayC),
											0,
											cFloatArrayLength,
											true);

		for (int j = 0; j < cFloatArrayLength; j++)
		{
			if (lArrayC[j] != 2 * j)
			{
				System.out.format("NOT EQUAL: %g \n", lArrayC[j]);
				assertTrue(false);
				break;
			}
		}
	}

}
