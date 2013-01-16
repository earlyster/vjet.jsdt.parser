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
import org.eclipse.mod.wst.jsdt.core.ast.IQualifiedThisReference;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ClassScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class QualifiedThisReference extends ThisReference implements IQualifiedThisReference {

	public TypeReference qualification;
	ReferenceBinding currentCompatibleType;

	public QualifiedThisReference(TypeReference name, int sourceStart, int sourceEnd) {
		super(sourceStart, sourceEnd);
		qualification = name;
		name.bits |= IgnoreRawTypeCheck; // no need to worry about raw type usage
		this.sourceStart = name.sourceStart;
	}


	public StringBuffer printExpression(int indent, StringBuffer output) {

		return qualification.print(0, output).append(".this"); //$NON-NLS-1$
	}

	public void traverse(
		ASTVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			qualification.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}

	public void traverse(
			ASTVisitor visitor,
			ClassScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			qualification.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
	public int getASTType() {
		return IASTNode.QUALIFIED_THIS_REFERENCE;
	
	}
}
