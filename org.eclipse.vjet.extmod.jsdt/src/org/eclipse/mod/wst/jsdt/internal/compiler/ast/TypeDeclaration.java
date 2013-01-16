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
import org.eclipse.mod.wst.jsdt.core.ast.ITypeDeclaration;
import org.eclipse.mod.wst.jsdt.core.compiler.CategorizedProblem;
import org.eclipse.mod.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.mod.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.Binding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ClassScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.FieldBinding;
//import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.MethodScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TagBits;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeIds;
import org.eclipse.mod.wst.jsdt.internal.compiler.parser.Parser;
import org.eclipse.mod.wst.jsdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.mod.wst.jsdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.mod.wst.jsdt.internal.compiler.problem.AbortMethod;
import org.eclipse.mod.wst.jsdt.internal.compiler.problem.AbortType;
import org.eclipse.mod.wst.jsdt.internal.compiler.problem.ProblemSeverities;

public class TypeDeclaration extends Statement implements ProblemSeverities, ReferenceContext, ITypeDeclaration {
	// Type decl kinds
	public static final int CLASS_DECL = 1;

	public int modifiers = ClassFileConstants.AccDefault;
	public int modifiersSourceStart;
	public char[] name;
	public TypeReference superclass;
	public FieldDeclaration[] fields;
	public AbstractMethodDeclaration[] methods;
	public TypeDeclaration[] memberTypes;
	public SourceTypeBinding binding= new SourceTypeBinding(null,null,null);
	public ClassScope scope;
	public MethodScope initializerScope;
	public MethodScope staticInitializerScope;
	public boolean ignoreFurtherInvestigation = false;
	public int maxFieldCount;
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int bodyStart;
	public int bodyEnd; // doesn't include the trailing comment if any.
	public CompilationResult compilationResult;
	public MethodDeclaration[] missingAbstractMethods;
	public Javadoc javadoc;

	public QualifiedAllocationExpression allocation; // for anonymous only
	public TypeDeclaration enclosingType; // for member types only

public TypeDeclaration(CompilationResult compilationResult){
	this.compilationResult = compilationResult;
}

/*
 *	We cause the compilation task to abort to a given extent.
 */
public void abort(int abortLevel, CategorizedProblem problem) {
	switch (abortLevel) {
		case AbortCompilation :
			throw new AbortCompilation(this.compilationResult, problem);
		case AbortCompilationUnit :
			throw new AbortCompilationUnit(this.compilationResult, problem);
		case AbortMethod :
			throw new AbortMethod(this.compilationResult, problem);
		default :
			throw new AbortType(this.compilationResult, problem);
	}
}

/**
 * This method is responsible for adding a <clinit> method declaration to the type method collections.
 * Note that this implementation is inserting it in first place (as VAJ or javac), and that this
 * impacts the behavior of the method ConstantPool.resetForClinit(int. int), in so far as
 * the latter will have to reset the constant pool state accordingly (if it was added first, it does
 * not need to preserve some of the method specific cached entries since this will be the first method).
 * inserts the clinit method declaration in the first position.
 *
 * @see org.eclipse.wst.jsdt.internal.compiler.codegen.ConstantPool#resetForClinit(int, int)
 */
public final void addClinit() {
	//see comment on needClassInitMethod
	if (needClassInitMethod()) {
		int length;
		AbstractMethodDeclaration[] methodDeclarations;
		if ((methodDeclarations = this.methods) == null) {
			length = 0;
			methodDeclarations = new AbstractMethodDeclaration[1];
		} else {
			length = methodDeclarations.length;
			System.arraycopy(
				methodDeclarations,
				0,
				(methodDeclarations = new AbstractMethodDeclaration[length + 1]),
				1,
				length);
		}
		Clinit clinit = new Clinit(this.compilationResult);
		methodDeclarations[0] = clinit;
		// clinit is added in first location, so as to minimize the use of ldcw (big consumer of constant inits)
		clinit.declarationSourceStart = clinit.sourceStart = this.sourceStart;
		clinit.declarationSourceEnd = clinit.sourceEnd = this.sourceEnd;
		clinit.bodyEnd = this.sourceEnd;
		this.methods = methodDeclarations;
	}
}




public CompilationResult compilationResult() {
	return this.compilationResult;
}

public ConstructorDeclaration createDefaultConstructor(	boolean needExplicitConstructorCall, boolean needToInsert) {
	//Add to method'set, the default constuctor that just recall the
	//super constructor with no arguments
	//The arguments' type will be positionned by the TC so just use
	//the default int instead of just null (consistency purpose)

	//the constructor
	ConstructorDeclaration constructor = new ConstructorDeclaration(this.compilationResult);
	constructor.bits |= ASTNode.IsDefaultConstructor;
	constructor.selector = this.name;
	constructor.modifiers = this.modifiers & ExtraCompilerModifiers.AccVisibilityMASK;

	//if you change this setting, please update the
	//SourceIndexer2.buildTypeDeclaration(TypeDeclaration,char[]) method
	constructor.declarationSourceStart = constructor.sourceStart = this.sourceStart;
	constructor.declarationSourceEnd =
		constructor.sourceEnd = constructor.bodyEnd = this.sourceEnd;

	//the super call inside the constructor
	if (needExplicitConstructorCall) {
		constructor.constructorCall = SuperReference.implicitSuperConstructorCall();
		constructor.constructorCall.sourceStart = this.sourceStart;
		constructor.constructorCall.sourceEnd = this.sourceEnd;
	}

	//adding the constructor in the methods list: rank is not critical since bindings will be sorted
	if (needToInsert) {
		if (this.methods == null) {
			this.methods = new AbstractMethodDeclaration[] { constructor };
		} else {
			AbstractMethodDeclaration[] newMethods;
			System.arraycopy(
				this.methods,
				0,
				newMethods = new AbstractMethodDeclaration[this.methods.length + 1],
				1,
				this.methods.length);
			newMethods[0] = constructor;
			this.methods = newMethods;
		}
	}
	return constructor;
}

/**
 * Find the matching parse node, answers null if nothing found
 */
public FieldDeclaration declarationOf(FieldBinding fieldBinding) {
	if (fieldBinding != null && this.fields != null) {
		for (int i = 0, max = this.fields.length; i < max; i++) {
			FieldDeclaration fieldDecl;
			if ((fieldDecl = this.fields[i]).binding == fieldBinding)
				return fieldDecl;
		}
	}
	return null;
}





public boolean hasErrors() {
	return this.ignoreFurtherInvestigation;
}

public final static int kind(int flags) {
	return TypeDeclaration.CLASS_DECL;
}

/**
 * A <clinit> will be requested as soon as static fields or assertions are present. It will be eliminated during
 * classfile creation if no bytecode was actually produced based on some optimizations/compiler settings.
 */
public final boolean needClassInitMethod() {
	// always need a <clinit> when assertions are present
	if ((this.bits & ASTNode.ContainsAssertion) != 0)
		return true;

	if (this.fields != null) {
		for (int i = this.fields.length; --i >= 0;) {
			FieldDeclaration field = this.fields[i];
			//need to test the modifier directly while there is no binding yet
			if ((field.modifiers & ClassFileConstants.AccStatic) != 0)
				return true; // TODO (philippe) shouldn't it check whether field is initializer or has some initial value ?
		}
	}
	return false;
}

public void parseMethod(Parser parser, CompilationUnitDeclaration unit) {
	//connect method bodies
	if (unit.ignoreMethodBodies)
		return;

	//members
	if (this.memberTypes != null) {
		int length = this.memberTypes.length;
		for (int i = 0; i < length; i++)
			this.memberTypes[i].parseMethod(parser, unit);
	}

	//methods
	if (this.methods != null) {
		int length = this.methods.length;
		for (int i = 0; i < length; i++) {
			this.methods[i].parseStatements(parser, unit);
		}
	}

	//initializers
	if (this.fields != null) {
		int length = this.fields.length;
		for (int i = 0; i < length; i++) {
			final FieldDeclaration fieldDeclaration = this.fields[i];
			switch(fieldDeclaration.getKind()) {
				case AbstractVariableDeclaration.INITIALIZER:
					((Initializer) fieldDeclaration).parseStatements(parser, this, unit);
					break;
			}
		}
	}
}

public StringBuffer print(int indent, StringBuffer output) {
	if (this.javadoc != null) {
		this.javadoc.print(indent, output);
	}
	if ((this.bits & ASTNode.IsAnonymousType) == 0) {
		printIndent(indent, output);
		printHeader(0, output);
	}
	return printBody(indent, output);
}

public StringBuffer printBody(int indent, StringBuffer output) {
	output.append(" {"); //$NON-NLS-1$
	if (this.memberTypes != null) {
		for (int i = 0; i < this.memberTypes.length; i++) {
			if (this.memberTypes[i] != null) {
				output.append('\n');
				this.memberTypes[i].print(indent + 1, output);
			}
		}
	}
	if (this.fields != null) {
		for (int fieldI = 0; fieldI < this.fields.length; fieldI++) {
			if (this.fields[fieldI] != null) {
				output.append('\n');
				this.fields[fieldI].print(indent + 1, output);
			}
		}
	}
	if (this.methods != null) {
		for (int i = 0; i < this.methods.length; i++) {
			if (this.methods[i] != null) {
				output.append('\n');
				this.methods[i].print(indent + 1, output);
			}
		}
	}
	output.append('\n');
	return printIndent(indent, output).append('}');
}

public StringBuffer printHeader(int indent, StringBuffer output) {
	printModifiers(this.modifiers, output);

	switch (kind(this.modifiers)) {
		case TypeDeclaration.CLASS_DECL :
			output.append("class "); //$NON-NLS-1$
			break;
	}
	output.append(this.name);
	
	if (this.superclass != null) {
		output.append(" extends ");  //$NON-NLS-1$
		this.superclass.print(0, output);
	}
	return output;
}

public StringBuffer printStatement(int tab, StringBuffer output) {
	return print(tab, output);
}


//
//public void resolve() {
//	SourceTypeBinding sourceType = this.binding;
//	if (sourceType == null) {
//		this.ignoreFurtherInvestigation = true;
//		return;
//	}
//	try {
//		if ((this.bits & ASTNode.UndocumentedEmptyBlock) != 0) {
//			this.scope.problemReporter().undocumentedEmptyBlock(this.bodyStart-1, this.bodyEnd);
//		}
//
//		// generics (and non static generic members) cannot extend Throwable
//		if (sourceType.findSuperTypeErasingTo(TypeIds.T_JavaLangThrowable, true) != null) {
//			ReferenceBinding current = sourceType;
//			checkEnclosedInGeneric : do {
//				if (current.isStatic()) break checkEnclosedInGeneric;
//				if (current.isLocalType()) {
//					NestedTypeBinding nestedType = (NestedTypeBinding) current;
//					if (nestedType.scope.methodScope().isStatic) break checkEnclosedInGeneric;
//				}
//			} while ((current = current.enclosingType()) != null);
//		}
//		this.maxFieldCount = 0;
//		int lastVisibleFieldID = -1;
//
//		if (this.memberTypes != null) {
//			for (int i = 0, count = this.memberTypes.length; i < count; i++) {
//				this.memberTypes[i].resolve(this.scope);
//			}
//		}
//		if (this.fields != null) {
//			for (int i = 0, count = this.fields.length; i < count; i++) {
//				FieldDeclaration field = this.fields[i];
//				switch(field.getKind()) {
//					case AbstractVariableDeclaration.FIELD:
//						FieldBinding fieldBinding = field.binding;
//						if (fieldBinding == null) {
//							// still discover secondary errors
//							if (field.initialization != null) field.initialization.resolve(field.isStatic() ? this.staticInitializerScope : this.initializerScope);
//							this.ignoreFurtherInvestigation = true;
//							continue;
//						}
//						this.maxFieldCount++;
//						lastVisibleFieldID = field.binding.id;
//						break;
//
//					case AbstractVariableDeclaration.INITIALIZER:
//						 ((Initializer) field).lastVisibleFieldID = lastVisibleFieldID + 1;
//						break;
//				}
//				field.resolve(field.isStatic() ? this.staticInitializerScope : this.initializerScope);
//			}
//		}
//
//		if (this.methods != null) {
//			for (int i = 0, count = this.methods.length; i < count; i++) {
//				this.methods[i].resolve(this.scope);
//			}
//		}
//		// Resolve javadoc
//		if (this.javadoc != null) {
////			if (this.scope != null && (this.name != TypeConstants.PACKAGE_INFO_NAME)) {
////				// if the type is package-info, the javadoc was resolved as part of the compilation unit javadoc
////				this.javadoc.resolve(this.scope);
////			}
//		} else if (sourceType != null && !sourceType.isLocalType()) {
//			this.scope.problemReporter().javadocMissing(this.sourceStart, this.sourceEnd, sourceType.modifiers);
//		}
//
//	} catch (AbortType e) {
//		this.ignoreFurtherInvestigation = true;
//		return;
//	}
//}

/**
 * Resolve a local type declaration
 */
//public void resolve(BlockScope blockScope) {
//
//	// need to build its scope first and proceed with binding's creation
//	if ((this.bits & ASTNode.IsAnonymousType) == 0) {
//		// check collision scenarii
//		blockScope.addLocalType(this);
//	}
//
//	if (this.binding != null) {
//		// remember local types binding for innerclass emulation propagation
//		blockScope.referenceCompilationUnit().record((LocalTypeBinding)this.binding);
//
//		// binding is not set if the receiver could not be created
//		resolve();
//		updateMaxFieldCount();
//	}
//}

/**
 * Resolve a member type declaration (can be a local member)
 */
//public void resolve(ClassScope upperScope) {
//	// member scopes are already created
//	// request the construction of a binding if local member type
//
//	if (this.binding != null && this.binding instanceof LocalTypeBinding) {
//		// remember local types binding for innerclass emulation propagation
//		upperScope.referenceCompilationUnit().record((LocalTypeBinding)this.binding);
//	}
//	resolve();
//	updateMaxFieldCount();
//}

/**
 * Resolve a top level type declaration
 */
//public void resolve(CompilationUnitScope upperScope) {
//	// top level : scope are already created
//	resolve();
//	updateMaxFieldCount();
//}

public void tagAsHavingErrors() {
	this.ignoreFurtherInvestigation = true;
}

/**
 *	Iteration for a package member type
 *
 */
public void traverse(ASTVisitor visitor, CompilationUnitScope unitScope) {

	if (this.ignoreFurtherInvestigation)
		return;
	try {
		if (visitor.visit(this, unitScope)) {
			if (this.javadoc != null) {
				this.javadoc.traverse(visitor, this.scope);
			}
			if (this.superclass != null)
				this.superclass.traverse(visitor, this.scope);
			if (this.memberTypes != null) {
				int length = this.memberTypes.length;
				for (int i = 0; i < length; i++)
					this.memberTypes[i].traverse(visitor, this.scope);
			}
			if (this.fields != null) {
				int length = this.fields.length;
				for (int i = 0; i < length; i++) {
					FieldDeclaration field;
					if ((field = this.fields[i]).isStatic()) {
						field.traverse(visitor, this.staticInitializerScope);
					} else {
						field.traverse(visitor, this.initializerScope);
					}
				}
			}
			if (this.methods != null) {
				int length = this.methods.length;
				for (int i = 0; i < length; i++)
					this.methods[i].traverse(visitor, this.scope);
			}
		}
		visitor.endVisit(this, unitScope);
	} catch (AbortType e) {
		// silent abort
	}
}

/**
 *	Iteration for a local innertype
 */
public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	if (this.ignoreFurtherInvestigation)
		return;
	try {
		if (visitor.visit(this, blockScope)) {
			if (this.javadoc != null) {
				this.javadoc.traverse(visitor, this.scope);
			}
			if (this.superclass != null)
				this.superclass.traverse(visitor, this.scope);
			if (this.memberTypes != null) {
				int length = this.memberTypes.length;
				for (int i = 0; i < length; i++)
					this.memberTypes[i].traverse(visitor, this.scope);
			}
			if (this.fields != null) {
				int length = this.fields.length;
				for (int i = 0; i < length; i++) {
					FieldDeclaration field;
					if ((field = this.fields[i]).isStatic()) {
						// local type cannot have static fields
					} else {
						field.traverse(visitor, this.initializerScope);
					}
				}
			}
			if (this.methods != null) {
				int length = this.methods.length;
				for (int i = 0; i < length; i++)
					this.methods[i].traverse(visitor, this.scope);
			}
		}
		visitor.endVisit(this, blockScope);
	} catch (AbortType e) {
		// silent abort
	}
}

/**
 *	Iteration for a member innertype
 *
 */
public void traverse(ASTVisitor visitor, ClassScope classScope) {
	if (this.ignoreFurtherInvestigation)
		return;
	try {
		if (visitor.visit(this, classScope)) {
			if (this.javadoc != null) {
				this.javadoc.traverse(visitor, scope);
			}
			if (this.superclass != null)
				this.superclass.traverse(visitor, this.scope);
			if (this.memberTypes != null) {
				int length = this.memberTypes.length;
				for (int i = 0; i < length; i++)
					this.memberTypes[i].traverse(visitor, this.scope);
			}
			if (this.fields != null) {
				int length = this.fields.length;
				for (int i = 0; i < length; i++) {
					FieldDeclaration field;
					if ((field = this.fields[i]).isStatic()) {
						field.traverse(visitor, this.staticInitializerScope);
					} else {
						field.traverse(visitor, this.initializerScope);
					}
				}
			}
			if (this.methods != null) {
				int length = this.methods.length;
				for (int i = 0; i < length; i++)
					this.methods[i].traverse(visitor, this.scope);
			}
		}
		visitor.endVisit(this, classScope);
	} catch (AbortType e) {
		// silent abort
	}
}



/**
 * Returns whether the type is a secondary one or not.
 */
public boolean isSecondary() {
	return (this.bits & ASTNode.IsSecondaryType) != 0;
}
public int getASTType() {
	return IASTNode.TYPE_DECLARATION;

}
}
