package ppl.dsl.forge
package templates
package ident

import java.io.PrintWriter
import scala.virtualization.lms.common._
import core._
import shared.{BaseGenPackages,BaseGenOps}
import Utilities._

trait LibGenPackages extends BaseGenPackages with BaseGenOps {  
  this: ForgeCodeGenIdent =>
  
  val IR: ForgeApplicationRunner with ForgeExp with ForgeOpsExp
  import IR._
 
  def emitApplicationRunner(opsGrps: List[DSLOps], stream: PrintWriter) {
    stream.println("trait " + dsl + "ApplicationInterpreter extends " + dsl + "Application with " + dsl+"Lib {")
    stream.println("  var args: Rep[Array[String]] = _")
    stream.println("  final def main(argsIn: Array[String]) {")
    stream.println("    this.args = argsIn")  
    stream.println("    main()")
    stream.println("  }")    
    stream.println()
    
    emitBlockComment("Dismabiguations for interpreter mode", stream, indent=2)
    for (opsGrp <- opsGrps if !isTpeClass(opsGrp.grp) && !isTpeClassInst(opsGrp.grp)) {
      for (o <- unique(opsGrp.ops) if (nameClashesUniversal(o).length > 1) && !o.args.exists(a => getHkTpe(a.tpe).name == "Var")) {        
        var prefix = ""
        if (o.style == infixMethod && !noInfix(o))
          prefix = "  override def infix_"
        else if (o.style == directMethod)
          prefix = "  override def "

        if (prefix != "") {
          stream.print(prefix + o.name + makeTpeParsWithBounds(o.tpePars))
          stream.print(makeOpArgsWithType(o))
          stream.print(makeOpImplicitArgsWithOverloadWithType(o))
          stream.println(" = " + makeOpMethodName(o) + makeTpePars(o.tpePars) + makeOpArgs(o) + makeOpImplicitArgsWithOverload(o))        
        }
      }      
    }
    
    // other required overrides for embedded controls
    stream.println("  override def __newVar[T](x: T) = super.__newVar(x)")    
    // TODO: this is ambiguous and unresolvable for some reason, even when supplying explicit arguments. possibly due to the thunks?
    // stream.println("  override def __whileDo(c: => Boolean, b: => Unit): Unit = super.__whileDo(c,b)")

    stream.println("}")
  }

  def emitDSLPackageDefinitions(opsGrps: List[DSLOps], stream: PrintWriter) {
    emitBlockComment("dsl library definition", stream)
    
    // base trait sets Rep[T] to T and mixes in the necessary portions of the front-end, without
    // bringing the abstract impls in scope, since they can cause a recursive loop in the library    
    stream.println("trait " + dsl + "Base extends Base with GenOverloadHack {")
    stream.println("  type Rep[+T] = T")
    stream.println("  protected def unit[T:Manifest](x: T) = x")
    stream.println()
    stream.println("}")    
    stream.println()
        
    // compiler ops mixes in an application ops with compiler only ops
    stream.println("trait " + dsl + "CompilerOps extends " + dsl + "Application")
    for (opsGrp <- opsGrps) {
      if (opsGrp.ops.exists(_.style == compilerMethod))
        stream.print(" with " + opsGrp.grp.name + "CompilerOps")          
    }      
    for (e <- Externs) {
      stream.print(" with " + e.opsGrp.grp.name + "CompilerOps")          
    }
    stream.println()
    stream.println()
    
    // library impl brings all of the library types in scope
    stream.println("trait " + dsl + "Lib extends " + dsl + "Base with " + dsl + "CompilerOps with " + dsl + "Classes {")
    stream.println("  this: " + dsl + "Application => ")
    stream.println()
    stream.println("  // override required due to mix-in")
    stream.println("  override type Rep[+T] = T")
    stream.println()
    emitBlockComment("dsl types", stream, indent=2)
    for (tpe <- Tpes) {
      if (!isForgePrimitiveType(tpe)) {
        stream.println("  def m_" + tpe.name + makeTpeParsWithBounds(tpe.tpePars) + " = manifest[" + quote(tpe) + "]")      
      }
    }        
    stream.println()
    stream.println("}")
  }  
}