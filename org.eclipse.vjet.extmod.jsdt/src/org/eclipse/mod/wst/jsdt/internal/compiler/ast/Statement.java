/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import org.eclipse.mod.wst.jsdt.core.ast.IStatement;

public abstract class Statement extends ProgramElement implements IStatement {






	public boolean isEmptyBlock() {
		return false;
	}

	public boolean isValidJavaStatement() {
		//the use of this method should be avoid in most cases
		//and is here mostly for documentation purpose.....
		//while the parser is responsable for creating
		//welled formed expression statement, which results
		//in the fact that java-non-semantic-expression-used-as-statement
		//should not be parsable...thus not being built.
		//It sounds like the java grammar as help the compiler job in removing
		//-by construction- some statement that would have no effect....
		//(for example all expression that may do side-effects are valid statement
		// -this is an appromative idea.....-)

		return true;
	}

	public StringBuffer print(int indent, StringBuffer output) {
		return printStatement(indent, output);
	}
//	public abstract StringBuffer printStatement(int indent, StringBuffer output);

//	public abstract void resolve(BlockScope scope);

	/**
	 * Returns case constant associated to this statement (NotAConstant if none)
	 */
//	public Constant resolveCase(BlockScope scope, TypeBinding testType, SwitchStatement switchStatement) {
//		// statement within a switch that are not case are treated as normal statement....
//
//		resolve(scope);
//		return Constant.NotAConstant;
//	}
	public int getASTType() {
		return IASTNode.STATEMENT;
	
	}

}
