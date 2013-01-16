/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
import org.eclipse.mod.wst.jsdt.core.ast.IObjectLiteral;
import org.eclipse.mod.wst.jsdt.core.ast.IObjectLiteralField;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;


public class ObjectLiteral extends Expression implements IObjectLiteral {

	public ObjectLiteralField [] fields;
	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		if (fields==null || fields.length==0)
		{
			output.append("{}"); //$NON-NLS-1$
		}
		else
		{
			output.append("{\n"); //$NON-NLS-1$
			printIndent(indent+1, output);
			for (int i = 0; i < fields.length; i++) {
				if (i>0)
				{
					output.append(",\n"); //$NON-NLS-1$
					printIndent(indent+1, output);
				}
				fields[i].printExpression(indent, output);
			}
			output.append("\n"); //$NON-NLS-1$
			printIndent(indent, output);
			output.append("}"); //$NON-NLS-1$
		}
		return output;
	}
	

	public IObjectLiteralField[] getFields() {
		return this.fields;
	}
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (fields!=null)
				for (int i = 0; i < fields.length; i++) {
					fields[i].traverse(visitor, scope);
				}
		}
		visitor.endVisit(this, scope);
	}



	public int nullStatus(FlowInfo flowInfo) {
			return FlowInfo.NON_NULL; // constant expression cannot be null
	}


	public int getASTType() {
		return IASTNode.OBJECT_LITERAL;
	
	}
}
