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
import org.eclipse.mod.wst.jsdt.core.ast.IForInStatement;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.LoopingFlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class ForInStatement extends Statement implements IForInStatement {

	public Statement  iterationVariable;
	public Expression collection;
	public Statement action;

	//when there is no local declaration, there is no need of a new scope
	//scope is positionned either to a new scope, or to the "upper"scope (see resolveType)
	public boolean neededScope;
	public BlockScope scope;


	// for local variables table attributes
	int preCondInitStateIndex = -1;
	int condIfTrueInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	public ForInStatement(
		Statement  iterationVariable,
		Expression collection,
		Statement action,
		boolean neededScope,
		int s,
		int e) {

		this.sourceStart = s;
		this.sourceEnd = e;
		this.iterationVariable = iterationVariable;
		this.collection = collection;
		this.action = action;
		// remember useful empty statement
		if (action instanceof EmptyStatement) action.bits |= IsUsefulEmptyStatement;
		this.neededScope = neededScope;
	}


	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output).append("for ("); //$NON-NLS-1$
		//inits
		if (iterationVariable != null) {
			if (iterationVariable instanceof AbstractVariableDeclaration) {
				AbstractVariableDeclaration variable = (AbstractVariableDeclaration) iterationVariable;
				variable.printAsExpression(0, output);
			}
			else
				iterationVariable.print(0, output);
		}
		output.append(" in "); //$NON-NLS-1$
		//cond
		if (collection != null) collection.printExpression(0, output);
		output.append(") "); //$NON-NLS-1$
		//block
		if (action == null)
			output.append(';');
		else {
			output.append('\n');
			action.printStatement(tab + 1, output);
		}
		return output;
	}

//	public void resolve(BlockScope upperScope) {
//
//		// use the scope that will hold the init declarations
//		scope = neededScope ? new BlockScope(upperScope) : upperScope;
//		if (iterationVariable != null)
//		{
//			if (iterationVariable instanceof Expression) {
//				Expression expression = (Expression) iterationVariable;
//				expression.resolveType(scope, true, null);
//// TODO: show a warning message here saying this var is at global scope
//			}
//			else
//				iterationVariable.resolve(scope);
//		}
//		if (collection != null) {
//			TypeBinding type = collection.resolveTypeExpecting(scope, TypeBinding.BOOLEAN);
//		}
//		if (action != null)
//			action.resolve(scope);
//	}

	public void traverse(ASTVisitor visitor, BlockScope blockScope) {
		BlockScope visitScope = (this.scope != null) ? this.scope : blockScope;
		if (visitor.visit(this, blockScope)) {
			if (iterationVariable != null) {
				iterationVariable.traverse(visitor, visitScope);
			}

			if (collection != null)
				collection.traverse(visitor, visitScope);

			if (action != null)
				action.traverse(visitor, visitScope);
		}
		visitor.endVisit(this, blockScope);
	}

	public int getASTType() {
		return IASTNode.FOR_IN_STATEMENT;
	
	}
}
