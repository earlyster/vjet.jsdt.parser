/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
import org.eclipse.mod.wst.jsdt.core.ast.IExpression;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.Binding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ClassScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.Scope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeIds;

public abstract class Expression extends Statement implements IExpression {

	public Constant constant;

	public int statementEnd = -1;

	//Some expression may not be used - from a java semantic point
	//of view only - as statements. Other may. In order to avoid the creation
	//of wrappers around expression in order to tune them as expression
	//Expression is a subclass of Statement. See the message isValidJavaStatement()

	public int implicitConversion;
	public TypeBinding resolvedType = TypeBinding.UNKNOWN;

public static final boolean isConstantValueRepresentable(Constant constant, int constantTypeID, int targetTypeID) {
	//true if there is no loss of precision while casting.
	// constantTypeID == constant.typeID
	if (targetTypeID == constantTypeID || constantTypeID==T_any)
		return true;
	switch (targetTypeID) {
		case T_char :
			switch (constantTypeID) {
				case T_char :
					return true;
				case T_double :
					return constant.doubleValue() == constant.charValue();
				case T_float :
					return constant.floatValue() == constant.charValue();
				case T_int :
					return constant.intValue() == constant.charValue();
				case T_short :
					return constant.shortValue() == constant.charValue();
				case T_long :
					return constant.longValue() == constant.charValue();
				default :
					return false;//boolean
			}

		case T_float :
			switch (constantTypeID) {
				case T_char :
					return constant.charValue() == constant.floatValue();
				case T_double :
					return constant.doubleValue() == constant.floatValue();
				case T_float :
					return true;
				case T_int :
					return constant.intValue() == constant.floatValue();
				case T_short :
					return constant.shortValue() == constant.floatValue();
				case T_long :
					return constant.longValue() == constant.floatValue();
				default :
					return false;//boolean
			}

		case T_double :
			switch (constantTypeID) {
				case T_char :
					return constant.charValue() == constant.doubleValue();
				case T_double :
					return true;
				case T_float :
					return constant.floatValue() == constant.doubleValue();
				case T_int :
					return constant.intValue() == constant.doubleValue();
				case T_short :
					return constant.shortValue() == constant.doubleValue();
				case T_long :
					return constant.longValue() == constant.doubleValue();
				default :
					return false; //boolean
			}

		case T_short :
			switch (constantTypeID) {
				case T_char :
					return constant.charValue() == constant.shortValue();
				case T_double :
					return constant.doubleValue() == constant.shortValue();
				case T_float :
					return constant.floatValue() == constant.shortValue();
				case T_int :
					return constant.intValue() == constant.shortValue();
				case T_short :
					return true;
				case T_long :
					return constant.longValue() == constant.shortValue();
				default :
					return false; //boolean
			}

		case T_int :
			switch (constantTypeID) {
				case T_char :
					return constant.charValue() == constant.intValue();
				case T_double :
					return constant.doubleValue() == constant.intValue();
				case T_float :
					return constant.floatValue() == constant.intValue();
				case T_int :
					return true;
				case T_short :
					return constant.shortValue() == constant.intValue();
				case T_long :
					return constant.longValue() == constant.intValue();
				default :
					return false; //boolean
			}

		case T_long :
			switch (constantTypeID) {
				case T_char :
					return constant.charValue() == constant.longValue();
				case T_double :
					return constant.doubleValue() == constant.longValue();
				case T_float :
					return constant.floatValue() == constant.longValue();
				case T_int :
					return constant.intValue() == constant.longValue();
				case T_short :
					return constant.shortValue() == constant.longValue();
				case T_long :
					return true;
				default :
					return false; //boolean
			}

		default :
			return false; //boolean
	}
}

public Expression() {
	super();
}






public boolean checkUnsafeCast(Scope scope, TypeBinding castType, TypeBinding expressionType, TypeBinding match, boolean isNarrowing) {
		if (match == castType) {
			if (!isNarrowing) tagAsUnnecessaryCast(scope, castType);
			return true;
		}
		if (!isNarrowing) tagAsUnnecessaryCast(scope, castType);
		return true;
	}

	public boolean isCompactableOperation() {

		return false;
	}

	//Return true if the conversion is done AUTOMATICALLY by the vm
	//while the javaVM is an int based-machine, thus for example pushing
	//a byte onto the stack , will automatically create an int on the stack
	//(this request some work d be done by the VM on signed numbers)
	public boolean isConstantValueOfTypeAssignableToType(TypeBinding constantType, TypeBinding targetType) {

		if (this.constant == Constant.NotAConstant)
			return false;
		if (constantType == targetType)
			return true;
		if (constantType.id==targetType.id)
			return true;
		if (constantType.isBaseType() && targetType.isBaseType()) {
			//No free assignment conversion from anything but to integral ones.
			if ((constantType == TypeBinding.INT
				|| BaseTypeBinding.isWidening(TypeIds.T_int, constantType.id))
				&& (BaseTypeBinding.isNarrowing(targetType.id, TypeIds.T_int))) {
				//use current explicit conversion in order to get some new value to compare with current one
				return isConstantValueRepresentable(this.constant, constantType.id, targetType.id);
			}
		}
		return false;
	}

	public boolean isTypeReference() {
		return false;
	}

	/**
	 * Returns the local variable referenced by this node. Can be a direct reference (SingleNameReference)
	 * or thru a cast expression etc...
	 */
	public LocalVariableBinding localVariableBinding() {
		return null;
	}

/**
 * Mark this expression as being non null, per a specific tag in the
 * source code.
 */
// this is no more called for now, waiting for inter procedural null reference analysis
public void markAsNonNull() {
	this.bits |= ASTNode.IsNonNull;
}

	public int nullStatus(FlowInfo flowInfo) {

		if (/* (this.bits & IsNonNull) != 0 || */
				this.constant != null && this.constant != Constant.NotAConstant)
			return FlowInfo.NON_NULL; // constant expression cannot be null

		LocalVariableBinding local = localVariableBinding();
		if (local != null) {
			if (flowInfo.isDefinitelyNull(local))
				return FlowInfo.NULL;
			if (flowInfo.isDefinitelyNonNull(local))
				return FlowInfo.NON_NULL;
			return FlowInfo.UNKNOWN;
		}
		return FlowInfo.NON_NULL;
	}

	/**
	 * Constant usable for bytecode pattern optimizations, but cannot be inlined
	 * since it is not strictly equivalent to the definition of constant expressions.
	 * In particular, some side-effects may be required to occur (only the end value
	 * is known).
	 * @return Constant known to be of boolean type
	 */
	public Constant optimizedBooleanConstant() {
		if(this.constant != null)
			return this.constant;
		return Constant.NotAConstant;
	}

	/**
	 * Returns the type of the expression after required implicit conversions. When expression type gets promoted
	 * or inserted a generic cast, the converted type will differ from the resolved type (surface side-effects from
	 * #computeConversion(...)).
	 * @return the type after implicit conversion
	 */
	public TypeBinding postConversionType(Scope scope) {
		TypeBinding convertedType = this.resolvedType;
		int runtimeType = (this.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4;
		switch (runtimeType) {
			case T_boolean :
				convertedType = TypeBinding.BOOLEAN;
				break;
			case T_short :
				convertedType = TypeBinding.SHORT;
				break;
			case T_char :
				convertedType = TypeBinding.CHAR;
				break;
			case T_int :
				convertedType = TypeBinding.INT;
				break;
			case T_float :
				convertedType = TypeBinding.FLOAT;
				break;
			case T_long :
				convertedType = TypeBinding.LONG;
				break;
			case T_double :
				convertedType = TypeBinding.DOUBLE;
				break;
			default :
		}
		if ((this.implicitConversion & TypeIds.BOXING) != 0) {
			convertedType = scope.environment().computeBoxingType(convertedType);
		}
		return convertedType;
	}

	public StringBuffer print(int indent, StringBuffer output) {
		printIndent(indent, output);
		return printExpression(indent, output);
	}

	public abstract StringBuffer printExpression(int indent, StringBuffer output);

	public StringBuffer printStatement(int indent, StringBuffer output) {
		return print(indent, output).append(";"); //$NON-NLS-1$
	}


	/**
	 * Returns an object which can be used to identify identical JSR sequence targets
	 * (see TryStatement subroutine codegen)
	 * or <code>null</null> if not reusable
	 */
	public Object reusableJSRTarget() {
		if (this.constant != Constant.NotAConstant)
			return this.constant;
		return null;
	}

	public void tagAsNeedCheckCast() {
	    // do nothing by default
	}

	/**
	 * Record the fact a cast expression got detected as being unnecessary.
	 *
	 * @param scope
	 * @param castType
	 */
	public void tagAsUnnecessaryCast(Scope scope, TypeBinding castType) {
	    // do nothing by default
	}

	public Expression toTypeReference() {
		//by default undefined

		//this method is meanly used by the parser in order to transform
		//an expression that is used as a type reference in a cast ....
		//--appreciate the fact that castExpression and ExpressionWithParenthesis
		//--starts with the same pattern.....

		return this;
	}


	/**
	 * Traverse an expression in the context of a classScope
	 * @param visitor
	 * @param scope
	 */
	public void traverse(ASTVisitor visitor, ClassScope scope) {
		// nothing to do
	}

//	public void traverse(ASTVisitor visitior, Scope scope)
//	{
//		if (scope instanceof BlockScope)
//			traverse(visitior,(BlockScope)scope);
//		else if (scope instanceof ClassScope)
//			traverse(visitior,(ClassScope)scope);
//		else if (scope instanceof CompilationUnitScope)
//			traverse(visitior,(CompilationUnitScope)scope);
//	}

	public boolean isPrototype()
	{
		return false;
	}

	// is completion or selection node
	public boolean isSpecialNode()
	{
		return false;
	}
	
	public Binding alternateBinding()
	{ return null;}
	
	
	public int getASTType() {
		return IASTNode.EXPRESSION;
	
	}
}
