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
import org.eclipse.mod.wst.jsdt.core.ast.IFunctionDeclaration;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.Scope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TagBits;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.parser.Parser;
import org.eclipse.mod.wst.jsdt.internal.compiler.problem.AbortMethod;

public class MethodDeclaration extends AbstractMethodDeclaration implements IFunctionDeclaration {

	public TypeReference returnType;

	/**
	 * FunctionDeclaration constructor comment.
	 */
	public MethodDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}


	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {

		//fill up the method body with statement
		if (ignoreFurtherInvestigation)
			return;
		parser.parse(this, unit);
	}

//	public void resolveStatements() {
//
//		// ========= abort on fatal error =============
//
//		super.resolveStatements();
//	}

	public void traverse(
		ASTVisitor visitor,
		 Scope classScope) {

		if (visitor.visit(this, classScope)) {
			if (this.javadoc != null) {
				this.javadoc.traverse(visitor, scope);
			}
			if (arguments != null) {
				int argumentLength = arguments.length;
				for (int i = 0; i < argumentLength; i++)
					arguments[i].traverse(visitor, scope);
			}
			if (statements != null) {
				int statementsLength = statements.length;
				for (int i = 0; i < statementsLength; i++)
					statements[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, classScope);
	}
	public void traverse(
			ASTVisitor visitor,
			BlockScope blockScope) {

			if (visitor.visit(this, blockScope)) {
				if (arguments != null) {
					int argumentLength = arguments.length;
					for (int i = 0; i < argumentLength; i++)
						arguments[i].traverse(visitor, scope);
				}
				if (statements != null) {
					int statementsLength = statements.length;
					for (int i = 0; i < statementsLength; i++)
						statements[i].traverse(visitor, scope);
				}
			}
			visitor.endVisit(this, blockScope);
		}

	public int getASTType() {
		return IASTNode.FUNCTION_DECLARATION;
	
	}
}
