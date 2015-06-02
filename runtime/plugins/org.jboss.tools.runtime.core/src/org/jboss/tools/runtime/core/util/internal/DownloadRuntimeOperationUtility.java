/*******************************************************************************
 * Copyright (c) 2014 Red Hat 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JBoss by Red Hat
 *******************************************************************************/
package org.jboss.tools.runtime.core.util.internal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.jboss.tools.foundation.core.ecf.URLTransportUtility;
import org.jboss.tools.foundation.core.tasks.TaskModel;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.extract.ExtractUtility;
import org.jboss.tools.runtime.core.extract.IOverwrite;
import org.jboss.tools.runtime.core.model.IDownloadRuntimeWorkflowConstants;

/**
 * Mixed class of core+ui to initiate the download, unzipping, 
 * and runtime creation for a downloaded runtime. 
 */
public class DownloadRuntimeOperationUtility {
	private static final String SEPARATOR = "/"; //$NON-NLS-1$

	protected File getNextUnusedFilename(File destination, String name) {
		String nameWithoutSuffix = null;
		if( name.indexOf('.') == -1 ) {
			nameWithoutSuffix = name;
		} else if( name.endsWith(".tar.gz"))
			nameWithoutSuffix = name.substring(0, name.length() - ".tar.gz".length());
		else 
			nameWithoutSuffix = name.substring(0, name.lastIndexOf('.'));
		String suffix = name.substring(nameWithoutSuffix.length());
		int i = 1;
		String tmpName = null;
		File file = new File (destination, name);
		while (file.exists()) {
			tmpName = nameWithoutSuffix + "(" + i++ + ")" + suffix; //$NON-NLS-1$ //$NON-NLS-2$
			file = new File(destination, tmpName); 
		}
		return file;
	}
	
	
	/**
	 * 
	 * @param downloadDestinationPath   The path to put the downloaded zip
	 * @param urlString					The remote url
	 * @param deleteOnExit				Whether to delete on exit or not
	 * @return
	 */
	private File getDestinationFile(String downloadDestinationPath, String urlString, boolean deleteOnExit) throws CoreException {
		File ret = null;
		try {
			URL url = new URL(urlString);
			String name = url.getPath();
			int slashIdx = name.lastIndexOf('/');
			if (slashIdx >= 0)
				name = name.substring(slashIdx + 1);

			File destination = new File(downloadDestinationPath);
			destination.mkdirs();
			ret = new File (destination, name);
			if (deleteOnExit) {
				ret = getNextUnusedFilename(destination, name);
			}
			if( deleteOnExit )
				ret.deleteOnExit();
			return ret;
		} catch (IOException e) {
			cancel(ret);
			IStatus s = new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, e.getMessage(), e);
			throw new CoreException(s);
		}
	}
	
	private boolean cacheOutdated(File local, boolean deleteOnExit, long urlLastModified) {
		boolean download = true;
		long urlModified = 0;
		if (!deleteOnExit) {
			long cacheModified = local.lastModified();
			download = cacheModified <= 0 || cacheModified != urlModified;
		}
		return download;
	}
	
	
	private long getRemoteURLModified(String urlString, String user, String pass, IProgressMonitor monitor) throws CoreException, IOException {
		monitor.beginTask("Checking remote timestamp", 100);
		long l = new URLTransportUtility().getLastModified(new URL(urlString), user, pass, monitor);
		monitor.worked(100);
		monitor.done();
		return l;
	}
	
	private void validateInputs(String downloadDirectoryPath, String unzipDirectoryPath) throws CoreException {
		File downloadDirectory = new File(downloadDirectoryPath);
		downloadDirectory.mkdirs();
		if (!downloadDirectory.isDirectory()) {
			throw new CoreException(new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, "The '" + downloadDirectory + "' is not a directory.")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		File unzipDirectory = new File(unzipDirectoryPath);
		unzipDirectory.mkdirs();
		if (!unzipDirectory.isDirectory()) {
			throw new CoreException( new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, "The '" + unzipDirectory + "' is not a directory.")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	

	public File download(String unzipDirectoryPath, String downloadDirectoryPath, 
			String urlString, boolean deleteOnExit, String user, String pass, TaskModel tm, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Download runtime from url " + urlString, 500);
		try {
			validateInputs(downloadDirectoryPath, unzipDirectoryPath);
			File downloadedFile = downloadRemoteRuntime(unzipDirectoryPath, downloadDirectoryPath, 
					urlString, deleteOnExit, user, pass, new SubProgressMonitor(monitor, 500));
			return downloadedFile;
		} finally {
			monitor.done();
		}
	}

	

	public IStatus downloadAndUnzip(String unzipDirectoryPath, String downloadDirectoryPath, 
			String urlString, boolean deleteOnExit, String user, String pass, TaskModel tm, IProgressMonitor monitor) {
		monitor.beginTask("Configuring runtime from url " + urlString, 500);
		try {
			validateInputs(downloadDirectoryPath, unzipDirectoryPath);
			File downloadedFile = downloadRemoteRuntime(unzipDirectoryPath, downloadDirectoryPath, urlString, deleteOnExit, user, pass, new SubProgressMonitor(monitor, 450));
			ExtractUtility extractUtil = new ExtractUtility(downloadedFile);
			IOverwrite ow = (IOverwrite)tm.getObject(IDownloadRuntimeWorkflowConstants.OVERWRITE);
			if( ow == null ) {
				ow = createOverwriteFileQuery();
			}
			unzip(extractUtil, downloadedFile, unzipDirectoryPath, ow, new SubProgressMonitor(monitor, 30));
			String updatedRuntimeRoot = getUpdatedUnzipPath(extractUtil, unzipDirectoryPath, new SubProgressMonitor(monitor, 10));
			tm.putObject(IDownloadRuntimeWorkflowConstants.UNZIPPED_SERVER_HOME_DIRECTORY, updatedRuntimeRoot);
		} catch(CoreException ce) {
			return ce.getStatus();
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	
	private File downloadRemoteRuntime(String unzipDirectoryPath, String destinationDirectory, 
			String urlString, boolean deleteOnExit, String user, String pass, IProgressMonitor monitor) throws CoreException  {
		monitor.beginTask("Downloading " + urlString, 1000);
		File file = null;
		try {
			file = getDestinationFile(destinationDirectory, urlString, deleteOnExit);
			
			long urlModified = deleteOnExit ? 0 : getRemoteURLModified(urlString, user, pass, new SubProgressMonitor(monitor, 100));
			boolean download = cacheOutdated(file, deleteOnExit, urlModified);

			IStatus result = null;
			if (download) {
				result = downloadFileFromRemoteUrl(file, new URL(urlString), urlModified, user, pass, new SubProgressMonitor(monitor, 900));
			}
			if( !result.isOK())
				throw new CoreException(result);
			if (monitor.isCanceled())
				throw new CoreException(cancel(file));
			
			return file;
		} catch (IOException  e) {
			cancel(file);
			throw new CoreException(new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, e.getMessage(), e));
		} finally {
			monitor.done();
		}
	}
	
	private void unzip(ExtractUtility util, File downloadedFile, String unzipDirectoryPath, IOverwrite overwriteQuery, IProgressMonitor monitor) throws CoreException  {
		monitor.beginTask("Unzipping " + downloadedFile.getAbsolutePath(), 1000);
		if (monitor.isCanceled())
			throw new CoreException(cancel(downloadedFile));

		final IStatus status = util.extract(new File(unzipDirectoryPath), overwriteQuery, new SubProgressMonitor(monitor, 1000));
		if (monitor.isCanceled())
			throw new CoreException( cancel(downloadedFile));
		if( !status.isOK())
			throw new CoreException(status);
	}
	
	private IStatus cancel(File f) {
		if( f != null ) {
			f.deleteOnExit();
			f.delete();
		}
		return Status.CANCEL_STATUS;
	}
	
	private IOverwrite createOverwriteFileQuery() {
		return new IOverwrite() {
			public int overwrite(File file) {
				return IOverwrite.YES;
			}
		};
	}
	
	private String getUpdatedUnzipPath(ExtractUtility util, String unzipDirectoryPath, IProgressMonitor monitor) throws CoreException {
		try {
			String root = util.getExtractedRootFolder( new SubProgressMonitor(monitor, 10));
			if (root != null) {
				File rootFile = new File(unzipDirectoryPath, root);
				if (rootFile != null && rootFile.exists()) {
					unzipDirectoryPath = rootFile.getAbsolutePath();
				}
			}
			return unzipDirectoryPath;
		} catch(CoreException ce) {
			cancel(util.getOriginalFile());
			throw ce;
		} finally {
			monitor.done();
		}
	}
	
	private IStatus downloadFileFromRemoteUrl(File toFile, URL url, long remoteUrlModified, String user, String pass, IProgressMonitor monitor) throws IOException {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(toFile));
			IStatus result = new URLTransportUtility().download(
					toFile.getName(), url.toExternalForm(), user, pass, out, -1, monitor);
			out.flush();
			out.close();
			if (remoteUrlModified > 0) {
				toFile.setLastModified(remoteUrlModified);
			}
			return result;
		} finally { 
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}			
		}
	}

}
