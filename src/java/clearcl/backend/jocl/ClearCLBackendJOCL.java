package clearcl.backend.jocl;

import static org.jocl.CL.CL_CONTEXT_PLATFORM;
import static org.jocl.CL.clBuildProgram;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clCreateCommandQueue;
import static org.jocl.CL.clCreateContext;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clCreateProgramWithSource;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clReleaseCommandQueue;
import static org.jocl.CL.clReleaseContext;
import static org.jocl.CL.clReleaseKernel;
import static org.jocl.CL.clReleaseMemObject;
import static org.jocl.CL.clReleaseProgram;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.jocl.CL;
import org.jocl.Pointer;
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
import clearcl.ClearCLImage;
import clearcl.ClearCLPeerPointer;
import clearcl.backend.BackendUtils;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.SizeOf;
import clearcl.enums.BuildStatus;
import clearcl.enums.DeviceInfo;
import clearcl.enums.DeviceType;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;
import clearcl.enums.MemAllocMode;
import clearcl.exceptions.ClearCLUnsupportedException;
import coremem.ContiguousMemoryInterface;
import coremem.fragmented.FragmentedMemoryInterface;
import coremem.util.Size;

public class ClearCLBackendJOCL implements ClearCLBackendInterface
{

  static
  {
    CL.setExceptionsEnabled(false);
  }

  @Override
  public boolean imageSupport(ClearCLPeerPointer pDevicePointer)
  {
    return Utils.getBoolean((cl_device_id) pDevicePointer.getPointer(),
                            CL.CL_DEVICE_IMAGE_SUPPORT);
  }

  @Override
  public int getNumberOfPlatforms()
  {
    return BackendUtils.checkExceptions(() -> {
      int numPlatformsArray[] = new int[1];
      BackendUtils.checkOpenCLError(clGetPlatformIDs(0,
                                                     null,
                                                     numPlatformsArray));
      int lNumberOfPlatforms = numPlatformsArray[0];
      return lNumberOfPlatforms;
    });
  }

  @Override
  public ClearCLPeerPointer getPlatformPeerPointer(int pPlatformIndex)
  {
    return BackendUtils.checkExceptions(() -> {
      cl_platform_id platforms[] =
                                 new cl_platform_id[getNumberOfPlatforms()];
      BackendUtils.checkOpenCLError(clGetPlatformIDs(platforms.length,
                                                     platforms,
                                                     null));
      cl_platform_id platform = platforms[pPlatformIndex];
      return new ClearCLPeerPointer(platform);
    });
  }

  @Override
  public int getNumberOfDevicesForPlatform(ClearCLPeerPointer pPlatformPointer,
                                           DeviceType pDeviceType)
  {
    return BackendUtils.checkExceptions(() -> {
      long lDeviceType = 0;
      if (pDeviceType == DeviceType.CPU)
        lDeviceType = CL.CL_DEVICE_TYPE_CPU;
      else if (pDeviceType == DeviceType.GPU)
        lDeviceType = CL.CL_DEVICE_TYPE_GPU;

      return getNumberOfDevicesForPlatform(pPlatformPointer,
                                           lDeviceType);
    });
  }

  @Override
  public int getNumberOfDevicesForPlatform(ClearCLPeerPointer pPlatformPointer)
  {
    return BackendUtils.checkExceptions(() -> {
      return getNumberOfDevicesForPlatform(pPlatformPointer,
                                           CL.CL_DEVICE_TYPE_ALL);
    });
  }

  private int getNumberOfDevicesForPlatform(ClearCLPeerPointer pPlatformPointer,
                                            long pDeviceType)
  {
    return BackendUtils.checkExceptions(() -> {
      int numDevicesArray[] = new int[1];
      BackendUtils.checkOpenCLError(clGetDeviceIDs((cl_platform_id) (pPlatformPointer.getPointer()),
                                                   pDeviceType,
                                                   0,
                                                   null,
                                                   numDevicesArray));
      int numDevices = numDevicesArray[0];
      return numDevices;
    });
  }

  @Override
  public ClearCLPeerPointer getDevicePeerPointer(ClearCLPeerPointer pPlatformPointer,
                                                 DeviceType pDeviceType,
                                                 int pDeviceIndex)
  {
    return BackendUtils.checkExceptions(() -> {
      long lDeviceType = 0;
      if (pDeviceType == DeviceType.CPU)
        lDeviceType = CL.CL_DEVICE_TYPE_CPU;
      else if (pDeviceType == DeviceType.GPU)
        lDeviceType = CL.CL_DEVICE_TYPE_CPU;

      return getDeviceId(pPlatformPointer, lDeviceType, pDeviceIndex);
    });
  }

  @Override
  public ClearCLPeerPointer getDevicePeerPointer(ClearCLPeerPointer pPlatformPointer,
                                                 int pDeviceIndex)
  {
    return getDeviceId(pPlatformPointer,
                       CL.CL_DEVICE_TYPE_ALL,
                       pDeviceIndex);
  }

  private ClearCLPeerPointer getDeviceId(ClearCLPeerPointer pPlatformPointer,
                                         long pDeviceType,
                                         int pDeviceIndex)
  {
    return BackendUtils.checkExceptions(() -> {
      // Obtain a device ID
      cl_device_id devices[] =
                             new cl_device_id[getNumberOfDevicesForPlatform(pPlatformPointer)];
      BackendUtils.checkOpenCLError(clGetDeviceIDs((cl_platform_id) pPlatformPointer.getPointer(),
                                                   pDeviceType,
                                                   devices.length,
                                                   devices,
                                                   null));
      cl_device_id device = devices[pDeviceIndex];
      return new ClearCLPeerPointer(device);
    });
  }

  @Override
  public String getPlatformName(ClearCLPeerPointer pPlatformPointer)
  {
    return getPlatformInfo(pPlatformPointer, CL.CL_PLATFORM_NAME);
  }

  private String getPlatformInfo(ClearCLPeerPointer pPlatformPointer,
                                 int pInfoId)
  {
    return BackendUtils.checkExceptions(() -> {
      return Utils.getString((cl_platform_id) pPlatformPointer.getPointer(),
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
    return BackendUtils.checkExceptions(() -> {
      long lDeviceType = getDeviceInfoLong(pDevicePointer,
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
    return getDeviceInfo(pDevicePointer,
                         CL.CL_DEVICE_OPENCL_C_VERSION);
  }

  @Override
  public String getDeviceExtensions(ClearCLPeerPointer pDevicePointer)
  {
    return getDeviceInfo(pDevicePointer, CL.CL_DEVICE_EXTENSIONS);
  }

  @Override
  public long getDeviceInfo(ClearCLPeerPointer pDevicePointer,
                            DeviceInfo pDeviceSpec)
  {
    switch (pDeviceSpec)
    {
    case MaxGlobalMemory:
      return getDeviceInfoLong(pDevicePointer,
                               CL.CL_DEVICE_GLOBAL_MEM_SIZE);
    case LocalMemSize:
      return getDeviceInfoLong(pDevicePointer,
                               CL.CL_DEVICE_LOCAL_MEM_SIZE);

    case MaxClockFreq:
      return getDeviceInfoInt(pDevicePointer,
                              CL.CL_DEVICE_MAX_CLOCK_FREQUENCY);
    case ComputeUnits:
      return getDeviceInfoInt(pDevicePointer,
                              CL.CL_DEVICE_MAX_COMPUTE_UNITS);
    case MaxMemoryAllocationSize:
      return getDeviceInfoLong(pDevicePointer,
                               CL.CL_DEVICE_MAX_MEM_ALLOC_SIZE);
    default:
      return -1;
    }

  }

  private String getDeviceInfo(ClearCLPeerPointer pDevicePointer,
                               int pInfoId)
  {
    return BackendUtils.checkExceptions(() -> {
      return Utils.getString((cl_device_id) pDevicePointer.getPointer(),
                             pInfoId);
    });
  }

  @Override
  public long getDeviceInfoLong(ClearCLPeerPointer pPointer,
                                int pInfoId)
  {
    return BackendUtils.checkExceptions(() -> {
      return Utils.getLong((cl_device_id) pPointer.getPointer(),
                           pInfoId);
    });
  }

  @Override
  public long getDeviceInfoInt(ClearCLPeerPointer pPointer,
                               int pInfoId)
  {
    return BackendUtils.checkExceptions(() -> {
      return Utils.getInt((cl_device_id) pPointer.getPointer(),
                          pInfoId);
    });
  }

  @Override
  public ClearCLPeerPointer getContextPeerPointer(ClearCLPeerPointer pPlatformPointer,
                                                  ClearCLPeerPointer... pDevicePointers)
  {
    return BackendUtils.checkExceptions(() -> {
      // Initialize the context properties
      cl_context_properties contextProperties =
                                              new cl_context_properties();
      contextProperties.addProperty(CL_CONTEXT_PLATFORM,
                                    (cl_platform_id) pPlatformPointer.getPointer());

      int lErrorCode[] = new int[1];

      cl_context context = clCreateContext(contextProperties,
                                           pDevicePointers.length,
                                           Utils.convertDevicePointers(pDevicePointers),
                                           null,
                                           null,
                                           lErrorCode);

      BackendUtils.checkOpenCLErrorCode(lErrorCode[0]);

      return new ClearCLPeerPointer(context);
    });
  }

  @Override
  public ClearCLPeerPointer getQueuePeerPointer(ClearCLPeerPointer pDevicePointer,
                                                ClearCLPeerPointer pContextPointer,
                                                boolean pInOrder)
  {
    return BackendUtils.checkExceptions(() -> {

      int lErrorCode[] = new int[1];

      @SuppressWarnings("deprecation")
      cl_command_queue commandQueue =
                                    clCreateCommandQueue((cl_context) pContextPointer.getPointer(),
                                                         (cl_device_id) pDevicePointer.getPointer(),
                                                         pInOrder ? 0
                                                                  : CL.CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE,
                                                         lErrorCode);

      BackendUtils.checkOpenCLErrorCode(lErrorCode[0]);

      ClearCLPeerPointer lCommandQueuePointer =
                                              new ClearCLPeerPointer(commandQueue);

      return lCommandQueuePointer;
    });
  }

  @Override
  public ClearCLPeerPointer getBufferPeerPointer(ClearCLPeerPointer pDevicePointer,
                                                 ClearCLPeerPointer pContextPointer,
                                                 MemAllocMode pMemAllocMode,
                                                 HostAccessType pHostAccessType,
                                                 KernelAccessType pKernelAccessType,
                                                 long pBufferSize)
  {
    return BackendUtils.checkExceptions(() -> {

      String lDeviceVersion = getDeviceVersion(pDevicePointer);

      boolean lOpenCL1p1o0 = (lDeviceVersion.contains("1.0")
                              || lDeviceVersion.contains("1.1"));

      long lMemFlags =
                     BackendUtils.getMemTypeFlags(pMemAllocMode,
                                                  pHostAccessType,
                                                  lOpenCL1p1o0 ? KernelAccessType.Undefined
                                                               : pKernelAccessType);

      int lErrorCode[] = new int[1];

      cl_mem lBufferPointer =
                            clCreateBuffer((cl_context) pContextPointer.getPointer(),
                                           lMemFlags,
                                           pBufferSize,
                                           null, // lPointer,
                                           lErrorCode);

      BackendUtils.checkOpenCLErrorCode(lErrorCode[0]);

      ClearCLPeerPointer lClearCLPointer =
                                         new ClearCLPeerPointer(lBufferPointer);
      return lClearCLPointer;
    });
  }

  @Override
  public ClearCLPeerPointer getImagePeerPointer(ClearCLPeerPointer pDevicePointer,
                                                ClearCLPeerPointer pContextPointer,
                                                MemAllocMode pMemAllocMode,
                                                HostAccessType pHostAccessType,
                                                KernelAccessType pKernelAccessType,
                                                ImageType pImageType,
                                                ImageChannelOrder pImageChannelOrder,
                                                ImageChannelDataType pImageChannelType,
                                                long... pDimensions)
  {
    String lDeviceVersion = getDeviceVersion(pDevicePointer);

    if (lDeviceVersion.contains("1.0")
        || lDeviceVersion.contains("1.1"))
    {
      cl_image_format lImageFormat = new cl_image_format();
      lImageFormat.image_channel_order =
                                       BackendUtils.getImageChannelOrderFlags(pImageChannelOrder);
      lImageFormat.image_channel_data_type =
                                           BackendUtils.getImageChannelDataTypeFlags(pImageChannelType);

      long image_width = (int) pDimensions[0];
      long image_height =
                        (int) (pDimensions.length < 2 ? 1
                                                      : pDimensions[1]);
      long image_depth =
                       (int) (pDimensions.length < 3 ? 1
                                                     : pDimensions[2]);

      long lMemFlags =
                     BackendUtils.getMemTypeFlags(pMemAllocMode,
                                                  pHostAccessType,
                                                  pKernelAccessType);

      int lErrorCode[] = new int[1];

      cl_mem lImageMem = null;

      if (pDimensions.length <= 2)
      {
        lImageMem =
                  CL.clCreateImage2D((cl_context) pContextPointer.getPointer(),
                                     lMemFlags,
                                     new cl_image_format[]
                                     { lImageFormat }, image_width, image_height, 0, null, lErrorCode);
      }
      else if (pDimensions.length == 3)
      {
        lImageMem =
                  CL.clCreateImage3D((cl_context) pContextPointer.getPointer(),
                                     lMemFlags,
                                     new cl_image_format[]
                                     { lImageFormat }, image_width, image_height, image_depth, 0, 0, null, lErrorCode);
      }

      BackendUtils.checkOpenCLErrorCode(lErrorCode[0]);

      ClearCLPeerPointer lClearCLPeerPointer =
                                             new ClearCLPeerPointer(lImageMem);

      return lClearCLPeerPointer;
    }
    if (lDeviceVersion.contains("1.2"))
    {

      cl_image_format lImageFormat = new cl_image_format();
      lImageFormat.image_channel_order =
                                       BackendUtils.getImageChannelOrderFlags(pImageChannelOrder);
      lImageFormat.image_channel_data_type =
                                           BackendUtils.getImageChannelDataTypeFlags(pImageChannelType);

      cl_image_desc lImageDescription = new cl_image_desc();
      lImageDescription.image_width = pDimensions[0];
      lImageDescription.image_height =
                                     pDimensions.length < 2 ? 1
                                                            : pDimensions[1];
      lImageDescription.image_depth =
                                    pDimensions.length < 3 ? 1
                                                           : pDimensions[2];
      lImageDescription.image_type =
                                   BackendUtils.getImageTypeFlags(pImageType);

      long lMemFlags =
                     BackendUtils.getMemTypeFlags(pMemAllocMode,
                                                  pHostAccessType,
                                                  pKernelAccessType);

      int lErrorCode[] = new int[1];

      cl_mem lImageMem =
                       CL.clCreateImage((cl_context) pContextPointer.getPointer(),
                                        lMemFlags,
                                        lImageFormat,
                                        lImageDescription,
                                        null,
                                        lErrorCode);

      BackendUtils.checkOpenCLErrorCode(lErrorCode[0]);

      ClearCLPeerPointer lClearCLPeerPointer =
                                             new ClearCLPeerPointer(lImageMem);

      return lClearCLPeerPointer;
    }

    return null;

  }

  @Override
  public ClearCLPeerPointer getProgramPeerPointer(ClearCLPeerPointer pContextPointer,
                                                  String... pSourceCode)
  {
    return BackendUtils.checkExceptions(() -> {

      int lErrorCode[] = new int[1];

      cl_program program =
                         clCreateProgramWithSource((cl_context) pContextPointer.getPointer(),
                                                   pSourceCode.length,
                                                   pSourceCode,
                                                   null,
                                                   null);

      BackendUtils.checkOpenCLErrorCode(lErrorCode[0]);

      ClearCLPeerPointer lClearCLPointer =
                                         new ClearCLPeerPointer(program);
      return lClearCLPointer;
    });
  }

  @Override
  public boolean buildProgram(ClearCLPeerPointer pProgramPointer,
                              String pOptions)
  {
    return BackendUtils.checkExceptions(() -> {
      int lError =
                 clBuildProgram((cl_program) pProgramPointer.getPointer(),
                                0,
                                null,
                                (pOptions == null
                                 || pOptions.isEmpty()) ? null
                                                        : pOptions,
                                null,
                                null);
      return lError == 0;
    });
  }

  @Override
  public BuildStatus getBuildStatus(ClearCLPeerPointer pDevicePointer,
                                    ClearCLPeerPointer pProgramPointer)
  {
    return BackendUtils.checkExceptions(() -> {
      int status[] = new int[1];
      BackendUtils.checkOpenCLError(CL.clGetProgramBuildInfo((cl_program) pProgramPointer.getPointer(),
                                                             (cl_device_id) pDevicePointer.getPointer(),
                                                             CL.CL_PROGRAM_BUILD_STATUS,
                                                             status.length * SizeOf.cl_int,
                                                             Pointer.to(status),
                                                             null));

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
    return getBuildProgramInfo(pDevicePointer,
                               pProgramPointer,
                               CL.CL_PROGRAM_BUILD_LOG);
  }

  private String getBuildProgramInfo(ClearCLPeerPointer pDevicePointer,
                                     ClearCLPeerPointer pProgramPointer,
                                     int pInfoName)
  {
    return BackendUtils.checkExceptions(() -> {
      // Obtain the length of the string that will be queried
      long size[] = new long[1];
      BackendUtils.checkOpenCLError(CL.clGetProgramBuildInfo((cl_program) pProgramPointer.getPointer(),
                                                             (cl_device_id) pDevicePointer.getPointer(),
                                                             pInfoName,
                                                             0,
                                                             null,
                                                             size));

      // Create a buffer of the appropriate size and fill it with the info
      byte buffer[] = new byte[(int) size[0]];
      BackendUtils.checkOpenCLError(CL.clGetProgramBuildInfo((cl_program) pProgramPointer.getPointer(),
                                                             (cl_device_id) pDevicePointer.getPointer(),
                                                             pInfoName,
                                                             buffer.length,
                                                             Pointer.to(buffer),
                                                             null));

      // Create a string from the buffer (excluding the trailing \0 byte)
      return new String(buffer, 0, buffer.length - 1);
    });

  }

  @Override
  public ClearCLPeerPointer getKernelPeerPointer(ClearCLPeerPointer pProgramPointer,
                                                 String pKernelName)
  {
    return BackendUtils.checkExceptions(() -> {

      int lErrorCode[] = new int[1];

      cl_kernel kernel =
                       clCreateKernel((cl_program) pProgramPointer.getPointer(),
                                      pKernelName,
                                      lErrorCode);
      BackendUtils.checkOpenCLErrorCode(lErrorCode[0]);

      ClearCLPeerPointer lClearCLPointer =
                                         new ClearCLPeerPointer(kernel);
      return lClearCLPointer;
    });
  }

  @Override
  public void setKernelArgument(ClearCLPeerPointer pKernelPeerPointer,
                                int pIndex,
                                Object pObject)
  {
    BackendUtils.checkExceptions(() -> {
      cl_kernel lKernelPointer =
                               (cl_kernel) pKernelPeerPointer.getPointer();

      // PRIMITIVE TYPES:
      if (pObject instanceof Byte)
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to(new byte[]
        { (byte) pObject })));
      else if (pObject instanceof Character)
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to(new char[]
        { (char) pObject })));
      else if (pObject instanceof Short)
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to(new short[]
        { (short) pObject })));
      else if (pObject instanceof Integer)
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to(new int[]
        { (int) pObject })));
      else if (pObject instanceof Long)
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to(new long[]
        { (long) pObject })));
      else if (pObject instanceof Float)
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to(new float[]
        { (float) pObject })));
      else if (pObject instanceof Double)
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to(new double[]
        { (double) pObject })));
      // ARRAY TYPES:
      else if (pObject instanceof byte[])
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to((byte[]) pObject)));
      else if (pObject instanceof char[])
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to((char[]) pObject)));
      else if (pObject instanceof short[])
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to((short[]) pObject)));
      else if (pObject instanceof int[])
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to((int[]) pObject)));
      else if (pObject instanceof long[])
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to((long[]) pObject)));
      else if (pObject instanceof float[])
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to((float[]) pObject)));
      else if (pObject instanceof double[])
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        Size.of(pObject.getClass()),
                                                        Pointer.to((double[]) pObject)));

      else if (pObject instanceof ClearCLBuffer)
      {
        ClearCLBuffer lClearCLBuffer = (ClearCLBuffer) pObject;
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        SizeOf.cl_mem,
                                                        Pointer.to(((cl_mem) lClearCLBuffer.getPeerPointer()
                                                                                           .getPointer()))));
      }
      else if (pObject instanceof ClearCLImage)
      {
        ClearCLImage lClearCLImage = (ClearCLImage) pObject;
        BackendUtils.checkOpenCLError(CL.clSetKernelArg(lKernelPointer,
                                                        pIndex,
                                                        SizeOf.cl_mem,
                                                        Pointer.to(((cl_mem) lClearCLImage.getPeerPointer()
                                                                                          .getPointer()))));
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
  public void enqueueKernelExecution(ClearCLPeerPointer pQueuePointer,
                                     ClearCLPeerPointer pKernelPointer,
                                     int pNumberOfDimension,
                                     long[] pGlobalOffsets,
                                     long[] pGlobalSizes,
                                     long[] pLocalSizes)
  {

    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(clEnqueueNDRangeKernel((cl_command_queue) pQueuePointer.getPointer(),
                                                           (cl_kernel) pKernelPointer.getPointer(),
                                                           pNumberOfDimension,
                                                           pGlobalOffsets,
                                                           pGlobalSizes,
                                                           pLocalSizes,
                                                           0,
                                                           null,
                                                           null));
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
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(clEnqueueReadBuffer((cl_command_queue) pQueuePointer.getPointer(),
                                                        (cl_mem) pBufferPointer.getPointer(),
                                                        pBlockingRead,
                                                        pOffsetInBuffer,
                                                        pLengthInBytes,
                                                        (Pointer) pHostMemPointer.getPointer(),
                                                        0,
                                                        null,
                                                        null));
    });
  }

  @Override
  public void enqueueWriteToBuffer(ClearCLPeerPointer pQueuePointer,
                                   ClearCLPeerPointer pBufferPointer,
                                   boolean pBlockingWrite,
                                   long pOffsetInBuffer,
                                   long pLengthInBytes,
                                   ClearCLPeerPointer pHostMemPointer)
  {
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(CL.clEnqueueWriteBuffer((cl_command_queue) pQueuePointer.getPointer(),
                                                            (cl_mem) pBufferPointer.getPointer(),
                                                            pBlockingWrite,
                                                            pOffsetInBuffer,
                                                            pLengthInBytes,
                                                            (Pointer) pHostMemPointer.getPointer(),
                                                            0,
                                                            null,
                                                            null));
    });
  }

  @Override
  public void enqueueReadFromBufferRegion(ClearCLPeerPointer pQueuePointer,
                                          ClearCLPeerPointer pBufferPointer,
                                          boolean pBlockingRead,
                                          long[] pBufferOrigin,
                                          long[] pHostOrigin,
                                          long[] pRegion,
                                          ClearCLPeerPointer pHostMemPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(CL.clEnqueueReadBufferRect((cl_command_queue) pQueuePointer.getPointer(),
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
                                                               null));
    });
  }

  @Override
  public void enqueueWriteToBufferRegion(ClearCLPeerPointer pQueuePointer,
                                         ClearCLPeerPointer pBufferPointer,
                                         boolean pBlockingWrite,
                                         long[] pBufferOrigin,
                                         long[] pHostOrigin,
                                         long[] pRegion,
                                         ClearCLPeerPointer pHostMemPointer)
  {
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(CL.clEnqueueWriteBufferRect((cl_command_queue) pQueuePointer.getPointer(),
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
                                                                null));
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
    BackendUtils.checkExceptions(() -> {

      Pointer lPatternPointer = Pointer.to(pPattern);

      BackendUtils.checkOpenCLError(CL.clEnqueueFillBuffer((cl_command_queue) pQueuePointer.getPointer(),
                                                           (cl_mem) pBufferPointer.getPointer(),
                                                           lPatternPointer,
                                                           pPattern.length,
                                                           pOffsetInBytes,
                                                           pLengthInBytes,
                                                           0,
                                                           null,
                                                           null));

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
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(CL.clEnqueueCopyBuffer((cl_command_queue) pQueuePointer.getPointer(),
                                                           (cl_mem) pSrcBufferPointer.getPointer(),
                                                           (cl_mem) pDstBufferPointer.getPointer(),
                                                           pSrcOffsetInBytes,
                                                           pDstOffsetInBytes,
                                                           pLengthToCopyInBytes,
                                                           0,
                                                           null,
                                                           null));

      if (pBlockingCopy)
        waitQueueToFinish(pQueuePointer);

    });
  }

  @Override
  public void enqueueCopyBufferRegion(ClearCLPeerPointer pQueuePointer,
                                      ClearCLPeerPointer pSrcBufferPointer,
                                      ClearCLPeerPointer pDstBufferPointer,
                                      boolean pBlockingCopy,
                                      long[] pSrcOrigin,
                                      long[] pDstOrigin,
                                      long[] pRegion)
  {
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(CL.clEnqueueCopyBufferRect((cl_command_queue) pQueuePointer.getPointer(),
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
                                                               null));

      if (pBlockingCopy)
        waitQueueToFinish(pQueuePointer);

    });
  }

  @Override
  public void enqueueCopyBufferToImage(ClearCLPeerPointer pQueuePointer,
                                       ClearCLPeerPointer pSrcBufferPointer,
                                       ClearCLPeerPointer pDstImagePointer,
                                       boolean pBlockingCopy,
                                       long pSrcOffsetInBytes,
                                       long[] pDstOrigin,
                                       long[] pDstRegion)
  {
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(CL.clEnqueueCopyBufferToImage((cl_command_queue) pQueuePointer.getPointer(),
                                                                  (cl_mem) pSrcBufferPointer.getPointer(),
                                                                  (cl_mem) pDstImagePointer.getPointer(),
                                                                  pSrcOffsetInBytes,
                                                                  pDstOrigin,
                                                                  pDstRegion,
                                                                  0,
                                                                  null,
                                                                  null));

      if (pBlockingCopy)
        waitQueueToFinish(pQueuePointer);

    });
  }

  @Override
  public void enqueueReadFromImage(ClearCLPeerPointer pQueuePointer,
                                   ClearCLPeerPointer pImagePointer,
                                   boolean pReadWrite,
                                   long[] pOrigin,
                                   long[] pRegion,
                                   ClearCLPeerPointer pHostMemPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(CL.clEnqueueReadImage((cl_command_queue) pQueuePointer.getPointer(),
                                                          (cl_mem) pImagePointer.getPointer(),
                                                          pReadWrite,
                                                          pOrigin,
                                                          pRegion,
                                                          0,
                                                          0,
                                                          (Pointer) pHostMemPointer.getPointer(),
                                                          0,
                                                          null,
                                                          null));
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
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(CL.clEnqueueWriteImage((cl_command_queue) pQueuePointer.getPointer(),
                                                           (cl_mem) pImagePointer.getPointer(),
                                                           true,
                                                           pOrigin,
                                                           pRegion,
                                                           0,
                                                           0,
                                                           (Pointer) pHostMemPointer.getPointer(),
                                                           0,
                                                           null,
                                                           null));
    });
  }

  @Override
  public void enqueueFillImage(ClearCLPeerPointer pQueuePointer,
                               ClearCLPeerPointer pImagePointer,
                               boolean pBlockingFill,
                               long[] pOrigin,
                               long[] pRegion,
                               byte[] pColor)
  {
    BackendUtils.checkExceptions(() -> {

      Pointer lColorPointer = Pointer.to(pColor);

      BackendUtils.checkOpenCLError(CL.clEnqueueFillImage((cl_command_queue) pQueuePointer.getPointer(),
                                                          (cl_mem) pImagePointer.getPointer(),
                                                          lColorPointer,
                                                          pOrigin,
                                                          pRegion,
                                                          0,
                                                          null,
                                                          null));

      if (pBlockingFill)
        waitQueueToFinish(pQueuePointer);

    });
  }

  @Override
  public void enqueueCopyImage(ClearCLPeerPointer pQueuePointer,
                               ClearCLPeerPointer pSrcBImagePointer,
                               ClearCLPeerPointer pDstImagePointer,
                               boolean pBlockingCopy,
                               long[] pSrcOrigin,
                               long[] pDstOrigin,
                               long[] pRegion)
  {
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(CL.clEnqueueCopyImage((cl_command_queue) pQueuePointer.getPointer(),
                                                          (cl_mem) pSrcBImagePointer.getPointer(),
                                                          (cl_mem) pDstImagePointer.getPointer(),
                                                          pSrcOrigin,
                                                          pDstOrigin,
                                                          pRegion,
                                                          0,
                                                          null,
                                                          null));

      if (pBlockingCopy)
        waitQueueToFinish(pQueuePointer);

    });
  }

  @Override
  public void enqueueCopyImageToBuffer(ClearCLPeerPointer pQueuePointer,
                                       ClearCLPeerPointer pSrcImagePointer,
                                       ClearCLPeerPointer pDstBufferPointer,
                                       boolean pBlockingCopy,
                                       long[] pSrcOrigin,
                                       long[] pSrcRegion,
                                       long pDstOffset)
  {
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(CL.clEnqueueCopyImageToBuffer((cl_command_queue) pQueuePointer.getPointer(),
                                                                  (cl_mem) pSrcImagePointer.getPointer(),
                                                                  (cl_mem) pDstBufferPointer.getPointer(),
                                                                  pSrcOrigin,
                                                                  pSrcRegion,
                                                                  pDstOffset,
                                                                  0,
                                                                  null,
                                                                  null));

      if (pBlockingCopy)
        waitQueueToFinish(pQueuePointer);

    });
  }

  @Override
  public ClearCLPeerPointer wrap(Buffer pBuffer)
  {
    return BackendUtils.checkExceptions(() -> {
      Pointer lPointer = Pointer.to(pBuffer);
      ClearCLPeerPointer lPeerPointer =
                                      new ClearCLPeerPointer(lPointer);
      return lPeerPointer;
    });
  }

  @Override
  public ClearCLPeerPointer wrap(ContiguousMemoryInterface pContiguousMemory)
  {
    return BackendUtils.checkExceptions(() -> {
      ByteBuffer lByteBuffer = pContiguousMemory.getByteBuffer();
      return wrap(lByteBuffer);
    });
  }

  @Override
  public ClearCLPeerPointer wrap(FragmentedMemoryInterface pFragmentedMemory)
  {
    throw new ClearCLUnsupportedException("fragmented buffers not supported.");
  }

  @Override
  public void releaseBuffer(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(clReleaseMemObject((cl_mem) pPeerPointer.getPointer()));
    });
  }

  @Override
  public void releaseContext(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(clReleaseContext((cl_context) pPeerPointer.getPointer()));
    });
  }

  @Override
  public void releaseDevice(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(CL.clReleaseDevice((cl_device_id) pPeerPointer.getPointer()));
    });

  }

  @Override
  public void releaseImage(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(clReleaseMemObject((cl_mem) pPeerPointer.getPointer()));
    });
  }

  @Override
  public void releaseKernel(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(clReleaseKernel((cl_kernel) pPeerPointer.getPointer()));
    });
  }

  @Override
  public void releaseProgram(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(clReleaseProgram((cl_program) pPeerPointer.getPointer()));
    });
  }

  @Override
  public void releaseQueue(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(clReleaseCommandQueue((cl_command_queue) pPeerPointer.getPointer()));
    });
  }

  @Override
  public void waitQueueToFinish(ClearCLPeerPointer pQueuePointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(CL.clFinish((cl_command_queue) pQueuePointer.getPointer()));
    });
  }

}
