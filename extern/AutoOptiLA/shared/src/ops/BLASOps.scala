package autooptila.shared.ops

import scala.tools.nsc.io._
import scala.reflect.{Manifest,SourceContext}
import scala.virtualization.lms.common.{Base,BaseExp,EffectExp,BaseFatExp}
import scala.virtualization.lms.common.{ScalaGenBase,ScalaGenEffect,ScalaGenFat}
import scala.virtualization.lms.util._
import scala.virtualization.lms.internal._
import autooptila.shared._
import autooptila.shared.ops._
import autooptila.shared.typeclass._

// TODO: BLAS and LAPACK extern files should be moved to extern/OptiLA, and sourced from there.
trait BLASOps {
  lazy val useBLAS = System.getProperty("optila.use.blas", "false").toBoolean
}
trait BLASCompilerOps extends BLASOps
