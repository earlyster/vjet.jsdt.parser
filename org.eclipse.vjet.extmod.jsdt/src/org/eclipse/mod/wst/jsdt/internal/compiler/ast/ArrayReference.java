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
import org.eclipse.mod.wst.jsdt.core.ast.IArrayReference;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class ArrayReference extends Reference implements IArrayReference {

	public Expression receiver;
	public Expression position;

	public ArrayReference(Expression rec, Expression pos) {
		this.receiver = rec;
		this.position = pos;
		sourceStart = rec.sourceStart;
	}


	public StringBuffer printExpression(int indent, StringBuffer output) {

		receiver.printExpression(0, output).append('[');
		return position.printExpression(0, output).append(']');
	}

	// VJET MOD remove resolve
//	public TypeBinding resolveType(BlockScope scope) {
//
//		constant = Constant.NotAConstant;
////		if (receiver instanceof CastExpression	// no cast check for ((type[])null)[0]
////				&& ((CastExpression)receiver).innermostCastedExpression() instanceof NullLiteral) {
////			this.receiver.bits |= DisableUnnecessaryCastCheck; // will check later on
////		}
//		TypeBinding arrayType = receiver.resolveType(scope);
//		if (arrayType != null) {
//			if (arrayType.isArrayType()) {
//				TypeBinding elementType = ((ArrayBinding) arrayType).elementsType();
//				this.resolvedType = elementType;
//			} else {
////				scope.problemReporter().referenceMustBeArrayTypeAt(arrayType, this);
//				this.resolvedType=TypeBinding.UNKNOWN;
//			}
//		}
//		else 
//			this.resolvedType=TypeBinding.UNKNOWN;
//		  position.resolveTypeExpecting(scope, new TypeBinding[] {scope.getJavaLangNumber(),scope.getJavaLangString(),TypeBinding.ANY});
////		if (positionType != null) {
////			position.computeConversion(scope, TypeBinding.INT, positionType);
////		}
//		return this.resolvedType;
//	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			receiver.traverse(visitor, scope);
			position.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
	public int getASTType() {
		return IASTNode.ARRAY_REFERENCE;
	
	}
}
