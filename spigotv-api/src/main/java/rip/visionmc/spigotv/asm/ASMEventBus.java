package rip.visionmc.spigotv.asm;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ASMEventBus {
    private final Map<Class<? extends Event>, List<ListenerHandlerPair>> eventListeners = new ConcurrentHashMap<>();

    public void registerListener(Listener listener, Class<? extends Event> eventType, ASMEventHandler handler) {
        eventListeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                .add(new ListenerHandlerPair(listener, handler));
    }

    public void dispatchEvent(Event event) {
        Class<? extends Event> eventType = event.getClass();

        List<ListenerHandlerPair> listeners = eventListeners.get(eventType);

        if (listeners != null) {
            for (ListenerHandlerPair listenerHandlerPair : listeners) {
                Listener listener = listenerHandlerPair.getListener();
                ASMEventHandler handler = listenerHandlerPair.getHandler();

                handler.handle(event);
            }
        }
    }

    // Generate the event handler dynamically using ASM
    public ASMEventHandler generateHandler(Listener listener, Method method, Class<?> eventType) {
        return new ASMEventHandler() {
            @Override
            public void handle(Event event) {
                if (eventType.isAssignableFrom(event.getClass())) {
                    try {
                        // Ensure the method is accessible before invocation
                        method.setAccessible(true);

                        // Invoke the listener method
                        method.invoke(listener, event);
                    } catch (IllegalAccessException e) {
                        System.err.println("Failed to access method: " + method.getName());
                        e.printStackTrace();  // Handle IllegalAccessException
                    } catch (Exception e) {
                        System.err.println("Error invoking listener method: " + method.getName());
                        e.printStackTrace();  // Handle other exceptions
                    }
                }
            }
        };
    }
}
