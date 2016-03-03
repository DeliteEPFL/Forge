package yy

import ch.epfl.yinyang._
import ch.epfl.yinyang.typetransformers._
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

package object optiql {

  def optiqlyy[T](block: => T): T = macro implementations.liftRep[T]

  object implementations {
    def liftRep[T](c: Context)(block: c.Expr[T]): c.Expr[T] =
      YYTransformer[c.type, T](c)(
        "optiql.yy.OptiQLYY",
        new GenericTypeTransformer[c.type](c) {
          override val IRType = "Rep"
        },
        None, None,
        Map(
          "shallow" -> false,
          "virtualizeFunctions" -> false,
          "virtualizeVal" -> false,
          "debug" -> 1,
          "featureAnalysing" -> false,
          "ascriptionTransforming" -> false))(block)
  }
}