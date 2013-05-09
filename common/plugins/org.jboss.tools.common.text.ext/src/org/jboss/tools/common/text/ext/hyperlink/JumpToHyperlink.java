/*******************************************************************************
 * Copyright (c) 2007-2012 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.common.text.ext.hyperlink;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMText;
import org.jboss.tools.common.text.ext.hyperlink.xml.XMLRootHyperlinkPartitioner;
import org.jboss.tools.common.text.ext.util.RegionHolder;
import org.jboss.tools.common.text.ext.util.StructuredModelWrapper;
import org.jboss.tools.common.text.ext.util.StructuredModelWrapper.ICommand;
import org.jboss.tools.common.text.ext.util.StructuredSelectionHelper;
import org.jboss.tools.common.text.ext.util.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Jeremy
 *
 */
abstract public class JumpToHyperlink extends AbstractHyperlink {

	/** 
	 * @see com.ibm.sse.editor.AbstractHyperlink#doHyperlink(org.eclipse.jface.text.IRegion)
	 */
	protected void doHyperlink(IRegion region) {
	
			Region holder = getRegionHolder(getName(region), region);
			if (holder != null) {
				StructuredSelectionHelper.setSelectionAndRevealInActiveEditor(holder);
			} else {
				openFileFailed();
			}
			
	}

	protected String getName(IRegion region) {
		try {
			return Utils.trimQuotes(getDocument().get(region.getOffset(), region.getLength()));
		} catch (BadLocationException x) {
			//ignore
			return null;
		}
	}

	abstract protected String getDestinationAxis();
	
	protected Region getRegionHolder(final String content, final IRegion region) {
		return StructuredModelWrapper.execute(getDocument(),new ICommand<Region>(){
			@Override
			public Region execute(IDOMDocument xmlDocument) {
				List elements = findElementsByAxis(xmlDocument.getChildNodes(), getDestinationAxis());
				
				if (elements == null || elements.size() == 0) return null;
				
				for (int i = elements.size() - 1; i >= 0; i--) {
					if (elements.get(i) instanceof IDOMElement) {
						IDOMElement element = (IDOMElement)elements.get(i);
						String text = Utils.trimQuotes(getElementText(element));
						if (content.equals(text)) {
							return new Region (element.getStartOffset(), 
									element.getStartStructuredDocumentRegion().getLength());
						}
					}
				}
				return null;
			}});
	}
	
	protected String getElementText(Node element) {
			if (element instanceof IDOMText) 
				return ((IDOMText)element).getData();

			StringBuffer text = new StringBuffer();
			if (element instanceof IDOMElement) {
				Node child = element.getFirstChild();
				while (child != null) {
					if (child instanceof IDOMText) {
						text.append(((IDOMText)child).getData());
					}
					child = child.getNextSibling();
				}
			}
			String result = text.toString();
			if (result.length() == 0) return null;
			return result;

	}
	
	protected List<Node> findElementsByAxis (NodeList list, String axis) {
		String requiredAxis = axis.toLowerCase();
		List<Node> elements = new ArrayList<Node>();
			for (int i = 0; list != null && i < list.getLength(); i++) {
				if (!(list.item(i) instanceof IDOMElement))
					continue;
				IDOMElement element = (IDOMElement)list.item(i);
				String currentAxis = XMLRootHyperlinkPartitioner.computeAxis(getDocument(), element.getStartOffset()) + "/"; //$NON-NLS-1$
				currentAxis  = currentAxis.toLowerCase();
				
				if (currentAxis.endsWith(requiredAxis)) {
					elements.add(element);
				}
				
				if (element.hasChildNodes()) {
					List<Node> add = findElementsByAxis(element.getChildNodes(), axis);
					if (add != null) 
						elements.addAll(add);
				}
			}
			return elements;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see IHyperlink#getHyperlinkText()
	 */
	abstract public String getHyperlinkText(); 
}
