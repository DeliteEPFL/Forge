package LOWERCASE_DSL_NAME.shared

import scala.annotation.unchecked.uncheckedVariance
import scala.reflect.{Manifest,SourceContext}
import scala.virtualization.lms.common._

trait RecordOps extends StructOps with ForgeArrayCompilerOps 
trait RecordCompilerOps extends RecordOps