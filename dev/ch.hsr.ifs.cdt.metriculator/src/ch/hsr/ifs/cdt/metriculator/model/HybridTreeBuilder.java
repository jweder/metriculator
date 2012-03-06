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

package ch.hsr.ifs.cdt.metriculator.model;

import java.util.HashMap;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;

import ch.hsr.ifs.cdt.metriculator.model.nodes.AbstractNode;
import ch.hsr.ifs.cdt.metriculator.model.nodes.ILogicNode;
import ch.hsr.ifs.cdt.metriculator.model.nodes.WorkspaceNode;

public class HybridTreeBuilder extends TreeBuilder {

	private HashMap<String,AbstractNode> descendants                   = new HashMap<String,AbstractNode>();

	//new merging
	private HashMap<IBinding, AbstractNode> funcDeclarations = new HashMap<IBinding, AbstractNode>();
	private HashMap<IBinding, AbstractNode> typeDeclarations  = new HashMap<IBinding, AbstractNode>();

	public HybridTreeBuilder(String workspace){
		root = new WorkspaceNode(workspace);
	}

	@Override
	public AbstractNode addChild(AbstractNode parent, AbstractNode child){

		//		AbstractNode defNode = getDefinitionForDeclarationOf(child, parent);
		//		if(defNode != null){
		//			return parent; 
		//		}

		String childsHybridId = combine(TreeBuilder.PATH_SEPARATOR, parent.getHybridId(), child.getScopeUniqueName());
		AbstractNode existing = parent.getChildBy(childsHybridId);

		prepareDeclDefMerging(child);

		if(existing != null){
			//			if(sameNodeExists(child, existing)){
			mergeChildrenOf(child, existing);
			child = existing;
		}else{
			child.setHybridId(childsHybridId);
			child = parent.add(child);
			PreOrderTreeVisitor visitor = new PreOrderTreeVisitor() {
				@Override
				void visitNode(AbstractNode n) {
					descendants.put(n.getHybridId(), n);
				}
			};
			visitor.visit(child);
		}

		return child;
	}

	private void mergeChildrenOf(AbstractNode node, AbstractNode intoParent){
		for(AbstractNode n : node.getChildren()){
			addChild(intoParent, n);
		}
	}

	public AbstractNode getChildBy(String hybridId){
		return descendants.get(hybridId);
	}

	private void prepareDeclDefMerging(AbstractNode child) {
		if(child.getNodeInfo().isFunctionDeclarator()){
			funcDeclarations.put(child.getNodeInfo().getBinding(), child);
		}

		if(child.getNodeInfo().isElaboratedTypeSpecifier()){
			typeDeclarations.put(child.getNodeInfo().getTypeBinding(), child);
		}
	}

	public void mergeDeclarationsAndDefinitions(IASTTranslationUnit tu) {

		for (IASTDeclaration decl : tu.getDeclarations()) {
			boolean type = false;
			if(decl instanceof IASTSimpleDeclaration){
				IBinding declBinding = null;
				if(((IASTSimpleDeclaration) decl).getDeclSpecifier() instanceof ICPPASTElaboratedTypeSpecifier){
					declBinding = ((ICPPASTElaboratedTypeSpecifier)((IASTSimpleDeclaration) decl).getDeclSpecifier()).getName().getBinding();
					type = true;
				}else{
					IASTDeclarator[] declarators = ((IASTSimpleDeclaration)decl).getDeclarators();
					if(declarators.length > 0){
						declBinding = tu.getIndex().adaptBinding(declarators[0].getName().getBinding());
						if(declBinding == null){
							declBinding = declarators[0].getName().getBinding();
						}
					}

				}
				if(declBinding != null){
					for(IName name : tu.getDefinitions(declBinding)){
						if(name instanceof IASTName && name.isDefinition()){
							AbstractNode declaration;
							if(type){
								declaration = typeDeclarations.get(((IASTName)name).getBinding());
							}else{
								declaration = funcDeclarations.get(tu.getIndex().adaptBinding(((IASTName)name).getBinding()));
								if(declaration == null){
									declaration = funcDeclarations.get(((IASTName)name).getBinding());
								}
							}
							if(declaration != null){
								declaration.removeFromParent();
								declaration = null;
							}
						}
					}
				}
			}
		}
		removeAllBindings();
	}


	private void removeAllBindings() {
		funcDeclarations.clear();
		typeDeclarations.clear();
	}

}
