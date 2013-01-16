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
import org.eclipse.mod.wst.jsdt.core.ast.IConditionalExpression;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class ConditionalExpression extends OperatorExpression implements IConditionalExpression {

	public Expression condition, valueIfTrue, valueIfFalse;
	public Constant optimizedBooleanConstant;
	public Constant optimizedIfTrueConstant;
	public Constant optimizedIfFalseConstant;

	// for local variables table attributes
	int trueInitStateIndex = -1;
	int falseInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	public ConditionalExpression(
		Expression condition,
		Expression valueIfTrue,
		Expression valueIfFalse) {
		this.condition = condition;
		this.valueIfTrue = valueIfTrue;
		this.valueIfFalse = valueIfFalse;
		sourceStart = condition.sourceStart;
		sourceEnd = valueIfFalse.sourceEnd;
	}


	public int nullStatus(FlowInfo flowInfo) {
	Constant cst = this.condition.optimizedBooleanConstant();
	if (cst != Constant.NotAConstant) {
		if (cst.booleanValue()) {
			return valueIfTrue.nullStatus(flowInfo);
		}
		return valueIfFalse.nullStatus(flowInfo);
	}
	int ifTrueNullStatus = valueIfTrue.nullStatus(flowInfo),
	    ifFalseNullStatus = valueIfFalse.nullStatus(flowInfo);
	if (ifTrueNullStatus == ifFalseNullStatus) {
		return ifTrueNullStatus;
	}
	return FlowInfo.UNKNOWN;
	// cannot decide which branch to take, and they disagree
}

	public Constant optimizedBooleanConstant() {

		return this.optimizedBooleanConstant == null ? this.constant : this.optimizedBooleanConstant;
	}

	public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {

		condition.printExpression(indent, output).append(" ? "); //$NON-NLS-1$
		valueIfTrue.printExpression(0, output).append(" : "); //$NON-NLS-1$
		return valueIfFalse.printExpression(0, output);
	}


	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			condition.traverse(visitor, scope);
			valueIfTrue.traverse(visitor, scope);
			valueIfFalse.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
	public int getASTType() {
		return IASTNode.CONDITIONAL_EXPRESSION;
	
	}
}
