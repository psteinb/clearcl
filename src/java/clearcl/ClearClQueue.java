package clearcl;

import clearcl.abs.ClearCLBase;

/**
 * ClearCLQueue is the ClearCL abstraction for OpenCl queues.
 *
 * @author royer
 */
public class ClearCLQueue extends ClearCLBase
{

  /**
   * This constructor is called internally from an OpenCl context.
   * 
   * @param pClearCLContext
   * @param pQueuePointer
   */
  public ClearCLQueue(ClearCLContext pClearCLContext,
                      ClearCLPeerPointer pQueuePointer)
  {
    super(pClearCLContext.getBackend(), pQueuePointer);
  }

  /**
   * Waits for queue to finish enqueued tasks (such as: kernel execution, buffer
   * and image copies, writes and reads).
   */
  public void waitToFinish()
  {
    getBackend().waitQueueToFinish(getPeerPointer());
  }

  /* (non-Javadoc)
   * @see clearcl.ClearCLBase#close()
   */
  @Override
  public void close()
  {
    getBackend().releaseQueue(getPeerPointer());
  }

}
