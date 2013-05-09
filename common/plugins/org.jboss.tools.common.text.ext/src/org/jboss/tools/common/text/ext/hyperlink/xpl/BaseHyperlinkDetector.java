/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     Exadel, Inc.
 *     Red Hat, Inc.     
 *******************************************************************************/
package org.jboss.tools.common.text.ext.hyperlink.xpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.jboss.tools.common.text.ext.hyperlink.AbstractHyperlink;
import org.jboss.tools.common.text.ext.hyperlink.HyperlinkBuilder;
import org.jboss.tools.common.text.ext.hyperlink.IHyperlinkRegion;
import org.jboss.tools.common.text.ext.util.StructuredModelWrapper;
import org.jboss.tools.common.text.ext.util.StructuredModelWrapper.ICommand;

public class BaseHyperlinkDetector implements IHyperlinkDetector{

	public List<IHyperlink> getHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		List<IHyperlink> hyperlinks = null;
		IHyperlinkRegion partition = getPartition(textViewer.getDocument(), region.getOffset());
		if(partition != null) {
			String contentType = getContentType(textViewer.getDocument());
			if(contentType!=null) {
				hyperlinks = getHyperlinks(textViewer, region, contentType, partition, canShowMultipleHyperlinks);
			}
		}
		return hyperlinks;
	}

	/**
	 * Returns IHyperlink array for the document and region type.
	 * @param canShowMultipleHyperlinks 
	 */
	public List<IHyperlink> getHyperlinks(ITextViewer textViewer, IRegion region, String contentType, IHyperlinkRegion partition, boolean canShowMultipleHyperlinks) {
	    List<IHyperlink> hyperlinks = new ArrayList<IHyperlink>();
		// determine the current partition
		if (textViewer != null && textViewer.getDocument() != null) {
			// query HyperlinkBuilder and get the list of open ons for the
			// current partition
			Collection<HyperlinkDefinition> defs = HyperlinkBuilder.getInstance().getHyperlinkDefinitions(contentType, partition.getType());

			for(HyperlinkDefinition def : defs) {
			    IHyperlink hyperlink = def.createHyperlink();
			    if(!hyperlinks.contains(hyperlink)) {
			    	if (hyperlink instanceof AbstractHyperlink) {
			    		((AbstractHyperlink)hyperlink).setDocument(textViewer.getDocument());
			    		((AbstractHyperlink)hyperlink).setOffset(region.getOffset());
			    		((AbstractHyperlink)hyperlink).setRegion(partition);
			    	}
				    hyperlinks.add(hyperlink);
				    if(!canShowMultipleHyperlinks) {
				    	break;
				    }
			    }
			}
		}
		return hyperlinks;
	}

	/**
	 * Returns the content type of document
	 * 
	 * @param document -
	 *            assumes document is not null
	 * @return String content type of given document
	 */
	protected String getContentType(IDocument document) {
		return StructuredModelWrapper.execute(document, new ICommand<String>() {
			@Override
			public String execute(IDOMDocument xmlDocument) {
				return xmlDocument.getModel().getContentTypeIdentifier();
			}
		});
	}

	/**
	 * Returns the partition type located at offset in the document
	 * 
	 * @param document -
	 *            assumes document is not null
	 * @param offset
	 * @return String partition type
	 */
	protected IHyperlinkRegion getPartition(IDocument document, int offset) {
	    String type = null;
	    IHyperlinkRegion[] regions = getPartitions(document, offset);
	    // if more than 1 hyperlink partitioner type is returned just returning the first one.
		return (regions != null && regions.length > 0 ? regions[0] : null);
	}

	/**
	 * 
	 */
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		IHyperlink[] result = null;
		List<IHyperlink> hyperlinks = getHyperlinks(textViewer, region, canShowMultipleHyperlinks);
	    // if more than 1 hyperlink is returned just returning the first one.
		if (hyperlinks.size() > 0 && !canShowMultipleHyperlinks) {
			result = new IHyperlink[] {hyperlinks.get(0)}; 
		}
		return hyperlinks.toArray(new IHyperlink[hyperlinks.size()]);
	}	
	
	protected IHyperlinkRegion[] getPartitions(IDocument document, int offset) {
		return new IHyperlinkRegion[0];
	}
}
