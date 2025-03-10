package org.utbot.python.fuzzing.provider

import org.utbot.fuzzing.Routine
import org.utbot.fuzzing.Seed
import org.utbot.fuzzing.ValueProvider
import org.utbot.python.framework.api.python.PythonTree
import org.utbot.python.framework.api.python.util.pythonBytearrayClassId
import org.utbot.python.fuzzing.PythonFuzzedValue
import org.utbot.python.fuzzing.PythonMethodDescription
import org.utbot.python.newtyping.general.Type
import org.utbot.python.newtyping.pythonTypeName
import org.utbot.python.newtyping.pythonTypeRepresentation

object BytearrayValueProvider : ValueProvider<Type, PythonFuzzedValue, PythonMethodDescription> {
    override fun accept(type: Type): Boolean {
        return type.pythonTypeName() == "builtins.bytearray"
    }

    override fun generate(description: PythonMethodDescription, type: Type) = sequence {
        yield(Seed.Recursive(
            construct = Routine.Create(
                listOf(
                    description.pythonTypeStorage.pythonInt,
                )
            ) { v ->
                val value = v.first().tree as PythonTree.PrimitiveNode
                PythonFuzzedValue(
                    PythonTree.PrimitiveNode(
                        pythonBytearrayClassId,
                        "bytearray(${value.repr})"
                    ),
                    "%var% = ${type.pythonTypeRepresentation()}",
                )
            },
            empty = Routine.Empty { PythonFuzzedValue(
                PythonTree.PrimitiveNode(
                    pythonBytearrayClassId,
                    "bytearray()"
                ),
                "%var% = ${type.pythonTypeRepresentation()}"
            ) }
        ))
    }
}
