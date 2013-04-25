/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.common.text.ext.util;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.jboss.tools.common.model.XModel;
import org.jboss.tools.common.text.ext.ExtensionsPlugin;
import org.jboss.tools.common.text.ext.hyperlink.AbstractHyperlink;
import org.w3c.dom.Document;

public class StructuredModelWrapper {
	public interface Command {

		void execute(IDOMDocument xmlDocument);
		
	}

	IStructuredModel model = null;
	
	public StructuredModelWrapper() {
	}
	
	public void init(IDocument id) {
		model = getModelManager().getExistingModelForRead(id);
	}

	public void init(IFile file) throws IOException, CoreException {
		model = getModelManager().getModelForRead(file);
	}
	
	public Document getDocument() {
		return (model instanceof IDOMModel) ? ((IDOMModel) model).getDocument() : null;
	}
	
	public XModel getXModel() {
		return AbstractHyperlink.getXModel(model);
	}
	
	public IFile getFile() {
		return AbstractHyperlink.getFile(model);
	}
	
	public String getBaseLocation() {
		return AbstractHyperlink.getBaseLocation(model);
	}
	
	public void dispose() {
		if(model != null) {
			model.releaseFromRead();
			model = null;
		}
	}

	protected IModelManager getModelManager() {
		return StructuredModelManager.getModelManager();
	}
	
	public String getContentTypeIdentifier() {
		return model.getContentTypeIdentifier();
	}

	public static void execute(IFile file, final Command command) {
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getModelForRead(file);
			if(model instanceof IDOMModel) {
				final IDOMDocument xmlDocument = ((IDOMModel) model).getDocument();
				if(xmlDocument != null) {
					SafeRunnable.run(new ISafeRunnable() {
						@Override
						public void handleException(Throwable exception) {
							ExtensionsPlugin.getDefault().logError(exception);
						}

						@Override
						public void run() throws Exception {
							command.execute(xmlDocument);
						}
						
					});
				}
			}
		} catch (IOException e) {
			ExtensionsPlugin.getDefault().logError(e);
		} catch (CoreException e) {
			ExtensionsPlugin.getDefault().logError(e);
		} finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
	}
}
