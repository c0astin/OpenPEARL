// Generated from OpenPearl.g4 by ANTLR 4.8
package org.openpearl.compiler;

import org.openpearl.compiler.OpenPearlLexer;
import org.openpearl.compiler.SourceLocation;
import org.openpearl.compiler.SourceLocations;
import org.openpearl.compiler.Compiler;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link OpenPearlParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface OpenPearlVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(OpenPearlParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#module}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule(OpenPearlParser.ModuleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#system_part}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSystem_part(OpenPearlParser.System_partContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#systemElementDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSystemElementDefinition(OpenPearlParser.SystemElementDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#systemPartName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSystemPartName(OpenPearlParser.SystemPartNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#configurationElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConfigurationElement(OpenPearlParser.ConfigurationElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#systemDescription}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSystemDescription(OpenPearlParser.SystemDescriptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#association}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssociation(OpenPearlParser.AssociationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#systemElementParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSystemElementParameters(OpenPearlParser.SystemElementParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#problem_part}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProblem_part(OpenPearlParser.Problem_partContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#identification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentification(OpenPearlParser.IdentificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#identificationDenotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentificationDenotation(OpenPearlParser.IdentificationDenotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeForIdentification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeForIdentification(OpenPearlParser.TypeForIdentificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeAttributeForIdentification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeAttributeForIdentification(OpenPearlParser.TypeAttributeForIdentificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#identificationAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentificationAttribute(OpenPearlParser.IdentificationAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDefinition(OpenPearlParser.TypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#identifierForType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierForType(OpenPearlParser.IdentifierForTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#variableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaration(OpenPearlParser.VariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#variableDenotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDenotation(OpenPearlParser.VariableDenotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#problemPartDataAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProblemPartDataAttribute(OpenPearlParser.ProblemPartDataAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeAttribute(OpenPearlParser.TypeAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#allocationProtection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllocationProtection(OpenPearlParser.AllocationProtectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#globalAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGlobalAttribute(OpenPearlParser.GlobalAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecification(OpenPearlParser.SpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#specificationItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecificationItem(OpenPearlParser.SpecificationItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#schedulingSignalReaction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchedulingSignalReaction(OpenPearlParser.SchedulingSignalReactionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#signalRST}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignalRST(OpenPearlParser.SignalRSTContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#signalReaction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignalReaction(OpenPearlParser.SignalReactionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#signalFinalStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignalFinalStatement(OpenPearlParser.SignalFinalStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#induceStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInduceStatement(OpenPearlParser.InduceStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#simpleType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleType(OpenPearlParser.SimpleTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeInteger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeInteger(OpenPearlParser.TypeIntegerContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#mprecision}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMprecision(OpenPearlParser.MprecisionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#integerWithoutPrecision}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerWithoutPrecision(OpenPearlParser.IntegerWithoutPrecisionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeFloatingPointNumber}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeFloatingPointNumber(OpenPearlParser.TypeFloatingPointNumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeBitString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeBitString(OpenPearlParser.TypeBitStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeCharacterString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeCharacterString(OpenPearlParser.TypeCharacterStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeDuration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDuration(OpenPearlParser.TypeDurationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeClock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeClock(OpenPearlParser.TypeClockContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#identifierDenotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierDenotation(OpenPearlParser.IdentifierDenotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#initialisationAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitialisationAttribute(OpenPearlParser.InitialisationAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#initElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitElement(OpenPearlParser.InitElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeStructure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeStructure(OpenPearlParser.TypeStructureContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#structureComponent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructureComponent(OpenPearlParser.StructureComponentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeAttributeInStructureComponent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeAttributeInStructureComponent(OpenPearlParser.TypeAttributeInStructureComponentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#structureSpecfication}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructureSpecfication(OpenPearlParser.StructureSpecficationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#structureDenotationS}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructureDenotationS(OpenPearlParser.StructureDenotationSContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeReference(OpenPearlParser.TypeReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeRefChar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeRefChar(OpenPearlParser.TypeRefCharContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeSema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeSema(OpenPearlParser.TypeSemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeBolt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeBolt(OpenPearlParser.TypeBoltContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeTask}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeTask(OpenPearlParser.TypeTaskContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeInterrupt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeInterrupt(OpenPearlParser.TypeInterruptContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeSignal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeSignal(OpenPearlParser.TypeSignalContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeReferenceCharType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeReferenceCharType(OpenPearlParser.TypeReferenceCharTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#semaDenotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSemaDenotation(OpenPearlParser.SemaDenotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#preset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreset(OpenPearlParser.PresetContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#procedureDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedureDeclaration(OpenPearlParser.ProcedureDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#endOfBlockLoopProcOrTask}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEndOfBlockLoopProcOrTask(OpenPearlParser.EndOfBlockLoopProcOrTaskContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#procedureDenotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedureDenotation(OpenPearlParser.ProcedureDenotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeProcedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeProcedure(OpenPearlParser.TypeProcedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#procedureBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedureBody(OpenPearlParser.ProcedureBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#listOfFormalParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListOfFormalParameters(OpenPearlParser.ListOfFormalParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#formalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameter(OpenPearlParser.FormalParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(OpenPearlParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#virtualDimensionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVirtualDimensionList(OpenPearlParser.VirtualDimensionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#commas}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommas(OpenPearlParser.CommasContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#assignmentProtection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentProtection(OpenPearlParser.AssignmentProtectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#passIdentical}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPassIdentical(OpenPearlParser.PassIdenticalContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#virtualDimensionList2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVirtualDimensionList2(OpenPearlParser.VirtualDimensionList2Context ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#parameterType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterType(OpenPearlParser.ParameterTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeRealTimeObject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeRealTimeObject(OpenPearlParser.TypeRealTimeObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#disableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDisableStatement(OpenPearlParser.DisableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#enableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnableStatement(OpenPearlParser.EnableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#triggerStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerStatement(OpenPearlParser.TriggerStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#resultAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResultAttribute(OpenPearlParser.ResultAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#resultType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResultType(OpenPearlParser.ResultTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#taskDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#taskDenotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskDenotation(OpenPearlParser.TaskDenotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#nameOfModuleTaskProc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNameOfModuleTaskProc(OpenPearlParser.NameOfModuleTaskProcContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#task_main}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTask_main(OpenPearlParser.Task_mainContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#taskBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskBody(OpenPearlParser.TaskBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(OpenPearlParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#unlabeled_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnlabeled_statement(OpenPearlParser.Unlabeled_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#empty_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmpty_statement(OpenPearlParser.Empty_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#label_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabel_statement(OpenPearlParser.Label_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#callStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallStatement(OpenPearlParser.CallStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#listOfActualParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListOfActualParameters(OpenPearlParser.ListOfActualParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#returnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(OpenPearlParser.ReturnStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#gotoStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGotoStatement(OpenPearlParser.GotoStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#exitStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExitStatement(OpenPearlParser.ExitStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#assignment_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment_statement(OpenPearlParser.Assignment_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#dereference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDereference(OpenPearlParser.DereferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#stringSelection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringSelection(OpenPearlParser.StringSelectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#bitSelection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitSelection(OpenPearlParser.BitSelectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#charSelection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharSelection(OpenPearlParser.CharSelectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#sequential_control_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequential_control_statement(OpenPearlParser.Sequential_control_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#if_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_statement(OpenPearlParser.If_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#then_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThen_block(OpenPearlParser.Then_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#else_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElse_block(OpenPearlParser.Else_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#fin_if_case}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFin_if_case(OpenPearlParser.Fin_if_caseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#case_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_statement(OpenPearlParser.Case_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#case_statement_selection1}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_statement_selection1(OpenPearlParser.Case_statement_selection1Context ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#case_statement_selection1_alt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_statement_selection1_alt(OpenPearlParser.Case_statement_selection1_altContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#case_statement_selection_out}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_statement_selection_out(OpenPearlParser.Case_statement_selection_outContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#case_statement_selection2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_statement_selection2(OpenPearlParser.Case_statement_selection2Context ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#case_statement_selection2_alt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_statement_selection2_alt(OpenPearlParser.Case_statement_selection2_altContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#case_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_list(OpenPearlParser.Case_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#index_section}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_section(OpenPearlParser.Index_sectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#constantCharacterString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantCharacterString(OpenPearlParser.ConstantCharacterStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#block_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock_statement(OpenPearlParser.Block_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#blockId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockId(OpenPearlParser.BlockIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#loopStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopStatement(OpenPearlParser.LoopStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#loopBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopBody(OpenPearlParser.LoopBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#loopStatement_for}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopStatement_for(OpenPearlParser.LoopStatement_forContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#loopStatement_from}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopStatement_from(OpenPearlParser.LoopStatement_fromContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#loopStatement_by}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopStatement_by(OpenPearlParser.LoopStatement_byContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#loopStatement_to}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopStatement_to(OpenPearlParser.LoopStatement_toContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#loopStatement_while}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopStatement_while(OpenPearlParser.LoopStatement_whileContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#loopStatement_end}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopStatement_end(OpenPearlParser.LoopStatement_endContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#realtime_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRealtime_statement(OpenPearlParser.Realtime_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#task_control_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTask_control_statement(OpenPearlParser.Task_control_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#task_terminating}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTask_terminating(OpenPearlParser.Task_terminatingContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#task_suspending}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTask_suspending(OpenPearlParser.Task_suspendingContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#taskContinuation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskContinuation(OpenPearlParser.TaskContinuationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#taskResume}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskResume(OpenPearlParser.TaskResumeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#task_preventing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTask_preventing(OpenPearlParser.Task_preventingContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#taskStart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskStart(OpenPearlParser.TaskStartContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#priority}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPriority(OpenPearlParser.PriorityContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#frequency}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrequency(OpenPearlParser.FrequencyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#startCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStartCondition(OpenPearlParser.StartConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#startConditionAFTER}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStartConditionAFTER(OpenPearlParser.StartConditionAFTERContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#startConditionAT}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStartConditionAT(OpenPearlParser.StartConditionATContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#startConditionWHEN}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStartConditionWHEN(OpenPearlParser.StartConditionWHENContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#task_coordination_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTask_coordination_statement(OpenPearlParser.Task_coordination_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#listOfNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListOfNames(OpenPearlParser.ListOfNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#semaRequest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSemaRequest(OpenPearlParser.SemaRequestContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#semaRelease}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSemaRelease(OpenPearlParser.SemaReleaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#semaTry}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSemaTry(OpenPearlParser.SemaTryContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#boltDenotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoltDenotation(OpenPearlParser.BoltDenotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#boltReserve}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoltReserve(OpenPearlParser.BoltReserveContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#boltFree}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoltFree(OpenPearlParser.BoltFreeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#boltEnter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoltEnter(OpenPearlParser.BoltEnterContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#boltLeave}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoltLeave(OpenPearlParser.BoltLeaveContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#interrupt_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterrupt_statement(OpenPearlParser.Interrupt_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#io_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIo_statement(OpenPearlParser.Io_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#open_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpen_statement(OpenPearlParser.Open_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#open_parameterlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpen_parameterlist(OpenPearlParser.Open_parameterlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#open_parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpen_parameter(OpenPearlParser.Open_parameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#open_parameter_idf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpen_parameter_idf(OpenPearlParser.Open_parameter_idfContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#open_parameter_old_new_any}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpen_parameter_old_new_any(OpenPearlParser.Open_parameter_old_new_anyContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#open_close_parameter_can_prm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpen_close_parameter_can_prm(OpenPearlParser.Open_close_parameter_can_prmContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#close_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClose_statement(OpenPearlParser.Close_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#close_parameterlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClose_parameterlist(OpenPearlParser.Close_parameterlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#close_parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClose_parameter(OpenPearlParser.Close_parameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#getStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGetStatement(OpenPearlParser.GetStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#putStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPutStatement(OpenPearlParser.PutStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#writeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWriteStatement(OpenPearlParser.WriteStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#readStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReadStatement(OpenPearlParser.ReadStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#takeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTakeStatement(OpenPearlParser.TakeStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#sendStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSendStatement(OpenPearlParser.SendStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#ioListElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIoListElement(OpenPearlParser.IoListElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#ioDataList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIoDataList(OpenPearlParser.IoDataListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#listOfFormatPositions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListOfFormatPositions(OpenPearlParser.ListOfFormatPositionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#dationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDationName(OpenPearlParser.DationNameContext ctx);
	/**
	 * Visit a parse tree produced by the {@code factorFormat}
	 * labeled alternative in {@link OpenPearlParser#formatPosition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactorFormat(OpenPearlParser.FactorFormatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code factorPosition}
	 * labeled alternative in {@link OpenPearlParser#formatPosition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactorPosition(OpenPearlParser.FactorPositionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code factorFormatPosition}
	 * labeled alternative in {@link OpenPearlParser#formatPosition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactorFormatPosition(OpenPearlParser.FactorFormatPositionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactor(OpenPearlParser.FactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#format}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormat(OpenPearlParser.FormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#absolutePosition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAbsolutePosition(OpenPearlParser.AbsolutePositionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#positionCOL}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionCOL(OpenPearlParser.PositionCOLContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#positionLINE}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionLINE(OpenPearlParser.PositionLINEContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#positionPOS}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionPOS(OpenPearlParser.PositionPOSContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#positionSOP}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionSOP(OpenPearlParser.PositionSOPContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#position}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPosition(OpenPearlParser.PositionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#relativePosition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelativePosition(OpenPearlParser.RelativePositionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#openClosePositionRST}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpenClosePositionRST(OpenPearlParser.OpenClosePositionRSTContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#positionPAGE}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionPAGE(OpenPearlParser.PositionPAGEContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#positionSKIP}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionSKIP(OpenPearlParser.PositionSKIPContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#positionX}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionX(OpenPearlParser.PositionXContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#positionADV}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionADV(OpenPearlParser.PositionADVContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#positionEOF}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionEOF(OpenPearlParser.PositionEOFContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#fixedFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFixedFormat(OpenPearlParser.FixedFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#fieldWidth}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldWidth(OpenPearlParser.FieldWidthContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#significance}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignificance(OpenPearlParser.SignificanceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code floatFormatE}
	 * labeled alternative in {@link OpenPearlParser#floatFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatFormatE(OpenPearlParser.FloatFormatEContext ctx);
	/**
	 * Visit a parse tree produced by the {@code floatFormatE3}
	 * labeled alternative in {@link OpenPearlParser#floatFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatFormatE3(OpenPearlParser.FloatFormatE3Context ctx);
	/**
	 * Visit a parse tree produced by the {@code bitFormat1}
	 * labeled alternative in {@link OpenPearlParser#bitFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitFormat1(OpenPearlParser.BitFormat1Context ctx);
	/**
	 * Visit a parse tree produced by the {@code bitFormat2}
	 * labeled alternative in {@link OpenPearlParser#bitFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitFormat2(OpenPearlParser.BitFormat2Context ctx);
	/**
	 * Visit a parse tree produced by the {@code bitFormat3}
	 * labeled alternative in {@link OpenPearlParser#bitFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitFormat3(OpenPearlParser.BitFormat3Context ctx);
	/**
	 * Visit a parse tree produced by the {@code bitFormat4}
	 * labeled alternative in {@link OpenPearlParser#bitFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitFormat4(OpenPearlParser.BitFormat4Context ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#numberOfCharacters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberOfCharacters(OpenPearlParser.NumberOfCharactersContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#timeFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeFormat(OpenPearlParser.TimeFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#durationFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDurationFormat(OpenPearlParser.DurationFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#decimalPositions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecimalPositions(OpenPearlParser.DecimalPositionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#scaleFactor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScaleFactor(OpenPearlParser.ScaleFactorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code characterStringFormatA}
	 * labeled alternative in {@link OpenPearlParser#characterStringFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterStringFormatA(OpenPearlParser.CharacterStringFormatAContext ctx);
	/**
	 * Visit a parse tree produced by the {@code characterStringFormatS}
	 * labeled alternative in {@link OpenPearlParser#characterStringFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterStringFormatS(OpenPearlParser.CharacterStringFormatSContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#channel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChannel(OpenPearlParser.ChannelContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#index_array}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_array(OpenPearlParser.Index_arrayContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#arraySlice}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArraySlice(OpenPearlParser.ArraySliceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#startIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStartIndex(OpenPearlParser.StartIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#endIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEndIndex(OpenPearlParser.EndIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#interruptDenotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterruptDenotation(OpenPearlParser.InterruptDenotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#signalDenotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignalDenotation(OpenPearlParser.SignalDenotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#dationDenotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDationDenotation(OpenPearlParser.DationDenotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typeDation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDation(OpenPearlParser.TypeDationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sourceSinkAttributeIN}
	 * labeled alternative in {@link OpenPearlParser#sourceSinkAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSourceSinkAttributeIN(OpenPearlParser.SourceSinkAttributeINContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sourceSinkAttributeOUT}
	 * labeled alternative in {@link OpenPearlParser#sourceSinkAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSourceSinkAttributeOUT(OpenPearlParser.SourceSinkAttributeOUTContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sourceSinkAttributeINOUT}
	 * labeled alternative in {@link OpenPearlParser#sourceSinkAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSourceSinkAttributeINOUT(OpenPearlParser.SourceSinkAttributeINOUTContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#systemDation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSystemDation(OpenPearlParser.SystemDationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#classAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassAttribute(OpenPearlParser.ClassAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#controlAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitControlAttribute(OpenPearlParser.ControlAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#alphicDation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlphicDation(OpenPearlParser.AlphicDationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#basicDation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBasicDation(OpenPearlParser.BasicDationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeOfTransmissionDataALL}
	 * labeled alternative in {@link OpenPearlParser#typeOfTransmissionData}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeOfTransmissionDataALL(OpenPearlParser.TypeOfTransmissionDataALLContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeOfTransmissionDataSimpleType}
	 * labeled alternative in {@link OpenPearlParser#typeOfTransmissionData}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeOfTransmissionDataSimpleType(OpenPearlParser.TypeOfTransmissionDataSimpleTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeOfTransmissionDataCompoundType}
	 * labeled alternative in {@link OpenPearlParser#typeOfTransmissionData}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeOfTransmissionDataCompoundType(OpenPearlParser.TypeOfTransmissionDataCompoundTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeOfTransmissionDataIdentifierForType}
	 * labeled alternative in {@link OpenPearlParser#typeOfTransmissionData}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeOfTransmissionDataIdentifierForType(OpenPearlParser.TypeOfTransmissionDataIdentifierForTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#accessAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAccessAttribute(OpenPearlParser.AccessAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#typology}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypology(OpenPearlParser.TypologyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dimension1Star}
	 * labeled alternative in {@link OpenPearlParser#dimension1}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimension1Star(OpenPearlParser.Dimension1StarContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dimension1Integer}
	 * labeled alternative in {@link OpenPearlParser#dimension1}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimension1Integer(OpenPearlParser.Dimension1IntegerContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dimension2Integer}
	 * labeled alternative in {@link OpenPearlParser#dimension2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimension2Integer(OpenPearlParser.Dimension2IntegerContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dimension3Integer}
	 * labeled alternative in {@link OpenPearlParser#dimension3}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimension3Integer(OpenPearlParser.Dimension3IntegerContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#tfu}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTfu(OpenPearlParser.TfuContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#tfuMax}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTfuMax(OpenPearlParser.TfuMaxContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#dimensionAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimensionAttribute(OpenPearlParser.DimensionAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#dimensionAttributeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimensionAttributeDeclaration(OpenPearlParser.DimensionAttributeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#boundaryDenotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoundaryDenotation(OpenPearlParser.BoundaryDenotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#indices}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndices(OpenPearlParser.IndicesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryMultiplicativeExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryMultiplicativeExpression(OpenPearlParser.UnaryMultiplicativeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpression(OpenPearlParser.NotExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sizeofExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSizeofExpression(OpenPearlParser.SizeofExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OrExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpression(OpenPearlParser.OrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code eqRelationalExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqRelationalExpression(OpenPearlParser.EqRelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subtractiveExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubtractiveExpression(OpenPearlParser.SubtractiveExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code upbDyadicExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpbDyadicExpression(OpenPearlParser.UpbDyadicExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code atanExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtanExpression(OpenPearlParser.AtanExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AndExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpression(OpenPearlParser.AndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code taskFunction}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskFunction(OpenPearlParser.TaskFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code gtRelationalExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGtRelationalExpression(OpenPearlParser.GtRelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CONTExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCONTExpression(OpenPearlParser.CONTExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code absExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAbsExpression(OpenPearlParser.AbsExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code neRelationalExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNeRelationalExpression(OpenPearlParser.NeRelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ltRelationalExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLtRelationalExpression(OpenPearlParser.LtRelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code shiftExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShiftExpression(OpenPearlParser.ShiftExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code prioFunction}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrioFunction(OpenPearlParser.PrioFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryAdditiveExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryAdditiveExpression(OpenPearlParser.UnaryAdditiveExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code remainderExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemainderExpression(OpenPearlParser.RemainderExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code baseExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBaseExpression(OpenPearlParser.BaseExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code divideExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDivideExpression(OpenPearlParser.DivideExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lnExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLnExpression(OpenPearlParser.LnExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code cosExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCosExpression(OpenPearlParser.CosExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code additiveExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpression(OpenPearlParser.AdditiveExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpExpression(OpenPearlParser.ExpExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TOFIXEDExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTOFIXEDExpression(OpenPearlParser.TOFIXEDExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code isRelationalExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsRelationalExpression(OpenPearlParser.IsRelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code divideIntegerExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDivideIntegerExpression(OpenPearlParser.DivideIntegerExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unarySubtractiveExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnarySubtractiveExpression(OpenPearlParser.UnarySubtractiveExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lwbMonadicExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLwbMonadicExpression(OpenPearlParser.LwbMonadicExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code entierExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEntierExpression(OpenPearlParser.EntierExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code upbMonadicExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpbMonadicExpression(OpenPearlParser.UpbMonadicExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code geRelationalExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeRelationalExpression(OpenPearlParser.GeRelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sqrtExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSqrtExpression(OpenPearlParser.SqrtExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tanExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTanExpression(OpenPearlParser.TanExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sinExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSinExpression(OpenPearlParser.SinExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code leRelationalExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeRelationalExpression(OpenPearlParser.LeRelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code signExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignExpression(OpenPearlParser.SignExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lwbDyadicExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLwbDyadicExpression(OpenPearlParser.LwbDyadicExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TOFLOATExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTOFLOATExpression(OpenPearlParser.TOFLOATExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code isntRelationalExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsntRelationalExpression(OpenPearlParser.IsntRelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code catExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatExpression(OpenPearlParser.CatExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TOCHARExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTOCHARExpression(OpenPearlParser.TOCHARExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code multiplicativeExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpression(OpenPearlParser.MultiplicativeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tanhExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTanhExpression(OpenPearlParser.TanhExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code cshiftExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCshiftExpression(OpenPearlParser.CshiftExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExorExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExorExpression(OpenPearlParser.ExorExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unarySignedLiteralExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnarySignedLiteralExpression(OpenPearlParser.UnarySignedLiteralExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TOBITExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTOBITExpression(OpenPearlParser.TOBITExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code fitExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFitExpression(OpenPearlParser.FitExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exponentiationExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExponentiationExpression(OpenPearlParser.ExponentiationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code roundExpression}
	 * labeled alternative in {@link OpenPearlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoundExpression(OpenPearlParser.RoundExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#refCharSizeofAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefCharSizeofAttribute(OpenPearlParser.RefCharSizeofAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#unaryLiteralExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryLiteralExpression(OpenPearlParser.UnaryLiteralExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpression(OpenPearlParser.UnaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#numericLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteral(OpenPearlParser.NumericLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#numericLiteralUnsigned}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteralUnsigned(OpenPearlParser.NumericLiteralUnsignedContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#numericLiteralPositive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteralPositive(OpenPearlParser.NumericLiteralPositiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#numericLiteralNegative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteralNegative(OpenPearlParser.NumericLiteralNegativeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(OpenPearlParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#listOfExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListOfExpression(OpenPearlParser.ListOfExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex(OpenPearlParser.IndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpression(OpenPearlParser.PrimaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#constantExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantExpression(OpenPearlParser.ConstantExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#constantFixedExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantFixedExpression(OpenPearlParser.ConstantFixedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#additiveConstantFixedExpressionTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveConstantFixedExpressionTerm(OpenPearlParser.AdditiveConstantFixedExpressionTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#subtractiveConstantFixedExpressionTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubtractiveConstantFixedExpressionTerm(OpenPearlParser.SubtractiveConstantFixedExpressionTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#constantFixedExpressionTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantFixedExpressionTerm(OpenPearlParser.ConstantFixedExpressionTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#multiplicationConstantFixedExpressionTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicationConstantFixedExpressionTerm(OpenPearlParser.MultiplicationConstantFixedExpressionTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#divisionConstantFixedExpressionTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDivisionConstantFixedExpressionTerm(OpenPearlParser.DivisionConstantFixedExpressionTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#remainderConstantFixedExpressionTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemainderConstantFixedExpressionTerm(OpenPearlParser.RemainderConstantFixedExpressionTermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code signPlus}
	 * labeled alternative in {@link OpenPearlParser#sign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignPlus(OpenPearlParser.SignPlusContext ctx);
	/**
	 * Visit a parse tree produced by the {@code signMinus}
	 * labeled alternative in {@link OpenPearlParser#sign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignMinus(OpenPearlParser.SignMinusContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#constantFixedExpressionFactor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantFixedExpressionFactor(OpenPearlParser.ConstantFixedExpressionFactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#constantFixedExpressionFit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantFixedExpressionFit(OpenPearlParser.ConstantFixedExpressionFitContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#convertStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConvertStatement(OpenPearlParser.ConvertStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#convertToStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConvertToStatement(OpenPearlParser.ConvertToStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#convertFromStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConvertFromStatement(OpenPearlParser.ConvertFromStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#listFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListFormat(OpenPearlParser.ListFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#rFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRFormat(OpenPearlParser.RFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#referenceConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferenceConstant(OpenPearlParser.ReferenceConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#fixedConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFixedConstant(OpenPearlParser.FixedConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#fixedNumberPrecision}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFixedNumberPrecision(OpenPearlParser.FixedNumberPrecisionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(OpenPearlParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#stringConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringConstant(OpenPearlParser.StringConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#bitStringConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitStringConstant(OpenPearlParser.BitStringConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#timeConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeConstant(OpenPearlParser.TimeConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#durationConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDurationConstant(OpenPearlParser.DurationConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#hours}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHours(OpenPearlParser.HoursContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#minutes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinutes(OpenPearlParser.MinutesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#seconds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeconds(OpenPearlParser.SecondsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#floatingPointConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatingPointConstant(OpenPearlParser.FloatingPointConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#cpp_inline}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCpp_inline(OpenPearlParser.Cpp_inlineContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#lengthDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLengthDefinition(OpenPearlParser.LengthDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lengthDefinitionFixedType}
	 * labeled alternative in {@link OpenPearlParser#lengthDefinitionType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLengthDefinitionFixedType(OpenPearlParser.LengthDefinitionFixedTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lengthDefinitionFloatType}
	 * labeled alternative in {@link OpenPearlParser#lengthDefinitionType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLengthDefinitionFloatType(OpenPearlParser.LengthDefinitionFloatTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lengthDefinitionBitType}
	 * labeled alternative in {@link OpenPearlParser#lengthDefinitionType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLengthDefinitionBitType(OpenPearlParser.LengthDefinitionBitTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lengthDefinitionCharacterType}
	 * labeled alternative in {@link OpenPearlParser#lengthDefinitionType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLengthDefinitionCharacterType(OpenPearlParser.LengthDefinitionCharacterTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#precision}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision(OpenPearlParser.PrecisionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenPearlParser#length}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLength(OpenPearlParser.LengthContext ctx);
}