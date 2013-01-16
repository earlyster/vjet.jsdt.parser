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
import org.eclipse.mod.wst.jsdt.core.ast.IIfStatement;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;

public class IfStatement extends Statement implements IIfStatement {

	//this class represents the case of only one statement in
	//either else and/or then branches.

	public Expression condition;
	public Statement thenStatement;
	public Statement elseStatement;

	boolean thenExit;

	// for local variables table attributes
	int thenInitStateIndex = -1;
	int elseInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	public IfStatement(Expression condition, Statement thenStatement, 	int sourceStart, int sourceEnd) {

		this.condition = condition;
		this.thenStatement = thenStatement;
		// remember useful empty statement
		if (thenStatement instanceof EmptyStatement) thenStatement.bits |= IsUsefulEmptyStatement;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}

	public IfStatement(Expression condition, Statement thenStatement, Statement elseStatement, int sourceStart, int sourceEnd) {

		this.condition = condition;
		this.thenStatement = thenStatement;
		// remember useful empty statement
		if (thenStatement instanceof EmptyStatement) thenStatement.bits |= IsUsefulEmptyStatement;
		this.elseStatement = elseStatement;
		if (elseStatement instanceof IfStatement) elseStatement.bits |= IsElseIfStatement;
		if (elseStatement instanceof EmptyStatement) elseStatement.bits |= IsUsefulEmptyStatement;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}




	public StringBuffer printStatement(int indent, StringBuffer output) {

		printIndent(indent, output).append("if ("); //$NON-NLS-1$
		condition.printExpression(0, output).append(")\n");	//$NON-NLS-1$
		thenStatement.printStatement(indent + 2, output);
		if (elseStatement != null) {
			output.append('\n');
			printIndent(indent, output);
			output.append("else\n"); //$NON-NLS-1$
			elseStatement.printStatement(indent + 2, output);
		}
		return output;
	}

//	public void resolve(BlockScope scope) {
//
//		TypeBinding type = condition.resolveTypeExpecting(scope, TypeBinding.BOOLEAN);
//		if (thenStatement != null)
//			thenStatement.resolve(scope);
//		if (elseStatement != null)
//			elseStatement.resolve(scope);
//	}

	public void traverse(
		ASTVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			condition.traverse(visitor, blockScope);
			if (thenStatement != null)
				thenStatement.traverse(visitor, blockScope);
			if (elseStatement != null)
				elseStatement.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
	public int getASTType() {
		return IASTNode.IF_STATEMENT;
	
	}
}
