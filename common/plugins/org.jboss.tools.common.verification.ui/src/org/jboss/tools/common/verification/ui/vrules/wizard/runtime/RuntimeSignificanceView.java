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
package org.jboss.tools.common.verification.ui.vrules.wizard.runtime;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.jboss.tools.common.verification.ui.vrules.wizard.SignificanceView;

public class RuntimeSignificanceView extends SignificanceView {
	Label significance;
	
	public void update() {
		if(significance != null && !significance.isDisposed()) { 
			String value = getMinSignificancePresentation(manager.getMinSignificance());
			significance.setText(value);
		}
	}
	
	protected Control createSignificanceControl(Composite parent) {
		significance = new Label(parent, SWT.NONE);
		update();
		return significance;
	}
	
}
