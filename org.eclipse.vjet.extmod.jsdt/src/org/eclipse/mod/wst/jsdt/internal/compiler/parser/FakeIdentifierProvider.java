/**
 * Copyright (c) 2005, 2012 eBay Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     eBay Inc - modification to jsdt
 */
package org.eclipse.mod.wst.jsdt.internal.compiler.parser;

import java.util.List;
import java.util.Map;

import org.eclipse.mod.wst.jsdt.internal.compiler.parser.BalanceUtil.Tuple;

/**
 * EBAY MOD extension for providing fake token to be injected
 */
public class FakeIdentifierProvider {

	private int m_currToken = -1;
	private int m_currTokenIndex = 0;
	private int m_lastCurPos = -1;	
	private Map<Integer, List<Tuple>> m_mapOfTuplesToInsert;
	private Map<Integer, Tuple> m_mapOfTuplesToIgnore;
	
	public FakeIdentifierProvider(
		Map<Integer, List<Tuple>> mapOfTuplesToInsert,
		Map<Integer, Tuple> mapOfTuplesToIgnore) {
		m_mapOfTuplesToInsert = mapOfTuplesToInsert;
		m_mapOfTuplesToIgnore = mapOfTuplesToIgnore;
	}
	
	/**
	 * If this call answers true, it could also set state of m_currTokenIndex
	 **/
	public boolean needsInject(int currentPosition) {
		List<Tuple> tuples = m_mapOfTuplesToInsert.get(currentPosition);
		if (tuples != null) {
				if (currentPosition != m_lastCurPos) {
					m_currToken = -1;
					m_currTokenIndex = 0;
				}
				m_lastCurPos = currentPosition;
				return true;
		}
		return false;
	}
	
	public boolean needsIngore(int currentPosition) {
		return m_mapOfTuplesToIgnore.containsKey(currentPosition);
	}

	public boolean hasNextToken() {
		List<Tuple> tuples = m_mapOfTuplesToInsert.get(m_lastCurPos);
		if (tuples == null) {
			return false;
		}		
		if (tuples.size() > m_currTokenIndex) {
			return true;
		}
		return false;
	}
	
	public int getNextToken() {	
		List<Tuple> tuples = m_mapOfTuplesToInsert.get(m_lastCurPos);		
		if (tuples == null){
			return -1;
		}		
		m_currToken = tuples.get(m_currTokenIndex).m_token;
		m_currTokenIndex++;
		return m_currToken;			
	}
	
	public char[] getTokenSource() {
		return BalanceUtil.getTokenSource(m_currToken);
	}	
}
