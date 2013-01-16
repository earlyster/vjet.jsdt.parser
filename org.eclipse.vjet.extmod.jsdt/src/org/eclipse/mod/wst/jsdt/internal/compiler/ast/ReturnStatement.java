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
import org.eclipse.mod.wst.jsdt.core.ast.IExpression;
import org.eclipse.mod.wst.jsdt.core.ast.IReturnStatement;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.InsideSubRoutineFlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.MethodScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class ReturnStatement extends Statement implements IReturnStatement {

	public Expression expression;
	public SubRoutineStatement[] subroutines;
	public LocalVariableBinding saveValueVariable;
	public int initStateIndex = -1;

public ReturnStatement(Expression expression, int sourceStart, int sourceEnd) {
	this.sourceStart = sourceStart;
	this.sourceEnd = sourceEnd;
	this.expression = expression ;
}



public boolean needValue() {
	return this.saveValueVariable != null
	|| ((this.bits & ASTNode.IsAnySubRoutineEscaping) == 0);
}

public void prepareSaveValueLocation(TryStatement targetTryStatement){
	this.saveValueVariable = targetTryStatement.secretReturnValue;
}

public StringBuffer printStatement(int tab, StringBuffer output){
	printIndent(tab, output).append("return "); //$NON-NLS-1$
	if (this.expression != null )
		this.expression.printExpression(0, output) ;
	return output.append(';');
}


public void traverse(ASTVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		if (this.expression != null)
			this.expression.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
public int getASTType() {
	return IASTNode.RETURN_STATEMENT;

}

public IExpression getExpression() {
	return this.expression;
}
}
