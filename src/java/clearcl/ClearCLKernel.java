package clearcl;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import clearcl.abs.ClearCLBase;
import clearcl.exceptions.ClearCLInvalidExecutionRange;
import clearcl.exceptions.ClearCLArgumentMissingException;
import clearcl.exceptions.ClearCLUnknownArgumentNameException;
import clearcl.interfaces.ClearCLImageInterface;
import clearcl.util.ElapsedTime;
import coremem.enums.NativeTypeEnum;

/**
 * ClearCLKernel is the ClearCL abstraction for OpenCL kernels.
 *
 * @author royer
 */
public class ClearCLKernel extends ClearCLBase implements Runnable
{

  private class Argument
  {
    public Object argument;

    public Argument(Object pObject)
    {
      argument = pObject;
    }
  }

  private final ClearCLContext mClearCLContext;
  private final ClearCLProgram mClearCLProgram;
  private final String mName;
  private final String mSourceCode;

  private final ConcurrentHashMap<String, Integer> mNameToIndexMap;
  private final ConcurrentHashMap<Integer, Argument> mIndexToArgumentMap =
                                                                         new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Number> mDefaultArgumentsMap;

  private long[] mGlobalOffsets = new long[]
  { 0, 0, 0 };
  private long[] mGlobalSizes = null;
  private long[] mLocalSizes = null;
  private boolean mLogExecutiontime = true;

  /**
   * This constructor is called internally from an OpenCl program.
   * 
   * @param pClearCLContext
   *          context
   * @param pClearCLProgram
   *          program
   * @param pKernelPointer
   *          kernel peer pointer
   * @param pKernelName
   *          kernel name
   * @param pSourceCode
   */
  ClearCLKernel(ClearCLContext pClearCLContext,
                ClearCLProgram pClearCLProgram,
                ClearCLPeerPointer pKernelPointer,
                String pKernelName,
                String pSourceCode)
  {
    super(pClearCLProgram.getBackend(), pKernelPointer);
    mClearCLContext = pClearCLContext;
    mClearCLProgram = pClearCLProgram;
    mName = pKernelName;
    mSourceCode = pSourceCode;

    mNameToIndexMap = getKernelIndexMap(pKernelName);
    mDefaultArgumentsMap = getKernelDefaultArgumentsMap(pKernelName);
  }

  public String getName()
  {
    return mName;
  }

  /**
   * Return global offsets.
   * 
   * @return global offsets
   */
  public long[] getGlobalOffsets()
  {
    return mGlobalOffsets;
  }

  /**
   * Sets the global offsets.
   * 
   * @param pGlobalOffsets
   *          new global offsets
   */
  public void setGlobalOffsets(long... pGlobalOffsets)
  {
    mGlobalOffsets = pGlobalOffsets;
  }

  /**
   * returns the global sizes
   * 
   * @return global sizes
   */
  public long[] getGlobalSizes()
  {
    return mGlobalSizes;
  }

  /**
   * Sets the global sizes
   * 
   * @param pGlobalSizes
   *          global sizes
   */
  public void setGlobalSizes(long... pGlobalSizes)
  {
    mGlobalSizes = pGlobalSizes;
  }

  /**
   * Sets the global sizes to the dimensions of a image (1D, 2D, or 3D)
   * 
   * @param pImage
   *          image
   */
  public void setGlobalSizes(ClearCLImageInterface pImage)
  {
    mGlobalSizes = pImage.getDimensions();
  }

  /**
   * Returns the local sizes.
   * 
   * @return local sizes
   */
  public long[] getLocalSizes()
  {
    return mLocalSizes;
  }

  /**
   * Sets the local sizes.
   * 
   * @param pLocalSizes
   *          local sizes
   */
  public void setLocalSizes(long... pLocalSizes)
  {
    mLocalSizes = pLocalSizes;
  }

  /**
   * Clears arguments.
   */
  public void clearArguments()
  {
    mIndexToArgumentMap.clear();
  }

  /**
   * Sets the kernel arguments for the next kernel run.
   * 
   * @param pArguments
   *          list of arguments
   */
  public void setArguments(Object... pArguments)
  {
    int i = 0;
    for (Object lObject : pArguments)
    {
      setArgument(i, lObject);
      i++;
    }
  }

  /**
   * Sets argument for a given argument index.
   * 
   * @param pIndex
   *          argument index
   * @param pObject
   *          argument
   */
  public void setArgument(int pIndex, Object pObject)
  {
    mIndexToArgumentMap.put(pIndex, new Argument(pObject));

  }

  /**
   * Sets argument for a given argument name. If argument is unknown for kernel,
   * an exception is thrown.
   * 
   * @param pIndex
   *          argument name
   * @param pObject
   *          argument
   */
  public void setArgument(String pArgumentName, Object pObject)
  {
    Integer lArgumentIndex = mNameToIndexMap.get(pArgumentName);

    if (lArgumentIndex == null)
      throw new ClearCLUnknownArgumentNameException(this,
                                                    pArgumentName,
                                                    pObject);

    mIndexToArgumentMap.put(lArgumentIndex, new Argument(pObject));

  }

  /**
   * Sets a local memory argument for the kernel.
   * 
   * @param pArgumentName
   *          argument name
   * @param pNativeTypeEnum
   *          native type for elements
   * @param pNumberOfElements
   *          number of elements
   */
  public void setLocalMemoryArgument(String pArgumentName,
                                     NativeTypeEnum pNativeTypeEnum,
                                     long pNumberOfElements)
  {
    setArgument(pArgumentName,
                new ClearCLLocalMemory(pNativeTypeEnum,
                                       pNumberOfElements));
  }

  /**
   * Sets optional argument for a given argument name.
   * 
   * @param pArgumentName
   *          argument name
   * @param pObject
   *          argument
   * @return true is argument set, false if argument unknown
   */
  public boolean setOptionalArgument(String pArgumentName,
                                     Object pObject)
  {
    Integer lArgumentIndex = mNameToIndexMap.get(pArgumentName);

    if (lArgumentIndex == null)
      return false;

    mIndexToArgumentMap.put(lArgumentIndex, new Argument(pObject));
    return true;
  }

  /**
   * Sets the arguments on the OpenCL side
   */
  private void setArgumentsInternal()
  {
    for (Map.Entry<String, Integer> lEntry : mNameToIndexMap.entrySet())
    {
      String lArgumentName = lEntry.getKey();
      Integer lArgumentIndex = lEntry.getValue();

      Argument lArgument = mIndexToArgumentMap.get(lArgumentIndex);

      if (lArgument == null)
      {
        Number lDefaultValue =
                             mDefaultArgumentsMap.get(lArgumentName);
        if (lDefaultValue != null)
          lArgument = new Argument(lDefaultValue);
      }

      if (lArgument == null)
        throw new ClearCLArgumentMissingException(this,
                                                  lArgumentName,
                                                  lArgumentIndex);

      if (lArgument.argument instanceof ClearCLLocalMemory)
      {
        ClearCLLocalMemory lLocalMemory =
                                        (ClearCLLocalMemory) lArgument.argument;
        getBackend().setKernelArgument(this.getPeerPointer(),
                                       lArgumentIndex,
                                       lArgument.argument);/**/

      }
      else
      {
        getBackend().setKernelArgument(this.getPeerPointer(),
                                       lArgumentIndex,
                                       lArgument.argument);/**/
      }
    }

  }

  /**
   * Executes kernel for current set of arguments on default queue (blocking
   * call until kernel finishes).
   * 
   */
  @Override
  public void run()
  {
    run(mClearCLContext.getDefaultQueue(), true);
  }

  /**
   * Executes kernel for current set of arguments on default queue.
   * 
   * @param pBlockingRun
   *          if true the call is blocking, false otherwise
   */
  public void run(boolean pBlockingRun)
  {
    run(mClearCLContext.getDefaultQueue(), pBlockingRun);
  }

  /**
   * Executes kernel for current set of arguments on provided queue. IMPORTANT:
   * about blocking calls: there is a cost associated to waiting for a kernel to
   * finish... If you execute several kernels in the same queue, you do no need
   * to wait.
   * 
   * @param pClearCLQueue
   *          queue
   * @param pBlockingRun
   *          if true the call is blocking, false otherwise
   */
  public void run(ClearCLQueue pClearCLQueue, boolean pBlockingRun)
  {

    ElapsedTime.measure(isLogExecutionTime(),
                        "kernel " + getName(),
                        () -> {

                          setArgumentsInternal();
                          if (getGlobalSizes() == null
                              || getGlobalOffsets() == null)
                            throw new ClearCLInvalidExecutionRange(String.format("global offset = %s, global range = %s, local range = %s",
                                                                                 Arrays.toString(getGlobalOffsets()),
                                                                                 Arrays.toString(getGlobalSizes()),
                                                                                 Arrays.toString(getLocalSizes())));

                          getBackend().enqueueKernelExecution(pClearCLQueue.getPeerPointer(),
                                                              getPeerPointer(),
                                                              getGlobalSizes().length,
                                                              getGlobalOffsets(),
                                                              getGlobalSizes(),
                                                              getLocalSizes());

                          // for (Map.Entry<Integer, Argument> lEntry :
                          // mIndexToArgumentMap.entrySet())
                          // {
                          // Object lArgument = lEntry.getValue().argument;
                          // if (lArgument instanceof ClearCLMemInterface)
                          // {
                          // ClearCLMemInterface lClearCLMemInterface =
                          // (ClearCLMemInterface)
                          // lArgument;
                          //
                          // if (lClearCLMemInterface.getHostAccessType()
                          // .isReadableFromHost())
                          // {
                          // AsynchronousNotification.notifyChange(() -> {
                          // pClearCLQueue.waitToFinish();
                          // lClearCLMemInterface.notifyListenersOfChange(mClearCLContext.getDefaultQueue());
                          // });
                          // }
                          // }
                          // }

                          if (pBlockingRun)
                            pClearCLQueue.waitToFinish();

                        });

  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return String.format("ClearCLKernel [mClearCLProgram=%s, mName=%s]",
                         mClearCLProgram,
                         mName);
  }

  public String getSourceCode()
  {
    return mSourceCode;
  }

  public ConcurrentHashMap<String, Number> getKernelDefaultArgumentsMap(String pKernelName)
  {
    ConcurrentHashMap<String, Number> lNameToDefaultArgumentMapMap =
                                                                   new ConcurrentHashMap<String, Number>();

    String lSourceCode = getSourceCode();

    int lBeginOfDefault = 0;
    while ((lBeginOfDefault =
                            lSourceCode.indexOf("//default "
                                                + pKernelName,
                                                lBeginOfDefault)) != -1)
    {
      int lEndOfDefault = lSourceCode.indexOf('\n', lBeginOfDefault);
      String lSubStringKernel = lSourceCode.substring(lBeginOfDefault,
                                                      lEndOfDefault);

      // System.out.println(lSubStringKernel);

      String[] lTwoPointsAndEqualSplit =
                                       lSubStringKernel.split("(\\s|=)+");
      // System.out.println(Arrays.toString(lTwoPointsAndEqualSplit));

      String lArgumentName = lTwoPointsAndEqualSplit[2].trim()
                                                       .toLowerCase();
      String lArgumentValue =
                            lTwoPointsAndEqualSplit[3].trim()
                                                      .toLowerCase();

      char lArgumentType =
                         lArgumentValue.charAt(lArgumentValue.length()
                                               - 1);

      lArgumentValue = lArgumentValue.substring(0,
                                                lArgumentValue.length()
                                                   - 1);

      switch (lArgumentType)
      {
      case 'b':
        lNameToDefaultArgumentMapMap.put(lArgumentName,
                                         Byte.parseByte(lArgumentValue));
        break;

      case 's':
        lNameToDefaultArgumentMapMap.put(lArgumentName,
                                         Short.parseShort(lArgumentValue));
        break;

      case 'i':
        lNameToDefaultArgumentMapMap.put(lArgumentName,
                                         Integer.parseInt(lArgumentValue));
        break;

      case 'l':
        lNameToDefaultArgumentMapMap.put(lArgumentName,
                                         Long.parseLong(lArgumentValue));
        break;

      case 'f':
        lNameToDefaultArgumentMapMap.put(lArgumentName,
                                         Float.parseFloat(lArgumentValue));
        break;

      case 'd':
        lNameToDefaultArgumentMapMap.put(lArgumentName,
                                         Double.parseDouble(lArgumentValue));
        break;
      }

      lBeginOfDefault = lEndOfDefault;
    }

    // System.out.println(lNameToDefaultArgumentMapMap);

    return lNameToDefaultArgumentMapMap;
  }

  public ConcurrentHashMap<String, Integer> getKernelIndexMap(String pKernelName)
  {
    ConcurrentHashMap<String, Integer> lNameToIndexMap =
                                                       new ConcurrentHashMap<String, Integer>();

    String[] lKernelSignature = getKernelSignature(pKernelName);

    int i = 0;
    for (String lArgumentEntry : lKernelSignature)
    {
      String[] lSplit = lArgumentEntry.split("[*\\s]+");
      // System.out.println(Arrays.toString(lSplit));
      String lArgumentName = lSplit[lSplit.length - 1];
      lNameToIndexMap.put(lArgumentName, i);

      i++;
    }

    return lNameToIndexMap;
  }

  public String[] getKernelSignature(String pKernelName)
  {
    String lSourceCode = getSourceCode();
    {
      int lBeginOfKernelSignature = lSourceCode.indexOf(pKernelName);
      if (lBeginOfKernelSignature >= 0)
      {
        int lEndOfKernelSignature =
                                  lSourceCode.indexOf('{',
                                                      lBeginOfKernelSignature);
        String lSubStringKernel =
                                lSourceCode.substring(lBeginOfKernelSignature,
                                                      lEndOfKernelSignature);

        String lSubStringSignature =
                                   lSubStringKernel.substring(lSubStringKernel.indexOf('(')
                                                              + 1,
                                                              lSubStringKernel.indexOf(')'));

        // System.out.println("[[[" + lSubStringSignature + "]]]");

        String[] lKernelSignature =
                                  lSubStringSignature.split(",", -1);

        for (int i = 0; i < lKernelSignature.length; i++)
          lKernelSignature[i] = lKernelSignature[i].trim();

        // System.out.println(Arrays.toString(lKernelSignature));
        return lKernelSignature;
      }
    }
    return null;
  }

  /**
   * Returns true if execution times for this kernel should be logged.
   * 
   * @return true if logging on.
   */
  public boolean isLogExecutionTime()
  {
    return mLogExecutiontime;
  }

  /**
   * Sets whether this kernel should log its execution time.
   * 
   * @param plogExecutionTime
   */
  public void setLogExecutionTime(boolean plogExecutionTime)
  {
    mLogExecutiontime = plogExecutionTime;
  }

  /* (non-Javadoc)
   * @see clearcl.ClearCLBase#close()
   */
  @Override
  public void close()
  {
    getBackend().releaseKernel(getPeerPointer());
    setPeerPointer(null);
  }

}
