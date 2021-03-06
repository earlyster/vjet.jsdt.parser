/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.mod.wst.jsdt.internal.core.interpret.builtin;

import org.eclipse.mod.wst.jsdt.internal.core.interpret.InterpreterContext;

public  class BuiltInHelper {

	public static void initializeBuiltinObjects(InterpreterContext context)
	{
		BuiltInObject.initializeContext(context);
		BuiltInString.initializeContext(context);
	}
}
