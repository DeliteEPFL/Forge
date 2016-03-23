package LOWERCASE_DSL_NAME.cdeep.ops

import scala.annotation.implicitNotFound
import scala.annotation.unchecked.uncheckedVariance

trait Base {
  @implicitNotFound("${T} is not a DSL type")
  type Typ[T]
  
  @implicitNotFound("${A} cannot be implicitly lifted to ${B}")
  type Lift[A,B]

  implicit def identLift[T:Typ]: Lift[T,T]
  implicit def lift[A,B](x: A)(implicit e: Lift[A,B]): B

  // type Rep[+T]
  // def unit[A:Manifest, B:Manifest](x: A): Rep[B]

  type Var[+T]
}

//TODO: this closely mirrors existing Expressions definitions, but probably should eventually subsume Manifest into Typ
trait BaseExp extends Base {
  
  //TODO: I'm not sure if it's better for T to be IR.Int or scala.Int
  //currently a bunch of Delite code assumes scala.Int, but this makes Typ and Ops definitions more complicated
  //best future resolution probably Exp[T:Typ] instead
  abstract class Exp[+T:Manifest] {
    def tp: Manifest[T @uncheckedVariance] = manifest[T]
  }

  // type Rep[+T] = Exp[T]

  case class Sym[+T:Manifest](id: Int) extends Exp[T] {
    override def toString = "x"+id
  }

  //inferior design to Reps: no longer enforcing that unwrapping IR.Int -> scala.Int, so need casts now
  case class Const[A:Manifest, +T:Manifest](x: A) extends Exp[T]

  abstract class Def[+T]

  var nVars = 0

  def fresh[T:Manifest] = { nVars += 1; Sym(nVars-1) }

  //note: the error messages are better when this is added explicitly, might be worth it since auto-generated anyway
  implicit def toAtom[T:Manifest](d: Def[T]): Exp[T] = {
    val e = fresh
    println(s"val $e = $d (${e.tp})")
    e
  }

  def unit[A:Manifest, B:Manifest](x: A): Exp[B] = Const[A,B](x)

  trait Typ[T] {
    def from(e: Exp[T]): T
    def to(x: T): Exp[T]
  }

  trait Lift[A,B] {
    def to(x: A): B
  }

  def identLift[T:Typ]: Lift[T,T] = new Lift[T,T]{ def to(x: T) = x }
  def lift[A,B](x: A)(implicit e: Lift[A,B]): B = e.to(x)

  def typ[T:Typ] = implicitly[Typ[T]]
}

trait Units extends Base {
  type Unit
  implicit def unitManifest: Manifest[Unit]
  implicit def unitTyp: Typ[Unit]
  implicit val unitLift: Lift[scala.Unit, Unit]
}

trait UnitsImpl extends BaseExp {
  case class Unit(e: Exp[Unit]) 
  val unitManifest = manifest[Unit]
  val unitTyp = new Typ[Unit] {
    def from(e: Exp[Unit]) = Unit(e)
    def to(x: Unit) = x.e
    override def toString = "Unit"
  }
  val unitLift: Lift[scala.Unit, Unit] = new Lift[scala.Unit, Unit] {
    def to(x: scala.Unit) = Unit(unit[scala.Unit, Unit](()))
  }
}