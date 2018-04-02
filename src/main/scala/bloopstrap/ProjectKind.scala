package bloopstrap

sealed trait ProjectKind
case object PlainProject extends ProjectKind
case object TestProject extends ProjectKind
