/******************************************************************************
* Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik 
* Rapperswil, University of applied sciences and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html 
*
* Contributors:
* 	Ueli Kunz <kunz@ideadapt.net>, Jules Weder <julesweder@gmail.com> - initial API and implementation
******************************************************************************/

package ch.hsr.ifs.cdt.metriculator.model.nodes;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;

import ch.hsr.ifs.cdt.metriculator.resources.Icon;


public class CompositeTypeNode extends AbstractNode implements ILogicNode {

	public CompositeTypeNode(ICPPASTCompositeTypeSpecifier astNode) {
		super(astNode);
		setAstNode(new NodeInfo(astNode));
		setScopeName(astNode.getName().toString());
	}

	public CompositeTypeNode(String name) {
		super(name);
	}

	public CompositeTypeNode(ICPPASTElaboratedTypeSpecifier astNode) {
		super(astNode);
		setAstNode(new NodeInfo(astNode));
		setScopeName(astNode.getName().toString());
	}

	@Override
	public String toString() {
		return isAnonymous() ? ILogicNode.ANONYMOUS_LABEL : getScopeName();
	}

	@Override
	public String getIconPath() {
		int key = getNodeInfo().getTypeKey();

		switch(key){
			case ICPPASTCompositeTypeSpecifier.k_struct:
				return Icon.Size16.STRUCT;
			case ICPPASTCompositeTypeSpecifier.k_class:
				return Icon.Size16.CLASS;
			case ICPPASTCompositeTypeSpecifier.k_union:
				return Icon.Size16.UNION;
			default:
				return Icon.Size16.CLASS;
		}
	}

	@Override
	public boolean isAnonymous() {
		return getScopeName().trim().isEmpty();
	}
}
