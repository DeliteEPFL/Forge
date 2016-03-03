package optiql.yy

// import ch.epfl.yinyang._
// import ch.epfl.yinyang.typetransformers._
// import scala.language.experimental.macros
// import scala.reflect.macros.blackbox.Context

import ch.epfl.yinyang.api._

// import java.io.{BufferedWriter, FileWriter, PrintWriter}
// import scala.reflect.{Manifest,SourceContext}
// import scala.virtualization.lms.common.{Base,BaseExp,EffectExp,BaseFatExp}
// import scala.virtualization.lms.common.{ScalaGenBase,ScalaGenEffect,ScalaGenFat,CudaGenFat,CGenFat}
// import scala.virtualization.lms.util._
// import scala.virtualization.lms.internal._
import optiql.shared._
import optiql.shared.ops._
// import optiql.compiler._
// import optiql.compiler.ops._
// import scala.tools.nsc.io._
// import scala.virtualization.lms.common.{Base,BaseExp,EffectExp,BaseFatExp}
// import scala.virtualization.lms.common.{ScalaGenBase,ScalaGenEffect,ScalaGenFat,CudaGenFat,CGenFat}
// import scala.virtualization.lms.util._
// import scala.virtualization.lms.internal._
// import ppl.delite.framework.{Config, DeliteApplication, ExpressionsOpt}
// import ppl.delite.framework.codegen.Target
// import ppl.delite.framework.codegen.scala.TargetScala
// import ppl.delite.framework.codegen.cuda.TargetCuda
// import ppl.delite.framework.codegen.cpp.TargetCpp
// import ppl.delite.framework.codegen.opencl.TargetOpenCL
// import ppl.delite.framework.ops._
// import ppl.delite.framework.datastructures._
// import ppl.delite.framework.codegen.delite.overrides._
// import ppl.delite.framework.transform._
// import ppl.delite.framework.ops.DeliteCollection
// import ppl.delite.framework.datastructures._
// import ppl.delite.framework.ops.{DeliteOpsExp, DeliteCollectionOpsExp}
// import ppl.delite.framework.Util._
// import ppl.delite.framework.codegen.restage.{DeliteCodeGenRestage,TargetRestage}
// import ppl.delite.framework.{DeliteInteractive, DeliteInteractiveRunner, DeliteRestageOps, DeliteRestageOpsExp, DeliteRestageRunner}
// import ppl.tests.scalatest._

import scala.virtualization.lms.common.{Base,BaseExp,EffectExp,BaseFatExp}
import scala.virtualization.lms.common.{ScalaGenBase,ScalaGenEffect,ScalaGenFat,CudaGenFat,CGenFat}
import scala.virtualization.lms.util._
import scala.virtualization.lms.internal._
import scala.virtualization.lms.common.{MiscOpsExp, StringOpsExp}

trait OptiQLYY
extends BaseYinYangManifest with FullyStaged
with CodeGenerator // prevent Match Error
with OptiQL
with OptiQLApplication
{

  implicit def implicitLift[T: Manifest]: LiftEvidence[T, Rep[T]] = new PolyLift[T]
  class PolyLift[T: Manifest] extends LiftEvidence[T, Rep[T]] {
   def lift(v: T) = unit(v) //Const(v)
   def hole(tpe: TypeRep[T], symbolId: Int): Rep[T] = ??? //how to access holetable from here? or what else should we do?
  }
  
  def compile[T: TypeRep, Ret](unstableHoleIds: Set[Int]): Ret = ???
  def generateCode(className: String, unstableHoleIds: Set[Int]): String = ???
}