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
import org.eclipse.mod.wst.jsdt.core.ast.IForStatement;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.LoopingFlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class ForStatement extends Statement implements IForStatement {

	public Statement[] initializations;
	public Expression condition;
	public Statement[] increments;
	public Statement action;

	//when there is no local declaration, there is no need of a new scope
	//scope is positionned either to a new scope, or to the "upper"scope (see resolveType)
	public boolean neededScope;
	public BlockScope scope;


	// for local variables table attributes
	int preCondInitStateIndex = -1;
	int preIncrementsInitStateIndex = -1;
	int condIfTrueInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	public ForStatement(
		Statement[] initializations,
		Expression condition,
		Statement[] increments,
		Statement action,
		boolean neededScope,
		int s,
		int e) {

		this.sourceStart = s;
		this.sourceEnd = e;
		this.initializations = initializations;
		this.condition = condition;
		this.increments = increments;
		this.action = action;
		// remember useful empty statement
		if (action instanceof EmptyStatement) action.bits |= IsUsefulEmptyStatement;
		this.neededScope = neededScope;
	}

	
	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output).append("for ("); //$NON-NLS-1$
		//inits
		if (initializations != null) {
			for (int i = 0; i < initializations.length; i++) {
				//nice only with expressions
				if (i > 0) output.append(", "); //$NON-NLS-1$
				initializations[i].print(0, output);
			}
		}
		output.append("; "); //$NON-NLS-1$
		//cond
		if (condition != null) condition.printExpression(0, output);
		output.append("; "); //$NON-NLS-1$
		//updates
		if (increments != null) {
			for (int i = 0; i < increments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				increments[i].print(0, output);
			}
		}
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
//		if (initializations != null)
//			for (int i = 0, length = initializations.length; i < length; i++) {
//				initializations[i].resolve(scope);
//		/* START -------------------------------- Bug 197884 Loosly defined var (for statement) and optional semi-colon --------------------- */
//		/* check where for variable exists in scope chain, report error if not local */
//				if(initializations[i] instanceof Assignment  ) {
//					Assignment as = ((Assignment)initializations[i]);
//					if (as.getLeftHandSide() instanceof SingleNameReference)
//					{
//						LocalVariableBinding bind1 = as.localVariableBinding();
//						if(bind1==null || bind1.declaringScope instanceof CompilationUnitScope){
//							upperScope.problemReporter().looseVariableDecleration(this, as);
//						}
//					}
//				}
//
//
//			}
//		/* END   -------------------------------- Bug 197884 Loosly defined var (for statement) and optional semi-colon --------------------- */
//
//		if (condition != null) {
//			TypeBinding type = condition.resolveTypeExpecting(scope, TypeBinding.BOOLEAN);
//		}
//		if (increments != null)
//			for (int i = 0, length = increments.length; i < length; i++)
//				increments[i].resolve(scope);
//		if (action != null)
//			action.resolve(scope);
//	}

	public void traverse(
		ASTVisitor visitor,
		BlockScope blockScope) {

		BlockScope visitScope= (this.scope!=null)?this.scope :blockScope;
		if (visitor.visit(this, blockScope)) {
			if (initializations != null) {
				int initializationsLength = initializations.length;
				for (int i = 0; i < initializationsLength; i++)
					initializations[i].traverse(visitor, visitScope);
			}

			if (condition != null)
				condition.traverse(visitor, visitScope);

			if (increments != null) {
				int incrementsLength = increments.length;
				for (int i = 0; i < incrementsLength; i++)
					increments[i].traverse(visitor, visitScope);
			}

			if (action != null)
				action.traverse(visitor, visitScope);
		}
		visitor.endVisit(this, blockScope);
	}
	public int getASTType() {
		return IASTNode.FOR_STATEMENT;
	
	}
}
