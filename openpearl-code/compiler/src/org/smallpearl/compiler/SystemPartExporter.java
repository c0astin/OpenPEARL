/*
 * [The "BSD license"]
 *  Copyright (c) 2012-2016 Marcel Schaible
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

package org.smallpearl.compiler;

import org.antlr.v4.runtime.tree.ParseTree;
import org.smallpearl.compiler.Exception.NotSupportedTypeException;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;
import org.smallpearl.compiler.SymbolTable.SymbolTableEntry;
import org.smallpearl.compiler.SymbolTable.VariableEntry;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import java.util.ArrayList;

public class SystemPartExporter extends SmallPearlBaseVisitor<ST> implements SmallPearlVisitor<ST> {

  private static final String IMC_EXPORT_STG = "IMC.stg";

  private STGroup group;
  private int m_verbose;
  private boolean m_debug;
  private String m_sourceFileName;
  private SymbolTableVisitor m_symbolTableVisitor;
  private SymbolTable m_symboltable;
  private SymbolTable m_currentSymbolTable;
  private AST m_ast = null;
  ST module = null;
  ST systemPart = null;
  ST problemPart = null;
  ST tfuUsage = null; 


  public SystemPartExporter(String sourceFileName, int verbose, boolean debug,
      SymbolTableVisitor symbolTableVisitor,
      AST ast) {

    m_debug = debug;
    m_verbose = verbose;
    m_sourceFileName = sourceFileName;
    m_symboltable = symbolTableVisitor.symbolTable;
    m_currentSymbolTable = m_symboltable;
    m_ast = ast;



    if (m_verbose > 1) {
      System.out.println("Generating InterModuleChecker definitions");
    }

    this.ReadTemplate(IMC_EXPORT_STG);
  }

  private Void ReadTemplate(String filename) {
    if (m_verbose > 1) {
      System.out.println("Read StringTemplate Group File: " + filename);
    }

    this.group = new STGroupFile(filename);

    return null;
  }

  @Override
  public ST visitModule(SmallPearlParser.ModuleContext ctx) {
    module = group.getInstanceOf("Module");

    module.add("sourcefile", m_sourceFileName);
    module.add("name", ctx.ID().getText());
    org.smallpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry = m_currentSymbolTable
        .lookupLocal(ctx.ID().getText());
    m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;


    if (ctx != null) {
      visitChildren(ctx);
//      for (ParseTree c : ctx.children) {
//        if (c instanceof SmallPearlParser.System_partContext) {
//          module.add("SystemPart", visitSystem_part((SmallPearlParser.System_partContext) c));
//        } else if (c instanceof SmallPearlParser.Problem_partContext) {
//          module.add("ProblemPart", visitProblem_part((SmallPearlParser.Problem_partContext) c));
//        }
//      }
    }
    module.add("SystemPart",systemPart);
    module.add("ProblemPart",problemPart);
    
    m_currentSymbolTable = m_currentSymbolTable.ascend();
    return module;
  }

  @Override
  public ST visitSystem_part(SmallPearlParser.System_partContext ctx) {
    systemPart = group.getInstanceOf("SystemPart");
    
    ErrorStack.enter(ctx, "system part");
    if (ctx != null) {
      visitChildren(ctx);
    }
    ErrorStack.leave();

    return null;
  }

 
  @Override
  public ST visitSystemElementDefinition(SmallPearlParser.SystemElementDefinitionContext ctx) {
    ST decl = group.getInstanceOf("SystemElementDefinition");

    decl.add("username", ctx.systemPartName().getText());
    decl.add("lineno", ctx.start.getLine());
    decl.add("col",  ctx.start.getCharPositionInLine()+1);
    decl.add("decl", visit(ctx.systemDescription()));

    systemPart.add("decls",  decl);
    return null;
  }

  @Override
  public ST visitConfigurationElement(SmallPearlParser.ConfigurationElementContext ctx) {
    ST decl = group.getInstanceOf("ConfigurationElement");

    decl.add("lineno", ctx.start.getLine());
    decl.add("col",  ctx.start.getCharPositionInLine()+1);
    decl.add("decl", visit(ctx.systemDescription()));

    systemPart.add("decls",  decl);
    return null;
  }


  @Override
  public ST visitSystemDescription(SmallPearlParser.SystemDescriptionContext ctx) {
    ST decl = group.getInstanceOf("SystemDescription");
    
    // we may have
    SymbolTableEntry se = m_currentSymbolTable.lookupSystemPartName(ctx.systemPartName().ID().toString());
    // the symbol table visitor enshures that this entry is a system name
    decl.add("sysname", se.getName()); ///ctx.systemPartName().ID().toString());

    if (ctx.systemElementParameters() != null) {
      decl.add("parameters", visitSystemElementParameters(ctx.systemElementParameters()));
    }
    if (ctx.association().size() > 0) {
      ST previousSt = decl;     
      for (int i=0; i<ctx.association().size(); i++) {
         ST association = group.getInstanceOf("Association");
         previousSt.add("association", association);
         String name = ctx.association(i).systemPartName().ID().toString();
         se = m_currentSymbolTable.lookupSystemPartName(name);
         
         association.add("name", ctx.association(i).systemPartName().ID());
         
 
         if (ctx.association(i).systemElementParameters()!= null) {
           if (se.isUserName()) {
               // we have an user defined name as system element name
               ErrorStack.add(ctx.association(i).systemPartName(),"no parameters allowed for user names","");
               return null;
            }
            association.add("parameters", visitSystemElementParameters(ctx.association(i).systemElementParameters()));
         }
         previousSt = association;
      
      }
    }
    
    return decl;
  }


  @Override
  public ST visitSystemElementParameters(SmallPearlParser.SystemElementParametersContext ctx) {
    ST parameters = group.getInstanceOf("SystemElementParameters");

    for (int i = 0; i < ctx.literal().size(); i++) {
      ST parameter = group.getInstanceOf("Parameter");
      String param = ctx.literal(i).getText();
      //            param = param.replaceAll("^'","");
      //            param = param.replaceAll("'$","");

      if (ctx.literal(i).StringLiteral() != null) {
        ST type = group.getInstanceOf("Type_Char");
        type.add("name", param);
        parameter.add("type", type);
      } else if (ctx.literal(i).BitStringLiteral() != null) {
        ST type = group.getInstanceOf("Type_Bit");
        type.add("name", param);
        parameter.add("type", type);
      } else if (ctx.literal(i).fixedConstant() != null) {
        ST type = group.getInstanceOf("Type_Fixed");
        type.add("name", param);
        parameter.add("type", type);
      }

      parameters.add("params", parameter);
    }

    return parameters;
  }

  @Override
  public ST visitProblem_part(SmallPearlParser.Problem_partContext ctx) {
    problemPart = group.getInstanceOf("ProblemPart");
    tfuUsage = group.getInstanceOf("TfuUsage");


    if (ctx != null) {
      visitChildren(ctx);
      /*
              for (ParseTree c : ctx.children) {

                if (c instanceof SmallPearlParser.IdentificationContext) {
                    visitIdentification((SmallPearlParser.IdentificationContext) c);
                } else if (c instanceof SmallPearlParser.DationSpecificationContext) {
                    visitDationSpecification((SmallPearlParser.DationSpecificationContext) c);
                } else if (c instanceof SmallPearlParser.InterruptSpecificationContext) {
                	visitInterruptSpecification((SmallPearlParser.InterruptSpecificationContext) c);
                } else if (c instanceof SmallPearlParser.DationDeclarationContext) {
                  visitDationDeclaration((SmallPearlParser.DationDeclarationContext) c);
                }
            }
       */
    }
    problemPart.add("decls", tfuUsage);
    return problemPart;
  }

  @Override
  public ST visitIdentification(SmallPearlParser.IdentificationContext ctx) {
    ST st = group.getInstanceOf("Specification");

    st.add("lineno", ctx.start.getLine());
    st.add("col",  ctx.start.getCharPositionInLine()+1);
    st.add("name", ctx.ID().toString());
    if (ctx.type() != null) {
      st.add("type", visitType(ctx.type()));
    }
    problemPart.add("decls", st);
    return null;
  }

  @Override
  public ST visitInterruptSpecification( SmallPearlParser.InterruptSpecificationContext ctx) {


    for (int i=0; i<ctx.ID().size(); i++) {
      ST interruptSpecification = group.getInstanceOf("Specification");
      interruptSpecification.add("type","INTERRUPT");
      interruptSpecification.add("lineno",  ctx.start.getLine());
      interruptSpecification.add("col",  ctx.start.getCharPositionInLine()+1);
      interruptSpecification.add("name", ctx.ID(i).toString());
      problemPart.add("decls", interruptSpecification);
    }

    return null;
  }

  @Override
  public ST visitDationSpecification(SmallPearlParser.DationSpecificationContext ctx) {


    boolean hasGlobalAttribute = false;

    ArrayList<String> identifierDenotationList = null;
    if (ctx != null) {
      if (ctx.identifierDenotation() != null ){
        identifierDenotationList = getIdentifierDenotation(ctx.identifierDenotation());
      }

      if ( ctx.globalAttribute() != null ) {
        hasGlobalAttribute = true;
      }

      for (int i = 0; i < identifierDenotationList.size(); i++) {
        ST dationSpecification = group.getInstanceOf("DationSpecification");

        dationSpecification.add("lineno", ctx.start.getLine());
        dationSpecification.add("col", ctx.start.getCharPositionInLine()+1);
        dationSpecification.add( "name", identifierDenotationList.get(i));

        if ( ctx.typeDation() != null ) {
          ST datalist = group.getInstanceOf("DataList");
          ST attributes = group.getInstanceOf("Attributes");

          if (ctx.typeDation().classAttribute() != null) {
            if (ctx.typeDation().classAttribute().alphicDation() != null) {
              ST data = group.getInstanceOf("Data");
              data.add("name", "ALPHIC");
              datalist.add("data",data);
            }

            if (ctx.typeDation().classAttribute().basicDation() != null) {
              ST attribute = group.getInstanceOf("Attribute");
              attribute.add("name", "BASIC");
              attributes.add("attributes", attribute);
            }

            if (ctx.typeDation().classAttribute().systemDation() != null) {
              ST attribute = group.getInstanceOf("Attribute");
              attribute.add("name", "SYSTEM");
              attributes.add("attributes", attribute);
            }

            if ( ctx.typeDation().classAttribute() != null ) {
              if ( ctx.typeDation().classAttribute().typeOfTransmissionData() != null) {
                ST data = group.getInstanceOf("Data");
                data.add("name", ctx.typeDation().classAttribute().typeOfTransmissionData().getText());
                datalist.add("data",data);
              }
            }
          }

          if ( ctx.typeDation().sourceSinkAttribute() != null) {
            ST attribute = group.getInstanceOf("Attribute");
            attribute.add("name", ctx.typeDation().sourceSinkAttribute().getText());
            attributes.add("attributes",attribute);
          }

          dationSpecification.add("datalist", datalist);
          dationSpecification.add("attributes", attributes);
        }
        problemPart.add("decls",  dationSpecification);
      }
    }

    return null;
  }

  @Override
  public ST visitDationDeclaration(SmallPearlParser.DationDeclarationContext ctx) {
    boolean hasGlobalAttribute = false;

    ArrayList<String> identifierDenotationList = null;
    if (ctx != null) {
      if (ctx.identifierDenotation() != null ){
        identifierDenotationList = getIdentifierDenotation(ctx.identifierDenotation());
      }

      if ( ctx.globalAttribute() != null ) {
        hasGlobalAttribute = true;
      }
      ErrorStack.enter(ctx, "DationDCL");

      for (int i = 0; i < identifierDenotationList.size(); i++) {

        String dationName = ctx.identifierDenotation().ID(i).toString();
        //System.out.println("DationName: "+ dationName);


        SymbolTableEntry entry1 = this.m_currentSymbolTable.lookup(dationName);

        if (entry1 == null) {
          ErrorStack.addInternal("Symbol table does not contain:"+dationName);
          ErrorStack.leave();
          return null;
        }
        SymbolTableEntry se = this.m_currentSymbolTable.lookup(dationName);
        if (se != null &&
            ! (se instanceof VariableEntry) &&
            !(((VariableEntry)se).getType() instanceof TypeDation)
            ) {
          ErrorStack.addInternal("symbol "+dationName+" not found/or no dation");
          ErrorStack.leave();
          return null;
        }
        TypeDation d = (TypeDation)(((VariableEntry)se).getType());     
        if (!d.isSystemDation()) {
          ST tfuInUserDation = group.getInstanceOf("TfuInUserDation");
          tfuInUserDation.add("lineno", se.getSourceLineNo());
          tfuInUserDation.add("col", se.getCharPositionInLine()+1);

          tfuUsage.add("decls", tfuInUserDation);
          tfuInUserDation.add( "userdation", identifierDenotationList.get(i));
          tfuInUserDation.add("systemdation",  d.getCreatedOn());
          if (d.hasTfu()) {
            // already checked, that the last dimension is const > 0
            // get last defined dimension
            int recordLength = d.getDimension3();
            if (recordLength < 0) recordLength = d.getDimension2();
            if (recordLength < 0) recordLength = d.getDimension1();

            // detect element size if not ALPPHIC or ALL
            if (d.getTypeOfTransmission()!= null) {
              if (!d.getTypeOfTransmission().contentEquals("ALL")) {
                recordLength *= d.getTypeOfTransmissionAsType().getSize();
              }
              // getSize returns -1, if the size is unknown to the compiler
              // the test od sufficient record size must be done at runtime
              // indicate this which recordLength=0
              if (recordLength <0) {
                recordLength = 0;
              }
            }
            tfuInUserDation.add("tfusize", recordLength);
          }
        }

      }
    }

    return null;
  }

  @Override
  public ST visitType(SmallPearlParser.TypeContext ctx) {
    ST type = group.getInstanceOf("Type");

    if ( ctx.simple_type() != null) {
      if ( ctx.simple_type().type_bit() != null ) {
        type.add("name", "BIT");
      }
      else if ( ctx.simple_type().type_char() != null ) {
        type.add("name", "CHAR");
      }
      else if ( ctx.simple_type().type_fixed() != null ) {
        type.add("name", "FIXED");
      }
      else if ( ctx.simple_type().type_float() != null ) {
        type.add("name", "FLOAT");
      }
      else {
        throw new NotSupportedTypeException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
      }
    }
    else if ( ctx.type_realtime_object() != null) {
      type.add("name", ctx.type_realtime_object().getText());
    }
    else if ( ctx.typeTime() != null) {
      if ( ctx.typeTime().type_clock() != null ) {
        type.add("name", "CLOCK");
      }
      else if ( ctx.typeTime().type_duration() != null ) {
        type.add("name", "DURATION");
      }
      else {
        throw new NotSupportedTypeException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
      }
    }

    return type;
  }


  private ArrayList<String> getIdentifierDenotation(SmallPearlParser.IdentifierDenotationContext ctx) {
    ArrayList<String> identifierDenotationList = new ArrayList<String>();

    if (ctx != null) {
      for (int i = 0; i < ctx.ID().size(); i++) {
        identifierDenotationList.add(ctx.ID().get(i).toString());
      }
    }

    return identifierDenotationList;
  }
}
