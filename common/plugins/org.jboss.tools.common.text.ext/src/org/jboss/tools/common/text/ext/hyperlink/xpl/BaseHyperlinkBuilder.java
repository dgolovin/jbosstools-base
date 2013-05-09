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

import org.eclipse.core.runtime.IConfigurationElement;
import org.jboss.tools.common.text.ext.hyperlink.HyperlinkBuilder;
import org.jboss.tools.common.text.ext.util.xpl.RegistryReader;

public class BaseHyperlinkBuilder extends RegistryReader{
	// extension point ID
	public static final String PL_HYPERLINK = "hyperlink"; //$NON-NLS-1$

//	public static final String PLUGIN_ID = ExtensionsPlugin.PLUGIN_ID;

	public static final String TAG_HYPERLINK = "hyperlink"; //$NON-NLS-1$
	public static final String TAG_CONTENT_TYPE_IDENTIFIER = "contenttypeidentifier"; //$NON-NLS-1$
	public static final String TAG_PARTITION_TYPE = "partitiontype"; //$NON-NLS-1$

	public static final String ATT_ID = "id"; //$NON-NLS-1$
	public static final String ATT_CLASS = "class"; //$NON-NLS-1$

	protected String targetContributionTag;

	private static HyperlinkBuilder fInstance;

	private List fHyperlinkDefs = null;
	private HyperlinkDefinition fCurrentHyperlinkDefinition = null;
	private String fCurrentContentType;

	/**
	 * returns singleton instance of HyperlinkBuilder
	 * 
	 * @return HyperlinkBuilder
	 */
	public synchronized static HyperlinkBuilder getInstance() {
		if (fInstance == null) {
			fInstance = new HyperlinkBuilder();
		}
		return fInstance;
	}

	/**
	 * Returns the name of the part ID attribute that is expected
	 * in the target extension.
	 * 
	 * @param element
	 * @return String
	 */
	protected String getId(IConfigurationElement element) {
		String value = element.getAttribute(ATT_ID);
		return value;
	}

	protected String getHyperlinkClass(IConfigurationElement element) {
		String value = element.getAttribute(ATT_CLASS);
		return value;
	}

	/**
	 * Processes element which should be a configuration element specifying an
	 * open on object.  Creates a new open on definition object and adds it to the
	 * list of open on definition objects
	 * 
	 * @param element hyperlink configuration element
	 */
	private void processHyperlinkTag(IConfigurationElement element) {
		String theId = getId(element);
		String theClass = getHyperlinkClass(element);

		if (theId != null && theClass != null) {
			// start building new HyperlinkDefinition
			fCurrentHyperlinkDefinition = new HyperlinkDefinition(theId, theClass, element);

			// create a new list of open on definitions if it hasnt been created yet
			if (fHyperlinkDefs == null) {
				fHyperlinkDefs = new ArrayList();
			}
			fHyperlinkDefs.add(fCurrentHyperlinkDefinition);
		}
		else {
			fCurrentHyperlinkDefinition = null;
		}
	}

	/**
	 * Processes element which should be a configuration element specifying a content
	 * type for the current open on tag.  Assumes that there is a valid current open
	 * on definition object.
	 * 
	 * @param element contenttypeidentifier configuration element
	 */
	private void processContentTypeTag(IConfigurationElement element) {
		// add to current HyperlinkDefinition
		String theId = getId(element);

		if (theId != null) {
			fCurrentContentType = theId;
			fCurrentHyperlinkDefinition.addContentTypeId(fCurrentContentType);
		}
		else {
			fCurrentContentType = null;
		}
	}

	/**
	 * Processes element which should be a configuration element specifying a partition
	 * type for the current open on/content type tag.  Assumes that there is a valid
	 * current open on/content type tag.
	 * 
	 * @param element partitiontype configuration element
	 */
	private void processPartitionTypeTag(IConfigurationElement element) {
		// add to current HyperlinkDefinition/contentType
		String theId = getId(element);

		if (theId != null) {
			fCurrentHyperlinkDefinition.addPartitionType(fCurrentContentType, theId);
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.sse.editor.internal.extension.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
	 */
	protected boolean readElement(IConfigurationElement element) {
		String tag = element.getName();

		if (tag.equals(targetContributionTag)) {
			processHyperlinkTag(element);

			// make sure processing of current open on tag resulted in a current open on definition
			// before continue reading the children
			if (fCurrentHyperlinkDefinition != null) {
				readElementChildren(element);
			}
			return true;
		}
		else if (tag.equals(TAG_CONTENT_TYPE_IDENTIFIER)) {
			processContentTypeTag(element);

			// make sure processing of current content type resulted in a valid content type
			// before reading the children
			if (fCurrentContentType != null) {
				readElementChildren(element);
			}
			return true;
		}
		else if (tag.equals(TAG_PARTITION_TYPE)) {
			processPartitionTypeTag(element);
			return true;
		}

		return false;
	}

	private void initCache() {
		if (fHyperlinkDefs == null) {
			readContributions(TAG_HYPERLINK, PL_HYPERLINK);
		}
	}



	protected void readContributions(String tag_hyperlink2, String pl_hyperlink2) {
		
	}

	/**
	 * Returns all the open on definition objects
	 * @return
	 */
	public Collection<HyperlinkDefinition> getHyperlinkDefinitions() {
		initCache();
		return fHyperlinkDefs;
	}

	/**
	 * Returns all the open on definition objects valid for contentType/partitionType
	 * @param contentType
	 * @param partitionType
	 * @return if either contentType or partitionType is null, null is returned 
	 */
	public Collection<HyperlinkDefinition> getHyperlinkDefinitions(String contentType, String partitionType) {
		// entire list of hyperlink definition objects
		Collection<HyperlinkDefinition> allDefs = getHyperlinkDefinitions();
		// current list of open on definitions valid for contentType/partitionType
		List<HyperlinkDefinition> defs = new ArrayList<HyperlinkDefinition>();
		// default definitions that should be added to end of list of open on definitions
		List<HyperlinkDefinition> lastDefs = new ArrayList<HyperlinkDefinition>();

		for( HyperlinkDefinition allDef : allDefs) {
			// for each one check if it contains contentType
			List<String> partitions = allDef.getContentTypes().get(contentType);
			if (partitions != null) {
				// this hyperlink definition is valid for all partition types for this content type
				if (partitions.isEmpty()) {
					// this will be added to end of list because this is considered a default hyperlink
					lastDefs.add(allDef);
				} else {
					// examine the partition types of this hyperlink
					int j = 0; // current index in list of partitions
					boolean added = false; // hyperlink has been added to list
					while (j < partitions.size() && !added) {
						// this hyperlink definition applies to partitionType so add to list of valid hyperlinks
						if (partitionType.equals(partitions.get(j))) {
							defs.add(allDef);
							added = true;
						}
						else {
							// continue checking to see if this hyperlink definition is valid for current partitionType
							++j;
						}
					}
				}
			}
		}
		// append the default hyperlink definitions
		defs.addAll(lastDefs);

		// return the list
		return defs;
	}
	
}
