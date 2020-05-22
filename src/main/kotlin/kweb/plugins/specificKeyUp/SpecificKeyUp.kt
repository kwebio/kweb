package kweb.plugins.specificKeyUp

import kweb.Element
import kweb.InputElement
import kweb.html.events.KeyboardEvent
import kweb.html.events.OnImmediateReceiver
import kweb.html.events.OnReceiver
import kweb.plugins.KwebPlugin

/**
 * @author sanity
 *
 * This plugin allows registering a listener on a specific key to avoid sending all `keyup` events
 * to the backend.
 */
class SpecificKeyUpPlugin : KwebPlugin()

private const val ENTER_PRESSED_EVENT_ATTACHED_FLAG = "enterPressedEventAttached"

fun InputElement.attachKeySpecificKeyupEvent(vararg keys: String) {
    require(keys.isNotEmpty()) { "You must supply at least one key" }
    require(ENTER_PRESSED_EVENT_ATTACHED_FLAG !in flags) { "KeySpecificKeyupEvent may only be attached once per element" }
    flags += ENTER_PRESSED_EVENT_ATTACHED_FLAG
    this.execute("""
        $jsExpression.addEventListener("keyup", function(origEvent) {
            var keys = [${keys.joinToString(separator = ",") { "\"$it\"" }}]
            if (keys.includes(origEvent.key)) {
                if (window.CustomEvent) {
                  var keySpecificKeyUpEvent = new CustomEvent('keySpecificKeyUpEvent');
                } else {
                  var keySpecificKeyUpEvent = document.createEvent('keySpecificKeyUpEvent');
                  enterPressedEvent.initCustomEvent('keySpecificKeyUpEvent', true, true, origEvent);
                }

                $jsExpression.dispatchEvent(keySpecificKeyUpEvent);
            }
        });
    """.trimIndent())
}

fun OnReceiver<Element>.keySpecificKeyup(callback: (event: KeyboardEvent) -> Unit): Element {
    require(source.flags.contains(ENTER_PRESSED_EVENT_ATTACHED_FLAG)) { "InputElement.attachKeySpecificKeyupEvent() must be called before listening for keySpecificKeyup" }
    return this.event("keySpecificKeyUpEvent", eventType = KeyboardEvent::class, callback = callback)
}

fun OnImmediateReceiver<Element>.keySpecificKeyup(callback: () -> Unit): Element {
    require(source.flags.contains(ENTER_PRESSED_EVENT_ATTACHED_FLAG)) { "InputElement.attachKeySpecificKeyupEvent() must be called before listening for keySpecificKeyup" }
    return event("keySpecificKeyUpEvent", callback = callback)
}