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
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.Binding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ClassScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.MethodScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.Scope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.mod.wst.jsdt.internal.compiler.parser.Parser;
import org.eclipse.mod.wst.jsdt.internal.compiler.problem.AbortMethod;

public class Clinit extends AbstractMethodDeclaration  {

	public Clinit(CompilationResult compilationResult) {
		super(compilationResult);
		modifiers = 0;
		selector = TypeConstants.CLINIT;
	}

	public boolean isClinit() {

		return true;
	}

	public boolean isInitializationMethod() {

		return true;
	}

	public boolean isStatic() {

		return true;
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		//the clinit is filled by hand ....
	}

	public StringBuffer print(int tab, StringBuffer output) {

		printIndent(tab, output).append("<clinit>()"); //$NON-NLS-1$
		printBody(tab + 1, output);
		return output;
	}

	public void traverse(
		ASTVisitor visitor,
		ClassScope classScope) {

		visitor.visit(this, classScope);
		visitor.endVisit(this, classScope);
	}


	public int getASTType() {
		return IASTNode.CL_INIT;
	
	}
}
