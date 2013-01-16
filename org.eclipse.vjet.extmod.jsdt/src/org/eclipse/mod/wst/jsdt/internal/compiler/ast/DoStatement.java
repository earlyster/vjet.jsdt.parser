/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
import org.eclipse.mod.wst.jsdt.core.ast.IDoStatement;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;


public class DoStatement extends Statement implements IDoStatement {

	public Expression condition;
	public Statement action;


	// for local variables table attributes
	int mergedInitStateIndex = -1;

public DoStatement(Expression condition, Statement action, int s, int e) {

	this.sourceStart = s;
	this.sourceEnd = e;
	this.condition = condition;
	this.action = action;
	// remember useful empty statement
	if (action instanceof EmptyStatement) action.bits |= ASTNode.IsUsefulEmptyStatement;
}


public StringBuffer printStatement(int indent, StringBuffer output) {
	printIndent(indent, output).append("do"); //$NON-NLS-1$
	if (this.action == null)
		output.append(" ;\n"); //$NON-NLS-1$
	else {
		output.append('\n');
		this.action.printStatement(indent + 1, output).append('\n');
	}
	output.append("while ("); //$NON-NLS-1$
	return this.condition.printExpression(0, output).append(");"); //$NON-NLS-1$
}


public void traverse(ASTVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		if (this.action != null) {
			this.action.traverse(visitor, scope);
		}
		this.condition.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
public int getASTType() {
	return IASTNode.DOUBLE_LITERAL;

}
}
