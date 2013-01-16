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

//import org.eclipse.mod.wst.jsdt.core.JavaScriptCore;
import org.eclipse.mod.wst.jsdt.core.ast.IASTNode;
import org.eclipse.mod.wst.jsdt.core.ast.ITryStatement;
import org.eclipse.mod.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FinallyFlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.InsideSubRoutineFlowContext;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.NullInfoRegistry;
import org.eclipse.mod.wst.jsdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.LocalVariableBinding;
//import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.MethodBinding;
//import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.MethodScope;
import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.ReferenceBinding;
//import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeBinding;
//import org.eclipse.mod.wst.jsdt.internal.compiler.lookup.TypeIds;

public class TryStatement extends SubRoutineStatement implements ITryStatement {

	public Block tryBlock;
	public Block[] catchBlocks;

	public Argument[] catchArguments;

	// should rename into subRoutineComplete to be set to false by default

	public Block finallyBlock;
	BlockScope scope;

	public UnconditionalFlowInfo subRoutineInits;
	ReferenceBinding[] caughtExceptionTypes;
	boolean[] catchExits;

	boolean isSubRoutineStartLabel;
	public LocalVariableBinding anyExceptionVariable,
		returnAddressVariable,
		secretReturnValue;



	// for local variables table attributes
	int mergedInitStateIndex = -1;
	int preTryInitStateIndex = -1;
	int naturalExitMergeInitStateIndex = -1;
	int[] catchExitInitStateIndexes;

 
public boolean isSubRoutineEscaping() {
	return (this.bits & ASTNode.IsSubRoutineEscaping) != 0;
}

public StringBuffer printStatement(int indent, StringBuffer output) {
	printIndent(indent, output).append("try \n"); //$NON-NLS-1$
	this.tryBlock.printStatement(indent + 1, output);

	//catches
	if (this.catchBlocks != null)
		for (int i = 0; i < this.catchBlocks.length; i++) {
				output.append('\n');
				printIndent(indent, output).append("catch ("); //$NON-NLS-1$
				this.catchArguments[i].print(0, output).append(") "); //$NON-NLS-1$
				this.catchBlocks[i].printStatement(indent + 1, output);
		}
	//finally
	if (this.finallyBlock != null) {
		output.append('\n');
		printIndent(indent, output).append("finally\n"); //$NON-NLS-1$
		this.finallyBlock.printStatement(indent + 1, output);
	}
	return output;
}

//public void resolve(BlockScope upperScope) {
//	// special scope for secret locals optimization.
//	this.scope = new BlockScope(upperScope);
//
//	BlockScope tryScope = new BlockScope(this.scope);
//	BlockScope finallyScope = null;
//
//	if (this.finallyBlock != null) {
//		if (this.finallyBlock.isEmptyBlock()) {
//			if ((this.finallyBlock.bits & ASTNode.UndocumentedEmptyBlock) != 0) {
//				this.scope.problemReporter().undocumentedEmptyBlock(this.finallyBlock.sourceStart, this.finallyBlock.sourceEnd);
//			}
//		} else {
//			finallyScope = JavaScriptCore.IS_ECMASCRIPT4 ? new BlockScope(this.scope, false) : this.scope; // don't add it yet to parent scope
//
//			// provision for returning and forcing the finally block to run
//			MethodScope methodScope = this.scope.methodScope();
//
//			// the type does not matter as long as it is not a base type
////			if (!upperScope.compilerOptions().inlineJsrBytecode) {
////				this.returnAddressVariable =
////					new LocalVariableBinding(TryStatement.SECRET_RETURN_ADDRESS_NAME, upperScope.getJavaLangObject(), ClassFileConstants.AccDefault, false);
////				finallyScope.addLocalVariable(this.returnAddressVariable);
////				this.returnAddressVariable.setConstant(Constant.NotAConstant); // not inlinable
////			}
//			this.isSubRoutineStartLabel = true;
//
////			this.anyExceptionVariable =
////				new LocalVariableBinding(TryStatement.SECRET_ANY_HANDLER_NAME, this.scope.getJavaLangThrowable(), ClassFileConstants.AccDefault, false);
////			finallyScope.addLocalVariable(this.anyExceptionVariable);
////			this.anyExceptionVariable.setConstant(Constant.NotAConstant); // not inlinable
//
//			if (methodScope != null && !methodScope.isInsideInitializer()) {
//				MethodBinding methodBinding =
//					((AbstractMethodDeclaration) methodScope.referenceContext).binding;
//				if (methodBinding != null) {
//					TypeBinding methodReturnType = methodBinding.returnType;
//					if (methodReturnType.id != TypeIds.T_void) {
////						this.secretReturnValue =
////							new LocalVariableBinding(
////								TryStatement.SECRET_RETURN_VALUE_NAME,
////								methodReturnType,
////								ClassFileConstants.AccDefault,
////								false);
////						finallyScope.addLocalVariable(this.secretReturnValue);
////						this.secretReturnValue.setConstant(Constant.NotAConstant); // not inlinable
//					}
//				}
//			}
//			this.finallyBlock.resolveUsing(finallyScope);
//			if (JavaScriptCore.IS_ECMASCRIPT4) {
//				// force the finally scope to have variable positions shifted after its try scope and catch ones
//				finallyScope.shiftScopes = new BlockScope[this.catchArguments == null ? 1
//						: this.catchArguments.length + 1];
//				finallyScope.shiftScopes[0] = tryScope;
//			}
//		}
//	}
//	this.tryBlock.resolveUsing(tryScope);
//
//	// arguments type are checked against JavaLangThrowable in resolveForCatch(..)
//	if (this.catchBlocks != null) {
//		int length = this.catchArguments.length;
//		TypeBinding[] argumentTypes = new TypeBinding[length];
//		boolean catchHasError = false;
//		for (int i = 0; i < length; i++) {
//			BlockScope catchScope = new BlockScope(this.scope);
//			if (JavaScriptCore.IS_ECMASCRIPT4 && finallyScope != null){
//				finallyScope.shiftScopes[i+1] = catchScope;
//			}
//			// side effect on catchScope in resolveForCatch(..)
//			if ((argumentTypes[i] = this.catchArguments[i].resolveForCatch(catchScope)) == null) {
//				catchHasError = true;
//			}
//			this.catchBlocks[i].resolveUsing(catchScope);
//		}
//		if (catchHasError) {
//			return;
//		}
//		// Verify that the catch clause are ordered in the right way:
//		// more specialized first.
//		this.caughtExceptionTypes = new ReferenceBinding[length];
//		for (int i = 0; i < length; i++) {
//			this.caughtExceptionTypes[i] = (ReferenceBinding) argumentTypes[i];
////			for (int j = 0; j < i; j++) {
////				if (this.caughtExceptionTypes[i].isCompatibleWith(argumentTypes[j])) {
////					this.scope.problemReporter().wrongSequenceOfExceptionTypesError(this, this.caughtExceptionTypes[i], i, argumentTypes[j]);
////				}
////			}
//		}
//	} else {
//		this.caughtExceptionTypes = new ReferenceBinding[0];
//	}
//
//	if (JavaScriptCore.IS_ECMASCRIPT4 && finallyScope != null){
//		// add finallyScope as last subscope, so it can be shifted behind try/catch subscopes.
//		// the shifting is necessary to achieve no overlay in between the finally scope and its
//		// sibling in term of local variable positions.
//		this.scope.addSubscope(finallyScope);
//	}
//}

public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		if(this.scope==null) this.scope=blockScope;
		this.tryBlock.traverse(visitor, this.scope);
		if (this.catchArguments != null) {
			for (int i = 0, max = this.catchBlocks.length; i < max; i++) {
				this.catchArguments[i].traverse(visitor, this.scope);
				this.catchBlocks[i].traverse(visitor, this.scope);
			}
		}
		if (this.finallyBlock != null)
			this.finallyBlock.traverse(visitor, this.scope);
	}
	visitor.endVisit(this, blockScope);
}
public int getASTType() {
	return IASTNode.TRY_STATEMENT;

}
}
