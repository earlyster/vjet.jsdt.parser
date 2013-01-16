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
import org.eclipse.mod.wst.jsdt.core.ast.IQualifiedAllocationExpression;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ReferenceBinding;

/**
 * Variation on allocation, where can optionally be specified any of:
 * - leading enclosing instance
 * - trailing anonymous type
 * - generic type arguments for generic constructor invocation
 */
public class QualifiedAllocationExpression extends AllocationExpression implements IQualifiedAllocationExpression {

	//qualification may be on both side
	public Expression enclosingInstance;
	public TypeDeclaration anonymousType;
	public ReferenceBinding superTypeBinding;

	public QualifiedAllocationExpression() {
		// for subtypes
	}

	public QualifiedAllocationExpression(TypeDeclaration anonymousType) {
		this.anonymousType = anonymousType;
		anonymousType.allocation = this;
	}

	

	public Expression enclosingInstance() {

		return this.enclosingInstance;
	}

	public boolean isSuperAccess() {

		// necessary to lookup super constructor of anonymous type
		return this.anonymousType != null;
	}


	public StringBuffer printExpression(int indent, StringBuffer output) {

		if (this.enclosingInstance != null)
			this.enclosingInstance.printExpression(0, output).append('.');
		super.printExpression(0, output);
		if (this.anonymousType != null) {
			this.anonymousType.print(indent, output);
		}
		return output;
	}

//	public TypeBinding resolveType(BlockScope scope) {
//
//		// added for code assist...cannot occur with 'normal' code
//		if (this.anonymousType == null && this.enclosingInstance == null) {
//			return super.resolveType(scope);
//		}
//
//		// Propagate the type checking to the arguments, and checks if the constructor is defined.
//		// ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
//		// ClassInstanceCreationExpression ::= Name '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
//
//		this.constant = Constant.NotAConstant;
//		TypeBinding enclosingInstanceType = null;
//		TypeBinding receiverType = null;
//		boolean hasError = false;
//		boolean argsContainCast = false;
//
//		if (this.enclosingInstance != null) {
//			if ((enclosingInstanceType = this.enclosingInstance.resolveType(scope)) == null){
//				hasError = true;
//			} else {
//				receiverType = ((SingleTypeReference) this.type).resolveTypeEnclosing(scope, (ReferenceBinding) enclosingInstanceType);
//			}
//		} else {
//			if (this.type == null) {
//				// initialization of an enum constant
//				receiverType = scope.enclosingSourceType();
//			} else {
//				receiverType = this.type.resolveType(scope, true /* check bounds*/);
//				checkParameterizedAllocation: {
//					if (receiverType == null) break checkParameterizedAllocation;
//				}
//			}
//		}
//		if (receiverType == null) {
//			hasError = true;
//		}
//
//		// will check for null after args are resolved
//		TypeBinding[] argumentTypes = Binding.NO_PARAMETERS;
//		if (this.arguments != null) {
//			int length = this.arguments.length;
//			argumentTypes = new TypeBinding[length];
//			for (int i = 0; i < length; i++) {
//				Expression argument = this.arguments[i];
//				if ((argumentTypes[i] = argument.resolveType(scope)) == null){
//					hasError = true;
//				}
//			}
//		}
//		// limit of fault-tolerance
//		if (hasError) {
//			if (receiverType instanceof ReferenceBinding) {
//				// record a best guess, for clients who need hint about possible contructor match
//				int length = this.arguments  == null ? 0 : this.arguments.length;
//				TypeBinding[] pseudoArgs = new TypeBinding[length];
//				for (int i = length; --i >= 0;) {
//					pseudoArgs[i] = argumentTypes[i] == null ? TypeBinding.NULL : argumentTypes[i]; // replace args with errors with null type
//				}
//				this.binding = scope.findMethod((ReferenceBinding) receiverType, TypeConstants.INIT, pseudoArgs, this);
//				if (this.binding != null && !this.binding.isValidBinding()) {
//					MethodBinding closestMatch = ((ProblemMethodBinding)this.binding).closestMatch;
//					// record the closest match, for clients who may still need hint about possible method match
//					if (closestMatch != null) {
//						this.binding = closestMatch;
//						MethodBinding closestMatchOriginal = closestMatch.original();
//						if ((closestMatchOriginal.isPrivate() || closestMatchOriginal.declaringClass.isLocalType()) && !scope.isDefinedInMethod(closestMatchOriginal)) {
//							// ignore cases where method is used from within inside itself (e.g. direct recursions)
//							closestMatchOriginal.modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
//						}
//					}
//				}
//
//			}
//			return this.resolvedType = receiverType;
//		}
//		if (this.anonymousType == null) {
//			ReferenceBinding allocationType = (ReferenceBinding) receiverType;
//			if ((this.binding = scope.getConstructor(allocationType, argumentTypes, this)).isValidBinding()) {
//				if (isMethodUseDeprecated(this.binding, scope, true)) {
//					scope.problemReporter().deprecatedMethod(this.binding, this);
//				}
//				checkInvocationArguments(scope, null, allocationType, this.binding, this.arguments, argumentTypes, argsContainCast, this);
//			} else {
//				if (this.binding.declaringClass == null) {
//					this.binding.declaringClass = allocationType;
//				}
//				scope.problemReporter().invalidConstructor(this, this.binding);
//				return this.resolvedType = receiverType;
//			}
//
//			// The enclosing instance must be compatible with the innermost enclosing type
//			ReferenceBinding expectedType = this.binding.declaringClass.enclosingType();
//			if (expectedType != enclosingInstanceType) // must call before computeConversion() and typeMismatchError()
//				scope.compilationUnitScope().recordTypeConversion(expectedType, enclosingInstanceType);
//			if (enclosingInstanceType.isCompatibleWith(expectedType) || scope.isBoxingCompatibleWith(enclosingInstanceType, expectedType)) {
//				return this.resolvedType = receiverType;
//			}
//			scope.problemReporter().typeMismatchError(enclosingInstanceType, expectedType, this.enclosingInstance);
//			return this.resolvedType = receiverType;
//		}
//
//		// anonymous type scenario
//		// an anonymous class inherits from java.lang.Object when declared "after" an interface
//		this.superTypeBinding = (ReferenceBinding) receiverType;
//		// insert anonymous type in scope
//		scope.addAnonymousType(this.anonymousType, (ReferenceBinding) receiverType);
//		this.anonymousType.resolve(scope);
//
//		// find anonymous super constructor
//		MethodBinding inheritedBinding = scope.getConstructor(this.superTypeBinding, argumentTypes, this);
//		if (!inheritedBinding.isValidBinding()) {
//			if (inheritedBinding.declaringClass == null) {
//				inheritedBinding.declaringClass = this.superTypeBinding;
//			}
//			scope.problemReporter().invalidConstructor(this, inheritedBinding);
//			return this.resolvedType = this.anonymousType.binding;
//		}
//		if (this.enclosingInstance != null) {
//			ReferenceBinding targetEnclosing = inheritedBinding.declaringClass.enclosingType();
//			if (targetEnclosing == null) {
//				return this.resolvedType = this.anonymousType.binding;
//			} else if (!enclosingInstanceType.isCompatibleWith(targetEnclosing) && !scope.isBoxingCompatibleWith(enclosingInstanceType, targetEnclosing)) {
//				scope.problemReporter().typeMismatchError(enclosingInstanceType, targetEnclosing, this.enclosingInstance);
//				return this.resolvedType = this.anonymousType.binding;
//			}
//		}
//		if (this.arguments != null)
//			checkInvocationArguments(scope, null, this.superTypeBinding, inheritedBinding, this.arguments, argumentTypes, argsContainCast, this);
//
//		// Update the anonymous inner class : superclass, interface
//		this.binding = this.anonymousType.createDefaultConstructorWithBinding(inheritedBinding);
//		return this.resolvedType = this.anonymousType.binding; // 1.2 change
//	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (this.enclosingInstance != null)
				this.enclosingInstance.traverse(visitor, scope);
			if (this.type != null) // case of enum constant
				this.type.traverse(visitor, scope);
			if (this.arguments != null) {
				int argumentsLength = this.arguments.length;
				for (int i = 0; i < argumentsLength; i++)
					this.arguments[i].traverse(visitor, scope);
			}
			if (this.anonymousType != null)
				this.anonymousType.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
	public int getASTType() {
		return IASTNode.QUALIFIED_ALLOCATION_EXPRESSION;
	
	}
}
