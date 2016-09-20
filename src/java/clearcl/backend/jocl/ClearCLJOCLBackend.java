package clearcl.backend.jocl;

import static org.jocl.CL.CL_CONTEXT_PLATFORM;
import static org.jocl.CL.CL_RGBA;
import static org.jocl.CL.CL_UNSIGNED_INT8;
import static org.jocl.CL.clBuildProgram;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clCreateCommandQueue;
import static org.jocl.CL.clCreateContext;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clCreateProgramWithSource;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clEnqueueReadImage;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clReleaseCommandQueue;
import static org.jocl.CL.clReleaseContext;
import static org.jocl.CL.clReleaseKernel;
import static org.jocl.CL.clReleaseMemObject;
import static org.jocl.CL.clReleaseProgram;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_image_desc;
import org.jocl.cl_image_format;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLException;
import clearcl.ClearCLPeerPointer;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.enums.BuildStatus;
import clearcl.enums.DeviceType;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;
import coremem.ContiguousMemoryInterface;

public class ClearCLJOCLBackend implements ClearCLBackendInterface
{

	static
	{
		CL.setExceptionsEnabled(true);
	}

	public boolean imageSupport(ClearCLPeerPointer pDevicePointer)
	{
		return Utils.getBoolean((cl_device_id) pDevicePointer.getPointer(),
														CL.CL_DEVICE_IMAGE_SUPPORT);
	}

	@Override
	public int getNumberOfPlatforms()
	{
		return checkExceptions(() -> {
			int numPlatformsArray[] = new int[1];
			clGetPlatformIDs(0, null, numPlatformsArray);
			int lNumberOfPlatforms = numPlatformsArray[0];
			return lNumberOfPlatforms;
		});
	}

	@Override
	public ClearCLPeerPointer getPlatformIds(int pPlatformIndex)
	{
		return checkExceptions(() -> {
			cl_platform_id platforms[] = new cl_platform_id[getNumberOfPlatforms()];
			clGetPlatformIDs(platforms.length, platforms, null);
			cl_platform_id platform = platforms[pPlatformIndex];
			return new ClearCLPeerPointer(platform);
		});
	}

	@Override
	public int getNumberOfDevicesForPlatform(	ClearCLPeerPointer pPlatformPointer,
																						DeviceType pDeviceType)
	{
		return checkExceptions(() -> {
			long lDeviceType = 0;
			if (pDeviceType == DeviceType.CPU)
				lDeviceType = CL.CL_DEVICE_TYPE_CPU;
			else if (pDeviceType == DeviceType.GPU)
				lDeviceType = CL.CL_DEVICE_TYPE_GPU;

			return getNumberOfDevicesForPlatform(	pPlatformPointer,
																						lDeviceType);
		});
	}

	@Override
	public int getNumberOfDevicesForPlatform(ClearCLPeerPointer pPlatformPointer)
	{
		return checkExceptions(() -> {
			return getNumberOfDevicesForPlatform(	pPlatformPointer,
																						CL.CL_DEVICE_TYPE_ALL);
		});
	}

	private int getNumberOfDevicesForPlatform(ClearCLPeerPointer pPlatformPointer,
																						long pDeviceType)
	{
		return checkExceptions(() -> {
			int numDevicesArray[] = new int[1];
			clGetDeviceIDs(	(cl_platform_id) (pPlatformPointer.getPointer()),
											pDeviceType,
											0,
											null,
											numDevicesArray);
			int numDevices = numDevicesArray[0];
			return numDevices;
		});
	}

	/*
	@Override
	public int getDeviceName(ClearCLPointer pPlatformPointer)
	{
		return getNumberOfDevicesForPlatform(	pPlatformPointer,
																					CL.CL_DEVICE_TYPE_CPU);
	}/**/

	@Override
	public ClearCLPeerPointer getDeviceId(ClearCLPeerPointer pPlatformPointer,
																				DeviceType pDeviceType,
																				int pDeviceIndex)
	{
		return checkExceptions(() -> {
			long lDeviceType = 0;
			if (pDeviceType == DeviceType.CPU)
				lDeviceType = CL.CL_DEVICE_TYPE_CPU;
			else if (pDeviceType == DeviceType.GPU)
				lDeviceType = CL.CL_DEVICE_TYPE_CPU;

			return getDeviceId(pPlatformPointer, lDeviceType, pDeviceIndex);
		});
	}

	@Override
	public ClearCLPeerPointer getDeviceId(ClearCLPeerPointer pPlatformPointer,
																				int pDeviceIndex)
	{
		return getDeviceId(	pPlatformPointer,
												CL.CL_DEVICE_TYPE_ALL,
												pDeviceIndex);
	}

	private ClearCLPeerPointer getDeviceId(	ClearCLPeerPointer pPlatformPointer,
																					long pDeviceType,
																					int pDeviceIndex)
	{
		return checkExceptions(() -> {
			// Obtain a device ID
			cl_device_id devices[] = new cl_device_id[getNumberOfDevicesForPlatform(pPlatformPointer)];
			clGetDeviceIDs(	(cl_platform_id) pPlatformPointer.getPointer(),
											pDeviceType,
											devices.length,
											devices,
											null);
			cl_device_id device = devices[pDeviceIndex];
			return new ClearCLPeerPointer(device);
		});
	}

	@Override
	public String getPlatformName(ClearCLPeerPointer pPlatformPointer)
	{
		return getPlatformInfo(pPlatformPointer, CL.CL_PLATFORM_NAME);
	}

	private String getPlatformInfo(	ClearCLPeerPointer pPlatformPointer,
																	int pInfoId)
	{
		return checkExceptions(() -> {
			return Utils.getString(	(cl_platform_id) pPlatformPointer.getPointer(),
															pInfoId);
		});
	}

	@Override
	public String getDeviceName(ClearCLPeerPointer pDevicePointer)
	{
		return getDeviceInfo(pDevicePointer, CL.CL_DEVICE_NAME);
	}

	@Override
	public DeviceType getDeviceType(ClearCLPeerPointer pDevicePointer)
	{
		return checkExceptions(() -> {
			long lDeviceType = getDeviceLong(	pDevicePointer,
																				CL.CL_DEVICE_TYPE);
			if (lDeviceType == CL.CL_DEVICE_TYPE_CPU)
				return DeviceType.CPU;
			else if (lDeviceType == CL.CL_DEVICE_TYPE_GPU)
				return DeviceType.GPU;
			else
				return DeviceType.OTHER;
		});
	}

	@Override
	public String getDeviceVersion(ClearCLPeerPointer pDevicePointer)
	{
		return getDeviceInfo(	pDevicePointer,
													CL.CL_DEVICE_OPENCL_C_VERSION);
	}

	private String getDeviceInfo(	ClearCLPeerPointer pDevicePointer,
																int pInfoId)
	{
		return checkExceptions(() -> {
			return Utils.getString(	(cl_device_id) pDevicePointer.getPointer(),
															pInfoId);
		});
	}

	private long getDeviceLong(ClearCLPeerPointer pPointer, int pInfoId)
	{
		return checkExceptions(() -> {
			return Utils.getLong(	(cl_device_id) pPointer.getPointer(),
														pInfoId);
		});
	}

	private long getDeviceInt(ClearCLPeerPointer pPointer, int pInfoId)
	{
		return checkExceptions(() -> {
			return Utils.getInt((cl_device_id) pPointer.getPointer(),
													pInfoId);
		});
	}

	@Override
	public ClearCLPeerPointer getContext(	ClearCLPeerPointer pPlatformPointer,
																				ClearCLPeerPointer... pDevicePointers)
	{
		return checkExceptions(() -> {
			// Initialize the context properties
			cl_context_properties contextProperties = new cl_context_properties();
			contextProperties.addProperty(CL_CONTEXT_PLATFORM,
																		(cl_platform_id) pPlatformPointer.getPointer());

			// Create a context for the given devices
			cl_context context = clCreateContext(	contextProperties,
																						pDevicePointers.length,
																						convertDevicePointers(pDevicePointers),
																						null,
																						null,
																						null);

			return new ClearCLPeerPointer(context);
		});
	}

	private cl_device_id[] convertDevicePointers(ClearCLPeerPointer... pDevicePointers)
	{
		return checkExceptions(() -> {
			cl_device_id[] lJOCLDevicePointers = new cl_device_id[pDevicePointers.length];

			for (int i = 0; i < pDevicePointers.length; i++)
				lJOCLDevicePointers[i] = (cl_device_id) pDevicePointers[i].getPointer();

			return lJOCLDevicePointers;
		});
	}

	@Override
	public ClearCLPeerPointer createQueue(ClearCLPeerPointer pDevicePointer,
																				ClearCLPeerPointer pContextPointer,
																				boolean pInOrder)
	{
		return checkExceptions(() -> {
			@SuppressWarnings("deprecation")
			cl_command_queue commandQueue = clCreateCommandQueue(	(cl_context) pContextPointer.getPointer(),
																														(cl_device_id) pDevicePointer.getPointer(),
																														pInOrder ? 0
																																		: CL.CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE,
																														null);

			ClearCLPeerPointer lCommandQueuePointer = new ClearCLPeerPointer(commandQueue);

			return lCommandQueuePointer;
		});
	}

	@Override
	public ClearCLPeerPointer createBuffer(	ClearCLPeerPointer pContextPointer,
																					HostAccessType pHostAccessType,
																					KernelAccessType pKernelAccessType,
																					long pBufferSize)
	{
		return checkExceptions(() -> {
			long lMemFlags = getMemTypeFlags(	pHostAccessType,
																				pKernelAccessType);
			Pointer lPointer = new Pointer();
			cl_mem lBufferPointer = clCreateBuffer(	(cl_context) pContextPointer.getPointer(),
																							lMemFlags,
																							pBufferSize,
																							lPointer,
																							null);
			ClearCLPeerPointer lClearCLPointer = new ClearCLPeerPointer(lBufferPointer);
			return lClearCLPointer;
		});
	}

	@Override
	public ClearCLPeerPointer createImage(ClearCLPeerPointer pContextPointer,
																				HostAccessType pHostAccessType,
																				KernelAccessType pKernelAccessType,
																				ImageType pImageType,
																				ImageChannelOrder pImageChannelOrder,
																				ImageChannelType pImageChannelType,
																				long pWidth,
																				long pHeight,
																				long pDepth)
	{
		cl_image_format lImageFormat = new cl_image_format();
		lImageFormat.image_channel_order = getImageChannelOrderFlags(pImageChannelOrder);
		lImageFormat.image_channel_data_type = getImageChannelTypeFlags(pImageChannelType);

		cl_image_desc lImageDescription = new cl_image_desc();
		lImageDescription.image_width = pWidth;
		lImageDescription.image_height = pHeight;
		lImageDescription.image_depth = pDepth;
		lImageDescription.image_type = getImageTypeFlags(pImageType);

		long lMemFlags = getMemTypeFlags(	pHostAccessType,
																			pKernelAccessType);

		cl_mem lImageMem = CL.clCreateImage((cl_context) pContextPointer.getPointer(),
																				lMemFlags,
																				lImageFormat,
																				lImageDescription,
																				null,
																				null);

		ClearCLPeerPointer lClearCLPeerPointer = new ClearCLPeerPointer(lImageMem);

		return lClearCLPeerPointer;
	}

	@Override
	public ClearCLPeerPointer createProgram(ClearCLPeerPointer pContextPointer,
																					String... pSourceCode)
	{
		return checkExceptions(() -> {
			cl_program program = clCreateProgramWithSource(	(cl_context) pContextPointer.getPointer(),
																											pSourceCode.length,
																											pSourceCode,
																											null,
																											null);

			ClearCLPeerPointer lClearCLPointer = new ClearCLPeerPointer(program);
			return lClearCLPointer;
		});
	}

	@Override
	public void buildProgram(	ClearCLPeerPointer pProgramPointer,
														String pOptions)
	{
		checkExceptions(() -> {
			clBuildProgram(	(cl_program) pProgramPointer.getPointer(),
											0,
											null,
											(pOptions == null || pOptions.isEmpty()) ? null
																															: pOptions,
											null,
											null);
		});
	}

	@Override
	public BuildStatus getBuildStatus(ClearCLPeerPointer pDevicePointer,
																		ClearCLPeerPointer pProgramPointer)
	{
		return checkExceptions(() -> {
			int status[] = new int[1];
			CL.clGetProgramBuildInfo(	(cl_program) pProgramPointer.getPointer(),
																(cl_device_id) pDevicePointer.getPointer(),
																CL.CL_PROGRAM_BUILD_STATUS,
																status.length * Sizeof.cl_int,
																Pointer.to(status),
																null);

			BuildStatus lBuildStatus = null;

			switch (status[0])
			{
			case CL.CL_BUILD_NONE:
				lBuildStatus = BuildStatus.None;
				break;
			case CL.CL_BUILD_ERROR:
				lBuildStatus = BuildStatus.Error;
				break;
			case CL.CL_BUILD_SUCCESS:
				lBuildStatus = BuildStatus.Success;
				break;
			case CL.CL_BUILD_IN_PROGRESS:
				lBuildStatus = BuildStatus.InProgress;
				break;
			}

			return lBuildStatus;
		});
	}

	@Override
	public String getBuildLog(ClearCLPeerPointer pDevicePointer,
														ClearCLPeerPointer pProgramPointer)
	{
		return getBuildProgramInfo(	pDevicePointer,
																pProgramPointer,
																CL.CL_PROGRAM_BUILD_LOG);
	}

	private String getBuildProgramInfo(	ClearCLPeerPointer pDevicePointer,
																			ClearCLPeerPointer pProgramPointer,
																			int pInfoName)
	{
		return checkExceptions(() -> {
			// Obtain the length of the string that will be queried
			long size[] = new long[1];
			CL.clGetProgramBuildInfo(	(cl_program) pProgramPointer.getPointer(),
																(cl_device_id) pDevicePointer.getPointer(),
																pInfoName,
																0,
																null,
																size);

			// Create a buffer of the appropriate size and fill it with the info
			byte buffer[] = new byte[(int) size[0]];
			CL.clGetProgramBuildInfo(	(cl_program) pProgramPointer.getPointer(),
																(cl_device_id) pDevicePointer.getPointer(),
																pInfoName,
																buffer.length,
																Pointer.to(buffer),
																null);

			// Create a string from the buffer (excluding the trailing \0 byte)
			return new String(buffer, 0, buffer.length - 1);
		});

	}

	@Override
	public ClearCLPeerPointer createKernel(	ClearCLPeerPointer pProgramPointer,
																					String pKernelName)
	{
		return checkExceptions(() -> {
			cl_kernel kernel = clCreateKernel((cl_program) pProgramPointer.getPointer(),
																				pKernelName,
																				null);
			ClearCLPeerPointer lClearCLPointer = new ClearCLPeerPointer(kernel);
			return lClearCLPointer;
		});
	}

	@Override
	public void setKernelArgument(ClearCLPeerPointer pKernelPointer,
																int pIndex,
																Object pObject)
	{
		checkExceptions(() -> {
			cl_kernel lKernelPointer = (cl_kernel) pKernelPointer.getPointer();
			if (pObject instanceof Byte)

				CL.clSetKernelArg(lKernelPointer,
													pIndex,
													Sizeof.cl_char,
													Pointer.to(new byte[]
													{ (byte) pObject }));
			else if (pObject instanceof Character)
				CL.clSetKernelArg(lKernelPointer,
													pIndex,
													Sizeof.cl_uchar2,
													Pointer.to(new char[]
													{ (char) pObject }));
			else if (pObject instanceof Short)
				CL.clSetKernelArg(lKernelPointer,
													pIndex,
													Sizeof.cl_char2,
													Pointer.to(new short[]
													{ (short) pObject }));
			else if (pObject instanceof Integer)
				CL.clSetKernelArg(lKernelPointer,
													pIndex,
													Sizeof.cl_int,
													Pointer.to(new int[]
													{ (int) pObject }));
			else if (pObject instanceof Long)
				CL.clSetKernelArg(lKernelPointer,
													pIndex,
													Sizeof.cl_long,
													Pointer.to(new long[]
													{ (long) pObject }));
			else if (pObject instanceof Float)
				CL.clSetKernelArg(lKernelPointer,
													pIndex,
													Sizeof.cl_float,
													Pointer.to(new float[]
													{ (float) pObject }));
			else if (pObject instanceof Double)
				CL.clSetKernelArg(lKernelPointer,
													pIndex,
													Sizeof.cl_double,
													Pointer.to(new double[]
													{ (double) pObject }));
			else if (pObject instanceof ClearCLBuffer)
			{
				ClearCLBuffer lClearCLBuffer = (ClearCLBuffer) pObject;
				CL.clSetKernelArg(lKernelPointer,
													pIndex,
													Sizeof.cl_mem,
													Pointer.to(((cl_mem) lClearCLBuffer.getPeerPointer()
																															.getPointer())));
			}
		});

	}

	/**
	 * @param pQueuePointer
	 * @param pKernelPointer
	 * @param pNumberOfDimension
	 * @param pOffsets
	 */
	@Override
	public void enqueueKernelExecution(	ClearCLPeerPointer pQueuePointer,
																			ClearCLPeerPointer pKernelPointer,
																			int pNumberOfDimension,
																			long[] pGlobalOffsets,
																			long[] pGlobalSizes,
																			long[] pLocalSizes)
	{

		checkExceptions(() -> {
			clEnqueueNDRangeKernel(	(cl_command_queue) pQueuePointer.getPointer(),
															(cl_kernel) pKernelPointer.getPointer(),
															pNumberOfDimension,
															pGlobalOffsets,
															pGlobalSizes,
															pLocalSizes,
															0,
															null,
															null);
		});

	}

	@Override
	public void enqueueReadFromBuffer(ClearCLPeerPointer pQueuePointer,
																		ClearCLPeerPointer pBufferPointer,
																		boolean pBlockingRead,
																		long pOffsetInBuffer,
																		long pLengthInBytes,
																		ClearCLPeerPointer pHostMemPointer)
	{
		checkExceptions(() -> {
			clEnqueueReadBuffer((cl_command_queue) pQueuePointer.getPointer(),
													(cl_mem) pBufferPointer.getPointer(),
													pBlockingRead,
													pOffsetInBuffer,
													pLengthInBytes,
													(Pointer) pHostMemPointer.getPointer(),
													0,
													null,
													null);
		});
	}

	@Override
	public void enqueueWriteToBuffer(	ClearCLPeerPointer pQueuePointer,
																		ClearCLPeerPointer pBufferPointer,
																		boolean pBlockingWrite,
																		long pOffsetInBuffer,
																		long pLengthInBytes,
																		ClearCLPeerPointer pHostMemPointer)
	{
		checkExceptions(() -> {

			CL.clEnqueueWriteBuffer((cl_command_queue) pQueuePointer.getPointer(),
															(cl_mem) pBufferPointer.getPointer(),
															pBlockingWrite,
															pOffsetInBuffer,
															pLengthInBytes,
															(Pointer) pHostMemPointer.getPointer(),
															0,
															null,
															null);
		});
	}

	@Override
	public void enqueueReadFromBufferBox(	ClearCLPeerPointer pQueuePointer,
																				ClearCLPeerPointer pBufferPointer,
																				boolean pBlockingRead,
																				long[] pBufferOrigin,
																				long[] pHostOrigin,
																				long[] pRegion,
																				ClearCLPeerPointer pHostMemPointer)
	{
		checkExceptions(() -> {
			CL.clEnqueueReadBufferRect(	(cl_command_queue) pQueuePointer.getPointer(),
																	(cl_mem) pBufferPointer.getPointer(),
																	pBlockingRead,
																	pBufferOrigin,
																	pHostOrigin,
																	pRegion,
																	0,
																	0,
																	0,
																	0,
																	(Pointer) pHostMemPointer.getPointer(),
																	0,
																	null,
																	null);
		});
	}

	@Override
	public void enqueueWriteToBufferBox(ClearCLPeerPointer pQueuePointer,
																			ClearCLPeerPointer pBufferPointer,
																			boolean pBlockingWrite,
																			long[] pBufferOrigin,
																			long[] pHostOrigin,
																			long[] pRegion,
																			ClearCLPeerPointer pHostMemPointer)
	{
		checkExceptions(() -> {

			CL.clEnqueueWriteBufferRect((cl_command_queue) pQueuePointer.getPointer(),
																	(cl_mem) pBufferPointer.getPointer(),
																	pBlockingWrite,
																	pBufferOrigin,
																	pHostOrigin,
																	pRegion,
																	0,
																	0,
																	0,
																	0,
																	(Pointer) pHostMemPointer.getPointer(),
																	0,
																	null,
																	null);
		});
	}

	@Override
	public void enqueueFillBuffer(ClearCLPeerPointer pQueuePointer,
																ClearCLPeerPointer pBufferPointer,
																boolean pBlockingFill,
																long pOffsetInBytes,
																long pLengthInBytes,
																byte[] pPattern)
	{
		checkExceptions(() -> {

			Pointer lPatternPointer = Pointer.to(pPattern);

			CL.clEnqueueFillBuffer(	(cl_command_queue) pQueuePointer.getPointer(),
															(cl_mem) pBufferPointer.getPointer(),
															lPatternPointer,
															pPattern.length,
															pOffsetInBytes,
															pLengthInBytes,
															0,
															null,
															null);

			if (pBlockingFill)
				waitQueueToFinish(pQueuePointer);

		});
	}

	@Override
	public void enqueueCopyBuffer(ClearCLPeerPointer pQueuePointer,
																ClearCLPeerPointer pSrcBufferPointer,
																ClearCLPeerPointer pDstBufferPointer,
																boolean pBlockingCopy,
																long pSrcOffsetInBytes,
																long pDstOffsetInBytes,
																long pLengthToCopyInBytes)
	{
		checkExceptions(() -> {

			CL.clEnqueueCopyBuffer(	(cl_command_queue) pQueuePointer.getPointer(),
															(cl_mem) pSrcBufferPointer.getPointer(),
															(cl_mem) pDstBufferPointer.getPointer(),
															pSrcOffsetInBytes,
															pDstOffsetInBytes,
															pLengthToCopyInBytes,
															0,
															null,
															null);

			if (pBlockingCopy)
				waitQueueToFinish(pQueuePointer);

		});
	}

	@Override
	public void enqueueCopyBufferBox(	ClearCLPeerPointer pQueuePointer,
																		ClearCLPeerPointer pSrcBufferPointer,
																		ClearCLPeerPointer pDstBufferPointer,
																		boolean pBlockingCopy,
																		long[] pSrcOrigin,
																		long[] pDstOrigin,
																		long[] pRegion)
	{
		checkExceptions(() -> {

			CL.clEnqueueCopyBufferRect(	(cl_command_queue) pQueuePointer.getPointer(),
																	(cl_mem) pSrcBufferPointer.getPointer(),
																	(cl_mem) pDstBufferPointer.getPointer(),
																	pSrcOrigin,
																	pDstOrigin,
																	pRegion,
																	0,
																	0,
																	0,
																	0,
																	0,
																	null,
																	null);

			if (pBlockingCopy)
				waitQueueToFinish(pQueuePointer);

		});
	}

	@Override
	public void enqueueCopyBufferToImage(	ClearCLPeerPointer pQueuePointer,
																				ClearCLPeerPointer pSrcBufferPointer,
																				ClearCLPeerPointer pDstImagePointer,
																				boolean pBlockingCopy,
																				long pSrcOffsetInBytes,
																				long[] pDstOrigin,
																				long[] pDstRegion)
	{
		checkExceptions(() -> {

			CL.clEnqueueCopyBufferToImage((cl_command_queue) pQueuePointer.getPointer(),
																		(cl_mem) pSrcBufferPointer.getPointer(),
																		(cl_mem) pDstImagePointer.getPointer(),
																		pSrcOffsetInBytes,
																		pDstOrigin,
																		pDstRegion,
																		0,
																		null,
																		null);

			if (pBlockingCopy)
				waitQueueToFinish(pQueuePointer);

		});
	}

	@Override
	public void enqueueReadFromImage(	ClearCLPeerPointer pQueuePointer,
																		ClearCLPeerPointer pImagePointer,
																		boolean pReadWrite,
																		long[] pOrigin,
																		long[] pRegion,
																		ClearCLPeerPointer pHostMemPointer)
	{
		checkExceptions(() -> {
			CL.clEnqueueReadImage((cl_command_queue) pQueuePointer.getPointer(),
														(cl_mem) pImagePointer.getPointer(),
														pReadWrite,
														pOrigin,
														pRegion,
														0,
														0,
														(Pointer) pHostMemPointer.getPointer(),
														0,
														null,
														null);
		});
	}

	@Override
	public void enqueueWriteToImage(ClearCLPeerPointer pQueuePointer,
																	ClearCLPeerPointer pImagePointer,
																	boolean pBlockingWrite,
																	long[] pOrigin,
																	long[] pRegion,
																	ClearCLPeerPointer pHostMemPointer)
	{
		checkExceptions(() -> {
			CL.clEnqueueWriteImage(	(cl_command_queue) pQueuePointer.getPointer(),
															(cl_mem) pImagePointer.getPointer(),
															true,
															pOrigin,
															pRegion,
															0,
															0,
															(Pointer) pHostMemPointer.getPointer(),
															0,
															null,
															null);
		});
	}

	@Override
	public void enqueueFillImage(	ClearCLPeerPointer pQueuePointer,
																ClearCLPeerPointer pImagePointer,
																boolean pBlockingFill,
																long[] pOrigin,
																long[] pRegion,
																byte[] pColor)
	{
		checkExceptions(() -> {

			Pointer lColorPointer = Pointer.to(pColor);

			CL.clEnqueueFillImage((cl_command_queue) pQueuePointer.getPointer(),
														(cl_mem) pImagePointer.getPointer(),
														lColorPointer,
														pOrigin,
														pRegion,
														0,
														null,
														null);

			if (pBlockingFill)
				waitQueueToFinish(pQueuePointer);

		});
	}

	@Override
	public void enqueueCopyImage(	ClearCLPeerPointer pQueuePointer,
																ClearCLPeerPointer pSrcBImagePointer,
																ClearCLPeerPointer pDstImagePointer,
																boolean pBlockingCopy,
																long[] pSrcOrigin,
																long[] pDstOrigin,
																long[] pRegion)
	{
		checkExceptions(() -> {

			CL.clEnqueueCopyImage((cl_command_queue) pQueuePointer.getPointer(),
														(cl_mem) pSrcBImagePointer.getPointer(),
														(cl_mem) pDstImagePointer.getPointer(),
														pSrcOrigin,
														pDstOrigin,
														pRegion,
														0,
														null,
														null);

			if (pBlockingCopy)
				waitQueueToFinish(pQueuePointer);

		});
	}

	@Override
	public void enqueueCopyImageToBuffer(	ClearCLPeerPointer pQueuePointer,
																				ClearCLPeerPointer pSrcImagePointer,
																				ClearCLPeerPointer pDstBufferPointer,
																				boolean pBlockingCopy,
																				long[] pSrcOrigin,
																				long[] pSrcRegion,
																				long pDstOffset)
	{
		checkExceptions(() -> {

			CL.clEnqueueCopyImageToBuffer((cl_command_queue) pQueuePointer.getPointer(),
																		(cl_mem) pSrcImagePointer.getPointer(),
																		(cl_mem) pDstBufferPointer.getPointer(),
																		pSrcOrigin,
																		pSrcRegion,
																		pDstOffset,
																		0,
																		null,
																		null);

			if (pBlockingCopy)
				waitQueueToFinish(pQueuePointer);

		});
	}

	@Override
	public ClearCLPeerPointer wrap(Buffer pBuffer)
	{
		return checkExceptions(() -> {
			Pointer lPointer = Pointer.to(pBuffer);
			ClearCLPeerPointer lPeerPointer = new ClearCLPeerPointer(lPointer);
			return lPeerPointer;
		});
	}

	@Override
	public ClearCLPeerPointer wrap(ContiguousMemoryInterface pContiguousMemory)
	{
		return checkExceptions(() -> {
			ByteBuffer lByteBuffer = pContiguousMemory.getByteBuffer();
			return wrap(lByteBuffer);
		});
	}

	@Override
	public void releaseBuffer(ClearCLPeerPointer pPeerPointer)
	{
		checkExceptions(() -> {
			clReleaseMemObject((cl_mem) pPeerPointer.getPointer());
		});
	}

	@Override
	public void releaseContext(ClearCLPeerPointer pPeerPointer)
	{
		checkExceptions(() -> {
			clReleaseContext((cl_context) pPeerPointer.getPointer());
		});
	}

	@Override
	public void releaseDevice(ClearCLPeerPointer pPeerPointer)
	{
		checkExceptions(() -> {
			CL.clReleaseDevice((cl_device_id) pPeerPointer.getPointer());
		});

	}

	@Override
	public void releaseImage(ClearCLPeerPointer pPeerPointer)
	{
		checkExceptions(() -> {
			clReleaseMemObject((cl_mem) pPeerPointer.getPointer());
		});
	}

	@Override
	public void releaseKernel(ClearCLPeerPointer pPeerPointer)
	{
		checkExceptions(() -> {
			clReleaseKernel((cl_kernel) pPeerPointer.getPointer());
		});
	}

	@Override
	public void releaseProgram(ClearCLPeerPointer pPeerPointer)
	{
		checkExceptions(() -> {
			clReleaseProgram((cl_program) pPeerPointer.getPointer());
		});
	}

	@Override
	public void releaseQueue(ClearCLPeerPointer pPeerPointer)
	{
		checkExceptions(() -> {
			clReleaseCommandQueue((cl_command_queue) pPeerPointer.getPointer());
		});
	}

	@Override
	public void waitQueueToFinish(ClearCLPeerPointer pQueuePointer)
	{
		checkExceptions(() -> {
			CL.clFinish((cl_command_queue) pQueuePointer.getPointer());
		});
	}

	private static final <T> T checkExceptions(Callable<T> pCallable)
	{
		try
		{
			return pCallable.call();
		}
		catch (Throwable e)
		{
			throw new ClearCLException(e.getMessage(), e);
		}
	}

	private static final void checkExceptions(Runnable pRunnable)
	{
		try
		{
			pRunnable.run();
		}
		catch (Throwable e)
		{
			throw new ClearCLException(e.getMessage(), e);
		}
	}

	private long getMemTypeFlags(	HostAccessType pHostAccessType,
																KernelAccessType pKernelAccessType)
	{
		long lMemFlags = 0;
		switch (pHostAccessType)
		{
		case ReadOnly:
			lMemFlags |= CL.CL_MEM_HOST_READ_ONLY;
			break;
		case WriteOnly:
			lMemFlags |= CL.CL_MEM_HOST_WRITE_ONLY;
			break;
		case ReadWrite:
			lMemFlags |= 0;
			break;
		case NoAccess:
			lMemFlags |= CL.CL_MEM_HOST_NO_ACCESS;
			break;
		}

		switch (pKernelAccessType)
		{
		case ReadOnly:
			lMemFlags |= CL.CL_MEM_READ_ONLY;
			break;
		case WriteOnly:
			lMemFlags |= CL.CL_MEM_WRITE_ONLY;
			break;
		case ReadWrite:
			lMemFlags |= CL.CL_MEM_READ_WRITE;
			break;
		}
		return lMemFlags;
	}

	private int getImageTypeFlags(ImageType pImageType)
	{
		int lImageTypeFlags = 0;
		switch (pImageType)
		{
		case IMAGE1D:
			lImageTypeFlags |= CL.CL_MEM_OBJECT_IMAGE1D;
			break;
		case IMAGE2D:
			lImageTypeFlags |= CL.CL_MEM_OBJECT_IMAGE2D;
			break;
		case IMAGE3D:
			lImageTypeFlags |= CL.CL_MEM_OBJECT_IMAGE3D;
			break;
		}
		return lImageTypeFlags;
	}

	private int getImageChannelOrderFlags(ImageChannelOrder pImageChannelOrder)
	{
		int lImageChannelOrderFlags = 0;
		switch (pImageChannelOrder)
		{
		case Intensity:
			lImageChannelOrderFlags |= CL.CL_INTENSITY;
			break;
		case Luminance:
			lImageChannelOrderFlags |= CL.CL_LUMINANCE;
			break;
		case A:
			lImageChannelOrderFlags |= CL.CL_A;
			break;
		case R:
			lImageChannelOrderFlags |= CL.CL_R;
			break;
		case RA:
			lImageChannelOrderFlags |= CL.CL_RA;
			break;
		case RG:
			lImageChannelOrderFlags |= CL.CL_RG;
			break;
		case RGB:
			lImageChannelOrderFlags |= CL.CL_RGB;
			break;
		case ARGB:
			lImageChannelOrderFlags |= CL.CL_ARGB;
			break;
		case BGRA:
			lImageChannelOrderFlags |= CL.CL_BGRA;
			break;
		case RGBA:
			lImageChannelOrderFlags |= CL.CL_RGBA;
			break;

		}
		return lImageChannelOrderFlags;
	}

	private int getImageChannelTypeFlags(ImageChannelType pImageChannelType)
	{
		int lImageChannelOrderFlags = 0;
		switch (pImageChannelType)
		{
		case Float:
			lImageChannelOrderFlags |= CL.CL_FLOAT;
			break;
		case HalfFloat:
			lImageChannelOrderFlags |= CL.CL_HALF_FLOAT;
			break;
		case SignedInt16:
			lImageChannelOrderFlags |= CL.CL_SIGNED_INT16;
			break;
		case SignedInt32:
			lImageChannelOrderFlags |= CL.CL_SIGNED_INT32;
			break;
		case SignedInt8:
			lImageChannelOrderFlags |= CL.CL_SIGNED_INT8;
			break;
		case SignedNormalizedInt16:
			lImageChannelOrderFlags |= CL.CL_SNORM_INT16;
			break;
		case SignedNormalizedInt8:
			lImageChannelOrderFlags |= CL.CL_SNORM_INT8;
			break;
		case UnsignedNormalizedInt16:
			lImageChannelOrderFlags |= CL.CL_UNORM_INT16;
			break;
		case UnsignedNormalizedInt8:
			lImageChannelOrderFlags |= CL.CL_UNORM_INT8;
			break;
		case UnsignedInt16:
			lImageChannelOrderFlags |= CL.CL_UNSIGNED_INT16;
			break;
		case UnsignedInt32:
			lImageChannelOrderFlags |= CL.CL_UNSIGNED_INT32;
			break;
		case UnsignedInt8:
			lImageChannelOrderFlags |= CL.CL_UNSIGNED_INT8;
			break;

		}
		return lImageChannelOrderFlags;
	}

}
