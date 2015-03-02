/**
 * 
 */
package org.jboss.tools.foundation.ui.widget;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author eskimo
 *
 */
public class License extends Composite {

	ILicensePresenter licenseViewer = null;

	public License(Composite parent, int style, String url, String noBrowserTextTemplate,
					String progressTemplate, String urlLoadErrorTemplate) {
		super(parent, style);
		licenseViewer = createLicensePresenter(parent, url, noBrowserTextTemplate, progressTemplate, urlLoadErrorTemplate);
	}

	public void setLicenseURL(String url) {
		licenseViewer.setLicenseURL(url);
	}

	public interface ILicensePresenter {
		void setLicenseURL(String url);

		Control getControl();
	}

	public ILicensePresenter createLicensePresenter(Composite parent, String url, String noBrowserTextTemplate,
					String progressTemplate, String urlLoadErrorTemplate) {
		ILicensePresenter presenter = null;

		try {
			presenter = new BrowserLicensePresenter(parent,url,urlLoadErrorTemplate);
		} catch (SWTError error) {
			presenter = new LinkLicensePresenter(parent,url,noBrowserTextTemplate,progressTemplate,urlLoadErrorTemplate);
		}
		return presenter;
	}
	
	
	public static class BrowserLicensePresenter implements ILicensePresenter {
		
		public BrowserLicensePresenter(Composite parent, String url, String urlLoadErrorTemplate) {
			throw new SWTError("No browser based implementation");
		}

		@Override
		public void setLicenseURL(String url) {
			throw new SWTError("No browser based implementation");
		}

		@Override
		public Control getControl() {
			throw new SWTError("No browser based implementation");
		}
		
	}

	public static class LinkLicensePresenter implements ILicensePresenter {

		public LinkLicensePresenter(Composite parent, String url, String noBrowserTextTemplate,
						String progressTemplate, String urlLoadErrorTemplate) {
		}

		@Override
		public void setLicenseURL(String url) {
		}

		@Override
		public Control getControl() {
			return null;
		}

	}

}
