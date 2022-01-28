/*
TFU  * [A "BSD license"]
 *  Copyright (c) 2019 Raier Müller
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.openpearl.compiler.SemanticAnalysis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.openpearl.compiler.*;
import org.openpearl.compiler.Compiler;
import org.openpearl.compiler.OpenPearlParser.IoListElementContext;
import org.openpearl.compiler.Exception.*;
import org.openpearl.compiler.SymbolTable.ModuleEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SymbolTable.SymbolTableEntry;
import org.openpearl.compiler.SymbolTable.VariableEntry;



/**
check types the io-statatements
with respect to the dation declaration
and constant values of format statements

Errors are forwarded to the ErrorStack class

 */

public class CheckIOStatements extends OpenPearlBaseVisitor<Void>
implements OpenPearlVisitor<Void> {


    private int m_verbose;
    private boolean m_debug;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private AST m_ast = null;
    private boolean m_formatListAborted = false;
    private boolean m_directionInput;
    private TypeDation m_typeDation;
    private boolean abortCheck; // flag to abort a check after a severe error was detected
    
    public CheckIOStatements(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            AST ast) {

        m_debug = debug;
        m_verbose = verbose;
        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;

        if (m_verbose > 0) {
            System.out.println("    Check IOFormats");
        }


    }

    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitModule");
        }

        org.openpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;
        visitChildren(ctx);


        // get all dation entries from the symbol table
        LinkedList<VariableEntry> entries = m_currentSymbolTable.getDationSpcAndDcl();
        for (int i=0; i<entries.size(); i++) {
            VariableEntry ve = entries.get(i); 
            TypeDation td = (TypeDation)(ve.getType());
            // System.out.println(ve.toString(1));
            if (ve.isSpecified() ) {
                if (td.isSystemDation()) {
                    checkSystemDationSpecification(ve,td);
                } else {
                    checkUserDation(ve,td,false);
                }
            } else {   
                checkUserDation(ve,td,true);
            }

        }
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    private void checkSystemDationSpecification(VariableEntry ve, TypeDation d) {
        //  System.out.println("SysSPC: "+ve.getName());
        //        TypeDation d = (TypeDation) (((VariableEntry) se).getType());
        ErrorStack.enter(ve.getCtx());
        if (d.hasTypology() == true) {
            ErrorStack.add("DIM not expeced on system dation");
        }
        if (d.hasTfu()) {
            ErrorStack.add("TFU not expected on system dation");
        }
        ErrorStack.leave();
    }

    private void checkUserDation(VariableEntry ve, TypeDation d, boolean isDecl) {
        ErrorStack.enter(ve.getCtx(),"DationDCL");
        if (isDecl) {
            // System.out.println("UserDCL: "+ve.getName());
            if (d.isSystemDation()) {
                ErrorStack.add("SYSTEM dations may not be declared");
                return;
            }


        } else {
            //            System.out.println("UserSPC: "+ve.getName());
        }
        // userdation must be 
        // of type ALPHIC                      -> DationPG
        // or type BASIC + typeOfTransmission  -> DationTS
        // or type of       typeOfTransmission -> DationRW
        // this is all enforced by the grammar



        // dimension settings must be >0 or '*' if given
        // only the last dimension may be '*' - the not 0 check is not in the grammar
        switch (d.getNumberOfDimensions()) {
            case 3:
                if (d.getDimension3() <= 0 || d.getDimension2() <= 0) {
                    // '*' not allowed
                    ErrorStack.add("only first dimension may be '*'");
                }
                break;
            case 2:
                if (d.getDimension2() == 0) {
                    // '*' not allowed
                    ErrorStack.add("only first dimension may be '*'");
                }
                break;
            case 1:
                break;
        }


        if (d.hasTfu() && d.getNumberOfDimensions() == 1 && d.getDimension1() == 0) {
            ErrorStack.add("TFU requires limited record length");
        }

        if (isDecl) {
            // SymbolTableEntry sys = this.m_currentSymbolTable.lookup(d.getCreatedOn().getName());;
            SymbolTableEntry sys = this.m_currentSymbolTable.lookup(d.getCreatedOnAsString());;
            if (sys == null) {

                ErrorStack.add("system dation '" + d.getCreatedOnAsString() + "' is not defined");
                ErrorStack.leave();
                return ;
            } else if ((!(sys instanceof VariableEntry))
                    || (!(((VariableEntry) sys).getType() instanceof TypeDation))) {
                ErrorStack.add("'" + d.getCreatedOnAsString() + "' is not of type DATION");
                ErrorStack.leave();
                return ;
            } else {
                TypeDation td = ((TypeDation)((VariableEntry)sys).getType());
                if (!td.isSystemDation() ) {
                    ErrorStack.add("'" + d.getCreatedOnAsString() + "' is not of type SYSTEM DATION");
                    ErrorStack.leave();
                    return ;
                }
                
                d.setCreatedOn((VariableEntry)sys);
                TypeDation sd = (TypeDation) (((VariableEntry) sys).getType());


                // (4) ClassAttribute must fit
                // check compatibility
                // use system dation as primary selector
                if (sd.isBasic()) {
                    if (d.isAlphic()) {
                        ErrorStack.add("attempt to create ALPHIC dation upon BASIC system dation");
                        ErrorStack.leave();
                        return ;
                    }
                    if (!d.isBasic()) {
                        // d is not of type ALPHIC and not type BASIC --> check typeOfTransmission for
                        // correct error message
                        if (d.getTypeOfTransmission() != null) {
                            ErrorStack.add("attempt to create a '" + d.getTypeOfTransmission()
                            + "' dation upon a BASIC system dation");
                        } else {
                            ErrorStack.add(
                                    "attempt to create an ALPHIC dation upon a BASIC system dation");
                        }
                        ErrorStack.leave();
                        return ;
                    }

                }
                if (sd.isAlphic()) {
                    if (!d.isAlphic()) {
                        if (d.isBasic()) {
                            ErrorStack.add(
                                    "attempt to create a BASIC dation upon an ALPHIC system dation");
                            ErrorStack.leave();
                            return ;
                        }
                        if (d.getTypeOfTransmission() != null) {
                            ErrorStack.add("attempt to create a '" + d.getTypeOfTransmission()
                            + "' dation upon an ALPHIC system dation");
                            ErrorStack.leave();
                            return ;
                        }
                    }
                }
                if (sd.getTypeOfTransmission() != null && !sd.isBasic()) {
                    if (d.isAlphic()) {
                        if (!sd.getTypeOfTransmission().equals("ALL")) {
                            ErrorStack.add("attempt to create an ALPHIC dation upon '"
                                    + d.getTypeOfTransmission() + "' system dation");
                            ErrorStack.leave();
                            return ;
                        }
                    }
                    if (d.isBasic()) {
                        ErrorStack.add("attempt to create a BASIC dation upon non BASIC system dation");
                        ErrorStack.leave();
                        return ;
                    }
                    if (d.getTypeOfTransmission() != null) {
                        if (!sd.getTypeOfTransmission().equals("ALL")) {
                            // types must be equal
                            if (!sd.getTypeOfTransmissionAsType()
                                    .equals(d.getTypeOfTransmissionAsType())) {
                                ErrorStack.add("attempt to create a '" + d.getTypeOfTransmission()
                                + "' dation upon a '" + sd.getTypeOfTransmission()
                                + "' system dation");

                            }
                        }
                    }
                }

                // (2) direction must fit (sourceSinkAttribute)
                if (d.isIn() && !sd.isIn()) {
                    ErrorStack.add("system dation does not provide direction IN");
                }
                if (d.isOut() && !sd.isOut()) {
                    ErrorStack.add("system dation does not provide direction OUT");
                }



                // (5) Typology
                //    Userdation of type ALPHIC or type need 
                //        * typology
                //        * FORWARD/DIRECT
                //        - STREAM/NOSTREAM/CYCLIC/NOCLYCIC without contradictions
                if (!d.isBasic() && !d.hasTypology()) {
                    ErrorStack.add("non BASIC user dation needs typology (DIM)");
                }
            }

            if (d.hasTfu() && d.isStream()) {
                ErrorStack.add("TFU requires NOSTREAM");
            }


            // (x) if the system dation specifies a TFU-size;
            //     the user dation must do this also
            //     this is checked by the IMC, since we do not know anything about
            //     details of the system dation

        }
        ErrorStack.leave();
    }

    @Override
    public Void visitProcedureDeclaration(OpenPearlParser.ProcedureDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitProcedureDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitVariableDeclaration(OpenPearlParser.VariableDeclarationContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitSpecification(OpenPearlParser.SpecificationContext ctx) {
        visitChildren(ctx);
        return null;
    }
    @Override
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitTaskDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(OpenPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitBlock_statement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(OpenPearlParser.LoopStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitLoopStatement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    /* ------------------------------------------------ */
    /* start of check specific code */
    /* ------------------------------------------------ */


    @Override
    public Void visitOpen_statement(OpenPearlParser.Open_statementContext ctx) {
        ErrorStack.enter(ctx, "OPEN");
        boolean hasRST = false;
        boolean hasIDF = false;
        boolean hasOLD = false;
        boolean hasNEW = false;
        boolean hasANY = false;
        boolean hasCAN = false;
        boolean hasPRM = false;

        visitDationName(ctx.dationName());

        //	lookupDation(ctx.dationName().name().getText().toString());

        if (ctx.open_parameterlist() != null && ctx.open_parameterlist().open_parameter() != null) {

            for (int i = 0; i < ctx.open_parameterlist().open_parameter().size(); i++) {
                //System.out.println("openParam: "+ ctx.open_parameterlist().open_parameter(i).getText());
                if (ctx.open_parameterlist().open_parameter(i).open_parameter_idf() != null) {
                    if (hasIDF)
                        ErrorStack.add("multiple IDF attributes");
                    hasIDF = true;
                    // let's check the type of the IDF-variable. It must be of type CHAR
                    if (ctx.open_parameterlist().open_parameter(i).open_parameter_idf()
                            .ID() != null) {
                        String name = ctx.open_parameterlist().open_parameter(i)
                                .open_parameter_idf().ID().toString();
                        SymbolTableEntry se = m_currentSymbolTable.lookup(name);
                        if (se == null) {
                            ErrorStack.add("'" + name + "' is not defined");
                        } else if ((se instanceof VariableEntry)
                                && !((((VariableEntry) se).getType() instanceof TypeChar))) {
                            ErrorStack.add("'" + name + "' must be of type CHAR -- has type "
                                    + (((VariableEntry) se).getType().toString()));
                        }
                    }
                    /*
                     it's ok for the moment
                     if there is a StringLiteral, this should be in the ConstantPool 
                     if not, it must be a character variable
                     both information should be places as ASTAttributes
                     */
                }
                if (ctx.open_parameterlist().open_parameter(i).openClosePositionRST() != null) {
                    OpenPearlParser.OpenClosePositionRSTContext c = (OpenPearlParser.OpenClosePositionRSTContext) (ctx
                            .open_parameterlist().open_parameter(i).openClosePositionRST());
                    if (hasRST)
                        ErrorStack.warn("multiple RST attributes");

                    //ASTAttribute attr = m_ast.lookup(c);
                    //ctx.open_parameterlist().open_parameter(i).openClosePositionRST());
                    checkPrecision(c);
                    //ctx.open_parameterlist().open_parameter(i).openClosePositionRST());
                    hasRST = true;
                }

                if (ctx.open_parameterlist().open_parameter(i)
                        .open_parameter_old_new_any() != null) {
                    OpenPearlParser.Open_parameter_old_new_anyContext c = (OpenPearlParser.Open_parameter_old_new_anyContext) (ctx
                            .open_parameterlist().open_parameter(i).open_parameter_old_new_any());

                    if (c.getText().equals("OLD")) {
                        if (hasOLD)
                            ErrorStack.warn("multiple OLD attributes");
                        hasOLD = true;
                    }


                    if (c.getText().equals("NEW")) {
                        if (hasNEW)
                            ErrorStack.warn("multiple NEW attributes");
                        hasNEW = true;
                    }

                    if (c.getText().equals("ANY")) {
                        if (hasANY)
                            ErrorStack.warn("multiple ANY attributes");
                        hasANY = true;
                    }
                }

                if (ctx.open_parameterlist().open_parameter(i)
                        .open_close_parameter_can_prm() != null) {
                    OpenPearlParser.Open_close_parameter_can_prmContext c =
                            (OpenPearlParser.Open_close_parameter_can_prmContext) (ctx.open_parameterlist()
                                    .open_parameter(i).open_close_parameter_can_prm());
                    if (c.getText().equals("CAN")) {
                        if (hasCAN)
                            ErrorStack.add("multiple CAN attributes");
                        hasCAN = true;
                    }


                    if (c.getText().equals("PRM")) {
                        if (hasPRM)
                            ErrorStack.add("multiple PRM attributes");
                        hasPRM = true;
                    }
                }


            }
            if (hasCAN && hasPRM) {
                ErrorStack.add("ether CAN or PRM allowed");
            }

            int nbrOfPreviousStati = 0;
            if (hasOLD)
                nbrOfPreviousStati++;
            if (hasNEW)
                nbrOfPreviousStati++;
            if (hasANY)
                nbrOfPreviousStati++;
            if (nbrOfPreviousStati > 1)
                ErrorStack.add("only one of OLD/NEW/ANY allowed");
        }

        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitClose_statement(OpenPearlParser.Close_statementContext ctx) {

        ErrorStack.enter(ctx, "CLOSE");

        boolean hasRST = false;
        boolean hasCAN = false;
        boolean hasPRM = false;

        visitDationName(ctx.dationName());

        //lookupDation(ctx.dationName().name().toString());
        if (ctx.close_parameterlist() != null
                && ctx.close_parameterlist().close_parameter() != null) {
            for (int i = 0; i < ctx.close_parameterlist().close_parameter().size(); i++) {

                if (ctx.close_parameterlist().close_parameter(i).openClosePositionRST() != null) {
                    checkPrecision(
                            ctx.close_parameterlist().close_parameter(i).openClosePositionRST());
                    if (hasRST)
                        ErrorStack.warn("multiple RST attributes");
                    hasRST = true;
                }


                if (ctx.close_parameterlist().close_parameter(i)
                        .open_close_parameter_can_prm() != null) {
                    OpenPearlParser.Open_close_parameter_can_prmContext c =
                            (OpenPearlParser.Open_close_parameter_can_prmContext) (ctx.close_parameterlist()
                                    .close_parameter(i).open_close_parameter_can_prm());
                    if (c.getText().equals("CAN")) {
                        if (hasCAN)
                            ErrorStack.add("multiple CAN attributes");
                        hasCAN = true;
                    }


                    if (c.getText().equals("PRM")) {
                        if (hasPRM)
                            ErrorStack.add("multiple PRM attributes");
                        hasPRM = true;
                    }
                }


            }
            if (hasCAN && hasPRM) {
                ErrorStack.add("ether CAN or PRM allowed");
            }
        }

        ErrorStack.leave();
        return null;
    }



    @Override
    public Void visitDationName(OpenPearlParser.DationNameContext ctx) {
        ASTAttribute attr = m_ast.lookup(ctx.name());
        TypeDefinition t = attr.getType();
        TypeDation td = null;
        
        t = TypeUtilities.performImplicitDereferenceAndFunctioncall(attr);
      
        if (t instanceof TypeDation) {
            td = (TypeDation) t;
            if (td.isSystemDation()) {
                ErrorStack.add("not allowed on SYSTEM dation");
            }
        } else {
            ErrorStack.add(ctx, null, "userdation required -- got (" + attr.getType() + ")");
        }
        m_typeDation = td;

        return null;
    }


    @Override
    public Void visitPutStatement(OpenPearlParser.PutStatementContext ctx) {

        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitPositionPUT");
        }

        abortCheck = false;

        ErrorStack.enter(ctx, "PUT");
        m_directionInput = false;

        visit(ctx.dationName());
        if (m_typeDation != null) {
            // enshure that the dation is of type ALPHIC
            if (!m_typeDation.isAlphic()) {
                ErrorStack.enter(ctx.dationName());
                ErrorStack.add("need ALPHIC dation");
                ErrorStack.leave();
            }
            
            checkPutGetDataFormat(ctx.ioDataList(), ctx.listOfFormatPositions());
            checkArraySliceForTakeSendConvert(ctx.ioDataList(),false);
        }
        ErrorStack.leave();
        return null;
    }

    /**
     * check if the ID-list is not INV
     * check type of dation
     * no positioning after last format element in position/format list
     */
    @Override
    public Void visitGetStatement(OpenPearlParser.GetStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitPositionGET");
        }
        abortCheck = false;
        ErrorStack.enter(ctx, "GET");
        m_directionInput = true;

        visit(ctx.dationName());
        if (m_typeDation != null) {
            // enshure that the dation is of type ALPHIC
            if (!m_typeDation.isAlphic()) {
                ErrorStack.enter(ctx.dationName());
                ErrorStack.add("need ALPHIC dation");
                ErrorStack.leave();
            }

            enshureDataForInput(ctx.ioDataList());

            checkPutGetDataFormat(ctx.ioDataList(), ctx.listOfFormatPositions());
            checkArraySliceForTakeSendConvert(ctx.ioDataList(),false);

            visitChildren(ctx);
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitConvertToStatement(OpenPearlParser.ConvertToStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitConvertTo");
        }

        abortCheck = false;

        // create dummy Type Dation for reuse of checkPutGetDataFormat
        m_typeDation = new TypeDation();
        m_typeDation.setTypeOfTransmission("ALPHIC");
        m_typeDation.setAlphic(true);
        m_typeDation.setTfu(false);

        ErrorStack.enter(ctx, "CONVERT TO");
        m_directionInput = false;

        visitChildren(ctx);

        checkPutGetDataFormat(ctx.ioDataList(), ctx.listOfFormatPositions());
        checkArraySliceForTakeSendConvert(ctx.ioDataList(),true);
        if (ctx.ioDataList() == null || ctx.ioDataList().ioListElement().size() < 1) {
            ErrorStack.add("need at least one expression");
        }

        ASTAttribute attr = m_ast.lookup(ctx.name());
        if (attr == null) {
            ErrorStack.addInternal("no ASTAttribute in CONVERT/TO destination");
        } else {
            if (!(attr.m_type instanceof TypeChar)) {
                ErrorStack.add("destination must be of type CHAR");
            }

            if (attr.isConstant()) {
                ErrorStack.add("destination is not writable");
            }
        }


        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitConvertFromStatement(OpenPearlParser.ConvertFromStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitConvertFrom");
        }

        abortCheck = false;
        ErrorStack.enter(ctx, "CONVERT FROM");



        ASTAttribute attr = m_ast.lookup(ctx.expression());
        if (!(attr.m_type instanceof TypeChar)) {
            ErrorStack.add("source must be of type CHAR");
        }

        // create dummy Type Dation for reuse of checkPutGetDataFormat
        m_typeDation = new TypeDation();
        m_typeDation.setTypeOfTransmission("ALPHIC");
        m_typeDation.setAlphic(true);
        m_typeDation.setTfu(false);


        m_directionInput = true;

        visitChildren(ctx);
        enshureDataForInput(ctx.ioDataList());

        checkPutGetDataFormat(ctx.ioDataList(), ctx.listOfFormatPositions());
        checkArraySliceForTakeSendConvert(ctx.ioDataList(),true);
        if (ctx.ioDataList() == null || ctx.ioDataList().ioListElement().size() < 1) {
            ErrorStack.add("need at least one expression");
        }

        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitReadStatement(OpenPearlParser.ReadStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitPositionREAD");
        }

        // enshure that the dation id of type 'type'
        ErrorStack.enter(ctx, "READ");
        m_directionInput = true;

        visit(ctx.dationName());
        if (m_typeDation != null) {
            if (m_typeDation.isAlphic() || m_typeDation.isBasic()) {
                ErrorStack.enter(ctx.dationName());
                ErrorStack.add("need 'type' dation");
                ErrorStack.leave();
            }

            enshureDataForInput(ctx.ioDataList());

            checkWriteReadFormat(ctx.listOfFormatPositions());
            checkReadWriteTakeSendDataTypes(ctx.ioDataList());
            checkArraySliceForTakeSendConvert(ctx.ioDataList(),false);
            

            visitChildren(ctx);
        }
        ErrorStack.leave();
        return null;
    }



    @Override
    public Void visitWriteStatement(OpenPearlParser.WriteStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitPositionWRITE");
        }

        // enshure that the dation id of type 'type'
        ErrorStack.enter(ctx, "WRITE");
        m_directionInput = false;

        visit(ctx.dationName());

        if (m_typeDation != null) {
            if (m_typeDation.isAlphic() || m_typeDation.isBasic()) {
                ErrorStack.enter(ctx.dationName());
                ErrorStack.add("need 'type' dation");
                ErrorStack.leave();
            }

            // check if absolute positions follow relative positions
            checkWriteReadFormat(ctx.listOfFormatPositions());
            checkReadWriteTakeSendDataTypes(ctx.ioDataList());
            checkArraySliceForTakeSendConvert(ctx.ioDataList(),false);

            visitChildren(ctx);
        }
        ErrorStack.leave();
        return null;
    }

    private void checkReadWriteTakeSendDataTypes(OpenPearlParser.IoDataListContext ioDataList) {
        if (m_typeDation.getTypeOfTransmissionAsType() == null) {
            // this is a type 'ALL' dation --> we are ready here
            return;
        }
        if (ioDataList != null) {
            for (int i = 0; i < ioDataList.ioListElement().size(); i++) {
                if (ioDataList.ioListElement(i).expression() != null) {
                    ASTAttribute attr = m_ast.lookup(ioDataList.ioListElement(i).expression());
                    if (attr != null) {
                        boolean typeMismatch = false;
                        if (m_directionInput && (!attr.getType().equals(m_typeDation.getTypeOfTransmissionAsType()))) {
                            typeMismatch=true;
                        }
                        TypeDefinition td = attr.getType();
                        if (!m_directionInput) {
                            // maybe we need implicit dereference
                            if (td instanceof TypeReference) {
                                td = TypeUtilities.performImplicitDereferenceAndFunctioncall(attr);
                            }
                            if ((!TypeUtilities.simpleTypeMayBeAssignedTo(m_typeDation.getTypeOfTransmissionAsType(),td))) {
                                typeMismatch=true;
                            }
                        }
                        
                        if (typeMismatch) {
                            ErrorStack.enter(ioDataList.ioListElement(i).expression());
                            ErrorStack.add("type mismatch: required: "
                                    + m_typeDation.getTypeOfTransmission() + " --- got "
                                    + attr.getType().toString());
                            ErrorStack.leave();
                        }
                    }
                }
            }
        }

    }


    
    private void checkArraySliceForTakeSendConvert(OpenPearlParser.IoDataListContext ioDataList, boolean forbiddenInPearl90) {
        if (ioDataList != null) {
            for (int i = 0; i < ioDataList.ioListElement().size(); i++) {
                if (ioDataList.ioListElement(i).arraySlice() != null) {
                    ASTAttribute attr = m_ast.lookup(ioDataList.ioListElement(i).arraySlice()); 
                    TypeArraySlice tas = (TypeArraySlice)(attr.getType());
                    TypeArray ta = (TypeArray)(tas.getBaseType());
                    ArrayDimension last = ta.getDimensions().get(ta.getNoOfDimensions()-1);

                    if (tas.getStartIndex() != null) {
                        long start = tas.getStartIndex().getValue();                        
                        if (start < last.getLowerBoundary() || start > last.getUpperBoundary()) {
                            int indexCount = ioDataList.ioListElement(i).arraySlice().startIndex().listOfExpression().expression().size();
                            ErrorStack.enter(ioDataList.ioListElement(i).arraySlice().startIndex().listOfExpression().expression(indexCount-1));
                            ErrorStack.add("array slice violates array boundary");
                            ErrorStack.leave();
                        }
                    }
                    if (tas.getEndIndex() != null) {
                        long end = tas.getEndIndex().getValue();                        
                        if (end < last.getLowerBoundary() || end > last.getUpperBoundary()) {
                            ErrorStack.enter(ioDataList.ioListElement(i).arraySlice().endIndex());
                            ErrorStack.add("array slice violates array boundary");
                            ErrorStack.leave();
                        }
                    }

                    if (Compiler.isStdPEARL90() && forbiddenInPearl90) {
                        ErrorStack.enter(ioDataList.ioListElement(i).arraySlice());
                        ErrorStack.warn("array slice not allowed in PEARL90");
                        ErrorStack.leave();
                    }
                }
            }
        }

    }
    
    // enshure that no formats and no absolute positions occur after relative positions
    /**
     * @param d
     * @param listOfFormatPositions
     */
    private void checkWriteReadFormat(OpenPearlParser.ListOfFormatPositionsContext listOfFormatPositions) {
        int nbrOfPositionsAlreadyFound = 0;

        if (listOfFormatPositions != null) {
            visitChildren(listOfFormatPositions);
        }

        if (listOfFormatPositions != null) {
            for (int i = 0; i < listOfFormatPositions.formatPosition().size(); i++) {
                if (listOfFormatPositions.formatPosition(i) instanceof OpenPearlParser.FactorFormatPositionContext
                        || listOfFormatPositions.formatPosition(i) instanceof OpenPearlParser.FactorFormatContext) {
                    ErrorStack.enter(listOfFormatPositions.formatPosition(i));
                    ErrorStack.add("only positions are allowed");
                    ErrorStack.leave();
                } else if (listOfFormatPositions
                        .formatPosition(i) instanceof OpenPearlParser.FactorPositionContext) {
                    OpenPearlParser.PositionContext pc =
                            ((OpenPearlParser.FactorPositionContext) (listOfFormatPositions.formatPosition(i)))
                            .position();

                    if (pc.absolutePosition() != null) {
                        OpenPearlParser.AbsolutePositionContext c = pc.absolutePosition();
                        if (c.positionPOS() != null || c.positionLINE() != null) {
                            ErrorStack.enter(pc);
                            if (nbrOfPositionsAlreadyFound > 0) {
                                ErrorStack.warn("previous positioning before LINE or POS are void");
                            }
                            ErrorStack.leave();
                        }
                    }

                    if (pc.openClosePositionRST() == null) {
                        nbrOfPositionsAlreadyFound++;
                    }
                }
            }
        }
    }


    @Override
    public Void visitTakeStatement(OpenPearlParser.TakeStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitTake");
        }

        ErrorStack.enter(ctx, "TAKE");
        m_directionInput = true;

        visit(ctx.dationName());
        if (m_typeDation != null) {
            if (!m_typeDation.isBasic()) {
                ErrorStack.enter(ctx.dationName());
                ErrorStack.add("need BASIC dation");
                ErrorStack.leave();
            }

            // check that only 1 time RST is allowed
            checkTakeSendFormat(ctx.listOfFormatPositions());

            if (ctx.ioDataList() == null) {
                ErrorStack.warn("nothing to transfer: statement ignored");
            } else if (ctx.ioDataList().ioListElement().size() > 1) {
                ErrorStack.add("only one expression allowed");
            }

            enshureDataForInput(ctx.ioDataList());
            checkReadWriteTakeSendDataTypes(ctx.ioDataList());
            checkArraySliceForTakeSendConvert(ctx.ioDataList(),true);

            visitChildren(ctx);
        }
        ErrorStack.leave();
        return null;
    }



    @Override
    public Void visitSendStatement(OpenPearlParser.SendStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitSend");
        }

        // enshure that the dation id of type BASIC
        ErrorStack.enter(ctx, "SEND");
        m_directionInput = false;

        visit(ctx.dationName());
        if (m_typeDation != null) {
            if (!m_typeDation.isBasic()) {
                ErrorStack.add("need BASIC dation");
            }
            // check that only 1 time RST is allowed
            checkTakeSendFormat(ctx.listOfFormatPositions());

            if (ctx.ioDataList() == null) {
                ErrorStack.warn("nothing to transfer: statement ignored");
            } else if (ctx.ioDataList().ioListElement().size() > 1) {
                ErrorStack.add("only one expression allowed");
            }

            checkReadWriteTakeSendDataTypes(ctx.ioDataList());
            checkArraySliceForTakeSendConvert(ctx.ioDataList(),true);

            visitChildren(ctx);
        }
        ErrorStack.leave();
        return null;
    }

    private void checkTakeSendFormat(OpenPearlParser.ListOfFormatPositionsContext listOfFormatPositions) {
        if (listOfFormatPositions != null) {
            for (int i = 0; i < listOfFormatPositions.formatPosition().size(); i++) {
                ErrorStack.enter(listOfFormatPositions.formatPosition(i));
                if (listOfFormatPositions.formatPosition(i) instanceof OpenPearlParser.FactorPositionContext) {
                    OpenPearlParser.FactorPositionContext fpc =
                            (OpenPearlParser.FactorPositionContext) (listOfFormatPositions.formatPosition(i));
                    if (fpc.factor() != null || fpc.position().openClosePositionRST() == null) {
                        ErrorStack.add("only RST allowed once");
                    }
                }
                ErrorStack.leave();
            }
        }

    }

    private void checkPutGetDataFormat(OpenPearlParser.IoDataListContext dataCtx,
                                       OpenPearlParser.ListOfFormatPositionsContext fmtCtx) {
        /* listOfExpressions   listOfFormats    reaction
         *    empty              empty          warning: no effect if no TFU is set
         *    empty              not empty      warning if format elements are in listOfFormats
         *    not empty          empty          nothing to do: LIST format applies to all expressions
         *    not empty          not empty      check matching until variable elements appear in 
         *                                      listOfExpressions or ListOfFormats
         */
        if (dataCtx == null && fmtCtx == null) {
            // no data no formats
            if (!m_typeDation.hasTfu()) {
                ErrorStack.warn("no data, no formats has no effect without TFU");
            }
        }

        if (fmtCtx != null) {
            visitChildren(fmtCtx);
        }

        m_formatListAborted = false;
        List<ParserRuleContext> fmtPos = null;
        if (fmtCtx != null) {
            fmtPos = getFormatOrPositions(fmtCtx);
        }

        List<IODataListElementWithCtx> expr = null;
        if (dataCtx != null) {
            expr = getIoDataListWithCtx(dataCtx);
        }

        if (dataCtx == null && fmtCtx != null) {
            // test if a format element is in the format list
            for (int i = 0; i < fmtPos.size(); i++) {
                if (fmtPos.get(i) instanceof OpenPearlParser.FormatContext) {

                    ErrorStack.enter(((OpenPearlParser.FormatContext) fmtPos.get(i)));
                    ErrorStack.warn("format is never applied");
                    ErrorStack.leave();
                    break;
                }

            }
        }
        if (abortCheck) {
            return; // severe error (eg. factor <=0) detected --> no further checks
        }

        if (dataCtx != null && fmtCtx != null) {
            // test if all expression types fit to the corresponding format
            // abort the search as soon as an non constant amount of expressions 
            // (like an array slice with variable length), or
            // an expression is detected for the factor of a format element 

            // test if listOfFormatPosition contains at least 1 format
            boolean fmtFound = false;
            boolean endOfAbortedFormatListReached = false;
            for (int i = 0; fmtFound == false && i < fmtPos.size(); i++) {
                if (fmtPos.get(i) instanceof OpenPearlParser.FormatContext) {
                    fmtFound = true;
                }
            }

            if (!fmtFound && !m_formatListAborted) {
                ErrorStack.add("non empty format list needs at least 1 format");
            } else {
                int fmtPosIndex = 0;
                for (int i = 0; i < expr.size(); i++) {
                    IODataListElementWithCtx etc = expr.get(i);
                    //            System.out.println(i+": "+etc.ctx.getText()+"  "+etc.td);
                    if (fmtPosIndex == fmtPos.size()) {
                        if (!m_formatListAborted) {
                            fmtPosIndex = 0;
                        } else {
                            endOfAbortedFormatListReached = true;
                        }
                    }

                    if (fmtPos.isEmpty() || endOfAbortedFormatListReached) {
                        ErrorStack.enter(fmtCtx.formatPosition(fmtPosIndex),
                                "expression type match format");
                        ErrorStack.warn("check aborted: non static element after here");
                        ErrorStack.leave();
                        break;
                    } else {

                        while (!fmtPos.isEmpty()
                                && fmtPos.get(fmtPosIndex) instanceof OpenPearlParser.PositionContext) {

                            //visit(fmtPos.get(fmtPosIndex));

                            fmtPosIndex++;

                            if (fmtPosIndex == fmtPos.size()) {
                                if (!m_formatListAborted) {
                                    fmtPosIndex = 0;
                                } else {
                                    ErrorStack.enter(fmtPos.get(fmtPosIndex - 1),
                                            "expression type match format");
                                    ErrorStack.warn("check aborted: non static element after here");
                                    ErrorStack.leave();
                                    break;
                                }
                            }
                        }

                        if (!fmtPos.isEmpty() && fmtPos.get(fmtPosIndex) instanceof OpenPearlParser.FormatContext) {
                            if (!exprMatchFormat(fmtPos.get(fmtPosIndex), etc)) {
                                ErrorStack.enter(etc.ctx, "expression type match format");
                                ErrorStack.add("" + etc.td + " does not apply to format ");

                                ErrorStack.enter(((OpenPearlParser.FormatContext) fmtPos.get(fmtPosIndex)));
                                ErrorStack.note("" + fmtPos.get(fmtPosIndex).getText());
                                ErrorStack.leave();
                                ErrorStack.leave();
                            }

                        }

                        fmtPosIndex++;
                    }
                }
            }
        }

    }

    private boolean exprMatchFormat(ParserRuleContext formatCtx, IODataListElementWithCtx dataElementWithContext) {
        // LIST matches all types
        if (formatCtx.getChild(0) instanceof OpenPearlParser.ListFormatContext) {
            return true;
        }
        ASTAttribute attr = null;
        
        TypeDefinition td = dataElementWithContext.td;
        IoListElementContext lctx = (IoListElementContext)(dataElementWithContext.ctx);
        if (lctx.expression() != null) {
            attr = m_ast.lookup(lctx.expression());
        } else {
            attr = m_ast.lookup(lctx.arraySlice());
        }

        // implicit dereference
        if (!m_directionInput && td instanceof TypeReference) {
            td = TypeUtilities.performImplicitDereferenceAndFunctioncall(attr);
        }
        if (!m_directionInput && td instanceof TypeProcedure) {
            td = ((TypeProcedure)td).getResultType();
            attr.setIsFunctionCall(true);
        }

        if (m_directionInput && (td instanceof TypeReference || td instanceof TypeProcedure) ) {
            ErrorStack.enter(dataElementWithContext.ctx);
            ErrorStack.add("type mismatch: required: ALPHIC --- got "
                    + attr.getType().toString());
            ErrorStack.leave();
            return true;  // let's say that it is ok - no further messages with illegal REF type
        }
        
        // treat only simple types
        // F-format
        if (formatCtx.getChild(0) instanceof OpenPearlParser.FixedFormatContext
                && (td instanceof TypeFixed || td instanceof TypeFloat)) {
            return true;
        }
        // E-format
        if ((formatCtx.getChild(0) instanceof OpenPearlParser.FloatFormatEContext
                || formatCtx.getChild(0) instanceof OpenPearlParser.FloatFormatE3Context)
                && td instanceof TypeFloat) {
            return true;
        }
        // B-Format
        if ((formatCtx.getChild(0) instanceof OpenPearlParser.BitFormat1Context
                || formatCtx.getChild(0) instanceof OpenPearlParser.BitFormat2Context
                || formatCtx.getChild(0) instanceof OpenPearlParser.BitFormat3Context
                || formatCtx.getChild(0) instanceof OpenPearlParser.BitFormat4Context) && td instanceof TypeBit) {
            return true;
        }
        //A-Format
        if (formatCtx.getChild(0) instanceof OpenPearlParser.CharacterStringFormatContext
                && td instanceof TypeChar) {
            return true;
        }
        if (formatCtx.getChild(0) instanceof OpenPearlParser.CharacterStringFormatContext
                && td instanceof TypeVariableChar && m_directionInput == false) {
            return true;
        }

        // D-Format
        if (formatCtx.getChild(0) instanceof OpenPearlParser.DurationFormatContext && td instanceof TypeDuration) {
            return true;
        }
        // T-Format
        if (formatCtx.getChild(0) instanceof OpenPearlParser.TimeFormatContext && td instanceof TypeClock) {
            return true;
        }


        return false;
    }

    private List<IODataListElementWithCtx> getIoDataListWithCtx(OpenPearlParser.IoDataListContext ioDataList) {
        List<IODataListElementWithCtx> list = new ArrayList<IODataListElementWithCtx>();

        for (int i = 0; i < ioDataList.ioListElement().size(); i++) {
            OpenPearlParser.IoListElementContext ctx = ioDataList.ioListElement(i);
            IODataListElementWithCtx etc = new IODataListElementWithCtx(ctx);

            ASTAttribute attr = null;
            if (ctx.expression() != null) {
                attr = m_ast.lookup(ctx.expression());
            } else if (ctx.arraySlice() != null) {
                attr = m_ast.lookup(ctx.arraySlice());
            }

            if (attr != null) {
                if (attr.getType() instanceof TypeArray) {
                    int nbrOfElements = ((TypeArray) (attr.getType())).getTotalNoOfElements();
                    etc.setType(((TypeArray) (attr.getType())).getBaseType());
                    for (int j = 0; j < nbrOfElements; j++) {
                        list.add(etc);
                    }
                } else if (attr.getType() instanceof TypeStructure) {
                    for (int j = 0; j < ((TypeStructure) (attr.getType())).m_listOfComponents
                            .size(); j++) {
                        etc = new IODataListElementWithCtx(ctx);
                        TypeDefinition td =
                                ((TypeStructure) (attr.getType())).m_listOfComponents.get(j).m_type;
                        if (td instanceof TypeArray || td instanceof TypeStructure) {
                            // delegate missing
                        } else {
                            etc.setType(td);
                            list.add(etc);
                        }
                    }
                } else if (attr.getType() instanceof TypeArraySlice) {
                    TypeArraySlice tas = (TypeArraySlice) (attr.getType());
                    if (tas.hasConstantSize()) {
                        etc.setType(((TypeArray) (tas.getBaseType())).getBaseType());
                        for (long j = 0; j < tas.getTotalNoOfElements(); j++) {
                            list.add(etc);
                        }
                    }
                } else {
                    etc.setType(attr.getType());
                    list.add(etc);
                }
            }
        }

        return list;
    }



    @Override
    public Void visitOpenClosePositionRST(OpenPearlParser.OpenClosePositionRSTContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitOpenClosePositionRST");
        }

        checkPrecision(ctx);
        return null;
    }

    @Override
    public Void visitFieldWidth(OpenPearlParser.FieldWidthContext ctx) {

        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitFieldWidth");
        }


        ErrorStack.enter(ctx, "fieldwidth");

        ASTAttribute attr = m_ast.lookup(ctx.expression());
        if (!(attr.m_type instanceof TypeFixed)) {
            ErrorStack.add("must by of type FIXED --- got " + attr.m_type);
        }
        // width is mandatory, which is definied in the grammar
        ConstantFixedValue cfv = getConstantValue(attr);
        if (cfv != null) {
            long value = cfv.getValue();
            if (value <= 0) {
                ErrorStack.add("must be >0 ");
            }
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitNumberOfCharacters(OpenPearlParser.NumberOfCharactersContext ctx) {

        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitNumberOfCharacters");
        }

        ErrorStack.enter(ctx, "numberOfCharacters");

        // check, if we have ASTattributes for this node
        TypeDefinition type = m_ast.lookupType(ctx.expression());

        if (!(type instanceof TypeFixed)) {
            ErrorStack.add("type must be FIXED");
        } else {
            ASTAttribute attr = m_ast.lookup(ctx.expression());
            // width is mandatory, which is definied in the grammar
            ConstantFixedValue cfv = getConstantValue(attr);
            if (cfv != null) {
                long value = cfv.getValue();
                if (value <= 0) {
                    ErrorStack.add("must be >0 ");
                }
            }
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitDecimalPositions(OpenPearlParser.DecimalPositionsContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitDecimalPositions");
        }

        ErrorStack.enter(ctx, "decimal positions");

        TypeDefinition type = m_ast.lookupType(ctx.expression());
        if (!(type instanceof TypeFixed)) {
            ErrorStack.add("type must be FIXED");
        } else {
            if (ctx.expression() != null) {
                // we have the attribute given
                ASTAttribute attr = m_ast.lookup(ctx.expression());
                ConstantFixedValue cfv = getConstantValue(attr);
                if (cfv != null) {
                    long value = cfv.getValue();
                    if (value < 0) {
                        ErrorStack.add("must be >=0 ");
                    }
                }
            }
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitSignificance(OpenPearlParser.SignificanceContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitSignificance");
        }

        ErrorStack.enter(ctx, "significance");
        
        TypeDefinition type = m_ast.lookupType(ctx.expression());
        if (!(type instanceof TypeFixed)) {
            ErrorStack.add("type must be FIXED");
        } else {
            if (ctx.expression() != null) {
                // we have the attribute given
                ASTAttribute attr = m_ast.lookup(ctx.expression());
                ConstantFixedValue cfv = getConstantValue(attr);
                if (cfv != null) {
                    long value = cfv.getValue();
                    if (value <= 0) {
                        ErrorStack.add("must be >0 ");
                    }
                }
            }
        }
        ErrorStack.leave();
        return null;
    }


    @Override
    public Void visitFloatFormatE(OpenPearlParser.FloatFormatEContext ctx) {
        boolean checkWidth = false;
        boolean checkDecimalPositions = false;
        boolean checkSignificance = false;
        long width = 0;
        long decimalPositions = 0;
        long significance = 0;

        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitFloatFormatE");
        }

        ErrorStack.enter(ctx, "E-format");

        enshureAlphicDation(ctx);

        // check the types of all children
        visitChildren(ctx);
        if (ErrorStack.getLocalCount() == 0) {

            // check of the parameters is possible if they are
            // of type ConstantFixedValue
            // or not given

            ASTAttribute attr = m_ast.lookup(ctx.fieldWidth().expression());
            // width is mandatory, which is defined in the grammar
            ConstantFixedValue cfv = getConstantValue(attr);
            if (cfv != null) {
                width = cfv.getValue();
                checkWidth = true;
            }

            if (ctx.decimalPositions() != null) {
                // we have the attribute given
                attr = m_ast.lookup(ctx.decimalPositions().expression());
                cfv = getConstantValue(attr);
                if (cfv != null) {
                    // is of type ConstantFixedValue
                    // --> get value and use it for checks
                    decimalPositions = cfv.getValue();
                    checkDecimalPositions = true;
                }
            } else {
                // decimalPositions not given --> used default value
                decimalPositions = 0;
                checkDecimalPositions = true;
            }

            if (ctx.significance() != null) {
                // we have the attribute given
                attr = m_ast.lookup(ctx.significance().expression());
                cfv = getConstantValue(attr);
                if (cfv != null) {
                    // is of type ConstantFixedValue
                    // --> get value and use it for checks
                    significance = cfv.getValue();
                    checkSignificance = true;
                }
            } else {
                // significance not given --> used default value
                // which need the dicimalPositions to be kwnown
                if (checkDecimalPositions) {
                    significance = decimalPositions + 1;
                    checkSignificance = true;
                }
            }

            // analysis complete --> let's apply the checks
            /*
            System.out.println("width=" + width + " (check=" + checkWidth + ")"
            		+ "decimalPositions=" + decimalPositions + " (check="
            		+ checkDecimalPositions + ")" + "significance="
            		+ significance + " (check=" + checkSignificance + ")");
             */
            int sizeForExponent = 4;
            if (m_directionInput) {
                sizeForExponent = 0;

            }

            if (checkWidth && checkDecimalPositions && checkSignificance) {
                if (significance <= decimalPositions) {
                    ErrorStack.add("significance must be larger than decimal positions");
                }

                // add 5 to significance due to decimal point and "E+xx"
                // add 6 to decimal positions due to
                // leading digit, decimal point and "E+xx"
                // if the output value is <0, there may still occur a
                // problem during run time, since the sign is not mandatory
                if ((width < significance + sizeForExponent + 1)
                        || (width < decimalPositions + sizeForExponent + 2)) {
                    ErrorStack
                    .add("field width too small (at least "
                            + Math.max((significance + sizeForExponent + 1),
                                    (decimalPositions + sizeForExponent + 2))
                            + " required)");
                }
            } else if (!checkWidth && checkDecimalPositions && checkSignificance) {
                if (significance <= decimalPositions) {
                    ErrorStack.add("significance must be larger than decimal positions");
                }
            } else if (checkWidth && checkDecimalPositions) {
                // add 6 to decimal positions due to
                // leading digit, decimal point and "E+xx"
                // if the output value is <0, there may still occur a
                // problem during run time, since the sign is not mandatory
                if (width < decimalPositions + sizeForExponent + 2) {
                    ErrorStack.add("field width too small (at least "
                            + (decimalPositions + sizeForExponent + 2) + " required)");
                }
            } else if (checkWidth && checkSignificance) {

                // add 5 to significance due to decimal point and "E+xx"
                // if the output value is <0, there may still occur a
                // problem during run time, since the sign is not mandatory
                if (width < significance + sizeForExponent + 1) {
                    ErrorStack.add("field width too small (at least "
                            + (significance + sizeForExponent + 1) + " required)");
                }
            }
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitFloatFormatE3(OpenPearlParser.FloatFormatE3Context ctx) {
        boolean checkWidth = false;
        boolean checkDecimalPositions = false;
        boolean checkSignificance = false;
        long width = 0;
        long decimalPositions = 0;
        long significance = 0;

        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitFloatFormatE3");
        }

        ErrorStack.enter(ctx, "E3-format");

        enshureAlphicDation(ctx);

        // check the types of all children
        visitChildren(ctx);
        if (ErrorStack.getLocalCount() == 0) {

            // check of the parameters is possible if they are
            // of type ConstantFixedValue
            // or not given

            ASTAttribute attr = m_ast.lookup(ctx.fieldWidth().expression());
            // width is mandatory, which is definied in the grammar
            ConstantFixedValue cfv = getConstantValue(attr);
            if (cfv != null) {
                width = cfv.getValue();
                checkWidth = true;
            }

            if (ctx.decimalPositions() != null) {
                // we have the attribute given
                attr = m_ast.lookup(ctx.decimalPositions().expression());
                cfv = getConstantValue(attr);
                if (cfv != null) {
                    // is of type ConstantFixedValue
                    // --> get value and use it for checks
                    decimalPositions = cfv.getValue();
                    checkDecimalPositions = true;
                }
            } else {
                // decimalPositions not given --> used default value
                decimalPositions = 0;
                checkDecimalPositions = true;
            }

            if (ctx.significance() != null) {
                // we have the attribute given
                attr = m_ast.lookup(ctx.significance().expression());
                cfv = getConstantValue(attr);
                if (cfv != null) {
                    // is of type ConstantFixedValue
                    // --> get value and use it for checks
                    significance = cfv.getValue();
                    checkSignificance = true;
                }
            } else {
                // significance not given --> used default value
                // which need the dicimalPositions to be kwnown
                if (checkDecimalPositions) {
                    significance = decimalPositions + 1;
                    checkSignificance = true;
                }
            }

            // analysis complete --> let's apply the checks
            /*
            System.out.println("width=" + width + " (check=" + checkWidth + ")"
            		+ "decimalPositions=" + decimalPositions + " (check="
            		+ checkDecimalPositions + ")" + "significance="
            		+ significance + " (check=" + checkSignificance + ")");

             */
            int sizeForExponent = 5;
            if (m_directionInput) {
                sizeForExponent = 0;

            }
            if (checkWidth && checkDecimalPositions && checkSignificance) {
                if (significance <= decimalPositions) {
                    ErrorStack.add("significance must be larger than decimal positions");
                }

                // add 6 to significance due to decimal point and "E+xxx"
                // add 7 to decimal positions due to
                // leading digit, decimal point and "E+xxx"
                // if the output value is <0, there may still occur a
                // problem during run time, since the sign is not mandatory
                if ((width < significance + sizeForExponent + 1)
                        || (width < decimalPositions + sizeForExponent + 2)) {
                    ErrorStack
                    .add("field width too small (at least "
                            + Math.max((significance + sizeForExponent + 1),
                                    (decimalPositions + sizeForExponent + 2))
                            + " required)");
                }
            } else if (!checkWidth && checkDecimalPositions && checkSignificance) {
                if (significance <= decimalPositions) {
                    ErrorStack.add("significance must be larger than decimal positions");
                }
            } else if (checkWidth && checkDecimalPositions) {
                // add 7 to decimal positions due to
                // leading digit, decimal point and "E+xxx"
                // if the output value is <0, there may still occur a
                // problem during run time, since the sign is not mandatory
                if (width < decimalPositions + sizeForExponent + 2) {
                    ErrorStack.add("field width too small (at least "
                            + (decimalPositions + sizeForExponent + 2) + " required)");
                }
            } else if (checkWidth && checkSignificance) {

                // add 6 to significance due to decimal point and "E+xxx"
                // if the output value is <0, there may still occur a
                // problem during run time, since the sign is not mandatory
                if (width < significance + sizeForExponent + 1) {
                    ErrorStack.add("field width too small (at least "
                            + (significance + sizeForExponent + 1) + " required)");
                }
            }
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitFixedFormat(OpenPearlParser.FixedFormatContext ctx) {
        boolean checkWidth = false;
        boolean checkDecimalPositions = false;
        long width = 0;
        long decimalPositions = 0;

        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitFixedFormat");
        }

        ErrorStack.enter(ctx, "F-format");

        enshureAlphicDation(ctx);

        // check the types of all children
        visitChildren(ctx);
        if (ErrorStack.getLocalCount() == 0) {

            // check of the parameters is possible if they are
            // of type ConstantFixedValue
            // or not given

            ASTAttribute attr = m_ast.lookup(ctx.fieldWidth().expression());
            // width is mandatory, which is defined in the grammar
            ConstantFixedValue cfv = getConstantValue(attr);
            if (cfv != null) {
                width = cfv.getValue();
                checkWidth = true;
            }

            if (ctx.decimalPositions() != null) {
                // we have the attribute given
                attr = m_ast.lookup(ctx.decimalPositions().expression());
                cfv = getConstantValue(attr);
                if (cfv != null) {
                    // is of type ConstantFixedValue
                    // --> get value and use it for checks
                    decimalPositions = cfv.getValue();
                    checkDecimalPositions = true;
                }
            }

            // analysis complete --> let's apply the checks
            /*
            System.out.println("width=" + width + " (check=" + checkWidth + ")"
            		+ "decimalPositions=" + decimalPositions + " (check="
            		+ checkDecimalPositions + ")" + 
            		")");

             */

            if (checkWidth && checkDecimalPositions) {
                // add 2 to decimal positions due to
                // leading digit, decimal point
                // if the output value is <0, there may still occur a
                // problem during run time, since the sign is not mandatory
                if (width < decimalPositions + 2) {
                    ErrorStack.add("field width too small (at least " + (decimalPositions + 2)
                            + " required)");
                }
            } //else if (checkWidth) {

            // at least 1 digit required
            // if the output value is <0, there may still occur a
            // problem during run time, since the sign is not mandatory
            // this is already checkes in visitFieldWidth!
            //if (width < 1) {
            //	ErrorStack.add("field width too small (at least 1 required)");
            //}
            //}
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitDurationFormat(OpenPearlParser.DurationFormatContext ctx) {
        boolean checkWidth = false;
        boolean checkDecimalPositions = false;
        long width = 0;
        long decimalPositions = 0;

        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitDurationFormat");
        }

        ErrorStack.enter(ctx, "D-format");

        enshureAlphicDation(ctx);

        // check the types of all children
        visitChildren(ctx);
        if (ErrorStack.getLocalCount() == 0) {

            // check of the parameters is possible if they are
            // of type ConstantFixedValue
            // or not given

            ASTAttribute attr = m_ast.lookup(ctx.fieldWidth().expression());
            // width is mandatory, which is defined in the grammar
            ConstantFixedValue cfv = getConstantValue(attr);
            if (cfv != null) {
                width = cfv.getValue();
                checkWidth = true;
            }

            if (ctx.decimalPositions() != null) {
                // we have the attribute given
                attr = m_ast.lookup(ctx.decimalPositions().expression());
                cfv = getConstantValue(attr);
                if (cfv != null) {
                    // is of type ConstantFixedValue
                    // --> get value and use it for checks
                    decimalPositions = cfv.getValue();
                    checkDecimalPositions = true;
                }
            }


            // analysis complete --> let's apply the checks
            /*
            System.out.println("width=" + width + " (check=" + checkWidth + ")"
            		+ "decimalPositions=" + decimalPositions + " (check="
            		+ checkDecimalPositions + ")" + 
            		")");

             */


            if (checkWidth && checkDecimalPositions) {
                // add 6 to decimal positions due to
                // leading digit, decimal point ans 'SEC'
                // if the output value is <0 or the value is larger than 1 sec,
                // there may still occur a problem during run time
                if (m_directionInput && width < 4) {
                    ErrorStack.add("field width too small (at least 4 required)");
                }
                if (!m_directionInput && width < decimalPositions + 6) {
                    ErrorStack.add("field width too small (at least " + (decimalPositions + 6)
                            + " required)");
                }
            } else if (checkWidth) {
                // at least 'x_SEC' required
                // if the output value is <0 or > 9, there may still occur a
                // problem during run time, since the sign is not mandatory
                // this is already checkes in visitFieldWidth!
                if (m_directionInput && width < 4) {
                    ErrorStack.add("field width too small (at least 4 required)");
                }
                if (!m_directionInput && width < 5) {
                    ErrorStack.add("field width too small (at least 5 required)");
                }
            }
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitTimeFormat(OpenPearlParser.TimeFormatContext ctx) {
        boolean checkWidth = false;
        boolean checkDecimalPositions = false;
        long width = 0;
        long decimalPositions = 0;

        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitTimeFormat");
        }

        ErrorStack.enter(ctx, "T-format");

        enshureAlphicDation(ctx);

        // check the types of all children
        visitChildren(ctx);
        if (ErrorStack.getLocalCount() == 0) {

            // check of the parameters is possible if they are
            // of type ConstantFixedValue
            // or not given

            ASTAttribute attr = m_ast.lookup(ctx.fieldWidth().expression());
            // width is mandatory, which is defined in the grammar
            ConstantFixedValue cfv = getConstantValue(attr);
            if (cfv != null) {
                width = cfv.getValue();
                checkWidth = true;
            }

            if (ctx.decimalPositions() != null) {
                // we have the attribute given
                attr = m_ast.lookup(ctx.decimalPositions().expression());
                cfv = getConstantValue(attr);
                if (cfv != null) {
                    // is of type ConstantFixedValue
                    // --> get value and use it for checks
                    decimalPositions = cfv.getValue();
                    checkDecimalPositions = true;
                }
            }


            // analysis complete --> let's apply the checks
            /*
            System.out.println("width=" + width + " (check=" + checkWidth + ")"
            		+ "decimalPositions=" + decimalPositions + " (check="
            		+ checkDecimalPositions + ")" + 
            		")");

             */

            if (checkWidth && checkDecimalPositions) {
                // add 8 to decimal positions due to
                // x:xx:xx.xxx
                // if the output value is <0 or the value is larger than 9:59:59.99,
                // there may still occur a problem during run time
                if (width < decimalPositions + 8) {
                    ErrorStack.add("field width too small (at least " + (decimalPositions + 8)
                            + " required)");
                }
            } else if (checkWidth) {
                // at least 'x:xx:xx' required
                // if the output value is > 9:59:59.999, there may still occur a
                // problem during run time, since the sign is not mandatory
                // this is already checkes in visitFieldWidth!
                if (width < 7) {
                    ErrorStack.add("field width too small (at least 7 required)");
                }
            }
        }
        ErrorStack.leave();
        return null;
    }


    @Override
    public Void visitCharacterStringFormatA(OpenPearlParser.CharacterStringFormatAContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitCharacterStringFormat");
        }

        ErrorStack.enter(ctx, "A-format");

        enshureAlphicDation(ctx);

        // check the types of all children
        visitChildren(ctx);
        if (ErrorStack.getLocalCount() == 0) {

            // check of the parameters is possible if they are
            // of type ConstantFixedValue
            // or not given
            // check of the value oof width is already done
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitCharacterStringFormatS(OpenPearlParser.CharacterStringFormatSContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitCharacterStringFormatS");
        }

        ErrorStack.enter(ctx, "S-format");

        enshureAlphicDation(ctx);

        ErrorStack.add("S-format not sopported yet");
        /*
        // check the types of all children
        visitChildren(ctx);
        if (ErrorStack.getLocalCount() == 0) {

        	// check of the parameters is possible if they are
        	// of type ConstantFixedValue
        	// or not given
        	// check of the value oof width is already done
        }
         */
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitBitFormat1(OpenPearlParser.BitFormat1Context ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitBitFormat1");
        }

        ErrorStack.enter(ctx, "B/B1-format");

        enshureAlphicDation(ctx);

        // check the types of all children
        visitChildren(ctx);
        if (ErrorStack.getLocalCount() == 0) {
            if (m_directionInput && ctx.numberOfCharacters() == null) {
                ErrorStack.add("B-format needs width on direction input");
            }
            // check of the parameters is possible if they are
            // of type ConstantFixedValue
            // or not given
            // check of the value of width is already done
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitBitFormat2(OpenPearlParser.BitFormat2Context ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitBitFormat2");
        }

        ErrorStack.enter(ctx, "B2-format");

        enshureAlphicDation(ctx);

        // check the types of all children
        visitChildren(ctx);
        if (m_directionInput && ctx.numberOfCharacters() == null) {
            ErrorStack.add("B2-format needs width on direction input");
        }
        if (ErrorStack.getLocalCount() == 0) {

            // check of the parameters is possible if they are
            // of type ConstantFixedValue
            // or not given
            // check of the value of width is already done
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitBitFormat3(OpenPearlParser.BitFormat3Context ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitBitFormat3");
        }

        ErrorStack.enter(ctx, "B3-format");

        enshureAlphicDation(ctx);

        // check the types of all children
        visitChildren(ctx);
        if (m_directionInput && ctx.numberOfCharacters() == null) {
            ErrorStack.add("B3-format needs width on direction input");
        }
        if (ErrorStack.getLocalCount() == 0) {

            // check of the parameters is possible if they are
            // of type ConstantFixedValue
            // or not given
            // check of the value of width is already done
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitBitFormat4(OpenPearlParser.BitFormat4Context ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitBitFormat4");
        }

        ErrorStack.enter(ctx, "B4-format");

        enshureAlphicDation(ctx);

        // check the types of all children
        visitChildren(ctx);
        if (m_directionInput && ctx.numberOfCharacters() == null) {
            ErrorStack.add("B4-format needs width on direction input");
        }
        if (ErrorStack.getLocalCount() == 0) {

            // check of the parameters is possible if they are
            // of type ConstantFixedValue
            // or not given
            // check of the value of width is already done
        }
        ErrorStack.leave();
        return null;
    }

    /* --------------------------------------------------- */
    // positions
    @Override
    public Void visitPositionX(OpenPearlParser.PositionXContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitPositionX");
        }

        ErrorStack.enter(ctx, "X-format");

        // check the types of all children
        // check, if we have ASTattributes for this node
        if (ctx.expression() != null) {
            enshureTypeFixed(ctx.expression());
        }
        ASTAttribute attr = m_ast.lookup(ctx.expression());
        if (m_typeDation.isForward()) {
            if (attr != null && attr.getConstantFixedValue() != null
                    && attr.getConstantFixedValue().getValue() < 0) {
                ErrorStack.add("no backward positioning allowed");
            }
        }

        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitPositionSKIP(OpenPearlParser.PositionSKIPContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitPositionSkip");
        }

        ErrorStack.enter(ctx, "SKIP-format");

        // check the types of all children
        // check, if we have ASTattributes for this node
        if (ctx.expression() != null) {
            enshureTypeFixed(ctx.expression());
        }

        enshureDimensionsFit(ctx, 2);
        ASTAttribute attr = m_ast.lookup(ctx.expression());
        if (attr != null && attr.getConstantFixedValue() != null && m_typeDation.isForward()) {
            if (attr.getConstantFixedValue().getValue() < 0) {
                ErrorStack.add("no backward positioning allowed");
            }
        }

        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitPositionPAGE(OpenPearlParser.PositionPAGEContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitPositionPAGE");
        }

        ErrorStack.enter(ctx, "PAGE-format");

        // check the types of all children
        // check, if we have ASTattributes for this node
        if (ctx.expression() != null) {
            enshureTypeFixed(ctx.expression());
        }

        enshureDimensionsFit(ctx, 3);

        ASTAttribute attr = m_ast.lookup(ctx.expression());
        if (m_typeDation.isForward()) {
            if (attr.getConstantFixedValue().getValue() < 0) {
                ErrorStack.add("no backward positioning allowed");
            }
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitPositionADV(OpenPearlParser.PositionADVContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitPositionADV");
        }

        ErrorStack.enter(ctx, "ADV-format");

        // check the types of all children
        // check, if we have ASTattributes for this node
        for (int i = 0; i < ctx.expression().size(); i++) {
            enshureTypeFixed(ctx.expression(i));
        }

        enshureDimensionsFit(ctx, ctx.expression().size());


        // calculate relative position, if possible
        boolean allExpressionsAreConstant = true;
        for (int i = 0; i < ctx.expression().size(); i++) {
            ASTAttribute attr = m_ast.lookup(ctx.expression(i));
            if (attr == null || attr.getConstantFixedValue() == null) {
                allExpressionsAreConstant = false;
            }
        }
        if (allExpressionsAreConstant) {
            long delta = 0;
            ASTAttribute attr;

            switch (ctx.expression().size()) {
                case 1:
                    attr = m_ast.lookup(ctx.expression(0));
                    delta = attr.getConstantFixedValue().getValue();
                    break;
                case 2:
                    attr = m_ast.lookup(ctx.expression(0));
                    delta = attr.getConstantFixedValue().getValue();
                    delta *= m_typeDation.getDimension2();
                    attr = m_ast.lookup(ctx.expression(1));
                    delta += attr.getConstantFixedValue().getValue();
                    break;
                case 3:
                    attr = m_ast.lookup(ctx.expression(0));
                    delta = attr.getConstantFixedValue().getValue();
                    delta *= m_typeDation.getDimension2();
                    attr = m_ast.lookup(ctx.expression(1));
                    delta += attr.getConstantFixedValue().getValue();
                    delta *= m_typeDation.getDimension3();
                    attr = m_ast.lookup(ctx.expression(2));
                    delta += attr.getConstantFixedValue().getValue();
                    break;
            }
            if (delta < 0) {
                if (m_typeDation.isForward()) {
                    ErrorStack.add("no backward positioning allowed");
                }
            }
        }


        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitPositionPOS(OpenPearlParser.PositionPOSContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitPositionPOS");
        }

        ErrorStack.enter(ctx, "POS-format");

        enshureDirectDation(ctx);

        // check the types of all children
        // check, if we have ASTattributes for this node
        for (int i = 0; i < ctx.expression().size(); i++) {
            enshureTypeFixed(ctx.expression(i));
            enshureGreaterZero(ctx.expression(i));
        }

        enshureDimensionsFit(ctx, ctx.expression().size());


        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitPositionSOP(OpenPearlParser.PositionSOPContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitPositionSOP");
        }

        ErrorStack.enter(ctx, "SOP-format");

        enshureDirectDation(ctx);

        // check the types of all children
        // check, if we have ASTattributes for this node
        for (int i = 0; i < ctx.ID().size(); i++) {
            checkFixedVariable(ctx.ID(i).getText(), ctx);
        }


        enshureDimensionsFit(ctx, ctx.ID().size());

        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitPositionCOL(OpenPearlParser.PositionCOLContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitPositionCOL");
        }

        ErrorStack.enter(ctx, "COL-format");

        enshureDirectDation(ctx);

        // check the types of all children
        // check, if we have ASTattributes for this node
        enshureTypeFixed(ctx.expression());
        enshureGreaterZero(ctx.expression());

        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitPositionLINE(OpenPearlParser.PositionLINEContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check IOStatements: visitPositionLINE");
        }

        ErrorStack.enter(ctx, "LINE-format");

        enshureDirectDation(ctx);
        enshureDimensionsFit(ctx, 2);

        // check the types of all children
        // check, if we have ASTattributes for this node
        enshureTypeFixed(ctx.expression());
        enshureGreaterZero(ctx.expression());
        ErrorStack.leave();
        return null;
    }


    /* ----------------------------------------------------_ */

    private ConstantFixedValue getConstantValue(ASTAttribute formatAttribute) {
        //System.out.println("formatAttribute=" + formatAttribute);
        if (formatAttribute.isConstant()) {
            if (formatAttribute.getType() instanceof TypeFixed) {
                ConstantFixedValue cfv = (ConstantFixedValue) formatAttribute.getConstant();
                cfv = (ConstantFixedValue) formatAttribute.getConstantFixedValue();
                /*
                if (cfv != null) {
                	System.out.println("width=" + cfv.getValue());
                	System.out.println("precision=" + cfv.getPrecision());
                }
                 */
                return cfv;
            }
        } else {
            // entry is in symbol table! --> is a variable!
            // we have no constant
        }
        return null;
    }

    private Void enshureTypeFixed(OpenPearlParser.ExpressionContext ctx) {
        TypeDefinition type = m_ast.lookupType(ctx);
        ASTAttribute attr = m_ast.lookup(ctx);
        ErrorStack.enter(ctx, "expression");
        if (type instanceof TypeReference) {
            type = TypeUtilities.performImplicitDereferenceAndFunctioncall(attr);
        }
        if (!(type instanceof TypeFixed)) {
            ErrorStack.add("must be FIXED");
        }
        ErrorStack.leave();
        return null;
    }

    private Void enshureGreaterZero(OpenPearlParser.ExpressionContext ctx) {
        ErrorStack.enter(ctx, "expression");
        ASTAttribute attr = m_ast.lookup(ctx);

        ConstantFixedValue cfv = getConstantValue(attr);
        if (cfv != null) {
            // is of type ConstantFixedValue
            // --> get value and use it for checks
            if (cfv.getValue() <= 0) {
                ErrorStack.add("must be > 0");
            }
        }
        ErrorStack.leave();
        return null;
    }

    private void checkPrecision(OpenPearlParser.OpenClosePositionRSTContext ctx) {
        ASTAttribute attr = m_ast.lookup(ctx.name());
        TypeDefinition td = attr.getType();
        if (td instanceof TypeReference) {
            td = TypeUtilities.performImplicitDereferenceAndFunctioncall(attr);
        }
        if (td instanceof TypeFixed) {
            TypeFixed type = (TypeFixed) (td);
            if (((TypeFixed)td).getPrecision() < 15) {
                ErrorStack.add(
                        "must be at least FIXED(15) -- got FIXED(" + type.getPrecision() + ")");
            }
        } else {
            ErrorStack.add("variable must be FIXED");
        }
        return;
    }

    private Void checkFixedVariable(String id, ParserRuleContext ctx) {

        SymbolTableEntry entry = m_currentSymbolTable.lookup(id);
        VariableEntry var = null;

        ErrorStack.enter(ctx, "variable");

        if (entry != null && entry instanceof VariableEntry) {
            // it would be got to set a new error environment to the 
            // context of the 'entry'
            var = (VariableEntry) entry;

            if (var.getType() instanceof TypeFixed) {
                TypeFixed type = (TypeFixed) var.getType();
                if (type.getPrecision() != 31) {
                    ErrorStack.add("must be FIXED(31)");
                }
            } else {
                ErrorStack.add("must be of type FIXED");
            }
        } else {
            ErrorStack.add("must be a variable");
        }

        ErrorStack.leave();

        return null;
    }

    private void enshureAlphicDation(RuleContext ctx) {
        if (!m_typeDation.isAlphic()) {
            ErrorStack.add("applies only on ALPHIC dations");
        }
    }

    private void enshureDirectDation(RuleContext ctx) {
        if (m_typeDation.hasAccessAttributes()) {
            if (!m_typeDation.isDirect()) {
                ErrorStack.add("format applies only on DIRECT dations");
            }
        }
    }

    //	private void allowBackwardPositioning(RuleContext ctx) {
    //		if (!m_typeDation.isDirect() && !m_typeDation.isForback()){
    //			ErrorStack.add("backward positioning need DIRECT or FORBACK");
    //		}
    //	}

    private void enshureDimensionsFit(RuleContext ctx, int nbr) {
        if (m_typeDation.hasTypology()) {
            if (m_typeDation.getNumberOfDimensions() < nbr) {
                ErrorStack.add("too many dimensions used (dation provides "
                        + m_typeDation.getNumberOfDimensions() + ")");
            }
        }
    }


    private void enshureDataForInput(OpenPearlParser.IoDataListContext dataCtx) {
        boolean isName = false;

        if (dataCtx != null) {
            for (int i = 0; i < dataCtx.ioListElement().size(); i++) {
                ErrorStack.enter(dataCtx.ioListElement(i));
                if (dataCtx.ioListElement(i).expression() != null) {
                    OpenPearlParser.ExpressionContext expr =
                            (OpenPearlParser.ExpressionContext) (dataCtx.ioListElement(i).expression());
                    // only name is allowed
                    if (expr instanceof OpenPearlParser.BaseExpressionContext) {
                        if (((OpenPearlParser.BaseExpressionContext) expr).primaryExpression() != null) {
                            OpenPearlParser.PrimaryExpressionContext pe =
                                    ((OpenPearlParser.BaseExpressionContext) expr).primaryExpression();
                            if (pe.name() != null) {
                                enshureDataForInput(pe.name().getText());
                                isName = true;
                            }
                        }
                    }

                    if (!isName) {
                        ErrorStack.enter(dataCtx.ioListElement(i).expression());
                        ErrorStack.add("only names allowed in input list");
                    }
                } else if (dataCtx.ioListElement(i).arraySlice() != null) {
                    enshureDataForInput(dataCtx.ioListElement(i).arraySlice().name().getText());
                } else {
                    ErrorStack.addInternal(
                            "CheckIoStatment:enshureDataForInput: missing alternative");
                }
                ErrorStack.leave();
            }
        }
    }

    private void enshureDataForInput(String var) {
        SymbolTableEntry se = m_currentSymbolTable.lookup(var);
        if (se == null) {
            ErrorStack.add("'" + var + "' is not defined");
        } else if ((se instanceof VariableEntry)) {
            if (((VariableEntry) se).getType().hasAssignmentProtection()) {
                ErrorStack.add("'" + var + "' is INV");
            }
        }
    }

 

   
    /**
     * parse a FactorFormatPositionContext and deliver a List of formats/positions
     *
     * Grammar:
    listOfFormatPositions :
      formatPosition ( ',' formatPosition )*
      ;

       formatPosition :
          factor? format                                    # factorFormat
        | factor? position                                  # factorPosition
        | factor '(' listOfFormatPositions ')'              # factorFormatPosition
        ;

    factor :
        '(' expression ')'
        | integerWithoutPrecision
        ;

     * Example: (2)(X(4), B4 ,(2)(A,X, 2F(x),SKIP))
     *           B4,A,F,F,A,F,F,B4,A,F,F,A,F,F,null+status(end of format)
     *          2A,(x)(F,E)
     *          A,A,F,E,null+status(end of static format)
     *           
     * @author mueller
     *
     */

    private List<ParserRuleContext> getFormatOrPositions(
            OpenPearlParser.ListOfFormatPositionsContext listOfFormatPosition) {
        List<ParserRuleContext> fmtPos = new ArrayList<ParserRuleContext>();
        long repetitions = 0;

        //System.out.println("get.. "+listOfFormatPosition.getText());

        for (int i = 0; i < listOfFormatPosition.formatPosition().size(); i++) {
            if (listOfFormatPosition.formatPosition(i) instanceof OpenPearlParser.FactorPositionContext) {
                OpenPearlParser.FactorPositionContext ctx =
                        ((OpenPearlParser.FactorPositionContext) listOfFormatPosition.formatPosition(i));
                try {
                    repetitions = getFactor(ctx.factor());
                } catch (FactorIsNotConstantException e) {
                    // if the repetitions of position are not constant and known
                    // we may continue with checks on data formats
                    repetitions = 0;
                }
                for (long j = 0; j < repetitions; j++) {
                    fmtPos.add(ctx.position());
                }
            } else if (listOfFormatPosition.formatPosition(i) instanceof OpenPearlParser.FactorFormatContext) {
                OpenPearlParser.FactorFormatContext ctx =
                        ((OpenPearlParser.FactorFormatContext) listOfFormatPosition.formatPosition(i));
                try {
                    repetitions = getFactor(ctx.factor());
                } catch (FactorIsNotConstantException e) {
                    m_formatListAborted = true;
                    return fmtPos;
                }

                for (long j = 0; j < repetitions; j++) {
                    fmtPos.add(ctx.format());
                }
            } else if (listOfFormatPosition
                    .formatPosition(i) instanceof OpenPearlParser.FactorFormatPositionContext) {
                OpenPearlParser.FactorFormatPositionContext ctx =
                        ((OpenPearlParser.FactorFormatPositionContext) listOfFormatPosition.formatPosition(i));
                try {
                    repetitions = getFactor(ctx.factor());
                } catch (FactorIsNotConstantException e) {
                    m_formatListAborted = true;
                    return fmtPos;
                }
                List<ParserRuleContext> l = getFormatOrPositions(ctx.listOfFormatPositions());
                for (long j = 0; j < repetitions; j++) {
                    for (int k = 0; k < l.size(); k++) {
                        fmtPos.add((ParserRuleContext) (l.get(k)));
                    }
                }
            }

        }
        return fmtPos;
    }

    private long getFactor(OpenPearlParser.FactorContext factor) {
        long value = 0;
        if (factor != null) {
            if (factor.integerWithoutPrecision() != null) {
                value = Integer.parseInt(factor.integerWithoutPrecision().getText());
            } else if (factor.expression() != null) {
                ASTAttribute attr = m_ast.lookup(factor.expression());
                if (attr.m_constant != null) {
                    value = attr.getConstantFixedValue().getValue();
                } else if (attr.isConstant() && attr.getVariable() != null) {
                    ErrorStack.enter(factor);

                    VariableEntry ve = attr.getVariable();
                    if (ve.getType() instanceof TypeArray) {
                        ErrorStack.warn("treatment of ARRAY components missing");
                    } else if (ve.getType() instanceof TypeStructure) {
                        ErrorStack.warn("treatment of STRUCT components missing");
                    } else if (ve.getType() instanceof TypeFixed && ve.getInitializer() != null) {
                        SimpleInitializer si = (SimpleInitializer) (ve.getInitializer());
                        if (si.getConstant() instanceof ConstantFixedValue) {
                            value = ((ConstantFixedValue) (si.getConstant())).getValue();
                            if (value <= 0) {
                                ErrorStack.add("repetition factor must be >0");
                                abortCheck = true;
                                throw new FactorIsNotConstantException();
                            }
                        } else {
                            ErrorStack.addInternal("type of initializer must be TypeFixed");
                        }

                    }
                    ErrorStack.leave();

                } else {
                    throw new FactorIsNotConstantException();
                }
            }
            return value;
        } else {
            return 1;
        }
    }

    private class IODataListElementWithCtx {
        public TypeDefinition td;
        public ParserRuleContext ctx;

        public IODataListElementWithCtx(ParserRuleContext ctx) {
            this.ctx = ctx;

        }

        public void setType(TypeDefinition td) {
            this.td = td;
        }
    }
}

