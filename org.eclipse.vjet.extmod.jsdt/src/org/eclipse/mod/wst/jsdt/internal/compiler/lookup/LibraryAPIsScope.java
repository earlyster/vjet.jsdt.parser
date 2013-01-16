/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Erling Ellingsen -  patch for bug 125570
 *******************************************************************************/
package org.eclipse.mod.wst.jsdt.internal.compiler.lookup;

import java.util.HashMap;


import org.eclipse.mod.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.mod.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.mod.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.mod.wst.jsdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.mod.wst.jsdt.internal.oaametadata.LibraryAPIs;



public class LibraryAPIsScope extends CompilationUnitScope {


	HashMap resolvedTypes=new HashMap();
	HashtableOfObject translations=new HashtableOfObject();
	LibraryAPIs apis;
public LibraryAPIsScope(LibraryAPIs apis, LookupEnvironment environment) {

	super(environment);
	this.apis=apis;
	this.referenceContext = null;

	
	this.currentPackageName = CharOperation.NO_CHAR_CHAR;
	
//	this.resolvedTypes.put("any", TypeBinding.ANY);
//	this.resolvedTypes.put("Any", TypeBinding.ANY);
//	this.resolvedTypes.put("null", TypeBinding.NULL);

	translations.put("object".toCharArray(), "Object".toCharArray());
	translations.put("boolean".toCharArray(), "Boolean".toCharArray());
	translations.put("number".toCharArray(), "Number".toCharArray());
	translations.put("string".toCharArray(), "String".toCharArray());
	translations.put("array".toCharArray(), "Array".toCharArray());
	
	CompilationResult result = new CompilationResult(apis.fileName.toCharArray(),new char[][]{},0,0,0);
	CompilationUnitDeclaration unit = new CompilationUnitDeclaration(environment.problemReporter,result,0);
	unit.scope=this;
	this.referenceContext=unit;
	
}

public PackageBinding getDefaultPackage() {
		return environment.defaultPackage;
}

public String toString() {
	return "--- LibraryAPIsScope Scope : " + new String(referenceContext.getFileName()); //$NON-NLS-1$
}

public void cleanup()
{
	super.cleanup();
}




public char[] getFileName() {
	return this.apis.fileName.toCharArray();
}

}
