/*
 [A "BSD license"]
 Copyright (c) 2012-2022 Rainer Mueller & Marcel Schaible
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
/* changelog
2020-05-50 (rm): loopStatement: loopBody now as separate rule to  simplify the 
   treatment in the ExpressionTypeVisitor 
   
   
2021-10-03 (rm):
   DCL and SPC with mixed types in one DCL/SPC
   task/proc usage with list of identifiers
   
2022-03-20 (rm)
   StructuredType replaced by typeStructure | ID (of type)   
   ID of type replaced by identifierForType
      
*/

grammar OpenPearl;

////////////////////////////////////////////////////////////////////////////////

@header
{
import org.openpearl.compiler.OpenPearlLexer;
import org.openpearl.compiler.SourceLocation;
import org.openpearl.compiler.SourceLocations;
import org.openpearl.compiler.Compiler;
}

////////////////////////////////////////////////////////////////////////////////

tokens {
     Letter,
     Digit
}

////////////////////////////////////////////////////////////////////////////////
// Excerpt from PEARL 90 LANGUAGE REPORT, Version 2.2 September 1998, page 135
//
// Following meta characters are used in the syntax description:
//
// meta character      meaning
// --------------      ---------------------------------------------------------
// ::=                 introduction of a Name (nonterminal symbol) for a language
//                     form
// []                  bracketing of optional parts of a language form
// |                   separation of alternative parts of a language form
// {}                  putting together several elements to a new element
// ...                 one or multiple repetition of the preceding element
//                     (or of several elements bracketed by { } or [ ] )
// §                   separates an explaining comment from a language form Name
// /∗ ∗/               comment brackets: includes an explaining text, possibly
//                     explaining the language form in detail instead of a formal
//                     description
//
// All other elements occurring in the syntax rules are either Names of language
// forms or terminal symbols. Examples for terminal symbols are the PEARL keywords
// or the characters semicolon “;”, opening round bracket “(” and closing round
// bracket “)”, or the apostrophe “ ’ ”; the terminal symbols opening square
// bracket “[” and closing square bracket “]” are printed boldly to distinguish
// them from the meta symbols for optional parts. Attention: the round brackets
// are no meta characters and have thus no grouping effect!
////////////////////////////////////////////////////////////////////////////////

program:
  module+
  ;

////////////////////////////////////////////////////////////////////////////////

ID :   Letter ( Letter | Digit | '_' )* ;
//IDENTIFIER :   Letter ( Letter | Digit | '_' )* ;

////////////////////////////////////////////////////////////////////////////////

module:
  'MODULE' ( '(' nameOfModuleTaskProc ')' 
             | nameOfModuleTaskProc ) ';' 
	cpp_inline* system_part? problem_part? 'MODEND' ';'
  ;

////////////////////////////////////////////////////////////////////////////////

system_part:
    'SYSTEM' ';'
    ( systemElementDefinition | configurationElement | cpp_inline )*
    ;


// Changes added by Hertwig modified by rainer (2021-04-13)
////////////////////////////////////////////////////
//
// Possible composition of the PEARL system part (new syntax)
// pca    : PCA9685Channel(0) --- PCA9685('40'B4,30) --- i2cbus;
// tempsensor: LM75('48'B4) --- i2cbus;

systemElementDefinition:
    systemPartName ':' systemDescription ';'
	;
	
systemPartName:
    ID
    ;
    
configurationElement:
    systemDescription ';'
	;

////////////////////////////////////////////////////////////////////////////////
// should match right hand side of system dation definition
// as well as configuration elements

systemDescription:
	systemPartName systemElementParameters? (association)*
	;

association:
    '---' systemPartName systemElementParameters?
    ;

systemElementParameters:
    '(' constant (',' constant)* ')'
    ;

////////////////////////////////////////////////////////////////////////////////
//  DationSpecification ::=
//    { SPECIFY | SPC } IdentifierDenotation TypeDation [ GlobalAttribute ] ;
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
//  DationDeclaration ::=
//    {DECLARE | DCL} IdentifierDenotation TypeDation [GlobalAttribute]
//    CREATED (Name§SystemDefDation);
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
//  TypeDation ::=
//    DATION SourceSinkAttribute ClassAttribute [ Structure ] [ AccessAttribute ]
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
//  SourceSinkAttribute ::=
//    IN | OUT | INOUT
////////////////////////////////////////////////////////////////////////////////


problem_part:
    'PROBLEM' ';'
    (
          lengthDefinition
        | typeDefinition
        | variableDeclaration
        | specification
        | taskDeclaration
        | procedureDeclaration
        | cpp_inline
    )*
    ;

////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// Identification ::=
//           { SPECIFY | SPC } Identifier [ AllocationProtection ] Type IdentificationAttribute ;

identification:
    ( 'SPECIFY' | 'SPC' ) identificationDenotation ';'
    ;


identificationDenotation:
	 ID  allocationProtection? typeForIdentification identificationAttribute
	 ;
	 
typeForIdentification:
	 parameterType
	 ;

typeAttributeForIdentification:

     ;

//allocation_protection:
//    ID
//    ;

////////////////////////////////////////////////////////////////////////////////
//  IdentificationAttribute ::=
//    IDENT ( Name§Object )
////////////////////////////////////////////////////////////////////////////////

identificationAttribute:
    'IDENT' '(' name ')'
    ;


////////////////////////////////////////////////////////////////////////////////
//
//  ScalarVariableDeclaration ::=
//      { DECLARE | DCL } VariableDenotation [ , VariableDenotation ] ... ;
//
//  VariableDenotation ::=
//      IdentifierDenotation [ AllocationProtection ] TypeAttribute [ GlobalAttribute ] [ InitialisationAttribute ]
//
//  IdentifierDenotation ::=
//      Identifier | (Identifier [ , Identifier ] ... )
//
//  TypeAttribute ::=
//      SimpleType | TypeReference | Identifier§ForType
//
//  SimpleType ::=
//      TypeInteger | TypeFloatingPointNumber | TypeBitString | TypeCharacterString | TypeTime | TypeDuration
//
//  InitialisationAttribute ::=
//      { INITIAL | INIT } ( InitElement [ , InitElement ] ... )
//
//  InitElement ::= Constant
//      | Identifier§NamedConstant | ConstantExpression ...
//
//  Constant ::= Integer
//      | FloatingPointNumber | BitStringConstant
//      | TimeConstant
//      | DurationConstant
//      | NIL
//
////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////
//   TypeDefinition ::=
//     TYPE Identifier§ForType { SimpleType | TypeStructure } ;
////////////////////////////////////////////////////////////////////////////////

typeDefinition :
    'TYPE' identifier ( simpleType | typeStructure ) ';'
    ;
    
identifierForType:
	ID
	;    

////////////////////////////////////////////////////////////////////////////////
//  ScalarVariableDeclaration ::=
//      { DECLARE | DCL } VariableDenotation [ , VariableDenotation ] ... ;
////////////////////////////////////////////////////////////////////////////////
// modification for all types of variable declarations in 1 rule

variableDeclaration :
    ( 'DECLARE' | 'DCL' ) variableDenotation ( ',' variableDenotation )* ';'
//    | cpp_inline
    ;


variableDenotation :
    identifierDenotation dimensionAttribute?
        (problemPartDataAttribute | semaDenotation | boltDenotation | dationDenotation)
    ;

problemPartDataAttribute :
    allocationProtection? typeAttribute globalAttribute? initialisationAttribute?
    ;

typeAttribute :
    simpleType
    | typeStructure
    | identifierForType
    | typeReference
    ;

////////////////////////////////////////////////////////////////////////////////

allocationProtection :
    'INV'
    ;

////////////////////////////////////////////////////////////////////////////////
// GlobalAttribute ::=
//      GLOBAL [ (Identifier§OfaModule) ]
////////////////////////////////////////////////////////////////////////////////

globalAttribute :
    'GLOBAL'  ( '(' ID ')' )?
    ;

specification :
( 'SPECIFY' | 'SPC' ) specificationItem ( ',' specificationItem )* ';'
 //   | cpp_inline
    ;

specificationItem :
	variableDenotation 
	| taskDenotation
	| procedureDenotation
	| interruptDenotation
	| identification
	;    


////////////////////////////////////////////////////////////////////////////////
// SimpleType ::=
//    TypeInteger | TypeFloatingPointNumber | TypeBitString | TypeCharacterString | TypeTime | TypeDuration
////////////////////////////////////////////////////////////////////////////////

simpleType :
       typeInteger
     | typeFloatingPointNumber
     | typeBitString
     | typeCharacterString
     | typeClock
     | typeDuration
    ;

////////////////////////////////////////////////////////////////////////////////
// TypeInteger ::=
//      FIXED [ (Precision) ]
////////////////////////////////////////////////////////////////////////////////

typeInteger :
    'FIXED' ( '(' mprecision ')' )?
    ;

////////////////////////////////////////////////////////////////////////////////
// Precision ::=
//      IntegerWithoutPrecision§GreaterZero
////////////////////////////////////////////////////////////////////////////////

mprecision :
    integerWithoutPrecision
    ;

integerWithoutPrecision :
    IntegerConstant
    ;

////////////////////////////////////////////////////////////////////////////////
//  Integer ::=
//    IntegerWithoutPrecision [ ( Precision ) ]
////////////////////////////////////////////////////////////////////////////////

typeFloatingPointNumber :
    'FLOAT' ( '(' length ')' )?
    ;

typeBitString :
    'BIT' ( '(' length ')' )?
    ;

typeCharacterString :
    ( 'CHARACTER' | 'CHAR' ) ( '(' length ')' )?
    ;

typeDuration :
    'DURATION' | 'DUR'
    ;

typeClock :
    'CLOCK' 
    ;

////////////////////////////////////////////////////////////////////////////////
//  IdentifierDenotation ::=
//      Identifier | (Identifier [ , Identifier ] ... )
////////////////////////////////////////////////////////////////////////////////

identifierDenotation :
    identifier | '(' identifier ( ',' identifier )*  ')'
    ;

////////////////////////////////////////////////////////////////////////////////
//  InitialisationAttribute ::=
//      { INITIAL | INIT } ( InitElement [ , InitElement ] ... )
////////////////////////////////////////////////////////////////////////////////

initialisationAttribute :
    ( 'INITIAL' | 'INIT' ) '(' initElement ( ',' initElement )* ')'
    ;

////////////////////////////////////////////////////////////////////////////////
//  InitElement ::= Constant
//      | Identifier§NamedConstant | ConstantExpression ...
////////////////////////////////////////////////////////////////////////////////

initElement
    : identifier
    | constant
    | constantExpression
    | name
    ;

////////////////////////////////////////////////////////////////////////////////
// TypeStructure ::=
//   STRUCT [ StructureComponent [ , StructureComponent ] ... ]
////////////////////////////////////////////////////////////////////////////////

typeStructure :
    'STRUCT' ('[' | '(/') structureComponent ( ',' structureComponent )* 
	     (']' | '/)')
    ;

////////////////////////////////////////////////////////////////////////////////
// StructureComponent ::=
//   OneIdentifierOrList§ForStructureComponent [ DimensionAttribute ]
//   TypeAttributeInStructureComponent
////////////////////////////////////////////////////////////////////////////////

structureComponent :
    ( ID | '(' ID ( ',' ID)* ')' ) dimensionAttribute? assignmentProtection? typeAttributeInStructureComponent
    ;

////////////////////////////////////////////////////////////////////////////////
// TypeAttributeInStructureComponent ::=
//   [ INV ] { SimpleType | StructuredType | TypeReference }
////////////////////////////////////////////////////////////////////////////////

typeAttributeInStructureComponent :
    (simpleType | typeStructure 
     | identifierForType
     | typeReference )
    ;


////////////////////////////////////////////////////////////////////////////////
// StructureSpecification ::=
//   { SPECIFY | SPC } StructureDenotationS [ , StructureDenotationS ] ... ;
////////////////////////////////////////////////////////////////////////////////

structureSpecfication :
    ;

////////////////////////////////////////////////////////////////////////////////
// StructureDenotationS ::=
//   OneIdentifierOrList§MainRecord [ VirtualDimensionList ]
//   [ INV ] TypeStructure { GlobalAttribute | IdentificationAttribute}
////////////////////////////////////////////////////////////////////////////////

structureDenotationS :
    ;

////////////////////////////////////////////////////////////////////////////////
// TypeReference ::=
//   REF
//   { [ VirtualDimensionList ] [ INV ] { SimpleType | StructuredType }
//   | [ VirtualDimensionList ] { TypeDation | SEMA | BOLT }
//   | TypeProcedure | TASK | INTERRUPT | IRPT | SIGNAL
//   | CHAR( )
//   }
////////////////////////////////////////////////////////////////////////////////

typeReference :
	'REF' virtualDimensionList? assignmentProtection? 
    (
	 simpleType 
	| typeStructure 
	| identifierForType
	| typeDation
	| typeProcedure		
	| typeTask				
	| typeSema
	| typeBolt
	| typeInterrupt		   
	| typeSignal			
	| typeRefChar						
	)
	;

typeRefChar:
	'CHAR' ('('  ')' | '()')
	;
	


typeSema
    : 'SEMA'
    ;

typeBolt
    : 'BOLT'
    ;


typeTask
    : 'TASK'
    ;

typeInterrupt
    : ( 'INTERRUPT' | 'IRPT' )
    ;

typeSignal
    : 'SIGNAL'
    ;

typeReferenceCharType
    : 'CHAR' ( '(' expression ')' )?
    ;

semaDenotation :
    'SEMA' globalAttribute? preset? 
    ;

////////////////////////////////////////////////////////////////////////////////
//   [ PRESET (IntegerWithoutPrecision [ , IntegerWithoutPrecision ] ... ) ];
////////////////////////////////////////////////////////////////////////////////

preset :
    'PRESET' '(' initElement (',' initElement )* ')'
    ;

////////////////////////////////////////////////////////////////////////////////
// ProcedureDeclaration ::=
//   Identifier: { PROCEDURE | PROC } [ ListOfFormalParameters ] [ ResultAttribute ] [ GlobalAttribute ] ;
//      ProcedureBody
//   END;
////////////////////////////////////////////////////////////////////////////////

procedureDeclaration:
	 nameOfModuleTaskProc ':' typeProcedure globalAttribute? ';'
        procedureBody
      'END' ';'
    ;

procedureDenotation:
	 identifierDenotation  typeProcedure globalAttribute? 
    ;
    

typeProcedure:
	 ( 'ENTRY' | ( ':' )? ('PROCEDURE' | 'PROC' ) ) listOfFormalParameters? resultAttribute?
	 ;
	 
////////////////////////////////////////////////////////////////////////////////
// ProcedureBody ::=
//   [ Declaration... ] [ Statement... ]
////////////////////////////////////////////////////////////////////////////////

procedureBody :
    ( variableDeclaration | lengthDefinition | typeDefinition )*
    statement*
    ;

////////////////////////////////////////////////////////////////////////////////
// ListOfFormalParameters ::=
//   (FormalParameter [ , FormalParameter ]...)
////////////////////////////////////////////////////////////////////////////////

listOfFormalParameters :
   '(' formalParameter ( ',' formalParameter )* ')'
   ;

////////////////////////////////////////////////////////////////////////////////
// FormalParameter ::=
//   Identifier or IdentifierList [ VirtualDimensionList ] [ AssignmentProtection ] ParameterType [ IDENTICAL | IDENT ]
////////////////////////////////////////////////////////////////////////////////

formalParameter :
    ( identifier | '(' identifier ( ',' identifier)* ')' )? virtualDimensionList? assignmentProtection? parameterType passIdentical?
    ;

identifier:
   ID
   ;

virtualDimensionList :
	'('  commas  ')'
       | '(' ')'
       | '()'
	;

commas:
	',' (',')*
	;

assignmentProtection :
    'INV'
    ;

passIdentical:
    'IDENTICAL' | 'IDENT'
    ;

////////////////////////////////////////////////////////////////////////////////
// VirtualDimensionList ::= ([,... ])
////////////////////////////////////////////////////////////////////////////////
virtualDimensionList2:
    '(' (',')* ')'
    ;

////////////////////////////////////////////////////////////////////////////////
// TODO: ParameterType ::=
//   SimpleType | TypeReference | TypeStructure
//  | Identifier§ForType | TypeDation | TypeRealTimeObject
////////////////////////////////////////////////////////////////////////////////

parameterType :
      simpleType
    | typeDation
    | typeReference
    | typeStructure
    | identifierForType
    | typeRealTimeObject
    ;

typeRealTimeObject :
      typeSema
    | typeBolt
    | typeInterrupt
    | typeSignal
    ;
    
////////////////////////////////////////////////////////////////////////////////
// DisableStatement ::=
//   DISABLE Name§Interrupt;
////////////////////////////////////////////////////////////////////////////////

disableStatement :
    'DISABLE' name ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// EnableStatement ::=
//   ENABLE Name§Interrupt;
////////////////////////////////////////////////////////////////////////////////

enableStatement :
    'ENABLE' name ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// TriggerStatement ::=
//   TRIGGER Name§Interrupt;
////////////////////////////////////////////////////////////////////////////////

triggerStatement :
    'TRIGGER' name ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// ResultAttribute ::=
//   RETURNS(ResultType)
////////////////////////////////////////////////////////////////////////////////

resultAttribute :
    'RETURNS' '(' resultType ')'
    ;

////////////////////////////////////////////////////////////////////////////////
// ResultType ::=
//   SimpleType | TypeReference | TypeStructure | Identifier§ForType
////////////////////////////////////////////////////////////////////////////////

resultType :
    simpleType
    | typeReference
    | typeStructure
    | identifierForType
    ;


////////////////////////////////////////////////////////////////////////////////
// TaskDeclaration ::=
//   Identifier: TASK [ PriorityAttribute ] [ MAIN ] [ GlobalAttribute ] ; TaskBody
//  END;
////////////////////////////////////////////////////////////////////////////////

taskDeclaration :
    nameOfModuleTaskProc ':' 'TASK' priority? task_main? globalAttribute? ';'
        taskBody 'END' ';' cpp_inline?
    ;

taskDenotation :
	identifierDenotation 'TASK' globalAttribute?
	;
	
nameOfModuleTaskProc :
    ID
    ;
////////////////////////////////////////////////////////////////////////////////

task_main: 'MAIN';

////////////////////////////////////////////////////////////////////////////////

taskBody:
    ( variableDeclaration | lengthDefinition | typeDefinition )*
    procedureDeclaration*
    statement*
    ;


////////////////////////////////////////////////////////////////////////////////

statement:
        label_statement* ( unlabeled_statement | block_statement | cpp_inline )  ;

////////////////////////////////////////////////////////////////////////////////

unlabeled_statement:
      empty_statement
    | realtime_statement
    | interrupt_statement
    | assignment_statement
    | sequential_control_statement
    | io_statement
    | callStatement
    | returnStatement
    | gotoStatement
    | loopStatement
    | exitStatement
    | convertStatement
    ;

////////////////////////////////////////////////////////////////////////////////

empty_statement
    : ';'
    ;

////////////////////////////////////////////////////////////////////////////////

label_statement
    : ID ':'
    ;

////////////////////////////////////////////////////////////////////////////////
// CallStatement ::=
//   [ CALL ] Name§SubprogramProcedure [ ListOfActualParameters ] ;
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// ListOfActualParameters ::=
//   (Expression [ , Expression ]...)
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// Example:
// SPC Output PROC (P FIXED, N FIXED) GLOBAL;
// DCL (Pos, No) FIXED;
// ...
// ! Assignments to Pos and No
// CALL Output (Pos, No);
////////////////////////////////////////////////////////////////////////////////

callStatement
    : 'CALL'? name ';'
    ;

////////////////////////////////////////////////////////////////////////////////

listOfActualParameters
    : '(' expression  ( ',' expression )* ')'
    ;

////////////////////////////////////////////////////////////////////////////////
// ReturnStatement ::=
//   RETURN [ ( Expression ) ] ;
////////////////////////////////////////////////////////////////////////////////

returnStatement
    : 'RETURN' ( '(' expression ')' )? ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// GoToStatement ::=
//   GOTO Identifier§Label ;
////////////////////////////////////////////////////////////////////////////////

gotoStatement
    : 'GOTO' ID ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// ExitStatement ::=
//   EXIT [ Identifier§BlockOrLoop ] ;
////////////////////////////////////////////////////////////////////////////////

exitStatement
    : 'EXIT' ID? ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// Assignment ::=
//    ScalarAssignment | StructureAssignment | RefProcAssignment
////////////////////////////////////////////////////////////////////////////////

assignment_statement:
     (dereference)? name ( bitSelectionSlice | charSelectionSlice)?
       ( ':=' | '=' ) expression ';'
    ;

////////////////////////////////////////////////////////////////////////////////

dereference
    : 'CONT'
    ;

////////////////////////////////////////////////////////////////////////////////
// StructureAssignment ::=
//    Name§Structure 1 { := | = } Expression§Structure 2;
////////////////////////////////////////////////////////////////////////////////

// TODO: MS: 2020-05-23 Is this still relevant?
// 2020-05-28: stringSelection is still used in primaryExpression

////////////////////////////////////////////////////////////////////////////////

stringSelection:
  	  bitSelection
   	| charSelection
   	;

////////////////////////////////////////////////////////////////////////////////
// 2020-03-17 (rm) language report defines that .BIT may be applied on a name
//		x.BIT(3:5).BIT(2) is not allowed
//bitSelection:
//    name bitSelectionSlice+
bitSelection:
    name bitSelectionSlice
    ;

////////////////////////////////////////////////////////////////////////////////

bitSelectionSlice:
    '.' 'BIT' '(' 
    (
    	 expression
       | expression ':' expression '+' IntegerConstant	    	 
       | expression ':' expression   // << may be constant fixed or not
    )  
    ')'
    ;

////////////////////////////////////////////////////////////////////////////////
// 2020-03-17 (rm) language report defines that .CHAR may be applied on a name
//		x.CHAR(3:5).CHAR(2) is not allowed
//charSelection:
//    name charSelectionSlice+
charSelection:
    name charSelectionSlice    
    ;

////////////////////////////////////////////////////////////////////////////////

charSelectionSlice:
   '.' 'CHAR' '(' 
    (
    	 expression
       | expression ':' expression '+' IntegerConstant	    	 
       | expression ':' expression  // << may be constant fixed or not
    )  
    ')'
	;

////////////////////////////////////////////////////////////////////////////////

sequential_control_statement:
      if_statement
    | case_statement
    ;

////////////////////////////////////////////////////////////////////////////////
// IfStatement ::=
//   IF Expression§OfType-BIT(1)
//   THEN [ Statement ... ]
//  [ ELSE [ Statement ... ] ] FIN ;
////////////////////////////////////////////////////////////////////////////////

if_statement
    : 'IF' expression then_block else_block? 'FIN' ';'
    ;

////////////////////////////////////////////////////////////////////////////////

then_block
    : 'THEN' statement+
    ;

////////////////////////////////////////////////////////////////////////////////

else_block
    : 'ELSE' statement+
    ;

////////////////////////////////////////////////////////////////////////////////
// CaseStatement ::=
//   StatementSelection1 | StatementSelection2
////////////////////////////////////////////////////////////////////////////////

case_statement
    : 'CASE' ( case_statement_selection1 | case_statement_selection2 ) 'FIN' ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// CaseStatement1 ::=
//   CASE Expression§WithIntegerAsValue
//     { ALT [ Statement ... ] } ...
//     [ OUT [ Statement ... ] ]
//   FIN ;
////////////////////////////////////////////////////////////////////////////////

case_statement_selection1
    : expression case_statement_selection1_alt+ case_statement_selection_out?
    ;

case_statement_selection1_alt
    : 'ALT' statement+
    ;

case_statement_selection_out
    : 'OUT' statement+
    ;

////////////////////////////////////////////////////////////////////////////////
// CaseStatement2 ::=
//   CASE CaseIndex
//     { ALT ( CaseList ) [ Statement ... ] }...
//     [ OUT [ Statement ... ] ]
//    FIN ;
////////////////////////////////////////////////////////////////////////////////
// CaseIndex ::=
//    Expression§WithValueOfType-FIXED-Or-CHAR(1)
////////////////////////////////////////////////////////////////////////////////

case_statement_selection2
    : expression case_statement_selection2_alt+ case_statement_selection_out?
    ;

case_statement_selection2_alt
    : 'ALT' case_list statement+
    ;

////////////////////////////////////////////////////////////////////////////////
// CaseList ::=
//   IndexSection [ , IndexSection ] ...
////////////////////////////////////////////////////////////////////////////////

case_list
    : '(' index_section ( ',' index_section)*  ')'
    ;

////////////////////////////////////////////////////////////////////////////////
//  IndexSection::=
//    Constant-FIXED-Expression [ : Constant-FIXED-Expression ]
//    | CharacterStringConstant§OfLength1 [ : CharacterStringConstant§OfLength1 ]
////////////////////////////////////////////////////////////////////////////////

index_section
    : constantFixedExpression ( ':' constantFixedExpression )?
    | constantCharacterString ( ':' constantCharacterString )?
    ;

constantCharacterString
    : StringLiteral
    ;

////////////////////////////////////////////////////////////////////////////////
//  Block::=
//    BEGIN
//      [{ Declaration | Identification } ... ] [ Statement ... ]
//    END [ Identifier§Block ] ;
////////////////////////////////////////////////////////////////////////////////

block_statement:
    'BEGIN'
    ( variableDeclaration | lengthDefinition | typeDefinition )*
    statement*
    'END' blockId? ';'
    ;
    
blockId:
	ID
	;    
    
////////////////////////////////////////////////////////////////////////////////

//  LoopStatement ::=
//    [ FOR Indicator§ControlVariable ] [ FROM Expression§InitialValue ] [ BY Expression§StepLength ]
//    [ TO Expression§EndValue ]
//    [ WHILE Expression§Condition ] REPEAT
//    [{ Declaration | Identification }... ]
//    [ Statement ... ] END [ Identifier§Loop ] ;
////////////////////////////////////////////////////////////////////////////////

loopStatement:
    loopStatement_for? loopStatement_from? loopStatement_by? loopStatement_to? loopStatement_while?
    'REPEAT'
       loopBody
    loopStatement_end ';'
    ;

loopBody:
   ( variableDeclaration | lengthDefinition | typeDefinition )*
    statement*
    ;

loopStatement_for:
    'FOR' ID
     ;

loopStatement_from:
    'FROM' expression
     ;

loopStatement_by:
    'BY' expression
    ;

loopStatement_to:
    'TO' expression
     ;

loopStatement_while:
    'WHILE' expression
    ;

loopStatement_end:
    'END' ID?
    ;

////////////////////////////////////////////////////////////////////////////////

realtime_statement
    : task_control_statement
    | task_coordination_statement
    ;

////////////////////////////////////////////////////////////////////////////////

task_control_statement
    : taskStart
    | task_terminating
    | task_suspending
    | taskContinuation
    | taskResume
    | task_preventing
    ;

////////////////////////////////////////////////////////////////////////////////
// 2020-05-06: ID -> name for task control statements
task_terminating
    : 'TERMINATE' name? ';'
    ;

////////////////////////////////////////////////////////////////////////////////

task_suspending
    : 'SUSPEND' name? ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// TaskContinuation ::=
//     [ SimpleStartCondition ] CONTINUE [ Name§Task ] [ Priority ]
//
//  SimpleStartCondition ::=
//      AT Expression§Time | AFTER Expression§Duration | WHEN Name§Interrupt
////////////////////////////////////////////////////////////////////////////////

taskContinuation
    : startCondition? 'CONTINUE' name? priority? ';'
    ;

////////////////////////////////////////////////////////////////////////////////
//
// TaskDelay ::=
//      SimpleStartCondition RESUME;
//
// SimpleStartCondition ::=
//     AT Expression§Time | AFTER Expression§Duration | WHEN Name§Interrupt
////////////////////////////////////////////////////////////////////////////////

taskResume
    : startCondition 'RESUME' ';'
    ;

////////////////////////////////////////////////////////////////////////////////

task_preventing
    : 'PREVENT' name? ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// TaskStart ::=
//      [ StartCondition ] ACTIVATE Name§Task [ Priority ];
////////////////////////////////////////////////////////////////////////////////

taskStart
    : startCondition? frequency? 'ACTIVATE' name  priority? ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// Priority ::=
//   { PRIORITY | PRIO } Expression§WithPositiveIntegerAsValue
////////////////////////////////////////////////////////////////////////////////

priority
    : ( 'PRIORITY' | 'PRIO' ) expression
    ;

////////////////////////////////////////////////////////////////////////////////
// Frequency ::=
//   ALL Expression§Duration [ { UNTIL Expression§Time } | { DURING Expression§Duration } ]
////////////////////////////////////////////////////////////////////////////////

frequency
    : 'ALL' expression ( 'UNTIL' expression |  'DURING' expression )?
    ;

////////////////////////////////////////////////////////////////////////////////
// StartCondition ::=
//   AT Expression§Time [ Frequency ]
//   | AFTER Expression§Duration [ Frequency ]
//   | WHEN Name§Interrupt [ AFTER Expression§Duration ] [ Frequency ]
//   | Frequency
////////////////////////////////////////////////////////////////////////////////
// SimpleStartCondition ::=
//   AT Expression§Time | AFTER Expression§Duration | WHEN Name§Interrupt
////////////////////////////////////////////////////////////////////////////////

// need default visitor for startCondition
//startCondition													
//    :
//      'AFTER'   expression                                      # startConditionAFTER
//    | 'AT'      expression                                      # startConditionAT
//    | 'WHEN'    name  ( 'AFTER' expression)?                      # startConditionWHEN
//    ;
startCondition :
    (startConditionAFTER
    | startConditionAT
    | startConditionWHEN
    )
    ;

startConditionAFTER: 
    'AFTER'   expression
	;

startConditionAT: 
    'AT'   expression
	;

startConditionWHEN: 
    'WHEN'    name  ( 'AFTER' expression)?
	;

////////////////////////////////////////////////////////////////////////////////

task_coordination_statement
    : semaRequest
    | semaRelease
    | boltReserve
    | boltFree
    | boltEnter
    | boltLeave
    ;
    
// generic rule for semaphore and bolt operations, which have a list of names     
listOfNames : 
	name ( ',' name)* 
	;    

////////////////////////////////////////////////////////////////////////////////
// REQUEST Name§Sema [ , Name§Sema ]... ;
////////////////////////////////////////////////////////////////////////////////

semaRequest
    : 'REQUEST' listOfNames ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// RELEASE Name§Sema [ , Name§Sema ]...;
////////////////////////////////////////////////////////////////////////////////

semaRelease
    : 'RELEASE' listOfNames ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// TRY Name§Sema;
////////////////////////////////////////////////////////////////////////////////

semaTry
    : 'TRY' listOfNames
    ;


boltDenotation :
    'BOLT' globalAttribute? 
    ;



////////////////////////////////////////////////////////////////////////////////
// BoltStatement ::=
//      RESERVE Name§Bolt [ , Name§Bolt ] ... ;
//    | FREE    Name§Bolt [ , Name§Bolt ] ... ;
//    | ENTER   Name§Bolt [ , Name§Bolt ] ... ;
//    | LEAVE   Name§Bolt [ , Name§Bolt ] ... ;
////////////////////////////////////////////////////////////////////////////////

boltReserve:
     'RESERVE'  listOfNames ';'
      ;

////////////////////////////////////////////////////////////////////////////////

boltFree:
     'FREE' listOfNames ';'
      ;

////////////////////////////////////////////////////////////////////////////////

boltEnter:
     'ENTER'   listOfNames ';'
     ;

////////////////////////////////////////////////////////////////////////////////

boltLeave:
     'LEAVE'  listOfNames ';'
     ;

////////////////////////////////////////////////////////////////////////////////
// Interrupt-Anweisung ::=
//   { ENABLE | DISABLE | TRIGGER } Name§Interrupt ;
////////////////////////////////////////////////////////////////////////////////

interrupt_statement :
      enableStatement
    | disableStatement
    | triggerStatement
    ;

////////////////////////////////////////////////////////////////////////////////

io_statement:
      open_statement
    | close_statement
    | putStatement
    | getStatement
    | writeStatement
    | readStatement
    | sendStatement
    | takeStatement
    ;

////////////////////////////////////////////////////////////////////////////////

// OpenStatement ::=
//      OPEN Name§Dation [ BY OpenParameter [ , OpenParameter ] ... ] ;
//
// When executing the open statement, a data station with typology is positioned at its beginning.
// The open parameters serve to handle data stations containing identifyable files. E.g., a system
// defined data station Disk can possess a file TAB1, which is also maintained after terminating the program under
// this name. Later on, the same or another program can create a user defined data station Table on Disk,
// identified with file TAB1 in the open statement.
//
// OpenParameter ::=
//      IDF ( {Name§CharacterVariable | CharacterStringConstant } ) | RST (Name§ErrorVariable-FIXED) |
//      { OLD | NEW | ANY } |
//      { CAN | PRM }

open_statement:
    'OPEN' dationName ( 'BY' open_parameterlist )? ';'
    ;

////////////////////////////////////////////////////////////////////////////////

open_parameterlist:
    open_parameter (',' open_parameter)*
    ;

////////////////////////////////////////////////////////////////////////////////

open_parameter:
    open_parameter_idf
    | openClosePositionRST                 
    | open_parameter_old_new_any         
    | open_close_parameter_can_prm      
    ;

////////////////////////////////////////////////////////////////////////////////

open_parameter_idf:
    'IDF' '(' ( ID | StringLiteral ) ')'  	
    ;

//open_close_RST :
//	'RST' ( '(' ID ')' )
//	;


open_parameter_old_new_any:
      'OLD'                    
    | 'NEW'                   
    | 'ANY'                 
    ;

open_close_parameter_can_prm:
      'CAN'                
    | 'PRM'              
    ;

////////////////////////////////////////////////////////////////////////////////
// CloseStatement ::=
//   CLOSE Name§Dation [ BY CloseParameter [ , CloseParameter ] ... ] ;
////////////////////////////////////////////////////////////////////////////////

close_statement:
    'CLOSE' dationName ( 'BY' close_parameterlist)? ';'
    ;

////////////////////////////////////////////////////////////////////////////////

close_parameterlist:
    close_parameter (',' close_parameter)*
    ;

////////////////////////////////////////////////////////////////////////////////
// CloseParameter ::=
//   CAN | PRM | RST (Name§ErrorVariable-FIXED)
////////////////////////////////////////////////////////////////////////////////

close_parameter :
      open_close_parameter_can_prm
    | openClosePositionRST                 
     ;

//////////////////////////////////////////////////////////////////////////////////    
// let's treat all i/o statements (PUT,GET,READ,WRITE,TRAKE,SEND, CONVERT FROM/TO 
// identical here and verify in the semantic analysis if wrong elements are used
// --> allow non, one or multiple data 
// --> allow name, expression and arraySlice for all io statements
//
// and the same for the formats!

////////////////////////////////////////////////////////////////////////////////
// GetStatement ::=
//   GET [ { Name§Variable | Segment } [ , { Name§Variable | Segment } ] ... ] FROM Name§Dation [ BY FormatPosition [ , FormatPosition ] ... ] ;
////////////////////////////////////////////////////////////////////////////////

//getStatement :
//    'GET' ( ID ( ',' ID )* )?  'FROM' dationName  'BY' formatPosition ( ',' formatPosition )* ';'
//    ;

getStatement:
    'GET' ioDataList? 'FROM' dationName
    ( 'BY' listOfFormatPositions )? ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// PutStatement ::=
//   PUT [ { Expression | ArraySlice } [ , { Expression | ArraySlice } ] ... ] TO Name§Dation [ BY FormatPosition [ , FormatPosition ] ... ] ;
////////////////////////////////////////////////////////////////////////////////
putStatement:
    'PUT' ioDataList? 'TO' dationName
    ( 'BY' listOfFormatPositions )? ';'
    ;


////////////////////////////////////////////////////////////////////////////////
// WriteStatement ::=
//  WRITE [ { Expression | ArraySlice } [ , { Expression | ArraySlice } ] ... ]
//     TO Name§Dation [ BY Position [ , Position ] ... ] ;
//
// ArraySlice ::=
//   Name§Field ( [ Index , ] ... Index : Index)
//
// Index ::=
//   Expression§WithIntegerAsValue
////////////////////////////////////////////////////////////////////////////////

writeStatement:
    'WRITE' ioDataList? 'TO' dationName
    ( 'BY' listOfFormatPositions )? ';'
    ;
    
//    'WRITE' ( expression ( ',' expression )* )? 
//	'TO' dationName
//	(  'BY' position ( ',' position )* )? ';'
//    ;

////////////////////////////////////////////////////////////////////////////////
// ReadStatement ::=
//   READ [ { Name§Variable | ArraySlice } [ , { Name§Variable | ArraySlice } ] ... ] FROM Name§Dation [ BY Position [ , Position ] ... ] ;
////////////////////////////////////////////////////////////////////////////////

readStatement :
    'READ' ioDataList? 'FROM' dationName
    ( 'BY' listOfFormatPositions )? ';'
    ;
//    'READ' ID  (','  ID  )* 
//	'FROM' dationName  
//	(  'BY' position ( ',' position )* )? ';'
//  ;

////////////////////////////////////////////////////////////////////////////////
// TakeStatement ::=
//   TAKE [ Name§Variable ] FROM Name§Dation
//     [ BY RST-S-CTRL-Format [ , RST-S-CTRL-Format ] ... ] ;
////////////////////////////////////////////////////////////////////////////////

takeStatement:
    //'TAKE' ID?  takeFrom ( 'BY'  take_send_rst_s_ctrl_format ( ',' take_send_rst_s_ctrl_format)* )? ';'
//    'TAKE' ID?  'FROM' dationName( 'BY'  take_send_rst )? ';'
//    ;
    'TAKE' ioDataList? 'FROM' dationName
    ( 'BY' listOfFormatPositions )? ';'
    ;


////////////////////////////////////////////////////////////////////////////////
// SendStatement ::=
//   SEND [ Expression ] TO Name§Dation
//    [ BY RST-S-CTRL-Format [ , RST-S-CTRL-Format ] ... ] ;
////////////////////////////////////////////////////////////////////////////////

sendStatement :
    //'SEND' expression? sendTo ( 'BY'  take_send_rst_s_ctrl_format ( ',' take_send_rst_s_ctrl_format)* )? ';'
    //'SEND' expression? 'TO' dationName ( 'BY'  take_send_rst )? ';'
    //;
    'SEND' ioDataList? 'TO' dationName
    ( 'BY' listOfFormatPositions )? ';'
    ;    
    
// obsolete 2020-02-24 (rm)
//take_send_rst :
//      'RST' '(' ID ')' 
//	;

// note 'name' is part of rule 'expression'
ioListElement:
	(expression | arraySlice)
	;
	
ioDataList:
	ioListElement (',' ioListElement) *
	;	
	
	
listOfFormatPositions:
	formatPosition ( ',' formatPosition )* 
	;

dationName: 
	name
	;

////////////////////////////////////////////////////////////////////////////////
// FormatPosition ::=
//   [ Factor ] { Format | Position } |
//   Factor ( FormatPosition [ , FormatPosition ] ... )
////////////////////////////////////////////////////////////////////////////////

formatPosition:
      factor? format                                 # factorFormat
    | factor? position                               # factorPosition
    | factor '(' listOfFormatPositions  ')'			 # factorFormatPosition
    ;

////////////////////////////////////////////////////////////////////////////////
// Factor ::=
//   ( Expression§IntegerGreaterZero ) | IntegerWithoutPrecision§GreaterZero
////////////////////////////////////////////////////////////////////////////////

factor:
    '(' expression ')' | integerWithoutPrecision
    ;

////////////////////////////////////////////////////////////////////////////////
// Format ::=
//   FixedFormat | FloatFormat | CharacterStringFormat | BitFormat | TimeFormat | DurationFormat | ListFormat | R-Format | RST (Name)
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
// Format ::=
//   { F | E} ( Expression [ , Expression [ , Expression ] ] ) | { B | B1 | B2 | B3 | B4 | A } [ ( Expression ) ]
//   | { T | D } ( Expression [ , Expression ] )
//   | LIST
//   | R ( Identifier§Format )
//   | S ( Name§LengthVariable-FIXED )
////////////////////////////////////////////////////////////////////////////////

format:
      fixedFormat
    | floatFormat
    | bitFormat
    | timeFormat
    | durationFormat
    | listFormat
    | characterStringFormat
    ;

////////////////////////////////////////////////////////////////////////////////
// Position ::=
//   RST ( Name§ErrorVariable-FIXED )
//   | { X | SKIP | PAGE } [ ( Expression ) ]
//   | { POS | ADV } ( Expression [ , Expression [ , Expression ] ] )
//   | { COL | LINE } ( Expression )
//   | SOP ( Name [ , Name [ , Name ] ] /∗ PositionVariables-FIXED ∗/ )
////////////////////////////////////////////////////////////////////////////////
absolutePosition:
       positionCOL
     | positionLINE
     | positionPOS
     | positionSOP
     ;

positionCOL:
       'COL' '(' expression ')'
	;

positionLINE:
     'LINE' '(' expression ')'                           
     ;

positionPOS:
     'POS' '(' ( (  expression ',' )? expression ',' )?
       expression ')'
     ;

positionSOP:
     'SOP' '(' ( ( ID ',' )? ID ',' )? ID ')'     
     ;


////////////////////////////////////////////////////////////////////////////////
// Position ::=
//   RST ( Name§ErrorVariable-FIXED )
//   | { X | SKIP | PAGE } [ ( Expression ) ]
//   | { POS | ADV } ( Expression [ , Expression [ , Expression ] ] )
//   | { COL | LINE } ( Expression )
//   | SOP ( Name [ , Name [ , Name ] ] /∗ PositionVariables-FIXED ∗/ )
////////////////////////////////////////////////////////////////////////////////
position:
      openClosePositionRST
    | relativePosition
    | absolutePosition
    ;

////////////////////////////////////////////////////////////////////////////////
// RelativePosition ::=
//   { X | SKIP | PAGE } [ (Expression) ] |
//   ADV ( [ [ Expression , ] Expression , ] Expression )
////////////////////////////////////////////////////////////////////////////////
relativePosition:
    | positionX
    | positionSKIP
    | positionPAGE
    | positionADV
    | positionEOF
    ;


openClosePositionRST :
	'RST' '(' name ')' 
	;

positionPAGE:
	'PAGE' ( '(' expression ')' )?
	;

positionSKIP:
	'SKIP' ( '(' expression ')' )?
	;

positionX:
	'X' ( '(' expression ')' )?
	;

positionADV:
    | 'ADV' '(' ( ( expression ',' )? expression ',' )?
      expression ')'                                     
    ;

positionEOF:
	'EOF'
	;
	
////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////
// FixedFormat ::=
//   F (FieldWidth [ , DecimalPositions [ , ScaleFactor ] ] )
////////////////////////////////////////////////////////////////////////////////

// no scaleFactor in OpenPEARL
//fixedFormat :
//    'F' '(' fieldWidth ( ',' decimalPositions ( ',' scaleFactor )? )? ')'
//    ;

fixedFormat:
    'F' '(' fieldWidth ( ',' decimalPositions )? ')'
    ;

////////////////////////////////////////////////////////////////////////////////
// FieldWidth ::= Expression§WithPositiveIntegerAsValue
////////////////////////////////////////////////////////////////////////////////

fieldWidth:
    expression
    ;

////////////////////////////////////////////////////////////////////////////////
// FloatFormat ::=
// E[3] (FieldWidth [ , DecimalPositions [ , Significance ] ] )
//
// Significance ::= Expression§WithIntegerAsValue
////////////////////////////////////////////////////////////////////////////////

significance:
	expression
	;

floatFormat:
      'E'  '(' fieldWidth ( ',' decimalPositions ( ',' significance )? )? ')'   # floatFormatE
    | 'E3' '(' fieldWidth ( ',' decimalPositions ( ',' significance )? )? ')'   # floatFormatE3
    ;

////////////////////////////////////////////////////////////////////////////////
// BitFormat ::=
//      { B | B1 | B2 | B3 | B4 } [ ( Expression§NumberCharacters) ]
////////////////////////////////////////////////////////////////////////////////

bitFormat:
      ( 'B' | 'B1')  ( '(' numberOfCharacters ')' )?   # bitFormat1
    | 'B2' ( '(' numberOfCharacters ')' )?             # bitFormat2
    | 'B3' ( '(' numberOfCharacters ')' )?             # bitFormat3
    | 'B4' ( '(' numberOfCharacters ')' )?             # bitFormat4
    ;

////////////////////////////////////////////////////////////////////////////////

numberOfCharacters :
    expression
    ;

////////////////////////////////////////////////////////////////////////////////
// TimeFormat ::=
//      T ( FieldWidth [ , DecimalPositions ] )
////////////////////////////////////////////////////////////////////////////////

timeFormat:
    'T' '(' fieldWidth ( ',' decimalPositions )? ')'
    ;

////////////////////////////////////////////////////////////////////////////////
// DurationFormat ::=
//      D ( FieldWidth [ , DecimalPositions ] )
////////////////////////////////////////////////////////////////////////////////


durationFormat:
    'D' '(' fieldWidth ( ',' decimalPositions )? ')'
    ;

////////////////////////////////////////////////////////////////////////////////
// DecimalPositions ::= Expression§WithNonNegativeIntegerAsValue
////////////////////////////////////////////////////////////////////////////////

decimalPositions:
    expression
    ;

////////////////////////////////////////////////////////////////////////////////
// ScaleFactor ::= Expression§WithIntegerAsValue
////////////////////////////////////////////////////////////////////////////////

scaleFactor:
    expression
    ;

////////////////////////////////////////////////////////////////////////////////
// CharacterStringFormat ::=
//   A [ (Expression§NumberCharacters) ] | S (Name§NumberCharactersVariableFixed)
////////////////////////////////////////////////////////////////////////////////

characterStringFormat :
      'A' ( '(' fieldWidth ')' )?       # characterStringFormatA
    | 'S' '(' ID ')'                    # characterStringFormatS
    ;

////////////////////////////////////////////////////////////////////////////////

channel: ID;


index_array :
	expression
	;

arraySlice :
    name '(' startIndex ':' endIndex ')'
    ;

startIndex:
    listOfExpression
    ;

endIndex:
    expression; 

////////////////////////////////////////////////////////////////////////////////
// Position ::=
//   AbsolutePosition | RelativePosition | RST (Name§ErrorVariable-FIXED)
////////////////////////////////////////////////////////////////////////////////





interruptDenotation :
     identifierDenotation ( 'INTERRUPT' | 'IRPT' ) globalAttribute? 
    ;

////////////////////////////////////////////////////////////////////////////////
// ListOfConstants ::=
//   ConstantParameter [, ConstantParameter ]...
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// ConstantParameter ::=
//   IntegerWithoutPrecision | BitStringConstant | CharacterStringConstant
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// Association ::=
//  --- { Identifier§UsernameOfConnectionProvider
//  | Identifier§ConnectionProviderSystemName [ ( ListOfConstants) ] }
//  [ - - - { Identifier§UsernameOfConnectionProvider
//  | Identifier§ConnectionProviderSystemName [ ( ListOfConstants) ] } ]...;
////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////
//  DationSpecification ::=
//    { SPECIFY | SPC } IdentifierDenotation TypeDation [ GlobalAttribute ] ;
////////////////////////////////////////////////////////////////////////////////
/* obsolte replaced by dationDenotation
dationSpecification
//    : ( 'SPECIFY' | 'SPC' ) identifierDenotation specifyTypeDation globalAttribute? ';'
    : ( 'SPECIFY' | 'SPC' ) identifierDenotation typeDation globalAttribute? ';'
    ;
*/
////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////
// DationDeclaration ::=
//    {DECLARE | DCL} IdentifierDenotation TypeDation [GlobalAttribute] CREATED (Name§SystemDefDation);
////////////////////////////////////////////////////////////////////////////////

dationDenotation :
    typeDation  globalAttribute? ('CREATED' '(' ID  ')')? 
    ;

////////////////////////////////////////////////////////////////////////////////
//  TypeDation ::=
//    DATION SourceSinkAttribute ClassAttribute [ Structure ] [ AccessAttribute ]
////////////////////////////////////////////////////////////////////////////////

typeDation:
    'DATION' sourceSinkAttribute classAttribute typology? accessAttribute?
    ;

////////////////////////////////////////////////////////////////////////////////
//  SourceSinkAttribute ::=
//    IN | OUT | INOUT
////////////////////////////////////////////////////////////////////////////////

sourceSinkAttribute
    : 'IN'                  # sourceSinkAttributeIN
    | 'OUT'                 # sourceSinkAttributeOUT
    | 'INOUT'               # sourceSinkAttributeINOUT
    ;

////////////////////////////////////////////////////////////////////////////////

systemDation
    : 'SYSTEM'
    ;

////////////////////////////////////////////////////////////////////////////////
//  ClassAttribute ::=
//    [ SYSTEM ]                       /* system dation */
//    ALPHIC                       |   /* PUT/GET */
//    BASIC TypeOfTransmissionData |   /* TAKE/SEND */
//    TypeOfTransmissionData           /* READ/WRITE */
////////////////////////////////////////////////////////////////////////////////

classAttribute
    : systemDation? ( alphicDation | basicDation typeOfTransmissionData| typeOfTransmissionData)
    ;

////////////////////////////////////////////////////////////////////////////////

alphicDation
    : 'ALPHIC'
    ;
////////////////////////////////////////////////////////////////////////////////

basicDation
    : 'BASIC'
    ;

////////////////////////////////////////////////////////////////////////////////
//  TypeOfTransmissionData ::=
//    ALL | SimpleType | CompoundType
////////////////////////////////////////////////////////////////////////////////

typeOfTransmissionData
    : 'ALL'                     # typeOfTransmissionDataALL
    | simpleType                # typeOfTransmissionDataSimpleType
    | typeStructure              # typeOfTransmissionDataCompoundType
    | identifierForType	         # typeOfTransmissionDataIdentifierForType
    ;
    
// AccessAttribute ::=
//  { DIRECT | FORWARD | FORBACK } [ NOCYCL | CYCLIC ] [ STREAM | NOSTREAM ]
////////////////////////////////////////////////////////////////////////////////

accessAttribute
    : ( 'DIRECT' | 'FORWARD' | 'FORBACK' ) ( 'NOCYCL' | 'CYCLIC' )? ( 'STREAM' | 'NOSTREAM' )?
    ;

////////////////////////////////////////////////////////////////////////////////
// Typology ::=
//      DIM( {⇤ |pi} [,pi[,pi]] ) [TFU[MAX]]
////////////////////////////////////////////////////////////////////////////////

typology :
    'DIM'
    '('
        dimension1 ( ( ',' dimension2 ) ( ',' dimension3 )? )?
    ')' ( tfu )?
    ;

dimension1:
     '*'                      # dimension1Star
    | constantFixedExpression          # dimension1Integer
    ;

dimension2:
    constantFixedExpression          # dimension2Integer
    ;

dimension3:
    constantFixedExpression          # dimension3Integer
    ;

tfu:
   'TFU' ( tfuMax )?
   ;

tfuMax: 
   'MAX'
   ;

////////////////////////////////////////////////////////////////////////////////
// DimensionAttribute ::=
//  (BoundaryDenotation§FirstDimension [ , BoundaryDenotation§FurtherDimension ] ...)
////////////////////////////////////////////////////////////////////////////////
dimensionAttribute:
	dimensionAttributeDeclaration
	| virtualDimensionList
	;

	
dimensionAttributeDeclaration:
    '(' boundaryDenotation ( ',' boundaryDenotation )* ')'
    ;

boundaryDenotation:
    constantFixedExpression ( ':' constantFixedExpression )?
    ;

////////////////////////////////////////////////////////////////////////////////

indices:
    '(' expression ( ',' expression )* ')'
    ;


////////////////////////////////////////////////////////////////////////////////
// Ranks of the operators defined in PEARL
//
// rank      dyadic operators         evaluation order
// -------------------------------------------------------------
//  1        **, FIT, LWB, UPB        from the right to the left
//  2        *, /, ><, //, REM        from the left to the right
//  3        +, -, <>, SHIFT          from the left to the right
//  4        <, >, <=, >=             from the left to the right
//  5        ==, /=, IS, ISNT         from the left to the right
//  6        AND                      from the left to the right
//  7        OR, EXOR                 from the left to the right
//
// All monadic standard operators have rank 1.
////////////////////////////////////////////////////////////////////////////////

expression:													
    primaryExpression                                       # baseExpression
    | op='ATAN' expression                                  # atanExpression
    | op='COS' expression                                   # cosExpression
    | op='EXP' expression                                   # expExpression
    | op='LN' expression                                    # lnExpression
    | op='SIN' expression                                   # sinExpression
    | op='SQRT' expression                                  # sqrtExpression
    | op='TAN' expression                                   # tanExpression
    | op='TANH' expression                                  # tanhExpression
    | op='ABS' expression                                   # absExpression
    | op='SIGN' expression                                  # signExpression
    | op='SIZEOF' ( name | simpleType)					// note ID is part of the rule name
                  refCharSizeofAttribute?					# sizeofExpression
    | op='NOT' expression                                   # notExpression
    | op='TOBIT' expression                                 # TOBITExpression
    | op='TOFIXED' expression                               # TOFIXEDExpression
    | op='TOFLOAT' expression                               # TOFLOATExpression
    | op='TOCHAR' expression                                # TOCHARExpression
    | op='ENTIER' expression                                # entierExpression
    | op='ROUND' expression                                 # roundExpression
    | op='CONT' expression                                  # CONTExpression
    | op='LWB' expression                                   # lwbMonadicExpression
    | op='UPB' expression                                   # upbMonadicExpression
    | op='NOW'                                              # nowFunction
    | op='DATE'                                             # dateFunction
    | op='TASK' ( '(' expression ')' )?                     # taskFunction
    | op='PRIO' ( '(' expression ')' )?                     # prioFunction
    | <assoc=right> expression op='**'  expression          # exponentiationExpression
    | <assoc=right> expression op='FIT' expression          # fitExpression
    | <assoc=right> expression op='LWB' expression          # lwbDyadicExpression
    | <assoc=right> expression op='UPB' expression          # upbDyadicExpression
    | op=('*'|'/') expression                               # unaryMultiplicativeExpression
    | op='-' expression                                     # unarySubtractiveExpression
    | op='+' expression                                     # unaryAdditiveExpression
    | expression op='*' expression                          # multiplicativeExpression
    | expression op='/' expression                          # divideExpression
    | expression op='//' expression                         # divideIntegerExpression
    | expression op='REM' expression                        # remainderExpression
    | expression op=('CAT'|'><') expression                 # catExpression
    | expression op='+' expression                          # additiveExpression
    | expression op='-' expression                          # subtractiveExpression
    | expression op=('CSHIFT'|'<>') expression              # cshiftExpression
    | expression op='SHIFT' expression                      # shiftExpression
    | expression op=( '<'|'LT') expression                  # ltRelationalExpression
    | expression op=( '<='|'LE') expression                 # leRelationalExpression
    | expression op=( '>'|'GT') expression                  # gtRelationalExpression
    | expression op=( '>='|'GE') expression                 # geRelationalExpression
    | expression op=( '=='|'EQ') expression                 # eqRelationalExpression
    | expression op=( '/='|'NE') expression                 # neRelationalExpression
    | expression op='IS' expression                      # isRelationalExpression
    | expression op='ISNT' expression               # isntRelationalExpression
    | expression op='AND' expression                        # AndExpression
    | expression op='OR' expression                         # OrExpression
    | expression op='EXOR' expression                       # ExorExpression
    | expression unaryLiteralExpression                     # unarySignedLiteralExpression
    ;

refCharSizeofAttribute
    : ( 'MAX' | 'LENGTH')
    ;
////////////////////////////////////////////////////////////////////////////////

unaryLiteralExpression
    : (numericLiteralPositive | numericLiteralNegative) ( op=('*'|'/') unaryExpression)?
    ;

////////////////////////////////////////////////////////////////////////////////

unaryExpression
    : op=('+'|'-')? primaryExpression
    ;

////////////////////////////////////////////////////////////////////////////////


// obsolete? (2020-02-26 (rm)
//expressionList:
//    expression (',' expression)*
//    ;

////////////////////////////////////////////////////////////////////////////////

numericLiteral:
    numericLiteralUnsigned | numericLiteralPositive | numericLiteralNegative
    ;

////////////////////////////////////////////////////////////////////////////////

numericLiteralUnsigned:
    IntegerConstant
    ;

////////////////////////////////////////////////////////////////////////////////

numericLiteralPositive:
    IntegerConstant
    ;

////////////////////////////////////////////////////////////////////////////////

numericLiteralNegative:
    '-' IntegerConstant
    ;

////////////////////////////////////////////////////////////////////////////////
// Name ::=
//    Identifier [ ( Index [ , Index ] ... ) ] [ . Name ]
////////////////////////////////////////////////////////////////////////////////

name:
    ID ( '(' listOfExpression ')' )? ( '.' name )?
    ;

listOfExpression:
    expression ( ',' expression )*
    ;

////////////////////////////////////////////////////////////////////////////////
// Index ::=
//  Expression§WithIntegerAsValue
////////////////////////////////////////////////////////////////////////////////

index:
    expression
    ;

////////////////////////////////////////////////////////////////////////////////

primaryExpression:
    '(' expression ')'
    | name
    | constant
    | semaTry
    | stringSelection
    ;

////////////////////////////////////////////////////////////////////////////////
// ConstantExpression ::=
//   { + | - } FloatingPointNumber
//   | { + | - } DurationConstant
//   | ConstantFIXEDExpression
////////////////////////////////////////////////////////////////////////////////

constantExpression:
    floatingPointConstant
    | sign? durationConstant
    | constantFixedExpression
    ;

////////////////////////////////////////////////////////////////////////////////
// ConstantFIXEDExpression ::=
//   Term [ { + | - } Term ] ...
////////////////////////////////////////////////////////////////////////////////

constantFixedExpression:
    constantFixedExpressionTerm ( additiveConstantFixedExpressionTerm | subtractiveConstantFixedExpressionTerm) *
    ;

////////////////////////////////////////////////////////////////////////////////

additiveConstantFixedExpressionTerm
    : op='+' constantFixedExpressionTerm
    ;

////////////////////////////////////////////////////////////////////////////////

subtractiveConstantFixedExpressionTerm
    : op='-' constantFixedExpressionTerm
    ;

////////////////////////////////////////////////////////////////////////////////
// Term ::=
//   Factor [ {∗ | // | REM } Factor ] ...
////////////////////////////////////////////////////////////////////////////////

constantFixedExpressionTerm
    : constantFixedExpressionFactor ( multiplicationConstantFixedExpressionTerm | divisionConstantFixedExpressionTerm | remainderConstantFixedExpressionTerm)*
    ;

////////////////////////////////////////////////////////////////////////////////

multiplicationConstantFixedExpressionTerm
    : op='*' constantFixedExpressionFactor
    ;

////////////////////////////////////////////////////////////////////////////////

divisionConstantFixedExpressionTerm
    : op='//' constantFixedExpressionFactor
    ;

////////////////////////////////////////////////////////////////////////////////

remainderConstantFixedExpressionTerm
    : op='REM' constantFixedExpressionFactor
    ;

////////////////////////////////////////////////////////////////////////////////
// Factor ::=
//   [+ | -] {  Integer
//             | ( ConstantFIXEDExpression )
//             | TOFIXED { CharacterStringConstant§OfLength1 | BitStringConstant }
//             | Identifier§NamedFIXEDConstant
//           }
// [ FIT ConstantFIXEDExpression ]
////////////////////////////////////////////////////////////////////////////////

sign
    : '+'                       #signPlus
    | '-'                       #signMinus
    ;

constantFixedExpressionFactor
    : sign? (   fixedConstant
              | '(' constantFixedExpression ')'
              | ID )
      constantFixedExpressionFit?
    ;

////////////////////////////////////////////////////////////////////////////////

constantFixedExpressionFit
    : <assoc=right>  'FIT' constantFixedExpression
    ;

////////////////////////////////////////////////////////////////////////////////
// ConvertStatement ::=
//   ConvertToStatement | ConvertFromStatement
////////////////////////////////////////////////////////////////////////////////

convertStatement
    : convertToStatement
    | convertFromStatement
    ;

////////////////////////////////////////////////////////////////////////////////
// ConvertToStatement ::=
//   CONVERT Expression [ , Expression ] ... TO Name§CharacterStringVariable
//   [ BY FormatOrPositionConvert [ , FormatOrPositionConvert ] ... ] ;
////////////////////////////////////////////////////////////////////////////////

convertToStatement:
    'CONVERT' ioDataList? 'TO' name ( 'BY' listOfFormatPositions )? ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// ConvertFromStatement ::=
//   CONVERT Name§Variable [ , Name§Variable ] ... FROM Expression§CharacterString
//   [ BY FormatOrPositionConvert [ , FormatOrPositionConvert ] ... ] ;
////////////////////////////////////////////////////////////////////////////////
convertFromStatement:
	//'CONVERT' ioDataList? 'FROM' name ( 'BY' listOfFormatPositions )? ';'
	'CONVERT' ioDataList? 'FROM' expression ( 'BY' listOfFormatPositions )? ';'
	;


////////////////////////////////////////////////////////////////////////////////
// ListFormat ::=
//   LIST
////////////////////////////////////////////////////////////////////////////////

listFormat
    : 'LIST'
    ;

////////////////////////////////////////////////////////////////////////////////
// RFormat ::=
//   R ( Identifier§Format )
////////////////////////////////////////////////////////////////////////////////

rFormat
    : 'R' '(' ID ')'
    ;

////////////////////////////////////////////////////////////////////////////////
  
referenceConstant:
	'NIL'
	;    

////////////////////////////////////////////////////////////////////////////////

fixedConstant
    : IntegerConstant  ( '(' fixedNumberPrecision ')' )?
    ;

////////////////////////////////////////////////////////////////////////////////

IntegerConstant
	:	DecimalIntegerConstant
	;

////////////////////////////////////////////////////////////////////////////////

fixedNumberPrecision
    : IntegerConstant
    ;

////////////////////////////////////////////////////////////////////////////////

StringLiteral
	:	'\'' StringCharacters? '\''
	;

fragment
StringCharacters
	:	(StringCharacter | EscapeSequence)+ 
	;

fragment
StringCharacter
	:	~['\r\n]
	| 	'\'\''
	;

fragment
EscapeSequence
	: '\'\\' (HexEscape | ' ' | [\r\n\t])* '\\\''
	;

fragment
HexEscape
	:  B4Digit B4Digit
	;

////////////////////////////////////////////////////////////////////////////////

CppStringLiteral
    : '"' CppSCharSequence? '"'
    ;

////////////////////////////////////////////////////////////////////////////////

fragment
CppSCharSequence
    :   CppSChar+
    ;

////////////////////////////////////////////////////////////////////////////////

fragment
CppSChar
    :   ~["\\\r\n]
    |   CppEscapeSequence
    |   '\\\n'   // Added line
    |   '\\\r\n' // Added line
    ;

////////////////////////////////////////////////////////////////////////////////

fragment
CppEscapeSequence
    :   CppSimpleEscapeSequence
    |   OctalEscapeSequence
    |   HexadecimalEscapeSequence
    ;

////////////////////////////////////////////////////////////////////////////////

fragment
CppSimpleEscapeSequence
    :   '\\' ['"?abfnrtv\\]
    ;

////////////////////////////////////////////////////////////////////////////////

fragment
OctalEscapeSequence
    :   '\\' OctalDigit
    |   '\\' OctalDigit OctalDigit
    |   '\\' OctalDigit OctalDigit OctalDigit
    ;

////////////////////////////////////////////////////////////////////////////////

fragment
HexadecimalEscapeSequence
    :   '\\x' HexadecimalDigit+
    ;

////////////////////////////////////////////////////////////////////////////////

fragment
OctalDigit
    :   [0-7]
    ;

////////////////////////////////////////////////////////////////////////////////

fragment
HexadecimalDigit
    :   [0-9a-fA-F]
    ;

////////////////////////////////////////////////////////////////////////////////

BitStringLiteral
	: '\'' B1Digit+ '\'' ( 'B' | 'B1' )
    | '\'' B2Digit+ '\'' 'B2'
    | '\'' B3Digit+ '\'' 'B3'
    | '\'' B4Digit+ '\'' 'B4'
	;

////////////////////////////////////////////////////////////////////////////////

fragment
DecimalIntegerConstant
	:	DecimalNumeral
	;

////////////////////////////////////////////////////////////////////////////////

fragment
DecimalNumeral
	:	Digits+
	;

////////////////////////////////////////////////////////////////////////////////

fragment
Digits
	:	Digit+
	;

////////////////////////////////////////////////////////////////////////////////

fragment
Digit
	:	'0'
	|   NonZeroDigit
	;

////////////////////////////////////////////////////////////////////////////////

fragment
NonZeroDigit
	:	[1-9]
	;

////////////////////////////////////////////////////////////////////////////////

fragment
Letter : [a-zA-Z] ;

////////////////////////////////////////////////////////////////////////////////
//  Constant ::= Integer
//      | FloatingPointNumber | BitStringConstant
//      | TimeConstant
//      | DurationConstant
//      | NIL
////////////////////////////////////////////////////////////////////////////////

constant:
      sign? ( fixedConstant | floatingPointConstant )
    | timeConstant
    | sign? durationConstant
    | bitStringConstant
    | stringConstant
    | referenceConstant
    ;

////////////////////////////////////////////////////////////////////////////////

stringConstant:
    StringLiteral
    ;

////////////////////////////////////////////////////////////////////////////////

bitStringConstant:
    BitStringLiteral
    ;

////////////////////////////////////////////////////////////////////////////////

fragment
B1Digit
    : '0' | '1'
    ;

////////////////////////////////////////////////////////////////////////////////

fragment
B2Digit
    : '0' | '1' | '2' | '3'
    ;

////////////////////////////////////////////////////////////////////////////////

fragment
B3Digit
    : '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7'
    ;

////////////////////////////////////////////////////////////////////////////////

fragment
B4Digit
    : '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | 'A' | 'B' | 'C' | 'D' | 'E' | 'F'
    ;

////////////////////////////////////////////////////////////////////////////////
// TimeConstant ::=
//   Digit ... : Digit ... : Digit ... [ . Digit ... ]
//     : timeHour ':' timeMinute ':' timeSeconds
// Digit+ ('.' Digit+)?
// ':'  IntegerConstant ':' FloatingPointNumberWithoutPrecisionAndExponent
////////////////////////////////////////////////////////////////////////////////

timeConstant:
    IntegerConstant ':' IntegerConstant ':' ( IntegerConstant | floatingPointConstant )
    ;

////////////////////////////////////////////////////////////////////////////////

durationConstant:
      hours  minutes? seconds?
    | minutes seconds?
    | seconds
    ;

////////////////////////////////////////////////////////////////////////////////

hours:
    IntegerConstant 'HRS'
    ;

////////////////////////////////////////////////////////////////////////////////

minutes:
    IntegerConstant 'MIN'
    ;

////////////////////////////////////////////////////////////////////////////////

seconds:
    ( IntegerConstant | floatingPointConstant ) 'SEC'
    ;

////////////////////////////////////////////////////////////////////////////////

floatingPointConstant:
    FloatingPointNumber
    ;

////////////////////////////////////////////////////////////////////////////////

FloatingPointNumber:
    ( Digit+ '.' ( Digit+)? | '.' Digit+ ) Exponent? FloatingPointNumberPrecision?
    | Digit+ Exponent FloatingPointNumberPrecision?
    ;

////////////////////////////////////////////////////////////////////////////////

fragment
FloatingPointNumberPrecision:
    '(' IntegerConstant ')'
    ;

////////////////////////////////////////////////////////////////////////////////
// Exponent ::=
//      E [ + | - ] Digit ...
////////////////////////////////////////////////////////////////////////////////

fragment
Exponent
    : 'E' ( '+' | '-' )? Digit+
    ;

////////////////////////////////////////////////////////////////////////////////

FloatingPointNumberWithoutPrecisionAndExponent
    : Digit+ '.' ( Digit+)?
    ;

////////////////////////////////////////////////////////////////////////////////

cpp_inline
    : ( '__cpp__' | '__cpp' )  '(' CppStringLiteral+ ')' ';'
    ;

////////////////////////////////////////////////////////////////////////////////
// LengthDefinition::=
//      LENGTH { { FIXED | FLOAT } (precision)
//               | { BIT | CHARACTER | CHAR } (length) };
//
// Example:
//      PROBLEM;
//          LENGTH FIXED(15);
//          LENGTH FLOAT(53);
//
//          DCL A FIXED,        /* A is of type FIXED(15) */
//              X FLOAT,        /* X is of type FLOAT(53) */
//              Y FLOAT(23);    /* Y is of type FLOAT(23) */
//
////////////////////////////////////////////////////////////////////////////////

lengthDefinition
    : 'LENGTH' lengthDefinitionType '(' length ')' ';'
    ;

////////////////////////////////////////////////////////////////////////////////

lengthDefinitionType
    : 'FIXED'                          #lengthDefinitionFixedType
    | 'FLOAT'                          #lengthDefinitionFloatType
    | 'BIT'                            #lengthDefinitionBitType
    | ( 'CHARACTER' | 'CHAR' )         #lengthDefinitionCharacterType
    ;
////////////////////////////////////////////////////////////////////////////////

 precision
    : IntegerConstant
    ;

////////////////////////////////////////////////////////////////////////////////

 length
     : IntegerConstant
     ;

////////////////////////////////////////////////////////////////////////////////

//
// Whitespace and comments
//

////////////////////////////////////////////////////////////////////////////////

BlockComment
    :   '/*' .*? '*/'
        -> channel(HIDDEN)
    ;

////////////////////////////////////////////////////////////////////////////////

LineComment
    :   '!' ~[\r\n]*
        -> channel(HIDDEN)
    ;

////////////////////////////////////////////////////////////////////////////////

Whitespace
    :   [ \t]+
        -> channel(HIDDEN)
    ;

////////////////////////////////////////////////////////////////////////////////

Newline
    :   (   '\r' '\n'?
        |   '\n'
        )
        -> channel(HIDDEN)
    ;

////////////////////////////////////////////////////////////////////////////////

STRING: '"' (~'"')* '"'
    ;

////////////////////////////////////////////////////////////////////////////////

// Handle Preprocessor Line Markings:
// # linenum filename flags
PP:
    '#'  ~('\n'|'\r')* '\r'? '\n'
    {
        Compiler.m_sourcelocations.process(getLine(),getText());
    } -> channel(HIDDEN);

////////////////////////////////////////////////////////////////////////////////

// handle characters which failed to match any other token
ErrorCharacter :
    .
    ;


////////////////////////////////////////////////////////////////////////////////
////////////////////////////// END OF FILE /////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

