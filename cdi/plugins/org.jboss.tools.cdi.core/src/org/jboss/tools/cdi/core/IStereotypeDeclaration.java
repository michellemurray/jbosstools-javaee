/******************************************************************************* 
 * Copyright (c) 2009 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.cdi.core;

import org.jboss.tools.common.java.IAnnotationDeclaration;

/**
 * Represents a stereotype declaration.
 * 
 * @author Alexey Kazakov
 */
public interface IStereotypeDeclaration extends IAnnotationDeclaration {

	/**
	 * Returns the corresponding stereotype.
	 * 
	 * @return the corresponding stereotype.
	 */
	IStereotype getStereotype();
}