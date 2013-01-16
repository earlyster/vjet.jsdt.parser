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
import org.eclipse.mod.wst.jsdt.core.ast.IAllocationExpression;
import org.eclipse.mod.wst.jsdt.core.ast.IExpression;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ReferenceBinding;

public class AllocationExpression extends Expression implements InvocationSite, IAllocationExpression {
		
	public TypeReference type;
	public Expression[] arguments;
//	public MethodBinding binding;							// exact binding resulting from lookup
//	protected MethodBinding codegenBinding;	// actual binding used for code generation (if no synthetic accessor)
    public Expression member;
	public boolean isShort;
	
	
	public Expression enclosingInstance() {
		return null;
	}
	
	public boolean isSuperAccess() {
		return false;
	}
	
	public boolean isTypeAccess() {
		return true;
	}
	

	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append("new "); //$NON-NLS-1$
		member.print(indent, output);
		
		if (type != null) { // type null for enum constant initializations
			type.printExpression(0, output); 
		}
		if (!isShort)
		{
			output.append('(');
			if (arguments != null) {
				for (int i = 0; i < arguments.length; i++) {
					if (i > 0) output.append(", "); //$NON-NLS-1$
					arguments[i].printExpression(0, output);
				}
			}
			output.append(')');
		} 
		return output;
	}
	
	
	public void setActualReceiverType(ReferenceBinding receiverType) {
		// ignored
	}
	
	public void setDepth(int i) {
		// ignored
	}
	
	public void setFieldIndex(int i) {
		// ignored
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.member!=null)
				this.member.traverse(visitor, scope);
			else if (this.type != null) { // enum constant scenario
				this.type.traverse(visitor, scope);
			}
			if (this.arguments != null) {
				for (int i = 0, argumentsLength = this.arguments.length; i < argumentsLength; i++)
					this.arguments[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
	public int getASTType() {
		return IASTNode.ALLOCATION_EXPRESSION;
	
	}
	
	public IExpression getMember() {
		return this.member;
	}
	
}
