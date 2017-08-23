package clearcl;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import clearcl.abs.ClearCLBase;
import clearcl.exceptions.ClearCLArgumentMissingException;
import clearcl.exceptions.ClearCLException;
import clearcl.exceptions.ClearCLInvalidExecutionRange;
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

    public Argument(final Object pObject)
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
  private final ConcurrentHashMap<Integer, Boolean> mUpdatedArgumentsMap =
                                                                         new ConcurrentHashMap<>();

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
  ClearCLKernel(final ClearCLContext pClearCLContext,
                final ClearCLProgram pClearCLProgram,
                final ClearCLPeerPointer pKernelPointer,
                final String pKernelName,
                final String pSourceCode)
  {
    super(pClearCLProgram.getBackend(), pKernelPointer);
    mClearCLContext = pClearCLContext;
    mClearCLProgram = pClearCLProgram;
    mName = pKernelName;
    mSourceCode = pSourceCode;

    mNameToIndexMap = getKernelIndexMap(pKernelName);
    mDefaultArgumentsMap = getKernelDefaultArgumentsMap(pKernelName);
  }

  /**
   * Returns kernel name
   * 
   * @return kernel name
   */
  public String getName()
  {
    return mName;
  }

  /**
   * Returns source code
   * 
   * @return source code
   */
  public String getSourceCode()
  {
    return mSourceCode;
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
  public void setGlobalOffsets(final long... pGlobalOffsets)
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
  public void setGlobalSizes(final long... pGlobalSizes)
  {
    mGlobalSizes = pGlobalSizes;
  }

  /**
   * Sets the global sizes to the dimensions of a image (1D, 2D, or 3D)
   * 
   * @param pImage
   *          image
   */
  public void setGlobalSizes(final ClearCLImageInterface pImage)
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
  public void setLocalSizes(final long... pLocalSizes)
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
  public void setArguments(final Object... pArguments)
  {
    int i = 0;
    for (final Object lObject : pArguments)
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
  public void setArgument(final int pIndex, final Object pObject)
  {
    mIndexToArgumentMap.put(pIndex, new Argument(pObject));

  }

  /**
   * Sets argument for a given argument name. If argument is unknown for kernel,
   * an exception is thrown.
   * 
   * @param pArgumentName
   *          argument name
   * @param pObject
   *          argument
   */
  public void setArgument(final String pArgumentName,
                          final Object pObject)
  {
    final Integer lArgumentIndex = mNameToIndexMap.get(pArgumentName);

    if (lArgumentIndex == null)
      throw new ClearCLUnknownArgumentNameException(this,
                                                    pArgumentName,
                                                    pObject);
    Argument lExistingArgumentValue =
                                    mIndexToArgumentMap.get(lArgumentIndex);

    if (lExistingArgumentValue == null)
    {
      mUpdatedArgumentsMap.put(lArgumentIndex, true);
    }
    else
    {
      if (lExistingArgumentValue.argument == pObject
          || lExistingArgumentValue.argument.equals(pObject))
      {
        mUpdatedArgumentsMap.put(lArgumentIndex, false);
      }
      else
      {
        mUpdatedArgumentsMap.put(lArgumentIndex, true);
      }

    }

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
  public void setLocalMemoryArgument(final String pArgumentName,
                                     final NativeTypeEnum pNativeTypeEnum,
                                     final long pNumberOfElements)
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
  public boolean setOptionalArgument(final String pArgumentName,
                                     final Object pObject)
  {
    final Integer lArgumentIndex = mNameToIndexMap.get(pArgumentName);

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
    // System.out.println("kernel:" + this.toString());

    for (final Map.Entry<String, Integer> lEntry : mNameToIndexMap.entrySet())
    {
      final String lArgumentName = lEntry.getKey();
      final Integer lArgumentIndex = lEntry.getValue();

      if (mUpdatedArgumentsMap.get(lArgumentIndex) == null
          || mUpdatedArgumentsMap.get(lArgumentIndex))
        try
        {

          Argument lArgument =
                             mIndexToArgumentMap.get(lArgumentIndex);

          /*System.out.format("index: %d, arg name: %s, arg: '%s' \n",
                          lArgumentIndex,
                          lArgumentName,
                          lArgument==null?"default~"+mDefaultArgumentsMap.get(lArgumentName):lArgument.argument);/**/

          if (lArgument == null)
          {
            final Number lDefaultValue =
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
            // TODO: why do we need lLocalMemory for ?
            @SuppressWarnings("unused")
            final ClearCLLocalMemory lLocalMemory =
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
        catch (final Throwable e)
        {
          throw new ClearCLException(String.format("problem while setting argument '%s' at index %d \n",
                                                   lArgumentName,
                                                   lArgumentIndex),
                                     e);
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
   * @param pWaitToFinish
   *          if true the call is blocking, false otherwise
   */
  public void run(final boolean pWaitToFinish)
  {
    run(mClearCLContext.getDefaultQueue(), pWaitToFinish);
  }

  /**
   * Executes kernel for current set of arguments on provided queue. IMPORTANT:
   * about blocking calls: there is a cost associated to waiting for a kernel to
   * finish... If you execute several kernels in the same queue, you do no need
   * to wait.
   * 
   * @param pClearCLQueue
   *          queue
   * @param pWaitToFinish
   *          if true the call is blocking, false otherwise
   */
  public void run(final ClearCLQueue pClearCLQueue,
                  final boolean pWaitToFinish)
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

                          if (pWaitToFinish)
                            pClearCLQueue.waitToFinish();

                        });

  }

  @Override
  public String toString()
  {
    return String.format("ClearCLKernel [mName=%s, mClearCLProgram=%s]",
                         mName,
                         mClearCLProgram);
  }

  /**
   * Returns the map of defaults arguments
   * 
   * @param pKernelName
   *          kernel name
   * @return default args map
   */
  private ConcurrentHashMap<String, Number> getKernelDefaultArgumentsMap(final String pKernelName)
  {
    final ConcurrentHashMap<String, Number> lNameToDefaultArgumentMapMap =
                                                                         new ConcurrentHashMap<String, Number>();

    final String lSourceCode = getSourceCode();

    int lBeginOfDefault = 0;
    while ((lBeginOfDefault =
                            lSourceCode.indexOf("//default "
                                                + pKernelName,
                                                lBeginOfDefault)) != -1)
    {
      final int lEndOfDefault = lSourceCode.indexOf('\n',
                                                    lBeginOfDefault);
      final String lSubStringKernel =
                                    lSourceCode.substring(lBeginOfDefault,
                                                          lEndOfDefault);

      // System.out.println(lSubStringKernel);

      final String[] lTwoPointsAndEqualSplit =
                                             lSubStringKernel.split("(\\s|=)+");
      // System.out.println(Arrays.toString(lTwoPointsAndEqualSplit));

      final String lArgumentName =
                                 lTwoPointsAndEqualSplit[2].trim()
                                                           .toLowerCase();
      String lArgumentValue =
                            lTwoPointsAndEqualSplit[3].trim()
                                                      .toLowerCase();

      final char lArgumentType =
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

  private ConcurrentHashMap<String, Integer> getKernelIndexMap(final String pKernelName)
  {
    final ConcurrentHashMap<String, Integer> lNameToIndexMap =
                                                             new ConcurrentHashMap<String, Integer>();

    final String[] lKernelSignature = getKernelSignature(pKernelName);

    int i = 0;
    for (final String lArgumentEntry : lKernelSignature)
    {
      final String[] lSplit = lArgumentEntry.split("[*\\s]+");
      // System.out.println(Arrays.toString(lSplit));
      final String lArgumentName = lSplit[lSplit.length - 1];
      lNameToIndexMap.put(lArgumentName, i);

      i++;
    }

    return lNameToIndexMap;
  }

  private String[] getKernelSignature(final String pKernelName)
  {
    final String lSourceCode = getSourceCode();
    {
      Pattern lPattern = Pattern.compile("[\n\r\\s]+(" + pKernelName
                                         + ")[\n\r\\s(]+");

      Matcher lMatcher = lPattern.matcher(lSourceCode);

      /*final int lBeginOfKernelSignature =
                                        max(lSourceCode.indexOf(pKernelName+" "),
                                            lSourceCode.indexOf(pKernelName+"("));/**/
      // if (lBeginOfKernelSignature >= 0)
      if (lMatcher.find())
      {
        int lBeginOfKernelSignature = lMatcher.start(1);
        final int lEndOfKernelSignature =
                                        lSourceCode.indexOf('{',
                                                            lBeginOfKernelSignature);
        final String lSubStringKernel =
                                      lSourceCode.substring(lBeginOfKernelSignature,
                                                            lEndOfKernelSignature);

        final String lSubStringSignature =
                                         lSubStringKernel.substring(lSubStringKernel.indexOf('(')
                                                                    + 1,
                                                                    lSubStringKernel.indexOf(')'));

        // System.out.println("[[[" + lSubStringSignature + "]]]");

        final String[] lKernelSignature =
                                        lSubStringSignature.split(",",
                                                                  -1);

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
   * @param pLogExecutionTime
   *          true for logging
   */
  public void setLogExecutionTime(final boolean pLogExecutionTime)
  {
    mLogExecutiontime = pLogExecutionTime;
  }

  /* (non-Javadoc)
   * @see clearcl.ClearCLBase#close()
   */
  @Override
  public void close()
  {
    if (getPeerPointer() != null)
    {
      getBackend().releaseKernel(getPeerPointer());
      setPeerPointer(null);
    }
  }

}
