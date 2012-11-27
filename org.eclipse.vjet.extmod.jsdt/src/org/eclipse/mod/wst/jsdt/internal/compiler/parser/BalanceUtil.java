/*******************************************************************************
 * Copyright (c) 2012 eBay Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     eBay Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.mod.wst.jsdt.internal.compiler.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class BalanceUtil {

	private static final int RBRACKET = TerminalTokens.TokenNameRBRACKET;
	private static final int LBRACKET = TerminalTokens.TokenNameLBRACKET;
	private static final int RBRACE = TerminalTokens.TokenNameRBRACE;
	private static final int LBRACE = TerminalTokens.TokenNameLBRACE;
	private static final int RPAREN = TerminalTokens.TokenNameRPAREN;
	private static final int LPAREN = TerminalTokens.TokenNameLPAREN;	
	private static final int VAR = TerminalTokens.TokenNamevar;
//	private static final int CONST = TerminalTokens.TokenNameconst; // TODO when we support FF lang specific const
	private static final int DELETE = TerminalTokens.TokenNamedelete;
	private static final int CONTINUE = TerminalTokens.TokenNamecontinue;
	private static final int BREAK = TerminalTokens.TokenNamebreak;
	private static final int RETURN = TerminalTokens.TokenNamereturn;
	private static final int DOT = TerminalTokens.TokenNameDOT;
	private static final int COLON = TerminalTokens.TokenNameCOLON;
	private static final int NAMEID = TerminalTokens.TokenNameIdentifier;
	private static final int COMMA = TerminalTokens.TokenNameCOMMA;
	private static final int CASE = TerminalTokens.TokenNamecase;
	private static final int DEFAULT = TerminalTokens.TokenNamedefault;
	private static final int THIS = TerminalTokens.TokenNamethis;
	private static final int EOF = TerminalTokens.TokenNameEOF;
	
	private static final int[] LHS_TOKENS = { LPAREN, LBRACE, LBRACKET};
	private static final int[] RHS_TOKENS = {RPAREN, RBRACE, RBRACKET};
	
	private static Map<Integer,char[]> LOOKUP = new HashMap<Integer, char[]>();
	
	static{
		LOOKUP.put(LPAREN, "(".toCharArray());
		LOOKUP.put(RPAREN, ")".toCharArray());
		LOOKUP.put(LBRACE, "{".toCharArray());
		LOOKUP.put(RBRACE, "}".toCharArray());
		LOOKUP.put(LBRACKET, "[".toCharArray());
		LOOKUP.put(RBRACKET, "]".toCharArray());
		LOOKUP.put(BREAK, "break".toCharArray());
		LOOKUP.put(COMMA, ",".toCharArray());
		LOOKUP.put(CONTINUE, "continue".toCharArray());
		LOOKUP.put(DELETE, "delete".toCharArray());
		LOOKUP.put(DOT, ".".toCharArray());
		LOOKUP.put(COLON, ":".toCharArray());
		LOOKUP.put(NAMEID, "$missing$".toCharArray());
		LOOKUP.put(RETURN, "return".toCharArray());
		LOOKUP.put(VAR, "var".toCharArray());
	}
	
	private Stack<Tuple> m_termStack = new Stack<Tuple>();
	private List<Tuple> m_tupleInsertList = new ArrayList<Tuple>();
	private List<Tuple> m_tupleRemoveList = new ArrayList<Tuple>();
	
	private Tuple m_currentTuple = null;
	private Tuple m_currentDotTuple = null;
	
	private Map<Integer,List<Tuple>> m_tuplesToBeInserted = null;
	private Map<Integer,Tuple> m_tuplesToBeIgnored = null;
	
	private Tuple m_previousCommaTuple = null;
	private Tuple m_previousColonTuple = null;
	private boolean m_caseTokenEncountered = false;
	
	public static char[] getTokenSource(int token){
		return LOOKUP.get(token);
	}
	
	public static char getTokenChar(int token){
		return getTokenSource(token)[0];
	}
	
	public static class Tuple{
		public Tuple(int token, int loc) {
			m_token = token;
			m_location = loc;
		}
		public Tuple(int token, int loc, int previousToken) {
			m_token = token;
			m_location = loc;
			m_previousToken = previousToken;
		}
		int m_token;
		int m_location;
		int m_previousToken = -1;
		
		@Override
		public String toString() {
			return m_token + " " + new String(LOOKUP.get(m_token)) + " " + m_location ;
		}
	}
	
	public BalanceUtil addToken(int token, int location){
			
		if (token==DOT){
			m_currentDotTuple = new Tuple(token,location);
		} else if(m_currentDotTuple!=null){
			if (token == COMMA || token == RBRACKET || token == EOF){
				m_tupleInsertList.add(new Tuple(NAMEID, m_currentDotTuple.m_location, DOT));
			} else if (token== RPAREN || token== RBRACE || token == VAR || token == THIS || token == RETURN || token == BREAK||token == CONTINUE ||token == DELETE){
				m_tupleInsertList.add(new Tuple(NAMEID, m_currentDotTuple.m_location, DOT));
				if(!m_termStack.isEmpty()){
					int tok = m_termStack.peek().m_token;
					if (tok == LPAREN && token!= RPAREN){
						m_tupleInsertList.add(new Tuple(RPAREN,m_currentDotTuple.m_location));
					} else if(tok == LBRACKET){
						m_tupleInsertList.add(new Tuple(RBRACKET,m_currentDotTuple.m_location));	
					}
				}
			}
			m_currentDotTuple = null;
		}

		if (m_previousCommaTuple != null &&
			(token == RBRACE || token == RPAREN || token == RBRACE)) {
			//ignore extra comma
			m_tupleRemoveList.add(m_previousCommaTuple);
		}
		if (m_previousColonTuple != null && token == RBRACE) {
			//fix the value part after colon
			m_tupleInsertList.add(new Tuple(NAMEID, m_previousColonTuple.m_location, COLON));
		}
				
		if (token == COMMA ) {
			m_previousCommaTuple = new Tuple(token,location);
		}
		else {
			m_previousCommaTuple = null;
		}
		
		if (token == CASE || token == DEFAULT) {
			m_caseTokenEncountered = true;
		}
		if (token == COLON) {
			if (m_caseTokenEncountered) {
				m_caseTokenEncountered = false; //consume it
			}
			else {
				m_previousColonTuple = new Tuple(token,location);
			}
		}
		else {
			m_previousColonTuple = null;
		}
		
		if (!isGroupingToken(token)) {
			return this;
		}
		
		if (m_currentTuple!=null &&
			m_currentTuple.m_location == location && 
			m_currentTuple.m_token == token) { //token re-entry should be ignored
			return this;
		}
		
		if (token == TerminalTokens.TokenNameEOF) {
//			if (m_currentTuple!=null && m_currentTuple.m_token == LBRACE) {
//				m_tupleInsertList.add(new Tuple(RBRACE, location, LBRACE));
//			}
			return this;
		}
		
		m_currentTuple = new Tuple(token,location);
		
		if (isLHToken(token)){
			m_termStack.add(m_currentTuple);
		} else {
			if (m_termStack.size()>0){			
				int token2 = m_termStack.peek().m_token;
				if (isLHToken(token2) && matching(token2, token)){
					m_termStack.pop();
					return this;
				}			
			}
			m_termStack.add(m_currentTuple);
		}
		return this;
	}

	private boolean isGroupingToken(int token) {
		if(m_currentTuple!=null && m_currentTuple.m_token==EOF){
			return false;
		}
		
		for(int t:LHS_TOKENS){
			if(token==t){
				return true;
			}
		}
		for(int t:RHS_TOKENS){
			if(token==t){
				return true;
			}
		}
		if(token==EOF){
			return true;
		}

		return false;
	}

	private static final Map<Integer, List<Tuple>> EMPTY_INSERT_MAP = new HashMap<Integer, List<Tuple>>(0);
	/**
	 *  produce a map of tokens to be inserted at location represented as a key in the map
	 */
	public Map<Integer,List<Tuple>> getTuplesToBeInserted() {
		if (m_tuplesToBeInserted != null) {
			return m_tuplesToBeInserted;
		}
		if (m_tupleInsertList.isEmpty()) {
			m_tuplesToBeInserted = EMPTY_INSERT_MAP;
			return m_tuplesToBeInserted;
		}
		m_tuplesToBeInserted = new LinkedHashMap<Integer, List<Tuple>>();		
		for(Tuple t: m_tupleInsertList){
			List<Tuple> list = m_tuplesToBeInserted.get(t.m_location);
			if(list==null){
				list= new ArrayList<Tuple>();
				m_tuplesToBeInserted.put(t.m_location, list);				
			}			
			list.add(t);			
		}		
		return m_tuplesToBeInserted;		
	}
	

	private static final Map<Integer, Tuple> EMPTY_IGNORE_MAP = new HashMap<Integer, Tuple>(0);
	public Map<Integer, Tuple> getTuplesToBeIgnored() {
		if (m_tuplesToBeIgnored != null) {
			return m_tuplesToBeIgnored;
		}
		if (m_tupleRemoveList.isEmpty()) {
			m_tuplesToBeIgnored = EMPTY_IGNORE_MAP;
			return m_tuplesToBeIgnored;
		}
		m_tuplesToBeIgnored = new HashMap<Integer, Tuple>();
		for (Tuple tuple : m_tupleRemoveList) {
			m_tuplesToBeIgnored.put(tuple.m_location, tuple);
		}
		return m_tuplesToBeIgnored;
	}

	@Override
	public String toString() {
		return m_tupleInsertList.toString();
	}
	
	private static boolean matching(int left, int right){
		if(left == LPAREN && right == RPAREN){
			return true;
		}
		if(left == LBRACE && right == RBRACE){
			return true;
		}
		if(left == LBRACKET&& right == RBRACKET){
			return true;
		}
		return false;
	}
	
	private static boolean isLHToken(int token){
		for(int token2: LHS_TOKENS){
			if(token2 == token){
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
			
		// 1 (… xxx.} --> ( xxx.$missing)}
		BalanceUtil butil = new BalanceUtil()
			.addToken(LPAREN, 4)
			.addToken(NAMEID, 13)
			.addToken(DOT, 14)
			.addToken(RBRACE, 21);		
		System.out.println("BALANCE = " + butil.getTuplesToBeInserted());
		
		// 2 [… xxx.} --> [ xxx.$missing]}
		butil = new BalanceUtil()
			.addToken(LBRACKET, 4)
			.addToken(NAMEID, 13)
			.addToken(DOT, 14)
			.addToken(RBRACE, 21);	
		System.out.println("BALANCE = " + butil.getTuplesToBeInserted());
		
		// 3 xxx.} --> xxx.$missing},
		butil = new BalanceUtil()
			.addToken(NAMEID, 13)
			.addToken(DOT, 14)
			.addToken(RBRACE, 15);			
		System.out.println("BALANCE = " + butil.getTuplesToBeInserted());
		
		// 4 xxx., --> xxx.$missing,
		butil = new BalanceUtil()
			.addToken(NAMEID, 13)
			.addToken(DOT, 14)
			.addToken(COMMA, 15);	
		System.out.println("BALANCE = " + butil.getTuplesToBeInserted());
				
		// 5 xxx.] --> xxx.$missing]
		butil = new BalanceUtil()
			.addToken(NAMEID, 13)
			.addToken(DOT, 14)
			.addToken(RBRACKET, 15);	
		System.out.println("BALANCE = " + butil.getTuplesToBeInserted());
		
		// 6 (... xxx. this --> (... xxx.$missing) this
		butil = new BalanceUtil()
			.addToken(LPAREN, 12)
			.addToken(NAMEID, 13)
			.addToken(DOT, 14)
			.addToken(THIS, 16);		
		System.out.println("BALANCE = " + butil.getTuplesToBeInserted());
		
		// 7 [... xxx. this --> [... xxx.$missing] this
		butil = new BalanceUtil()
			.addToken(LBRACKET, 12)
			.addToken(NAMEID, 13)
			.addToken(DOT, 14)
			.addToken(THIS, 16);		
		System.out.println("BALANCE = " + butil.getTuplesToBeInserted());
		
		// 8 xxx. this --> xxx.$missing this
		butil = new BalanceUtil()
			.addToken(NAMEID, 13)
			.addToken(DOT, 14)
			.addToken(THIS, 16);		
		System.out.println("BALANCE = " + butil.getTuplesToBeInserted());
				
		// 9 (... xxx. var --> (... xxx.$missing) var
		butil = new BalanceUtil()
			.addToken(LPAREN, 4)
			.addToken(DOT, 13)
			.addToken(VAR, 14);			
		System.out.println("BALANCE = " + butil.getTuplesToBeInserted());		
		
		// 10 [... xxx. var --> [... xxx.$missing] var
		butil = new BalanceUtil()
			.addToken(LBRACKET, 13)
			.addToken(NAMEID, 13)
			.addToken(DOT, 14)
			.addToken(VAR, 16);	
		System.out.println("BALANCE = " + butil.getTuplesToBeInserted());
				
		// 11 xxx. var --> xxx.$missing var
		butil = new BalanceUtil()
			.addToken(NAMEID, 13)
			.addToken(DOT, 14)
			.addToken(VAR, 16);		
		System.out.println("BALANCE = " + butil.getTuplesToBeInserted());
	}
}
