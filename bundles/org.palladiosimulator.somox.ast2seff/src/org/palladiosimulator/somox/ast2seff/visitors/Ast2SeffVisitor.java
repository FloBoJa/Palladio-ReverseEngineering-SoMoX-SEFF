package org.palladiosimulator.somox.ast2seff.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.palladiosimulator.generator.fluent.repository.api.seff.ActionSeff;
import org.palladiosimulator.generator.fluent.repository.api.seff.InternalSeff;
import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.generator.fluent.repository.structure.components.BasicComponentCreator;
import org.palladiosimulator.generator.fluent.repository.structure.components.seff.BranchActionCreator;
import org.palladiosimulator.generator.fluent.repository.structure.components.seff.ExternalCallActionCreator;
import org.palladiosimulator.generator.fluent.repository.structure.components.seff.LoopActionCreator;
import org.palladiosimulator.generator.fluent.repository.structure.components.seff.SeffCreator;
import org.palladiosimulator.generator.fluent.repository.structure.components.seff.SetVariableActionCreator;
import org.palladiosimulator.generator.fluent.shared.components.VariableUsageCreator;
import org.palladiosimulator.pcm.parameter.VariableCharacterisationType;
import org.palladiosimulator.pcm.reliability.ResourceTimeoutFailureType;
import org.palladiosimulator.pcm.repository.CompositeDataType;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.PrimitiveDataType;
import org.palladiosimulator.somox.ast2seff.models.ComponentInformation;
import org.palladiosimulator.somox.ast2seff.models.MethodBundlePair;
import org.palladiosimulator.somox.ast2seff.models.MethodPalladioInformation;
import org.palladiosimulator.somox.ast2seff.util.NameUtil;
import org.palladiosimulator.somox.ast2seff.util.SwitchStatementUtil;

public class Ast2SeffVisitor extends ASTVisitor {

	private static final Logger LOGGER = Logger.getLogger(Ast2SeffVisitor.class);
	
	private FluentRepositoryFactory create;	
	private Map<String, MethodPalladioInformation> methodPalladioInfoMap;
	private MethodPalladioInformation methodPalladioInfo;
	private MethodBundlePair methodBundlePair;
	private ActionSeff actionSeff;
	private BasicComponentCreator basicComponentCreator;
	private ComponentInformation componentInformation;
	private int internalCallActionDepth = 0;
	
	public Ast2SeffVisitor(MethodPalladioInformation methodPalladioInformation, ActionSeff actionSeff, Map<String, MethodPalladioInformation> methodPalladionInfoMap, ComponentInformation componentInformation, FluentRepositoryFactory create) {
		this.actionSeff = actionSeff;
		this.methodPalladioInfoMap = methodPalladionInfoMap;
		this.methodBundlePair = methodPalladioInformation.getMethodBundlePair();
		this.methodPalladioInfo = methodPalladioInformation;
		this.componentInformation = componentInformation;
		this.basicComponentCreator = componentInformation.getBasicComponentCreator();
		this.create = create;
	}
	
	private Ast2SeffVisitor(MethodPalladioInformation methodPalladioInformation, ActionSeff actionSeff, Map<String, MethodPalladioInformation> methodPalladionInfoMap, ComponentInformation componentInformation, FluentRepositoryFactory create, int internalCallActionDepth) {
		this.actionSeff = actionSeff;
		this.methodPalladioInfoMap = methodPalladionInfoMap;
		this.methodBundlePair = methodPalladioInformation.getMethodBundlePair();
		this.methodPalladioInfo = methodPalladioInformation;
		this.componentInformation = componentInformation;
		this.basicComponentCreator = componentInformation.getBasicComponentCreator();
		this.create = create;
		this.internalCallActionDepth = internalCallActionDepth;
	}
	
	public static ActionSeff perform(MethodPalladioInformation methodPalladioInformation, ActionSeff actionSeff, Map<String, MethodPalladioInformation> methodPalladionInfoMap, ComponentInformation componentInformation, FluentRepositoryFactory create) {
		Ast2SeffVisitor newFunctionCallClassificationVisitor = new Ast2SeffVisitor(methodPalladioInformation, actionSeff, methodPalladionInfoMap, componentInformation, create);
		methodPalladioInformation.getMethodBundlePair().getAstNode().accept(newFunctionCallClassificationVisitor);
		return actionSeff;
	}
	
	private ActionSeff perform(ASTNode node, ActionSeff actionSeff) {
		Ast2SeffVisitor newFunctionCallClassificationVisitor = new Ast2SeffVisitor(methodPalladioInfo, actionSeff, methodPalladioInfoMap, componentInformation, create);
		node.accept(newFunctionCallClassificationVisitor);
		return actionSeff;
	}
	
	private ActionSeff perform(ASTNode node, ActionSeff actionSeff, int internalCallActionDepth) {
		Ast2SeffVisitor newFunctionCallClassificationVisitor = new Ast2SeffVisitor(methodPalladioInfo, actionSeff, methodPalladioInfoMap, componentInformation, create, internalCallActionDepth);
		node.accept(newFunctionCallClassificationVisitor);
		return actionSeff;
	}
	
	public boolean visit(final ExpressionStatement expressionStatement) {
		LOGGER.debug("Visit Expression Statement");
		Expression expression = expressionStatement.getExpression();

		if (expression instanceof Assignment) {
			// TODO: further tests if this makes sense - Limitation
			// Variable Assignment
			// Assignment transformedExpression = (Assignment) expression;
			// SetVariableActionCreator setVariableActionCreator = actionSeff.setVariableAction();
			// VariableUsageCreator inputVariable = this.generateInputVariableUsage(transformedExpression.getRightHandSide());
			// setVariableActionCreator.withLocalVariableUsage(inputVariable);
			// this.actionSeff = setVariableActionCreator.followedBy();
		} else if (expression instanceof MethodInvocation && this.isExternal((MethodInvocation) expression)) {
		    
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			MethodPalladioInformation methodPalladioInformation = this.getMethodPalladioInformation(methodInvocation);
			
			String potentialInterfaceName = "I" + methodBundlePair.getBundleName();
			if (!potentialInterfaceName.equals(methodPalladioInformation.getOperationInterfaceName())) {
				createExternalCallAction(methodInvocation, methodPalladioInformation);
			} else {
				if (internalCallActionDepth < 1) {
					// TODO: Finish InternalCallAction With Depth 1
					createInternalCallAction(expressionStatement, methodPalladioInformation);					
				} else {
					createInternalAction(expressionStatement);
				}
			}
		} else {
			createInternalAction(expressionStatement);
		}
		return false;
	}
	
	private void createInternalCallAction(ExpressionStatement expressionStatement, MethodPalladioInformation methodPalladioInformation) {
		LOGGER.debug("Expression Statement is Internal Call Action");
		
		ActionSeff internalActionSeff = create.newInternalBehaviour().withStartAction().withName(NameUtil.START_ACTION_NAME).followedBy();
//		internalActionSeff = this.perform(methodPalladioInformation.getMethodBundlePair().getAstNode(), internalActionSeff, internalCallActionDepth + 1);
		InternalSeff internalBehaviour = internalActionSeff.stopAction().withName(NameUtil.STOP_ACTION_NAME).createBehaviourNow();
		
		actionSeff = actionSeff.internalCallAction()
				.withName(NameUtil.getEntityName(expressionStatement))
				.withInternalBehaviour(internalBehaviour)
				.followedBy();

		
	}
	
	private void createExternalCallAction(MethodInvocation methodInvocation, MethodPalladioInformation externalMethodInformation) {
		LOGGER.debug("Expression Statement is External Call Action");
		
		ExternalCallActionCreator externalCallActionCreator = actionSeff.externalCallAction();

		addRequiredInterfaceToComponent(externalMethodInformation.getOperationInterfaceName());
		externalCallActionCreator.withCalledService(create.fetchOfOperationSignature(externalMethodInformation.getOperationSignatureName()));
		externalCallActionCreator.withRequiredRole(create.fetchOfOperationRequiredRole(externalMethodInformation.getOperationInterfaceName()));
		
		OperationSignature calledFunctionSignature = create.fetchOfOperationSignature(externalMethodInformation.getOperationSignatureName());
		VariableUsageCreator variableUsage;
		if (!methodInvocation.arguments().isEmpty()) {
			if (calledFunctionSignature != null && (calledFunctionSignature.getParameters__OperationSignature().size() == methodInvocation.arguments().size())) {
				// try to get variables from interface
				EList<Parameter> calledFunctParameterList = calledFunctionSignature.getParameters__OperationSignature();
				
				for (int i = 0; i < calledFunctParameterList.size(); i++) {
					Parameter para = calledFunctParameterList.get(i);
					Expression castedArgument = (Expression) methodInvocation.arguments().get(i);
					variableUsage = generateInputVariableUsage(castedArgument, para);
					externalCallActionCreator.withInputVariableUsage(variableUsage);
				}
			} else {
				// fallback if interface is not found or argumentsArrays have different sizes
				for (Object argument : methodInvocation.arguments()) {
					Expression castedArgument = (Expression) argument;
					variableUsage = generateInputVariableUsage(castedArgument);
					externalCallActionCreator.withInputVariableUsage(variableUsage);
				}
			}
		}
		
		if (calledFunctionSignature != null && calledFunctionSignature.getReturnType__OperationSignature() != null) {
			DataType returnType = calledFunctionSignature.getReturnType__OperationSignature();
			variableUsage = generateOutputVariableUsage(returnType);
			externalCallActionCreator.withReturnVariableUsage(variableUsage);
		}
		
		actionSeff = externalCallActionCreator.withName(NameUtil.getEntityName(methodInvocation)).followedBy();
	}
	
	private void addRequiredInterfaceToComponent(String requiredInterfaceName) {
		String basicComponentName = methodBundlePair.getBundleName();
		Map<String, List<String>> componentRequiredListMap = componentInformation.getComponentRequiredListMap();
		if (componentRequiredListMap.containsKey(basicComponentName)) {
			List<String> requiredList = componentRequiredListMap.get(basicComponentName);
			if (!requiredList.contains(requiredInterfaceName)) {
				basicComponentCreator.requires(create.fetchOfOperationInterface(requiredInterfaceName), requiredInterfaceName);
				requiredList.add(requiredInterfaceName);
			}
		} else {
			basicComponentCreator.requires(create.fetchOfOperationInterface(requiredInterfaceName), requiredInterfaceName);
			List<String> requiredList = new ArrayList<>();
			requiredList.add(requiredInterfaceName);
			componentRequiredListMap.put(basicComponentName, requiredList);
		}
	}
	
	private void createInternalAction(final ExpressionStatement expressionStatement) {
		LOGGER.debug("Expression Statement is Internal Action");
		actionSeff = actionSeff.internalAction().withName(NameUtil.getEntityName(expressionStatement)).followedBy();
	}
	
	public boolean visit(final IfStatement ifStatement) {
		LOGGER.debug("Visit If Statement");
		BranchActionCreator branchActionCreator = actionSeff.branchAction();
		branchActionCreator = generateBranchAction(ifStatement, branchActionCreator);
		
		if (ifStatement.getElseStatement() != null) {
			
			branchActionCreator = handleElseStatement(ifStatement.getElseStatement(), branchActionCreator);	
		}
		
		actionSeff = branchActionCreator.withName(NameUtil.getEntityName(ifStatement)).followedBy();
		return false;
	}
	
	public boolean visit(final SynchronizedStatement synchronizedStatement) {
		LOGGER.debug("Visit Synchronized Statement");
		
		if (!componentInformation.getIsPassiveResourceSet()) {
			basicComponentCreator.withPassiveResource("1", (ResourceTimeoutFailureType) create.newResourceTimeoutFailureType("PassiveResourceTimeoutFailure").build(), "Passive Resource");
			componentInformation.setPassiveResourceSetTrue();
		}

		actionSeff.acquireAction().withName(NameUtil.ACQUIRE_ACTION_NAME).withPassiveResource(create.fetchOfPassiveResource("Passive Resource")).followedBy();

		actionSeff = this.perform(synchronizedStatement.getBody(), actionSeff);
				
		actionSeff.releaseAction().withName(NameUtil.RELEASE_ACTION_NAME).withPassiveResource(create.fetchOfPassiveResource("Passive Resource")).followedBy();

		return false;
	}
	
	public boolean visit(final TryStatement tryStatement) {
		LOGGER.debug("Visit Try Statement");
		
		BranchActionCreator branchActionCreator = actionSeff.branchAction();
		branchActionCreator = generateBranchAction(tryStatement, branchActionCreator);
		
		List<CatchClause> catchClauseList = tryStatement.catchClauses();
		for (CatchClause catchClause : catchClauseList) {
			ActionSeff innerActionSeff = create.newSeff().withSeffBehaviour().withStartAction().withName(NameUtil.START_ACTION_NAME).followedBy();
			innerActionSeff = this.perform(catchClause.getBody(), innerActionSeff);
			SeffCreator seffCreator = innerActionSeff.stopAction().withName(NameUtil.STOP_ACTION_NAME).createBehaviourNow();
			branchActionCreator = branchActionCreator.withGuardedBranchTransition("expression", seffCreator, NameUtil.getEntityName(tryStatement));
		}
		
		actionSeff = branchActionCreator.withName(NameUtil.getEntityName(tryStatement)).followedBy();
		return false;
	}
	
	public boolean visit(final ForStatement forStatement) {
		LOGGER.debug("Visit For Statement");
		LoopActionCreator loopActionCreator = actionSeff.loopAction();
		Expression forStatementExpression = forStatement.getExpression();
		if (forStatementExpression != null && forStatementExpression instanceof InfixExpression) {
			loopActionCreator.withIterationCount(((InfixExpression) forStatementExpression).getRightOperand().toString());
		}
		loopActionCreator = generateLoopAction(forStatement.getBody(), loopActionCreator);
		actionSeff = loopActionCreator.withName(NameUtil.getEntityName(forStatement)).followedBy();
		return false;
	}
	
	public boolean visit(final EnhancedForStatement forStatement) {
		LOGGER.debug("Visit Enhanced For Statement");
		LoopActionCreator loopActionCreator = actionSeff.loopAction();
		Expression forStatementExpression = forStatement.getExpression();
		if (forStatementExpression != null && forStatementExpression instanceof SimpleName) {
			loopActionCreator.withIterationCount("1");
		}
		loopActionCreator = generateLoopAction(forStatement.getBody(), loopActionCreator);
		actionSeff = loopActionCreator.withName(NameUtil.getEntityName(forStatement)).followedBy();
		return false;
	}
	
	public boolean visit(final WhileStatement whileStatement) {
		LOGGER.debug("Visit While Statement");
		LoopActionCreator loopActionCreator = actionSeff.loopAction();
		Expression whileStatementExpression = whileStatement.getExpression();
		if (whileStatementExpression != null && whileStatementExpression instanceof SimpleName) {
			loopActionCreator.withIterationCount("1");
		}
		loopActionCreator = generateLoopAction(whileStatement.getBody(), loopActionCreator);
		actionSeff = loopActionCreator.withName(NameUtil.getEntityName(whileStatement)).followedBy();
		return false;
	}
	
	public boolean visit(final SwitchStatement switchStatement) {
		LOGGER.debug("Visit Switch Statement");
		BranchActionCreator branchActionCreator = actionSeff.branchAction();
		branchActionCreator.withName(NameUtil.getEntityName(switchStatement));
		
		List<List<Statement>> blockList = SwitchStatementUtil.createBlockListFromSwitchStatement(switchStatement);
		LOGGER.debug("Generate Inner Branch Behaviour");
		for (List<Statement> block : blockList) {
			
			ActionSeff innerActionSeff = create.newSeff().withSeffBehaviour().withStartAction().withName(NameUtil.START_ACTION_NAME).followedBy();
			
			for (Statement statement : block) {
				innerActionSeff = this.perform(statement, innerActionSeff);
			}
			
			SeffCreator seffCreator = innerActionSeff.stopAction().withName(NameUtil.STOP_ACTION_NAME).createBehaviourNow();
			branchActionCreator= branchActionCreator.withGuardedBranchTransition("expression", seffCreator);
		}
		
		actionSeff = branchActionCreator.withName(NameUtil.getEntityName(switchStatement)).followedBy();
		return false;
	}

	/*
	 * Neu in Ast2Seff dazu gekommen, war nicht in JaMoPP vorhanden
	 * Verhalten aus "MediaStore3 -> AudioWatermarking" abgeschaut
	 */
	public boolean visit(final ReturnStatement returnStatement) {
		LOGGER.debug("Visit Return Statement");
		Expression returnExpression = returnStatement.getExpression();
		SetVariableActionCreator setVariableActionCreator = actionSeff.setVariableAction();
		VariableUsageCreator returnVariable = this.generateInputVariableUsage(returnExpression);
		setVariableActionCreator.withLocalVariableUsage(returnVariable);
		this.actionSeff = setVariableActionCreator.followedBy();
		return false;
	}
	
	// TODO: Further work - Limitation
	public boolean visit(final VariableDeclarationStatement variableDeclarationStatement) {
		LOGGER.debug("Visit Variable Declaration Statement");
		Type test = variableDeclarationStatement.getType();
		return super.visit(variableDeclarationStatement);
	}
	
	/*
	 * generates Input Variable Usages
	 * 
	 * Parameters:  
	 * 	Expression: Expression of variable that is passed in AST
	 *	Parameter: optional parameter, if matching interface to function was found for additional 
	 * 
	 * Neu in Ast2Seff dazu gekommen, war nicht in JaMoPP vorhanden
	 * Verhalten aus "MediaStore3 -> AudioWatermarking" abgeschaut +
	 * zus�tzliche infos von: https://www.palladio-simulator.com/tools/tutorials/ (PCM Parameter (PDF) -> 18)
	 * 
	 *** The following types are available
	 * BYTESIZE: Memory footprint of a parameter
	 * VALUE: The actual value of a parameter for primitive types
	 * STRUCTURE: Structure of data, like sorted or unsorted
	 * NUMBER_OF_ELEMENTS: The number of elements in a collection
	 * TYPE: The actual type of a parameter (vs. the declared type)
	 */
	private VariableUsageCreator generateInputVariableUsage(Expression variable, Parameter para) {
		VariableUsageCreator variableUsage = create.newVariableUsage();
		
		
		// TODO: Muss das noch hinzugefügt werden?
		//randomPCMVariable.setSpecification(namespaceReference.getReferenceName().toString() + "." + variableReference.getReferenceName().toString() + "." + booleanVariable.getType().toString());
		
		String randomPCMName;
		DataType paraDataType = para.getDataType__Parameter();
		if (paraDataType instanceof PrimitiveDataType) {
			variableUsage.withNamespaceReference(((PrimitiveDataType) paraDataType).getType().toString(), variable.toString());
			randomPCMName = ((PrimitiveDataType) paraDataType).getType().toString() + "." + variable.toString();
			variableUsage.withVariableCharacterisation(randomPCMName, VariableCharacterisationType.VALUE);
		}
		else if (paraDataType instanceof CompositeDataType) {
			variableUsage.withNamespaceReference(((CompositeDataType) paraDataType).getEntityName(), variable.toString());
			randomPCMName = ((CompositeDataType) paraDataType).getEntityName() + "." + variable.toString();
			variableUsage.withVariableCharacterisation(randomPCMName, VariableCharacterisationType.BYTESIZE);
		}
		else {
			variableUsage.withNamespaceReference(para.getParameterName(), variable.toString());
			randomPCMName = para.getParameterName() + "." + variable.toString();
			variableUsage.withVariableCharacterisation(randomPCMName, VariableCharacterisationType.VALUE);
		}
		return variableUsage;
	}
	
	private VariableUsageCreator generateInputVariableUsage(Expression variable) {
		VariableUsageCreator variableUsage = create.newVariableUsage();
		variableUsage.withNamespaceReference("PrimitiveType", NameUtil.getExpressionClassName(variable));
		String randomPCMName = "PrimitiveType" + "." + NameUtil.getExpressionClassName(variable);
		variableUsage.withVariableCharacterisation(randomPCMName, VariableCharacterisationType.VALUE);
		return variableUsage;
	}
	
	/*
	 * Same as generateVariables above, but for Output Variables 
	 */
	private VariableUsageCreator generateOutputVariableUsage(DataType returnType) {
		// From ParameterFactory Docs: Note that it was an explicit design decision to refer to variable names instead of the actual variables (i.e., by refering to Parameter class).
		VariableUsageCreator variableUsage = create.newVariableUsage();

		String randomPCMName = "tempVariable";
		
		if (returnType instanceof PrimitiveDataType) {
			variableUsage.withNamespaceReference(((PrimitiveDataType) returnType).getType().toString(), randomPCMName);
			randomPCMName = ((PrimitiveDataType) returnType).getType().toString() + "." + randomPCMName;
			variableUsage.withVariableCharacterisation(randomPCMName, VariableCharacterisationType.VALUE);
		}
		else if (returnType instanceof CompositeDataType) {
			variableUsage.withNamespaceReference(((CompositeDataType) returnType).getEntityName(), randomPCMName);
			randomPCMName = ((CompositeDataType) returnType).getEntityName() + "." + randomPCMName;
			variableUsage.withVariableCharacterisation(randomPCMName, VariableCharacterisationType.BYTESIZE);
		}
		else {
			variableUsage.withNamespaceReference(null, randomPCMName);
			variableUsage.withVariableCharacterisation(randomPCMName, VariableCharacterisationType.VALUE);
		}
		return variableUsage;
	}

	private BranchActionCreator generateBranchAction(ASTNode node, BranchActionCreator branchActionCreator) {

		String condition = "";
		
		if (node instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) node;
			node = ifStatement.getThenStatement();
			condition = NameUtil.getIfStatementConditionString(ifStatement);
		} else if (node instanceof TryStatement) {
			TryStatement tryStatement = (TryStatement) node;
			node = tryStatement.getBody();
			condition = NameUtil.getTryStatementConditionString(tryStatement);
		}
		
		LOGGER.debug("Generate Inner Branch Behaviour");
		ActionSeff innerActionSeff = create.newSeff().withSeffBehaviour().withStartAction().withName(NameUtil.START_ACTION_NAME).followedBy();
		innerActionSeff = this.perform(node, innerActionSeff);
		SeffCreator seffCreator = innerActionSeff.stopAction().withName(NameUtil.STOP_ACTION_NAME).createBehaviourNow();
		
		// TODO: Enter Expression
		branchActionCreator.withGuardedBranchTransition(condition, seffCreator, "Guarded Branch Transition").withName(NameUtil.BRANCH_ACTION_NAME);

		return branchActionCreator;
	}
	
	private BranchActionCreator handleElseStatement(Statement statement, BranchActionCreator branchActionCreator) {
		
		ActionSeff innerActionSeff = create.newSeff().withSeffBehaviour().withStartAction().withName(NameUtil.START_ACTION_NAME).followedBy();
		
		String condition = "condition";
		
		if (statement instanceof IfStatement) {
			LOGGER.debug("Else Statement is If Else Statement");
			IfStatement elseIfStatement = (IfStatement) statement;
			innerActionSeff = this.perform(elseIfStatement.getThenStatement(), innerActionSeff);
			condition = NameUtil.getIfStatementConditionString(elseIfStatement);
		} else {
			LOGGER.debug("If Statement is Else Statement");
			innerActionSeff = this.perform(statement, innerActionSeff);
			
			// TODO: How to define else statement?
//			condition = NameUtil.getElseStatementConditionString(statement);
		}
		
		SeffCreator seffCreator = innerActionSeff.stopAction().withName(NameUtil.STOP_ACTION_NAME).createBehaviourNow();
		
		// TODO: Enter Expression
		branchActionCreator = branchActionCreator.withGuardedBranchTransition("expression", seffCreator, "Guarded Branch Transition");

		if (statement instanceof IfStatement) {
			IfStatement elseIfStatement = (IfStatement) statement;
			branchActionCreator = handleElseStatement(elseIfStatement.getElseStatement(), branchActionCreator);
		}
		
		return branchActionCreator;
	}
	
	private LoopActionCreator generateLoopAction(ASTNode node, LoopActionCreator loopActionCreator) {
		LOGGER.debug("Generate Inner Loop Behaviour");
		ActionSeff innerActionSeff = create.newSeff().withSeffBehaviour().withStartAction().withName(NameUtil.START_ACTION_NAME).followedBy();
		innerActionSeff = this.perform(node, innerActionSeff);
		SeffCreator seffCreator = innerActionSeff.stopAction().withName(NameUtil.STOP_ACTION_NAME).createBehaviourNow();
		loopActionCreator.withLoopBody(seffCreator).withName(NameUtil.LOOP_ACTION_NAME);
		return loopActionCreator;
	}
	
	private boolean isExternal(MethodInvocation methodInvocation) {
		String methodName = methodInvocation.getName().toString();
		String className = NameUtil.getClassName(methodInvocation);
		return this.methodPalladioInfoMap.containsKey(className + "." + methodName);
	}
	
	private MethodPalladioInformation getMethodPalladioInformation(MethodInvocation methodInvocation) {
		String methodName = methodInvocation.getName().toString();
		String className = NameUtil.getClassName(methodInvocation);
		if (this.methodPalladioInfoMap.containsKey(className + "." + methodName)) {
			return this.methodPalladioInfoMap.get(className + "." + methodName);
		} else {
			// TODO: handle the return of null
			return null;
		}
	}
}
