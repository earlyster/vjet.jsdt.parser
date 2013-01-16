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
import org.eclipse.mod.wst.jsdt.core.ast.ILabeledStatement;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;

public class LabeledStatement extends Statement implements ILabeledStatement {

	public Statement statement;
	public char[] label;
	public int labelEnd;

	// for local variables table attributes
	int mergedInitStateIndex = -1;

	/**
	 * LabeledStatement constructor comment.
	 */
	public LabeledStatement(char[] label, Statement statement, long labelPosition, int sourceEnd) {

		this.statement = statement;
		// remember useful empty statement
		if (statement instanceof EmptyStatement) statement.bits |= IsUsefulEmptyStatement;
		this.label = label;
		this.sourceStart = (int)(labelPosition >>> 32);
		this.labelEnd = (int) labelPosition;
		this.sourceEnd = sourceEnd;
	}

	public ASTNode concreteStatement() {

		// return statement.concreteStatement(); // for supporting nested labels:   a:b:c: someStatement (see 21912)
		return statement;
	}

	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output).append(label).append(": "); //$NON-NLS-1$
		if (this.statement == null)
			output.append(';');
		else
			this.statement.printStatement(0, output);
		return output;
	}

//	public void resolve(BlockScope scope) {
//
//		if (this.statement != null) {
//			this.statement.resolve(scope);
//		}
//	}


	public void traverse(
		ASTVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			if (this.statement != null) this.statement.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
	public int getASTType() {
		return IASTNode.LABELED_STATEMENT;
	
	}
}
