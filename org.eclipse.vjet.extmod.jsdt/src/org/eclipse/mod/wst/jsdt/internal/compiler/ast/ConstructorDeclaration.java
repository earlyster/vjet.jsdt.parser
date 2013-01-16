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

import java.util.ArrayList;

import org.eclipse.mod.wst.jsdt.core.ast.IASTNode;
import org.eclipse.mod.wst.jsdt.core.ast.IConstructorDeclaration;
import org.eclipse.mod.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ClassScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.Scope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TagBits;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeIds;
import org.eclipse.mod.wst.jsdt.internal.compiler.parser.Parser;
import org.eclipse.mod.wst.jsdt.internal.compiler.problem.AbortMethod;

public class ConstructorDeclaration extends AbstractMethodDeclaration implements IConstructorDeclaration {

	public ExplicitConstructorCall constructorCall;

	public boolean isDefaultConstructor = false;

public ConstructorDeclaration(CompilationResult compilationResult){
	super(compilationResult);
}


public boolean isConstructor() {
	return true;
}

public boolean isDefaultConstructor() {
	return this.isDefaultConstructor;
}

public boolean isInitializationMethod() {
	return true;
}


public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
	//fill up the constructor body with its statements
	if (this.ignoreFurtherInvestigation)
		return;
	if (this.isDefaultConstructor && this.constructorCall == null){
		this.constructorCall = SuperReference.implicitSuperConstructorCall();
		this.constructorCall.sourceStart = this.sourceStart;
		this.constructorCall.sourceEnd = this.sourceEnd;
		return;
	}
	parser.parse(this, unit);

}

public StringBuffer printBody(int indent, StringBuffer output) {
	output.append(" {"); //$NON-NLS-1$
	if (this.constructorCall != null) {
		output.append('\n');
		this.constructorCall.printStatement(indent, output);
	}
	if (this.statements != null) {
		for (int i = 0; i < this.statements.length; i++) {
			output.append('\n');
			this.statements[i].printStatement(indent, output);
		}
	}
	output.append('\n');
	printIndent(indent == 0 ? 0 : indent - 1, output).append('}');
	return output;
}

//public void resolveJavadoc() {
//	if (this.binding == null || this.javadoc != null) {
//		super.resolveJavadoc();
//	} else if (!this.isDefaultConstructor) {
//		this.scope.problemReporter().javadocMissing(this.sourceStart, this.sourceEnd, this.binding.modifiers);
//	}
//}

/*
 * Type checking for constructor, just another method, except for special check
 * for recursive constructor invocations.
 */
//public void resolveStatements() {
//	SourceTypeBinding sourceType = this.scope.enclosingSourceType();
//	if (!CharOperation.equals(sourceType.sourceName, this.selector)){
//		this.scope.problemReporter().missingReturnType(this);
//	}
//	if (this.binding != null && !this.binding.isPrivate()) {
//		sourceType.tagBits |= TagBits.HasNonPrivateConstructor;
//	}
//	// if null ==> an error has occurs at parsing time ....
//	if (this.constructorCall != null) {
//		if (sourceType.id == TypeIds.T_JavaLangObject
//				&& this.constructorCall.accessMode != ExplicitConstructorCall.This) {
//			this.constructorCall = null;
//		} else {
//			this.constructorCall.resolve(this.scope);
//		}
//	}
//	super.resolveStatements();
//}

public void traverse(ASTVisitor visitor,	ClassScope classScope) {
	if (visitor.visit(this, classScope)) {
		if (this.javadoc != null) {
			this.javadoc.traverse(visitor, this.scope);
		}
		if (this.arguments != null) {
			int argumentLength = this.arguments.length;
			for (int i = 0; i < argumentLength; i++)
				this.arguments[i].traverse(visitor, this.scope);
		}
		if (this.constructorCall != null)
			this.constructorCall.traverse(visitor, this.scope);
		if (this.statements != null) {
			int statementsLength = this.statements.length;
			for (int i = 0; i < statementsLength; i++)
				this.statements[i].traverse(visitor, this.scope);
		}
	}
	visitor.endVisit(this, classScope);
}
public int getASTType() {
	return IASTNode.CONSTRUCTOR_DECLARATION;

}
}
