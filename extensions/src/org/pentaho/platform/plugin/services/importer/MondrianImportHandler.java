package org.pentaho.platform.plugin.services.importer;

/**
 * Used by REST Services to handle mulit part form upload from Schema WorkBench 
 * 
 * @author tband
 * @date 6/27/12
 * 
 */
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle.Builder;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryImporter;
import org.pentaho.platform.repository.messages.Messages;

public class MondrianImportHandler implements IPlatformImportHandler {

  private static final String DOMAIN_ID = "domain-id";

  private static final String MONDRIAN_MIME_TYPE = "application/vnd.pentaho.mondrian+xml";

  private static final String UTF_8 = "UTF-8";

  private static final Log logger = LogFactory.getLog(MondrianImportHandler.class);

  private static final Messages messages = Messages.getInstance();

  IPentahoMetadataDomainRepositoryImporter metadataRepositoryImporter;

  public MondrianImportHandler(final IPentahoMetadataDomainRepositoryImporter metadataImporter) {
    if (metadataImporter == null) {
      throw new IllegalArgumentException();
    }
    this.metadataRepositoryImporter = metadataImporter;
  }

  /**
   * Override function to pass in the input stream and name then create a bundle and importFile
   * @param dataInputStream
   * @param domainId
   * @throws PlatformImportException
   * @throws IOException 
   * @throws DomainStorageException 
   * @throws DomainAlreadyExistsException 
   * @throws DomainIdNullException 
   */
  public void importSchema(InputStream dataInputStream, String domainId, boolean overwriteInRepossitory)
      throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException, IOException {
    String mimeType = this.MONDRIAN_MIME_TYPE;//should we pass this in??
    IPlatformImportBundle bundle = fileIImportBundle(dataInputStream, domainId, mimeType, overwriteInRepossitory);
    logger.debug("importSchema start " + domainId);

    this.importFile(bundle, overwriteInRepossitory);
  }

  /**
   * Utility to build a bundle from the data input stream
   * @param dataInputStream
   * @param domainId
   * @param overwriteInRepossitory
   * @return
   */
  public IPlatformImportBundle fileIImportBundle(InputStream dataInputStream, String domainId, String mimeType,
      boolean overwriteInRepossitory) {
    RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder().input(dataInputStream)
        .charSet(UTF_8).hidden(false).mime(mimeType).withParam(DOMAIN_ID, domainId).overwrite(overwriteInRepossitory);
    logger.debug("fileIImportBundle start " + domainId);
    return (IPlatformImportBundle) bundleBuilder.build();

  }

  /**
   * overloaded method from original - default to false (do not overwrite)
   * @throws IOException 
   * @throws DomainStorageException 
   * @throws DomainAlreadyExistsException 
   * @throws DomainIdNullException 
   */
  public void importFile(IPlatformImportBundle bundle) throws PlatformImportException, DomainIdNullException,
      DomainAlreadyExistsException, DomainStorageException, IOException {
    this.importFile(bundle, false);
  }

  public void importFile(IPlatformImportBundle bundle, boolean overwriteInRepossitory) throws DomainIdNullException,
      DomainAlreadyExistsException, DomainStorageException, IOException {
    logger.debug("importFile start " + bundle.getName() + " overwriteInRepossitory:" + overwriteInRepossitory);
    final String domainId = (String) bundle.getProperty("domain-id");

    if (domainId == null) {
      throw new DomainIdNullException("Bundle missing required domain-id property");
    }

    logger.debug("Importing as metadata - [domain=" + domainId + "]");
    metadataRepositoryImporter.storeDomain(bundle.getInputStream(), domainId, overwriteInRepossitory);
  }

  /**
   * remove the domainId from the Jcr
   * @param domainId
   * @throws PlatformImportException
   */
  public void removeDomain(String domainId) throws PlatformImportException {

    if (domainId == null) {
      throw new PlatformImportException("Bundle missing required domain-id property");
    }    
    logger.debug("Remove metadata - [domain=" + domainId + "]");
    metadataRepositoryImporter.removeDomain(domainId);

  }

}