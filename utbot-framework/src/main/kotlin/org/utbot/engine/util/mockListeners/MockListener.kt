package org.utbot.engine.util.mockListeners

import org.utbot.engine.EngineController
import org.utbot.engine.MockStrategy

/**
 * Listener that can be attached using [MockListenerController] to mocker in symbolic engine.
 */
interface MockListener {
    fun onShouldMock(controller: EngineController, strategy: MockStrategy, shouldMock: Boolean)
}
