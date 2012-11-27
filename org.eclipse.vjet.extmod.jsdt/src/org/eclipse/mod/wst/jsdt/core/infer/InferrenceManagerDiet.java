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
package org.eclipse.mod.wst.jsdt.core.infer;

import java.util.ArrayList;

import org.eclipse.mod.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;

public class InferrenceManagerDiet {


	public static final String EXTENSION_POINT= "inferrenceSupport"; //$NON-NLS-1$

	protected static final String TAG_INFERENCE_PROVIDER = "inferenceProvider"; //$NON-NLS-1$
	protected static final String ATTR_INFERENGINE_CLASS = "class"; //$NON-NLS-1$


	private static InferrenceManagerDiet instance = null;


	private  InferrenceSupportExtension [] extensions;

	public static InferrenceManagerDiet getInstance(){
		if( instance == null )
			instance = new InferrenceManagerDiet();

		return instance;
	}



	public InferrenceProvider [] getInferenceProviders()
	{

		if (extensions==null)
		{
			//loadInferenceExtensions();
		}
		ArrayList extProviders=new ArrayList();
		extProviders.add(new DefaultInferrenceProvider());
		for (int i = 0; i < extensions.length; i++) {
			  if (extensions[i].inferProvider!=null)
				  extProviders.add(extensions[i].inferProvider);
			}
		return (InferrenceProvider [] )extProviders.toArray(new InferrenceProvider[extProviders.size()]);
	}


	public InferrenceProvider [] getInferenceProviders(IInferenceFile script)
	{
		InferrenceProvider[] inferenceProviders = getInferenceProviders();
		ArrayList extProviders=new ArrayList();
		for (int i = 0; i < inferenceProviders.length; i++) {
			    int applies = inferenceProviders[i].applysTo(script);
			    switch (applies) {
				case InferrenceProvider.MAYBE_THIS:
					  extProviders.add(inferenceProviders[i]);
					break;

				case InferrenceProvider.ONLY_THIS:
					InferrenceProvider [] thisProvider = {inferenceProviders[i]};
					return thisProvider;


				default:
					break;
				}
			}
		return (InferrenceProvider [] )extProviders.toArray(new InferrenceProvider[extProviders.size()]);
	}


	

	public InferEngine [] getInferenceEngines(CompilationUnitDeclaration script)
	{
		InferrenceProvider[] inferenceProviders = getInferenceProviders();
		if (inferenceProviders.length==1)
			  return getSingleEngine(inferenceProviders[0]);
			
		ArrayList extEngines=new ArrayList();
		for (int i = 0; i < inferenceProviders.length; i++) {
			    if (script.compilationResult!=null && script.compilationResult.compilationUnit!=null)
			    {
			    	String inferenceID = script.compilationResult.compilationUnit.getInferenceID();
			    	if (inferenceProviders[i].getID().equals(inferenceID))
			    	{
						  return getSingleEngine(inferenceProviders[i]);
			    	}
			    }
			    int applies = inferenceProviders[i].applysTo(script);
			    switch (applies) {
				case InferrenceProvider.MAYBE_THIS:
					  InferEngine eng=inferenceProviders[i].getInferEngine();
					  eng.appliesTo=InferrenceProvider.MAYBE_THIS;
					  eng.inferenceProvider=inferenceProviders[i];
					  extEngines.add(eng);
					break;

				case InferrenceProvider.ONLY_THIS:
					  return getSingleEngine(inferenceProviders[i]);


				default:
					break;
				}
			}
		return (InferEngine [] )extEngines.toArray(new InferEngine[extEngines.size()]);
	}

	
	private InferEngine [] getSingleEngine(InferrenceProvider provider)
	{
		  InferEngine engine=provider.getInferEngine();
		  engine.appliesTo=InferrenceProvider.ONLY_THIS;
		  engine.inferenceProvider=provider;
		  InferEngine [] thisEngine = {engine};
		  return thisEngine;
	}
	
}
