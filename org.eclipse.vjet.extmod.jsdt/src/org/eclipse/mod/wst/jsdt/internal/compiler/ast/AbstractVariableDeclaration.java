/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.mod.wst.jsdt.internal.compiler.ast;

import org.eclipse.mod.wst.jsdt.core.ast.IASTNode;
import org.eclipse.mod.wst.jsdt.core.ast.IAbstractVariableDeclaration;
import org.eclipse.mod.wst.jsdt.core.ast.IExpression;
import org.eclipse.mod.wst.jsdt.core.ast.IJsDoc;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ReferenceBinding;

public abstract class AbstractVariableDeclaration extends Statement implements  IAbstractVariableDeclaration, InvocationSite {
	public int declarationEnd;
	public int declarationSourceEnd;
	public int declarationSourceStart;
	public int hiddenVariableDepth; // used to diagnose hiding scenarii
	public Expression initialization;
	public int modifiers;
	public int modifiersSourceStart;
	public Javadoc javadoc;

	public char[] name;

	public TypeReference type;
	
	public AbstractVariableDeclaration nextLocal;
	
	public char[] getName() {
		return this.name;
	}


	public static final int FIELD = 1;
	public static final int INITIALIZER = 2;
	public static final int LOCAL_VARIABLE = 4;
	public static final int PARAMETER = 5;

	/**
	 * Returns the constant kind of this variable declaration
	 */
	public abstract int getKind();

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.internal.compiler.lookup.InvocationSite#isSuperAccess()
	 */
	public boolean isSuperAccess() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.internal.compiler.lookup.InvocationSite#isTypeAccess()
	 */
	public boolean isTypeAccess() {
		return false;
	}

	public StringBuffer printStatement(int indent, StringBuffer output) {
		printAsExpression(indent, output);
		return output.append(';');
	}

	public StringBuffer printAsExpression(int indent, StringBuffer output) {
		printIndent(indent, output);
		printModifiers(this.modifiers, output);
		output.append("var "); //$NON-NLS-1$

		printFragment(indent, output);
		if (this.nextLocal!=null)
		{
			output.append(", "); //$NON-NLS-1$
			this.nextLocal.printFragment(indent, output);
		}
		return output;
	}

	protected void printFragment(int indent, StringBuffer output) {
		if (type != null) {
			type.print(0, output).append(' ');
		}
		output.append(this.name);

		if (initialization != null) {
			output.append(" = "); //$NON-NLS-1$
			initialization.printExpression(indent, output);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.internal.compiler.lookup.InvocationSite#setActualReceiverType(org.eclipse.wst.jsdt.internal.compiler.lookup.ReferenceBinding)
	 */
	public void setActualReceiverType(ReferenceBinding receiverType) {
		// do nothing by default
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.internal.compiler.lookup.InvocationSite#setDepth(int)
	 */
	public void setDepth(int depth) {

		this.hiddenVariableDepth = depth;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.internal.compiler.lookup.InvocationSite#setFieldIndex(int)
	 */
	public void setFieldIndex(int depth) {
		// do nothing by default
	}


	public int getASTType() {
		return IASTNode.ABSTRACT_VARIABLE_DECLARATION;
	
	}
	
	public IJsDoc getJsDoc()
	{
		return this.javadoc;
	}
	
	public IExpression getInitialization()
	{
		return this.initialization;
	}
}
