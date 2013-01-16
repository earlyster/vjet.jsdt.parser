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
import org.eclipse.mod.wst.jsdt.core.ast.IArrayAllocationExpression;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class ArrayAllocationExpression extends Expression implements IArrayAllocationExpression {

	public TypeReference type;

	//dimensions.length gives the number of dimensions, but the
	// last ones may be nulled as in new int[4][5][][]
	public Expression[] dimensions;
	public ArrayInitializer initializer;


	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append("new "); //$NON-NLS-1$
		this.type.print(0, output);
		for (int i = 0; i < this.dimensions.length; i++) {
			if (this.dimensions[i] == null)
				output.append("[]"); //$NON-NLS-1$
			else {
				output.append('[');
				this.dimensions[i].printExpression(0, output);
				output.append(']');
			}
		}
		if (this.initializer != null) this.initializer.printExpression(0, output);
		return output;
	}



	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			int dimensionsLength = this.dimensions.length;
			this.type.traverse(visitor, scope);
			for (int i = 0; i < dimensionsLength; i++) {
				if (this.dimensions[i] != null)
					this.dimensions[i].traverse(visitor, scope);
			}
			if (this.initializer != null)
				this.initializer.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
	public int getASTType() {
		return IASTNode.ARRAY_ALLOCATION_EXPRESSION;
	
	}
}
