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
import org.eclipse.mod.wst.jsdt.core.ast.IInstanceOfExpression;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TagBits;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class InstanceOfExpression extends OperatorExpression implements IInstanceOfExpression {

	public Expression expression;
	public TypeReference type;

	public InstanceOfExpression(Expression expression, TypeReference type) {

		this.expression = expression;
		this.type = type;
		type.bits |= IgnoreRawTypeCheck; // no need to worry about raw type usage
		this.bits |= INSTANCEOF << OperatorSHIFT;
		this.sourceStart = expression.sourceStart;
		this.sourceEnd = type.sourceEnd;
	}



	public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {

		expression.printExpression(indent, output).append(" instanceof "); //$NON-NLS-1$
		return type.print(0, output);
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			expression.traverse(visitor, scope);
			type.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
	public int getASTType() {
		return IASTNode.INSTANCEOF_EXPRESSION;
	
	}
}
