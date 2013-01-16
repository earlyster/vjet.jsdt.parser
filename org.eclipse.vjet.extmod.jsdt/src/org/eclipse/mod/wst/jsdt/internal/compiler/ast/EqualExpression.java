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
import org.eclipse.mod.wst.jsdt.core.ast.IEqualExpression;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.mod.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TagBits;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class EqualExpression extends BinaryExpression implements IEqualExpression {

	public EqualExpression(Expression left, Expression right,int operator) {
		super(left,right,operator);
	}
	private void checkNullComparison(BlockScope scope, FlowContext flowContext, FlowInfo flowInfo, FlowInfo initsWhenTrue, FlowInfo initsWhenFalse) {

		LocalVariableBinding local = this.left.localVariableBinding();
		if (local != null && (local.type.tagBits & TagBits.IsBaseType) == 0) {
			checkVariableComparison(scope, flowContext, flowInfo, initsWhenTrue, initsWhenFalse, local, right.nullStatus(flowInfo), this.left);
		}
		local = this.right.localVariableBinding();
		if (local != null && (local.type.tagBits & TagBits.IsBaseType) == 0) {
			checkVariableComparison(scope, flowContext, flowInfo, initsWhenTrue, initsWhenFalse, local, left.nullStatus(flowInfo), this.right);
		}
	}
	private void checkVariableComparison(BlockScope scope, FlowContext flowContext, FlowInfo flowInfo, FlowInfo initsWhenTrue, FlowInfo initsWhenFalse, LocalVariableBinding local, int nullStatus, Expression reference) {
		switch (nullStatus) {
		case FlowInfo.NULL :
			if (((this.bits & OperatorMASK) >> OperatorSHIFT) == EQUAL_EQUAL) {
				flowContext.recordUsingNullReference(scope, local, reference,
						FlowContext.CAN_ONLY_NULL_NON_NULL | FlowContext.IN_COMPARISON_NULL, flowInfo);
				initsWhenTrue.markAsComparedEqualToNull(local); // from thereon it is set
				initsWhenFalse.markAsComparedEqualToNonNull(local); // from thereon it is set
			} else {
				flowContext.recordUsingNullReference(scope, local, reference,
						FlowContext.CAN_ONLY_NULL_NON_NULL | FlowContext.IN_COMPARISON_NON_NULL, flowInfo);
				initsWhenTrue.markAsComparedEqualToNonNull(local); // from thereon it is set
				initsWhenFalse.markAsComparedEqualToNull(local); // from thereon it is set
			}
			break;
		case FlowInfo.NON_NULL :
			if (((this.bits & OperatorMASK) >> OperatorSHIFT) == EQUAL_EQUAL) {
				flowContext.recordUsingNullReference(scope, local, reference,
						FlowContext.CAN_ONLY_NULL | FlowContext.IN_COMPARISON_NON_NULL, flowInfo);
				initsWhenTrue.markAsComparedEqualToNonNull(local); // from thereon it is set
			} else {
				flowContext.recordUsingNullReference(scope, local, reference,
						FlowContext.CAN_ONLY_NULL | FlowContext.IN_COMPARISON_NULL, flowInfo);
			}
			break;
	}
	// we do not impact enclosing try context because this kind of protection
	// does not preclude the variable from being null in an enclosing scope
	}

	public final void computeConstant(TypeBinding leftType, TypeBinding rightType) {
		if ((this.left.constant != Constant.NotAConstant) && (this.right.constant != Constant.NotAConstant)) {
			this.constant =
				Constant.computeConstantOperationEQUAL_EQUAL(
					left.constant,
					leftType.id,
					right.constant,
					rightType.id);
			if (((this.bits & OperatorMASK) >> OperatorSHIFT) == NOT_EQUAL)
				constant = BooleanConstant.fromValue(!constant.booleanValue());
		} else {
			this.constant = Constant.NotAConstant;
			// no optimization for null == null
		}
	}
	public boolean isCompactableOperation() {
		return false;
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			left.traverse(visitor, scope);
			right.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
	public int getASTType() {
		return IASTNode.EQUAL_EXPRESSION;
	
	}
}
