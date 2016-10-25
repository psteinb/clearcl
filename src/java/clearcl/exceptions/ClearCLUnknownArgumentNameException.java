package clearcl.exceptions;

import clearcl.ClearCLKernel;

/**
 * Exception thrown when unknown argument name provided.
 *
 * @author royer
 */
public class ClearCLUnknownArgumentNameException extends
                                                ClearCLException
{

  private static final long serialVersionUID = 1L;

  public ClearCLUnknownArgumentNameException(ClearCLKernel pKernel,
                                             String pArgumentName,
                                             Object pObject)
  {
    super(String.format("Argument name unknow: '%s' for object: %s in kernel: '%s'",
                        pArgumentName,
                        pObject.getClass().getName(),
                        pKernel.getName()));
  }

}
