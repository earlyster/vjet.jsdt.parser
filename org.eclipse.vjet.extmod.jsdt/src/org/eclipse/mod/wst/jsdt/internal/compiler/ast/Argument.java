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
import org.eclipse.mod.wst.jsdt.core.ast.IArgument;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ClassScope;

public class Argument extends LocalDeclaration implements IArgument {

	public char [] comment;

	public Argument(char[] name, long posNom, TypeReference tr, int modifiers) {

		super(name, (int) (posNom >>> 32), (int) posNom);
		this.declarationSourceEnd = (int) posNom;
		this.modifiers = modifiers;
		type = tr;
		this.bits |= IsLocalDeclarationReachable;
	}

	public char[] getComment() {
		return this.comment;
	}
	
	

	/**
	 * @see org.eclipse.wst.jsdt.internal.compiler.ast.AbstractVariableDeclaration#getKind()
	 */
	public int getKind() {
		return PARAMETER;
	}

	public boolean isVarArgs() {
		return this.type != null &&  (this.type.bits & IsVarArgs) != 0;
	}

	public StringBuffer print(int indent, StringBuffer output) {

		printIndent(indent, output);
		printModifiers(this.modifiers, output);
//		if (this.annotations != null) printAnnotations(this.annotations, output);

//		if (type == null) {
//			output.append("<no type> "); //$NON-NLS-1$
//		} else {
//			type.print(0, output).append(' ');
//		}
		return output.append(this.name);
	}

	public StringBuffer printStatement(int indent, StringBuffer output) {

		return print(indent, output).append(';');
	}

	// VJET MOD - remove resolve
//	public TypeBinding resolveForCatch(BlockScope scope) {
//
//		// resolution on an argument of a catch clause
//		// provide the scope with a side effect : insertion of a LOCAL
//		// that represents the argument. The type must be from JavaThrowable
//
//		ReferenceBinding javaLangError = scope.getJavaLangError();
//		TypeBinding exceptionType = this.type!=null ?
//			this.type.resolveType(scope, true /* check bounds*/) : javaLangError;
//		if (exceptionType == null) return null;
//		boolean hasError = false;
//
//		Binding existingVariable = scope.getBinding(name, Binding.VARIABLE, this, false /*do not resolve hidden field*/);
//		if (existingVariable != null && existingVariable.isValidBinding()){
////			if (existingVariable instanceof LocalVariableBinding && this.hiddenVariableDepth == 0) {
////				scope.problemReporter().redefineArgument(this);
////			} else {
//				scope.problemReporter().localVariableHiding(this, existingVariable, false);
////			}
//		}
//
//		this.binding = new LocalVariableBinding(this, exceptionType, modifiers, false); // argument decl, but local var  (where isArgument = false)
////		resolveAnnotations(scope, this.annotations, this.binding);
//
//		scope.addLocalVariable(binding);
//		if (hasError) return null;
//		return exceptionType;
//	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (type != null)
				type.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
	
	public void traverse(ASTVisitor visitor, ClassScope scope) {

		if (visitor.visit(this, scope)) {
			if (type != null)
				type.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
	
	public int getASTType() {
		return IASTNode.ARGUMENT;
	}
}
