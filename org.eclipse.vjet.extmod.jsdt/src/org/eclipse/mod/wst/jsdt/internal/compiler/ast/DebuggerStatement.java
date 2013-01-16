/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
import org.eclipse.mod.wst.jsdt.core.ast.IDebuggerStatement;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;

public class DebuggerStatement extends Statement implements IDebuggerStatement {

	public DebuggerStatement(int startPosition, int endPosition) {
		this.sourceStart = startPosition;
		this.sourceEnd = endPosition;
	}


	public StringBuffer printStatement(int tab, StringBuffer output) {
		return printIndent(tab, output).append("debugger;");
	}

	public void resolve(BlockScope scope) {
		if ((bits & IsUsefulEmptyStatement) == 0) {
			scope.problemReporter().superfluousSemicolon(this.sourceStart, this.sourceEnd);
		} else {
			scope.problemReporter().emptyControlFlowStatement(this.sourceStart, this.sourceEnd);
		}
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
	public int getASTType() {
		return IASTNode.EMPTY_STATEMENT;
	
	}

}

