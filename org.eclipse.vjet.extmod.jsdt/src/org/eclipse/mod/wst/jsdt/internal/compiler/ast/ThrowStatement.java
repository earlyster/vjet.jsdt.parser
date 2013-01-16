/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import org.eclipse.mod.wst.jsdt.core.ast.IThrowStatement;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class ThrowStatement extends Statement implements IThrowStatement {

	public Expression exception;
	public TypeBinding exceptionType;

public ThrowStatement(Expression exception, int sourceStart, int sourceEnd) {
	this.exception = exception;
	this.sourceStart = sourceStart;
	this.sourceEnd = sourceEnd;
}


public StringBuffer printStatement(int indent, StringBuffer output) {
	printIndent(indent, output).append("throw "); //$NON-NLS-1$
	this.exception.printExpression(0, output);
	return output.append(';');
}



public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope))
		this.exception.traverse(visitor, blockScope);
	visitor.endVisit(this, blockScope);
}
public int getASTType() {
	return IASTNode.THROW_STATEMENT;

}
}
