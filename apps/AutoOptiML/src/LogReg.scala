import autooptiml.compiler._
import autooptiml.library._
import autooptiml.shared._

object LogRegCompiler extends AutoOptiMLApplicationCompiler with LogReg
object LogRegInterpreter extends AutoOptiMLApplicationInterpreter with LogReg

trait LogReg extends AutoOptiMLApplication {
  def print_usage = {
    println("Usage: LogReg <input matrix file> <output vector file>")
    exit(-1)
  }

  def main() = {
    if (args.length < 2) print_usage

    val x = readMatrix(args(0))
    val y = readVector(args(1)).t

    println("x.numRows: " + x.numRows)
    println("x.numCols: " + x.numCols)
    println("y.length:  " + y.length)

    tic()
    val theta = DenseVector.zeros(x.numCols)

    // gradient descent with logistic function
    val alpha = 1.0

    val w = untilconverged(theta, maxIter = 30) { cur =>
      val gradient = sum((0::x.numRows) { i =>
        x(i)*(y(i) - sigmoid(cur *:* x(i)))
      })

      // println("gradient: ")
      // gradient.pprint

      // alpha*gradient returns an inaccessible type when using implicits (instead of infix)
      val z = cur + gradient*alpha
      // println("next value (c): ")
      // z.pprint
      z
    }

    toc(w)
    println("w:")
    w.pprint
  }
}
