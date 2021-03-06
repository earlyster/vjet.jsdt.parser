/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.mod.wst.jsdt.internal.compiler.lookup;


import org.eclipse.mod.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.mod.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
//import org.eclipse.mod.wst.jsdt.internal.compiler.impl.Constant;

public final class ArrayBinding extends ReferenceBinding {
	// creation and initialization of the length field
	// the declaringClass of this field is intentionally set to null so it can be distinguished.
	public static final FieldBinding ArrayLength = new FieldBinding(TypeConstants.LENGTH, TypeBinding.INT, ClassFileConstants.AccPublic | ClassFileConstants.AccFinal, null);

	public TypeBinding leafComponentType;
	public int dimensions;
	LookupEnvironment environment;
	char[] constantPoolName;
	char[] genericTypeSignature;
	ReferenceBinding referenceBinding;

public ArrayBinding(TypeBinding type, int dimensions, LookupEnvironment environment) {
	this.tagBits |= TagBits.IsArrayType;
	this.leafComponentType = type;
	this.dimensions = dimensions;
	this.environment = environment;
	if (type instanceof UnresolvedReferenceBinding)
		((UnresolvedReferenceBinding) type).addWrapper(this, environment);

//	referenceBinding = environment.getResolvedType(TypeConstants.ARRAY,null);
	this.fPackage=environment.defaultPackage;
}

/*
 * brakets leafUniqueKey
 * p.X[][] --> [[Lp/X;
 */
public char[] computeUniqueKey(boolean isLeaf) {
	char[] brackets = new char[dimensions];
	for (int i = dimensions - 1; i >= 0; i--) brackets[i] = '[';
	return CharOperation.concat(brackets, this.leafComponentType.computeUniqueKey(isLeaf));
 }

/**
 * Answer the receiver's constant pool name.
 * NOTE: This method should only be used during/after code gen.
 * e.g. '[Ljava/lang/Object;'
 */
public char[] constantPoolName() {
	if (constantPoolName != null)
		return constantPoolName;

	char[] brackets = new char[dimensions];
	for (int i = dimensions - 1; i >= 0; i--) brackets[i] = '[';
	return constantPoolName = CharOperation.concat(brackets, leafComponentType.signature());
}
public String debugName() {
	StringBuffer brackets = new StringBuffer(dimensions * 2);
	for (int i = dimensions; --i >= 0;)
		brackets.append("[]"); //$NON-NLS-1$
	return leafComponentType.debugName() + brackets.toString();
}
public int dimensions() {
	return this.dimensions;
}

/* Answer an array whose dimension size is one less than the receiver.
*
* When the receiver's dimension size is one then answer the leaf component type.
*/

//public TypeBinding elementsType() {
//	if (this.dimensions == 1) return this.leafComponentType;
//	return this.environment.createArrayType(this.leafComponentType, this.dimensions - 1);
//}

public LookupEnvironment environment() {
    return this.environment;
}

public char[] genericTypeSignature() {

    if (this.genericTypeSignature == null) {
		char[] brackets = new char[dimensions];
		for (int i = dimensions - 1; i >= 0; i--) brackets[i] = '[';
		this.genericTypeSignature = CharOperation.concat(brackets, leafComponentType.signature());
    }
    return this.genericTypeSignature;
}

public PackageBinding getPackage() {
	return leafComponentType.getPackage();
}

public int hashCode() {
	return this.leafComponentType == null ? super.hashCode() : this.leafComponentType.hashCode();
}

/* Answer true if the receiver type can be assigned to the argument type (right)
*/
public boolean isCompatibleWith(TypeBinding otherType) {
	if (this == otherType)
		return true;

	switch (otherType.kind()) {
		case Binding.ARRAY_TYPE :
			ArrayBinding otherArray = (ArrayBinding) otherType;
//			if (otherArray.leafComponentType.isBaseType())
//				return false; // relying on the fact that all equal arrays are identical
//			if (dimensions == otherArray.dimensions)
				return leafComponentType.isCompatibleWith(otherArray.leafComponentType);
//			if (dimensions < otherArray.dimensions)
//				return false; // cannot assign 'String[]' into 'Object[][]' but can assign 'byte[][]' into 'Object[]'
//			break;
		case Binding.BASE_TYPE :
			return otherType.isAnyType();

		case Binding.TYPE :
		    return otherType==this.referenceBinding;

	}
	//Check dimensions - Java does not support explicitly sized dimensions for types.
	//However, if it did, the type checking support would go here.
	switch (otherType.leafComponentType().id) {
	    case TypeIds.T_JavaLangObject :
	        return true;
	}
	return false;
}

public int kind() {
	return ARRAY_TYPE;
}

public TypeBinding leafComponentType(){
	return leafComponentType;
}

/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/
public int problemId() {
	return leafComponentType.problemId();
}
/**
* Answer the source name for the type.
* In the case of member types, as the qualified name from its top level type.
* For example, for a member type N defined inside M & A: "A.M.N".
*/

public char[] qualifiedSourceName() {
	char[] brackets = new char[dimensions * 2];
	for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(leafComponentType.qualifiedSourceName(), brackets);
}
public char[] readableName() /* java.lang.Object[] */ {
	char[] brackets = new char[dimensions * 2];
	for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(leafComponentType.readableName(), brackets);
}
public char[] shortReadableName(){
	char[] brackets = new char[dimensions * 2];
	for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(leafComponentType.shortReadableName(), brackets);
}
public char[] sourceName() {
	char[] brackets = new char[dimensions * 2];
	for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(leafComponentType.sourceName(), brackets);
}
public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType, LookupEnvironment env) {
	if (this.leafComponentType == unresolvedType) {
		this.leafComponentType = resolvedType;
		this.tagBits |= this.leafComponentType.tagBits;
	}
}
public String toString() {
	return leafComponentType != null ? debugName() : "NULL TYPE ARRAY"; //$NON-NLS-1$
}

//public FieldBinding[] availableFields() {
//	return referenceBinding.availableFields();
//}

public int fieldCount() {
	return referenceBinding.fieldCount();
}

public FieldBinding[] fields() {
	return referenceBinding.fields();
}

public FieldBinding getField(char[] fieldName, boolean needResolve) {
	return referenceBinding.getField(fieldName, needResolve);
}

}
