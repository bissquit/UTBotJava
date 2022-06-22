package org.utbot.engine.util.mockListeners
import org.utbot.engine.EngineController
import org.utbot.engine.MockStrategy
import org.utbot.engine.util.mockListeners.exceptions.ForceMockCancellationException

/**
 * Listener for mocker events in symbolic engine.
 * If forced mock happened, cancels the engine job.
 *
 * Supposed to be created only if Mockito is not installed.
 */
class ForceMockListener: MockListener {
    var forceMockHappened = false

    override fun onShouldMock(controller: EngineController, strategy: MockStrategy, shouldMock: Boolean) {
        // If force mocking happened
        if (shouldMock) {
            // Cancel engine job
            controller.job!!.cancel(ForceMockCancellationException())
            forceMockHappened = true
        }
    }
}