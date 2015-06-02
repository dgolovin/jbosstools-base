package org.jboss.tools.runtime.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.jboss.tools.runtime.ui.internal.wizard.DownloadRuntimeOperationUIUtility;

public class DownloadRuntimeOperationUtilityTest extends TestCase {
	public void testDownloadRuntimeOperationUtility() {
		File destFolder = RuntimeTestActivator.getDefault().getStateLocation().toFile();
		DownloadRuntimeOperationUtilityExt util = new DownloadRuntimeOperationUtilityExt();
		
		
		File ret = util.invokeGetNextUnusedFilename(destFolder, "test.jar");
		assertEquals(ret.getName(), "test.jar");
		create(ret);
		ret = util.invokeGetNextUnusedFilename(destFolder, "test.jar");
		assertEquals(ret.getName(), "test(1).jar");
		create(ret);
		ret = util.invokeGetNextUnusedFilename(destFolder, "test.jar");
		assertEquals(ret.getName(), "test(2).jar");
		create(ret);
		
		
		ret = util.invokeGetNextUnusedFilename(destFolder, "test.tar.gz");
		assertEquals(ret.getName(), "test.tar.gz");
		create(ret);
		ret = util.invokeGetNextUnusedFilename(destFolder, "test.tar.gz");
		assertEquals(ret.getName(), "test(1).tar.gz");
		create(ret);
		ret = util.invokeGetNextUnusedFilename(destFolder, "test.tar.gz");
		assertEquals(ret.getName(), "test(2).tar.gz");
		create(ret);


		ret = util.invokeGetNextUnusedFilename(destFolder, "test");
		assertEquals(ret.getName(), "test");
		create(ret);
		ret = util.invokeGetNextUnusedFilename(destFolder, "test");
		assertEquals(ret.getName(), "test(1)");
		create(ret);
		ret = util.invokeGetNextUnusedFilename(destFolder, "test");
		assertEquals(ret.getName(), "test(2)");
		create(ret);


	}
	
	private void create(File f) {
		try {
			f.createNewFile();
		} catch(IOException ioe) {
			fail();
		}
	}
	
	private static class DownloadRuntimeOperationUtilityExt extends DownloadRuntimeOperationUIUtility {
		public File invokeGetNextUnusedFilename(File destination, String name) {
			return super.getNextUnusedFilename(destination, name);
		}
	}
}
