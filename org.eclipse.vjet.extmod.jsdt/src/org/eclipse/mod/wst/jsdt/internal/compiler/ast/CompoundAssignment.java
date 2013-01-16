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
import org.eclipse.mod.wst.jsdt.core.ast.ICompoundAssignment;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class CompoundAssignment extends Assignment implements OperatorIds, ICompoundAssignment {
	public int operator;
	public int preAssignImplicitConversion;

	//  var op exp is equivalent to var = (varType) var op exp
	// assignmentImplicitConversion stores the cast needed for the assignment

	public CompoundAssignment(Expression lhs, Expression expression,int operator, int sourceEnd) {
		//lhs is always a reference by construction ,
		//but is build as an expression ==> the checkcast cannot fail

		super(lhs, expression, sourceEnd);
		lhs.bits &= ~IsStrictlyAssigned; // tag lhs as NON assigned - it is also a read access
		lhs.bits |= IsCompoundAssigned; // tag lhs as assigned by compound
		this.operator = operator ;
	}



	public int nullStatus(FlowInfo flowInfo) {
	return FlowInfo.NON_NULL;
	// we may have complained on checkNPE, but we avoid duplicate error
}

	public String operatorToString() {
		switch (operator) {
			case PLUS :
				return "+="; //$NON-NLS-1$
			case MINUS :
				return "-="; //$NON-NLS-1$
			case MULTIPLY :
				return "*="; //$NON-NLS-1$
			case DIVIDE :
				return "/="; //$NON-NLS-1$
			case AND :
				return "&="; //$NON-NLS-1$
			case OR :
				return "|="; //$NON-NLS-1$
			case XOR :
				return "^="; //$NON-NLS-1$
			case REMAINDER :
				return "%="; //$NON-NLS-1$
			case LEFT_SHIFT :
				return "<<="; //$NON-NLS-1$
			case RIGHT_SHIFT :
				return ">>="; //$NON-NLS-1$
			case UNSIGNED_RIGHT_SHIFT :
				return ">>>="; //$NON-NLS-1$
		}
		return "unknown operator"; //$NON-NLS-1$
	}

	public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {

		lhs.printExpression(indent, output).append(' ').append(operatorToString()).append(' ');
		return expression.printExpression(0, output) ;
	}


	public boolean restrainUsageToNumericTypes(){
		return false ;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			lhs.traverse(visitor, scope);
			expression.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
	public int getASTType() {
		return IASTNode.COMPOUND_ASSIGNMENT;
	
	}
}
