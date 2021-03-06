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
package org.jboss.tools.common.meta.impl.adapters;

import org.jboss.tools.common.meta.constraint.*;
import org.jboss.tools.common.model.XModelObjectConstants;
import org.jboss.tools.common.model.XModelObject;
import org.jboss.tools.common.model.util.XModelObjectLoaderUtil;

public class XAdapterModelPath extends XAdapter {

    public XAdapterModelPath() {}

    public String getProperty(XProperty object) {
        String p = XModelObjectLoaderUtil.getResourcePath((XModelObject)object);
        return (p == null) ? "" + object.get(XModelObjectConstants.XML_ATTR_NAME) : p; //$NON-NLS-1$
    }

    public void setProperty(XProperty object, String value) {}

}
